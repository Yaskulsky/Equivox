package moze_intel.projecte.network.packets.to_client.knowledge;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.network.PEStreamCodecs;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record KnowledgeSyncEmcPKT(BigInteger emc) implements IPEPacket {

	public static final CustomPacketPayload.Type<KnowledgeSyncEmcPKT> TYPE = new CustomPacketPayload.Type<>(PECore.rl("knowledge_sync_emc"));
	public static final StreamCodec<ByteBuf, KnowledgeSyncEmcPKT> STREAM_CODEC = PEStreamCodecs.EMC_VALUE.map(
			KnowledgeSyncEmcPKT::new, KnowledgeSyncEmcPKT::emc
	);

	@NotNull
	@Override
	public CustomPacketPayload.Type<KnowledgeSyncEmcPKT> type() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext context) {
		Player player = context.player();
		IKnowledgeProvider knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		if (knowledge != null) {
			knowledge.setEmc(emc);
			if (player.containerMenu instanceof TransmutationContainer container) {
				container.transmutationInventory.updateClientTargets(true);
			}
		}
		PECore.debugLog("** RECEIVED TRANSMUTATION EMC DATA CLIENTSIDE **");
	}
}