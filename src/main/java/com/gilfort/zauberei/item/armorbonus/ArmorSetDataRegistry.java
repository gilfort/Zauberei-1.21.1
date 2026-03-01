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

    // ─── Sentinel values for wildcards ───────────────────────────────────
    public static final String WILDCARD_MAJOR = "*";
    public static final int    WILDCARD_YEAR  = -1;

    /**
     * Returns set data for the given major/year/tag with priority fallback:
     * <ol>
     *   <li>Exact match: {@code major + year + tag}</li>
     *   <li>Wildcard major: {@code all_majors + year + tag}</li>
     *   <li>Wildcard both: {@code all_majors + all_years + tag}</li>
     * </ol>
     */
    public static ArmorSetData getData(String major, int year, String tag) {
        // Priority 1: exact match (e.g. naturalist/3/magiccloth_armor)
        ArmorSetData data = DATA_MAP.get(makeKey(major, year, tag));
        if (data != null) return data;

        // Priority 2: all_majors for this year (e.g. all_majors/3/magiccloth_armor)
        data = DATA_MAP.get(makeKey(WILDCARD_MAJOR, year, tag));
        if (data != null) return data;

        // Priority 3: all_majors_all_years (e.g. all_majors_all_years/magiccloth_armor)
        return DATA_MAP.get(makeKey(WILDCARD_MAJOR, WILDCARD_YEAR, tag));
    }

    /**
     * Returns all tag strings relevant for a given major/year,
     * including wildcard entries. Specific entries take priority
     * (the same tag won't appear twice).
     */
    public static Set<String> getRegisteredTags(String major, int year) {
        // Use a map to track priority: tag → already found at higher priority?
        Map<String, Boolean> tagMap = new HashMap<>();

        // Priority 1: exact match
        String exactPrefix = major + ":" + year + ":";
        // Priority 2: all_majors for this year
        String wildcardMajorPrefix = WILDCARD_MAJOR + ":" + year + ":";
        // Priority 3: all_majors_all_years
        String wildcardBothPrefix = WILDCARD_MAJOR + ":" + WILDCARD_YEAR + ":";

        for (String key : DATA_MAP.keySet()) {
            String tag = null;
            if (key.startsWith(exactPrefix)) {
                tag = key.substring(exactPrefix.length());
            } else if (key.startsWith(wildcardMajorPrefix)) {
                tag = key.substring(wildcardMajorPrefix.length());
            } else if (key.startsWith(wildcardBothPrefix)) {
                tag = key.substring(wildcardBothPrefix.length());
            }

            if (tag != null) {
                tagMap.putIfAbsent(tag, true);
            }
        }

        return Collections.unmodifiableSet(tagMap.keySet());
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
    /**
     * Returns all real major names registered in the data map.
     * Excludes the wildcard sentinel ({@code "*"}) so that command
     * auto-completion only suggests actual majors.
     */
    public static Set<String> getMajors() {
        return Collections.unmodifiableSet(
                DATA_MAP.keySet().stream()
                        .map(key -> key.split(":", 2)[0])
                        .filter(m -> !WILDCARD_MAJOR.equals(m))
                        .collect(Collectors.toSet())
        );
    }



    private static String makeKey(String major, int year, String tag) {
        return major + ":" + year + ":" + tag;
    }
}
