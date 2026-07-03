package moze_intel.projecte.config;

import moze_intel.projecte.config.value.CachedBooleanValue;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * For config options that only the client cares about
 */
public class ClientConfig extends BasePEConfig {

	private final ModConfigSpec configSpec;

	public final CachedBooleanValue emcToolTips;
	public final CachedBooleanValue shiftEmcToolTips;
	public final CachedBooleanValue shiftLearnedToolTips;
	public final CachedBooleanValue pedestalToolTips;
	public final CachedBooleanValue statToolTips;
	public final CachedBooleanValue tagToolTips;

	public final CachedBooleanValue pulsatingOverlay;

	ClientConfig() {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

		pulsatingOverlay = CachedBooleanValue.wrap(this, PEConfigTranslations.CLIENT_PHILO_OVERLAY.applyToBuilder(builder).define("pulsatingOverlay", false));

		PEConfigTranslations.CLIENT_TOOLTIPS.applyToBuilder(builder).push("tooltips");
		emcToolTips = CachedBooleanValue.wrap(this, PEConfigTranslations.CLIENT_TOOLTIPS_EMC.applyToBuilder(builder).define("emc", true));
		shiftEmcToolTips = CachedBooleanValue.wrap(this, PEConfigTranslations.CLIENT_TOOLTIPS_EMC_SHIFT.applyToBuilder(builder)
				.define("shift_emc", false));
		shiftLearnedToolTips = CachedBooleanValue.wrap(this, PEConfigTranslations.CLIENT_TOOLTIPS_LEARNED_SHIFT.applyToBuilder(builder)
				.define("shift_learned", true));
		pedestalToolTips = CachedBooleanValue.wrap(this, PEConfigTranslations.CLIENT_TOOLTIPS_PEDESTAL.applyToBuilder(builder)
				.define("pedestal", true));
		statToolTips = CachedBooleanValue.wrap(this, PEConfigTranslations.CLIENT_TOOLTIPS_STATS.applyToBuilder(builder).define("statToolTips", true));
		tagToolTips = CachedBooleanValue.wrap(this, PEConfigTranslations.CLIENT_TOOLTIPS_TAGS.applyToBuilder(builder).define("tag", false));
		builder.pop();

		configSpec = builder.build();
	}

	@Override
	public String getFileName() {
		return "client";
	}

	@Override
	public String getTranslation() {
		return "Client Config";
	}

	@Override
	public ModConfigSpec getConfigSpec() {
		return configSpec;
	}

	@Override
	public ModConfig.Type getConfigType() {
		return ModConfig.Type.CLIENT;
	}
}