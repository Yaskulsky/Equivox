package com.yaskulsky.equivox.gameObjs.container;

import java.util.List;
import com.yaskulsky.equivox.api.capabilities.IKnowledgeProvider;
import com.yaskulsky.equivox.gameObjs.container.inventory.TransmutationInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

/**
 * Shared 3x3 + knowledge/EMC crafting used by Arcane Tablet and Transmutation Table/Tablet.
 */
public interface IArcaneCraftingMenu {

	Player getCraftingPlayer();

	Inventory getCraftingPlayerInventory();

	IKnowledgeProvider getProvider();

	TransmutationInventory transmutationInventory();

	TransientCraftingContainer getCraftSlots();

	ResultContainer getResultSlots();

	int getResultSlotIndex();

	boolean isSkipRefill();

	void setSkipRefill(boolean skip);

	void setCrafting(boolean crafting);

	AbstractContainerMenu asMenu();

	void applyCraftingResult(ItemStack stack);

	default void onRecipeTransfer(List<List<ItemStack>> recipe, boolean transferAll) {
		ArcaneCraftingLogic.onRecipeTransfer(this, recipe, transferAll);
	}

	default void fillCraftingSlots(List<List<ItemStack>> recipe, boolean transferAll) {
		ArcaneCraftingLogic.fillCraftingSlots(this, recipe, transferAll);
	}

	default void clearCrafting(boolean force) {
		ArcaneCraftingLogic.clearCrafting(this, force);
	}

	default void rotateCrafting(boolean clockwise) {
		ArcaneCraftingLogic.rotateCrafting(this, clockwise);
	}

	default void balanceCrafting() {
		ArcaneCraftingLogic.balanceCrafting(this);
	}

	default void spreadCrafting() {
		ArcaneCraftingLogic.spreadCrafting(this);
	}
}
