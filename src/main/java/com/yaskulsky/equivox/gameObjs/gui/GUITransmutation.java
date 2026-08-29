package com.yaskulsky.equivox.gameObjs.gui;

import java.math.BigInteger;
import com.mojang.blaze3d.platform.InputConstants;
import com.yaskulsky.equivox.PECore;
import com.yaskulsky.equivox.gameObjs.container.TransmutationContainer;
import com.yaskulsky.equivox.gameObjs.container.inventory.TransmutationInventory;
import com.yaskulsky.equivox.network.packets.to_server.ArcaneTabletActionPKT;
import com.yaskulsky.equivox.utils.EMCHelper;
import com.yaskulsky.equivox.utils.TransmutationEMCFormatter;
import com.yaskulsky.equivox.utils.text.PELang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class GUITransmutation extends PEContainerScreen<TransmutationContainer> {

	private static final BigInteger MAX_EXACT_TRANSMUTATION_DISPLAY = BigInteger.valueOf(1_000_000_000_000L);
	private static final int SEARCH_BOX_X = 79;
	private static final int SEARCH_BOX_Y = 6;
	private static final int SEARCH_BOX_WIDTH = 70;
	private static final int SEARCH_BOX_HEIGHT = 10;
	private static final Component SEARCH_HINT = Component.literal("Search...");
	private static final Identifier texture = PECore.rl("textures/gui/transmute.png");
	private static final Identifier CRAFTING_PANEL = PECore.rl("textures/gui/arcane_tablet.png");

	private final TransmutationInventory inv;
	private EditBox textBoxFilter;
	private Button previous, next, rotate, balance, clear;

	public GUITransmutation(TransmutationContainer container, Inventory invPlayer, Component title) {
		super(container, invPlayer, title, 228, 196);
		this.inv = container.transmutationInventory;
		this.titleLabelX = 6;
		this.titleLabelY = 8;
		this.inventoryLabelY = this.imageHeight + 100;
	}

	@Override
	public void init() {
		super.init();

		this.textBoxFilter = addRenderableWidget(new EditBox(this.font, leftPos + SEARCH_BOX_X, topPos + SEARCH_BOX_Y, SEARCH_BOX_WIDTH, SEARCH_BOX_HEIGHT, Component.empty()));
		this.textBoxFilter.setMaxLength(64);
		this.textBoxFilter.setBordered(true);
		this.textBoxFilter.setHint(SEARCH_HINT);
		this.textBoxFilter.setResponder(inv::updateFilter);

		previous = addRenderableWidget(Button.builder(Component.literal("<"), b -> inv.previousPage())
				.pos(leftPos + 125, topPos + 100)
				.size(14, 14)
				.build());
		next = addRenderableWidget(Button.builder(Component.literal(">"), b -> inv.nextPage())
				.pos(leftPos + 193, topPos + 100)
				.size(14, 14)
				.build());
		rotate = addRenderableWidget(Button.builder(Component.literal("R"), b ->
						menu.sendAction(isShiftDown() ? ArcaneTabletActionPKT.Action.ROTATE_CC : ArcaneTabletActionPKT.Action.ROTATE))
				.pos(leftPos - 71, topPos + 16)
				.size(9, 9)
				.build());
		balance = addRenderableWidget(Button.builder(Component.literal("B"), b ->
						menu.sendAction(isShiftDown() ? ArcaneTabletActionPKT.Action.SPREAD : ArcaneTabletActionPKT.Action.BALANCE))
				.pos(leftPos - 71, topPos + 26)
				.size(9, 9)
				.build());
		clear = addRenderableWidget(Button.builder(Component.literal("X"), b ->
						menu.sendAction(isShiftDown() ? ArcaneTabletActionPKT.Action.CLEAR_FORCE : ArcaneTabletActionPKT.Action.CLEAR))
				.pos(leftPos - 71, topPos + 61)
				.size(9, 9)
				.build());
		updateButtons();
	}

	private static boolean isShiftDown() {
		var window = Minecraft.getInstance().getWindow();
		return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
				|| InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
	}

	@Override
	public void resize(int width, int height) {
		String filter = this.textBoxFilter.getValue();
		super.resize(width, height);
		this.textBoxFilter.setValue(filter);
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		updateButtons();
	}

	private void updateButtons() {
		if (previous != null) {
			previous.active = inv.hasPreviousPage();
		}
		if (next != null) {
			next.active = inv.hasNextPage();
		}
	}

	@Override
	protected void peExtractBackground(@NotNull GuiGraphicsExtractor graphics, float partialTicks, int mouseX, int mouseY) {
		PEGuiGraphics.blit(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		PEGuiGraphics.blit(graphics, CRAFTING_PANEL, leftPos - 75, topPos + 10, 180, 32, 76, 89);
		if (menu.isCrafting) {
			PEGuiGraphics.blit(graphics, CRAFTING_PANEL, leftPos - 50, topPos + 76, 177, 19, 18, 12);
		}
	}

	@Override
	protected void extractLabels(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		peExtractLabels(graphics, mouseX, mouseY);
	}

	@Override
	protected void peExtractLabels(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		graphics.text(font, title, titleLabelX, titleLabelY, PEGuiGraphics.LABEL_COLOR, false);
		graphics.text(font, PELang.EMC_TOOLTIP.translate(""), 6, this.imageHeight - 104, PEGuiGraphics.LABEL_COLOR, false);
		Component emc = TransmutationEMCFormatter.formatEMC(inv.getAvailableEmc());
		graphics.text(font, emc, 6, this.imageHeight - 94, PEGuiGraphics.LABEL_COLOR, false);
		if (inv.learnFlag > 0) {
			graphics.text(font, PELang.TRANSMUTATION_LEARNED_1.translate(), 98, 30, PEGuiGraphics.LABEL_COLOR, false);
			graphics.text(font, PELang.TRANSMUTATION_LEARNED_2.translate(), 99, 38, PEGuiGraphics.LABEL_COLOR, false);
			graphics.text(font, PELang.TRANSMUTATION_LEARNED_3.translate(), 100, 46, PEGuiGraphics.LABEL_COLOR, false);
			graphics.text(font, PELang.TRANSMUTATION_LEARNED_4.translate(), 101, 54, PEGuiGraphics.LABEL_COLOR, false);
			graphics.text(font, PELang.TRANSMUTATION_LEARNED_5.translate(), 102, 62, PEGuiGraphics.LABEL_COLOR, false);
			graphics.text(font, PELang.TRANSMUTATION_LEARNED_6.translate(), 103, 70, PEGuiGraphics.LABEL_COLOR, false);
			graphics.text(font, PELang.TRANSMUTATION_LEARNED_7.translate(), 104, 78, PEGuiGraphics.LABEL_COLOR, false);
			graphics.text(font, PELang.TRANSMUTATION_LEARNED_8.translate(), 107, 86, PEGuiGraphics.LABEL_COLOR, false);
			inv.learnFlag--;
		}
		if (inv.unlearnFlag > 0) {
			graphics.text(font, PELang.TRANSMUTATION_UNLEARNED_1.translate(), 97, 22, PEGuiGraphics.LABEL_COLOR, false);
			graphics.text(font, PELang.TRANSMUTATION_UNLEARNED_2.translate(), 98, 30, PEGuiGraphics.LABEL_COLOR, false);
			graphics.text(font, PELang.TRANSMUTATION_UNLEARNED_3.translate(), 99, 38, PEGuiGraphics.LABEL_COLOR, false);
			graphics.text(font, PELang.TRANSMUTATION_UNLEARNED_4.translate(), 100, 46, PEGuiGraphics.LABEL_COLOR, false);
			graphics.text(font, PELang.TRANSMUTATION_UNLEARNED_5.translate(), 101, 54, PEGuiGraphics.LABEL_COLOR, false);
			graphics.text(font, PELang.TRANSMUTATION_UNLEARNED_6.translate(), 102, 62, PEGuiGraphics.LABEL_COLOR, false);
			graphics.text(font, PELang.TRANSMUTATION_UNLEARNED_7.translate(), 103, 70, PEGuiGraphics.LABEL_COLOR, false);
			graphics.text(font, PELang.TRANSMUTATION_UNLEARNED_8.translate(), 104, 78, PEGuiGraphics.LABEL_COLOR, false);
			graphics.text(font, PELang.TRANSMUTATION_UNLEARNED_9.translate(), 107, 86, PEGuiGraphics.LABEL_COLOR, false);
			inv.unlearnFlag--;
		}
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (textBoxFilter.canConsumeInput()) {
			return textBoxFilter.charTyped(event);
		}
		return super.charTyped(event);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (textBoxFilter.canConsumeInput()) {
			if (event.isEscape()) {
				setFocused(null);
				return true;
			}
			return textBoxFilter.keyPressed(event);
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double x = event.x();
		double y = event.y();
		if (textBoxFilter.isMouseOver(x, y) && event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			this.textBoxFilter.setValue("");
			setFocused(this.textBoxFilter);
			return true;
		}
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}
		if (textBoxFilter.isFocused()) {
			if (hoveredSlot == null || (!hoveredSlot.hasItem() && menu.getCarried().isEmpty())) {
				setFocused(null);
			}
		}
		return false;
	}

	@Override
	public void removed() {
		super.removed();
		inv.learnFlag = 0;
		inv.unlearnFlag = 0;
	}

	@Override
	protected void extractTooltip(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		int searchLeft = leftPos + SEARCH_BOX_X;
		int searchRight = searchLeft + SEARCH_BOX_WIDTH;
		int searchTop = topPos + SEARCH_BOX_Y;
		int searchBottom = searchTop + SEARCH_BOX_HEIGHT;
		if (mouseX > searchLeft && mouseX < searchRight && mouseY > searchTop && mouseY < searchBottom) {
			graphics.setTooltipForNextFrame(font, SEARCH_HINT, mouseX, mouseY);
			return;
		}
		BigInteger emcAmount = inv.getAvailableEmc();
		if (emcAmount.compareTo(MAX_EXACT_TRANSMUTATION_DISPLAY) >= 0) {
			int emcLeft = leftPos;
			int emcRight = emcLeft + 82;
			int emcTop = 95 + topPos;
			int emcBottom = emcTop + 15;
			if (mouseX > emcLeft && mouseX < emcRight && mouseY > emcTop && mouseY < emcBottom) {
				graphics.setTooltipForNextFrame(font, PELang.EMC_TOOLTIP.translate(EMCHelper.formatEmc(emcAmount)), mouseX, mouseY);
				return;
			}
		}
		super.extractTooltip(graphics, mouseX, mouseY);
	}
}
