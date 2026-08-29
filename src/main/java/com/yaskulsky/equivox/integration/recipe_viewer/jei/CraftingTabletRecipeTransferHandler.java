package com.yaskulsky.equivox.integration.recipe_viewer.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.yaskulsky.equivox.api.proxy.IEMCProxy;
import com.yaskulsky.equivox.gameObjs.container.IArcaneCraftingMenu;
import com.yaskulsky.equivox.network.packets.to_server.ArcaneTabletRecipeTransferPKT;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * JEI crafting transfer into an Equivox 3x3 (inventory first, then EMC / knowledge).
 */
public class CraftingTabletRecipeTransferHandler<C extends AbstractContainerMenu & IArcaneCraftingMenu>
		implements IRecipeTransferHandler<C, RecipeHolder<CraftingRecipe>> {

	private final Class<C> containerClass;
	private final MenuType<C> menuType;

	public CraftingTabletRecipeTransferHandler(Class<C> containerClass, MenuType<C> menuType) {
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

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(C container, RecipeHolder<CraftingRecipe> recipe,
			IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		List<List<ItemStack>> stacks = new ArrayList<>();
		List<ItemStack> empty = List.of(ItemStack.EMPTY);
		List<IRecipeSlotView> views = recipeSlots.getSlotViews();
		for (int i = 1; i < views.size(); i++) {
			List<ItemStack> options = new ArrayList<>(views.get(i).getItemStacks().toList());
			if (options.isEmpty()) {
				stacks.add(empty);
			} else {
				options.sort((a, b) -> Long.compare(IEMCProxy.INSTANCE.getValue(a), IEMCProxy.INSTANCE.getValue(b)));
				stacks.add(options);
			}
		}
		if (!doTransfer) {
			return null;
		}
		ClientPacketDistributor.sendToServer(new ArcaneTabletRecipeTransferPKT(stacks, maxTransfer));
		return null;
	}
}
