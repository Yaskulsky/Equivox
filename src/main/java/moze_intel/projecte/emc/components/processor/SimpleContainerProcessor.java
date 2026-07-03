package moze_intel.projecte.emc.components.processor;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.proxy.IEMCProxy;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public abstract class SimpleContainerProcessor<TYPE> extends PersistentComponentProcessor<TYPE> {

	@Override
	public boolean usePersistentComponents() {
		//Disable persisting by default
		return false;
	}

	protected abstract Iterable<ItemStack> getStoredItems(TYPE component);

	@Override
	@Range(from = 0, to = Long.MAX_VALUE)
	protected final long recalculateEMC(@NotNull ItemInfo info, @Range(from = 1, to = Long.MAX_VALUE) long currentEMC, @NotNull TYPE component) throws ArithmeticException {
		for (ItemStack item : getStoredItems(component)) {
			if (!item.isEmpty()) {
				long itemEmc = IEMCProxy.INSTANCE.getValue(item);
				if (itemEmc == 0) {//Return that this item can't be converted as it has items that don't have emc values in them
					return 0;
				}
				long stackEmc = Math.multiplyExact(itemEmc, item.getCount());
				currentEMC = Math.addExact(currentEMC, stackEmc);
			}
		}
		return currentEMC;
	}
}
