package com.yaskulsky.equivox.integration.recipe_viewer.alias;

import java.util.ArrayList;
import java.util.List;
import com.yaskulsky.equivox.gameObjs.PETags;
import com.yaskulsky.equivox.gameObjs.registries.PEBlocks;
import com.yaskulsky.equivox.gameObjs.registries.PEItems;
import net.minecraft.world.item.Items;

public final class EquivoxAliasMapping implements IAliasMapping {

	@Override
	public <ITEM> void addAliases(RVAliasHelper<ITEM> rv) {
		addBlockAliases(rv);
		addGearAliases(rv);
		addMiscAliases(rv);
	}

	private <ITEM> void addBlockAliases(RVAliasHelper<ITEM> rv) {
		rv.addAliases(PEBlocks.ALCHEMICAL_CHEST, EquivoxAliases.ITEM_STORAGE);
		rv.addAliases(PETags.Items.COLLECTORS, EquivoxAliases.EMC_GENERATOR);
		rv.addAliases(PEBlocks.DARK_MATTER_PEDESTAL, EquivoxAliases.AOE, EquivoxAliases.AOE_LONG);
		rv.addAliases(PEBlocks.RED_MATTER_PEDESTAL, EquivoxAliases.AOE, EquivoxAliases.AOE_LONG);
		rv.addAliases(PETags.Items.RELAYS, EquivoxAliases.EMC_CHARGER, EquivoxAliases.EMC_TRANSFER);

		rv.addAliases(PEBlocks.ALCHEMICAL_COAL, EquivoxAliases.BLOCK_ALCHEMICAL_COAL);
		rv.addAliases(PEBlocks.MOBIUS_FUEL, EquivoxAliases.BLOCK_MOBIUS_FUEL);
		rv.addAliases(PEBlocks.AETERNALIS_FUEL, EquivoxAliases.BLOCK_AETERNALIS_FUEL);
		rv.addAliases(PEBlocks.DARK_MATTER, EquivoxAliases.BLOCK_DARK_MATTER);
		rv.addAliases(PEBlocks.RED_MATTER, EquivoxAliases.BLOCK_RED_MATTER);
	}

	private <ITEM> void addGearAliases(RVAliasHelper<ITEM> rv) {
		addArmorAliases(rv);
		addToolAliases(rv);
		rv.addAliases(PETags.Items.ALCHEMICAL_BAGS, EquivoxAliases.BACKPACK, EquivoxAliases.ITEM_STORAGE);
		rv.addAliases(List.of(
				PEItems.LOW_DIVINING_ROD,
				PEItems.MEDIUM_DIVINING_ROD,
				PEItems.HIGH_DIVINING_ROD
		), EquivoxAliases.EMC_DETECTOR);
		rv.addAliases(PEItems.MERCURIAL_EYE, EquivoxAliases.BUILDING_WAND);
		rv.addAliases(PEItems.MIND_STONE, EquivoxAliases.XP_STORAGE);
		rv.addAliases(PEItems.PHILOSOPHERS_STONE, EquivoxAliases.PORTABLE_CRAFTING_TABLE, EquivoxAliases.PORTABLE_WORKBENCH, EquivoxAliases.WORD_TRANSMUTATION);
		rv.addAliases(PEItems.TRANSMUTATION_TABLET, EquivoxAliases.PORTABLE_TRANSMUTATION);
		rv.addAliases(PEItems.BODY_STONE, EquivoxAliases.AUTO_HEALER);
		rv.addAliases(PEItems.SOUL_STONE, EquivoxAliases.AUTO_FEEDER);
		rv.addAliases(PEItems.LIFE_STONE, EquivoxAliases.AUTO_FEEDER, EquivoxAliases.AUTO_HEALER);
		rv.addAliases(PEItems.WATCH_OF_FLOWING_TIME, EquivoxAliases.TICK_ACCELERATOR, EquivoxAliases.TIME_CONTROL, EquivoxAliases.SLOW_HOSTILE, EquivoxAliases.SLOW_MOBS);

		rv.addAliases(PEItems.EVERTIDE_AMULET, EquivoxAliases.INFINITE_WATER, EquivoxAliases.WATER_WALKING, EquivoxAliases.WEATHER_CONTROL, EquivoxAliases.TOOL_RANGED);
		rv.addAliases(PEItems.VOLCANITE_AMULET, EquivoxAliases.INFINITE_LAVA, EquivoxAliases.LAVA_WALKING, EquivoxAliases.WEATHER_CONTROL, EquivoxAliases.TOOL_RANGED,
				EquivoxAliases.FIRE_PROTECTION);

		rv.addAliases(PEItems.ARCHANGEL_SMITE, EquivoxAliases.AOE, EquivoxAliases.AOE_LONG, EquivoxAliases.TOOL_WEAPON, EquivoxAliases.TOOL_RANGED,
				Items.ARROW::getDescriptionId);

		rv.addAliases(PEItems.BLACK_HOLE_BAND, EquivoxAliases.VOID_FLUID, EquivoxAliases.FLUID_REMOVER);
		rv.addAliases(PEItems.VOID_RING, EquivoxAliases.TELEPORATION, EquivoxAliases.SELF_TELEPORTER, Items.ENDER_PEARL::getDescriptionId);
		rv.addAliases(List.of(
				PEItems.BLACK_HOLE_BAND,
				PEItems.VOID_RING
		), EquivoxAliases.MAGNET, EquivoxAliases.TOOL_RANGED);
		rv.addAliases(List.of(
				PEBlocks.CONDENSER,
				PEBlocks.CONDENSER_MK2,
				PEItems.GEM_OF_ETERNAL_DENSITY,
				PEItems.VOID_RING
		), EquivoxAliases.CONDENSER_ITEMS, EquivoxAliases.CONDENSER_MATTER);

		rv.addAliases(List.of(
				PEItems.DESTRUCTION_CATALYST,
				PEItems.CATALYTIC_LENS
		), EquivoxAliases.AOE, EquivoxAliases.AOE_LONG);
		rv.addAliases(List.of(
				PEItems.HYPERKINETIC_LENS,
				PEItems.CATALYTIC_LENS
		), EquivoxAliases.EXPLOSIVE, EquivoxAliases.TOOL_RANGED);

		rv.addAliases(List.of(
				PEItems.ARCANA_RING,
				PEItems.ZERO_RING
		), EquivoxAliases.FREEZE, EquivoxAliases.TOOL_RANGED);
		rv.addAliases(List.of(
				PEItems.IGNITION_RING,
				PEItems.ZERO_RING
		), EquivoxAliases.AOE, EquivoxAliases.AOE_LONG, EquivoxAliases.FIRE_EXTINGUISHER);
		rv.addAliases(List.of(
				PEItems.ARCANA_RING,
				PEItems.IGNITION_RING
		), Items.FLINT_AND_STEEL::getDescriptionId, EquivoxAliases.FIRE_STARTER, EquivoxAliases.FIRE_PROTECTION, EquivoxAliases.TOOL_RANGED);
		rv.addAliases(List.of(
				PEItems.ARCANA_RING,
				PEItems.HARVEST_GODDESS_BAND
		), EquivoxAliases.AOE, EquivoxAliases.AOE_LONG, EquivoxAliases.PLANT_ACCELERATOR, EquivoxAliases.PLANT_GROWER);
		rv.addAliases(List.of(
				PEItems.ARCANA_RING,
				PEItems.SWIFTWOLF_RENDING_GALE
		), EquivoxAliases.REPEL_HOSTILE, EquivoxAliases.REPEL_MOB, EquivoxAliases.REPEL_PROJECTILE, EquivoxAliases.CREATIVE_FLIGHT, EquivoxAliases.LIGHTNING,
				EquivoxAliases.TOOL_RANGED);

		List<ITEM> repairItems = new ArrayList<>(rv.tagContents(PETags.Items.COVALENCE_DUST));
		repairItems.add(rv.ingredient(PEItems.REPAIR_TALISMAN));
		rv.addAliases(repairItems, EquivoxAliases.ITEM_REPAIR);
	}

	private <ITEM> void addArmorAliases(RVAliasHelper<ITEM> rv) {
		rv.addAliases(PEItems.GEM_BOOTS, EquivoxAliases.AUTO_STEP, EquivoxAliases.STEP_ASSIST, EquivoxAliases.MOVEMENT_SPEED);
		rv.addAliases(List.of(
				PEItems.GEM_LEGGINGS,
				PEBlocks.INTERDICTION_TORCH
		), EquivoxAliases.REPEL_HOSTILE, EquivoxAliases.REPEL_MOB, EquivoxAliases.REPEL_PROJECTILE);
		rv.addAliases(PEItems.GEM_CHESTPLATE, EquivoxAliases.EXPLOSIVE, EquivoxAliases.AUTO_FEEDER, EquivoxAliases.FIRE_PROTECTION);
		rv.addAliases(PEItems.GEM_HELMET, EquivoxAliases.NIGHT_VISION, EquivoxAliases.AUTO_HEALER, EquivoxAliases.LIGHTNING, EquivoxAliases.TOOL_RANGED);
	}

	private <ITEM> void addToolAliases(RVAliasHelper<ITEM> rv) {
		rv.addAliases(PEItems.RED_MATTER_KATAR, EquivoxAliases.TOOL_AXE, EquivoxAliases.TOOL_HOE, EquivoxAliases.TOOL_SHEARS, EquivoxAliases.TOOL_SWORD,
				EquivoxAliases.TOOL_WEAPON, EquivoxAliases.AOE, EquivoxAliases.AOE_LONG, EquivoxAliases.TOOL_RANGED);
		rv.addAliases(PEItems.RED_MATTER_MORNING_STAR, EquivoxAliases.TOOL_HAMMER, EquivoxAliases.TOOL_SHOVEL, EquivoxAliases.TOOL_PICKAXE,
				EquivoxAliases.AOE, EquivoxAliases.AOE_LONG);
		rv.addAliases(List.of(
				PEItems.DARK_MATTER_SWORD,
				PEItems.RED_MATTER_SWORD
		), EquivoxAliases.TOOL_WEAPON);
		rv.addAliases(List.of(
				PEItems.DARK_MATTER_HAMMER,
				PEItems.RED_MATTER_HAMMER
		), EquivoxAliases.AOE, EquivoxAliases.AOE_LONG, EquivoxAliases.TOOL_PICKAXE);
	}

	private <ITEM> void addMiscAliases(RVAliasHelper<ITEM> rv) {
		rv.addAliases(PEItems.IRON_BAND, EquivoxAliases.RING_BASE);
		rv.addAliases(PETags.Items.KLEIN_STARS, EquivoxAliases.EMC_STORAGE, EquivoxAliases.EMC_BATTERY);
		rv.addAliases(List.of(
				PEBlocks.NOVA_CATALYST,
				PEBlocks.NOVA_CATACLYSM
		), Items.TNT::getDescriptionId, EquivoxAliases.EXPLOSIVE);
	}
}