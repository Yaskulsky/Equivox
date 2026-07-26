package com.yaskulsky.equivox.gameObjs.block_entities;

import java.util.UUID;
import com.yaskulsky.equivox.api.ItemInfo;
import com.yaskulsky.equivox.api.capabilities.IKnowledgeProvider;
import com.yaskulsky.equivox.api.capabilities.PECapabilities;
import com.yaskulsky.equivox.api.capabilities.block_entity.IEmcStorage.EmcAction;
import com.yaskulsky.equivox.api.event.PlayerAttemptLearnEvent;
import com.yaskulsky.equivox.api.proxy.IEMCProxy;
import com.yaskulsky.equivox.gameObjs.EnumEntropySinkTier;
import com.yaskulsky.equivox.gameObjs.container.slots.SlotPredicates;
import com.yaskulsky.equivox.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import com.yaskulsky.equivox.gameObjs.registries.PEBlockEntityTypes;
import com.yaskulsky.equivox.utils.LegacyItemHandlerResourceHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Burns hopper/pipe-fed items into EMC with soft diminishing returns.
 *
 * <p>Efficiency formula (sliding ~1s window of granted EMC):
 * <pre>
 *   rateEmcPerSec = emcGrantedInLast20Ticks
 *   if rate &lt;= 1000: efficiency = 1.0
 *   else: efficiency = max(0.25, 1.0 - 0.75 * (rate - 1000) / 9000)
 *   // → 100% below 1k EMC/s, linearly down to 25% at 10k EMC/s
 *   granted = floor(itemSellEmc * countBurned * efficiency)
 * </pre>
 */
public class EntropySinkBlockEntity extends EmcBlockEntity {

	public static final int INVENTORY_SIZE = 9;

	/** Full efficiency while sustained burn stays at or below this EMC/s. */
	public static final long FULL_EFFICIENCY_THRESHOLD_EMC_S = 1_000;
	/** Rate (EMC/s) at which efficiency reaches {@link #MIN_EFFICIENCY}. */
	public static final long MIN_EFFICIENCY_RATE_EMC_S = 10_000;
	public static final double MIN_EFFICIENCY = 0.25;
	private static final int RATE_WINDOW_TICKS = 20;

	public static final ICapabilityProvider<EntropySinkBlockEntity, @Nullable Direction, ResourceHandler<ItemResource>> INVENTORY_PROVIDER =
			(sink, side) -> LegacyItemHandlerResourceHandler.of(sink.automationInventory);

	private final EnumEntropySinkTier tier;
	private final ItemStackHandler inventory = new StackHandler(INVENTORY_SIZE) {
		@NotNull
		@Override
		public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
			return canBurn(stack) ? super.insertItem(slot, stack, simulate) : stack;
		}
	};
	private final IItemHandler automationInventory = new WrappedItemHandler(inventory, WrappedItemHandler.WriteMode.IN);

	@Nullable
	private UUID owner;
	private int tickCounter;
	/** Ring buffer of EMC granted per tick over the last {@link #RATE_WINDOW_TICKS} ticks. */
	private final long[] recentGranted = new long[RATE_WINDOW_TICKS];
	private int recentIndex;
	private long recentSum;

	public EntropySinkBlockEntity(BlockPos pos, BlockState state) {
		this(PEBlockEntityTypes.ENTROPY_SINK, pos, state, EnumEntropySinkTier.BASIC);
	}

	protected EntropySinkBlockEntity(BlockEntityTypeRegistryObject<? extends EntropySinkBlockEntity> type, BlockPos pos,
			BlockState state, EnumEntropySinkTier tier) {
		super(type, pos, state, tier.getStorage());
		this.tier = tier;
	}

	public EnumEntropySinkTier getTier() {
		return tier;
	}

	public void setOwner(@Nullable UUID owner) {
		this.owner = owner;
		setChanged();
	}

	@Nullable
	public UUID getOwner() {
		return owner;
	}

	@Override
	protected boolean canAcceptEmc() {
		return false;
	}

	@Override
	protected boolean canProvideEmc() {
		return true;
	}

	@Override
	protected boolean emcAffectsComparators() {
		return true;
	}

	public static void tickServer(Level level, BlockPos pos, BlockState state, EntropySinkBlockEntity sink) {
		sink.advanceRateWindow();
		sink.tickCounter++;
		if (sink.tickCounter >= sink.tier.getBurnIntervalTicks()) {
			sink.tickCounter = 0;
			sink.burnItems(level);
		}
		long stored = sink.getStoredEmc();
		if (stored > 0) {
			sink.sendToAllAcceptors(level, pos, stored);
		}
		sink.updateComparators(level, pos);
	}

	private void advanceRateWindow() {
		recentIndex = (recentIndex + 1) % RATE_WINDOW_TICKS;
		recentSum -= recentGranted[recentIndex];
		recentGranted[recentIndex] = 0;
	}

	private void recordGranted(long amount) {
		recentGranted[recentIndex] += amount;
		recentSum += amount;
	}

	/**
	 * Soft diminishing returns — see class javadoc for the formula.
	 */
	private double currentEfficiency() {
		// recentSum is EMC granted in the last ~20 ticks ≈ EMC/s
		long rate = recentSum;
		if (rate <= FULL_EFFICIENCY_THRESHOLD_EMC_S) {
			return 1.0;
		}
		double span = MIN_EFFICIENCY_RATE_EMC_S - FULL_EFFICIENCY_THRESHOLD_EMC_S;
		double t = (rate - FULL_EFFICIENCY_THRESHOLD_EMC_S) / span;
		return Math.max(MIN_EFFICIENCY, 1.0 - (1.0 - MIN_EFFICIENCY) * Math.min(1.0, t));
	}

	private void burnItems(Level level) {
		int remaining = tier.getMaxItemsPerBurn();
		double efficiency = currentEfficiency();
		ServerPlayer ownerPlayer = null;
		if (owner != null && level.getServer() != null) {
			ownerPlayer = level.getServer().getPlayerList().getPlayer(owner);
		}

		for (int slot = 0; slot < inventory.getSlots() && remaining > 0; slot++) {
			ItemStack stack = inventory.getStackInSlot(slot);
			if (!canBurn(stack)) {
				continue;
			}
			long sell = IEMCProxy.INSTANCE.getSellValue(stack);
			if (sell <= 0) {
				continue;
			}
			int toBurn = Math.min(remaining, stack.getCount());
			long raw;
			try {
				raw = Math.multiplyExact(sell, toBurn);
			} catch (ArithmeticException overflow) {
				raw = Long.MAX_VALUE;
			}
			long granted = Math.max(0, (long) Math.floor(raw * efficiency));
			if (granted > 0) {
				long inserted = forceInsertEmc(granted, EmcAction.EXECUTE);
				recordGranted(inserted);
			}
			if (ownerPlayer != null) {
				tryLearn(ownerPlayer, stack);
			}
			inventory.extractItem(slot, toBurn, false);
			remaining -= toBurn;
		}
	}

	private static void tryLearn(ServerPlayer player, ItemStack stack) {
		IKnowledgeProvider knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		if (knowledge == null) {
			return;
		}
		ItemInfo info = ItemInfo.fromStack(stack);
		ItemInfo cleaned = IEMCProxy.INSTANCE.getPersistentInfo(info);
		if (!knowledge.hasKnowledge(cleaned)
				&& !NeoForge.EVENT_BUS.post(new PlayerAttemptLearnEvent(player, info, cleaned)).isCanceled()) {
			if (knowledge.addKnowledge(cleaned)) {
				knowledge.syncKnowledgeChange(player, cleaned, true);
			}
		}
	}

	public static boolean canBurn(ItemStack stack) {
		if (stack.isEmpty() || !SlotPredicates.HAS_EMC.test(stack)) {
			return false;
		}
		if (stack.get(DataComponents.CUSTOM_NAME) != null) {
			return false;
		}
		if (stack.get(DataComponents.CONTAINER) != null || stack.get(DataComponents.CONTAINER_LOOT) != null) {
			return false;
		}
		if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
			return false;
		}
		return IEMCProxy.INSTANCE.getSellValue(stack) > 0;
	}

	@Override
	public void loadAdditional(@NotNull ValueInput input) {
		super.loadAdditional(input);
		inventory.deserialize(input.childOrEmpty("inventory"));
		owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
		tickCounter = input.getIntOr("tick_counter", 0);
	}

	@Override
	protected void saveAdditional(@NotNull ValueOutput output) {
		super.saveAdditional(output);
		inventory.serialize(output.child("inventory"));
		if (owner != null) {
			output.store("owner", UUIDUtil.CODEC, owner);
		}
		output.putInt("tick_counter", tickCounter);
	}
}
