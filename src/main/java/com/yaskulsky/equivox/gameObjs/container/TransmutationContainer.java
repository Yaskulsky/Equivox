package com.yaskulsky.equivox.gameObjs.container;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
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

public class TransmutationContainer extends PEHandContainer implements IArcaneCraftingMenu {

	private static final int PLAYER = 27;
	private static final int PLAYER_COUNT = 36;
	private static final int CRAFTING = 64;

	private final List<SlotInput> inputSlots = new ArrayList<>();
	public final TransmutationInventory transmutationInventory;
	private final Player player;
	private final IKnowledgeProvider provider;
	private TransientCraftingContainer craftSlots;
	private ResultContainer resultSlots;
	private SlotUnlearn unlearn;
	private int resultSlotIndex = 63;
	public boolean isCrafting;
	public boolean skipRefill;

	public static TransmutationContainer fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
		if (buf.readBoolean()) {
			return new TransmutationContainer(windowId, playerInv, buf.readEnum(InteractionHand.class), buf.readByte());
		}
		return new TransmutationContainer(windowId, playerInv);
	}

	public TransmutationContainer(int windowId, Inventory playerInv) {
		super(PEContainerTypes.TRANSMUTATION_CONTAINER, windowId, playerInv, null, 0);
		this.player = playerInv.player;
		this.transmutationInventory = new TransmutationInventory(this.player);
		this.provider = this.transmutationInventory.provider;
		initSlots();
	}

	public TransmutationContainer(int windowId, Inventory playerInv, InteractionHand hand, int selected) {
		super(PEContainerTypes.TRANSMUTATION_CONTAINER, windowId, playerInv, hand, selected);
		this.player = playerInv.player;
		this.transmutationInventory = new TransmutationInventory(this.player);
		this.provider = this.transmutationInventory.provider;
		initSlots();
	}

	private void initSlots() {
		this.addSlot(new SlotInput(transmutationInventory, 0, 43, 23));
		this.addSlot(new SlotInput(transmutationInventory, 1, 34, 41));
		this.addSlot(new SlotInput(transmutationInventory, 2, 52, 41));
		this.addSlot(new SlotInput(transmutationInventory, 3, 16, 50));
		this.addSlot(new SlotInput(transmutationInventory, 4, 70, 50));
		this.addSlot(new SlotInput(transmutationInventory, 5, 34, 59));
		this.addSlot(new SlotInput(transmutationInventory, 6, 52, 59));
		this.addSlot(new SlotInput(transmutationInventory, 7, 43, 77));
		this.addSlot(new SlotLock(transmutationInventory, 8, 158, 50));
		this.addSlot(new SlotConsume(transmutationInventory, 9, 107, 97));
		this.addSlot(unlearn = new SlotUnlearn(transmutationInventory, 10, 89, 97));
		this.addSlot(new SlotOutput(transmutationInventory, 11, 158, 9));
		this.addSlot(new SlotOutput(transmutationInventory, 12, 176, 13));
		this.addSlot(new SlotOutput(transmutationInventory, 13, 193, 30));
		this.addSlot(new SlotOutput(transmutationInventory, 14, 199, 50));
		this.addSlot(new SlotOutput(transmutationInventory, 15, 193, 70));
		this.addSlot(new SlotOutput(transmutationInventory, 16, 176, 87));
		this.addSlot(new SlotOutput(transmutationInventory, 17, 158, 91));
		this.addSlot(new SlotOutput(transmutationInventory, 18, 140, 87));
		this.addSlot(new SlotOutput(transmutationInventory, 19, 123, 70));
		this.addSlot(new SlotOutput(transmutationInventory, 20, 116, 50));
		this.addSlot(new SlotOutput(transmutationInventory, 21, 123, 30));
		this.addSlot(new SlotOutput(transmutationInventory, 22, 140, 13));
		this.addSlot(new SlotOutput(transmutationInventory, 23, 158, 31));
		this.addSlot(new SlotOutput(transmutationInventory, 24, 177, 50));
		this.addSlot(new SlotOutput(transmutationInventory, 25, 158, 69));
		this.addSlot(new SlotOutput(transmutationInventory, 26, 139, 50));
		addPlayerInventory(35, 117);

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

	@Override
	public Player getCraftingPlayer() {
		return player;
	}

	@Override
	public Inventory getCraftingPlayerInventory() {
		return playerInv;
	}

	@Override
	public IKnowledgeProvider getProvider() {
		return provider;
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
	public int getResultSlotIndex() {
		return resultSlotIndex;
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

	/** @apiNote client only */
	public void sendAction(ArcaneTabletActionPKT.Action action) {
		ClientPacketDistributor.sendToServer(new ArcaneTabletActionPKT(action));
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
			player.getInventory().placeItemBackInInventory(unlearn.getItem());
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
		Slot currentSlot = tryGetSlot(slotIndex);
		if (currentSlot instanceof ArcaneCraftingSlot && currentSlot.hasItem()) {
			ItemStack stack = currentSlot.getItem().copy();
			moveItemStackTo(stack, PLAYER, PLAYER + PLAYER_COUNT, true);
			currentSlot.set(ItemStack.EMPTY);
			return ItemStack.EMPTY;
		}
		if (currentSlot instanceof ArcaneResultSlot || slotIndex == resultSlotIndex) {
			if (currentSlot != null && currentSlot.hasItem()) {
				ItemStack result = currentSlot.getItem();
				ItemStack copy = result.copy();
				if (!moveItemStackTo(result, PLAYER, PLAYER + PLAYER_COUNT, true)) {
					return ItemStack.EMPTY;
				}
				currentSlot.onQuickCraft(result, copy);
				if (result.isEmpty()) {
					currentSlot.set(ItemStack.EMPTY);
				} else {
					currentSlot.setChanged();
				}
				if (result.getCount() == copy.getCount()) {
					return ItemStack.EMPTY;
				}
				currentSlot.onTake(player, result);
				return copy;
			}
		}
		if (slotIndex < 9 || slotIndex == 10) {
			return super.quickMoveStack(player, slotIndex);
		}
		if (currentSlot == null || !currentSlot.hasItem()) {
			return ItemStack.EMPTY;
		}
		if (slotIndex >= 11 && slotIndex <= 26) {
			ItemStack stack = currentSlot.getItem().copy();
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
			ItemStack slotStack = currentSlot.getItem();
			ItemStack stackToInsert = slotStack;
			if (stackToInsert.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY) != null) {
				stackToInsert = insertItem(inputSlots, stackToInsert, true);
				if (slotStack.getCount() == stackToInsert.getCount()) {
					stackToInsert = insertItem(inputSlots, stackToInsert, false);
				}
				if (slotStack.getCount() != stackToInsert.getCount()) {
					return transferSuccess(currentSlot, player, slotStack, stackToInsert);
				}
			}
			long emc = IEMCProxy.INSTANCE.getSellValue(stackToInsert);
			if (emc > 0 || stackToInsert.getItem() instanceof Tome) {
				if (transmutationInventory.isServer()) {
					BigInteger emcBigInt = BigInteger.valueOf(emc);
					transmutationInventory.handleKnowledge(stackToInsert);
					transmutationInventory.addEmc(emcBigInt.multiply(BigInteger.valueOf(stackToInsert.getCount())));
				}
				currentSlot.set(ItemStack.EMPTY);
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
}
