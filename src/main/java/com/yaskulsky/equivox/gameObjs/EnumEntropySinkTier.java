package com.yaskulsky.equivox.gameObjs;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Balance knobs for Entropy Sink tiers — tune here.
 */
public enum EnumEntropySinkTier implements StringRepresentable {
	BASIC("entropy_sink", 20, 1, 50_000),
	DARK("entropy_sink_dark", 15, 4, 200_000),
	RED("entropy_sink_red", 10, 16, 1_000_000);

	private final String name;
	private final int burnIntervalTicks;
	private final int maxItemsPerBurn;
	private final long storage;

	EnumEntropySinkTier(String name,
			@Range(from = 1, to = Integer.MAX_VALUE) int burnIntervalTicks,
			@Range(from = 1, to = Integer.MAX_VALUE) int maxItemsPerBurn,
			@Range(from = 1, to = Long.MAX_VALUE) long storage) {
		this.name = name;
		this.burnIntervalTicks = burnIntervalTicks;
		this.maxItemsPerBurn = maxItemsPerBurn;
		this.storage = storage;
	}

	@NotNull
	@Override
	public String getSerializedName() {
		return name;
	}

	@Range(from = 1, to = Integer.MAX_VALUE)
	public int getBurnIntervalTicks() {
		return burnIntervalTicks;
	}

	@Range(from = 1, to = Integer.MAX_VALUE)
	public int getMaxItemsPerBurn() {
		return maxItemsPerBurn;
	}

	@Range(from = 1, to = Long.MAX_VALUE)
	public long getStorage() {
		return storage;
	}

	@Override
	public String toString() {
		return getSerializedName();
	}
}
