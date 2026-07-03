package moze_intel.projecte.utils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

public final class ItemCapabilityHelper {

	private ItemCapabilityHelper() {
	}

	@Nullable
	public static IItemHandler getPlayerInventory(Player player) {
		return of(player.getCapability(Capabilities.Item.ENTITY));
	}

	@Nullable
	public static IItemHandler of(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		return of(ItemAccess.forStack(stack).getCapability(Capabilities.Item.ITEM));
	}

	@Nullable
	public static IItemHandler of(@Nullable ResourceHandler<ItemResource> handler) {
		return handler == null ? null : IItemHandler.of(handler);
	}
}
