package moze_intel.projecte.utils;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helpers for Inventories, ItemStacks, Items, and the Ore Dictionary Notice: Please try to keep methods tidy and alphabetically ordered. Thanks!
 */
public final class ItemHelper {

	/**
	 * Gets an ActionResult based on a type
	 */
	public static InteractionResult actionResultFromType(InteractionResult type, ItemStack stack) {
		if (type instanceof InteractionResult.Success success) {
			return success.heldItemTransformedTo(stack);
		}
		return type;
	}

	/**
	 * Compacts an inventory and returns if the inventory is/was empty.
	 *
	 * @return True if the inventory was empty.
	 */
	public static boolean compactInventory(IItemHandlerModifiable inventory) {
		List<ItemStack> temp = new ArrayList<>();
		for (int i = 0, slots = inventory.getSlots(); i < slots; i++) {
			ItemStack stackInSlot = inventory.getStackInSlot(i);
			if (!stackInSlot.isEmpty()) {
				temp.add(stackInSlot);
				inventory.setStackInSlot(i, ItemStack.EMPTY);
			}
		}
		for (ItemStack s : temp) {
			ItemHandlerHelper.insertItemStacked(inventory, s, false);
		}
		return temp.isEmpty();
	}

	public static IItemHandlerModifiable immutableCopy(IItemHandler toCopy) {
		int slots = toCopy.getSlots();
		final List<ItemStack> list = new ArrayList<>(slots);
		for (int i = 0; i < slots; i++) {
			list.add(toCopy.getStackInSlot(i).copy());
		}
		return new IItemHandlerModifiable() {
			@Override
			public void setStackInSlot(int slot, @NotNull ItemStack stack) {
			}

			@Override
			public int getSlots() {
				return list.size();
			}

			@NotNull
			@Override
			public ItemStack getStackInSlot(int slot) {
				return list.get(slot);
			}

			@NotNull
			@Override
			public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
				return stack;
			}

			@NotNull
			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				return ItemStack.EMPTY;
			}

			@Override
			public int getSlotLimit(int slot) {
				return getStackInSlot(slot).getMaxStackSize();
			}

			@Override
			public boolean isItemValid(int slot, @NotNull ItemStack stack) {
				return true;
			}
		};
	}

	public static boolean isRepairableDamagedItem(ItemStack stack) {
		return stack.isDamageableItem() && stack.getDamageValue() > 0;
	}

	public static NonNullList<ItemStack> getInventoryStacks(Inventory inventory) {
		NonNullList<ItemStack> stacks = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			stacks.set(i, inventory.getItem(i));
		}
		return stacks;
	}

	/**
	 * @return The amount of the given stack that could not fit. If it all fit, zero is returned
	 */
	public static int simulateFit(NonNullList<ItemStack> inv, ItemStack stack) {
		int remainder = stack.getCount();
		for (ItemStack invStack : inv) {
			if (invStack.isEmpty()) {
				//Slot is empty, just put it all there
				return 0;
			}
			if (ItemStack.isSameItemSameComponents(stack, invStack)) {
				int amountSlotNeeds = invStack.getMaxStackSize() - invStack.getCount();
				//Double check we don't have an over sized stack
				if (amountSlotNeeds > 0) {
					if (remainder <= amountSlotNeeds) {
						//If the slot can accept it all, return it all fit
						return 0;
					}
					//Otherwise take that many items out and
					remainder -= amountSlotNeeds;
				}
			}
		}
		return remainder;
	}

	public static ItemStack size(ItemStack stack, int size) {
		if (size <= 0 || stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		return stack.copyWithCount(size);
	}

	public static BlockState stackToState(ItemStack stack, @Nullable BlockPlaceContext context) {
		if (stack.getItem() instanceof BlockItem blockItem) {
			if (context == null) {
				return blockItem.getBlock().defaultBlockState();
			}
			return blockItem.getBlock().getStateForPlacement(context);
		}
		return null;
	}
}
