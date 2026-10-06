package com.yaskulsky.equivox.gameObjs.container;

import com.yaskulsky.equivox.api.inventory.PEItemStacksHandler;
import com.yaskulsky.equivox.gameObjs.block_entities.RMFurnaceBlockEntity;
import com.yaskulsky.equivox.gameObjs.container.slots.MatterFurnaceOutputSlot;
import com.yaskulsky.equivox.gameObjs.container.slots.SlotPredicates;
import com.yaskulsky.equivox.gameObjs.container.slots.ValidatedSlot;
import com.yaskulsky.equivox.gameObjs.registries.PEContainerTypes;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class RMFurnaceContainer extends DMFurnaceContainer {

	public RMFurnaceContainer(int windowId, Inventory playerInv, RMFurnaceBlockEntity furnace) {
		super(PEContainerTypes.RM_FURNACE_CONTAINER, windowId, playerInv, furnace);
	}

	@Override
	void initSlots() {
		ResourceHandler<ItemResource> fuel = furnace.getFuel();
		ResourceHandler<ItemResource> input = furnace.getInput();
		ResourceHandler<ItemResource> output = furnace.getOutput();

		//Fuel
		this.addSlot(new ValidatedSlot(fuel, 0, 65, 53, SlotPredicates.FURNACE_FUEL));

		//Input(0)
		this.addSlot(new ValidatedSlot(input, 0, 65, 17, furnace::hasSmeltingResult));

		int counter = 1;
		//Input Storage
		for (int i = 2; i >= 0; i--) {
			for (int j = 3; j >= 0; j--) {
				this.addSlot(new ValidatedSlot(input, counter++, 11 + i * 18, 8 + j * 18, furnace::hasSmeltingResult));
			}
		}

		counter = PEItemStacksHandler.getSlotCount(output) - 1;

		//Output(0)
		this.addSlot(new MatterFurnaceOutputSlot(playerInv.player, furnace, output, counter--, 125, 35));

		//Output Storage
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 4; j++) {
				this.addSlot(new MatterFurnaceOutputSlot(playerInv.player, furnace, output, counter--, 147 + i * 18, 8 + j * 18));
			}
		}

		addPlayerInventory(24, 84);
	}
}