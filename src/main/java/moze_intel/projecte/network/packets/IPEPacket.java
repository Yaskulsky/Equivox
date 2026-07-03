package moze_intel.projecte.network.packets;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface IPEPacket extends CustomPacketPayload {

	void handle(IPayloadContext context);
}