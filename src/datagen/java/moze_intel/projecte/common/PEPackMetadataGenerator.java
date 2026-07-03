package moze_intel.projecte.common;

import java.util.concurrent.CompletableFuture;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.text.IHasTranslationKey;
import net.minecraft.DetectedVersion;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

//From Mekanism's BasePackMetadataGenerator
public class PEPackMetadataGenerator extends PackMetadataGenerator {

	public PEPackMetadataGenerator(PackOutput output, IHasTranslationKey description) {
		super(output);
		add(PackMetadataSection.CLIENT_TYPE, new PackMetadataSection(
				Component.translatable(description.getTranslationKey()),
				DetectedVersion.BUILT_IN.packVersion(PackType.CLIENT_RESOURCES).minorRange()
		));
	}
}
