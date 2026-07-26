package com.yaskulsky.equivox.gameObjs.items;

import java.util.function.Consumer;
import com.yaskulsky.equivox.gameObjs.container.ArcaneTabletContainer;
import com.yaskulsky.equivox.utils.text.PELang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Portable upgraded transmutation tablet with an integrated 3x3 crafting grid that can pull from
 * learned knowledge / player EMC. Inspired by ProjectEX / BruceDelta Arcane Tablet (MIT).
 */
public class ArcaneTablet extends ItemPE {

	public ArcaneTablet(Properties props) {
		super(props);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display,
			@NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, context, display, tooltip, flags);
		tooltip.accept(PELang.TOOLTIP_ARCANE_TABLET.translate());
	}

	@NotNull
	@Override
	public InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		if (!level.isClientSide()) {
			player.openMenu(new ContainerProvider(hand), buf -> {
				buf.writeEnum(hand);
				buf.writeByte(player.getInventory().getSelectedSlot());
			});
		}
		return InteractionResult.SUCCESS;
	}

	private record ContainerProvider(InteractionHand hand) implements MenuProvider {

		@Override
		public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
			return new ArcaneTabletContainer(windowId, playerInventory, hand, playerInventory.getSelectedSlot());
		}

		@NotNull
		@Override
		public Component getDisplayName() {
			return PELang.ARCANE_TABLET.translate();
		}
	}
}
