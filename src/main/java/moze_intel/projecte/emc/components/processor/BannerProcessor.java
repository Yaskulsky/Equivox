package moze_intel.projecte.emc.components.processor;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.api.components.IComponentProcessorHelper;
import moze_intel.projecte.config.PEConfigTranslations;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@DataComponentProcessor
public class BannerProcessor extends PersistentComponentProcessor<BannerPatternLayers> {

	@Override
	public String getName() {
		return PEConfigTranslations.DCP_BANNERS.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.DCP_BANNERS.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.DCP_BANNERS.tooltip();
	}

	@Override
	@Range(from = 0, to = Long.MAX_VALUE)
	public long recalculateEMC(@NotNull ItemInfo info, @Range(from = 1, to = Long.MAX_VALUE) long currentEMC, @NotNull BannerPatternLayers patternLayers) throws ArithmeticException {
		for (BannerPatternLayers.Layer layer : patternLayers.layers()) {
			long dyeEmc = IComponentProcessorHelper.INSTANCE.getColorEmc(layer.color());
			if (dyeEmc == 0) {//The dye doesn't have an EMC value so we can't get the emc value of the total thing
				return 0;
			}
			currentEMC = Math.addExact(currentEMC, dyeEmc);
		}
		return currentEMC;
	}

	@Override
	protected boolean validItem(@NotNull ItemInfo info) {
		return info.getItem().value() instanceof BannerItem;
	}

	@Override
	protected boolean shouldPersist(@NotNull ItemInfo info, @NotNull BannerPatternLayers component) {
		return !component.equals(BannerPatternLayers.EMPTY);
	}

	@Override
	protected DataComponentType<BannerPatternLayers> getComponentType(@NotNull ItemInfo info) {
		return DataComponents.BANNER_PATTERNS;
	}
}
