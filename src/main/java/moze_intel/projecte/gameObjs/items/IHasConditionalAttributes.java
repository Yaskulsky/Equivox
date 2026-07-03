package moze_intel.projecte.gameObjs.items;

import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

public interface IHasConditionalAttributes {

	/**
	 * Called on any items that implement this interface when the event is fired for an item of that type.
	 */
	void adjustAttributes(ItemAttributeModifierEvent event);
}