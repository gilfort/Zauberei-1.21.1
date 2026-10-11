# Zauberei

Zauberei is a NeoForge mod for Minecraft 1.21.1 about a school of magic: the **Witchwood School** and its
**Magiccloth** school uniform.

## Content

- [Magiccloth Armor](Magiccloth-Armor) — animated school uniform in two styles, optional Ars Nouveau perk variants.
- [Introduction Letter](Introduction-Letter) — hands out the first-year supplies.
- [Witchwood School](Witchwood-School) — school structure above the world spawn.
- [Configuration](Configuration) — all config options.

## Requirements

- NeoForge for Minecraft 1.21.1
- GeckoLib
- Ars Nouveau (optional)

## Upgrading from 0.3.x to 1.0.0

The data-driven armor set effect system, the major/year commands and the timed command system were removed.
Existing worlds keep working: armor that still carries the old `zauberei:major` / `zauberei:year` data loads
normally. The old config folder `config/zauberei/set_armor/` and the file `config/zauberei/commands.json5` are no
longer read and can be deleted.
