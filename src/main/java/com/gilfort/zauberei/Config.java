package com.gilfort.zauberei;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.

// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends String>> LETTER_ITEMS;
    public static final ModConfigSpec.ConfigValue<String> LETTER_TEXT;
    public static final ModConfigSpec.ConfigValue<Boolean> SPAWN_WITCHWOOD_SCHOOL;

    static {
        LETTER_ITEMS = BUILDER
                .comment("\nItems, the player can get when using the Introduction Letter\n")
                .defineListAllowEmpty(
                        List.of("letter_items"),
                        List.of(
                                "zauberei:magiccloth_helmet",
                                "zauberei:magiccloth_chestplate",
                                "zauberei:magiccloth_leggings",
                                "zauberei:magiccloth_boots"
                        ),
                        o -> o instanceof String
                );

        LETTER_TEXT = BUILDER
                .comment("\nText shown in the introduction letter item. Use %player% as a placeholder for the player name.\n")
                .define("letter_text",
                     """
                     Dear %player%,
                     
                     Congratulations!
                     Our detection system - \"Agnosce\" - has recognized your magical talent.
                     We are pleased to inform you that you have been accepted into our school as a scholarship student.
                     From today on you are part of the Witchwood Academy.
                     
                     To receive your first-year supplies, please tear this letter apart.
                     Welcome to a world where magic comes to life.
                     We look forward to guiding you on your journey and helping you master your new skills.
                     
                     Yours sincerely,
                     Professor McDumblesnape
                     Headmaster of Witchwood Academy
                     """);

        SPAWN_WITCHWOOD_SCHOOL = BUILDER
                .comment("\nWhether the Witchwood School should be spawned in the world. (at 0 250 0)\n")
                .define("spawn_witchwood_school", false);

    }

    static final ModConfigSpec SPEC = BUILDER.build();


    public static List<ItemStack> getLetterItems() {

        return LETTER_ITEMS.get().stream()
                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .filter(item -> item != Items.AIR)
                .map(ItemStack::new)
                .toList();
    }

    public static String getLetterText(String playerName) {
        return LETTER_TEXT.get().replace("%player%", playerName).replace("\r", "");
    }


}