package moze_intel.projecte.api.conversion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongSortedMap;
import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.codec.IPECodecHelper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

/**
 * @param setValueBefore Map of {@link NormalizedSimpleStack} to the value to set before applying conversions.
 * @param setValueAfter  Map of {@link NormalizedSimpleStack} to the value to set after applying conversions.
 * @param conversions    List of conversions
 */
public record FixedValues(Object2LongSortedMap<NormalizedSimpleStack> setValueBefore, Object2LongSortedMap<NormalizedSimpleStack> setValueAfter,
						  List<CustomConversion> conversions) implements IHasConversions {

	private static <T> Object2LongSortedMap<T> createEmptyValueMap() {
		Object2LongSortedMap<T> map = new Object2LongLinkedOpenHashMap<>();
		map.defaultReturnValue(-1);
		return map;
	}

	private static final Codec<Object2LongSortedMap<NormalizedSimpleStack>> VALUE_CODEC = IPECodecHelper.INSTANCE.modifiableMap(IPECodecHelper.INSTANCE.lenientKeyUnboundedMap(
			IPECodecHelper.INSTANCE.nssMapCodec(),
			NeoForgeExtraCodecs.withAlternative(
					IPECodecHelper.INSTANCE.positiveLong(),
					Codec.stringResolver(
							val -> val == ProjectEAPI.FREE_ARITHMETIC_VALUE ? "free" : null,
							str -> str.equalsIgnoreCase("free") ? ProjectEAPI.FREE_ARITHMETIC_VALUE : null
					)
			).fieldOf("emc_value")
	), immutableMap -> {
		Object2LongSortedMap<NormalizedSimpleStack> map = new Object2LongLinkedOpenHashMap<>(immutableMap);
		map.defaultReturnValue(-1);
		return map;
	});

	public static final Codec<FixedValues> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			VALUE_CODEC.optionalFieldOf("before").forGetter(values -> IPECodecHelper.INSTANCE.ifNotEmpty(values.setValueBefore())),
			VALUE_CODEC.optionalFieldOf("after").forGetter(values -> IPECodecHelper.INSTANCE.ifNotEmpty(values.setValueAfter())),
			CustomConversion.MODIFIABLE_LIST_CODEC.optionalFieldOf("conversion").forGetter(values -> IPECodecHelper.INSTANCE.ifNotEmpty(values.conversions()))
	).apply(instance, (before, after, conversions) -> new FixedValues(
			before.orElseGet(FixedValues::createEmptyValueMap), after.orElseGet(FixedValues::createEmptyValueMap), conversions.orElseGet(ArrayList::new)
	)));

	public FixedValues() {
		this(createEmptyValueMap(), createEmptyValueMap(), new ArrayList<>());
	}

	/**
	 * Merges another FixedValues into this one
	 *
	 * @param other Values to merge.
	 */
	public void merge(FixedValues other) {
		setValueBefore.putAll(other.setValueBefore());
		setValueAfter.putAll(other.setValueAfter());
		conversions.addAll(other.conversions());
	}

	/**
	 * Checks whether all the backing values and conversions of this object are empty.
	 *
	 * @return {@code true} if all are empty.
	 */
	public boolean isEmpty() {
		return setValueBefore.isEmpty() && setValueAfter.isEmpty() && conversions.isEmpty();
	}
}