package com.yaskulsky.equivox.gameObjs.blocks;

import java.util.List;
import java.util.function.Consumer;
import com.yaskulsky.equivox.api.capabilities.PECapabilities;
import com.yaskulsky.equivox.api.capabilities.item.IItemEmcHolder;
import com.yaskulsky.equivox.config.EquivoxConfig;
import com.yaskulsky.equivox.gameObjs.EnumCollectorTier;
import com.yaskulsky.equivox.gameObjs.block_entities.CollectorMK1BlockEntity;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import com.yaskulsky.equivox.api.inventory.PEItemStacksHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Collector extends BlockDirection implements PEEntityBlock<CollectorMK1BlockEntity> {

	private final EnumCollectorTier tier;

	public Collector(EnumCollectorTier tier, Properties props) {
		super(props);
		this.tier = tier;
	}

	public EnumCollectorTier getTier() {
		return tier;
	}

	@NotNull
	@Override
	@Deprecated
	protected InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		CollectorMK1BlockEntity collector = WorldHelper.getBlockEntity(CollectorMK1BlockEntity.class, level, pos, true);
		if (collector != null) {
			player.openMenu(collector, pos);
		}
		return InteractionResult.CONSUME;
	}

	public void addTooltip(ItemStack stack, Consumer<Component> tooltip) {
		if (EquivoxConfig.client.statToolTips.get()) {
			tooltip.accept(PELang.EMC_MAX_GEN_RATE.translateColored(ChatFormatting.DARK_PURPLE, ChatFormatting.BLUE, EMCHelper.formatEmc(tier.getGenRate())));
			tooltip.accept(PELang.EMC_MAX_STORAGE.translateColored(ChatFormatting.DARK_PURPLE, ChatFormatting.BLUE, EMCHelper.formatEmc(tier.getStorage())));
		}
	}

	@Nullable
	@Override
	public BlockEntityTypeRegistryObject<? extends CollectorMK1BlockEntity> getType() {
		return switch (tier) {
			case MK1 -> PEBlockEntityTypes.COLLECTOR;
			case MK2 -> PEBlockEntityTypes.COLLECTOR_MK2;
			case MK3 -> PEBlockEntityTypes.COLLECTOR_MK3;
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
		CollectorMK1BlockEntity collector = WorldHelper.getBlockEntity(CollectorMK1BlockEntity.class, level, pos, true);
		if (collector == null) {
			return 0;
		}
		ResourceHandler<ItemResource> handler = WorldHelper.getItemHandler(level, pos, state, collector, Direction.UP);
		if (handler == null) {
			return 0;
		}
		ItemStack charging = PEItemStacksHandler.getStack(handler, CollectorMK1BlockEntity.UPGRADING_SLOT);
		if (charging.isEmpty()) {
			return MathUtils.scaleToRedstone(collector.getStoredEmc(), collector.getMaximumEmc());
		}
		IItemEmcHolder emcHolder = charging.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY);
		if (emcHolder != null) {
			return MathUtils.scaleToRedstone(emcHolder.getStoredEmc(charging), emcHolder.getMaximumEmc(charging));
		}
		return MathUtils.scaleToRedstone(collector.getStoredEmc(), collector.getEmcToNextGoal());
	}

	@Override
	public void onBlockStateChange(LevelReader level, BlockPos pos, BlockState oldState, BlockState newState) {
		if (oldState.getBlock() != newState.getBlock() && level instanceof Level world) {
			CollectorMK1BlockEntity ent = WorldHelper.getBlockEntity(CollectorMK1BlockEntity.class, world, pos);
			if (ent != null) {
				ent.clearLocked();
			}
		}
		super.onBlockStateChange(level, pos, oldState, newState);
	}
}