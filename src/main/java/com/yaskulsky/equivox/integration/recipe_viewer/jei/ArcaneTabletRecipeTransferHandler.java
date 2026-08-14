package com.yaskulsky.equivox.integration.recipe_viewer.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.yaskulsky.equivox.api.capabilities.IKnowledgeProvider;
import com.yaskulsky.equivox.api.capabilities.PECapabilities;
import com.yaskulsky.equivox.api.proxy.IEMCProxy;
import com.yaskulsky.equivox.gameObjs.container.ArcaneTabletContainer;
import com.yaskulsky.equivox.gameObjs.container.slots.arcane.ArcaneTabletHelper;
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
			// Preview: still allow the button; server rejects / no-ops missing ingredients.
			// Prefer inventory matches, then learned items with enough personal EMC.
			for (List<ItemStack> options : stacks) {
				if (!options.isEmpty() && !ItemStack.EMPTY.equals(options.getFirst()) && !canSatisfy(player, options)) {
					// Keep transfer enabled — Arcane Tablet can still fill partial grids.
					break;
				}
			}
			return null;
		}
		ClientPacketDistributor.sendToServer(new ArcaneTabletRecipeTransferPKT(stacks, maxTransfer));
		return null;
	}

	private static boolean canSatisfy(Player player, List<ItemStack> options) {
		for (ItemStack option : options) {
			ItemStack cleaned = ArcaneTabletHelper.cleanStack(option);
			if (cleaned.isEmpty()) {
				continue;
			}
			for (ItemStack inv : player.getInventory().getNonEquipmentItems()) {
				if (ArcaneTabletHelper.areStacksEqual(cleaned, ArcaneTabletHelper.cleanStack(inv))) {
					return true;
				}
			}
			IKnowledgeProvider knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
			if (knowledge != null && knowledge.hasKnowledge(cleaned)) {
				long value = IEMCProxy.INSTANCE.getValue(cleaned);
				if (value > 0 && knowledge.getEmc().compareTo(java.math.BigInteger.valueOf(value)) >= 0) {
					return true;
				}
			}
		}
		return false;
	}
}
