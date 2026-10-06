package com.yaskulsky.equivox.common;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
public class PEDataMapsProvider extends DataMapProvider {

	public PEDataMapsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	protected void gather(HolderLookup.Provider provider) {
		// NeoForge 26.3 removed FurnaceFuel data maps; fuel burn times are defined in generated tags/data.
	}
}
