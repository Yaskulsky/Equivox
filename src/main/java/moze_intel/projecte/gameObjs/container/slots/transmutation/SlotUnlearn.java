package moze_intel.projecte.gameObjs.container.slots.transmutation;

import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.gameObjs.container.slots.InventoryContainerSlot;
import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotUnlearn extends InventoryContainerSlot {

	private final TransmutationInventory inv;

	public SlotUnlearn(TransmutationInventory inv, int index, int x, int y) {
		super(inv, index, x, y);
		this.inv = inv;
	}

	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
		return !this.hasItem() && (IEMCProxy.INSTANCE.hasValue(stack) || stack.is(PEItems.TOME_OF_KNOWLEDGE));
	}

	@Override
	public void initialize(@NotNull ItemStack stack) {
		//Note: We don't need to copy any of the logic from set as initialize is only ever called on the client
		super.initialize(stack);
	}

	@Override
	public void set(@NotNull ItemStack stack) {
		if (inv.isServer() && !stack.isEmpty()) {
			inv.handleUnlearn(stack.copy());
		}
		super.set(stack);
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}
}