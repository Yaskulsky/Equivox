package com.yaskulsky.equivox.utils;

import java.math.BigInteger;
import java.util.UUID;
import com.yaskulsky.equivox.api.capabilities.IKnowledgeProvider;
import com.yaskulsky.equivox.api.capabilities.PECapabilities;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Deposits EMC into a player's personal transmutation pool.
 */
public final class EmcDepositHelper {

	private EmcDepositHelper() {
	}

	public static long depositToPlayer(ServerPlayer player, long amount) {
		if (amount <= 0) {
			return 0;
		}
		IKnowledgeProvider knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		if (knowledge == null) {
			return 0;
		}
		BigInteger newEmc = knowledge.getEmc().add(BigInteger.valueOf(amount));
		knowledge.setEmc(newEmc);
		knowledge.syncEmc(player);
		PlayerHelper.updateScore(player, PlayerHelper.SCOREBOARD_EMC, newEmc);
		return amount;
	}

	@Nullable
	public static ServerPlayer findOnlineOwner(@Nullable MinecraftServer server, @Nullable UUID owner) {
		if (server == null || owner == null) {
			return null;
		}
		return server.getPlayerList().getPlayer(owner);
	}
}
