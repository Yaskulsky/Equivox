package moze_intel.projecte.api.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import org.jetbrains.annotations.Nullable;

public interface IPECodecHelper {

	/**
	 * Helper for dealing with {@link Codec Codecs} related to ProjectE.
	 */
	IPECodecHelper INSTANCE = ServiceLoader.load(IPECodecHelper.class).findFirst()
			.orElseThrow(() -> new IllegalStateException("No valid ServiceImpl for IPECodecHelper found, ProjectE may be absent, damaged, or outdated"));

	/**
	 * {@link Codec} that tries to encode a {@link NormalizedSimpleStack}..
	 */
	Codec<NormalizedSimpleStack> nssCodec();

	/**
	 * {@link MapCodec} that tries to encode a {@link NormalizedSimpleStack}..
	 */
	MapCodec<NormalizedSimpleStack> nssMapCodec();

	/**
	 * {@return Big Integer Codec that validates the value is greater than or equal to zero}
	 */
	Codec<BigInteger> nonNegativeBigInt();

	/**
	 * {@return Big Integer Codec that validates the value is greater than zero}
	 */
	Codec<BigInteger> positiveBigInt();

	/**
	 * {@return BigInteger Codec that validates the value is within the range, and if not produces the given error}
	 *
	 * @param min          Min value inclusive. Null if no min
	 * @param max          Max value inclusive. Null if no max
	 * @param errorMessage Error message producer.
	 */
	Codec<BigInteger> bigIntRangeWithMessage(@Nullable BigInteger min, @Nullable BigInteger max, Function<BigInteger, String> errorMessage);

	/**
	 * {@return Long Codec that validates the long is greater than or equal to zero}
	 */
	Codec<Long> nonNegativeLong();

	/**
	 * {@return Long Codec that validates the long is greater than zero}
	 */
	Codec<Long> positiveLong();

	/**
	 * {@return Long Codec that validates the long is within the range, and if not produces the given error}
	 *
	 * @param min          Min value inclusive.
	 * @param max          Max value inclusive.
	 * @param errorMessage Error message producer.
	 */
	Codec<Long> longRangeWithMessage(long min, long max, Function<Long, String> errorMessage);

	/**
	 * Helper to create a Codec for a map that logs an error but does not fail on invalid keys. Invalid values still cause a failure though.
	 *
	 * @param keyCodec     Codec to serialize the keys with. Does not have to be string serializable.
	 * @param elementCodec Codec to serialize the values with.
	 */
	<K, V> Codec<Map<K, V>> lenientKeyUnboundedMap(MapCodec<K> keyCodec, MapCodec<V> elementCodec, MapProcessor<K, V> processor);

	/**
	 * Helper to create a Codec for a map that logs an error but does not fail on invalid keys. Invalid values still cause a failure though.
	 *
	 * @param keyCodec     Codec to serialize the keys with. Does not have to be string serializable.
	 * @param elementCodec Codec to serialize the values with.
	 */
	default <K, V> Codec<Map<K, V>> lenientKeyUnboundedMap(MapCodec<K> keyCodec, MapCodec<V> elementCodec) {
		return lenientKeyUnboundedMap(keyCodec, elementCodec, MapProcessor.putIfAbsent());
	}

	/**
	 * Helper to create a Codec for an unbounded map. Unlike {@link Codec#unboundedMap(Codec, Codec)} this method allows for a custom merge function on duplicate keys,
	 * and does not require keys to be string serializable.
	 *
	 * @param keyCodec     Codec to serialize the keys with. Does not have to be string serializable.
	 * @param elementCodec Codec to serialize the values with.
	 */
	<K, V> Codec<Map<K, V>> unboundedMap(MapCodec<K> keyCodec, MapCodec<V> elementCodec, MapProcessor<K, V> processor);

	/**
	 * Helper to create a Codec for an unbounded map. Unlike {@link Codec#unboundedMap(Codec, Codec)} this method does not require keys to be string serializable.
	 *
	 * @param keyCodec     Codec to serialize the keys with. Does not have to be string serializable.
	 * @param elementCodec Codec to serialize the values with.
	 */
	default <K, V> Codec<Map<K, V>> unboundedMap(MapCodec<K> keyCodec, MapCodec<V> elementCodec) {
		return unboundedMap(keyCodec, elementCodec, MapProcessor.putIfAbsent());
	}

	/**
	 * Helper to validate that the element being passed into the codec is not null and if it is produce the given error.
	 *
	 * @param codec        Codec
	 * @param errorMessage Error message to produce if the element is null.
	 */
	default <T> Codec<T> validatePresent(Codec<T> codec, Supplier<String> errorMessage) {
		return codec.validate(t -> t == null ? DataResult.error(errorMessage) : DataResult.success(t));
	}

	/**
	 * Similar to {@link MapCodec#orElse(Consumer, Object)} but logs the error instead of just remapping it on the result
	 *
	 * @param codec    Codec
	 * @param fallback Fallback value for if an error is encountered.
	 * @param onError  Supplier providing the string to log. Should contain {@code {}} to include the data result's error.
	 */
	<T> MapCodec<T> orElseWithLog(MapCodec<T> codec, T fallback, Supplier<String> onError);

	/**
	 * Helper to convert a codec of maps to one that decodes into {@link HashMap mutable maps}.
	 *
	 * @param codec Base codec for serializing and deserializing a map.
	 *
	 * @implNote Only modifies the decoding and leaves encoding alone.
	 */
	default <K, V> Codec<Map<K, V>> modifiableMap(Codec<Map<K, V>> codec) {
		return modifiableMap(codec, HashMap::new);
	}

	/**
	 * Helper to convert a codec of maps to one that decodes into mutable maps.
	 *
	 * @param codec          Base codec for serializing and deserializing a map.
	 * @param mapConstructor Converts the immutable map to a mutable one.
	 *
	 * @implNote Only modifies the decoding and leaves encoding alone.
	 */
	@SuppressWarnings("unchecked")
	default <K, V, M extends Map<K, V>> Codec<M> modifiableMap(Codec<Map<K, V>> codec, Function<Map<K, V>, M> mapConstructor) {
		return Codec.of((Codec<M>) codec, codec.map(mapConstructor));
	}

	/**
	 * Helper method to wrap a collection in an optional if it is not empty.
	 *
	 * @param collection Collection to check and wrap.
	 */
	default <COLLECTION extends Collection<?>> Optional<COLLECTION> ifNotEmpty(COLLECTION collection) {
		return ifNotEmpty(collection, Collection::isEmpty);
	}

	/**
	 * Helper method to wrap a map in an optional if it is not empty.
	 *
	 * @param map Map to check and wrap.
	 */
	default <MAP extends Map<?, ?>> Optional<MAP> ifNotEmpty(MAP map) {
		return ifNotEmpty(map, Map::isEmpty);
	}

	/**
	 * Helper method to wrap an object in an optional if it is not empty.
	 *
	 * @param obj        Object to check and wrap.
	 * @param emptyCheck Check for if the object is empty.
	 */
	default <OBJ> Optional<OBJ> ifNotEmpty(OBJ obj, Predicate<OBJ> emptyCheck) {
		return emptyCheck.test(obj) ? Optional.empty() : Optional.of(obj);
	}
}