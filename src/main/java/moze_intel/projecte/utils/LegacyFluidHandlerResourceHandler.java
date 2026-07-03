package moze_intel.projecte.utils;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * Bridges legacy {@link IFluidHandler} implementations to the 26.1 {@link ResourceHandler} fluid capability.
 */
public final class LegacyFluidHandlerResourceHandler implements ResourceHandler<FluidResource> {

	private final IFluidHandler handler;

	private LegacyFluidHandlerResourceHandler(IFluidHandler handler) {
		this.handler = handler;
	}

	@Nullable
	public static ResourceHandler<FluidResource> of(@Nullable IFluidHandler handler) {
		return handler == null ? null : new LegacyFluidHandlerResourceHandler(handler);
	}

	@Nullable
	public static ResourceHandler<FluidResource> ofItem(@Nullable IFluidHandlerItem handler) {
		return of(handler);
	}

	@Override
	public int size() {
		return handler.getTanks();
	}

	@Override
	public FluidResource getResource(int index) {
		return FluidResource.of(handler.getFluidInTank(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		return handler.getFluidInTank(index).getAmount();
	}

	@Override
	public long getCapacityAsLong(int index, FluidResource resource) {
		return handler.getTankCapacity(index);
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		return handler.isFluidValid(index, resource.toStack(1));
	}

	@Override
	public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		return handler.fill(resource.toStack(amount), IFluidHandler.FluidAction.EXECUTE);
	}

	@Override
	public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		FluidStack extracted = handler.drain(resource.toStack(amount), IFluidHandler.FluidAction.EXECUTE);
		return extracted.isEmpty() ? 0 : extracted.getAmount();
	}
}
