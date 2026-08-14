package com.yaskulsky.equivox.utils;

import java.math.BigInteger;
import java.util.List;
import com.yaskulsky.equivox.api.ItemInfo;
import com.yaskulsky.equivox.api.capabilities.IKnowledgeProvider;
import com.yaskulsky.equivox.api.capabilities.PECapabilities;
import com.yaskulsky.equivox.api.proxy.IEMCProxy;
import com.yaskulsky.equivox.gameObjs.block_entities.TransmutationProviderBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Virtual item capability: exposes the owner's learned EMC items for AE2/RS storage buses.
 * Extract deducts personal EMC and is transaction-safe so AE2 extractable-only scans do not drain EMC.
 */
public final class KnowledgeExportHandler extends SnapshotJournal<BigInteger> implements ResourceHandler<ItemResource> {

	/** Match AE2 ExternalStorageFacade reporting cap (~2^42). */
	private static final long MAX_REPORTED = 1L << 42;

	private final TransmutationProviderBlockEntity provider;

	public KnowledgeExportHandler(TransmutationProviderBlockEntity provider) {
		this.provider = provider;
	}

	@Override
	public int size() {
		return provider.getExposedKnowledge().size();
	}

	@Override
	public ItemResource getResource(int index) {
		ItemInfo info = infoAt(index);
		return info == null ? ItemResource.of(ItemStack.EMPTY) : ItemResource.of(info.createStack());
	}

	@Override
	public long getAmountAsLong(int index) {
		ItemInfo info = infoAt(index);
		ServerPlayer owner = provider.getOnlineOwner();
		if (info == null || owner == null || !provider.isExportActive()) {
			return 0;
		}
		return affordable(owner, info, MAX_REPORTED);
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		return MAX_REPORTED;
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return false;
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		ItemInfo info = infoAt(index);
		ServerPlayer owner = provider.getOnlineOwner();
		if (info == null || owner == null || !provider.isExportActive()) {
			return 0;
		}
		ItemStack prototype = info.createStack();
		ItemResource expected = ItemResource.of(prototype);
		if (!expected.equals(resource)) {
			return 0;
		}
		IKnowledgeProvider knowledge = owner.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		if (knowledge == null || !knowledge.hasKnowledge(info)) {
			return 0;
		}
		long unitEmc = IEMCProxy.INSTANCE.getValue(info);
		if (unitEmc <= 0) {
			return 0;
		}
		BigInteger unit = BigInteger.valueOf(unitEmc);
		int can = knowledge.getEmc().divide(unit).min(BigInteger.valueOf(amount)).intValue();
		if (can <= 0) {
			return 0;
		}
		updateSnapshots(transaction);
		knowledge.setEmc(knowledge.getEmc().subtract(unit.multiply(BigInteger.valueOf(can))));
		return can;
	}

	@Override
	protected BigInteger createSnapshot() {
		ServerPlayer owner = provider.getOnlineOwner();
		if (owner == null) {
			return BigInteger.ZERO;
		}
		IKnowledgeProvider knowledge = owner.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		return knowledge == null ? BigInteger.ZERO : knowledge.getEmc();
	}

	@Override
	protected void revertToSnapshot(BigInteger snapshot) {
		ServerPlayer owner = provider.getOnlineOwner();
		if (owner == null) {
			return;
		}
		IKnowledgeProvider knowledge = owner.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		if (knowledge != null) {
			knowledge.setEmc(snapshot);
		}
	}

	@Override
	protected void onRootCommit(BigInteger originalState) {
		ServerPlayer owner = provider.getOnlineOwner();
		if (owner == null) {
			return;
		}
		IKnowledgeProvider knowledge = owner.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		if (knowledge != null) {
			knowledge.syncEmc(owner);
			PlayerHelper.updateScore(owner, PlayerHelper.SCOREBOARD_EMC, knowledge.getEmc());
		}
	}

	@Nullable
	private ItemInfo infoAt(int index) {
		List<ItemInfo> exposed = provider.getExposedKnowledge();
		if (index < 0 || index >= exposed.size()) {
			return null;
		}
		return exposed.get(index);
	}

	private static long affordable(@NotNull ServerPlayer player, @NotNull ItemInfo info, long max) {
		IKnowledgeProvider knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		if (knowledge == null) {
			return 0;
		}
		long unitEmc = IEMCProxy.INSTANCE.getValue(info);
		if (unitEmc <= 0) {
			return 0;
		}
		BigInteger unit = BigInteger.valueOf(unitEmc);
		return knowledge.getEmc().divide(unit).min(BigInteger.valueOf(max)).longValue();
	}
}
