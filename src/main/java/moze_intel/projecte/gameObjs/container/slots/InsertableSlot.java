package moze_intel.projecte.gameObjs.container.slots;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class InsertableSlot extends Slot implements IInsertableSlot {

    public InsertableSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }
}