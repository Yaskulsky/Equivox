package moze_intel.projecte.gameObjs.registration;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

public class DeferredCodecHolder<R, T extends R> extends PEDeferredHolder<MapCodec<? extends R>, MapCodec<T>> {

	protected DeferredCodecHolder(@NotNull ResourceKey<MapCodec<? extends R>> key) {
		super(key);
	}
}