package com.gilfort.zauberei.item.armorbonus;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ArmorSetDataRegistry {

    // Key format: "major:year:namespace:tagpath"
    // Example:   "naturalist:3:zauberei:magiccloth_armor"
    private static final Map<String, ArmorSetData> DATA_MAP = new HashMap<>();

    public static void clear() {
        DATA_MAP.clear();
    }

    public static void put(String major, int year, String tag, ArmorSetData data) {
        String key = makeKey(major, year, tag);
        DATA_MAP.put(key, data);
    }

    public static ArmorSetData getData(String major, int year, String tag) {
        String key = makeKey(major, year, tag);
        return DATA_MAP.get(key);
    }

    /**
     * Returns all registered tag strings for a given major/year.
     * Example: {"zauberei:magiccloth_armor", "arsnouveau:tier2armor"}
     */
    public static Set<String> getRegisteredTags(String major, int year) {
        String prefix = major + ":" + year + ":";
        return DATA_MAP.keySet().stream()
                .filter(k -> k.startsWith(prefix))
                .map(k -> k.substring(prefix.length()))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static Set<String> getMajors() {
        return Collections.unmodifiableSet(
                DATA_MAP.keySet().stream()
                        .map(key -> key.split(":", 2)[0])
                        .collect(Collectors.toSet())
        );
    }

    public static boolean isItemInAnyRegisteredTag(ItemStack stack) {
        for (String key : DATA_MAP.keySet()) {
            // Key format: "major:year:namespace:tagpath"
            // Tag ist alles ab dem 3. Doppelpunkt
            String[] parts = key.split(":", 3);
            if (parts.length < 3) continue;
            String tagString = parts[2]; // z.B. "zauberei:magiccloth_armor"
            try {
                ResourceLocation tagLoc = ResourceLocation.parse(tagString);
                TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagLoc);
                if (stack.is(tagKey)) return true;
            } catch (Exception e) {
                continue;
            }
        }
        return false;
    }

    private static String makeKey(String major, int year, String tag) {
        return major + ":" + year + ":" + tag;
    }
}
