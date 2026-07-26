# Changelog — Equivox

All notable changes to [Yaskulsky/projecte-26-port](https://github.com/Yaskulsky/projecte-26-port) are documented here.

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

## [1.0.0] — 2026-07-17

Equivox public starting version (versioning restarted at 1.0 after the Equivox rebrand). Earlier fork builds used 1.2.x–1.5.0 numbering.

### Changed
- **Full rebrand to Equivox** — `modId` `equivox`, display name / creative tab **Equivox**, Java package `com.yaskulsky.equivox`, assets/data namespace `equivox`, JAR `equivox-*.jar`, config folder `config/Equivox`.
- Primary command is `/equivox` (was `/equivalence`).

### Added
- **Legacy `equivalence:` ID aliases** — worlds that ran Equivalence 1.4.0 keep resolving `equivalence:*` → `equivox:*`.
- Existing **`projecte:` → `equivox:*`** aliases retained (registry IDs only — no `/projecte` or `/equivalence` commands).
- Commands: only **`/equivox`** (legacy `/projecte` and `/equivalence` redirects removed).

### Note
Breaking rename from Equivalence 1.4.x. Not affiliated with or endorsed by the ProjectE authors.

---

## [1.4.0] — 2026-07-16

### Changed
- **Full rebrand to Equivalence** — `modId` `equivalence`, display name / creative tab **Equivalence**, Java package `com.yaskulsky.equivalence`, assets/data namespace `equivalence`, JAR `equivalence-*.jar`.
- Authors / mods.toml identity updated (Yaskulsky); upstream ProjectE authors credited in LICENSE / credits only.
- Update checker / docs no longer point at the official ProjectE CurseForge project as this mod.

### Added
- **Legacy `projecte:` ID aliases** — `DeferredRegister#addAlias` maps old `projecte:*` registry IDs to `equivalence:*` (items, blocks, block entities, menus, sounds, data components, attachments, etc.) so existing worlds can migrate after the rebrand. `/projecte` redirects to `/equivalence`.

### Note
Not affiliated with or endorsed by the ProjectE authors. Forked from their MIT-licensed codebase after a request to stop using the ProjectE name.
Datapack paths (`data/projecte/...`), config folder (`config/ProjectE` vs `config/Equivalence`), and other mods depending on modId `projecte` are **not** covered by registry aliases.

---

## [1.3.0] — 2026-07-15

### Changed
- **Textures** — bundled **Bbublick** ProjectE Retexture / Exchange Extended assets (used with author permission). Philosopher's Stone credit: Retro Exchange.
- Mod display name / creative tab: **ProjectEE** (no hardcoded version in item tooltips).
- Version bump to **1.3.0** (Minecraft 26.1.2 / NeoForge).

---

## [1.2.5] — 2026-07-11

### Fixed
- **Transmutation Table GUI rendering** — restored `extractLabels` override (skip vanilla labels that overlap slots / use invisible alpha on MC 26.1). Synced GUI code and textures from dev tree; search bar still uses `addRenderableWidget`.

---

## [1.2.4] — 2026-07-11

### Fixed
- **Transmutation Table search bar** — search field now uses `addRenderableWidget` so it actually draws on MC 26.1 (was invisible with `addWidget`).

---

## [1.2.3] — 2026-07-11

### Added
- **Search bar** on the Transmutation Table GUI — filter learned items by name while browsing pages.

### Fixed
- **Transmutation Table rendering** — Klein Star slot sprite now stitches to the GUI atlas (`slot/empty_klein_star`) instead of the block atlas, fixing missing/broken slot icons in the table UI.

### Changed
- Version bump to **1.2.3** (Minecraft 26.1.2 / NeoForge).

---

## [1.2.2] — earlier releases

Unofficial community port for Minecraft **26.1.2** / **NeoForge**.  
Maintainer: **Yaskulsky**

See git history for prior changes.
