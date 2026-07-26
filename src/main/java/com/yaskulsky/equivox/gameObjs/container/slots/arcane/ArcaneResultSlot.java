package com.yaskulsky.equivox.gameObjs.container.slots.arcane;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.yaskulsky.equivox.api.proxy.IEMCProxy;
import com.yaskulsky.equivox.gameObjs.container.ArcaneTabletContainer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Crafting result slot that learns the crafted item and refills the grid from inventory / EMC.
 */
public class ArcaneResultSlot extends ResultSlot {

	private final CraftingContainer craftSlots;
	private final Player player;
	private final ArcaneTabletContainer tablet;

	public ArcaneResultSlot(Player player, CraftingContainer craftSlots, ResultContainer container, ArcaneTabletContainer tablet,
			int slot, int x, int y) {
		super(player, craftSlots, container, slot, x, y);
		this.craftSlots = craftSlots;
		this.player = player;
		this.tablet = tablet;
	}

	@Override
	public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
		learnResult(stack);
		List<ItemStack> previousItems = craftSlots.getItems().stream().map(ItemStack::copy).collect(Collectors.toCollection(ArrayList::new));
		super.onTake(player, stack);
		refillAfterTake(previousItems);
	}

	@Override
	protected void onQuickCraft(@NotNull ItemStack stack, int amount) {
		learnResult(stack);
		super.onQuickCraft(stack, amount);
	}

	private void learnResult(ItemStack stack) {
		if (player instanceof ServerPlayer && IEMCProxy.INSTANCE.hasValue(stack)) {
			tablet.transmutationInventory.handleKnowledge(stack);
		}
	}

	private void refillAfterTake(List<ItemStack> previousItems) {
		if (player.level().isClientSide() || tablet.skipRefill) {
			return;
		}
		List<ItemStack> currentItems = craftSlots.getItems().stream().map(ItemStack::copy).collect(Collectors.toCollection(ArrayList::new));
		for (int i = 0; i < currentItems.size(); i++) {
			ItemStack stack = currentItems.get(i);
			if (stack.isEmpty() || ItemStack.isSameItemSameComponents(previousItems.get(i), stack)) {
				continue;
			}
			if (IEMCProxy.INSTANCE.hasValue(stack)) {
				tablet.transmutationInventory.handleKnowledge(stack);
				long value = IEMCProxy.INSTANCE.getValue(stack);
				tablet.transmutationInventory.addEmc(BigInteger.valueOf(value).multiply(BigInteger.valueOf(stack.getCount())));
				craftSlots.setItem(i, ItemStack.EMPTY);
				continue;
			}
			ItemStack leftover = ArcaneTabletHelper.returnToInventory(player.getInventory(), player, stack, false);
			craftSlots.setItem(i, leftover);
		}
		List<List<ItemStack>> recipe = previousItems.stream().map(List::of).map(ArrayList::new).collect(Collectors.toList());
		tablet.fillCraftingSlots(recipe, false);
	}

	@Override
	public void set(@NotNull ItemStack stack) {
		super.set(stack);
		tablet.isCrafting = !stack.isEmpty();
	}
}
