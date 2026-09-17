package com.yaskulsky.equivox.integration.refinedstorage;

import com.refinedmods.refinedstorage.api.network.impl.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.common.support.network.NetworkNodeContainerProviderImpl;
import com.refinedmods.refinedstorage.common.support.network.SimpleConnectionStrategy;
import com.yaskulsky.equivox.gameObjs.block_entities.TransmutationProviderBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * RS network attachment for {@link TransmutationProviderBlockEntity}: learned items appear in Grid / Wireless Grid.
 */
public final class TransmutationRsNetwork {

	private final TransmutationProviderBlockEntity blockEntity;
	private final ExternalStorageNetworkNode networkNode;
	private final InWorldNetworkNodeContainer container;
	private final NetworkNodeContainerProviderImpl containerProvider = new NetworkNodeContainerProviderImpl();
	private boolean initialized;

	public TransmutationRsNetwork(TransmutationProviderBlockEntity blockEntity) {
		this.blockEntity = blockEntity;
		long energy = Platform.INSTANCE.getConfig().getExternalStorage().getEnergyUsage();
		this.networkNode = new ExternalStorageNetworkNode(energy, () -> energy);
		this.container = RefinedStorageApi.INSTANCE.createNetworkNodeContainer(blockEntity, networkNode)
				.name("Equivox Transmutation")
				.connectionStrategy(new SimpleConnectionStrategy(blockEntity.getBlockPos()))
				.build();
		containerProvider.addContainer(container);
	}

	public NetworkNodeContainerProvider getContainerProvider() {
		return containerProvider;
	}

	public void onLoad(@Nullable Level level) {
		if (level == null || level.isClientSide()) {
			return;
		}
		if (!initialized) {
			RefinedStorageApi.INSTANCE.initializeNetworkNodeContainer(container, level, blockEntity::setChanged);
			initialized = true;
		}
		refresh(level);
	}

	public void refresh(@Nullable Level level) {
		if (level == null || level.isClientSide() || !initialized) {
			return;
		}
		if (blockEntity.isExportActive()) {
			networkNode.initialize(new KnowledgeRsExternalStorageProvider(blockEntity));
		} else {
			networkNode.initialize(EmptyRsExternalStorageProvider.INSTANCE);
		}
		if (networkNode.detectChanges()) {
			RefinedStorageApi.INSTANCE.updateNetworkNodeContainer(container, level);
		}
	}

	public void onRemoved(@Nullable Level level) {
		if (level == null || level.isClientSide() || !initialized) {
			return;
		}
		RefinedStorageApi.INSTANCE.removeNetworkNodeContainer(container, level);
		initialized = false;
	}

	void onNeighborChanged(ServerLevel level) {
		refresh(level);
	}
}
