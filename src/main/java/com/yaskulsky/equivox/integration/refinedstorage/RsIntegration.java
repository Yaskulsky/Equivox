package com.yaskulsky.equivox.integration.refinedstorage;

import com.refinedmods.refinedstorage.neoforge.api.RefinedStorageNeoForgeApi;
import com.yaskulsky.equivox.gameObjs.registries.PEBlockEntityTypes;
import com.yaskulsky.equivox.integration.IntegrationHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class RsIntegration {

	private RsIntegration() {
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		if (!ModList.get().isLoaded(IntegrationHelper.RS_MODID)) {
			return;
		}
		event.registerBlockEntity(
				RefinedStorageNeoForgeApi.INSTANCE.getNetworkNodeContainerProviderCapability(),
				PEBlockEntityTypes.TRANSMUTATION_PROVIDER.get(),
				(be, side) -> be.rsNetwork().getContainerProvider()
		);
	}
}
