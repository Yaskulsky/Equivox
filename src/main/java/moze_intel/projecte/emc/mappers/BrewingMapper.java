package moze_intel.projecte.emc.mappers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.PEConfigTranslations;
import moze_intel.projecte.emc.MappingHelper;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

@EMCMapper
public class BrewingMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, ReloadableServerResources serverResources,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null) {
			PECore.LOGGER.error("Failed to get server and potion data when trying to map potions");
			return;
		}
		PotionBrewing potionBrewing = server.potionBrewing();
		Set<ItemInfo> allReagents = mapAllReagents(potionBrewing);
		Set<ItemInfo> allInputs = mapAllInputs(potionBrewing);

		//Add conversion for empty bottle + water to water bottle
		mapper.addConversion(1, NSSItem.createItem(PotionContents.createItemStack(Items.POTION, Potions.WATER)), EMCHelper.intMapOf(
				NSSItem.createItem(Items.GLASS_BOTTLE), 1,
				NSSFluid.createTag(FluidTags.WATER), FluidType.BUCKET_VOLUME / 3
		));

		int recipeCount = 0;


		//Check all known valid inputs and reagents to see which ones create valid inputs
		// Note: Getting the list of outputs while getting reagents will cause things to be missed
		// As the PotionBrewing class does not contain all valid mappings (For example: Potion of luck + gunpowder -> splash potion of luck)
		for (ItemInfo inputInfo : allInputs) {
			ItemStack validInput = inputInfo.createStack();
			NormalizedSimpleStack nssInput = inputInfo.toNSS();
			for (ItemInfo reagentInfo : allReagents) {
				ItemStack output = potionBrewing.mix(reagentInfo.createStack(), validInput);
				if (!output.isEmpty()) {
					//Add the conversion, 3 input + reagent = 3 y output as the output technically could be stacked
					mapper.addConversion(3 * output.getCount(), NSSItem.createItem(output), EMCHelper.intMapOf(
							nssInput, 3,
							reagentInfo.toNSS(), 1
					));
					recipeCount++;
				}
			}
		}

		Set<Class<?>> canNotMap = new HashSet<>();
		for (IBrewingRecipe recipe : potionBrewing.getRecipes()) {
			if (recipe instanceof BrewingRecipe brewingRecipe) {
				ItemStack[] validInputs = getMatchingStacks(brewingRecipe.getInput());
				ItemStack[] validReagents = getMatchingStacks(brewingRecipe.getIngredient());
				if (validInputs == null || validReagents == null) {
					//Skip brewing recipes that we are not able to process such as ones using tags
					// as ingredients, as tags don't exist when the brewing recipe is being defined
					continue;
				}
				ItemStack output = brewingRecipe.getOutput();
				NormalizedSimpleStack nssOut = NSSItem.createItem(output);
				for (ItemStack validInput : validInputs) {
					NormalizedSimpleStack nssInput = NSSItem.createItem(validInput);
					for (ItemStack validReagent : validReagents) {
						//Add the conversion, 3 input + x reagent = 3 y output as strictly speaking the only one of the three parts
						// in the recipe that are required to be one in stack size is the input
						mapper.addConversion(3 * output.getCount(), nssOut, EMCHelper.intMapOf(
								nssInput, 3,
								NSSItem.createItem(validReagent), validReagent.getCount()
						));
						recipeCount++;
					}
				}
			} else {
				canNotMap.add(recipe.getClass());
			}
		}

		PECore.debugLog("{} Statistics:", getName());
		PECore.debugLog("Found {} Brewing Recipes", recipeCount);
		for (Class<?> c : canNotMap) {
			PECore.debugLog("Could not map Brewing Recipes with Type: {}", c.getName());
		}
	}

	@Override
	public String getName() {
		return PEConfigTranslations.MAPPING_BREWING_MAPPER.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.MAPPING_BREWING_MAPPER.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.MAPPING_BREWING_MAPPER.tooltip();
	}

	@Nullable
	private static ItemStack[] getMatchingStacks(Ingredient ingredient) {
		return MappingHelper.getMatchingStacks(ingredient, Identifier.withDefaultNamespace("brewing"));
	}

	private Set<ItemInfo> mapAllReagents(PotionBrewing potionBrewing) {
		Set<ItemInfo> allReagents = new HashSet<>();
		addReagents(allReagents, potionBrewing.containerMixes);
		addReagents(allReagents, potionBrewing.potionMixes);
		return allReagents;
	}

	private <T> void addReagents(Set<ItemInfo> allReagents, List<PotionBrewing.Mix<T>> conversions) {
		for (PotionBrewing.Mix<T> conversion : conversions) {
			for (ItemStack r : MappingHelper.getMatchingStacks(conversion.ingredient(), Identifier.withDefaultNamespace("brewing"))) {
				allReagents.add(ItemInfo.fromStack(r));
			}
		}
	}

	private Set<ItemInfo> mapAllInputs(PotionBrewing potionBrewing) {
		Set<ItemInfo> allInputs = new HashSet<>();

		Set<ItemInfo> inputs = new HashSet<>();
		for (Ingredient potionItem : potionBrewing.containers) {
			ItemStack[] matchingStacks = getMatchingStacks(potionItem);
			if (matchingStacks != null) {
				//Silently ignore any invalid potion items (ingredients that may be tags) this should never be the case
				// unless someone ATs the map and inserts a custom ingredient into it, but just in case, don't crash
				for (ItemStack input : matchingStacks) {
					inputs.add(ItemInfo.fromStack(input));
				}
			}
		}
		for (Holder.Reference<Potion> potion : BuiltInRegistries.POTION.listElements().toList()) {
			PotionContents contents = new PotionContents(potion);
			for (ItemInfo input : inputs) {
				ItemStack stack = input.createStack();
				stack.set(DataComponents.POTION_CONTENTS, contents);
				allInputs.add(ItemInfo.fromStack(stack));
			}
		}
		return allInputs;
	}
}