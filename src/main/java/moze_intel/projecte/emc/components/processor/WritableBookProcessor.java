package moze_intel.projecte.emc.components.processor;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.config.PEConfigTranslations;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.WritableBookContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@DataComponentProcessor
public class WritableBookProcessor extends SimplePersistentComponentProcessor<WritableBookContent> {

	@Override
	public String getName() {
		return PEConfigTranslations.DCP_WRITABLE_BOOK.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.DCP_WRITABLE_BOOK.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.DCP_WRITABLE_BOOK.tooltip();
	}

	@Override
	@Range(from = 0, to = Long.MAX_VALUE)
	public long recalculateEMC(@NotNull ItemInfo info, @Range(from = 1, to = Long.MAX_VALUE) long currentEMC) throws ArithmeticException {
		//Contents of the written book do not change the calculated EMC
		return currentEMC;
	}

	@Override
	protected DataComponentType<WritableBookContent> getComponentType(@NotNull ItemInfo info) {
		return DataComponents.WRITABLE_BOOK_CONTENT;
	}

	@Override
	protected boolean shouldPersist(@NotNull ItemInfo info, @NotNull WritableBookContent component) {
		return !component.equals(WritableBookContent.EMPTY);
	}
}
