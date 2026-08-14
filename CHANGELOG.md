# Changelog — Equivox

All notable changes to [Yaskulsky/projecte-26-port](https://github.com/Yaskulsky/projecte-26-port) are documented here.

## [1.3.0] — 2026-08-14

### Added
- **Transmutation Provider** — place under a Transmutation Table; AE2/RS storage buses see the owner's learned items paid from personal EMC (`KnowledgeExportHandler` / `KnowledgeProvideHelper`).
- **Transmutation Provider ONLINE** — blockstate + dim textures when offline; table linked, owner online, export active.
- **Red Matter Pedestal** — craftable upgrade from Dark Matter Pedestal (textures/recipe/JEI).
- **Entropy Sink / Stellar Condenser texture folders** — own `textures/block/entropy_sink/` and `stellar_condenser/` (no longer share collectors).
- **AUTOMATION.md** — packer notes for Condenser / Provider / Arcane Tablet.
- **Jade** — soft-dep plugin ships in the JAR (optional at runtime).

### Changed
- **Energy Condenser MK2** — side-aware automation: horizontal faces insert, top/bottom extract (friendlier for AE2/RS).
- **Arcane Tablet JEI transfer** — clearer inventory/EMC satisfaction checks when optional integrations are enabled.
- **Transmutation Provider** — requires table above; virtual knowledge export instead of single lock + 3×3 buffer.

### Fixed
- **Transmutation Provider GUI** — status panel (link / owner / exposed count) instead of mistaken Collector layout; correct 256×256 texture blit.
- **AE2 Storage Bus** — export extract is transaction-safe so extractable-only scans no longer drain personal EMC / hide learned items.
- **Jade look-at** — `Device Online` / `Device Offline` as a gray line in the same tooltip (AE2-style); removed separate HUD overlay.

---

## [1.2.1] — 2026-08-08

### Fixed
- **Philosopher's Stone crash on NeoForge 26.1.2.21-beta+** — `PlayerHelper.checkBreakPermission` now links against `CommonHooks.fireBlockBreak(..., Player, ...) → BreakBlockEvent` instead of the removed `ServerPlayer` / `BlockEvent.BreakEvent` signature (`NoSuchMethodError` when right-clicking dirt/sand/cobble).

### Changed
- Compile / minimum NeoForge bumped to **26.1.2.76** / **`[26.1.2.21-beta,)`**.

---

## [1.2.0] — 2026-07-24

### Added
- **Entropy Sink** (Basic / Dark / Red) — hopper-fed block that burns EMC items into an internal buffer (pipe/relay extractable), with soft diminishing returns above ~1k EMC/s and optional learn-on-burn for the placing player.
- **Stellar Condenser** — owner-bound radius machine that grants death-echo EMC (not item-drop EMC) from nearby mob kills into personal EMC when online, or buffers while offline. Soft per-minute cap and boss cooldown included.

### Credits
- Original feature ideas for Equivox EMC sources (Entropy Sink / Stellar Condenser).

---

## [1.1.0] — 2026-07-22

### Added
- **Arcane Tablet** — upgraded portable transmutation tablet with integrated 3x3 crafting that pulls from learned items / EMC (BruceDelta / ProjectEX-style, MIT credit). JEI recipe transfer stubbed until optional integrations are enabled for 26.1.

---
## [1.0.0] ΓÇö 2026-07-17

Equivox public starting version (versioning restarted at 1.0 after the Equivox rebrand). Earlier fork builds used 1.2.xΓÇô1.5.0 numbering.

### Changed
- **Full rebrand to Equivox** ΓÇö `modId` `equivox`, display name / creative tab **Equivox**, Java package `com.yaskulsky.equivox`, assets/data namespace `equivox`, JAR `equivox-*.jar`, config folder `config/Equivox`.
- Primary command is `/equivox` (was `/equivalence`).

### Added
- **Legacy `equivalence:` ID aliases** ΓÇö worlds that ran Equivalence 1.4.0 keep resolving `equivalence:*` ΓåÆ `equivox:*`.
- Existing **`projecte:` ΓåÆ `equivox:*`** aliases retained (registry IDs only ΓÇö no `/projecte` or `/equivalence` commands).
- Commands: only **`/equivox`** (legacy `/projecte` and `/equivalence` redirects removed).

### Note
Breaking rename from Equivalence 1.4.x. Not affiliated with or endorsed by the ProjectE authors.

---

## [1.4.0] ΓÇö 2026-07-16

### Changed
- **Full rebrand to Equivalence** ΓÇö `modId` `equivalence`, display name / creative tab **Equivalence**, Java package `com.yaskulsky.equivalence`, assets/data namespace `equivalence`, JAR `equivalence-*.jar`.
- Authors / mods.toml identity updated (Yaskulsky); upstream ProjectE authors credited in LICENSE / credits only.
- Update checker / docs no longer point at the official ProjectE CurseForge project as this mod.

### Added
- **Legacy `projecte:` ID aliases** ΓÇö `DeferredRegister#addAlias` maps old `projecte:*` registry IDs to `equivalence:*` (items, blocks, block entities, menus, sounds, data components, attachments, etc.) so existing worlds can migrate after the rebrand. `/projecte` redirects to `/equivalence`.

### Note
Not affiliated with or endorsed by the ProjectE authors. Forked from their MIT-licensed codebase after a request to stop using the ProjectE name.
Datapack paths (`data/projecte/...`), config folder (`config/ProjectE` vs `config/Equivalence`), and other mods depending on modId `projecte` are **not** covered by registry aliases.

---

## [1.3.0] ΓÇö 2026-07-15

### Changed
- **Textures** ΓÇö bundled **Bbublick** ProjectE Retexture / Exchange Extended assets (used with author permission). Philosopher's Stone credit: Retro Exchange.
- Mod display name / creative tab: **ProjectEE** (no hardcoded version in item tooltips).
- Version bump to **1.3.0** (Minecraft 26.1.2 / NeoForge).

---

## [1.2.5] ΓÇö 2026-07-11

### Fixed
- **Transmutation Table GUI rendering** ΓÇö restored `extractLabels` override (skip vanilla labels that overlap slots / use invisible alpha on MC 26.1). Synced GUI code and textures from dev tree; search bar still uses `addRenderableWidget`.

---

## [1.2.4] ΓÇö 2026-07-11

### Fixed
- **Transmutation Table search bar** ΓÇö search field now uses `addRenderableWidget` so it actually draws on MC 26.1 (was invisible with `addWidget`).

---

## [1.2.3] ΓÇö 2026-07-11

### Added
- **Search bar** on the Transmutation Table GUI ΓÇö filter learned items by name while browsing pages.

### Fixed
- **Transmutation Table rendering** ΓÇö Klein Star slot sprite now stitches to the GUI atlas (`slot/empty_klein_star`) instead of the block atlas, fixing missing/broken slot icons in the table UI.

### Changed
- Version bump to **1.2.3** (Minecraft 26.1.2 / NeoForge).

---

## [1.2.2] ΓÇö earlier releases

Unofficial community port for Minecraft **26.1.2** / **NeoForge**.  
Maintainer: **Yaskulsky**

See git history for prior changes.