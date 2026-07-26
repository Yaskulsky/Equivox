package com.yaskulsky.equivox.gameObjs.blocks;

import java.util.function.Consumer;
import com.yaskulsky.equivox.config.EquivoxConfig;
import com.yaskulsky.equivox.gameObjs.EnumEntropySinkTier;
import com.yaskulsky.equivox.gameObjs.block_entities.EntropySinkBlockEntity;
import com.yaskulsky.equivox.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import com.yaskulsky.equivox.gameObjs.registries.PEBlockEntityTypes;
import com.yaskulsky.equivox.utils.EMCHelper;
import com.yaskulsky.equivox.utils.MathUtils;
import com.yaskulsky.equivox.utils.WorldHelper;
import com.yaskulsky.equivox.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntropySink extends BlockDirection implements PEEntityBlock<EntropySinkBlockEntity> {

	private final EnumEntropySinkTier tier;

	public EntropySink(EnumEntropySinkTier tier, Properties props) {
		super(props);
		this.tier = tier;
	}

	public EnumEntropySinkTier getTier() {
		return tier;
	}

	@Override
	public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
			@Nullable LivingEntity placer, @NotNull ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide() && placer instanceof Player player) {
			EntropySinkBlockEntity sink = WorldHelper.getBlockEntity(EntropySinkBlockEntity.class, level, pos, true);
			if (sink != null) {
				sink.setOwner(player.getUUID());
			}
		}
	}

	public void addTooltip(ItemStack stack, Consumer<Component> tooltip) {
		if (EquivoxConfig.client.statToolTips.get()) {
			tooltip.accept(PELang.EMC_MAX_STORAGE.translateColored(ChatFormatting.DARK_PURPLE, ChatFormatting.BLUE,
					EMCHelper.formatEmc(tier.getStorage())));
		}
	}

	@Nullable
	@Override
	public BlockEntityTypeRegistryObject<? extends EntropySinkBlockEntity> getType() {
		return switch (tier) {
			case BASIC -> PEBlockEntityTypes.ENTROPY_SINK;
			case DARK -> PEBlockEntityTypes.ENTROPY_SINK_DARK;
			case RED -> PEBlockEntityTypes.ENTROPY_SINK_RED;
		};
	}

	@Override
	@Deprecated
	public boolean triggerEvent(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		return triggerBlockEntityEvent(state, level, pos, id, param);
	}

	@Override
	@Deprecated
	public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
		return true;
	}

	@Deprecated
	public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Direction direction) {
		EntropySinkBlockEntity sink = WorldHelper.getBlockEntity(EntropySinkBlockEntity.class, level, pos, true);
		if (sink == null) {
			return 0;
		}
		return MathUtils.scaleToRedstone(sink.getStoredEmc(), sink.getMaximumEmc());
	}
}
