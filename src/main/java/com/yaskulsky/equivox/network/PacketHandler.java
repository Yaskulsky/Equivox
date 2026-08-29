package com.yaskulsky.equivox.network;

import com.yaskulsky.equivox.PECore;
import com.yaskulsky.equivox.api.capabilities.IKnowledgeProvider;
import com.yaskulsky.equivox.api.capabilities.PECapabilities;
import com.yaskulsky.equivox.gameObjs.container.ArcaneTabletContainer;
import com.yaskulsky.equivox.gameObjs.container.TransmutationContainer;
import com.yaskulsky.equivox.gameObjs.items.rings.ArchangelSmite;
import com.yaskulsky.equivox.gameObjs.registries.PEItems;
import com.yaskulsky.equivox.network.packets.IPEPacket;
import com.yaskulsky.equivox.network.packets.to_client.NovaExplosionSyncPKT;
import com.yaskulsky.equivox.network.packets.to_client.SyncEmcPKT;
import com.yaskulsky.equivox.network.packets.to_client.SyncFuelMapperPKT;
import com.yaskulsky.equivox.network.packets.to_client.container.SyncOffhandPkt;
import com.yaskulsky.equivox.network.packets.to_client.SyncWorldTransmutations;
import com.yaskulsky.equivox.network.packets.to_client.container.UpdateCondenserLockPKT;
import com.yaskulsky.equivox.network.packets.to_client.container.UpdateWindowLongPKT;
import com.yaskulsky.equivox.network.packets.to_client.alch_bag.SyncAllBagDataPKT;
import com.yaskulsky.equivox.network.packets.to_client.alch_bag.SyncBagsDataPKT;
import com.yaskulsky.equivox.network.packets.to_client.knowledge.KnowledgeSyncChangePKT;
import com.yaskulsky.equivox.network.packets.to_client.knowledge.KnowledgeSyncEmcPKT;
import com.yaskulsky.equivox.network.packets.to_client.knowledge.KnowledgeSyncInputsAndLocksPKT;
import com.yaskulsky.equivox.network.packets.to_client.knowledge.KnowledgeSyncPKT;
import com.yaskulsky.equivox.network.packets.to_server.ArcaneTabletActionPKT;
import com.yaskulsky.equivox.network.packets.to_server.ArcaneTabletRecipeTransferPKT;
import com.yaskulsky.equivox.network.packets.to_server.KeyPressPKT;
import com.yaskulsky.equivox.network.packets.to_server.PedestalTimeBonusPKT;
import com.yaskulsky.equivox.network.packets.to_server.SearchUpdatePKT;
import com.yaskulsky.equivox.network.packets.to_server.UpdateGemModePKT;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.maven.artifact.versioning.ArtifactVersion;

/**
 * Heavily based off of Mekanism's packet handler
 */
public final class PacketHandler {

	//Client to server instanced packets
	private SimplePacketPayLoad activateArchangel;

	//Server to client instanced packets
	private SimplePacketPayLoad clearKnowledge;
	private SimplePacketPayLoad updateTransmutationTargets;

	private SimplePacketPayLoad resetCooldown;

	public PacketHandler(IEventBus modEventBus, ArtifactVersion version) {
		modEventBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
			PayloadRegistrar registrar = event.registrar(version.toString());
			registerClientToServer(new PacketRegistrar(registrar, true));
			registerServerToClient(new PacketRegistrar(registrar, false));
		});
	}

	private void registerClientToServer(PacketRegistrar registrar) {
		registrar.play(KeyPressPKT.TYPE, KeyPressPKT.STREAM_CODEC);
		activateArchangel = registrar.playInstanced(PECore.rl("activate_archangel"), (ignored, context) -> {
			Player player = context.player();
			ItemStack main = player.getMainHandItem();
			ItemStack off = player.getOffhandItem();
			if (!main.isEmpty() && main.is(PEItems.ARCHANGEL_SMITE)) {
				ArchangelSmite.fireVolley(main, player);
			} else if (!off.isEmpty() && off.is(PEItems.ARCHANGEL_SMITE)) {
				ArchangelSmite.fireVolley(off, player);
			}
		});
		registrar.play(SearchUpdatePKT.TYPE, SearchUpdatePKT.STREAM_CODEC);
		registrar.play(UpdateGemModePKT.TYPE, UpdateGemModePKT.STREAM_CODEC);
		registrar.play(PedestalTimeBonusPKT.TYPE, PedestalTimeBonusPKT.STREAM_CODEC);
		registrar.play(ArcaneTabletActionPKT.TYPE, ArcaneTabletActionPKT.STREAM_CODEC);
		registrar.play(ArcaneTabletRecipeTransferPKT.TYPE, ArcaneTabletRecipeTransferPKT.STREAM_CODEC);
	}

	private void registerServerToClient(PacketRegistrar registrar) {
		resetCooldown = registrar.playInstanced(PECore.rl("reset_cooldown"), (ignored, context) -> context.player().resetAttackStrengthTicker());
		clearKnowledge = registrar.playInstanced(PECore.rl("clear_knowledge"), (ignored, context) -> {
			Player player = context.player();
			IKnowledgeProvider knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
			if (knowledge != null) {
				knowledge.clearKnowledge();
				if (player.containerMenu instanceof TransmutationContainer container) {
					container.transmutationInventory.updateClientTargets(false);
				} else if (player.containerMenu instanceof ArcaneTabletContainer container) {
					container.transmutationInventory.updateClientTargets(false);
				}
			}
		});
		registrar.play(KnowledgeSyncPKT.TYPE, KnowledgeSyncPKT.STREAM_CODEC);
		registrar.play(KnowledgeSyncEmcPKT.TYPE, KnowledgeSyncEmcPKT.STREAM_CODEC);
		registrar.play(KnowledgeSyncInputsAndLocksPKT.TYPE, KnowledgeSyncInputsAndLocksPKT.STREAM_CODEC);
		registrar.play(KnowledgeSyncChangePKT.TYPE, KnowledgeSyncChangePKT.STREAM_CODEC);
		registrar.play(NovaExplosionSyncPKT.TYPE, NovaExplosionSyncPKT.STREAM_CODEC);
		registrar.play(SyncAllBagDataPKT.TYPE, SyncAllBagDataPKT.STREAM_CODEC);
		registrar.play(SyncBagsDataPKT.TYPE, SyncBagsDataPKT.STREAM_CODEC);
		registrar.play(SyncEmcPKT.TYPE, SyncEmcPKT.STREAM_CODEC);
		registrar.play(SyncOffhandPkt.TYPE, SyncOffhandPkt.STREAM_CODEC);
		registrar.play(SyncFuelMapperPKT.TYPE, SyncFuelMapperPKT.STREAM_CODEC);
		registrar.play(SyncWorldTransmutations.TYPE, SyncWorldTransmutations.STREAM_CODEC);
		registrar.play(UpdateCondenserLockPKT.TYPE, UpdateCondenserLockPKT.STREAM_CODEC);
		updateTransmutationTargets = registrar.playInstanced(PECore.rl("update_transmutation_targets"), (ignored, context) -> {
			if (context.player().containerMenu instanceof TransmutationContainer container) {
				container.transmutationInventory.updateClientTargets(false);
			} else if (context.player().containerMenu instanceof ArcaneTabletContainer container) {
				container.transmutationInventory.updateClientTargets(false);
			}
		});
		registrar.play(UpdateWindowLongPKT.TYPE, UpdateWindowLongPKT.STREAM_CODEC);
	}

	public void clearKnowledge(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, clearKnowledge);
	}

	public void updateTransmutationTargets(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, updateTransmutationTargets);
	}

	public void resetCooldown(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, resetCooldown);
	}

	public void activateArchangel() {
		ClientPacketDistributor.sendToServer(activateArchangel);
	}

	protected record SimplePacketPayLoad(CustomPacketPayload.Type<CustomPacketPayload> type) implements CustomPacketPayload {

		private SimplePacketPayLoad(Identifier id) {
			this(new CustomPacketPayload.Type<>(id));
		}
	}

	protected record PacketRegistrar(PayloadRegistrar registrar, boolean toServer) {

		public <MSG extends IPEPacket> void play(CustomPacketPayload.Type<MSG> type, StreamCodec<? super RegistryFriendlyByteBuf, MSG> reader) {
			if (toServer) {
				registrar.playToServer(type, reader, IPEPacket::handle);
			} else {
				registrar.playToClient(type, reader, IPEPacket::handle);
			}
		}

		public SimplePacketPayLoad playInstanced(Identifier id, IPayloadHandler<CustomPacketPayload> handler) {
			SimplePacketPayLoad payload = new SimplePacketPayLoad(id);
			if (toServer) {
				registrar.playToServer(payload.type(), StreamCodec.unit(payload), handler);
			} else {
				registrar.playToClient(payload.type(), StreamCodec.unit(payload), handler);
			}
			return payload;
		}
	}
}