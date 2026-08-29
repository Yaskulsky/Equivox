package com.yaskulsky.equivox.integration.recipe_viewer.jei;

import java.util.Collection;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import com.yaskulsky.equivox.PECore;
import com.yaskulsky.equivox.api.capabilities.item.IModeChanger;
import com.yaskulsky.equivox.gameObjs.container.ArcaneTabletContainer;
import com.yaskulsky.equivox.gameObjs.container.PhilosStoneContainer;
import com.yaskulsky.equivox.gameObjs.container.TransmutationContainer;
import com.yaskulsky.equivox.gameObjs.gui.AbstractCollectorScreen;
import com.yaskulsky.equivox.gameObjs.gui.GUIArcaneTablet;
import com.yaskulsky.equivox.gameObjs.gui.GUIDMFurnace;
import com.yaskulsky.equivox.gameObjs.gui.GUIRMFurnace;
import com.yaskulsky.equivox.gameObjs.gui.GUITransmutation;
import com.yaskulsky.equivox.gameObjs.registries.PEBlocks;
import com.yaskulsky.equivox.gameObjs.registries.PEContainerTypes;
import com.yaskulsky.equivox.gameObjs.registries.PEDataComponentTypes;
import com.yaskulsky.equivox.gameObjs.registries.PEItems;
import com.yaskulsky.equivox.integration.IntegrationHelper;
import com.yaskulsky.equivox.integration.recipe_viewer.RecipeViewerHelper;
import com.yaskulsky.equivox.integration.recipe_viewer.alias.EquivoxAliasMapping;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JeiPlugin
public class PEJeiPlugin implements IModPlugin {

	private static final Identifier UID = PECore.rl("main");

	private static final ISubtypeInterpreter<ItemStack> PROJECTE_INTERPRETER = new ISubtypeInterpreter<>() {
		@Nullable
		@Override
		public Object getSubtypeData(@NotNull ItemStack stack, @NotNull UidContext context) {
			if (context == UidContext.Ingredient) {
				Object mode = null;
				if (stack.getItem() instanceof IModeChanger<?> modeChanger) {
					mode = modeChanger.getMode(stack);
				}
				Long stored = stack.get(PEDataComponentTypes.STORED_EMC);
				if (stored != null && stored > 0) {
					return mode == null ? stored : List.of(mode, stored);
				}
				return mode;
			}
			return null;
		}
	};

	@NotNull
	@Override
	public Identifier getPluginUid() {
		return UID;
	}

	private static boolean shouldLoad() {
		//Skip handling if both EMI and JEI are loaded as otherwise some things behave strangely
		return !ModList.get().isLoaded(IntegrationHelper.EMI_MODID);
	}

	public static void registerItemSubtypes(ISubtypeRegistration registry, Collection<? extends Holder<? extends ItemLike>> itemProviders) {
		for (Holder<? extends ItemLike> itemProvider : itemProviders) {
			registry.registerSubtypeInterpreter(itemProvider.value().asItem(), PROJECTE_INTERPRETER);
		}
	}

	@Override
	public void registerItemSubtypes(@NotNull ISubtypeRegistration registry) {
		if (shouldLoad()) {
			registerItemSubtypes(registry, PEItems.ITEMS.getEntries());
			registerItemSubtypes(registry, PEBlocks.BLOCKS.getSecondaryEntries());
		}
	}

	@Override
	public void registerCategories(@NotNull IRecipeCategoryRegistration registry) {
		if (shouldLoad()) {
			registry.addRecipeCategories(new WorldTransmuteRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
			registry.addRecipeCategories(new CollectorRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		}
	}

	@Override
	public void registerRecipeTransferHandlers(@NotNull IRecipeTransferRegistration registration) {
		// Always register crafting transfer so ATM11 (JEI + EMI) still gets the + button.
		registration.addRecipeTransferHandler(PhilosStoneContainer.class, MenuType.CRAFTING, RecipeTypes.CRAFTING, 1, 9, 10, 36);
		registration.addRecipeTransferHandler(new CraftingTabletRecipeTransferHandler<>(ArcaneTabletContainer.class,
				PEContainerTypes.ARCANE_TABLET_CONTAINER.get()), RecipeTypes.CRAFTING);
		registration.addRecipeTransferHandler(new CraftingTabletRecipeTransferHandler<>(TransmutationContainer.class,
				PEContainerTypes.TRANSMUTATION_CONTAINER.get()), RecipeTypes.CRAFTING);
	}

	@Override
	public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registry) {
		registry.addRecipeCatalyst(PEItems.ARCANE_TABLET.asStack(), RecipeTypes.CRAFTING);
		registry.addRecipeCatalyst(PEItems.TRANSMUTATION_TABLET.asStack(), RecipeTypes.CRAFTING);
		registry.addRecipeCatalyst(new ItemStack(PEBlocks.TRANSMUTATION_TABLE), RecipeTypes.CRAFTING);
		if (shouldLoad()) {
			registry.addRecipeCatalyst(PEItems.PHILOSOPHERS_STONE.asStack(), RecipeTypes.CRAFTING, WorldTransmuteRecipeCategory.RECIPE_TYPE);
			registry.addRecipeCatalyst(new ItemStack(PEBlocks.COLLECTOR), CollectorRecipeCategory.RECIPE_TYPE);
			registry.addRecipeCatalyst(new ItemStack(PEBlocks.COLLECTOR_MK2), CollectorRecipeCategory.RECIPE_TYPE);
			registry.addRecipeCatalyst(new ItemStack(PEBlocks.COLLECTOR_MK3), CollectorRecipeCategory.RECIPE_TYPE);
			registry.addRecipeCatalyst(new ItemStack(PEBlocks.DARK_MATTER_FURNACE), RecipeTypes.SMELTING, RecipeTypes.SMELTING_FUEL);
			registry.addRecipeCatalyst(new ItemStack(PEBlocks.RED_MATTER_FURNACE), RecipeTypes.SMELTING, RecipeTypes.SMELTING_FUEL);
		}
	}

	@Override
	public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registry) {
		registry.addRecipeClickArea(GUITransmutation.class, -59, 17, 54, 54, RecipeTypes.CRAFTING);
		registry.addRecipeClickArea(GUIArcaneTablet.class, -59, 17, 54, 54, RecipeTypes.CRAFTING);
		if (shouldLoad()) {
			registry.addRecipeClickArea(GUIDMFurnace.class, 73, 34, 25, 16, RecipeTypes.SMELTING, RecipeTypes.SMELTING_FUEL);
			registry.addRecipeClickArea(GUIRMFurnace.class, 88, 35, 25, 17, RecipeTypes.SMELTING, RecipeTypes.SMELTING_FUEL);
			registry.addRecipeClickArea(AbstractCollectorScreen.MK1.class, 138, 31, 10, 24, CollectorRecipeCategory.RECIPE_TYPE);
			registry.addRecipeClickArea(AbstractCollectorScreen.MK2.class, 138 + 16, 31, 10, 24, CollectorRecipeCategory.RECIPE_TYPE);
			registry.addRecipeClickArea(AbstractCollectorScreen.MK3.class, 138 + 34, 31, 10, 24, CollectorRecipeCategory.RECIPE_TYPE);
		}
	}

	@Override
	public void registerIngredientAliases(@NotNull IIngredientAliasRegistration registration) {
		new EquivoxAliasMapping().addAliases(new JEIAliasHelper(registration));
	}

	@Override
	public void registerRecipes(@NotNull IRecipeRegistration registry) {
		if (shouldLoad()) {
			registry.addRecipes(WorldTransmuteRecipeCategory.RECIPE_TYPE, List.copyOf(RecipeViewerHelper.getAllTransmutations()));
			registry.addRecipes(CollectorRecipeCategory.RECIPE_TYPE, RecipeViewerHelper.getFuelUpgrades());
		}
	}
}