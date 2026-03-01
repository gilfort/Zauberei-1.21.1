package com.gilfort.zauberei.item.armorbonus;

import com.gilfort.zauberei.Zauberei;
import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Loads all JSON files from config/zauberei/set_armor/.
 *
 * Folder structure:
 *   config/zauberei/set_armor/{major}/{year}/{namespace}__{tagpath}.json
 *
 * Examples:
 *   config/zauberei/set_armor/naturalist/3/zauberei__magiccloth_armor.json
 *   config/zauberei/set_armor/naturalist/3/arsnouveau__tier2armor.json
 *
 * Filename is interpreted as an item tag:
 *   "zauberei__magiccloth_armor" -> "zauberei:magiccloth_armor"
 *
 * Tag-only (no armor material compatibility).
 */
public class ZaubereiReloadListener {

    private static final File BASE_DIR = new File(
            FMLPaths.CONFIGDIR.get().toFile(),
            "zauberei" + File.separator + "set_armor"
    );

    private static final Gson GSON = new Gson();

    public static void loadAllEffects() {
        if (!BASE_DIR.exists()) {
            BASE_DIR.mkdirs();
            Zauberei.LOGGER.info("[Zauberei] Created config directory: {}", BASE_DIR.getAbsolutePath());
            return;
        }

        ArmorSetDataRegistry.clear();
        walkDirectory(BASE_DIR);
    }

    private static void walkDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                walkDirectory(file);
            } else if (file.getName().endsWith(".json")) {
                try {
                    handleJsonFile(file);
                } catch (Exception e) {
                    Zauberei.LOGGER.error("[Zauberei] Error loading file {}: {}",
                            file.getAbsolutePath(), e.getMessage());
                }
            }
        }
    }

    private static void handleJsonFile(File file) throws IOException {
        // ── Determine major & year from relative path depth ──────────────
        // 3 segments: {major}/{year}/file.json          → standard
        // 2 segments: all_majors_all_years/file.json    → universal wildcard
        java.nio.file.Path relativePath = BASE_DIR.toPath().relativize(file.toPath());
        int segmentCount = relativePath.getNameCount(); // includes filename

        String major;
        int year;

        if (segmentCount == 3) {
            // Standard:    {major}/{year}/file.json
            // Or:          all_majors/{year}/file.json
            String majorName = relativePath.getName(0).toString();
            String yearName  = relativePath.getName(1).toString();

            if ("all_majors".equalsIgnoreCase(majorName)) {
                major = ArmorSetDataRegistry.WILDCARD_MAJOR;
            } else {
                major = majorName;
            }

            try {
                year = Integer.parseInt(yearName);
            } catch (NumberFormatException e) {
                Zauberei.LOGGER.error(
                        "[Zauberei] Invalid year folder '{}' in path: {}. Skipping.",
                        yearName, file.getAbsolutePath());
                return;
            }

        } else if (segmentCount == 2) {
            // Universal:   all_majors_all_years/file.json
            String dirName = relativePath.getName(0).toString();
            if (!"all_majors_all_years".equalsIgnoreCase(dirName)) {
                Zauberei.LOGGER.error(
                        "[Zauberei] Unexpected 2-level path: {}. " +
                                "Expected 'all_majors_all_years/' or '{major}/{year}/'. Skipping.",
                        file.getAbsolutePath());
                return;
            }
            major = ArmorSetDataRegistry.WILDCARD_MAJOR;
            year  = ArmorSetDataRegistry.WILDCARD_YEAR;

        } else {
            Zauberei.LOGGER.error(
                    "[Zauberei] Invalid directory depth ({} segments) for file: {}. " +
                            "Expected: {major}/{year}/file.json or all_majors_all_years/file.json",
                    segmentCount, file.getAbsolutePath());
            return;
        }

        // ── Parse filename as tag ────────────────────────────────────────
        String fileName = file.getName().replace(".json", "");

        if (!fileName.contains("__")) {
            Zauberei.LOGGER.error(
                    "[Zauberei] Invalid filename '{}' – expected format: " +
                            "namespace__tagpath.json (e.g. 'zauberei__magiccloth_armor.json'). Skipping.",
                    file.getName());
            return;
        }

        String tagString = fileName.replaceFirst("__", ":");
        ResourceLocation tagLoc = ResourceLocation.tryParse(tagString);
        if (tagLoc == null) {
            Zauberei.LOGGER.error(
                    "[Zauberei] '{}' is not a valid ResourceLocation (from file '{}'). Skipping.",
                    tagString, file.getName());
            return;
        }

        // ── Parse & validate JSON ────────────────────────────────────────
        try (FileReader reader = new FileReader(file)) {
            JsonElement json = JsonParser.parseReader(reader);

            if (!json.isJsonObject()) {
                Zauberei.LOGGER.error("[Zauberei] Invalid JSON format in file: {}",
                        file.getAbsolutePath());
                return;
            }

            ArmorSetData rawData = GSON.fromJson(json, ArmorSetData.class);
            if (rawData == null || rawData.getParts() == null || rawData.getParts().isEmpty()) {
                Zauberei.LOGGER.error(
                        "[Zauberei] No 'parts' found in file: {} (tag {}). Skipping.",
                        file.getAbsolutePath(), tagString);
                return;
            }

            ArmorSetData validatedData = validateData(rawData, file);
            ArmorSetDataRegistry.put(major.toLowerCase(), year, tagString, validatedData);

            // ── Descriptive log message ──────────────────────────────────
            String scope;
            if (ArmorSetDataRegistry.WILDCARD_MAJOR.equals(major)
                    && year == ArmorSetDataRegistry.WILDCARD_YEAR) {
                scope = "ALL majors, ALL years";
            } else if (ArmorSetDataRegistry.WILDCARD_MAJOR.equals(major)) {
                scope = "ALL majors, year=" + year;
            } else {
                scope = "major=" + major + ", year=" + year;
            }
            Zauberei.LOGGER.info("[Zauberei] Loaded set definition: {} → tag={}",
                    scope, tagString);
        }
    }


    private static ArmorSetData validateData(ArmorSetData data, File file) {
        data.getParts().forEach((partName, partData) -> {

            // Validate effects
            if (partData.getEffects() != null) {
                var it = partData.getEffects().iterator();
                while (it.hasNext()) {
                    ArmorSetData.EffectData ed = it.next();
                    ResourceLocation id = tryMakeResourceLocation(ed.getEffect());
                    MobEffect mob = id == null ? null : BuiltInRegistries.MOB_EFFECT.get(id);
                    if (mob == null) {
                        Zauberei.LOGGER.error("[Zauberei] Unknown effect '{}' in {} – skipped", ed.getEffect(), file);
                        it.remove();
                        continue;
                    }

                    // Clamp invalid roman level keys (existing behavior)
                    int lvl = ed.getAmplifier() + 1;
                    String key = "enchantment.level." + lvl;
                    if (!Language.getInstance().has(key)) {
                        int max = 5;
                        Zauberei.LOGGER.warn("[Zauberei] Level {} for effect '{}' in {} invalid, clamped to {}",
                                lvl, ed.getEffect(), file, max);
                        ed.setAmplifier(max - 1);
                    }
                }
            }

            // Validate attributes
            if (partData.getAttributes() != null) {
                var attrIt = partData.getAttributes().entrySet().iterator();
                while (attrIt.hasNext()) {
                    var entry = attrIt.next();
                    ResourceLocation aid = tryMakeResourceLocation(entry.getKey());
                    Attribute attr = aid == null ? null : BuiltInRegistries.ATTRIBUTE.get(aid);
                    if (attr == null) {
                        Zauberei.LOGGER.error("[Zauberei] Unknown attribute '{}' in {} – removed",
                                entry.getKey(), file);
                        attrIt.remove();
                    }
                }
            }
        });

        return data;
    }

    private static ResourceLocation tryMakeResourceLocation(String raw) {
        String s = raw.contains(":") ? raw : "minecraft:" + raw;
        try {
            return ResourceLocation.tryParse(s);
        } catch (Exception e) {
            Zauberei.LOGGER.error("[Zauberei] Invalid ResourceLocation '{}' – {}", raw, e.getMessage());
            return null;
        }
    }
}
