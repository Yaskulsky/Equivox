package moze_intel.projecte.emc.mappers.recipe.special;

import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

// TODO 26.1: ShulkerBoxColoring was removed; reimplement when replacement API is identified.
public class ShulkerRecoloringMapper implements IRecipeTypeMapper {

	@Override
	public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, RecipeHolder<?> recipeHolder, RegistryAccess registryAccess,
			INSSFakeGroupManager fakeGroupManager) {
		return false;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public String getTranslationKey() {
		return "";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public boolean canHandle(RecipeType<?> recipeType) {
		return false;
	}
}
