# Changelog

All notable changes between mod versions, newest first. Each entry lists everything that changed since the
previous version. Version lines: `2.x` = Minecraft 26.3 (branch `main`), `1.x` = Minecraft 1.21.1
(branch `1.21.1`).

---

## 2.0.0 — Minecraft 26.3

**Requires:** NeoForge 26.3, GeckoLib 5.5.7 or newer, Java 25.

### Changed
- Ported to Minecraft 26.3 / NeoForge 26.3 and GeckoLib 5.
- The config file is now `config/zauberei-local.toml` (was `zauberei-common.toml`) — copy your values over.
- Magiccloth armor rendering rewritten for GeckoLib 5; the hood and waistband layers follow head and body.
- Introduction Letter screen rebuilt on the new GUI system.

### Removed
- Ars Nouveau perk armor (`_perks` pieces) is unavailable until Ars Nouveau supports Minecraft 26.x.

---

## 1.0.0 — Minecraft 1.21.1

### Removed
- Data-driven armor set effect system, including the Sets Manager / Editor GUIs and its config folder
  `config/zauberei/set_armor/`.
- Major / year player data and the `/zauberei setmajor|setyear|checkmajor|checkyear|sets|debug` commands.
- Timed command system (`config/zauberei/commands.json5`).

### Changed
- Introduction Letter text now comes from the lang file (translatable); `letter_text` in the config is an
  optional override.
- Invalid ids in `letter_items` are skipped with a log warning instead of failing.

### Fixed
- Introduction Letter always greeted "Trainee" instead of the player name.
- Magiccloth tags failed to load without Ars Nouveau (optional `_perks` entries).
- Witchwood School was not placed when a second world was created in the same session.
- Armor carrying the old set effect data still loads in existing worlds.

---

## 0.3.0 — Minecraft 1.21.1

### Added
- Sets Manager, Set Wizard and Set Editor GUIs (`/zauberei sets`) to browse, create and edit armor set effects.
- Wildcard scopes for set effects: all majors, all majors & all years.
- Set display names, tooltip pagination for items in several sets (Shift + scroll).
- Ars Nouveau compatibility: Magiccloth variants with perk slots.
- Config option `enable_magical_armor` to hide the Magiccloth items.

### Changed
- Sets use the highest reached threshold and keep it when more pieces than needed are worn.

### Fixed
- Several sets on the same armor apply all values instead of overriding weaker ones.

---

## 0.2.5 — Minecraft 1.21.1

### Added
- Tag-based armor set effects (`#zauberei:magiccloth_armor`).
- Timed command system driven by advancements.

---

## 0.2.2 — Minecraft 1.21.1

### Added
- Enchantability for Magiccloth armor.

### Fixed
- Year tag of the player data.

---

## 0.1.0 — Minecraft 1.21.1

### Added
- Magiccloth armor set, Introduction Letter, Witchwood School spawning, English lang file, set effect tooltips.

---

## 0.0.1 — Minecraft 1.21.1

### Added
- First version: JSON-based armor set effects.
