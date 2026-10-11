# Configuration

File: `config/zauberei-common.toml`

| Key | Default | Description |
|---|---|---|
| `letter_items` | the four Magiccloth pieces | Item ids the player receives when tearing the [Introduction Letter](Introduction-Letter). Unknown ids are skipped with a warning in the log. |
| `letter_text` | empty | Optional override of the letter text; `%player%` is replaced with the player name. Empty = translated default text. |
| `spawn_witchwood_school` | `false` | Places the [Witchwood School](Witchwood-School) when a new world is created. |
| `enable_magical_armor` | `true` | When `false`, the Magiccloth items and the letter are hidden from the creative tab and JEI/EMI. The items stay registered. Requires a restart. |
