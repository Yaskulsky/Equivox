package moze_intel.projecte.api.world_transmutation;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IWorldTransmutationFunction {

	/**
	 * Gets the result of applying this world transmutation to a given state.
	 *
	 * @param state      Input state to try and match.
	 * @param isSneaking Whether the player is sneaking and the alternate result should be used if present.
	 *
	 * @return The resulting state, or {@code null} if the block can't be transmuted.
	 */
	@Nullable
	BlockState result(@NotNull BlockState state, boolean isSneaking);
}