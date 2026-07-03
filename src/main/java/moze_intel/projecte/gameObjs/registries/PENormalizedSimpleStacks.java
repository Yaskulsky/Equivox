package moze_intel.projecte.gameObjs.registries;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ProjectERegistries;
import moze_intel.projecte.api.nss.NSSFake;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.gameObjs.registration.DeferredCodecHolder;
import moze_intel.projecte.gameObjs.registration.DeferredCodecRegister;

public class PENormalizedSimpleStacks {

	private PENormalizedSimpleStacks() {
	}

	public static final DeferredCodecRegister<NormalizedSimpleStack> NSS_SERIALIZERS = new DeferredCodecRegister<>(ProjectERegistries.NSS_SERIALIZER_NAME, PECore.MODID);

	public static final DeferredCodecHolder<NormalizedSimpleStack, NSSItem> ITEM = NSS_SERIALIZERS.registerCodec("item", () -> NSSItem.CODEC);
	public static final DeferredCodecHolder<NormalizedSimpleStack, NSSFluid> FLUID = NSS_SERIALIZERS.registerCodec("fluid", () -> NSSFluid.CODEC);
	public static final DeferredCodecHolder<NormalizedSimpleStack, NSSFake> FAKE = NSS_SERIALIZERS.registerCodec("fake", () -> NSSFake.CODEC);
}