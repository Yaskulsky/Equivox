package moze_intel.projecte.utils.text;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.TranslatableEnum;
import org.jetbrains.annotations.NotNull;

/**
 * @apiNote From Mekanism
 */
public interface IHasTranslationKey {

	String getTranslationKey();

	interface IHasEnumNameTranslationKey extends IHasTranslationKey, TranslatableEnum {

		@NotNull
		@Override
		default Component getTranslatedName() {
			return TextComponentUtil.translate(getTranslationKey());
		}
	}
}