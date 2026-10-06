package com.yaskulsky.equivox.gameObjs.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.yaskulsky.equivox.api.block_entity.IDMPedestal;
import com.yaskulsky.equivox.api.capabilities.item.IPedestalItem;
import com.yaskulsky.equivox.api.capabilities.item.IProjectileShooter;
import com.yaskulsky.equivox.config.EquivoxConfig;
import com.yaskulsky.equivox.gameObjs.entity.EntityWaterProjectile;
import com.yaskulsky.equivox.gameObjs.registries.PESoundEvents;
import com.yaskulsky.equivox.integration.IntegrationHelper;
import com.yaskulsky.equivox.utils.ClientKeyHelper;
import com.yaskulsky.equivox.utils.LevelHelper;
import com.yaskulsky.equivox.utils.MathUtils;
import com.yaskulsky.equivox.utils.PEKeybind;
import com.yaskulsky.equivox.utils.PlayerHelper;
import com.yaskulsky.equivox.utils.WorldHelper;
import com.yaskulsky.equivox.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.InfiniteResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.NotNull;

public class EvertideAmulet extends ItemPE implements IProjectileShooter, IPedestalItem, ICapabilityAware, ISelfCraftingRemainder {

	public EvertideAmulet(Properties props) {
		super(props);
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Player player = ctx.getPlayer();
		if (player == null) {
			return InteractionResult.FAIL;
		}
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		if (!level.isClientSide() && PlayerHelper.hasEditPermission(player, level, pos)) {
			Direction sideHit = ctx.getClickedFace();
			ResourceHandler<FluidResource> fluidHandler = WorldHelper.getFluidHandler(level, pos, sideHit);
			if (fluidHandler != null) {
				ResourceHandlerUtil.insertStacking(fluidHandler, FluidResource.of(Fluids.WATER), FluidType.BUCKET_VOLUME, null);
				return InteractionResult.CONSUME;
			}
			WorldHelper.placeFluid(player, level, pos, sideHit, Fluids.WATER, !EquivoxConfig.server.items.opEvertide.get());
			level.playSound(null, player.getX(), player.getY(), player.getZ(), PESoundEvents.WATER_MAGIC.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		return WorldHelper.sidedSuccess(level);
	}

	@Override
	public boolean shootProjectile(@NotNull Player player, @NotNull ItemStack stack, InteractionHand hand) {
		Level level = player.level();
		if (EquivoxConfig.server.items.opEvertide.get() || level.dimension() != Level.NETHER) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(), PESoundEvents.WATER_MAGIC.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			EntityWaterProjectile ent = new EntityWaterProjectile(player, level);
			ent.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 1.5F, 1);
			level.addFreshEntity(ent);
			return true;
		}
		return false;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, context, display, tooltip, flags);
		tooltip.accept(PELang.TOOLTIP_EVERTIDE_1.translate(ClientKeyHelper.getKeyName(PEKeybind.FIRE_PROJECTILE)));
		tooltip.accept(PELang.TOOLTIP_EVERTIDE_2.translate());
		tooltip.accept(PELang.TOOLTIP_EVERTIDE_3.translate());
		tooltip.accept(PELang.TOOLTIP_EVERTIDE_4.translate());
	}

	@Override
	public <PEDESTAL extends BlockEntity & IDMPedestal> boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos,
			@NotNull PEDESTAL pedestal) {
		if (!level.isClientSide() && EquivoxConfig.server.cooldown.pedestal.evertide.get() != -1) {
			if (pedestal.getActivityCooldown() == 0) {
				if (level instanceof ServerLevel serverLevel) {
					int i = (300 + level.getRandom().nextInt(600)) * SharedConstants.TICKS_PER_SECOND;
					LevelHelper.setWeather(serverLevel, i, true, true);
				}
				pedestal.setActivityCooldown(level, pos, EquivoxConfig.server.cooldown.pedestal.evertide.get());
			} else {
				pedestal.decrementActivityCooldown(level, pos);
			}
		}
		return false;
	}

	@NotNull
	@Override
	public List<Component> getPedestalDescription(float tickRate) {
		List<Component> list = new ArrayList<>();
		if (EquivoxConfig.server.cooldown.pedestal.evertide.get() != -1) {
			list.add(PELang.PEDESTAL_EVERTIDE_1.translateColored(ChatFormatting.BLUE));
			list.add(PELang.PEDESTAL_EVERTIDE_2.translateColored(ChatFormatting.BLUE, MathUtils.tickToSecFormatted(EquivoxConfig.server.cooldown.pedestal.evertide.get(), tickRate)));
		}
		return list;
	}

	@Override
	public void attachCapabilities(RegisterCapabilitiesEvent event) {
		event.registerItem(Capabilities.Fluid.ITEM, (stack, context) -> new InfiniteResourceHandler<>(FluidResource.of(Fluids.WATER)), this);
		IntegrationHelper.registerCuriosCapability(event, this);
	}
}