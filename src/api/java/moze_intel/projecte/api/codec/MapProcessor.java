package moze_intel.projecte.api.codec;

import java.util.Map;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface MapProcessor<KEY, VALUE> {

	MapProcessor<?, ?> PUT_IF_ABSENT = Map::putIfAbsent;

	/**
	 * Default processor that tries to add an entry to the map, but doesn't override existing.
	 */
	@SuppressWarnings("unchecked")
	static <KEY, VALUE> MapProcessor<KEY, VALUE> putIfAbsent() {
		return (MapProcessor<KEY, VALUE>) PUT_IF_ABSENT;
	}

	/**
	 * Used to process what happens when attempting to add a key value pair to a map.
	 * @param map   Map to modify.
	 * @param key   Key to add.
	 * @param value Value to add.
	 * @return null, or the value that was already stored if this processor should prevent duplicate keys.
	 */
	@Nullable
	VALUE addElement(Map<KEY, VALUE> map, KEY key, VALUE value);
}