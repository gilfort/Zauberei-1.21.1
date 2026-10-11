# Contributing to Zauberei

These rules apply to everyone working on the repository — people, local AI sessions and **cloud / remote AI
sessions** alike. Cloud sessions start from a fresh clone and only see committed files, so this file is the
binding reference for them.

## Target

> This is the **`1.21.1` maintenance branch** (mod versions `1.x`): bug fixes only. New features go to `main`
> (newest Minecraft version). See [Branches and versions](#branches-and-versions).

- Minecraft **1.21.1**, NeoForge 21.1.x (`gradle.properties` → `neo_version`), Java **21**
- Mod id `zauberei`, base package `com.gilfort.zauberei`, Mojang mappings with Parchment
  (`ResourceLocation`; create ids with `Zauberei.id(path)`)
- Dependencies: GeckoLib (required), Ars Nouveau (optional — Ars classes are only touched from `compat/`
  behind `ModList.get().isLoaded("ars_nouveau")`)
- Each registry gets its own class with a `register(IEventBus)` method, wired in the `Zauberei` constructor.
- Client-only code lives in `client/` and is reached from common code only through `client/ClientHooks`
  behind a client-side check.

## Code conventions

1. **English only:** all identifiers (classes, methods, variables, registry names, config keys) and all comments.
2. **Javadoc** on every public / protected class, method and field. Short `//` line comments inside method bodies
   (explaining a non-obvious step) are fine. No commented-out code, no template leftovers.
3. **Every player-facing text goes into the lang file** `src/main/resources/assets/zauberei/lang/en_us.json`
   — never hardcode display strings, use `Component.translatable(...)`. Key scheme:
   `item.zauberei.<name>`, `block.zauberei.<name>`, `itemGroup.zauberei.<name>`, `gui.zauberei.<name>`,
   `message.zauberei.<name>`, `tooltip.zauberei.<name>`, `zauberei.configuration.<key>`.
   New registered content without a lang entry is incomplete. Log messages are not player-facing.
4. Prefer vanilla / own-code solutions over mixins; write a mixin only when nothing else works.
5. Content (structures, tags, loot, recipes) is **datapack only**; the TOML config holds global options only.
6. **Never remove a persistent registry entry** (data component, attachment, item) that may be stored in
   existing worlds — keep it registered as legacy (`@Deprecated`, documented) or migrate the data first.
7. `@EventBusSubscriber` without `bus`: FML (4.0.41+) routes mod bus events automatically; `bus` is deprecated.

## Branches and versions

- `main` always targets the **newest supported Minecraft version** (currently 26.3, mod versions `2.x`).
- Every older Minecraft version keeps a long-lived branch named after it (`1.21.1` → mod versions `1.x`). It only
  receives bug fixes: branch `fix/<mc-version>-<short-name>` from that branch, PR back into it. A fix that applies
  to several versions is ported with `git cherry-pick` in its own PR per branch.
- Version branches are never deleted or force-pushed.

## Changelog

- `docs/wiki/Changelog.md` is mandatory. **Every change of `mod_version` adds a section for the new version**
  (`## <version> — Minecraft <mc version>`, newest first) listing everything that changed since the previous
  version, grouped into `Added` / `Changed` / `Fixed` / `Removed`.
- Write it for players and modpack makers (what changed, what they must do — e.g. renamed config files), not as a
  commit list.
- On a version branch, the changelog entry goes into that branch's copy; mirror it to `main` so the wiki shows all
  versions.

## Workflow

- Work is sliced into GitHub issues labeled `user story` (`US-XX`). **One story at a time.**
- **One branch and one PR per story:** `feature/us-XX-<short-name>`, created from up-to-date `main`.
  - PR body contains `Implements #N`, the story's final commit contains `Closes #N`.
  - Fix-up commits reference the story, e.g. `fix: … (US-XX)`.
  - Merge **only after the maintainer's OK**.
- Never bundle several stories in one branch or PR.
- No `wip:` commits on branches that get merged — squash them or commit only working states.
- Commit messages follow Conventional Commits (`feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `ci:` …).

### Cloud / remote sessions

Cloud sessions (claude.ai/code, auto-created `ccr-*` branches) follow the **same workflow, no exceptions**:

- If the session starts on a `ccr-*` branch, create `feature/us-XX-…` from `main` for each story instead of
  committing everything to the session branch.
- Finish one story completely (branch → PR → OK → merge) before starting the next from the updated `main`.
- **Do not discover APIs by pushing temporary CI debug steps.** Look up signatures locally: `javap` on the jars
  under `build/moddev/artifacts` / the Gradle cache, or extract the sources jar. Check the versions this project
  actually resolves (`./gradlew dependencies`) — other projects in the Gradle cache use different versions.

## Documentation

- **The wiki is mandatory:** `docs/wiki/*.md` (English) is the single source of truth, mirrored to the GitHub wiki
  by `.github/workflows/wiki.yml` on push to `main`. Every story PR updates the affected wiki pages (and
  `_Sidebar.md` for new pages). Never edit the GitHub wiki directly.
- The README stays short (features, install, development, links) and never duplicates wiki content.
- Every story adds its manual in-game checks to `docs/TESTING.md`:
  `## US-XX (#N): <title>`, the covering GameTests first, then a checkbox list.

## Tests

- GameTests belong in a `src/gametest` source set (never part of the jar) and run with
  `./gradlew runGameTestServer`. The source set does not exist yet — the first story that adds testable server
  logic sets it up. Until then, manual checks in `docs/TESTING.md` cover each change.
- A story PR needs a green build.

## Commands

```bash
./gradlew build      # jar -> build/libs/
./gradlew runClient
./gradlew runServer
./gradlew runData    # datagen -> src/generated/resources/
```
