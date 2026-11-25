package com.yaskulsky.equivox.gameObjs.block_entities;

import java.util.UUID;
import com.yaskulsky.equivox.api.capabilities.block_entity.IEmcStorage.EmcAction;
import com.yaskulsky.equivox.gameObjs.EnumEntropySinkTier;
import com.yaskulsky.equivox.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import com.yaskulsky.equivox.gameObjs.registries.PEBlockEntityTypes;
import com.yaskulsky.equivox.utils.EmcDepositHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Converts Forge Energy (FE) into EMC and deposits it straight into the owner's personal EMC pool.
 *
 * <ul>
 *   <li>Accepts FE from any side, up to {@link EnumEntropySinkTier#getMaxFePerTick()} per tick.</li>
 *   <li>Each tick converts up to that much buffered FE at {@link EnumEntropySinkTier#getFePerEmc()} FE per EMC;
 *       leftover FE stays buffered for the next tick (no loss).</li>
 *   <li>Converted EMC is flushed to the owner once per second while they are online. While offline it collects
 *       in the block's EMC buffer; when that is full the sink stops consuming FE.</li>
 *   <li>No pipe/relay output — EMC only goes to the owner.</li>
 * </ul>
 */
public class EntropySinkBlockEntity extends EmcBlockEntity {

	private static final int FLUSH_INTERVAL_TICKS = 20;

	public static final ICapabilityProvider<EntropySinkBlockEntity, @Nullable Direction, EnergyHandler> ENERGY_PROVIDER =
			(sink, side) -> sink.energy;

	private final EnumEntropySinkTier tier;
	private final SimpleEnergyHandler energy;

	@Nullable
	private UUID owner;
	private int flushCounter;

	public EntropySinkBlockEntity(BlockPos pos, BlockState state) {
		this(PEBlockEntityTypes.ENTROPY_SINK, pos, state, EnumEntropySinkTier.BASIC);
	}

	protected EntropySinkBlockEntity(BlockEntityTypeRegistryObject<? extends EntropySinkBlockEntity> type, BlockPos pos,
			BlockState state, EnumEntropySinkTier tier) {
		super(type, pos, state, tier.getStorage());
		this.tier = tier;
		this.energy = new SimpleEnergyHandler(tier.getFeCapacity(), tier.getMaxFePerTick(), 0) {
			@Override
			protected void onEnergyChanged(int previousAmount) {
				EntropySinkBlockEntity.this.setChanged();
			}
		};
	}

	public EnumEntropySinkTier getTier() {
		return tier;
	}

	public EnergyHandler getEnergy() {
		return energy;
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
		return false;
	}

	@Override
	protected boolean emcAffectsComparators() {
		return true;
	}

	public static void tickServer(Level level, BlockPos pos, BlockState state, EntropySinkBlockEntity sink) {
		sink.convertEnergy();
		if (++sink.flushCounter >= FLUSH_INTERVAL_TICKS) {
			sink.flushCounter = 0;
			sink.flushToOwner(level);
		}
		sink.updateComparators(level, pos);
	}

	private void convertEnergy() {
		if (owner == null) {
			return;
		}
		int fePerEmc = tier.getFePerEmc();
		long usable = Math.min(energy.getAmountAsLong(), tier.getMaxFePerTick());
		long emc = usable / fePerEmc;
		long room = getMaximumEmc() - getStoredEmc();
		emc = Math.min(emc, room);
		if (emc <= 0) {
			return;
		}
		long inserted = forceInsertEmc(emc, EmcAction.EXECUTE);
		if (inserted > 0) {
			energy.set((int) (energy.getAmountAsLong() - inserted * fePerEmc));
		}
	}

	private void flushToOwner(Level level) {
		if (owner == null || getStoredEmc() <= 0) {
			return;
		}
		ServerPlayer player = EmcDepositHelper.findOnlineOwner(level.getServer(), owner);
		if (player == null) {
			return;
		}
		long extracted = forceExtractEmc(getStoredEmc(), EmcAction.EXECUTE);
		long deposited = EmcDepositHelper.depositToPlayer(player, extracted);
		if (deposited < extracted) {
			forceInsertEmc(extracted - deposited, EmcAction.EXECUTE);
		}
	}

	@Override
	public void loadAdditional(@NotNull ValueInput input) {
		super.loadAdditional(input);
		energy.deserialize(input.childOrEmpty("energy"));
		owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
	}

	@Override
	protected void saveAdditional(@NotNull ValueOutput output) {
		super.saveAdditional(output);
		energy.serialize(output.child("energy"));
		if (owner != null) {
			output.store("owner", UUIDUtil.CODEC, owner);
		}
	}
}
