package com.yaskulsky.equivox.gameObjs;

import com.yaskulsky.equivox.config.EquivoxConfig;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Balance knobs for Entropy Sink tiers (FE → EMC converter).
 * The FE-per-EMC rate is server-configurable ({@code server.toml → entropySink}); the values here are defaults.
 */
public enum EnumEntropySinkTier implements StringRepresentable {
	BASIC("entropy_sink", 2_000, 2_000, 50_000),
	DARK("entropy_sink_dark", 1_500, 16_000, 200_000),
	RED("entropy_sink_red", 1_000, 128_000, 1_000_000);

	private final String name;
	private final int defaultFePerEmc;
	private final int maxFePerTick;
	private final long storage;

	EnumEntropySinkTier(String name,
			@Range(from = 1, to = Integer.MAX_VALUE) int defaultFePerEmc,
			@Range(from = 1, to = Integer.MAX_VALUE) int maxFePerTick,
			@Range(from = 1, to = Long.MAX_VALUE) long storage) {
		this.name = name;
		this.defaultFePerEmc = defaultFePerEmc;
		this.maxFePerTick = maxFePerTick;
		this.storage = storage;
	}

	@NotNull
	@Override
	public String getSerializedName() {
		return name;
	}

	public int getDefaultFePerEmc() {
		return defaultFePerEmc;
	}

	/** Current FE cost of 1 EMC (server config, falls back to the default before the config loads). */
	public int getFePerEmc() {
		int value = switch (this) {
			case BASIC -> EquivoxConfig.server.entropySink.basicFePerEmc.getOrDefault();
			case DARK -> EquivoxConfig.server.entropySink.darkFePerEmc.getOrDefault();
			case RED -> EquivoxConfig.server.entropySink.redFePerEmc.getOrDefault();
		};
		return Math.max(1, value);
	}

	/** Max FE accepted / converted per tick. */
	public int getMaxFePerTick() {
		return maxFePerTick;
	}

	/** Internal FE buffer (one second of max input). */
	public int getFeCapacity() {
		return maxFePerTick * 20;
	}

	/** EMC buffer used while the owner is offline. */
	@Range(from = 1, to = Long.MAX_VALUE)
	public long getStorage() {
		return storage;
	}

	@Override
	public String toString() {
		return getSerializedName();
	}
}
