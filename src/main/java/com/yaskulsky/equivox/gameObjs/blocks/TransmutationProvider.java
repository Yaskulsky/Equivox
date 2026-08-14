package com.yaskulsky.equivox.gameObjs.blocks;

import java.util.function.Consumer;
import com.yaskulsky.equivox.config.EquivoxConfig;
import com.yaskulsky.equivox.gameObjs.block_entities.TransmutationProviderBlockEntity;
import com.yaskulsky.equivox.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import com.yaskulsky.equivox.gameObjs.registries.PEBlockEntityTypes;
import com.yaskulsky.equivox.utils.WorldHelper;
import com.yaskulsky.equivox.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Owner-bound bridge placed under a Transmutation Table. AE2/RS storage buses see learned items paid from personal EMC.
 */
public class TransmutationProvider extends BlockDirection implements PEEntityBlock<TransmutationProviderBlockEntity> {

	/** AE2-style face light: true when table linked, owner online, and export active. */
	public static final BooleanProperty ONLINE = BooleanProperty.create("online");

	public TransmutationProvider(Properties props) {
		super(props);
		this.registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(ONLINE, false));
	}

	@Override
	protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> props) {
		super.createBlockStateDefinition(props);
		props.add(ONLINE);
	}

	@Override
	public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
			@Nullable LivingEntity placer, @NotNull ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide() && placer instanceof Player player) {
			TransmutationProviderBlockEntity be = WorldHelper.getBlockEntity(TransmutationProviderBlockEntity.class, level, pos, true);
			if (be != null) {
				be.setOwner(player.getUUID());
				be.onAboveChanged();
			}
		}
	}

	@Override
	protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighbor,
			@Nullable Orientation orientation, boolean movedByPiston) {
		super.neighborChanged(state, level, pos, neighbor, orientation, movedByPiston);
		if (!level.isClientSide()) {
			TransmutationProviderBlockEntity be = WorldHelper.getBlockEntity(TransmutationProviderBlockEntity.class, level, pos, true);
			if (be != null) {
				be.onAboveChanged();
			}
		}
	}

	public void addTooltip(ItemStack stack, Consumer<Component> tooltip) {
		if (EquivoxConfig.client.statToolTips.get()) {
			tooltip.accept(PELang.TRANSMUTATION_PROVIDER_TOOLTIP.translate().withStyle(ChatFormatting.DARK_AQUA));
		}
	}

	@Nullable
	@Override
	public BlockEntityTypeRegistryObject<? extends TransmutationProviderBlockEntity> getType() {
		return PEBlockEntityTypes.TRANSMUTATION_PROVIDER;
	}

	@NotNull
	@Override
	@Deprecated
	protected InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
			@NotNull Player player, @NotNull BlockHitResult rtr) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		TransmutationProviderBlockEntity be = WorldHelper.getBlockEntity(TransmutationProviderBlockEntity.class, level, pos, true);
		if (be != null) {
			player.openMenu(be, pos);
		}
		return InteractionResult.CONSUME;
	}

	@Override
	@Deprecated
	public boolean triggerEvent(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		return triggerBlockEntityEvent(state, level, pos, id, param);
	}
}
