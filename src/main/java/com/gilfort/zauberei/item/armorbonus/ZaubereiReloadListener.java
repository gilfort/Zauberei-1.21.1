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
        File yearDir = file.getParentFile();
        File majorDir = yearDir != null ? yearDir.getParentFile() : null;

        if (majorDir == null || yearDir == null) {
            Zauberei.LOGGER.error("[Zauberei] Ignoring invalid file structure: {}", file.getAbsolutePath());
            return;
        }

        String major = majorDir.getName();
        int year;
        try {
            year = Integer.parseInt(yearDir.getName());
        } catch (NumberFormatException e) {
            Zauberei.LOGGER.error("[Zauberei] Invalid year folder name in file path: {}", file.getAbsolutePath());
            return;
        }

        String fileName = file.getName().replace(".json", "");

        // Tag-only: require "__" in filename
        if (!fileName.contains("__")) {
            Zauberei.LOGGER.error(
                    "[Zauberei] Invalid filename '{}' – expected format: namespace__tagpath.json (e.g. 'zauberei__magiccloth_armor.json'). Skipping.",
                    file.getName()
            );
            return;
        }

        // Convert "namespace__tagpath" -> "namespace:tagpath"
        String tagString = fileName.replaceFirst("__", ":");

        ResourceLocation tagLoc = ResourceLocation.tryParse(tagString);
        if (tagLoc == null) {
            Zauberei.LOGGER.error("[Zauberei] '{}' is not a valid ResourceLocation (from file '{}'). Skipping.",
                    tagString, file.getName());
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonElement json = JsonParser.parseReader(reader);

            if (!json.isJsonObject()) {
                Zauberei.LOGGER.error("[Zauberei] Invalid JSON format in file: {}", file.getAbsolutePath());
                return;
            }

            ArmorSetData rawData = GSON.fromJson(json, ArmorSetData.class);
            if (rawData == null || rawData.getParts() == null || rawData.getParts().isEmpty()) {
                Zauberei.LOGGER.error("[Zauberei] No 'parts' found in file: {} (tag {}). Skipping.",
                        file.getAbsolutePath(), tagString);
                return;
            }

            ArmorSetData validatedData = validateData(rawData, file);
            ArmorSetDataRegistry.put(major.toLowerCase(), year, tagString, validatedData);

            // Safe log: only on load, not in tick
            Zauberei.LOGGER.info("[Zauberei] Loaded set definition: major={}, year={}, tag={}", major, year, tagString);
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
