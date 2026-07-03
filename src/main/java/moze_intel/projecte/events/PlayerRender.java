package moze_intel.projecte.events;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;

@EventBusSubscriber(modid = PECore.MODID, value = Dist.CLIENT)
public class PlayerRender {

	@SubscribeEvent
	public static void onFOVUpdateEvent(ComputeFovModifierEvent evt) {
		if (!evt.getPlayer().getItemBySlot(EquipmentSlot.FEET).isEmpty() && evt.getPlayer().getItemBySlot(EquipmentSlot.FEET).is(PEItems.GEM_BOOTS)) {
			evt.setNewFovModifier(evt.getNewFovModifier() - 0.5F * Minecraft.getInstance().options.fovEffectScale().get().floatValue());
		}
	}
}