package moze_intel.projecte.config;

import moze_intel.projecte.config.value.CachedValue;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public interface IPEConfig {

	String getFileName();

	String getTranslation();

	ModConfigSpec getConfigSpec();

	default boolean isLoaded() {
		return getConfigSpec().isLoaded();
	}

	ModConfig.Type getConfigType();

	void save();

	void clearCache(boolean unloading);

	<T> void addCachedValue(CachedValue<T> configValue);
}