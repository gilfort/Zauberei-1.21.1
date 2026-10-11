# Introduction Letter

`zauberei:introductionletter` welcomes a new student to the Witchwood Academy.

1. Use (right-click) the letter to read it.
2. Click **Tear Letter**. The letter is consumed and the player receives the items listed in `letter_items`;
   items that do not fit into the inventory are dropped.

The server checks that the player really holds a letter, so the reward cannot be triggered without one.

## Customizing

- **Items:** `letter_items` in the config (default: the four Magiccloth pieces).
- **Text:** the default text comes from the lang key `message.zauberei.introduction_letter.text` (`%1$s` = player
  name) and can be translated or changed with a resource pack. For a quick server-wide change, set
  `letter_text` in the config (`%player%` = player name); it overrides the translated text.

See [Configuration](Configuration).
