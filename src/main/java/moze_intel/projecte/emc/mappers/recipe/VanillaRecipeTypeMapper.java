package moze_intel.projecte.emc.mappers.recipe;

import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.config.PEConfigTranslations;
import net.minecraft.world.item.crafting.RecipeType;

@RecipeTypeMapper
public class VanillaRecipeTypeMapper extends BaseRecipeTypeMapper {

	@Override
	public String getName() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_VANILLA.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_VANILLA.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_VANILLA.tooltip();
	}

	@Override
	public boolean canHandle(RecipeType<?> recipeType) {
		return recipeType == RecipeType.CRAFTING || recipeType == RecipeType.SMELTING || recipeType == RecipeType.BLASTING || recipeType == RecipeType.SMOKING
			   || recipeType == RecipeType.CAMPFIRE_COOKING || recipeType == RecipeType.STONECUTTING;
	}
}