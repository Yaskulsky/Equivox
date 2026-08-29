package com.yaskulsky.equivox.integration.recipe_viewer.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.yaskulsky.equivox.api.capabilities.item.IModeChanger;
import com.yaskulsky.equivox.gameObjs.PETags;
import com.yaskulsky.equivox.gameObjs.registries.PEBlocks;
import com.yaskulsky.equivox.gameObjs.registries.PEContainerTypes;
import com.yaskulsky.equivox.gameObjs.registries.PEDataComponentTypes;
import com.yaskulsky.equivox.gameObjs.registries.PEItems;
import com.yaskulsky.equivox.integration.recipe_viewer.FuelUpgradeRecipe;
import com.yaskulsky.equivox.integration.recipe_viewer.RecipeViewerHelper;
import com.yaskulsky.equivox.integration.recipe_viewer.WorldTransmuteEntry;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

@EmiEntrypoint
public class PEEmiPlugin implements EmiPlugin {

	private static final Comparison PROJECTE_COMPARISON = Comparison.compareData(emiStack -> {
		Set<Object> representation = new HashSet<>();
		ItemStack stack = emiStack.getItemStack();
		if (stack.getItem() instanceof IModeChanger<?> modeChanger) {
			representation.add(modeChanger.getMode(stack));
		}
		Long stored = stack.get(PEDataComponentTypes.STORED_EMC);
		if (stored != null && stored > 0) {
			representation.add(stored);
		}
		return representation.isEmpty() ? null : representation;
	});

	@Override
	public void register(EmiRegistry registry) {
		registry.addCategory(WorldTransmuteEmiRecipe.CATEGORY);
		for (WorldTransmuteEntry recipe : RecipeViewerHelper.getAllTransmutations()) {
			registry.addRecipe(new WorldTransmuteEmiRecipe(recipe));
		}
		registry.addCategory(CollectorEmiRecipe.CATEGORY);
		for (FuelUpgradeRecipe recipe : RecipeViewerHelper.getFuelUpgrades()) {
			registry.addRecipe(new CollectorEmiRecipe(recipe));
		}

		//Workstations for vanilla categories
		registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(PEItems.PHILOSOPHERS_STONE));
		registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(PEItems.ARCANE_TABLET));
		registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(PEItems.TRANSMUTATION_TABLET));
		registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(PEBlocks.TRANSMUTATION_TABLE));
		registry.addWorkstation(VanillaEmiRecipeCategories.SMELTING, EmiIngredient.of(PETags.Items.MATTER_FURNACES));
		registry.addWorkstation(CollectorEmiRecipe.CATEGORY, EmiIngredient.of(PETags.Items.COLLECTORS));
		registry.addWorkstation(WorldTransmuteEmiRecipe.CATEGORY, EmiStack.of(PEItems.PHILOSOPHERS_STONE));

		registry.addRecipeHandler(PEContainerTypes.ARCANE_TABLET_CONTAINER.get(), new CraftingTabletEmiRecipeHandler<>());
		registry.addRecipeHandler(PEContainerTypes.TRANSMUTATION_CONTAINER.get(), new CraftingTabletEmiRecipeHandler<>());

		registerItemSubtypes(registry, PEItems.ITEMS.getEntries());
		registerItemSubtypes(registry, PEBlocks.BLOCKS.getSecondaryEntries());
	}

	public static void registerItemSubtypes(EmiRegistry registry, Collection<? extends Holder<? extends ItemLike>> itemProviders) {
		for (Holder<? extends ItemLike> itemProvider : itemProviders) {
			Item item = itemProvider.value().asItem();
			if (item instanceof IModeChanger<?> || item.components().has(PEDataComponentTypes.STORED_EMC.get())) {
				registry.setDefaultComparison(item, PROJECTE_COMPARISON);
			}
		}
	}
}