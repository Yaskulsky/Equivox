package com.yaskulsky.equivox.gameObjs.container;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.yaskulsky.equivox.api.capabilities.IKnowledgeProvider;
import com.yaskulsky.equivox.api.capabilities.PECapabilities;
import com.yaskulsky.equivox.api.proxy.IEMCProxy;
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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Transmutation tablet + 3x3 crafting that can consume learned items / EMC.
 * Layout and behavior adapted from ProjectExpansion Arcane Tablet (MIT).
 */
public class ArcaneTabletContainer extends PEHandContainer implements IArcaneCraftingMenu {

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

	@Override
	public Player getCraftingPlayer() {
		return player;
	}

	@Override
	public Inventory getCraftingPlayerInventory() {
		return playerInv;
	}

	@Override
	public TransmutationInventory transmutationInventory() {
		return transmutationInventory;
	}

	@Override
	public TransientCraftingContainer getCraftSlots() {
		return craftSlots;
	}

	@Override
	public ResultContainer getResultSlots() {
		return resultSlots;
	}

	@Override
	public boolean isSkipRefill() {
		return skipRefill;
	}

	@Override
	public void setSkipRefill(boolean skip) {
		this.skipRefill = skip;
	}

	@Override
	public void setCrafting(boolean crafting) {
		this.isCrafting = crafting;
	}

	@Override
	public AbstractContainerMenu asMenu() {
		return this;
	}

	@Override
	public void applyCraftingResult(ItemStack stack) {
		setRemoteSlot(resultSlotIndex, stack);
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), resultSlotIndex, stack));
		}
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
		if (player.level().isClientSide() && slotIndex <= 26 && transmutationInventory.getHandlerForSlot(slotIndex) == transmutationInventory.outputs) {
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
		ArcaneCraftingLogic.slotChangedCraftingGrid(this);
		super.slotsChanged(container);
	}

	@Override
	public boolean canTakeItemForPickAll(@NotNull ItemStack stack, Slot slot) {
		return slot.container != resultSlots && super.canTakeItemForPickAll(stack, slot);
	}

	/** @apiNote client only */
	public void sendAction(ArcaneTabletActionPKT.Action action) {
		ClientPacketDistributor.sendToServer(new ArcaneTabletActionPKT(action));
	}
}
