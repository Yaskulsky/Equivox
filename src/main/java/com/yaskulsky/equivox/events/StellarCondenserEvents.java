package com.yaskulsky.equivox.events;

import java.util.Comparator;
import java.util.List;
import com.yaskulsky.equivox.PECore;
import com.yaskulsky.equivox.gameObjs.StellarCondenserBalance;
import com.yaskulsky.equivox.gameObjs.block_entities.StellarCondenserBlockEntity;
import com.yaskulsky.equivox.utils.WorldHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * Routes living-death "echo" EMC to the nearest owned Stellar Condenser in range.
 * Does not touch item drops — echo values come only from {@link StellarCondenserBlockEntity#echoEmcFor}.
 */
@EventBusSubscriber(modid = PECore.MODID)
public class StellarCondenserEvents {

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		LivingEntity dead = event.getEntity();
		if (dead.level().isClientSide() || dead instanceof Player) {
			return;
		}
		if (!(dead.level() instanceof ServerLevel level)) {
			return;
		}
		Vec3 center = dead.position();
		AABB box = dead.getBoundingBox().inflate(StellarCondenserBalance.RADIUS);
		List<BlockEntity> condensers = WorldHelper.getBlockEntitiesWithinAABB(level, box,
				be -> be instanceof StellarCondenserBlockEntity stellar && stellar.getOwner() != null);
		if (condensers.isEmpty()) {
			return;
		}
		double radiusSq = (double) StellarCondenserBalance.RADIUS * StellarCondenserBalance.RADIUS;
		condensers.sort(Comparator.comparingDouble(be -> be.getBlockPos().distToCenterSqr(center)));
		long gameTime = level.getGameTime();
		for (BlockEntity be : condensers) {
			if (!(be instanceof StellarCondenserBlockEntity stellar)) {
				continue;
			}
			if (be.getBlockPos().distToCenterSqr(center) > radiusSq) {
				continue;
			}
			if (stellar.tryGrantDeathEcho(level, dead, gameTime)) {
				return;
			}
		}
	}
}
