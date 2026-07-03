package moze_intel.projecte.network.packets.to_client.knowledge;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record KnowledgeSyncChangePKT(ItemInfo change, boolean learned) implements IPEPacket {

	public static final CustomPacketPayload.Type<KnowledgeSyncChangePKT> TYPE = new CustomPacketPayload.Type<>(PECore.rl("knowledge_sync_change"));
	public static final StreamCodec<RegistryFriendlyByteBuf, KnowledgeSyncChangePKT> STREAM_CODEC = StreamCodec.composite(
			ItemInfo.STREAM_CODEC, KnowledgeSyncChangePKT::change,
			ByteBufCodecs.BOOL, KnowledgeSyncChangePKT::learned,
			KnowledgeSyncChangePKT::new
	);

	@NotNull
	@Override
	public CustomPacketPayload.Type<KnowledgeSyncChangePKT> type() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext context) {
		Player player = context.player();
		IKnowledgeProvider knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		if (knowledge != null) {
			if (learned) {
				if (!knowledge.hasKnowledge(change) && knowledge.addKnowledge(change) && player.containerMenu instanceof TransmutationContainer container) {
					container.transmutationInventory.itemLearned(change);
				}
			} else if (knowledge.hasKnowledge(change) && knowledge.removeKnowledge(change) && player.containerMenu instanceof TransmutationContainer container) {
				container.transmutationInventory.itemUnlearned(change);
			}
		}
		PECore.debugLog("** RECEIVED TRANSMUTATION KNOWLEDGE CHANGE DATA CLIENTSIDE **");
	}
}