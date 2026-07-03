package moze_intel.projecte.api.nss;

import net.minecraft.core.component.DataComponentPatch;
import org.jetbrains.annotations.NotNull;

/**
 * An extension of {@link NormalizedSimpleStack} that allows for representing stacks that are both "simple" and can have a {@link DataComponentPatch} attached.
 */
public interface NSSDataComponentHolder extends NormalizedSimpleStack {

	/**
	 * Gets the {@link DataComponentPatch} containing the modified data that this {@link NSSDataComponentHolder} has.
	 */
	@NotNull
	DataComponentPatch getComponentsPatch();

	/**
	 * Checks if this {@link NSSDataComponentHolder} has an associated {@link DataComponentPatch}.
	 *
	 * @return True if this {@link NSSDataComponentHolder} has an associated {@link DataComponentPatch}, false otherwise.
	 */
	default boolean hasModifiedData() {
		return !getComponentsPatch().isEmpty();
	}
}