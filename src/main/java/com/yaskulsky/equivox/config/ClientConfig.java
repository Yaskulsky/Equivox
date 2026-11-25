package com.yaskulsky.equivox.config;

import com.yaskulsky.equivox.config.value.CachedBooleanValue;
import com.yaskulsky.equivox.config.value.CachedIntValue;
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
	public final CachedBooleanValue emcHud;
	public final CachedIntValue emcHudX;
	public final CachedIntValue emcHudY;

	ClientConfig() {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

		pulsatingOverlay = CachedBooleanValue.wrap(this, PEConfigTranslations.CLIENT_PHILO_OVERLAY.applyToBuilder(builder).define("pulsatingOverlay", false));
		emcHud = CachedBooleanValue.wrap(this, PEConfigTranslations.CLIENT_EMC_HUD.applyToBuilder(builder).define("emcHud", true));
		emcHudX = CachedIntValue.wrap(this, PEConfigTranslations.CLIENT_EMC_HUD_X.applyToBuilder(builder).defineInRange("emcHudX", 2, 0, 4000));
		emcHudY = CachedIntValue.wrap(this, PEConfigTranslations.CLIENT_EMC_HUD_Y.applyToBuilder(builder).defineInRange("emcHudY", 11, 0, 4000));

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