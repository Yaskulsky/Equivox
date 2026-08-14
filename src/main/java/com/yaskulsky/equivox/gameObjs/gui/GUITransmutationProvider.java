package com.yaskulsky.equivox.gameObjs.gui;

import com.yaskulsky.equivox.PECore;
import com.yaskulsky.equivox.gameObjs.container.TransmutationProviderContainer;
import com.yaskulsky.equivox.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GUITransmutationProvider extends PEContainerScreen<TransmutationProviderContainer> {

	private static final Identifier TEXTURE = PECore.rl("textures/gui/transmutation_provider.png");
	private static final int ONLINE_COLOR = 0xFF55FF55;
	private static final int OFFLINE_COLOR = 0xFFFF5555;

	public GUITransmutationProvider(TransmutationProviderContainer container, Inventory inv, Component title) {
		super(container, inv, title, 176, 166);
		this.inventoryLabelY = 72;
	}

	@Override
	protected void peExtractBackground(@NotNull GuiGraphicsExtractor graphics, float partialTicks, int mouseX, int mouseY) {
		PEGuiGraphics.blit(graphics, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}

	@Override
	protected void extractLabels(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		graphics.text(font, title, (imageWidth - font.width(title)) / 2, 6, PEGuiGraphics.LABEL_COLOR, false);

		boolean online = menu.isOnline();
		Component status = online
				? PELang.TRANSMUTATION_PROVIDER_ONLINE.translate().withStyle(ChatFormatting.GREEN)
				: PELang.TRANSMUTATION_PROVIDER_OFFLINE.translate().withStyle(ChatFormatting.RED);
		graphics.text(font, status, (imageWidth - font.width(status)) / 2, 22, online ? ONLINE_COLOR : OFFLINE_COLOR, false);

		int y = 36;
		graphics.text(font, menu.isLinked()
				? PELang.TRANSMUTATION_PROVIDER_LINKED.translate()
				: PELang.TRANSMUTATION_PROVIDER_UNLINKED.translate(), 14, y, PEGuiGraphics.LABEL_COLOR, false);
		y += 12;
		graphics.text(font, menu.isOwnerOnline()
				? PELang.TRANSMUTATION_PROVIDER_OWNER_ONLINE.translate()
				: PELang.TRANSMUTATION_PROVIDER_OWNER_OFFLINE.translate(), 14, y, PEGuiGraphics.LABEL_COLOR, false);
		y += 12;
		if (menu.isTomeBlocked()) {
			graphics.text(font, PELang.TRANSMUTATION_PROVIDER_TOME.translate(), 14, y, PEGuiGraphics.LABEL_COLOR, false);
		} else {
			graphics.text(font, PELang.TRANSMUTATION_PROVIDER_EXPOSED.translate(menu.getExposedCount()), 14, y, PEGuiGraphics.LABEL_COLOR, false);
		}

		graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, PEGuiGraphics.LABEL_COLOR, false);
	}
}
