package com.yaskulsky.equivox.client;

import com.yaskulsky.equivox.PECore;
import com.yaskulsky.equivox.api.capabilities.PECapabilities;
import com.yaskulsky.equivox.config.EquivoxConfig;
import com.yaskulsky.equivox.gameObjs.block_entities.DMPedestalBlockEntity;
import com.yaskulsky.equivox.gameObjs.blocks.Pedestal;
import com.yaskulsky.equivox.gameObjs.items.rings.TimeWatch;
import com.yaskulsky.equivox.network.packets.to_server.PedestalTimeBonusPKT;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Create-style scroll tuning for Watch of Flowing Time bonus ticks on a DM Pedestal.
 */
@EventBusSubscriber(modid = PECore.MODID, value = Dist.CLIENT)
public final class PedestalTimeBonusScrollHandler {

	private PedestalTimeBonusScrollHandler() {
	}

	@SubscribeEvent
	public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null || mc.gui.screen() != null) {
			return;
		}
		if (!mc.player.isShiftKeyDown()) {
			return;
		}
		if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
			return;
		}
		BlockPos pos = hit.getBlockPos();
		if (!(mc.level.getBlockState(pos).getBlock() instanceof Pedestal)) {
			return;
		}
		BlockEntity be = mc.level.getBlockEntity(pos);
		if (!(be instanceof DMPedestalBlockEntity pedestal)) {
			return;
		}
		ItemStack stack = pedestal.getInventory().getStackInSlot(0);
		if (stack.isEmpty() || !(stack.getItem() instanceof TimeWatch) || stack.getCapability(PECapabilities.PEDESTAL_ITEM_CAPABILITY) == null) {
			return;
		}
		int max = EquivoxConfig.server.effects.timePedBonus.get();
		if (max <= 0) {
			return;
		}
		int delta = event.getScrollDeltaY() > 0 ? 1 : -1;
		int next = Math.min(Math.max(pedestal.getTimeBonusTicks() + delta, 0), max);
		if (next != pedestal.getTimeBonusTicks()) {
			pedestal.setTimeBonusTicks(mc.level, pos, next);
			ClientPacketDistributor.sendToServer(new PedestalTimeBonusPKT(pos, next));
		}
		event.setCanceled(true);
	}
}
