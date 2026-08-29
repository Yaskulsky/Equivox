package com.yaskulsky.equivox.network.packets.to_server;

import com.yaskulsky.equivox.PECore;
import com.yaskulsky.equivox.gameObjs.container.IArcaneCraftingMenu;
import com.yaskulsky.equivox.network.packets.IPEPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ArcaneTabletRecipeTransferPKT(List<List<ItemStack>> recipe, boolean transferAll) implements IPEPacket {

	public static final CustomPacketPayload.Type<ArcaneTabletRecipeTransferPKT> TYPE = new CustomPacketPayload.Type<>(PECore.rl("arcane_tablet_recipe_transfer"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ArcaneTabletRecipeTransferPKT> STREAM_CODEC = StreamCodec.composite(
			ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list()), ArcaneTabletRecipeTransferPKT::recipe,
			ByteBufCodecs.BOOL, ArcaneTabletRecipeTransferPKT::transferAll,
			ArcaneTabletRecipeTransferPKT::new
	);

	@NotNull
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext context) {
		Player player = context.player();
		if (player.containerMenu instanceof IArcaneCraftingMenu container) {
			container.onRecipeTransfer(recipe, transferAll);
		}
	}
}
