package com.yaskulsky.equivox.integration.refinedstorage;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.yaskulsky.equivox.gameObjs.block_entities.TransmutationProviderBlockEntity;
import com.yaskulsky.equivox.utils.KnowledgeExportHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes {@link KnowledgeExportHandler} to a Refined Storage network (Grid / autocraft).
 */
final class KnowledgeRsExternalStorageProvider implements ExternalStorageProvider {

	private final TransmutationProviderBlockEntity provider;

	KnowledgeRsExternalStorageProvider(TransmutationProviderBlockEntity provider) {
		this.provider = provider;
	}

	@Override
	public Iterator<ResourceAmount> iterator() {
		if (!provider.isExportActive()) {
			return EmptyRsExternalStorageProvider.INSTANCE.iterator();
		}
		KnowledgeExportHandler handler = provider.getKnowledgeExportHandler();
		List<ResourceAmount> amounts = new ArrayList<>();
		for (int i = 0; i < handler.size(); i++) {
			long amount = handler.getAmountAsLong(i);
			if (amount <= 0) {
				continue;
			}
			ItemStack stack = handler.getResource(i).toStack(1);
			RefinedStorageApi.INSTANCE.getItemResourceFactory().create(stack)
					.map(existing -> new ResourceAmount(existing.resource(), amount))
					.ifPresent(amounts::add);
		}
		return amounts.iterator();
	}

	@Override
	public long extract(ResourceKey resource, long amount, Action action, Actor actor) {
		if (amount <= 0 || !provider.isExportActive()) {
			return 0;
		}
		net.neoforged.neoforge.transfer.item.ItemResource neoResource = toNeoItemResource(resource);
		if (neoResource == null) {
			return 0;
		}
		KnowledgeExportHandler handler = provider.getKnowledgeExportHandler();
		int slot = findSlot(handler, neoResource);
		if (slot < 0) {
			return 0;
		}
		int toExtract = (int) Math.min(amount, Integer.MAX_VALUE);
		if (action == Action.SIMULATE) {
			return Math.min(toExtract, (int) Math.min(handler.getAmountAsLong(slot), Integer.MAX_VALUE));
		}
		try (Transaction tx = Transaction.openRoot()) {
			int extracted = handler.extract(slot, neoResource, toExtract, tx);
			tx.commit();
			return extracted;
		}
	}

	@Override
	public long insert(ResourceKey resource, long amount, Action action, Actor actor) {
		return 0;
	}

	@Nullable
	private static net.neoforged.neoforge.transfer.item.ItemResource toNeoItemResource(ResourceKey resource) {
		if (!(resource instanceof ItemResource rsItem)) {
			return null;
		}
		return net.neoforged.neoforge.transfer.item.ItemResource.of(rsItem.toItemStack());
	}

	private static int findSlot(KnowledgeExportHandler handler, net.neoforged.neoforge.transfer.item.ItemResource resource) {
		for (int i = 0; i < handler.size(); i++) {
			if (handler.getAmountAsLong(i) > 0 && handler.getResource(i).equals(resource)) {
				return i;
			}
		}
		return -1;
	}
}
