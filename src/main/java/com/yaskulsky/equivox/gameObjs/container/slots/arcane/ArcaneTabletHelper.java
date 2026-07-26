package com.yaskulsky.equivox.gameObjs.container.slots.arcane;

import java.math.BigInteger;
import com.yaskulsky.equivox.api.ItemInfo;
import com.yaskulsky.equivox.api.capabilities.IKnowledgeProvider;
import com.yaskulsky.equivox.api.event.PlayerAttemptLearnEvent;
import com.yaskulsky.equivox.api.proxy.IEMCProxy;
import com.yaskulsky.equivox.utils.ItemCapabilityHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Small helpers for Arcane Tablet crafting / inventory return.
 * Patterns adapted from ProjectExpansion Arcane Tablet (MIT).
 */
public final class ArcaneTabletHelper {

	private ArcaneTabletHelper() {
	}

	public static ItemStack cleanStack(ItemStack stack) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack copy = stack.copyWithCount(1);
		if (copy.isDamageableItem()) {
			copy.setDamageValue(0);
		}
		return IEMCProxy.INSTANCE.getPersistentInfo(ItemInfo.fromStack(copy)).createStack();
	}

	public static boolean areStacksEqual(ItemStack a, ItemStack b) {
		return ItemStack.isSameItemSameComponents(a, b);
	}

	public static boolean tryLearnAndConvertToEmc(Player player, IKnowledgeProvider provider, ItemStack stack) {
		if (!IEMCProxy.INSTANCE.hasValue(stack)) {
			return false;
		}
		ItemInfo raw = ItemInfo.fromStack(stack);
		ItemInfo cleaned = IEMCProxy.INSTANCE.getPersistentInfo(raw);
		if (!provider.hasKnowledge(cleaned) && NeoForge.EVENT_BUS.post(new PlayerAttemptLearnEvent(player, raw, cleaned)).isCanceled()) {
			return false;
		}
		provider.addKnowledge(cleaned);
		if (player instanceof ServerPlayer serverPlayer) {
			provider.syncKnowledgeChange(serverPlayer, cleaned, true);
		}
		long value = IEMCProxy.INSTANCE.getValue(cleaned);
		provider.setEmc(provider.getEmc().add(BigInteger.valueOf(value).multiply(BigInteger.valueOf(stack.getCount()))));
		if (player instanceof ServerPlayer serverPlayer) {
			provider.syncEmc(serverPlayer);
		}
		return true;
	}

	public static ItemStack returnToInventoryOrEmc(Inventory playerInv, Player player, IKnowledgeProvider provider, ItemStack stack, boolean force) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if (IEMCProxy.INSTANCE.hasValue(stack) && areStacksEqual(stack, cleanStack(stack))) {
			if (tryLearnAndConvertToEmc(player, provider, stack)) {
				return ItemStack.EMPTY;
			}
		}
		return returnToInventory(playerInv, player, stack, force);
	}

	public static ItemStack returnToInventory(Inventory playerInv, Player player, ItemStack stack, boolean force) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		IItemHandler inv = ItemCapabilityHelper.getPlayerInventory(player);
		if (inv != null) {
			ItemStack leftover = ItemHandlerHelper.insertItemStacked(inv, stack.copy(), false);
			if (leftover.isEmpty()) {
				return ItemStack.EMPTY;
			}
			if (force) {
				player.drop(leftover, false);
				return ItemStack.EMPTY;
			}
			return leftover;
		}
		if (force) {
			playerInv.placeItemBackInInventory(stack);
			return ItemStack.EMPTY;
		}
		return stack;
	}
}
