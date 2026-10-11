# Test plan

There are no GameTests yet. Manual tests cover each change: run `./gradlew runClient` (single player) or
`./gradlew runServer` plus a client, and tick off the checklist of the change.

---

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
