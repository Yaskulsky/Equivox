package moze_intel.projecte.utils;

import java.util.function.IntBinaryOperator;
import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public final class Constants {

	public static final IntBinaryOperator INT_SUM = Integer::sum;

	public static final int MAX_VEIN_SIZE = 250;
	public static final int TICKS_PER_HALF_SECOND = SharedConstants.TICKS_PER_SECOND / 2;

	/**
	 * @apiNote DO NOT MODIFY THE BACKING ARRAY
	 */
	public static final DyeColor[] COLORS = DyeColor.values();
	/**
	 * @apiNote DO NOT MODIFY THE BACKING ARRAY
	 */
	public static final Direction[] DIRECTIONS = Direction.values();
}