package moze_intel.projecte.impl.codec;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.RecordBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.codec.MapProcessor;

/**
 * Based semi loosely off of:
 * <li>
 *     <ul><a href="https://gist.github.com/thiakil/7cadeb2a8e50aabc5056bc6574af0d90">Thiakil's Map as List Codec</a></ul>
 *     <ul>{@link com.mojang.serialization.codecs.ListCodec} for how to handle decoder state and handing of lists</ul>
 *     <ul>{@link com.mojang.serialization.codecs.UnboundedMapCodec} and the base implementation in {@link com.mojang.serialization.codecs.BaseMapCodec}</ul>
 *     <ul>{@link net.neoforged.neoforge.common.LenientUnboundedMapCodec} for making parts lenient (though we only optionally make keys lenient here)</ul>
 * </li>
 */
public record PEUnboundedMapCodec<KEY, VALUE, MAP extends Map<KEY, VALUE>>(
		MapCodec<KEY> keyCodec, MapCodec<VALUE> valueCodec, MapProcessor<KEY, VALUE> processor, boolean lenientKey,
		Supplier<? extends MAP> elementReaderCreator, UnaryOperator<MAP> makeImmutable
) implements Codec<MAP> {

	private static <T> void validateCodecKeys(DynamicOps<T> ops, MapCodec<?> keyCodec, MapCodec<?> valueCodec) {
		Set<T> keyCodecKeys = keyCodec.keys(ops).collect(Collectors.toSet());
		if (!keyCodecKeys.isEmpty()) {
			String overlappingKeys = valueCodec.keys(ops)
					.filter(keyCodecKeys::contains)
					.map(ops::getStringValue)
					.filter(DataResult::isSuccess)
					.map(DataResult::getOrThrow)
					.collect(Collectors.joining(", "));
			if (!overlappingKeys.isEmpty()) {
				throw new IllegalArgumentException("Keys and Values for unbounded maps cannot have overlapping keys: " + overlappingKeys);
			}
		}
	}

	public PEUnboundedMapCodec {
		validateCodecKeys(JsonOps.INSTANCE, keyCodec, valueCodec);
	}

	public static <KEY, VALUE> PEUnboundedMapCodec<KEY, VALUE, Map<KEY, VALUE>> create(MapCodec<KEY> keyCodec, MapCodec<VALUE> valueCodec,
			MapProcessor<KEY, VALUE> processor, boolean lenientKey) {
		//Note: We use a LinkedHashMap instead of an Object2ObjectArrayMap like BaseMapCodec uses, as some of our maps may have a large number of values,
		// and the performance of array maps degrades for a large number of values
		//Note: ImmutableMap::copyOf maintains the order of the map that is being copied
		return new PEUnboundedMapCodec<>(keyCodec, valueCodec, processor, lenientKey, LinkedHashMap::new, ImmutableMap::copyOf);
	}

	@Override
	public <T> DataResult<T> encode(final MAP input, final DynamicOps<T> ops, final T prefix) {
		final ListBuilder<T> builder = ops.listBuilder();
		for (final Map.Entry<KEY, VALUE> entry : input.entrySet()) {
			RecordBuilder<T> encoded = keyCodec().encode(entry.getKey(), ops, ops.mapBuilder());
			encoded = valueCodec().encode(entry.getValue(), ops, encoded);
			builder.add(encoded.build(ops.emptyMap()));
		}
		return builder.build(prefix);
	}

	@Override
	public <T> DataResult<Pair<MAP, T>> decode(final DynamicOps<T> ops, final T input) {
		return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap(stream -> {
			final DecoderState<T> decoder = new DecoderState<>(ops);
			stream.accept(decoder::accept);
			return decoder.build();
		}).map(result -> Pair.of(result, input));
	}

	@Override
	public String toString() {
		if (lenientKey) {
			return "projecte:LenientKeyUnboundedMapCodec[" + keyCodec + " -> " + valueCodec + ']';
		}
		return "projecte:UnboundedMapCodec[" + keyCodec + " -> " + valueCodec + ']';
	}

	private class DecoderState<T> {

		private static final DataResult<Unit> INITIAL_RESULT = DataResult.success(Unit.INSTANCE, Lifecycle.stable());

		private final DynamicOps<T> ops;
		private final MAP elements = elementReaderCreator.get();
		private final Stream.Builder<T> failed = Stream.builder();
		private DataResult<Unit> result = INITIAL_RESULT;

		private DecoderState(final DynamicOps<T> ops) {
			this.ops = ops;
		}

		private void accept(final T input) {
			DataResult<KEY> keyResult = keyCodec().decoder().parse(ops, input);
			if (lenientKey() && keyResult.isError()) {
				//Skip this key as it is invalid (potentially representing something unloaded)
				// Note: We log the error to help diagnose any issues
				//TODO: Do we want to try and allow partial deserialization for example if it just has invalid components? (probably not)
				PECore.LOGGER.error("Unable to deserialize key: {}", keyResult.error().orElseThrow().message());
				return;
			}
			DataResult<VALUE> valueResult = valueCodec().decoder().parse(ops, input);
			DataResult<Map.Entry<KEY, VALUE>> entryResult = keyResult.apply2stable(Map::entry, valueResult);

			Optional<Map.Entry<KEY, VALUE>> resultOrPartial = entryResult.resultOrPartial();
			if (resultOrPartial.isPresent()) {
				Map.Entry<KEY, VALUE> entry = resultOrPartial.get();
				VALUE existing = processor().addElement(elements, entry.getKey(), entry.getValue());
				if (existing != null) {
					failed.add(input);
					result = result.apply2stable((result, element) -> result, DataResult.error(() -> "Duplicate entry for key: '" + entry.getKey() + "'"));
					return;
				}
			}
			entryResult.ifError(error -> failed.add(input));
			result = result.apply2stable((result, element) -> result, entryResult);
		}

		public DataResult<MAP> build() {
			final T errors = ops.createList(failed.build());
			final MAP immutableElements = makeImmutable.apply(elements);
			return result.map(ignored -> immutableElements)
					.setPartial(immutableElements)
					.mapError(e -> e + " missed input: " + errors);
		}
	}

}