package moze_intel.projecte.api.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public interface IDMPedestal {

	/**
	 * @return Pedestal's current cooldown
	 */
	int getActivityCooldown();

	/**
	 * Sets the pedestal's cooldown
	 *
	 * @param level The level the DM Pedestal is in. (Used for saving, pass in the values that you get passed in)
	 * @param pos   The position the DM Pedestal is at. (Used for saving, pass in the values that you get passed in)
	 */
	void setActivityCooldown(@NotNull Level level, @NotNull BlockPos pos, int cooldown);

	/**
	 * Decrement pedestal cooldown
	 *
	 * @param level The level the DM Pedestal is in. (Used for saving, pass in the values that you get passed in)
	 * @param pos   The position the DM Pedestal is at. (Used for saving, pass in the values that you get passed in)
	 */
	void decrementActivityCooldown(@NotNull Level level, @NotNull BlockPos pos);

	/**
	 * @return Inclusive bounding box of all positions this pedestal should apply effects in
	 */
	AABB getEffectBounds();
}