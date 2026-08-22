package com.yaskulsky.equivox.client;

import com.yaskulsky.equivox.PECore;
import com.yaskulsky.equivox.gameObjs.container.DMFurnaceContainer;
import com.yaskulsky.equivox.gameObjs.entity.EntitySWRGProjectile;
import com.yaskulsky.equivox.gameObjs.gui.AbstractCollectorScreen;
import com.yaskulsky.equivox.gameObjs.gui.AbstractCondenserScreen;
import com.yaskulsky.equivox.gameObjs.gui.AlchBagScreen;
import com.yaskulsky.equivox.gameObjs.gui.AlchChestScreen;
import com.yaskulsky.equivox.gameObjs.gui.GUIDMFurnace;
import com.yaskulsky.equivox.gameObjs.gui.GUIEternalDensity;
import com.yaskulsky.equivox.gameObjs.gui.GUIMercurialEye;
import com.yaskulsky.equivox.gameObjs.gui.GUIRMFurnace;
import com.yaskulsky.equivox.gameObjs.gui.GUIRelay.GUIRelayMK1;
import com.yaskulsky.equivox.gameObjs.gui.GUIRelay.GUIRelayMK2;
import com.yaskulsky.equivox.gameObjs.gui.GUIRelay.GUIRelayMK3;
import com.yaskulsky.equivox.gameObjs.gui.GUIArcaneTablet;
import com.yaskulsky.equivox.gameObjs.gui.GUITransmutation;
import com.yaskulsky.equivox.gameObjs.gui.GUITransmutationProvider;
import com.yaskulsky.equivox.gameObjs.gui.PEContainerScreen;
import com.yaskulsky.equivox.gameObjs.registries.PEContainerTypes;
import com.yaskulsky.equivox.gameObjs.sound.MovingSoundSWRG;
import com.yaskulsky.equivox.network.commands.client.DumpMissingEmc;
import com.yaskulsky.equivox.gameObjs.registries.PEBlockEntityTypes;
import com.yaskulsky.equivox.gameObjs.registries.PEEntityTypes;
import com.yaskulsky.equivox.rendering.HomingArrowRenderer;
import com.yaskulsky.equivox.rendering.PedestalRenderer;
import com.yaskulsky.equivox.rendering.TransmutationRenderingOverlay;
import com.yaskulsky.equivox.utils.ClientKeyHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@Mod(value = PECore.MODID, dist = Dist.CLIENT)
public class PEClient {

	public PEClient(ModContainer container, IEventBus modEventBus) {
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		modEventBus.addListener(this::registerScreens);
		modEventBus.addListener(this::clientSetup);
		modEventBus.addListener(this::registerKeybindings);
		modEventBus.addListener(this::registerOverlays);
		modEventBus.addListener(this::registerRenderers);
		modEventBus.addListener(this::addLayers);

		NeoForge.EVENT_BUS.addListener(this::onEntityJoinWorld);
		NeoForge.EVENT_BUS.addListener(this::registerClientCommands);
	}

	private void onEntityJoinWorld(EntityJoinLevelEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (event.getEntity() instanceof EntitySWRGProjectile projectile && mc.mouseHandler.isMouseGrabbed()) {
			mc.getSoundManager().play(new MovingSoundSWRG(projectile, event.getLevel().getRandom()));
		}
	}

	private void registerClientCommands(RegisterClientCommandsEvent event) {
		CommandBuildContext context = event.getBuildContext();
		event.getDispatcher().register(Commands.literal("equivox")
				.then(DumpMissingEmc.register(context))
		);
	}

	private void registerScreens(RegisterMenuScreensEvent event) {
		event.register(PEContainerTypes.RM_FURNACE_CONTAINER.get(), GUIRMFurnace::new);
		//noinspection RedundantTypeArguments (necessary for it to actually compile)
		event.<DMFurnaceContainer, GUIDMFurnace<DMFurnaceContainer>>register(PEContainerTypes.DM_FURNACE_CONTAINER.get(), GUIDMFurnace::new);
		event.register(PEContainerTypes.CONDENSER_CONTAINER.get(), AbstractCondenserScreen.MK1::new);
		event.register(PEContainerTypes.CONDENSER_MK2_CONTAINER.get(), AbstractCondenserScreen.MK2::new);
		event.register(PEContainerTypes.ALCH_CHEST_CONTAINER.get(), AlchChestScreen::new);
		event.register(PEContainerTypes.ALCH_BAG_CONTAINER.get(), AlchBagScreen::new);
		event.register(PEContainerTypes.ETERNAL_DENSITY_CONTAINER.get(), GUIEternalDensity::new);
		event.register(PEContainerTypes.TRANSMUTATION_CONTAINER.get(), GUITransmutation::new);
		event.register(PEContainerTypes.ARCANE_TABLET_CONTAINER.get(), GUIArcaneTablet::new);
		event.register(PEContainerTypes.TRANSMUTATION_PROVIDER_CONTAINER.get(), GUITransmutationProvider::new);
		event.register(PEContainerTypes.RELAY_MK1_CONTAINER.get(), GUIRelayMK1::new);
		event.register(PEContainerTypes.RELAY_MK2_CONTAINER.get(), GUIRelayMK2::new);
		event.register(PEContainerTypes.RELAY_MK3_CONTAINER.get(), GUIRelayMK3::new);
		event.register(PEContainerTypes.COLLECTOR_MK1_CONTAINER.get(), AbstractCollectorScreen.MK1::new);
		event.register(PEContainerTypes.COLLECTOR_MK2_CONTAINER.get(), AbstractCollectorScreen.MK2::new);
		event.register(PEContainerTypes.COLLECTOR_MK3_CONTAINER.get(), AbstractCollectorScreen.MK3::new);
		event.register(PEContainerTypes.MERCURIAL_EYE_CONTAINER.get(), GUIMercurialEye::new);
	}

	private void clientSetup(FMLClientSetupEvent evt) {
		if (ModList.get().isLoaded("jei")) {
			//Note: This listener is only registered if JEI is loaded
			NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (ScreenEvent.Opening event) -> {
				if (event.getCurrentScreen() instanceof PEContainerScreen<?> screen) {
					//If JEI is loaded and our current screen is a Equivox gui,
					// check if the new screen is a JEI recipe screen
					if (isJeiRecipesScreen(event.getNewScreen())) {
						//If it is mark on our current screen that we are switching to JEI
						screen.switchingToJEI = true;
					}
				}
			});
		}
	}

	private void registerKeybindings(RegisterKeyMappingsEvent event) {
		ClientKeyHelper.registerKeyBindings(event);
	}

	private void registerOverlays(RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.CROSSHAIR, PECore.rl("transmutation_result"), new TransmutationRenderingOverlay());
	}

	private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(PEBlockEntityTypes.DARK_MATTER_PEDESTAL.get(), PedestalRenderer::new);
		event.registerEntityRenderer(PEEntityTypes.HOMING_ARROW.get(), HomingArrowRenderer::new);
	}

	private void addLayers(EntityRenderersEvent.AddLayers event) {
		//TODO 1.21.6+: Port player layer registration to new player renderer API.
	}

	private static boolean isJeiRecipesScreen(net.minecraft.client.gui.screens.Screen screen) {
		return screen != null && screen.getClass().getName().equals("mezz.jei.library.gui.recipes.RecipesGui");
	}
}