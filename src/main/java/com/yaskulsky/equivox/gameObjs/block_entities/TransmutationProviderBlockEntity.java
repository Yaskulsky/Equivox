package com.yaskulsky.equivox.gameObjs.block_entities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import com.yaskulsky.equivox.api.ItemInfo;
import com.yaskulsky.equivox.api.capabilities.IKnowledgeProvider;
import com.yaskulsky.equivox.api.capabilities.PECapabilities;
import com.yaskulsky.equivox.api.proxy.IEMCProxy;
import com.yaskulsky.equivox.gameObjs.blocks.TransmutationProvider;
import com.yaskulsky.equivox.gameObjs.container.TransmutationProviderContainer;
import com.yaskulsky.equivox.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import com.yaskulsky.equivox.gameObjs.registries.PEBlockEntityTypes;
import com.yaskulsky.equivox.gameObjs.registries.PEBlocks;
import com.yaskulsky.equivox.utils.EmcDepositHelper;
import com.yaskulsky.equivox.utils.KnowledgeExportHandler;
import com.yaskulsky.equivox.utils.text.TextComponentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Owner-bound bridge under a Transmutation Table: AE2/RS buses see learned items paid from personal EMC.
 */
public class TransmutationProviderBlockEntity extends EmcBlockEntity implements MenuProvider {

	public static final ICapabilityProvider<TransmutationProviderBlockEntity, @Nullable Direction, ResourceHandler<ItemResource>> INVENTORY_PROVIDER =
			(provider, side) -> provider.isExportActive() ? provider.exportHandler : null;

	private final KnowledgeExportHandler exportHandler = new KnowledgeExportHandler(this);
	private final List<ItemInfo> exposedKnowledge = new ArrayList<>();
	@Nullable
	private UUID owner;
	private boolean tableLinked;
	private boolean ownerOnline;
	private boolean tomeBlocked;

	public TransmutationProviderBlockEntity(BlockPos pos, BlockState state) {
		this(PEBlockEntityTypes.TRANSMUTATION_PROVIDER, pos, state);
	}

	protected TransmutationProviderBlockEntity(BlockEntityTypeRegistryObject<? extends TransmutationProviderBlockEntity> type,
			BlockPos pos, BlockState state) {
		super(type, pos, state, 1);
	}

	public void setOwner(@Nullable UUID owner) {
		this.owner = owner;
		setChanged();
	}

	@Nullable
	public UUID getOwner() {
		return owner;
	}

	public boolean hasTableAbove() {
		return tableLinked;
	}

	public boolean isOwnerOnline() {
		return ownerOnline;
	}

	public boolean isTomeBlocked() {
		return tomeBlocked;
	}

	public boolean isExportActive() {
		return tableLinked && ownerOnline && !tomeBlocked;
	}

	@NotNull
	public List<ItemInfo> getExposedKnowledge() {
		return exposedKnowledge;
	}

	public int getExposedCount() {
		return exposedKnowledge.size();
	}

	@Nullable
	public ServerPlayer getOnlineOwner() {
		Level level = getLevel();
		if (level == null || level.getServer() == null) {
			return null;
		}
		return EmcDepositHelper.findOnlineOwner(level.getServer(), owner);
	}

	@Override
	protected boolean canAcceptEmc() {
		return false;
	}

	@Override
	protected boolean canProvideEmc() {
		return false;
	}

	public void onAboveChanged() {
		refreshLinkState();
	}

	public static void tickServer(Level level, BlockPos pos, BlockState state, TransmutationProviderBlockEntity be) {
		be.tickExport(level);
	}

	private void tickExport(Level level) {
		refreshLinkState();
		if (!isExportActive()) {
			if (!exposedKnowledge.isEmpty()) {
				exposedKnowledge.clear();
				invalidateExport(level);
			}
			return;
		}
		ServerPlayer player = getOnlineOwner();
		if (player == null) {
			exposedKnowledge.clear();
			return;
		}
		IKnowledgeProvider knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		if (knowledge == null || knowledge.hasFullKnowledge()) {
			boolean wasEmpty = exposedKnowledge.isEmpty();
			exposedKnowledge.clear();
			tomeBlocked = knowledge != null && knowledge.hasFullKnowledge();
			if (!wasEmpty) {
				invalidateExport(level);
			}
			return;
		}
		tomeBlocked = false;
		List<ItemInfo> next = new ArrayList<>();
		for (ItemInfo info : knowledge.getKnowledge()) {
			if (IEMCProxy.INSTANCE.getValue(info) > 0) {
				next.add(info);
			}
		}
		next.sort(Comparator.comparing(info -> BuiltInRegistries.ITEM.getKey(info.getItem().value()).toString()));
		if (!next.equals(exposedKnowledge)) {
			exposedKnowledge.clear();
			exposedKnowledge.addAll(next);
			invalidateExport(level);
		}
	}

	private void refreshLinkState() {
		Level level = getLevel();
		boolean linked = level != null && level.getBlockState(worldPosition.above()).is(PEBlocks.TRANSMUTATION_TABLE.getBlock());
		ServerPlayer player = getOnlineOwner();
		boolean online = player != null;
		boolean tome = false;
		if (player != null) {
			IKnowledgeProvider knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
			tome = knowledge != null && knowledge.hasFullKnowledge();
		}
		boolean changed = linked != tableLinked || online != ownerOnline || tome != tomeBlocked;
		tableLinked = linked;
		ownerOnline = online;
		tomeBlocked = tome;
		if (changed && level != null) {
			invalidateExport(level);
		}
		syncOnlineState(level);
	}

	private void syncOnlineState(@Nullable Level level) {
		if (level == null || level.isClientSide()) {
			return;
		}
		BlockState state = getBlockState();
		boolean online = isExportActive();
		if (state.hasProperty(TransmutationProvider.ONLINE) && state.getValue(TransmutationProvider.ONLINE) != online) {
			level.setBlock(worldPosition, state.setValue(TransmutationProvider.ONLINE, online), Block.UPDATE_CLIENTS);
		}
	}

	private void invalidateExport(Level level) {
		level.invalidateCapabilities(worldPosition);
	}

	@Override
	public void loadAdditional(@NotNull ValueInput input) {
		super.loadAdditional(input);
		owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
	}

	@Override
	protected void saveAdditional(@NotNull ValueOutput outputTag) {
		super.saveAdditional(outputTag);
		if (owner != null) {
			outputTag.store("owner", UUIDUtil.CODEC, owner);
		}
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInv, @NotNull Player player) {
		return new TransmutationProviderContainer(windowId, playerInv, this);
	}

	@NotNull
	@Override
	public Component getDisplayName() {
		return TextComponentUtil.build(PEBlocks.TRANSMUTATION_PROVIDER);
	}
}
