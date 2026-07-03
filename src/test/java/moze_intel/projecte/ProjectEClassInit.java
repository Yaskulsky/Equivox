package moze_intel.projecte;

import moze_intel.projecte.api.codec.IPECodecHelper;
import moze_intel.projecte.api.components.IComponentProcessorHelper;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import net.neoforged.fml.common.Mod;

/**
 * Add an extra injection point for all our service loaders to force load them while we know for a fact we are using the transforming class loader, as there seems to be
 * some issue with {@link net.neoforged.fml.junit.JUnitService}, and presumably the fact it doesn't use {@link org.junit.platform.launcher.LauncherInterceptor} which
 * causes all the test methods to actually be executed using the app class loader.
 */
@Mod(PECore.MODID)
public class ProjectEClassInit {//TODO: Remove this as soon as possible

	public ProjectEClassInit() {
		forceInit(IComponentProcessorHelper.INSTANCE);
		forceInit(IEMCProxy.INSTANCE);
		forceInit(ITransmutationProxy.INSTANCE);
		forceInit(IPECodecHelper.INSTANCE);
	}

	private void forceInit(Object obj) {
	}
}