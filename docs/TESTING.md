# Test plan

There are no GameTests yet. Manual tests cover each change: run `./gradlew runClient` (single player) or
`./gradlew runServer` plus a client, and tick off the checklist of the change.

---

## Upgrade to Minecraft 26.3 (branch `feature/upgrade-mc-26.3`)

GameTests: none. Automated so far: `runData` and a dedicated GameTest server start without errors.

- [x] `runClient` starts; Zauberei tab shows all items with correct icons (no missing textures).
- [x] Wear each Magiccloth piece (normal and `_alt`): GeckoLib model renders, follows arms / legs / head
      while walking, sneaking and swimming.
- [x] Robe: hood layer follows the head; pants: waistband layer follows the body.
- [ ] Armor values (helmet 1, chest 3, legs 2, boots 1), durability, repairing with leather, enchanting.
- [x] Introduction Letter: screen shows background, text (with player name) and button; tearing gives the items.
- [ ] `letter_text` / `letter_items` in `config/zauberei-local.toml` work.
- [ ] Dedicated server (`runServer` + client): letter and armor work; no client class errors in the server log.
- [ ] `spawn_witchwood_school = true`, new world: school appears above spawn; advancement fires only there.
- [ ] Old 1.21.1 world (1.0.0) upgraded to 26.3: Magiccloth and other armor still in the inventory.

## Remove set effects and command system (branch `remove-set_effects`)

GameTests: none.

- [ ] `runClient` and `runServer` start without errors; the log shows no unknown data component or tag errors.
- [ ] Without Ars Nouveau: Magiccloth armor is enchantable (helmet, chestplate, leggings, boots) and
      `#zauberei:magiccloth_armor` loads.
- [ ] With Ars Nouveau: the `_perks` pieces appear in the creative tab, perks can be applied and tick.
- [ ] Old world (created with 0.3.x, armor worn at least 3 s): load it with this version — all armor
      (also from other mods) is still there.
- [ ] Introduction Letter: the text shows the player name; **Tear Letter** consumes the letter and gives the
      `letter_items`; full inventory drops the rest.
- [ ] Introduction Letter with `letter_text` set in the config: the override is shown with `%player%` replaced.
- [ ] Dedicated server: using the letter does not crash the server.
- [ ] `spawn_witchwood_school = true`, new world: the school appears above spawn.
- [ ] `enable_magical_armor = false`: items hidden from the creative tab and JEI/EMI.
- [ ] Armor renders correctly (normal and `_alt` style, robe parts follow the legs, hood follows the head).
