package moze_intel.projecte.utils;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * Bridges legacy {@link IItemHandler} implementations to the 26.1 {@link ResourceHandler} item capability.
 */
public final class LegacyItemHandlerResourceHandler implements ResourceHandler<ItemResource> {

	private final IItemHandler handler;

	private LegacyItemHandlerResourceHandler(IItemHandler handler) {
		this.handler = handler;
	}

	@Nullable
	public static ResourceHandler<ItemResource> of(@Nullable IItemHandler handler) {
		return handler == null ? null : new LegacyItemHandlerResourceHandler(handler);
	}

	@Override
	public int size() {
		return handler.getSlots();
	}

	@Override
	public ItemResource getResource(int index) {
		return ItemResource.of(handler.getStackInSlot(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		return handler.getStackInSlot(index).getCount();
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		return handler.getSlotLimit(index);
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return handler.isItemValid(index, resource.toStack(1));
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		ItemStack remaining = handler.insertItem(index, resource.toStack(amount), false);
		return amount - remaining.getCount();
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		ItemStack extracted = handler.extractItem(index, amount, false);
		return extracted.isEmpty() ? 0 : extracted.getCount();
	}
}
