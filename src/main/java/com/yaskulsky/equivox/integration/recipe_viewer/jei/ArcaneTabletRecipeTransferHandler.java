package com.yaskulsky.equivox.integration.recipe_viewer.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.yaskulsky.equivox.api.proxy.IEMCProxy;
import com.yaskulsky.equivox.gameObjs.container.ArcaneTabletContainer;
import com.yaskulsky.equivox.gameObjs.registries.PEContainerTypes;
import com.yaskulsky.equivox.network.packets.to_server.ArcaneTabletRecipeTransferPKT;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * JEI crafting transfer into the Arcane Tablet grid (inventory first, then EMC / knowledge).
 * Only compiled when {@code enable_optional_integrations=true}.
 */
public class ArcaneTabletRecipeTransferHandler implements IRecipeTransferHandler<ArcaneTabletContainer, RecipeHolder<CraftingRecipe>> {

	@Override
	public Class<? extends ArcaneTabletContainer> getContainerClass() {
		return ArcaneTabletContainer.class;
	}

	@Override
	public Optional<MenuType<ArcaneTabletContainer>> getMenuType() {
		return Optional.of(PEContainerTypes.ARCANE_TABLET_CONTAINER.get());
	}

	@Override
	public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
		return RecipeTypes.CRAFTING;
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(ArcaneTabletContainer container, RecipeHolder<CraftingRecipe> recipe,
			IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		if (!doTransfer) {
			// Cosmetic missing-slot highlighting can be added later; allow transfer attempt.
			return null;
		}
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
		ClientPacketDistributor.sendToServer(new ArcaneTabletRecipeTransferPKT(stacks, maxTransfer));
		return null;
	}
}
