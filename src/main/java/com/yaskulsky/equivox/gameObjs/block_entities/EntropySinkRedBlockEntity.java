package com.yaskulsky.equivox.gameObjs.block_entities;

import com.yaskulsky.equivox.gameObjs.EnumEntropySinkTier;
import com.yaskulsky.equivox.gameObjs.registries.PEBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class EntropySinkRedBlockEntity extends EntropySinkBlockEntity {

	public EntropySinkRedBlockEntity(BlockPos pos, BlockState state) {
		super(PEBlockEntityTypes.ENTROPY_SINK_RED, pos, state, EnumEntropySinkTier.RED);
	}
}
