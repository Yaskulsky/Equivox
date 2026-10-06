package com.yaskulsky.equivox.gameObjs.container;

import java.util.Objects;
import java.util.function.Predicate;
import com.yaskulsky.equivox.api.ItemInfo;
import com.yaskulsky.equivox.gameObjs.block_entities.CondenserBlockEntity;
import com.yaskulsky.equivox.gameObjs.container.slots.SlotCondenserLock;
import com.yaskulsky.equivox.gameObjs.container.slots.SlotPredicates;
import com.yaskulsky.equivox.gameObjs.container.slots.ValidatedSlot;
import com.yaskulsky.equivox.gameObjs.registration.impl.ContainerTypeRegistryObject;
import com.yaskulsky.equivox.gameObjs.registries.PEContainerTypes;
import com.yaskulsky.equivox.network.packets.to_client.container.UpdateCondenserLockPKT;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CondenserContainer extends EmcChestBlockEntityContainer<CondenserBlockEntity> {

	public static final int MAX_PROGRESS = 102;

	public final BoxedLong displayEmc = new BoxedLong();
	public final BoxedLong requiredEmc = new BoxedLong();
	@Nullable
	private ItemInfo lastLockInfo;

	public CondenserContainer(int windowId, Inventory playerInv, CondenserBlockEntity condenser) {
		this(PEContainerTypes.CONDENSER_CONTAINER, windowId, playerInv, condenser);
	}

	protected CondenserContainer(ContainerTypeRegistryObject<? extends CondenserContainer> type, int windowId, Inventory playerInv, CondenserBlockEntity condenser) {
		super(type, windowId, playerInv, condenser);
		this.longFields.add(displayEmc);
		this.longFields.add(requiredEmc);
		initSlots();
	}

	protected void initSlots() {
		this.addSlot(new SlotCondenserLock(blockEntity::getLockInfo, 0, 12, 6));
		Predicate<ItemStack> validator = s -> SlotPredicates.HAS_EMC.test(s) && !blockEntity.isStackEqualToLock(s);
		ResourceHandler<ItemResource> handler = blockEntity.getInput();
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 13; j++) {
				this.addSlot(new ValidatedSlot(handler,  j + i * 13, 12 + j * 18, 26 + i * 18, validator));
			}
		}
		addPlayerInventory(48, 154);
	}

	@Override
	protected void broadcastPE(boolean all) {
		this.displayEmc.set(blockEntity.displayEmc);
		this.requiredEmc.set(blockEntity.requiredEmc);
		ItemInfo lockInfo = blockEntity.getLockInfo();
		if (all || !Objects.equals(lockInfo, lastLockInfo)) {
			lastLockInfo = lockInfo;
			syncDataChange(new UpdateCondenserLockPKT((short) containerId, lockInfo));
		}
		super.broadcastPE(all);
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return Container.stillValidBlockEntity(blockEntity, player);
	}

	@Override
	public void clicked(int slot, int button, @NotNull ContainerInput flag, @NotNull Player player) {
		if (slot == 0) {
			if (blockEntity.attemptCondenserSet(player)) {
				this.broadcastChanges();
			}
		} else {
			super.clicked(slot, button, flag, player);
		}
	}

	public int getProgressScaled() {
		if (requiredEmc.get() == 0) {
			return 0;
		}
		if (displayEmc.get() >= requiredEmc.get()) {
			return MAX_PROGRESS;
		}
		return (int) (MAX_PROGRESS * ((double) displayEmc.get() / requiredEmc.get()));
	}

	public void updateLockInfo(@Nullable ItemInfo lockInfo) {
		blockEntity.setLockInfoFromPacket(lockInfo);
	}
}