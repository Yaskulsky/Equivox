package moze_intel.projecte.gameObjs.items;

import moze_intel.projecte.api.capabilities.item.IModeChanger;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IItemMode<MODE extends Enum<MODE> & IModeEnum<MODE>> extends IModeChanger<MODE> {

	DataComponentType<MODE> getDataComponentType();

	MODE getDefaultMode();

	@Override
	default MODE getMode(@NotNull ItemStack stack) {
		return stack.getOrDefault(getDataComponentType(), getDefaultMode());
	}

	@Override
	default boolean changeMode(@NotNull Player player, @NotNull ItemStack stack, InteractionHand hand) {
		//Update the mode
		MODE mode = getMode(stack);
		MODE newMode = mode.next(stack);
		if (mode != newMode) {
			stack.set(getDataComponentType(), newMode);
			player.sendSystemMessage(getModeSwitchEntry().translate(newMode));
			return true;
		}
		//If we have no modes, or we are set to the only mode fail
		return false;
	}

	default ILangEntry getModeSwitchEntry() {
		return PELang.MODE_SWITCH;
	}

	default Component getToolTip(ItemStack stack) {
		return PELang.CURRENT_MODE.translate(ChatFormatting.AQUA, getMode(stack));
	}
}