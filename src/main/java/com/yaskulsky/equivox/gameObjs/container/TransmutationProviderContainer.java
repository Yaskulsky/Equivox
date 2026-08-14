package com.yaskulsky.equivox.gameObjs.container;

import com.yaskulsky.equivox.gameObjs.block_entities.TransmutationProviderBlockEntity;
import com.yaskulsky.equivox.gameObjs.registries.PEContainerTypes;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TransmutationProviderContainer extends PEContainer {

	public final TransmutationProviderBlockEntity provider;
	private final DataSlot linked = DataSlot.standalone();
	private final DataSlot ownerOnline = DataSlot.standalone();
	private final DataSlot tomeBlocked = DataSlot.standalone();
	private final DataSlot exposedCount = DataSlot.standalone();

	public TransmutationProviderContainer(int windowId, Inventory playerInv, TransmutationProviderBlockEntity provider) {
		super(PEContainerTypes.TRANSMUTATION_PROVIDER_CONTAINER, windowId, playerInv);
		this.provider = provider;
		addDataSlot(linked);
		addDataSlot(ownerOnline);
		addDataSlot(tomeBlocked);
		addDataSlot(exposedCount);
		addPlayerInventory(8, 84);
	}

	@Override
	protected void broadcastPE(boolean all) {
		linked.set(provider.hasTableAbove() ? 1 : 0);
		ownerOnline.set(provider.isOwnerOnline() ? 1 : 0);
		tomeBlocked.set(provider.isTomeBlocked() ? 1 : 0);
		exposedCount.set(provider.getExposedCount());
		super.broadcastPE(all);
	}

	public boolean isLinked() {
		return linked.get() != 0;
	}

	public boolean isOwnerOnline() {
		return ownerOnline.get() != 0;
	}

	/** AE2-style export ready: table linked, owner online, not tome-blocked. */
	public boolean isOnline() {
		return isLinked() && isOwnerOnline() && !isTomeBlocked();
	}

	public boolean isTomeBlocked() {
		return tomeBlocked.get() != 0;
	}

	public int getExposedCount() {
		return exposedCount.get();
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return Container.stillValidBlockEntity(provider, player);
	}

	@NotNull
	@Override
	public ItemStack quickMoveStack(@NotNull Player player, int index) {
		return ItemStack.EMPTY;
	}
}
