# EMC values — Allthemodium & Powah (bundled in mod)

File: `data/projecte/pe_custom_conversions/atm_powah_compat.json`

## Powah baseline

**`energized_steel` = 2048 EMC (1× reference)**

All Powah material values are multiples of 2048.

### Materials

| Item | × steel | EMC |
|------|---------|-----|
| `dielectric_paste` | 0.5× | 1024 |
| `dielectric_casing` | 0.75× | 1536 |
| **`energized_steel`** | **1×** | **2048** |
| `energized_steel_block` | 9× ingot | 18432 |
| `uraninite_raw` | 1.5× | 3072 |
| `uraninite` | 2× | 4096 |
| `uraninite_block` | 9× uraninite | 36864 |
| `blazing_crystal` | 8× | 16384 |
| `niotic_crystal` | 32× | 65536 |
| `spirited_crystal` | 128× | 262144 |
| `nitro_crystal` | 512× | 1048576 |

Crystal ladder: each tier ×4 from the previous (blazing → niotic → spirited → nitro).

### Energy storage (tier proxy)

**ProjectE does not read FE from NBT.** Cell/battery EMC follows tier multiples of steel, aligned with Powah storage tiers (also ~1000 FE = 1 EMC on max capacity).

| Item | × steel | EMC |
|------|---------|-----|
| `battery_basic` | 1× | 2048 |
| `energy_cell_basic` | 2× | 4096 |
| `battery_hardened` | 4× | 8192 |
| `energy_cell_hardened` | 8× | 16384 |
| `battery_blazing` | 16× | 32768 |
| `energy_cell_blazing` | 32× | 65536 |
| `battery_niotic` | 64× | 131072 |
| `energy_cell_niotic` | 128× | 262144 |
| `battery_spirited` | 256× | 524288 |
| `energy_cell_spirited` | 512× | 1048576 |
| `battery_nitro` | 1024× | 2097152 |
| `energy_cell_nitro` | 2048× | 4194304 |

Each storage tier: **×4** from the previous (matches Powah cell upgrades).

---

## Allthemodium (unchanged)

| Item | EMC |
|------|-----|
| ancient_stone / leaves / sapling | 16 |
| ancient_debris | 64 |
| soul_stone | 32 |
| **allthemodium_ingot** | **122800** |
| vibranium_ingot | 245600 (2×) |
| unobtainium_ingot | 491200 (4×) |
| nugget | ingot ÷ 9 |
| block | ingot × 9 |
| raw | ~75% ingot |

Reference: ProjectE Integration (Allthemodium ingot 122800).

---

## FE note

True **1 FE = 10 EMC** at runtime is **not supported**. To approximate that rate, multiply all Powah storage EMC values by **100** (not recommended for balance).
