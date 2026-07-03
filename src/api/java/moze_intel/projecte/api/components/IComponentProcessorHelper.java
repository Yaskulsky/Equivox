package moze_intel.projecte.api.components;

import java.util.ServiceLoader;
import java.util.function.ToLongFunction;
import moze_intel.projecte.api.ItemInfo;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Helper class for getting cached EMC costs and calculating cached values in {@link IDataComponentProcessor#updateCachedValues(ToLongFunction)}.
 */
public interface IComponentProcessorHelper {

	/**
	 * The helper for querying information as part of a {@link IDataComponentProcessor}.
	 */
	IComponentProcessorHelper INSTANCE = ServiceLoader.load(IComponentProcessorHelper.class).findFirst()
			.orElseThrow(() -> new IllegalStateException("No valid ServiceImpl for IComponentProcessorHelper found, ProjectE may be absent, damaged, or outdated"));

	/**
	 * Gets an {@link ItemInfo} with the {@link net.minecraft.core.component.DataComponentPatch} reduced to what will be saved to knowledge/used for condensing.
	 *
	 * @param info The ItemInfo we want to trim to the data that will be used for persistence.
	 *
	 * @return An {@link ItemInfo} for the same item as the input info, but with a potentially reduced {@link net.minecraft.core.component.DataComponentPatch}, containing
	 * whatever data is persistent/matters.
	 */
	@NotNull
	ItemInfo getPersistentInfo(@NotNull ItemInfo info);

	/**
	 * Gets the minimum EMC value from the lookup for the given ingredient. This will check all matching stacks and find the lowest non-zero value. If there are no
	 * non-zero values, this will return zero.
	 *
	 * @param emcLookup  Function to look up the emc value of an item, null if the cache should be cleared as data is being unloaded.
	 * @param ingredient Ingredient to find the EMC value for.
	 *
	 * @return The minimum EMC value for the given ingredient.
	 */
	@Range(from = 0, to = Long.MAX_VALUE)
	long getMinEmcFor(@NotNull ToLongFunction<ItemInfo> emcLookup, @NotNull Ingredient ingredient);

	/**
	 * Gets the minimum EMC value from the lookup for the given color. This will get the cached value of the lowest EMC value of all items in the color's dye tag.
	 *
	 * @return The minimum EMC value for the given dye.
	 */
	@Range(from = 0, to = Long.MAX_VALUE)
	long getColorEmc(@NotNull DyeColor color);
}