package com.yaskulsky.equivox.utils;

import java.math.BigInteger;
import com.yaskulsky.equivox.api.ItemInfo;
import com.yaskulsky.equivox.api.capabilities.IKnowledgeProvider;
import com.yaskulsky.equivox.api.capabilities.PECapabilities;
import com.yaskulsky.equivox.api.proxy.IEMCProxy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Pulls learned items from a player's personal EMC / knowledge (Transmutation Table pool).
 * Used by Arcane Tablet transfer and the Transmutation Provider (virtual AE2/RS export).
 */
public final class KnowledgeProvideHelper {

	private KnowledgeProvideHelper() {
	}

	/**
	 * @return how many items could be provided without mutating EMC (0 if none)
	 */
	public static int simulateProvideFromKnowledge(@NotNull ServerPlayer player, @NotNull ItemStack request, int count) {
		return provide(player, request, count, true).getCount();
	}

	/**
	 * Creates up to {@code count} of the requested learned item, deducting personal EMC.
	 *
	 * @return provided stack (may be empty / smaller than requested)
	 */
	@NotNull
	public static ItemStack tryProvideFromKnowledge(@NotNull ServerPlayer player, @NotNull ItemStack request, int count) {
		return provide(player, request, count, false);
	}

	@NotNull
	public static ItemStack tryProvideFromKnowledge(@NotNull ServerPlayer player, @NotNull ItemInfo info, int count) {
		return provide(player, info.createStack(), count, false);
	}

	@NotNull
	private static ItemStack provide(@NotNull ServerPlayer player, @NotNull ItemStack request, int count, boolean simulate) {
		if (count <= 0 || request.isEmpty()) {
			return ItemStack.EMPTY;
		}
		IKnowledgeProvider knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		if (knowledge == null) {
			return ItemStack.EMPTY;
		}
		ItemInfo info = IEMCProxy.INSTANCE.getPersistentInfo(ItemInfo.fromStack(request));
		if (!knowledge.hasKnowledge(info)) {
			return ItemStack.EMPTY;
		}
		long unitEmc = IEMCProxy.INSTANCE.getValue(info);
		if (unitEmc <= 0) {
			return ItemStack.EMPTY;
		}
		ItemStack prototype = info.createStack();
		int max = Math.min(count, prototype.getMaxStackSize());
		BigInteger unit = BigInteger.valueOf(unitEmc);
		BigInteger available = knowledge.getEmc();
		int affordable = available.divide(unit).min(BigInteger.valueOf(max)).intValue();
		if (affordable <= 0) {
			return ItemStack.EMPTY;
		}
		prototype.setCount(affordable);
		if (!simulate) {
			knowledge.setEmc(available.subtract(unit.multiply(BigInteger.valueOf(affordable))));
			knowledge.syncEmc(player);
			PlayerHelper.updateScore(player, PlayerHelper.SCOREBOARD_EMC, knowledge.getEmc());
		}
		return prototype;
	}
}
