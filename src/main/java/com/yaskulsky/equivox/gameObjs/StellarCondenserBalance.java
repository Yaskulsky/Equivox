package com.yaskulsky.equivox.gameObjs;

/**
 * Balance knobs for Stellar Condenser — tune here.
 */
public final class StellarCondenserBalance {

	public static final int RADIUS = 12;
	/** Soft per-minute EMC cap from non-boss kills (resets every 1200 ticks). */
	public static final long SOFT_CAP_PER_MINUTE = 2_048;
	/** Minimum ticks between awarding echo EMC for any kill. */
	public static final int KILL_COOLDOWN_TICKS = 5;
	/** Long cooldown after a boss echo payout. */
	public static final int BOSS_COOLDOWN_TICKS = 20 * 60 * 10; // 10 minutes
	public static final long BOSS_EMC = 2_048;

	public static final long EMC_ZOMBIE = 16;
	public static final long EMC_SKELETON = 16;
	public static final long EMC_CREEPER = 24;
	public static final long EMC_ENDERMAN = 128;
	public static final long EMC_WITHER_SKELETON = 64;
	public static final long EMC_PIGLIN = 20;
	public static final long EMC_DEFAULT_ANIMAL = 4;
	public static final long EMC_DEFAULT_MONSTER = 12;

	private StellarCondenserBalance() {
	}
}
