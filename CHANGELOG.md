# Changelog — ProjectE (Unofficial 26.1.2 port)

All notable changes to [Yaskulsky/projecte-26-port](https://github.com/Yaskulsky/projecte-26-port) are documented here.

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

Unofficial community port of ProjectE for Minecraft **26.1.2** / **NeoForge**.  
Maintainer: **Yaskulsky**

See git history for prior changes.
