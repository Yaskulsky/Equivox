package moze_intel.projecte.emc.pregenerated;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.codec.IPECodecHelper;
import moze_intel.projecte.api.codec.MapProcessor;
import moze_intel.projecte.impl.codec.PECodecHelper;
import moze_intel.projecte.impl.codec.PEUnboundedMapCodec;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.VisibleForTesting;

public class PregeneratedEMC {

	//Allow skipping when there are invalid entries in the map as a common case of this might be if a mod is removed after
	// emc values were pregenerated, and then it will be referencing an item that doesn't exist anymore
	@VisibleForTesting
	static final Codec<Object2LongMap<ItemInfo>> CODEC = new PEUnboundedMapCodec<>(
			ItemInfo.MAP_CODEC,
			IPECodecHelper.INSTANCE.positiveLong().fieldOf("emc"),
			MapProcessor.putIfAbsent(),
			true,
			Object2LongLinkedOpenHashMap::new,
			Object2LongMaps::unmodifiable//Note: We can just make it unmodifiable as the original map isn't used after it gets transferred to being immutable
	);

	public static Optional<Object2LongMap<ItemInfo>> read(HolderLookup.Provider registries, Path path, boolean shouldUsePregenerated) {
		if (shouldUsePregenerated && Files.isReadable(path)) {
			return PECodecHelper.readFromFile(registries, path, CODEC, "pregenerated emc");
		}
		return Optional.empty();
	}

	public static void write(HolderLookup.Provider registries, Path path, Object2LongMap<ItemInfo> map) {
		PECodecHelper.writeToFile(registries, path, CODEC, map, "pregenerated emc");
	}
}