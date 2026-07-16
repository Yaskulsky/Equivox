package moze_intel.projecte.client;

import java.util.concurrent.CompletableFuture;
import moze_intel.projecte.PECore;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.data.SpriteSourceProvider;

public class PESpriteSourceProvider extends SpriteSourceProvider {

	public PESpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, lookupProvider, PECore.MODID);
	}

	@Override
	protected void gather() {
	}
}
