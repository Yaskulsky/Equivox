package com.yaskulsky.equivox.utils;

import com.yaskulsky.equivox.api.inventory.PEItemStacksHandler;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

/**
 * Combined item handler with direct slot mutation for {@link net.neoforged.neoforge.transfer.item.ResourceHandlerSlot}.
 */
public class PECombinedItemStacks extends CombinedResourceHandler<ItemResource> implements IndexModifier<ItemResource> {

	@SafeVarargs
	public PECombinedItemStacks(ResourceHandler<ItemResource>... handlers) {
		super(handlers);
	}

	@Override
	public void set(int index, ItemResource resource, int amount) {
		int handlerIndex = getHandlerIndex(index);
		ResourceHandler<ItemResource> handler = getHandlerFromIndex(handlerIndex);
		int slot = getSlotFromIndex(index, handlerIndex);
		if (handler instanceof ItemStacksResourceHandler itemStacks) {
			itemStacks.set(slot, resource, amount);
		} else if (handler instanceof PEItemStacksHandler peHandler) {
			peHandler.set(slot, resource, amount);
		} else {
			throw new IllegalStateException("Cannot set slot on non-stack handler: " + handler);
		}
	}

	public PEItemStacksHandler getStackHandler(int handlerIndex) {
		return (PEItemStacksHandler) getHandlerFromIndex(handlerIndex);
	}
}
