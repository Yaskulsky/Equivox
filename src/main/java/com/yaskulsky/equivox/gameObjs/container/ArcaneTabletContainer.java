package com.yaskulsky.equivox.gameObjs.container;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.yaskulsky.equivox.api.capabilities.IKnowledgeProvider;
import com.yaskulsky.equivox.api.capabilities.PECapabilities;
import com.yaskulsky.equivox.api.proxy.IEMCProxy;
import com.yaskulsky.equivox.config.EquivoxConfig;
import com.yaskulsky.equivox.gameObjs.container.inventory.TransmutationInventory;
import com.yaskulsky.equivox.gameObjs.container.slots.arcane.ArcaneCraftingSlot;
import com.yaskulsky.equivox.gameObjs.container.slots.arcane.ArcaneResultSlot;
import com.yaskulsky.equivox.gameObjs.container.slots.arcane.ArcaneTabletHelper;
import com.yaskulsky.equivox.gameObjs.container.slots.transmutation.SlotConsume;
import com.yaskulsky.equivox.gameObjs.container.slots.transmutation.SlotInput;
import com.yaskulsky.equivox.gameObjs.container.slots.transmutation.SlotLock;
import com.yaskulsky.equivox.gameObjs.container.slots.transmutation.SlotOutput;
import com.yaskulsky.equivox.gameObjs.container.slots.transmutation.SlotUnlearn;
import com.yaskulsky.equivox.gameObjs.items.Tome;
import com.yaskulsky.equivox.gameObjs.registries.PEContainerTypes;
import com.yaskulsky.equivox.network.packets.to_server.ArcaneTabletActionPKT;
import com.yaskulsky.equivox.network.packets.to_server.SearchUpdatePKT;
import com.yaskulsky.equivox.utils.ItemCapabilityHelper;
import com.yaskulsky.equivox.utils.ItemHelper;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Transmutation tablet + 3x3 crafting that can consume learned items / EMC.
 * Layout and behavior adapted from ProjectExpansion Arcane Tablet (MIT).
 */
public class ArcaneTabletContainer extends PEHandContainer {

	private static final int[] ROTATION_SLOTS = {0, 1, 2, 5, 8, 7, 6, 3};

	private static final int INPUT = 0;
	private static final int LOCK = 8;
	private static final int UNLEARN = 10;
	private static final int OUTPUT = 11;
	private static final int OUTPUT_COUNT = 16;
	private static final int PLAYER = 27;
	private static final int PLAYER_COUNT = 36;
	private static final int RESULT = 63;
	private static final int CRAFTING = 64;

	private final List<SlotInput> inputSlots = new ArrayList<>();
	public final TransmutationInventory transmutationInventory;
	private final Player player;
	private final IKnowledgeProvider provider;
	private TransientCraftingContainer craftSlots;
	private ResultContainer resultSlots;
	private SlotUnlearn unlearn;
	private int resultSlotIndex = RESULT;

	public boolean isCrafting;
	public boolean skipRefill;

	public static ArcaneTabletContainer fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
		return new ArcaneTabletContainer(windowId, playerInv, buf.readEnum(InteractionHand.class), buf.readByte());
	}

	public ArcaneTabletContainer(int windowId, Inventory playerInv, InteractionHand hand, int selected) {
		super(PEContainerTypes.ARCANE_TABLET_CONTAINER, windowId, playerInv, hand, selected);
		this.player = playerInv.player;
		this.provider = Objects.requireNonNull(player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY));
		this.transmutationInventory = new TransmutationInventory(player);
		initSlots();
	}

	private void initSlots() {
		addSlot(new SlotInput(transmutationInventory, INPUT, 30, 21));
		addSlot(new SlotInput(transmutationInventory, 1, 130, 21));
		addSlot(new SlotInput(transmutationInventory, 2, 8, 43));
		addSlot(new SlotInput(transmutationInventory, 3, 152, 43));
		addSlot(new SlotInput(transmutationInventory, 4, 8, 94));
		addSlot(new SlotInput(transmutationInventory, 5, 152, 94));
		addSlot(new SlotInput(transmutationInventory, 6, 30, 115));
		addSlot(new SlotInput(transmutationInventory, 7, 130, 115));
		addSlot(new SlotLock(transmutationInventory, LOCK, 80, 68));
		addSlot(new SlotConsume(transmutationInventory, 9, 152, 115));
		addSlot(unlearn = new SlotUnlearn(transmutationInventory, UNLEARN, 8, 115));
		addSlot(new SlotOutput(transmutationInventory, OUTPUT, 80, 20));
		addSlot(new SlotOutput(transmutationInventory, 12, 105, 26));
		addSlot(new SlotOutput(transmutationInventory, 13, 55, 26));
		addSlot(new SlotOutput(transmutationInventory, 14, 123, 44));
		addSlot(new SlotOutput(transmutationInventory, 15, 37, 44));
		addSlot(new SlotOutput(transmutationInventory, 16, 128, 68));
		addSlot(new SlotOutput(transmutationInventory, 17, 32, 68));
		addSlot(new SlotOutput(transmutationInventory, 18, 123, 92));
		addSlot(new SlotOutput(transmutationInventory, 19, 37, 92));
		addSlot(new SlotOutput(transmutationInventory, 20, 105, 110));
		addSlot(new SlotOutput(transmutationInventory, 21, 55, 110));
		addSlot(new SlotOutput(transmutationInventory, 22, 80, 116));
		addSlot(new SlotOutput(transmutationInventory, 23, 60, 48));
		addSlot(new SlotOutput(transmutationInventory, 24, 100, 48));
		addSlot(new SlotOutput(transmutationInventory, 25, 60, 88));
		addSlot(new SlotOutput(transmutationInventory, 26, 100, 88));
		addPlayerInventory(8, 135);

		this.craftSlots = new TransientCraftingContainer(this, 3, 3);
		this.resultSlots = new ResultContainer();
		resultSlotIndex = slots.size();
		addSlot(new ArcaneResultSlot(player, craftSlots, resultSlots, this, 0, -23, 75));
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				addSlot(new ArcaneCraftingSlot(craftSlots, col + row * 3, -59 + col * 18, 17 + row * 18));
			}
		}
	}

	@NotNull
	@Override
	protected Slot addSlot(@NotNull Slot slot) {
		if (slot instanceof SlotInput input) {
			inputSlots.add(input);
		}
		return super.addSlot(slot);
	}

	public IKnowledgeProvider getProvider() {
		return provider;
	}

	public int getResultSlotIndex() {
		return resultSlotIndex;
	}

	public int getCraftingSlotStart() {
		return resultSlotIndex + 1;
	}

	@Override
	public void removed(@NotNull Player player) {
		super.removed(player);
		boolean disconnect = !player.isAlive() || player instanceof ServerPlayer serverPlayer && serverPlayer.hasDisconnected();
		if (disconnect) {
			player.drop(unlearn.getItem(), false);
			for (ItemStack stack : craftSlots.getItems()) {
				player.drop(stack, false);
			}
		} else {
			ArcaneTabletHelper.returnToInventoryOrEmc(playerInv, player, provider, unlearn.getItem(), true);
			unlearn.set(ItemStack.EMPTY);
			for (int i = 0; i < craftSlots.getContainerSize(); i++) {
				ItemStack stack = craftSlots.getItem(i);
				if (!stack.isEmpty()) {
					ArcaneTabletHelper.returnToInventoryOrEmc(playerInv, player, provider, stack, true);
					craftSlots.setItem(i, ItemStack.EMPTY);
				}
			}
		}
	}

	@NotNull
	@Override
	public ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
		Slot current = tryGetSlot(slotIndex);
		if (current instanceof ArcaneCraftingSlot && current.hasItem()) {
			ItemStack stack = current.getItem().copy();
			moveItemStackTo(stack, PLAYER, PLAYER + PLAYER_COUNT, true);
			current.set(ItemStack.EMPTY);
			return ItemStack.EMPTY;
		}
		if (current instanceof ArcaneResultSlot || slotIndex == resultSlotIndex) {
			if (current != null && current.hasItem()) {
				ItemStack result = current.getItem();
				ItemStack copy = result.copy();
				if (!moveItemStackTo(result, PLAYER, PLAYER + PLAYER_COUNT, true)) {
					return ItemStack.EMPTY;
				}
				current.onQuickCraft(result, copy);
				if (result.isEmpty()) {
					current.set(ItemStack.EMPTY);
				} else {
					current.setChanged();
				}
				if (result.getCount() == copy.getCount()) {
					return ItemStack.EMPTY;
				}
				current.onTake(player, result);
				return copy;
			}
		}
		if ((slotIndex >= INPUT && slotIndex < LOCK) || slotIndex == LOCK || slotIndex == UNLEARN) {
			return super.quickMoveStack(player, slotIndex);
		}
		if (current == null || !current.hasItem()) {
			return ItemStack.EMPTY;
		}
		if (slotIndex >= OUTPUT && slotIndex < OUTPUT + OUTPUT_COUNT) {
			ItemStack stack = current.getItem().copy();
			long itemEmc = IEMCProxy.INSTANCE.getValue(stack);
			if (itemEmc > 0) {
				stack.setCount(stack.getMaxStackSize());
				int itemsRoomFor = stack.getCount() - ItemHelper.simulateFit(ItemHelper.getInventoryStacks(player.getInventory()), stack);
				if (itemsRoomFor == 1) {
					long availableEMC = transmutationInventory.getAvailableEmcAsLong();
					if (itemEmc > availableEMC) {
						return ItemStack.EMPTY;
					}
					if (transmutationInventory.isServer()) {
						transmutationInventory.removeEmc(BigInteger.valueOf(itemEmc));
					}
					stack.setCount(1);
					ItemHandlerHelper.insertItemStacked(ItemCapabilityHelper.getPlayerInventory(player), stack, false);
				} else if (itemsRoomFor > 1) {
					BigInteger availableEMC = transmutationInventory.getAvailableEmc();
					BigInteger emc = BigInteger.valueOf(itemEmc);
					BigInteger totalEmc = emc.multiply(BigInteger.valueOf(itemsRoomFor));
					if (totalEmc.compareTo(availableEMC) > 0) {
						BigInteger numOperations = availableEMC.divide(emc);
						itemsRoomFor = numOperations.intValue();
						totalEmc = emc.multiply(numOperations);
						if (itemsRoomFor <= 0) {
							return ItemStack.EMPTY;
						}
					}
					if (transmutationInventory.isServer()) {
						transmutationInventory.removeEmc(totalEmc);
					}
					stack.setCount(itemsRoomFor);
					ItemHandlerHelper.insertItemStacked(ItemCapabilityHelper.getPlayerInventory(player), stack, false);
				}
			}
		} else if (slotIndex >= PLAYER && slotIndex < CRAFTING) {
			ItemStack slotStack = current.getItem();
			ItemStack stackToInsert = slotStack;
			if (stackToInsert.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY) != null) {
				stackToInsert = insertItem(inputSlots, stackToInsert, true);
				if (slotStack.getCount() == stackToInsert.getCount()) {
					stackToInsert = insertItem(inputSlots, stackToInsert, false);
				}
				if (slotStack.getCount() != stackToInsert.getCount()) {
					return transferSuccess(current, player, slotStack, stackToInsert);
				}
			}
			long emc = IEMCProxy.INSTANCE.getSellValue(stackToInsert);
			if (emc > 0 || stackToInsert.getItem() instanceof Tome) {
				if (transmutationInventory.isServer()) {
					transmutationInventory.handleKnowledge(stackToInsert);
					transmutationInventory.addEmc(BigInteger.valueOf(emc).multiply(BigInteger.valueOf(stackToInsert.getCount())));
				}
				current.set(ItemStack.EMPTY);
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void clickPostValidate(int slotIndex, int dragType, @NotNull ContainerInput clickType, @NotNull Player player) {
		if (player.level().isClientSide() && transmutationInventory.getHandlerForSlot(slotIndex) == transmutationInventory.outputs) {
			Slot slot = tryGetSlot(slotIndex);
			if (slot != null) {
				ClientPacketDistributor.sendToServer(new SearchUpdatePKT(transmutationInventory.getIndexFromSlot(slotIndex), slot.getItem()));
			}
		}
		super.clickPostValidate(slotIndex, dragType, clickType, player);
	}

	@Override
	public boolean canDragTo(@NotNull Slot slot) {
		return !(slot instanceof SlotConsume || slot instanceof SlotUnlearn || slot instanceof SlotInput
				|| slot instanceof SlotLock || slot instanceof SlotOutput || slot instanceof ArcaneResultSlot);
	}

	@Override
	public void clicked(int slotId, int dragType, @NotNull ContainerInput clickType, @NotNull Player player) {
		if (clickType == ContainerInput.QUICK_MOVE) {
			skipRefill = true;
		}
		super.clicked(slotId, dragType, clickType, player);
		if (clickType == ContainerInput.QUICK_MOVE) {
			skipRefill = false;
		}
	}

	@Override
	public void slotsChanged(@NotNull Container container) {
		slotChangedCraftingGrid(this, player.level(), player, craftSlots, resultSlots);
		super.slotsChanged(container);
	}

	protected static void slotChangedCraftingGrid(ArcaneTabletContainer menu, Level level, Player player,
			CraftingContainer crafting, ResultContainer result) {
		if (level.isClientSide()) {
			return;
		}
		CraftingInput input = CraftingInput.of(3, 3, crafting.getItems());
		ServerPlayer serverPlayer = (ServerPlayer) player;
		ItemStack stack = ItemStack.EMPTY;
		Optional<RecipeHolder<CraftingRecipe>> optional = Objects.requireNonNull(level.getServer())
				.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
		if (optional.isPresent()) {
			RecipeHolder<CraftingRecipe> recipe = optional.get();
			stack = recipe.value().assemble(input);
			result.setRecipeUsed(recipe);
		}
		result.setItem(0, stack);
		menu.setRemoteSlot(menu.resultSlotIndex, stack);
		serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), menu.resultSlotIndex, stack));
	}

	@Override
	public boolean canTakeItemForPickAll(@NotNull ItemStack stack, Slot slot) {
		return slot.container != resultSlots && super.canTakeItemForPickAll(stack, slot);
	}

	public void onRecipeTransfer(List<List<ItemStack>> recipe, boolean transferAll) {
		clearCrafting(false);
		fillCraftingSlots(recipe, transferAll);
	}

	public void fillCraftingSlots(List<List<ItemStack>> recipe, boolean transferAll) {
		int max = Math.min(recipe.size(), craftSlots.getContainerSize());
		transferItems(recipe, max);
		if (transferAll) {
			for (int i = 0; i < 63; i++) {
				transferItems(recipe, max);
			}
		}
		if (player instanceof ServerPlayer serverPlayer) {
			provider.syncEmc(serverPlayer);
		}
		slotChangedCraftingGrid(this, player.level(), player, craftSlots, resultSlots);
	}

	private boolean transferFromTablet(int slot, List<ItemStack> possibilities) {
		List<ItemStack> sorted = new ArrayList<>(possibilities);
		sorted.sort(Comparator.comparingLong(IEMCProxy.INSTANCE::getValue));
		for (ItemStack stack : sorted) {
			ItemStack cleaned = ArcaneTabletHelper.cleanStack(stack);
			if (!provider.hasKnowledge(cleaned)) {
				continue;
			}
			long value = IEMCProxy.INSTANCE.getValue(cleaned);
			if (value <= 0 || provider.getEmc().compareTo(BigInteger.valueOf(value)) < 0) {
				continue;
			}
			ItemStack slotItem = craftSlots.getItem(slot);
			if (slotItem.isEmpty()) {
				craftSlots.setItem(slot, cleaned);
			} else if (slotItem.getCount() < slotItem.getMaxStackSize()
					&& ArcaneTabletHelper.areStacksEqual(slotItem, cleaned)) {
				slotItem.grow(1);
			} else {
				continue;
			}
			provider.setEmc(provider.getEmc().subtract(BigInteger.valueOf(value)));
			return true;
		}
		return false;
	}

	private boolean transferFromInventory(int slot, List<ItemStack> possibilities) {
		for (ItemStack possibility : possibilities) {
			ItemStack cleaned = ArcaneTabletHelper.cleanStack(possibility);
			for (int j = 0; j < playerInv.getContainerSize(); j++) {
				ItemStack stack = playerInv.getItem(j);
				if (!ArcaneTabletHelper.areStacksEqual(cleaned, ArcaneTabletHelper.cleanStack(stack))) {
					continue;
				}
				ItemStack slotItem = craftSlots.getItem(slot);
				if (slotItem.isEmpty()) {
					craftSlots.setItem(slot, stack.copyWithCount(1));
				} else if (slotItem.getCount() < slotItem.getMaxStackSize()
						&& ArcaneTabletHelper.areStacksEqual(slotItem, stack)) {
					slotItem.grow(1);
				} else {
					continue;
				}
				stack.shrink(1);
				if (stack.isEmpty()) {
					playerInv.setItem(j, ItemStack.EMPTY);
				}
				return true;
			}
		}
		return false;
	}

	public void transferItems(List<List<ItemStack>> recipe, int max) {
		for (int i = 0; i < max; i++) {
			if (recipe.get(i) != null && !recipe.get(i).isEmpty()) {
				transferFromInventory(i, recipe.get(i));
			}
		}
		for (int i = 0; i < max; i++) {
			if (recipe.get(i) != null && !recipe.get(i).isEmpty()) {
				transferFromTablet(i, recipe.get(i));
			}
		}
	}

	/** @apiNote server only */
	public void clearCrafting(boolean force) {
		boolean emcUpdate = false;
		for (int i = 0; i < craftSlots.getContainerSize(); i++) {
			ItemStack stack = craftSlots.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (EquivoxConfig.server.difficulty.covalenceLoss.get() >= 1.0D && IEMCProxy.INSTANCE.hasValue(stack)
					&& ArcaneTabletHelper.tryLearnAndConvertToEmc(player, provider, stack)) {
				emcUpdate = true;
				craftSlots.setItem(i, ItemStack.EMPTY);
				continue;
			}
			craftSlots.setItem(i, ArcaneTabletHelper.returnToInventory(playerInv, player, stack, force));
		}
		if (emcUpdate && player instanceof ServerPlayer serverPlayer) {
			provider.syncEmc(serverPlayer);
		}
		slotsChanged(craftSlots);
		craftSlots.setChanged();
	}

	/** @apiNote server only */
	public void rotateCrafting(boolean clockwise) {
		ItemStack[] stacks = new ItemStack[ROTATION_SLOTS.length];
		if (clockwise) {
			for (int i = 0; i < ROTATION_SLOTS.length; i++) {
				int j = i - 1;
				if (j < 0) {
					j = ROTATION_SLOTS.length - 1;
				}
				stacks[i] = craftSlots.getItem(ROTATION_SLOTS[j]);
			}
		} else {
			for (int i = 0; i < ROTATION_SLOTS.length; i++) {
				stacks[i] = craftSlots.getItem(ROTATION_SLOTS[(i + 1) % ROTATION_SLOTS.length]);
			}
		}
		for (int i = 0; i < ROTATION_SLOTS.length; i++) {
			craftSlots.setItem(ROTATION_SLOTS[i], stacks[i]);
		}
		slotsChanged(craftSlots);
		craftSlots.setChanged();
	}

	/** @apiNote server only */
	public void balanceCrafting() {
		ArrayListMultimap<String, ItemStack> map = ArrayListMultimap.create();
		Multiset<String> itemCount = HashMultiset.create();
		for (int i = 0; i < craftSlots.getContainerSize(); i++) {
			ItemStack stack = craftSlots.getItem(i);
			if (!stack.isEmpty() && stack.getMaxStackSize() > 1) {
				String key = stackKey(stack);
				map.put(key, stack);
				itemCount.add(key, stack.getCount());
			}
		}
		for (String key : map.keySet()) {
			List<ItemStack> list = map.get(key);
			int totalCount = itemCount.count(key);
			int countPerStack = totalCount / list.size();
			int restCount = totalCount % list.size();
			for (ItemStack stack : list) {
				stack.setCount(countPerStack);
			}
			int idx = 0;
			while (restCount > 0) {
				ItemStack stack = list.get(idx);
				if (stack.getCount() < stack.getMaxStackSize()) {
					stack.grow(1);
					restCount--;
				}
				idx++;
				if (idx >= list.size()) {
					idx = 0;
				}
			}
		}
		slotsChanged(craftSlots);
		craftSlots.setChanged();
	}

	/** @apiNote server only */
	public void spreadCrafting() {
		while (true) {
			ItemStack biggestStack = null;
			int biggestSize = 1;
			for (int i = 0; i < craftSlots.getContainerSize(); i++) {
				ItemStack stack = craftSlots.getItem(i);
				if (!stack.isEmpty() && stack.getCount() > biggestSize) {
					biggestStack = stack;
					biggestSize = stack.getCount();
				}
			}
			if (biggestStack == null) {
				return;
			}
			boolean emptyBiggestSlot = false;
			for (int i = 0; i < craftSlots.getContainerSize(); i++) {
				ItemStack stack = craftSlots.getItem(i);
				if (stack.isEmpty()) {
					if (biggestStack.getCount() > 1) {
						craftSlots.setItem(i, biggestStack.split(1));
					} else {
						emptyBiggestSlot = true;
					}
				}
			}
			if (!emptyBiggestSlot) {
				break;
			}
		}
		balanceCrafting();
	}

	private static String stackKey(ItemStack stack) {
		String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
		DataComponentPatch patch = stack.getComponentsPatch();
		return id + "|" + patch;
	}

	/** @apiNote client only */
	public void sendAction(ArcaneTabletActionPKT.Action action) {
		ClientPacketDistributor.sendToServer(new ArcaneTabletActionPKT(action));
	}
}
