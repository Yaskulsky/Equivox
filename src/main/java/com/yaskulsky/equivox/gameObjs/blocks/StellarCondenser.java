package com.yaskulsky.equivox.gameObjs.blocks;

import java.util.function.Consumer;
import com.yaskulsky.equivox.config.EquivoxConfig;
import com.yaskulsky.equivox.gameObjs.StellarCondenserBalance;
import com.yaskulsky.equivox.gameObjs.block_entities.StellarCondenserBlockEntity;
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

public class StellarCondenser extends BlockDirection implements PEEntityBlock<StellarCondenserBlockEntity> {

	public StellarCondenser(Properties props) {
		super(props);
	}

	@Override
	public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
			@Nullable LivingEntity placer, @NotNull ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide() && placer instanceof Player player) {
			StellarCondenserBlockEntity condenser = WorldHelper.getBlockEntity(StellarCondenserBlockEntity.class, level, pos, true);
			if (condenser != null) {
				condenser.setOwner(player.getUUID());
			}
		}
	}

	public void addTooltip(ItemStack stack, Consumer<Component> tooltip) {
		if (EquivoxConfig.client.statToolTips.get()) {
			tooltip.accept(Component.literal("Radius: " + StellarCondenserBalance.RADIUS)
					.withStyle(ChatFormatting.DARK_PURPLE));
			tooltip.accept(PELang.EMC_MAX_STORAGE.translateColored(ChatFormatting.DARK_PURPLE, ChatFormatting.BLUE,
					EMCHelper.formatEmc(StellarCondenserBlockEntity.MAX_STORAGE)));
		}
	}

	@Nullable
	@Override
	public BlockEntityTypeRegistryObject<? extends StellarCondenserBlockEntity> getType() {
		return PEBlockEntityTypes.STELLAR_CONDENSER;
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
		StellarCondenserBlockEntity condenser = WorldHelper.getBlockEntity(StellarCondenserBlockEntity.class, level, pos, true);
		if (condenser == null) {
			return 0;
		}
		return MathUtils.scaleToRedstone(condenser.getStoredEmc(), condenser.getMaximumEmc());
	}
}
