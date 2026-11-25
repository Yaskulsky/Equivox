package com.yaskulsky.equivox.gameObjs.container.slots.transmutation;

import java.math.BigInteger;
import com.yaskulsky.equivox.api.proxy.IEMCProxy;
import com.yaskulsky.equivox.gameObjs.container.inventory.TransmutationInventory;
import com.yaskulsky.equivox.gameObjs.registries.PEItems;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotConsume extends Slot {

	private final TransmutationInventory inv;

	public SlotConsume(TransmutationInventory inv, int index, int x, int y) {
		super(new SimpleContainer(index + 1), index, x, y);
		this.inv = inv;
	}

	@Override
	public void set(@NotNull ItemStack stack) {
		if (inv.isServer() && !stack.isEmpty()) {
			inv.handleKnowledge(stack);
			inv.addEmc(BigInteger.valueOf(IEMCProxy.INSTANCE.getSellValue(stack)).multiply(BigInteger.valueOf(stack.getCount())));
			this.setChanged();
		}
	}

	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
		return IEMCProxy.INSTANCE.hasValue(stack) || stack.is(PEItems.TOME_OF_KNOWLEDGE);
	}
}
