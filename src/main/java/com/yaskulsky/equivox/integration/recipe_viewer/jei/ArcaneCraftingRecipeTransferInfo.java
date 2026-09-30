package com.yaskulsky.equivox.integration.recipe_viewer.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.yaskulsky.equivox.gameObjs.container.IArcaneCraftingMenu;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Tells JEI which menu slots are the 3x3 grid vs player inventory. Without this, JEI assumes slots 0-8
 * (transmutation inputs on the Arcane Tablet) and ghost/+ transfer targets the wrong cells.
 */
public final class ArcaneCraftingRecipeTransferInfo<C extends AbstractContainerMenu & IArcaneCraftingMenu>
		implements IRecipeTransferInfo<C, RecipeHolder<CraftingRecipe>> {

	private static final int PLAYER_SLOT_START = 27;
	private static final int PLAYER_SLOT_COUNT = 36;

	private final Class<C> containerClass;
	private final MenuType<C> menuType;

	public ArcaneCraftingRecipeTransferInfo(Class<C> containerClass, MenuType<C> menuType) {
		this.containerClass = containerClass;
		this.menuType = menuType;
	}

	@Override
	public Class<? extends C> getContainerClass() {
		return containerClass;
	}

	@Override
	public Optional<MenuType<C>> getMenuType() {
		return Optional.of(menuType);
	}

	@Override
	public IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
		return RecipeTypes.CRAFTING;
	}

	@Override
	public boolean canHandle(C container, RecipeHolder<CraftingRecipe> recipe) {
		return true;
	}

	@Override
	public List<Slot> getRecipeSlots(C container, RecipeHolder<CraftingRecipe> recipe) {
		int start = container.getCraftingSlotStart();
		List<Slot> recipeSlots = new ArrayList<>(9);
		for (int i = 0; i < 9; i++) {
			recipeSlots.add(container.slots.get(start + i));
		}
		return recipeSlots;
	}

	@Override
	public List<Slot> getInventorySlots(C container, RecipeHolder<CraftingRecipe> recipe) {
		int end = Math.min(PLAYER_SLOT_START + PLAYER_SLOT_COUNT, container.slots.size());
		return container.slots.subList(PLAYER_SLOT_START, end);
	}
}
