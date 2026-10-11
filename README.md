# Zauberei

[![Build](https://github.com/gilfort/Zauberei-1.21.1/actions/workflows/build.yml/badge.svg)](https://github.com/gilfort/Zauberei-1.21.1/actions/workflows/build.yml)

A NeoForge mod for Minecraft 26.3 about a school of magic: the Witchwood School and its Magiccloth school
uniform.

**📖 Documentation: [Wiki](https://github.com/gilfort/Zauberei-1.21.1/wiki)**

## Features

- **Magiccloth Armor** — animated school uniform (GeckoLib) in two styles.
- **Introduction Letter** — tear it to receive configurable starting items.
- **Witchwood School** — optional school structure placed above the world spawn.
- **Ars Nouveau support** — optional Magiccloth variants with perk slots (disabled until Ars Nouveau supports 26.x).

## Installation

Requires NeoForge for Minecraft 26.3 and GeckoLib 5.5.7 or newer. Put the mod jar into the `mods` folder of client
and server.

## For developers

```bash
./gradlew build      # mod jar -> build/libs/
./gradlew runClient
./gradlew runData    # datagen -> src/generated/resources/
```

The wiki is generated from [`docs/wiki/`](docs/wiki) by a GitHub Action on every push to `main` — edit the
Markdown files there, never the wiki directly.

Conventions, branch / PR workflow and test rules: [CONTRIBUTING.md](CONTRIBUTING.md).
