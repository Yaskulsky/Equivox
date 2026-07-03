package moze_intel.projecte.network.packets.to_server;

import io.netty.buffer.ByteBuf;
import moze_intel.projecte.PECore;
import moze_intel.projecte.components.GemData;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.network.PEStreamCodecs;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record UpdateGemModePKT(InteractionHand hand, boolean mode) implements IPEPacket {

	public static final CustomPacketPayload.Type<UpdateGemModePKT> TYPE = new CustomPacketPayload.Type<>(PECore.rl("update_gem_mode"));
	public static final StreamCodec<ByteBuf, UpdateGemModePKT> STREAM_CODEC = StreamCodec.composite(
			PEStreamCodecs.INTERACTION_HAND, UpdateGemModePKT::hand,
			ByteBufCodecs.BOOL, UpdateGemModePKT::mode,
			UpdateGemModePKT::new
	);

	@NotNull
	@Override
	public CustomPacketPayload.Type<UpdateGemModePKT> type() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext context) {
		ItemStack stack = context.player().getItemInHand(hand);
		if (!stack.isEmpty()) {
			if (stack.is(PEItems.GEM_OF_ETERNAL_DENSITY) || stack.is(PEItems.VOID_RING)) {
				stack.update(PEDataComponentTypes.GEM_DATA, GemData.EMPTY, mode, GemData::withWhitelist);
			}
		}
	}
}