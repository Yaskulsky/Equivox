package moze_intel.projecte.gameObjs.block_entities;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.event.PlayerAttemptCondenserSetEvent;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.gameObjs.container.CondenserContainer;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import moze_intel.projecte.utils.LegacyItemHandlerResourceHandler;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CondenserBlockEntity extends EmcChestBlockEntity {

	public static final ICapabilityProvider<CondenserBlockEntity, @Nullable Direction, ResourceHandler<ItemResource>> INVENTORY_PROVIDER = (condenser, side) -> LegacyItemHandlerResourceHandler.of(condenser.automationInventory);

	protected final ItemStackHandler inputInventory = createInput();
	private final ItemStackHandler outputInventory = createOutput();
	@Nullable
	private ItemInfo lockInfo;
	private boolean isAcceptingEmc;
	public long displayEmc;
	public long requiredEmc;
	//Start at one less than actual just to ensure we run initially after loading
	private int loadIndex = EMCMappingHandler.getLoadIndex() - 1;
	private final IItemHandler automationInventory;

	public CondenserBlockEntity(BlockPos pos, BlockState state) {
		this(PEBlockEntityTypes.CONDENSER, pos, state);
	}

	protected CondenserBlockEntity(BlockEntityTypeRegistryObject<? extends CondenserBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		this.automationInventory = createAutomationInventory();
	}

	@Override
	protected boolean canAcceptEmc() {
		return isAcceptingEmc;
	}

	@Override
	protected boolean canProvideEmc() {
		return false;
	}

	@Nullable
	public final ItemInfo getLockInfo() {
		if (requiredEmc == 0) {
			if (level == null || !level.isClientSide()) {
				//If the lock doesn't have EMC don't tell the client it is there
				return null;
			}
		}
		return lockInfo;
	}

	public ItemStackHandler getInput() {
		return inputInventory;
	}

	public ItemStackHandler getOutput() {
		return outputInventory;
	}

	protected ItemStackHandler createInput() {
		return new StackHandler(91);
	}

	protected ItemStackHandler createOutput() {
		return inputInventory;
	}

	@NotNull
	protected IItemHandler createAutomationInventory() {
		return new WrappedItemHandler(inputInventory, WrappedItemHandler.WriteMode.IN_OUT) {
			@NotNull
			@Override
			public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
				return SlotPredicates.HAS_EMC.test(stack) && !isStackEqualToLock(stack) ? super.insertItem(slot, stack, simulate) : stack;
			}

			@NotNull
			@Override
			public ItemStack extractItem(int slot, int max, boolean simulate) {
				return isStackEqualToLock(getStackInSlot(slot)) ? super.extractItem(slot, max, simulate) : ItemStack.EMPTY;
			}
		};
	}

	public static void tickServer(Level level, BlockPos pos, BlockState state, CondenserBlockEntity condenser) {
		condenser.checkLockAndUpdate(false);
		condenser.displayEmc = condenser.getStoredEmc();
		if (condenser.getLockInfo() != null) {
			condenser.condense();
		}
		condenser.updateComparators(level, pos);
	}

	private void checkLockAndUpdate(boolean force) {
		if (!force && loadIndex == EMCMappingHandler.getLoadIndex()) {
			//Only update if we are forcing it or are on a different load index
			return;
		}
		loadIndex = EMCMappingHandler.getLoadIndex();
		if (lockInfo != null) {
			long lockEmc = IEMCProxy.INSTANCE.getValue(lockInfo);
			if (lockEmc > 0) {
				if (requiredEmc != lockEmc) {
					requiredEmc = lockEmc;
					this.isAcceptingEmc = true;
				}
				return;
			}
			//Don't reset the lockInfo just because it has no EMC, as if a reload makes it have EMC again
			// then we want to allow it to happen again
		}
		displayEmc = 0;
		requiredEmc = 0;
		this.isAcceptingEmc = false;
	}

	protected void condense() {
		for (int i = 0, slots = inputInventory.getSlots(); i < slots; i++) {
			ItemStack stack = inputInventory.getStackInSlot(i);
			if (!stack.isEmpty() && !isStackEqualToLock(stack)) {
				inputInventory.extractItem(i, 1, false);
				forceInsertEmc(IEMCProxy.INSTANCE.getSellValue(stack), EmcAction.EXECUTE);
				break;
			}
		}
		if (this.getStoredEmc() >= requiredEmc && this.hasSpace()) {
			forceExtractEmc(requiredEmc, EmcAction.EXECUTE);
			pushStack();
		}
	}

	protected final void pushStack() {
		ItemInfo lockInfo = getLockInfo();
		if (lockInfo != null) {
			ItemHandlerHelper.insertItemStacked(outputInventory, lockInfo.createStack(), false);
		}
	}

	protected boolean hasSpace() {
		for (int i = 0, slots = outputInventory.getSlots(); i < slots; i++) {
			ItemStack stack = outputInventory.getStackInSlot(i);
			if (stack.isEmpty() || (isStackEqualToLock(stack) && stack.getCount() < stack.getMaxStackSize())) {
				return true;
			}
		}
		return false;
	}

	public boolean isStackEqualToLock(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		ItemInfo lockInfo = getLockInfo();
		if (lockInfo == null) {
			return false;
		}
		//Compare our lock to the persistent item that the stack would have
		return lockInfo.equals(IEMCProxy.INSTANCE.getPersistentInfo(ItemInfo.fromStack(stack)));
	}

	public void setLockInfoFromPacket(@Nullable ItemInfo lockInfo) {
		this.lockInfo = lockInfo;
	}

	public boolean attemptCondenserSet(Player player) {
		return level != null && attemptCondenserSet(level, worldPosition, player);
	}

	private boolean attemptCondenserSet(@NotNull Level level, @NotNull BlockPos pos, Player player) {
		if (level.isClientSide()) {
			return false;
		}
		if (getLockInfo() == null) {
			ItemStack stack = player.containerMenu.getCarried();
			if (!stack.isEmpty()) {
				ItemInfo sourceInfo = ItemInfo.fromStack(stack);
				ItemInfo reducedInfo = IEMCProxy.INSTANCE.getPersistentInfo(sourceInfo);
				if (!NeoForge.EVENT_BUS.post(new PlayerAttemptCondenserSetEvent(player, sourceInfo, reducedInfo)).isCanceled()) {
					lockInfo = reducedInfo;
					checkLockAndUpdate(true);
					markDirty(level, pos, false);
					return true;
				}
				return false;
			}
			//If the lock item is actually null and the player didn't carry anything don't do anything
			// otherwise just fall through as we need to update it to actually being empty
			if (lockInfo == null) {
				return false;
			}
		}
		lockInfo = null;
		checkLockAndUpdate(true);
		markDirty(level, pos, false);
		return true;
	}

	@Override
	public void loadAdditional(@NotNull ValueInput input) {
		super.loadAdditional(input);
		inputInventory.deserialize(input.childOrEmpty("input"));
		lockInfo = input.read("lock", ItemInfo.CODEC).orElse(null);
	}

	@Override
	protected void saveAdditional(@NotNull ValueOutput output) {
		super.saveAdditional(output);
		inputInventory.serialize(output.child("input"));
		if (lockInfo != null) {
			output.store("lock", ItemInfo.CODEC, lockInfo);
		}
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player playerIn) {
		return new CondenserContainer(windowId, playerInventory, this);
	}

	@NotNull
	@Override
	public Component getDisplayName() {
		return TextComponentUtil.build(PEBlocks.CONDENSER);
	}
}