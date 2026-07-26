package com.yaskulsky.equivox.network.packets.to_server;

import java.util.function.IntFunction;
import com.yaskulsky.equivox.PECore;
import com.yaskulsky.equivox.gameObjs.container.ArcaneTabletContainer;
import com.yaskulsky.equivox.network.packets.IPEPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ArcaneTabletActionPKT(Action action) implements IPEPacket {

	public static final CustomPacketPayload.Type<ArcaneTabletActionPKT> TYPE = new CustomPacketPayload.Type<>(PECore.rl("arcane_tablet_action"));
	public static final StreamCodec<ByteBuf, ArcaneTabletActionPKT> STREAM_CODEC = Action.STREAM_CODEC.map(ArcaneTabletActionPKT::new, ArcaneTabletActionPKT::action);

	@NotNull
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext context) {
		Player player = context.player();
		if (!(player.containerMenu instanceof ArcaneTabletContainer container)) {
			return;
		}
		switch (action) {
			case CLEAR -> container.clearCrafting(false);
			case CLEAR_FORCE -> container.clearCrafting(true);
			case ROTATE -> container.rotateCrafting(true);
			case ROTATE_CC -> container.rotateCrafting(false);
			case BALANCE -> container.balanceCrafting();
			case SPREAD -> container.spreadCrafting();
		}
	}

	public enum Action {
		CLEAR,
		CLEAR_FORCE,
		ROTATE,
		ROTATE_CC,
		BALANCE,
		SPREAD;

		public static final IntFunction<Action> BY_ID = ByIdMap.continuous(Action::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
		public static final StreamCodec<ByteBuf, Action> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Action::ordinal);
	}
}
