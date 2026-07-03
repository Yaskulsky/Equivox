package moze_intel.projecte.network.packets.to_client.alch_bag;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.registries.PEAttachmentTypes;
import moze_intel.projecte.impl.capability.AlchBagImpl.AlchemicalBagAttachment;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncAllBagDataPKT(AlchemicalBagAttachment data) implements IPEPacket {

	public static final Type<SyncAllBagDataPKT> TYPE = new Type<>(PECore.rl("sync_all_bag_data"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncAllBagDataPKT> STREAM_CODEC = AlchemicalBagAttachment.STREAM_CODEC.map(
			SyncAllBagDataPKT::new, SyncAllBagDataPKT::data
	);

	@NotNull
	@Override
	public CustomPacketPayload.Type<SyncAllBagDataPKT> type() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext context) {
		//We have to use the client's player instance rather than context#player as the first usage of this packet is sent during player login
		// which is before the player exists on the client so the context does not contain it.
		//Note: This must stay LocalPlayer to not cause classloading issues
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			player.setData(PEAttachmentTypes.ALCHEMICAL_BAGS, data);
		}
		PECore.debugLog("** RECEIVED BAGS CLIENTSIDE **");
	}
}