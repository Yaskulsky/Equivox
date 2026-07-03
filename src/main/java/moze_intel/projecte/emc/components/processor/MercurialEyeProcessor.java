package moze_intel.projecte.emc.components.processor;

import java.util.Collections;
import java.util.List;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.config.PEConfigTranslations;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.NotNull;

@DataComponentProcessor
public class MercurialEyeProcessor extends SimpleContainerProcessor<ItemContainerContents> {

	@Override
	public String getName() {
		return PEConfigTranslations.DCP_MERCURIAL_EYE.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.DCP_MERCURIAL_EYE.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.DCP_MERCURIAL_EYE.tooltip();
	}

	@Override
	protected DataComponentType<ItemContainerContents> getComponentType(@NotNull ItemInfo info) {
		return PEDataComponentTypes.EYE_INVENTORY.get();
	}

	@Override
	protected boolean shouldPersist(@NotNull ItemInfo info, @NotNull ItemContainerContents component) {
		return !component.equals(ItemContainerContents.EMPTY) && !component.copyOne().isEmpty();
	}

	@Override
	protected ItemContainerContents cleanPersistentComponent(@NotNull ItemContainerContents component) {
		if (component.getSlots() == 1) {
			return component;
		}
		return ItemContainerContents.fromItems(List.of(component.copyOne()));
	}

	@Override
	protected Iterable<ItemStack> getStoredItems(ItemContainerContents component) {
		//Note: Only the first slot is a real slot
		return Collections.singleton(component.copyOne());
	}
}
