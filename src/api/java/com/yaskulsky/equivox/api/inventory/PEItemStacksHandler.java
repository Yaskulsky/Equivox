package com.yaskulsky.equivox.api.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

/**
 * ProjectE inventory storage (replaces removed {@code PEItemStacksHandler} on NeoForge 26.3+).
 */
public class PEItemStacksHandler extends ItemStacksResourceHandler implements IndexModifier<ItemResource> {

	public PEItemStacksHandler(int size) {
		super(size);
	}

	public PEItemStacksHandler(NonNullList<ItemStack> stacks) {
		super(stacks);
	}

	public int getSlots() {
		return size();
	}

	@NotNull
	public ItemStack getStackInSlot(int slot) {
		return copyToList().get(slot).copy();
	}

	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		set(slot, ItemResource.of(stack), stack.getCount());
	}

	@NotNull
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		try (Transaction tx = Transaction.openRoot()) {
			int inserted = insert(slot, ItemResource.of(stack), stack.getCount(), tx);
			if (!simulate) {
				tx.commit();
			}
			return inserted >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted);
		}
	}

	@NotNull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount <= 0) {
			return ItemStack.EMPTY;
		}
		ItemResource resource = getResource(slot);
		if (resource.isEmpty()) {
			return ItemStack.EMPTY;
		}
		try (Transaction tx = Transaction.openRoot()) {
			int extracted = extract(slot, resource, amount, tx);
			if (!simulate) {
				tx.commit();
			}
			return extracted <= 0 ? ItemStack.EMPTY : resource.toStack(extracted);
		}
	}

	public int getSlotLimit(int slot) {
		return getCapacityAsInt(slot, ItemResource.EMPTY);
	}

	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return isValid(slot, ItemResource.of(stack));
	}

	@Override
	protected void onContentsChanged(int index, ItemStack previousContents) {
		onContentsChanged(index);
	}

	protected void onContentsChanged(int slot) {
	}

	protected void onLoad() {
	}

	public static int insertStacked(@NotNull ResourceHandler<ItemResource> handler, @NotNull ItemStack stack, boolean simulate) {
		if (stack.isEmpty()) {
			return 0;
		}
		try (Transaction tx = Transaction.openRoot()) {
			int inserted = ResourceHandlerUtil.insertStacking(handler, ItemResource.of(stack), stack.getCount(), tx);
			if (!simulate) {
				tx.commit();
			}
			return inserted;
		}
	}

	@NotNull
	public static ItemStack insertStackedRemainder(@NotNull ResourceHandler<ItemResource> handler, @NotNull ItemStack stack, boolean simulate) {
		int inserted = insertStacked(handler, stack, simulate);
		return inserted >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted);
	}

	@NotNull
	public static ItemStack getStack(@NotNull ResourceHandler<ItemResource> handler, int slot) {
		return handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
	}

	@NotNull
	public static ItemStack extractItem(@NotNull ResourceHandler<ItemResource> handler, int slot, int amount, boolean simulate) {
		if (handler instanceof PEItemStacksHandler peHandler) {
			return peHandler.extractItem(slot, amount, simulate);
		}
		if (amount <= 0) {
			return ItemStack.EMPTY;
		}
		ItemResource resource = handler.getResource(slot);
		if (resource.isEmpty()) {
			return ItemStack.EMPTY;
		}
		try (Transaction tx = Transaction.openRoot()) {
			int extracted = handler.extract(slot, resource, amount, tx);
			if (!simulate) {
				tx.commit();
			}
			return extracted <= 0 ? ItemStack.EMPTY : resource.toStack(extracted);
		}
	}

	public static int getSlotCount(@NotNull ResourceHandler<ItemResource> handler) {
		return handler.size();
	}

	@NotNull
	@SuppressWarnings("unchecked")
	public static IndexModifier<ItemResource> requireModifier(@NotNull ResourceHandler<ItemResource> handler) {
		if (handler instanceof IndexModifier<?> modifier) {
			return (IndexModifier<ItemResource>) modifier;
		}
		throw new IllegalArgumentException("Handler does not support direct slot mutation: " + handler);
	}
}
