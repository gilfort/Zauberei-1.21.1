package com.gilfort.zauberei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common (global) configuration of the mod, stored in {@code config/zauberei-common.toml}.
 * Display names of the options live in the lang file under {@code zauberei.configuration.<key>}.
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** Item ids handed out when a player tears the Introduction Letter. */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> LETTER_ITEMS;

    /**
     * Optional override of the Introduction Letter text. When empty, the translated text from the
     * lang file ({@code message.zauberei.introduction_letter.text}) is shown.
     */
    public static final ModConfigSpec.ConfigValue<String> LETTER_TEXT;

    /** Whether the Witchwood School is placed above the world spawn when a new world is created. */
    public static final ModConfigSpec.ConfigValue<Boolean> SPAWN_WITCHWOOD_SCHOOL;

    /**
     * When false, all Magiccloth Armor items and the Introduction Letter are hidden from the creative tab
     * and from item viewers like JEI/EMI. Items stay registered so existing worlds keep working.
     * Requires a game restart to take effect.
     */
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_MAGICAL_ARMOR;

    static {
        LETTER_ITEMS = BUILDER
                .comment("Items the player receives when tearing the Introduction Letter.")
                .translation("zauberei.configuration.letter_items")
                .defineListAllowEmpty(
                        List.of("letter_items"),
                        List.of(
                                "zauberei:magiccloth_helmet",
                                "zauberei:magiccloth_chestplate",
                                "zauberei:magiccloth_leggings",
                                "zauberei:magiccloth_boots"
                        ),
                        () -> "zauberei:magiccloth",
                        o -> o instanceof String s && ResourceLocation.tryParse(s) != null
                );

        LETTER_TEXT = BUILDER
                .comment("Optional override of the text shown in the Introduction Letter.",
                        "Leave empty to use the translated default text.",
                        "Use %player% as a placeholder for the player name.")
                .translation("zauberei.configuration.letter_text")
                .define("letter_text", "");

        SPAWN_WITCHWOOD_SCHOOL = BUILDER
                .comment("Whether the Witchwood School is placed above the world spawn (around 0 244 0)",
                        "when a new world is created.")
                .translation("zauberei.configuration.spawn_witchwood_school")
                .define("spawn_witchwood_school", false);

        ENABLE_MAGICAL_ARMOR = BUILDER
                .comment("Whether the Magiccloth Armor items and the Introduction Letter are enabled.",
                        "When false, these items are hidden from the creative tab and item viewers (JEI/EMI).",
                        "Items remain registered to prevent crashes in existing worlds.",
                        "Requires a game restart to take effect.")
                .translation("zauberei.configuration.enable_magical_armor")
                .define("enable_magical_armor", true);
    }

    static final ModConfigSpec SPEC = BUILDER.build();

    /**
     * Resolves {@link #LETTER_ITEMS} into item stacks. Unknown ids are skipped with a warning.
     *
     * @return one stack per configured, existing item
     */
    public static List<ItemStack> getLetterItems() {
        List<ItemStack> stacks = new ArrayList<>();
        for (String itemName : LETTER_ITEMS.get()) {
            ResourceLocation id = ResourceLocation.tryParse(itemName);
            if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
                Zauberei.LOGGER.warn("[Zauberei] Unknown item '{}' in letter_items, skipped.", itemName);
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(id);
            stacks.add(new ItemStack(item));
        }
        return stacks;
    }
}
