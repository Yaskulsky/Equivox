package com.yaskulsky.equivox.network.packets.to_server;

import com.yaskulsky.equivox.PECore;
import com.yaskulsky.equivox.gameObjs.container.ArcaneTabletContainer;
import com.yaskulsky.equivox.gameObjs.container.TransmutationContainer;
import com.yaskulsky.equivox.network.packets.IPEPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SearchUpdatePKT(int slot, ItemStack itemStack) implements IPEPacket {

	public static final CustomPacketPayload.Type<SearchUpdatePKT> TYPE = new CustomPacketPayload.Type<>(PECore.rl("update_search"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SearchUpdatePKT> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SearchUpdatePKT::slot,
			ItemStack.OPTIONAL_STREAM_CODEC, SearchUpdatePKT::itemStack,
			SearchUpdatePKT::new
	);

	@NotNull
	@Override
	public CustomPacketPayload.Type<SearchUpdatePKT> type() {
		return TYPE;
	}

	public SearchUpdatePKT {
		itemStack = itemStack.copy();
	}

	@Override
	public void handle(IPayloadContext context) {
		if (context.player().containerMenu instanceof TransmutationContainer container) {
			container.transmutationInventory.writeIntoOutputSlot(slot, itemStack);
		} else if (context.player().containerMenu instanceof ArcaneTabletContainer container) {
			container.transmutationInventory.writeIntoOutputSlot(slot, itemStack);
		}
	}
}