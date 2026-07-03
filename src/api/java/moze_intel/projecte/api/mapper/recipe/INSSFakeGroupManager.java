package moze_intel.projecte.api.mapper.recipe;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Set;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

/**
 * Interface to make for a cleaner API than using a {@link java.util.function.Function} when creating groupings of {@link NormalizedSimpleStack}s.
 */
public interface INSSFakeGroupManager {

	/**
	 * Gets or creates a singular {@link NormalizedSimpleStack} representing the grouping or "ingredient" of the given stacks. Additionally, a boolean is returned
	 * specifying if it was created or already existed. {@code true} for if it was created.
	 *
	 * @param stacks Individual stacks to represent as a single "combined" stack. May be modified after this method is called.
	 *
	 * @apiNote If the combined representation had to be created the {@link FakeGroupData} will represent this, and conversions from the individual elements to the
	 * returned stack <strong>MUST</strong> be added.
	 */
	default FakeGroupData getOrCreateFakeGroup(Set<NormalizedSimpleStack> stacks) {
		Object2IntMap<NormalizedSimpleStack> map = new Object2IntOpenHashMap<>(stacks.size());
		for (NormalizedSimpleStack stack : stacks) {
			map.put(stack, 1);
		}
		return getOrCreateFakeGroupDirect(map, true);
	}

	/**
	 * Gets or creates a singular {@link NormalizedSimpleStack} representing the grouping or "ingredient" of the given stacks. Additionally, a boolean is returned
	 * specifying if it was created or already existed. {@code true} for if it was created.
	 *
	 * @param stacks               Individual stacks to represent as a single "combined" stack. May be modified after this method is called.
	 * @param representsIngredient {@code true} to treat the individual stacks as a single ingredient ("combined" stack), or as a grouping of different ingredients as
	 *                             part of a sum.
	 *
	 * @apiNote If the combined representation had to be created the {@link FakeGroupData} will represent this, and conversions from the individual elements to the
	 * returned stack <strong>MUST</strong> be added.
	 */
	default FakeGroupData getOrCreateFakeGroup(Object2IntMap<NormalizedSimpleStack> stacks, boolean representsIngredient) {
		return getOrCreateFakeGroup(stacks, representsIngredient, false);
	}

	/**
	 * Gets or creates a singular {@link NormalizedSimpleStack} representing the grouping or "ingredient" of the given stacks. Additionally, a boolean is returned
	 * specifying if it was created or already existed. {@code true} for if it was created.
	 *
	 * @param stacks               Individual stacks to represent as a single "combined" stack. May be modified after this method is called.
	 * @param representsIngredient {@code true} to treat the individual stacks as a single ingredient ("combined" stack), or as a grouping of different ingredients as
	 *                             part of a sum.
	 * @param skipConversions      Used to skip adding the conversions on a newly created group.
	 *
	 * @apiNote If the combined representation had to be created the {@link FakeGroupData} will represent this, and conversions from the individual elements to the
	 * returned stack <strong>MUST</strong> be added.
	 */
	FakeGroupData getOrCreateFakeGroup(Object2IntMap<NormalizedSimpleStack> stacks, boolean representsIngredient, boolean skipConversions);

	/**
	 * Gets or creates a singular {@link NormalizedSimpleStack} representing the grouping or "ingredient" of the given stacks. Additionally, a boolean is returned
	 * specifying if it was created or already existed. {@code true} for if it was created.
	 *
	 * @param stacks               Individual stacks to represent as a single "combined" stack. Must not be modified after this method is called.
	 * @param representsIngredient {@code true} to treat the individual stacks as a single ingredient ("combined" stack), or as a grouping of different ingredients as
	 *                             part of a sum.
	 *
	 * @apiNote If the combined representation had to be created the {@link FakeGroupData} will represent this, and conversions from the individual elements to the
	 * returned stack <strong>MUST</strong> be added.
	 */
	default FakeGroupData getOrCreateFakeGroupDirect(Object2IntMap<NormalizedSimpleStack> stacks, boolean representsIngredient) {
		return getOrCreateFakeGroupDirect(stacks, representsIngredient, false);
	}

	/**
	 * Gets or creates a singular {@link NormalizedSimpleStack} representing the grouping or "ingredient" of the given stacks. Additionally, a boolean is returned
	 * specifying if it was created or already existed. {@code true} for if it was created.
	 *
	 * @param stacks               Individual stacks to represent as a single "combined" stack. Must not be modified after this method is called.
	 * @param representsIngredient {@code true} to treat the individual stacks as a single ingredient ("combined" stack), or as a grouping of different ingredients as
	 *                             part of a sum.
	 * @param skipConversions      Used to skip adding the conversions on a newly created group.
	 *
	 * @apiNote If the combined representation had to be created the {@link FakeGroupData} will represent this, and conversions from the individual elements to the
	 * returned stack <strong>MUST</strong> be added.
	 */
	FakeGroupData getOrCreateFakeGroupDirect(Object2IntMap<NormalizedSimpleStack> stacks, boolean representsIngredient, boolean skipConversions);

	/**
	 * Represents data for a fake group.
	 *
	 * @param dummy   Fake stack that represents the group.
	 * @param created Whether the fake group had to be created for this call.
	 */
	record FakeGroupData(NormalizedSimpleStack dummy, boolean created) {
	}
}