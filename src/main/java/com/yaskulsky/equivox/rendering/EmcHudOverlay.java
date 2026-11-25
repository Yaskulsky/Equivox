package com.yaskulsky.equivox.rendering;

import java.math.BigInteger;
import com.yaskulsky.equivox.api.capabilities.IKnowledgeProvider;
import com.yaskulsky.equivox.api.capabilities.PECapabilities;
import com.yaskulsky.equivox.config.EquivoxConfig;
import com.yaskulsky.equivox.utils.TransmutationEMCFormatter;
import com.yaskulsky.equivox.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.jetbrains.annotations.NotNull;

/**
 * HUD showing personal transmutation EMC at the vanilla left-overlay slot
 * (same place as Project Infinity's {@code EMC:} line: x=2, y=11).
 */
public class EmcHudOverlay implements GuiLayer {

	/** Vanilla left debug/overlay text uses 0xFFFFFF55 yellow with drop shadow. */
	private static final int HUD_COLOR = 0xFFFFFF55;
	private final Minecraft mc = Minecraft.getInstance();

	@Override
	public void render(@NotNull GuiGraphicsExtractor graphics, @NotNull DeltaTracker delta) {
		if (!EquivoxConfig.client.emcHud.get()) {
			return;
		}
		if (mc.player == null || mc.level == null) {
			return;
		}
		IKnowledgeProvider knowledge = mc.player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		if (knowledge == null) {
			return;
		}
		BigInteger emc = knowledge.getEmc();
		Component value = TransmutationEMCFormatter.formatEMC(emc);
		Component line = PELang.EMC_TOOLTIP.translateColored(ChatFormatting.YELLOW, value);
		int x = EquivoxConfig.client.emcHudX.get();
		int y = EquivoxConfig.client.emcHudY.get();
		graphics.text(mc.font, line, x, y, HUD_COLOR, true);
	}
}
