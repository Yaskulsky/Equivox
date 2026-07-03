package moze_intel.projecte.integration.recipe_viewer.alias;

import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.world.item.Items;

public final class ProjectEAliasMapping implements IAliasMapping {

	@Override
	public <ITEM> void addAliases(RVAliasHelper<ITEM> rv) {
		addBlockAliases(rv);
		addGearAliases(rv);
		addMiscAliases(rv);
	}

	private <ITEM> void addBlockAliases(RVAliasHelper<ITEM> rv) {
		rv.addAliases(PEBlocks.ALCHEMICAL_CHEST, ProjectEAliases.ITEM_STORAGE);
		rv.addAliases(PETags.Items.COLLECTORS, ProjectEAliases.EMC_GENERATOR);
		rv.addAliases(PEBlocks.DARK_MATTER_PEDESTAL, ProjectEAliases.AOE, ProjectEAliases.AOE_LONG);
		rv.addAliases(PETags.Items.RELAYS, ProjectEAliases.EMC_CHARGER, ProjectEAliases.EMC_TRANSFER);

		rv.addAliases(PEBlocks.ALCHEMICAL_COAL, ProjectEAliases.BLOCK_ALCHEMICAL_COAL);
		rv.addAliases(PEBlocks.MOBIUS_FUEL, ProjectEAliases.BLOCK_MOBIUS_FUEL);
		rv.addAliases(PEBlocks.AETERNALIS_FUEL, ProjectEAliases.BLOCK_AETERNALIS_FUEL);
		rv.addAliases(PEBlocks.DARK_MATTER, ProjectEAliases.BLOCK_DARK_MATTER);
		rv.addAliases(PEBlocks.RED_MATTER, ProjectEAliases.BLOCK_RED_MATTER);
	}

	private <ITEM> void addGearAliases(RVAliasHelper<ITEM> rv) {
		addArmorAliases(rv);
		addToolAliases(rv);
		rv.addAliases(PETags.Items.ALCHEMICAL_BAGS, ProjectEAliases.BACKPACK, ProjectEAliases.ITEM_STORAGE);
		rv.addAliases(List.of(
				PEItems.LOW_DIVINING_ROD,
				PEItems.MEDIUM_DIVINING_ROD,
				PEItems.HIGH_DIVINING_ROD
		), ProjectEAliases.EMC_DETECTOR);
		rv.addAliases(PEItems.MERCURIAL_EYE, ProjectEAliases.BUILDING_WAND);
		rv.addAliases(PEItems.MIND_STONE, ProjectEAliases.XP_STORAGE);
		rv.addAliases(PEItems.PHILOSOPHERS_STONE, ProjectEAliases.PORTABLE_CRAFTING_TABLE, ProjectEAliases.PORTABLE_WORKBENCH, ProjectEAliases.WORD_TRANSMUTATION);
		rv.addAliases(PEItems.TRANSMUTATION_TABLET, ProjectEAliases.PORTABLE_TRANSMUTATION);
		rv.addAliases(PEItems.BODY_STONE, ProjectEAliases.AUTO_HEALER);
		rv.addAliases(PEItems.SOUL_STONE, ProjectEAliases.AUTO_FEEDER);
		rv.addAliases(PEItems.LIFE_STONE, ProjectEAliases.AUTO_FEEDER, ProjectEAliases.AUTO_HEALER);
		rv.addAliases(PEItems.WATCH_OF_FLOWING_TIME, ProjectEAliases.TICK_ACCELERATOR, ProjectEAliases.TIME_CONTROL, ProjectEAliases.SLOW_HOSTILE, ProjectEAliases.SLOW_MOBS);

		rv.addAliases(PEItems.EVERTIDE_AMULET, ProjectEAliases.INFINITE_WATER, ProjectEAliases.WATER_WALKING, ProjectEAliases.WEATHER_CONTROL, ProjectEAliases.TOOL_RANGED);
		rv.addAliases(PEItems.VOLCANITE_AMULET, ProjectEAliases.INFINITE_LAVA, ProjectEAliases.LAVA_WALKING, ProjectEAliases.WEATHER_CONTROL, ProjectEAliases.TOOL_RANGED,
				ProjectEAliases.FIRE_PROTECTION);

		rv.addAliases(PEItems.ARCHANGEL_SMITE, ProjectEAliases.AOE, ProjectEAliases.AOE_LONG, ProjectEAliases.TOOL_WEAPON, ProjectEAliases.TOOL_RANGED,
				Items.ARROW::getDescriptionId);

		rv.addAliases(PEItems.BLACK_HOLE_BAND, ProjectEAliases.VOID_FLUID, ProjectEAliases.FLUID_REMOVER);
		rv.addAliases(PEItems.VOID_RING, ProjectEAliases.TELEPORATION, ProjectEAliases.SELF_TELEPORTER, Items.ENDER_PEARL::getDescriptionId);
		rv.addAliases(List.of(
				PEItems.BLACK_HOLE_BAND,
				PEItems.VOID_RING
		), ProjectEAliases.MAGNET, ProjectEAliases.TOOL_RANGED);
		rv.addAliases(List.of(
				PEBlocks.CONDENSER,
				PEBlocks.CONDENSER_MK2,
				PEItems.GEM_OF_ETERNAL_DENSITY,
				PEItems.VOID_RING
		), ProjectEAliases.CONDENSER_ITEMS, ProjectEAliases.CONDENSER_MATTER);

		rv.addAliases(List.of(
				PEItems.DESTRUCTION_CATALYST,
				PEItems.CATALYTIC_LENS
		), ProjectEAliases.AOE, ProjectEAliases.AOE_LONG);
		rv.addAliases(List.of(
				PEItems.HYPERKINETIC_LENS,
				PEItems.CATALYTIC_LENS
		), ProjectEAliases.EXPLOSIVE, ProjectEAliases.TOOL_RANGED);

		rv.addAliases(List.of(
				PEItems.ARCANA_RING,
				PEItems.ZERO_RING
		), ProjectEAliases.FREEZE, ProjectEAliases.TOOL_RANGED);
		rv.addAliases(List.of(
				PEItems.IGNITION_RING,
				PEItems.ZERO_RING
		), ProjectEAliases.AOE, ProjectEAliases.AOE_LONG, ProjectEAliases.FIRE_EXTINGUISHER);
		rv.addAliases(List.of(
				PEItems.ARCANA_RING,
				PEItems.IGNITION_RING
		), Items.FLINT_AND_STEEL::getDescriptionId, ProjectEAliases.FIRE_STARTER, ProjectEAliases.FIRE_PROTECTION, ProjectEAliases.TOOL_RANGED);
		rv.addAliases(List.of(
				PEItems.ARCANA_RING,
				PEItems.HARVEST_GODDESS_BAND
		), ProjectEAliases.AOE, ProjectEAliases.AOE_LONG, ProjectEAliases.PLANT_ACCELERATOR, ProjectEAliases.PLANT_GROWER);
		rv.addAliases(List.of(
				PEItems.ARCANA_RING,
				PEItems.SWIFTWOLF_RENDING_GALE
		), ProjectEAliases.REPEL_HOSTILE, ProjectEAliases.REPEL_MOB, ProjectEAliases.REPEL_PROJECTILE, ProjectEAliases.CREATIVE_FLIGHT, ProjectEAliases.LIGHTNING,
				ProjectEAliases.TOOL_RANGED);

		List<ITEM> repairItems = new ArrayList<>(rv.tagContents(PETags.Items.COVALENCE_DUST));
		repairItems.add(rv.ingredient(PEItems.REPAIR_TALISMAN));
		rv.addAliases(repairItems, ProjectEAliases.ITEM_REPAIR);
	}

	private <ITEM> void addArmorAliases(RVAliasHelper<ITEM> rv) {
		rv.addAliases(PEItems.GEM_BOOTS, ProjectEAliases.AUTO_STEP, ProjectEAliases.STEP_ASSIST, ProjectEAliases.MOVEMENT_SPEED);
		rv.addAliases(List.of(
				PEItems.GEM_LEGGINGS,
				PEBlocks.INTERDICTION_TORCH
		), ProjectEAliases.REPEL_HOSTILE, ProjectEAliases.REPEL_MOB, ProjectEAliases.REPEL_PROJECTILE);
		rv.addAliases(PEItems.GEM_CHESTPLATE, ProjectEAliases.EXPLOSIVE, ProjectEAliases.AUTO_FEEDER, ProjectEAliases.FIRE_PROTECTION);
		rv.addAliases(PEItems.GEM_HELMET, ProjectEAliases.NIGHT_VISION, ProjectEAliases.AUTO_HEALER, ProjectEAliases.LIGHTNING, ProjectEAliases.TOOL_RANGED);
	}

	private <ITEM> void addToolAliases(RVAliasHelper<ITEM> rv) {
		rv.addAliases(PEItems.RED_MATTER_KATAR, ProjectEAliases.TOOL_AXE, ProjectEAliases.TOOL_HOE, ProjectEAliases.TOOL_SHEARS, ProjectEAliases.TOOL_SWORD,
				ProjectEAliases.TOOL_WEAPON, ProjectEAliases.AOE, ProjectEAliases.AOE_LONG, ProjectEAliases.TOOL_RANGED);
		rv.addAliases(PEItems.RED_MATTER_MORNING_STAR, ProjectEAliases.TOOL_HAMMER, ProjectEAliases.TOOL_SHOVEL, ProjectEAliases.TOOL_PICKAXE,
				ProjectEAliases.AOE, ProjectEAliases.AOE_LONG);
		rv.addAliases(List.of(
				PEItems.DARK_MATTER_SWORD,
				PEItems.RED_MATTER_SWORD
		), ProjectEAliases.TOOL_WEAPON);
		rv.addAliases(List.of(
				PEItems.DARK_MATTER_HAMMER,
				PEItems.RED_MATTER_HAMMER
		), ProjectEAliases.AOE, ProjectEAliases.AOE_LONG, ProjectEAliases.TOOL_PICKAXE);
	}

	private <ITEM> void addMiscAliases(RVAliasHelper<ITEM> rv) {
		rv.addAliases(PEItems.IRON_BAND, ProjectEAliases.RING_BASE);
		rv.addAliases(PETags.Items.KLEIN_STARS, ProjectEAliases.EMC_STORAGE, ProjectEAliases.EMC_BATTERY);
		rv.addAliases(List.of(
				PEBlocks.NOVA_CATALYST,
				PEBlocks.NOVA_CATACLYSM
		), Items.TNT::getDescriptionId, ProjectEAliases.EXPLOSIVE);
	}
}