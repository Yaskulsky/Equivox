package moze_intel.projecte.api.mapper;

import moze_intel.projecte.api.config.IConfigurableElement;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Interface for Classes that want to make Contributions to the EMC Mapping.
 *
 * @param <T> The type, that is used to uniquely identify Items/Blocks/Everything
 * @param <V> The type for the EMC Value
 */
public interface IEMCMapper<T, V extends Comparable<V>> extends IConfigurableElement {

	/**
	 * {@inheritDoc} If this returns {@code false} {@link #addMappings} will not be called.
	 */
	@Override
	default boolean isAvailable() {
		return IConfigurableElement.super.isAvailable();
	}

	/**
	 * The method that allows the {@link IEMCMapper} to contribute to the EMC Mapping. Use the methods provided by the {@link IMappingCollector}.
	 */
	void addMappings(IMappingCollector<T, V> mapper, ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager);
}