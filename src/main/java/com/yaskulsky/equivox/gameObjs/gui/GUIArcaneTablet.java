package com.yaskulsky.equivox.gameObjs.gui;

import java.math.BigInteger;
import com.yaskulsky.equivox.PECore;
import com.yaskulsky.equivox.gameObjs.container.ArcaneTabletContainer;
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
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * Arcane Tablet GUI: transmutation + left-side crafting panel.
 * Search-bar focus handling mirrors {@link GUITransmutation}.
 */
public class GUIArcaneTablet extends PEContainerScreen<ArcaneTabletContainer> {

	private static final BigInteger MAX_EXACT_TRANSMUTATION_DISPLAY = BigInteger.valueOf(1_000_000_000_000L);
	private static final int SEARCH_BOX_X = 7;
	private static final int SEARCH_BOX_Y = 6;
	private static final int SEARCH_BOX_WIDTH = 162;
	private static final int SEARCH_BOX_HEIGHT = 12;
	private static final Component SEARCH_HINT = Component.literal("Search...");
	private static final Identifier TEXTURE = PECore.rl("textures/gui/arcane_tablet.png");

	private final TransmutationInventory inv;
	private EditBox textBoxFilter;
	private Button previous, next, rotate, balance, clear;

	public GUIArcaneTablet(ArcaneTabletContainer container, Inventory invPlayer, Component title) {
		super(container, invPlayer, title, 176, 217);
		this.inv = container.transmutationInventory;
		this.inventoryLabelY = this.imageHeight + 100;
	}

	@Override
	public void init() {
		super.init();

		this.textBoxFilter = addRenderableWidget(new EditBox(this.font, leftPos + SEARCH_BOX_X, topPos + SEARCH_BOX_Y,
				SEARCH_BOX_WIDTH, SEARCH_BOX_HEIGHT, Component.empty()));
		this.textBoxFilter.setMaxLength(64);
		this.textBoxFilter.setBordered(true);
		this.textBoxFilter.setHint(SEARCH_HINT);
		this.textBoxFilter.setResponder(inv::updateFilter);

		previous = addRenderableWidget(Button.builder(Component.literal("<"), b -> inv.previousPage())
				.pos(leftPos + 7, topPos + 20)
				.size(18, 18)
				.build());
		next = addRenderableWidget(Button.builder(Component.literal(">"), b -> inv.nextPage())
				.pos(leftPos + 151, topPos + 20)
				.size(18, 18)
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
		PEGuiGraphics.blit(graphics, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		PEGuiGraphics.blit(graphics, TEXTURE, leftPos - 75, topPos + 10, 180, 32, 76, 89);
		if (menu.isCrafting) {
			PEGuiGraphics.blit(graphics, TEXTURE, leftPos - 50, topPos + 76, 177, 19, 18, 12);
		}
	}

	@Override
	protected void extractLabels(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		peExtractLabels(graphics, mouseX, mouseY);
	}

	@Override
	protected void peExtractLabels(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		graphics.text(font, title, 8, -10, PEGuiGraphics.LABEL_COLOR, false);
		Component emc = TransmutationEMCFormatter.formatEMC(inv.getAvailableEmc());
		graphics.text(font, PELang.EMC_TOOLTIP.translate(""), 8, 123, PEGuiGraphics.LABEL_COLOR, false);
		graphics.text(font, emc, 8, 133, PEGuiGraphics.LABEL_COLOR, false);

		if (inv.learnFlag > 0) {
			graphics.text(font, PELang.TRANSMUTATION_LEARNED_1.translate(), 98, 45, PEGuiGraphics.LABEL_COLOR, false);
			inv.learnFlag--;
		}
		if (inv.unlearnFlag > 0) {
			graphics.text(font, PELang.TRANSMUTATION_UNLEARNED_1.translate(), 97, 45, PEGuiGraphics.LABEL_COLOR, false);
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
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY < 0 && inv.hasNextPage()) {
			inv.nextPage();
			return true;
		}
		if (scrollY > 0 && inv.hasPreviousPage()) {
			inv.previousPage();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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
			int emcLeft = leftPos + 6;
			int emcRight = emcLeft + 80;
			int emcTop = topPos + 120;
			int emcBottom = emcTop + 20;
			if (mouseX > emcLeft && mouseX < emcRight && mouseY > emcTop && mouseY < emcBottom) {
				graphics.setTooltipForNextFrame(font, PELang.EMC_TOOLTIP.translate(EMCHelper.formatEmc(emcAmount)), mouseX, mouseY);
				return;
			}
		}
		super.extractTooltip(graphics, mouseX, mouseY);
	}
}
