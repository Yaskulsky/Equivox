#!/usr/bin/env python3
"""Generate energizedpower.json pe_custom_conversions file."""
import json
from pathlib import Path

# Vanilla-ish reference EMC (ProjectE defaults)
V = {
    "iron": 256,
    "copper": 128,
    "gold": 2048,
    "tin": 256,
    "redstone": 64,
    "diamond": 8192,
    "emerald": 16384,
    "amethyst": 32,
    "quartz": 256,
    "coal": 128,
    "stick": 32,
    "glass_pane": 1,
    "brick": 64,
    "furnace": 1344,
    "stonecutter": 256,
    "blue_ice": 72,
    "lapis": 64,
    "flint": 64,
    "wool": 48,
    "plank": 32,
    "cobble": 1,
    "stone": 1,
    "bucket": 768,
}

# User anchor values (exact)
A = {
    "redstone_alloy_ingot": 384,
    "energized_copper_ingot": 1024,
    "energized_gold_ingot": 8196,
    "advanced_alloy_ingot": 3072,
    "steel_ingot": 512,
    "energized_copper_plate": 1536,
    "energized_gold_plate": 10256,
}

# Derived base constants
SILICON = 128
INSULATOR = 16
PLATE_MULT = 1.5
NUGGET_DIV = 9
DUST_MULT = 0.5
WIRE_DIV = 3  # 3 wires per plate (metal press)

def plate(ingot):
    return int(ingot * PLATE_MULT)

def nugget(ingot):
    return max(1, int(ingot / NUGGET_DIV))

def dust(ingot):
    return max(1, int(ingot * DUST_MULT))

def wire_from_plate(p):
    return max(1, int(p / WIRE_DIV))

def block_val(ingot):
    return ingot * 9

def cable_per(ingot_emc, count=6):
    return max(1, int((ingot_emc + INSULATOR * 6) / count))

# Build EMC map
E = {}

# --- Anchors ---
E.update(A)

# --- Tin chain ---
E["tin_ingot"] = V["tin"]
E["tin_nugget"] = nugget(V["tin"])
E["tin_dust"] = dust(V["tin"])
E["tin_plate"] = plate(V["tin"])
E["tin_wire"] = wire_from_plate(E["tin_plate"])
E["tin_cable"] = cable_per(V["tin"])
E["tin_block"] = block_val(V["tin"])
E["raw_tin"] = V["tin"]
E["raw_tin_block"] = block_val(V["tin"])
E["tin_ore"] = V["tin"]
E["deepslate_tin_ore"] = V["tin"]

# --- Copper chain ---
E["copper_dust"] = dust(V["copper"])
E["copper_plate"] = plate(V["copper"])
E["copper_wire"] = wire_from_plate(E["copper_plate"])
E["copper_cable"] = cable_per(V["copper"])
E["copper_hammer"] = V["copper"] + V["stick"]
E["copper_fluid_pipe"] = E["copper_plate"]

# --- Iron chain ---
E["iron_dust"] = dust(V["iron"])
E["iron_plate"] = plate(V["iron"])
E["iron_rod"] = V["iron"] // 2
E["iron_gear"] = E["iron_plate"]
E["iron_hammer"] = V["iron"] + V["stick"]
E["iron_fluid_pipe"] = E["iron_plate"]

# --- Gold chain ---
E["gold_dust"] = dust(V["gold"])
E["gold_plate"] = plate(V["gold"])
E["gold_wire"] = wire_from_plate(E["gold_plate"])
E["gold_cable"] = cable_per(V["gold"])
E["golden_hammer"] = V["gold"] + V["stick"]
E["golden_fluid_pipe"] = E["gold_plate"]

# --- Steel chain ---
E["steel_nugget"] = nugget(E["steel_ingot"])
E["steel_plate"] = plate(E["steel_ingot"])
E["steel_rod"] = E["steel_ingot"] // 2
E["steel_gear"] = E["steel_plate"]
E["steel_block"] = block_val(E["steel_ingot"])
E["steel_fluid_pipe"] = E["steel_plate"]

# --- Silicon ---
E["silicon"] = SILICON
E["silicon_block"] = block_val(SILICON)

# --- Cable insulator (villager trade only) ---
E["cable_insulator"] = INSULATOR

# --- Alloy plates/blocks ---
E["advanced_alloy_block"] = block_val(E["advanced_alloy_ingot"])
E["advanced_alloy_plate"] = plate(E["advanced_alloy_ingot"])
E["redstone_alloy_plate"] = plate(E["redstone_alloy_ingot"])  # not anchor but consistent

# --- Energized copper/gold wires & cables ---
E["energized_copper_wire"] = wire_from_plate(E["energized_copper_plate"])
E["energized_copper_cable"] = cable_per(E["energized_copper_ingot"], 3)
E["energized_gold_wire"] = wire_from_plate(E["energized_gold_plate"])
E["energized_gold_cable"] = cable_per(E["energized_gold_ingot"], 3)

# --- Crystal / elite alloys (tiered above energized gold) ---
E["crystallized_lapis_lazuli"] = 2048
E["crystal_matrix"] = 8192  # diamond-tier composite
E["energized_crystal_matrix"] = 16384
E["crystallized_alloy_ingot"] = (
    2 * E["crystallized_lapis_lazuli"]
    + 2 * E["crystal_matrix"]
    + 2 * E["advanced_alloy_ingot"]
)
E["crystallized_alloy_plate"] = plate(E["crystallized_alloy_ingot"])
E["energized_alloy_ingot"] = 32768
E["energized_alloy_plate"] = plate(E["energized_alloy_ingot"])

# --- Superconductor (recipe / 3 with markup) ---
E["superconductor"] = (
    4 * 1024  # coolant_cell placeholder
    + 4 * E["crystallized_lapis_lazuli"]
    + 2 * E["energized_alloy_plate"]
    + 6 * E["energized_gold_wire"]
) // 3

# --- Circuits ---
E["basic_circuit"] = (
    2 * V["redstone"] + 3 * E["copper_wire"] + E["iron_plate"] + SILICON
)
E["advanced_circuit"] = (
    4 * E["basic_circuit"]
    + 4 * E["energized_copper_wire"]
    + 4 * SILICON
    + 2 * V["redstone"]
)

# --- Solar cells ---
E["basic_solar_cell"] = V["copper"] + V["quartz"] + 2 * SILICON + V["tin"]
E["advanced_solar_cell"] = (
    2 * E["basic_solar_cell"]
    + 4 * E["energized_copper_ingot"]
    + 2 * V["tin"]
    + E["redstone_alloy_ingot"]
)
E["elite_solar_cell"] = (
    2 * E["advanced_solar_cell"]
    + 4 * E["energized_gold_ingot"]
    + 2 * E["advanced_alloy_ingot"]
    + E["crystallized_lapis_lazuli"]
)
E["reinforced_advanced_solar_cell"] = (
    2 * E["elite_solar_cell"]
    + 4 * E["energized_alloy_ingot"]
    + 2 * E["energized_crystal_matrix"]
)

# --- Machine frames ---
E["basic_machine_frame"] = 4 * V["copper"] + 4 * V["iron"] + E["silicon_block"]
E["hardened_machine_frame"] = (
    E["basic_machine_frame"]
    + 2 * E["iron_gear"]
    + 2 * E["iron_rod"]
    + 4 * SILICON
    + 2 * E["steel_ingot"]
)
E["advanced_machine_frame"] = (
    E["hardened_machine_frame"]
    + 4 * E["advanced_alloy_ingot"]
    + 4 * E["energized_copper_ingot"]
    + 4 * SILICON
)
E["reinforced_advanced_machine_frame"] = (
    E["advanced_machine_frame"]
    + 4 * E["energized_crystal_matrix"]
    + 4 * E["silicon_block"]
)

# --- Processing units ---
E["processing_unit"] = (
    4 * E["advanced_circuit"]
    + 6 * E["energized_gold_wire"]
    + 6 * SILICON
)
E["quantum_processing_unit"] = (
    4 * E["processing_unit"]
    + 4 * E["superconductor"]
    + 4 * E["crystallized_lapis_lazuli"]
    + 2 * 1024  # coolant_cell
)
E["teleporter_processing_unit"] = E["quantum_processing_unit"] * 2

# --- Coolant ---
E["coolant_cell"] = (
    V["blue_ice"] + 4 * 64 + 4 * E["crystallized_lapis_lazuli"] + 4 * E["tin_plate"]
) // 4

# --- Batteries (tiered chain) ---
E["battery_1"] = 2 * E["tin_nugget"] + 3 * E["copper_plate"] + V["redstone"] + V["coal"]
for i in range(2, 9):
    prev = E[f"battery_{i-1}"]
    if i <= 4:
        E[f"battery_{i}"] = int(prev * 1.8 + E["copper_plate"] + SILICON)
    elif i <= 6:
        E[f"battery_{i}"] = int(prev * 1.8 + E["energized_copper_plate"])
    else:
        E[f"battery_{i}"] = int(
            prev * 1.8 + E["energized_gold_plate"] + E["advanced_alloy_plate"]
        )

# --- Solar panels ---
E["solar_panel_1"] = (
    3 * V["glass_pane"]
    + E["basic_solar_cell"]
    + V["redstone"]
    + V["copper"]
    + V["iron"]
)
for i in range(2, 8):
    prev = E[f"solar_panel_{i-1}"]
    tier_cell = [
        E["basic_solar_cell"],
        E["advanced_solar_cell"],
        E["advanced_solar_cell"],
        E["elite_solar_cell"],
        E["elite_solar_cell"],
        E["reinforced_advanced_solar_cell"],
    ][i - 2]
    E[f"solar_panel_{i}"] = int(prev * 1.5 + tier_cell + 3 * V["glass_pane"])

# --- Upgrade modules ---
def upgrade_chain(prefix, base, tiers=8, mult=1.6):
    E[f"{prefix}_1"] = base
    for i in range(2, tiers + 1):
        E[f"{prefix}_{i}"] = int(E[f"{prefix}_{i-1}"] * mult)

E["basic_upgrade_module"] = (
    E["basic_circuit"] + 2 * E["copper_plate"] + 3 * E["gold_wire"]
)
E["advanced_upgrade_module"] = (
    E["advanced_circuit"] + E["basic_upgrade_module"] + 2 * E["energized_copper_plate"] + 3 * E["energized_gold_wire"]
)
E["elite_upgrade_module"] = int(E["advanced_upgrade_module"] * 2.5 + E["crystallized_lapis_lazuli"])
E["reinforced_advanced_upgrade_module"] = int(
    E["elite_upgrade_module"] * 2 + E["energized_crystal_matrix"]
)

upgrade_chain("speed_upgrade_module", 512)
upgrade_chain("energy_capacity_upgrade_module", 768)
upgrade_chain("energy_efficiency_upgrade_module", 640)
upgrade_chain("energy_production_upgrade_module", 896)
upgrade_chain("duration_upgrade_module", 384, tiers=6)
upgrade_chain("extraction_depth_upgrade_module", 448, tiers=6)
upgrade_chain("extraction_range_upgrade_module", 448, tiers=6)
upgrade_chain("item_ejector_upgrade_module", 384, tiers=6)
upgrade_chain("item_pulling_upgrade_module", 384, tiers=6)
upgrade_chain("range_upgrade_module", 512, tiers=3)
upgrade_chain("moon_light_upgrade_module", 256, tiers=5, mult=1.4)

E["blast_furnace_upgrade_module"] = 2048
E["smoker_upgrade_module"] = 1536

# --- Hammers & tools ---
E["wooden_hammer"] = V["plank"] + V["stick"] * 2
E["stone_hammer"] = V["cobble"] * 3 + V["stick"] * 2
E["iron_hammer"] = E["iron_hammer"] if False else V["iron"] + V["stick"] * 2
E["copper_hammer"] = V["copper"] + V["stick"] * 2
E["golden_hammer"] = V["gold"] + V["stick"] * 2
E["diamond_hammer"] = V["diamond"] + V["stick"] * 2
E["netherite_hammer"] = 73728  # netherite tier
E["wrench"] = V["iron"] + nugget(V["iron"]) * 2
E["cutter"] = V["iron"] * 2 + V["stick"]

# --- Press molds ---
E["gear_press_mold"] = V["iron"] * 3
E["rod_press_mold"] = V["iron"] * 3
E["wire_press_mold"] = V["iron"] * 3
E["raw_gear_press_mold"] = V["iron"] * 2
E["raw_rod_press_mold"] = V["iron"] * 2
E["raw_wire_press_mold"] = V["iron"] * 2

# --- Misc materials ---
E["charcoal_dust"] = 64
E["charcoal_filter"] = E["charcoal_dust"] * 4 + V["wool"]
E["sawdust"] = 8
E["sawdust_block"] = E["sawdust"] * 9
E["stone_pebble"] = 1
E["saw_blade"] = V["iron"] * 2

# --- Fertilizers ---
E["basic_fertilizer"] = V["redstone"] + V["coal"]
E["good_fertilizer"] = E["basic_fertilizer"] * 2 + SILICON
E["advanced_fertilizer"] = E["good_fertilizer"] * 2 + E["redstone_alloy_ingot"]

# --- Fluid pipes ---
E["fluid_pipe"] = E["copper_plate"]
E["pressurized_fluid_pipe"] = E["steel_plate"]
E["copper_fluid_pipe"] = E["copper_plate"]
E["golden_fluid_pipe"] = E["gold_plate"]
E["steel_fluid_pipe"] = E["steel_plate"]

# --- Fluid tanks ---
E["fluid_tank_small"] = E["copper_plate"] * 4 + V["glass_pane"] * 4
E["fluid_tank_medium"] = E["fluid_tank_small"] * 2 + E["iron_plate"] * 2
E["fluid_tank_large"] = E["fluid_tank_medium"] * 2 + E["steel_plate"] * 4

# --- Item silos ---
E["item_silo_tiny"] = E["copper_plate"] * 4 + V["iron"] * 2
E["item_silo_small"] = E["item_silo_tiny"] * 2
E["item_silo_medium"] = E["item_silo_small"] * 2
E["item_silo_large"] = E["item_silo_medium"] * 2
E["item_silo_giant"] = E["item_silo_large"] * 2

# --- Conveyor belts (tiered) ---
def belt_set(tier_name, base):
    for kind in [
        "item_conveyor_belt",
        "item_conveyor_belt_loader",
        "item_conveyor_belt_merger",
        "item_conveyor_belt_sorter",
        "item_conveyor_belt_splitter",
        "item_conveyor_belt_switch",
    ]:
        prefix = tier_name + "_" if tier_name else ""
        E[prefix + kind] = base
        if "loader" in kind or "sorter" in kind:
            E[prefix + kind] = int(base * 1.3)
        elif "merger" in kind or "splitter" in kind:
            E[prefix + kind] = int(base * 1.2)

belt_set("", E["iron_plate"] + V["redstone"])
belt_set("fast", int((E["iron_plate"] + V["redstone"]) * 2))
belt_set("express", int((E["iron_plate"] + V["redstone"]) * 4))

# --- Transformers ---
def transformer_set(prefix, tier_mult, config_name=None):
    base = E["basic_machine_frame"] + E["copper_cable"] * 4
    val = int(base * tier_mult)
    E[f"{prefix}transformer_1_to_n"] = val
    E[f"{prefix}transformer_3_to_3"] = int(val * 1.2)
    E[f"{prefix}transformer_n_to_1"] = int(val * 1.1)
    if config_name:
        E[config_name] = int(val * 1.5)


transformer_set("", 1.0)
transformer_set("lv_", 1.5, "configurable_lv_transformer")
transformer_set("mv_", 2.5, "configurable_mv_transformer")
transformer_set("hv_", 4.0, "configurable_hv_transformer")
transformer_set("ehv_", 8.0, "configurable_ehv_transformer")

# --- Machines (parts + 25% markup) ---
def machine(name, parts):
    E[name] = int(sum(parts) * 1.25)

machine("alloy_furnace", [E["iron_plate"] * 5, V["furnace"], V["brick"] * 3])
machine("pulverizer", [E["basic_machine_frame"], E["iron_plate"] * 4, V["stonecutter"]])
machine("advanced_pulverizer", [E["pulverizer"], E["advanced_machine_frame"], E["steel_plate"] * 4])
machine("crusher", [E["basic_machine_frame"], E["iron_plate"] * 4, V["cobble"] * 8])
machine("advanced_crusher", [E["crusher"], E["advanced_machine_frame"], E["steel_plate"] * 4])
machine("charger", [E["basic_machine_frame"], E["silicon_block"], E["copper_plate"] * 2, E["iron_plate"] * 2])
machine("advanced_charger", [E["charger"], E["advanced_machine_frame"], E["energized_copper_plate"] * 2])
machine("uncharger", [E["charger"], E["copper_plate"] * 2])
machine("advanced_uncharger", [E["uncharger"], E["advanced_machine_frame"]])
machine("energizer", [E["advanced_machine_frame"], E["energized_copper_plate"] * 4, E["advanced_circuit"]])
machine("compressor", [E["hardened_machine_frame"], E["steel_plate"] * 4, E["iron_piston"] if False else E["iron_plate"] * 2])
machine("metal_press", [E["hardened_machine_frame"], E["iron_gear"] * 2, E["steel_plate"] * 2])
machine("auto_press_mold_maker", [E["metal_press"], E["basic_circuit"]])
machine("press_mold_maker", [E["iron_plate"] * 4, V["stonecutter"]])
machine("assembling_machine", [E["advanced_machine_frame"], E["advanced_circuit"] * 2, E["steel_gear"] * 2])
machine("auto_crafter", [E["hardened_machine_frame"], E["basic_circuit"], V["crafting_table"] if False else 128])
machine("advanced_auto_crafter", [E["auto_crafter"], E["advanced_machine_frame"], E["advanced_circuit"]])
machine("auto_stonecutter", [E["hardened_machine_frame"], V["stonecutter"], E["iron_plate"] * 2])
machine("powered_furnace", [E["hardened_machine_frame"], V["furnace"], E["redstone_alloy_ingot"]])
machine("advanced_powered_furnace", [E["powered_furnace"], E["advanced_machine_frame"], E["energized_copper_ingot"] * 2])
machine("induction_smelter", [E["alloy_furnace"], E["hardened_machine_frame"], E["steel_plate"] * 4])
machine("sawmill", [E["hardened_machine_frame"], E["saw_blade"], E["iron_plate"] * 2])
machine("crystal_growth_chamber", [E["advanced_machine_frame"], E["crystallized_lapis_lazuli"], V["glass_pane"] * 8])
machine("plant_growth_chamber", [E["advanced_machine_frame"], E["good_fertilizer"] * 4, V["glass_pane"] * 8])
machine("filtration_plant", [E["advanced_machine_frame"], E["charcoal_filter"] * 4, E["copper_plate"] * 4])
machine("fluid_pump", [E["hardened_machine_frame"], E["iron_plate"] * 2, E["copper_plate"] * 2])
machine("advanced_fluid_pump", [E["fluid_pump"], E["advanced_machine_frame"], E["energized_copper_plate"] * 2])
machine("fluid_drainer", [E["fluid_pump"], E["copper_plate"] * 4])
machine("fluid_filler", [E["fluid_pump"], E["copper_plate"] * 4])
machine("fluid_freezer", [E["advanced_machine_frame"], V["blue_ice"] * 4, E["coolant_cell"]])
machine("fluid_transposer", [E["advanced_machine_frame"], E["fluid_filler"], E["fluid_drainer"]])
machine("fluid_analyzer", [E["advanced_machine_frame"], E["advanced_circuit"], V["glass_pane"] * 4])
machine("drain", [E["copper_plate"] * 2, V["iron"]])
machine("block_placer", [E["hardened_machine_frame"], E["basic_circuit"], V["piston"] if False else 256])
machine("coal_engine", [E["hardened_machine_frame"], V["furnace"], E["iron_plate"] * 4])
machine("inventory_coal_engine", [E["coal_engine"], E["chest"] if False else 256])
machine("heat_generator", [E["hardened_machine_frame"], V["furnace"], E["copper_plate"] * 4])
machine("thermal_generator", [E["advanced_machine_frame"], E["heat_generator"], E["redstone_alloy_ingot"] * 2])
machine("lightning_generator", [E["reinforced_advanced_machine_frame"], E["energized_gold_ingot"] * 4, V["diamond"]])
machine("charging_station", [E["advanced_charger"], E["energized_copper_plate"] * 4])
machine("inventory_charger", [E["charger"], E["chest"] if False else 256])
machine("minecart_charger", [E["charger"], V["iron"] * 5])
machine("advanced_minecart_charger", [E["advanced_charger"], V["iron"] * 5])
machine("minecart_uncharger", [E["uncharger"], V["iron"] * 5])
machine("advanced_minecart_uncharger", [E["advanced_uncharger"], V["iron"] * 5])
machine("battery_box", [E["hardened_machine_frame"], E["battery_2"] * 2, E["copper_plate"] * 4])
machine("advanced_battery_box", [E["battery_box"], E["advanced_machine_frame"], E["battery_4"]])
machine("battery_box_minecart", [E["battery_box"], V["iron"] * 5])
machine("advanced_battery_box_minecart", [E["advanced_battery_box"], V["iron"] * 5])
machine("energy_analyzer", [E["advanced_machine_frame"], E["advanced_circuit"], E["energized_copper_wire"] * 4])
machine("stone_liquefier", [E["hardened_machine_frame"], V["lava_bucket"] if False else 832, E["copper_plate"] * 2])
machine("stone_solidifier", [E["stone_liquefier"], E["iron_plate"] * 2])
machine("teleporter", [E["reinforced_advanced_machine_frame"], E["teleporter_processing_unit"], E["energized_alloy_ingot"] * 4])
machine("teleporter_matrix", [E["teleporter"] * 4])
machine("inventory_teleporter", [E["teleporter"], E["chest"] if False else 256])
machine("time_controller", [E["reinforced_advanced_machine_frame"], E["quantum_processing_unit"], V["clock"] if False else 128])
machine("weather_controller", [E["reinforced_advanced_machine_frame"], E["quantum_processing_unit"], V["diamond"]])
machine("powered_lamp", [E["redstone_alloy_ingot"], V["glowstone"] if False else 384, V["glass_pane"]])

# --- Energized crystal matrix cable ---
E["energized_crystal_matrix_cable"] = cable_per(E["energized_crystal_matrix"], 1)

# Items intentionally skipped
SKIP = {
    "creative_battery",
    "creative_battery_box",
    "creative_fluid_tank",
    "creative_item_silo",
    "energized_power_book",
    "dirty_water_bucket",
}

# Full item list from mod jar
ALL_ITEMS = """
advanced_alloy_block advanced_alloy_ingot advanced_alloy_plate advanced_auto_crafter
advanced_battery_box advanced_battery_box_minecart advanced_charger advanced_circuit
advanced_crusher advanced_fertilizer advanced_fluid_pump advanced_machine_frame
advanced_minecart_charger advanced_minecart_uncharger advanced_powered_furnace
advanced_pulverizer advanced_solar_cell advanced_uncharger advanced_upgrade_module
alloy_furnace assembling_machine auto_crafter auto_press_mold_maker auto_stonecutter
basic_circuit basic_fertilizer basic_machine_frame basic_solar_cell basic_upgrade_module
battery_1 battery_2 battery_3 battery_4 battery_5 battery_6 battery_7 battery_8
battery_box battery_box_minecart blast_furnace_upgrade_module block_placer cable_insulator
charcoal_dust charcoal_filter charger charging_station coal_engine compressor
configurable_ehv_transformer configurable_hv_transformer configurable_lv_transformer
configurable_mv_transformer coolant_cell copper_cable copper_dust copper_fluid_pipe
copper_hammer copper_plate copper_wire creative_battery creative_battery_box
creative_fluid_tank creative_item_silo crusher crystal_growth_chamber crystal_matrix
crystallized_alloy_ingot crystallized_alloy_plate crystallized_lapis_lazuli cutter
deepslate_tin_ore diamond_hammer dirty_water_bucket drain duration_upgrade_module_1
duration_upgrade_module_2 duration_upgrade_module_3 duration_upgrade_module_4
duration_upgrade_module_5 duration_upgrade_module_6 ehv_transformer_1_to_n
ehv_transformer_3_to_3 ehv_transformer_n_to_1 elite_solar_cell elite_upgrade_module
energized_alloy_ingot energized_alloy_plate energized_copper_cable energized_copper_ingot
energized_copper_plate energized_copper_wire energized_crystal_matrix
energized_crystal_matrix_cable energized_gold_cable energized_gold_ingot energized_gold_plate
energized_gold_wire energized_power_book energizer energy_analyzer
energy_capacity_upgrade_module_1 energy_capacity_upgrade_module_2
energy_capacity_upgrade_module_3 energy_capacity_upgrade_module_4
energy_capacity_upgrade_module_5 energy_capacity_upgrade_module_6
energy_capacity_upgrade_module_7 energy_capacity_upgrade_module_8
energy_efficiency_upgrade_module_1 energy_efficiency_upgrade_module_2
energy_efficiency_upgrade_module_3 energy_efficiency_upgrade_module_4
energy_efficiency_upgrade_module_5 energy_efficiency_upgrade_module_6
energy_efficiency_upgrade_module_7 energy_efficiency_upgrade_module_8
energy_production_upgrade_module_1 energy_production_upgrade_module_2
energy_production_upgrade_module_3 energy_production_upgrade_module_4
energy_production_upgrade_module_5 energy_production_upgrade_module_6
energy_production_upgrade_module_7 energy_production_upgrade_module_8
express_item_conveyor_belt express_item_conveyor_belt_loader
express_item_conveyor_belt_merger express_item_conveyor_belt_sorter
express_item_conveyor_belt_splitter express_item_conveyor_belt_switch
extraction_depth_upgrade_module_1 extraction_depth_upgrade_module_2
extraction_depth_upgrade_module_3 extraction_depth_upgrade_module_4
extraction_depth_upgrade_module_5 extraction_depth_upgrade_module_6
extraction_range_upgrade_module_1 extraction_range_upgrade_module_2
extraction_range_upgrade_module_3 extraction_range_upgrade_module_4
extraction_range_upgrade_module_5 extraction_range_upgrade_module_6
fast_item_conveyor_belt fast_item_conveyor_belt_loader fast_item_conveyor_belt_merger
fast_item_conveyor_belt_sorter fast_item_conveyor_belt_splitter fast_item_conveyor_belt_switch
filtration_plant fluid_analyzer fluid_drainer fluid_filler fluid_freezer fluid_pipe
fluid_pump fluid_tank_large fluid_tank_medium fluid_tank_small fluid_transposer
gear_press_mold gold_cable gold_dust gold_plate gold_wire golden_fluid_pipe golden_hammer
good_fertilizer hardened_machine_frame heat_generator hv_transformer_1_to_n
hv_transformer_3_to_3 hv_transformer_n_to_1 induction_smelter inventory_charger
inventory_coal_engine inventory_teleporter iron_dust iron_gear iron_hammer iron_plate
iron_rod item_conveyor_belt item_conveyor_belt_loader item_conveyor_belt_merger
item_conveyor_belt_sorter item_conveyor_belt_splitter item_conveyor_belt_switch
item_ejector_upgrade_module_1 item_ejector_upgrade_module_2 item_ejector_upgrade_module_3
item_ejector_upgrade_module_4 item_ejector_upgrade_module_5 item_ejector_upgrade_module_6
item_pulling_upgrade_module_1 item_pulling_upgrade_module_2 item_pulling_upgrade_module_3
item_pulling_upgrade_module_4 item_pulling_upgrade_module_5 item_pulling_upgrade_module_6
item_silo_giant item_silo_large item_silo_medium item_silo_small item_silo_tiny
lightning_generator lv_transformer_1_to_n lv_transformer_3_to_3 lv_transformer_n_to_1
metal_press minecart_charger minecart_uncharger moon_light_upgrade_module_1
moon_light_upgrade_module_2 moon_light_upgrade_module_3 moon_light_upgrade_module_4
moon_light_upgrade_module_5 netherite_hammer plant_growth_chamber powered_furnace
powered_lamp press_mold_maker pressurized_fluid_pipe processing_unit pulverizer
quantum_processing_unit range_upgrade_module_1 range_upgrade_module_2 range_upgrade_module_3
raw_gear_press_mold raw_rod_press_mold raw_tin raw_tin_block raw_wire_press_mold
redstone_alloy_ingot reinforced_advanced_machine_frame reinforced_advanced_solar_cell
reinforced_advanced_upgrade_module rod_press_mold saw_blade sawdust sawdust_block sawmill
silicon silicon_block smoker_upgrade_module solar_panel_1 solar_panel_2 solar_panel_3
solar_panel_4 solar_panel_5 solar_panel_6 solar_panel_7 speed_upgrade_module_1
speed_upgrade_module_2 speed_upgrade_module_3 speed_upgrade_module_4 speed_upgrade_module_5
speed_upgrade_module_6 speed_upgrade_module_7 speed_upgrade_module_8 steel_block steel_fluid_pipe
steel_gear steel_ingot steel_nugget steel_plate steel_rod stone_hammer stone_liquefier
stone_pebble stone_solidifier superconductor teleporter teleporter_matrix
teleporter_processing_unit thermal_generator time_controller tin_block tin_cable tin_dust
tin_ingot tin_nugget tin_ore tin_plate tin_wire transformer_1_to_n transformer_3_to_3
transformer_n_to_1 uncharger weather_controller wire_press_mold wooden_hammer wrench
""".split()

missing = [i for i in ALL_ITEMS if i not in E and i not in SKIP]
if missing:
    print("WARNING: missing EMC for:", missing)

before = []
for item in sorted(E.keys()):
    before.append({
        "type": "projecte:item",
        "id": f"energizedpower:{item}",
        "emc_value": E[item],
    })

conversion = [
    # Ingot <-> block
    *[
        {
            "count": 9,
            "ingredients": [{"type": "projecte:item", "tag": f"c:storage_blocks/{metal}"}],
            "output": {"type": "projecte:item", "tag": f"c:ingots/{metal}"},
            "propagateTags": True,
        }
        for metal in [
            "tin", "steel", "advanced_alloy", "redstone_alloy",
            "energized_copper", "energized_gold", "crystallized_alloy", "energized_alloy",
        ]
    ],
    # Ingot -> plate
    *[
        {
            "ingredients": [{"type": "projecte:item", "tag": f"c:ingots/{metal}"}],
            "output": {"type": "projecte:item", "tag": f"c:plates/{metal}"},
            "propagateTags": True,
        }
        for metal in [
            "tin", "copper", "iron", "gold", "steel", "advanced_alloy",
            "redstone_alloy", "energized_copper", "energized_gold",
            "crystallized_alloy", "energized_alloy",
        ]
    ],
    # Ingot -> nugget
    {
        "count": 9,
        "ingredients": [{"type": "projecte:item", "tag": "c:ingots/tin"}],
        "output": {"type": "projecte:item", "tag": "c:nuggets/tin"},
        "propagateTags": True,
    },
    {
        "count": 9,
        "ingredients": [{"type": "projecte:item", "tag": "c:ingots/steel"}],
        "output": {"type": "projecte:item", "tag": "c:nuggets/steel"},
        "propagateTags": True,
    },
    # Ingot -> dust
    *[
        {
            "count": 2,
            "ingredients": [{"type": "projecte:item", "tag": f"c:ingots/{metal}"}],
            "output": {"type": "projecte:item", "tag": f"c:dusts/{metal}"},
            "propagateTags": True,
        }
        for metal in ["copper", "iron", "gold", "tin"]
    ],
    # Plate -> wire (3 wires)
    *[
        {
            "count": 3,
            "ingredients": [{"type": "projecte:item", "tag": f"c:plates/{metal}"}],
            "output": {"type": "projecte:item", "tag": f"c:wires/{metal}"},
            "propagateTags": True,
        }
        for metal in ["copper", "tin", "gold", "energized_copper", "energized_gold"]
    ],
    # Silicon block
    {
        "count": 9,
        "ingredients": [{"type": "projecte:item", "id": "energizedpower:silicon_block"}],
        "output": {"type": "projecte:item", "id": "energizedpower:silicon"},
    },
    {
        "ingredients": [{"type": "projecte:item", "id": "energizedpower:sawdust_block"}],
        "output": {"type": "projecte:item", "id": "energizedpower:sawdust"},
        "count": 9,
    },
]

doc = {
    "comment": (
        "Energized Power EMC values for ProjectE. Anchor values match ATM11 custom_emc.json; "
        "related items scaled by recipe tiers (plates ~1.5x ingot, nuggets 1/9, dust 1/2, "
        "wires 1/3 plate, cables by tier, batteries/solar/upgrades chained, machines = parts + 25%)."
    ),
    "values": {
        "before": before,
        "conversion": conversion,
    },
}

out = Path("src/datagen/generated/data/projecte/pe_custom_conversions/energizedpower.json")
out.parent.mkdir(parents=True, exist_ok=True)
out.write_text(json.dumps(doc, indent=2) + "\n", encoding="utf-8")
print(f"Wrote {len(before)} EMC values to {out}")
print(f"Skipped {len(SKIP)} items: {sorted(SKIP)}")
if missing:
    print(f"Still missing from ALL_ITEMS: {missing}")
