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


// Ein Listener, der alle JSON-Dateien in config/zauberei/set_effects einliest
public class ZaubereiReloadListener {


    private static final File BASE_DIR = new File(FMLPaths.CONFIGDIR.get().toFile(), "zauberei" + File.separator + "set_armor");
    private static final Gson GSON = new Gson();

    public static void loadAllEffects() {
        if (!BASE_DIR.exists()) {
            BASE_DIR.mkdirs();
            Zauberei.LOGGER.info("Created config directory: {}", BASE_DIR.getAbsolutePath());
            return;
        }

        ArmorSetDataRegistry.clear();

        walkDirectory(BASE_DIR);
    }

    private static void walkDirectory(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                walkDirectory(file);
            } else if (file.getName().endsWith(".json")) {
                try {
                    handleJsonFile(file);
                } catch (Exception e) {
                    Zauberei.LOGGER.error("Error loading file {}: {}", file.getAbsolutePath(), e.getMessage());
                }
            }
        }
    }

    private static void handleJsonFile(File file) throws IOException {
        File yearDir = file.getParentFile();
        File majorDir = yearDir.getParentFile();

        if (majorDir == null || yearDir == null) {
            Zauberei.LOGGER.error("Ignoring invalid file structure: {}", file.getAbsolutePath());
            return;
        }

        String material = file.getName().replace(".json", "");
        String major = majorDir.getName();
        int year;

        try {
            year = Integer.parseInt(yearDir.getName());
        } catch (NumberFormatException e) {
            Zauberei.LOGGER.error("Invalid year folder name in file path: {}", file.getAbsolutePath());
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonElement json = JsonParser.parseReader(reader);

            if (!json.isJsonObject()) {
                Zauberei.LOGGER.error("Invalid JSON format in file: {}", file.getAbsolutePath());
                return;
            }

            ArmorSetData rawdata = GSON.fromJson(json, ArmorSetData.class);
            ArmorSetData validatedData = validateData(rawdata, file);
            ArmorSetDataRegistry.put(major, year, material, validatedData);
            Zauberei.LOGGER.info("Loaded effects for {}; {}; {}", major, year, material);
        }
    }

    private static ArmorSetData validateData(ArmorSetData data, File file) {
        data.getParts().forEach((partName, partData) -> {
            // 2.1 Effekte validieren
            if (partData.getEffects() != null) {
                var it = partData.getEffects().iterator();
                while (it.hasNext()) {
                    ArmorSetData.EffectData ed = it.next();
                    // Ressource lokalisieren
                    ResourceLocation id = tryMakeResourceLocation(ed.getEffect());
                    MobEffect mob = id == null ? null : BuiltInRegistries.MOB_EFFECT.get(id);
                    if (mob == null) {
                        Zauberei.LOGGER.error("Unbekannter Effekt '{}' in {} – übersprungen", ed.getEffect(), file);
                        it.remove();
                        continue;
                    }
                    // Level prüfen und clampen
                    int lvl = ed.getAmplifier() + 1;
                    String key = "enchantment.level." + lvl;
                    if (!Language.getInstance().has(key)) {
                        int max = 5; // z.B. Max-Level
                        Zauberei.LOGGER.warn("Level {} für Effekt '{}' in {} ungültig, clamped auf {}", lvl, ed.getEffect(), file, max);
                        ed.setAmplifier(max - 1);
                    }
                }
            }

            // 2.2 Attribute validieren
            if (partData.getAttributes() != null) {
                var attrIt = partData.getAttributes().entrySet().iterator();
                while (attrIt.hasNext()) {
                    var entry = attrIt.next();
                    ResourceLocation aid = tryMakeResourceLocation(entry.getKey());
                    Attribute attr = aid == null ? null : BuiltInRegistries.ATTRIBUTE.get(aid);
                    if (attr == null) {
                        Zauberei.LOGGER.error("Unbekanntes Attribut '{}' in {} – entfernt", entry.getKey(), file);
                        attrIt.remove();
                    }
                    // Bei Bedarf hier noch Wert-Clamping implementieren
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
            Zauberei.LOGGER.error("Ungültige ResourceLocation '{}' – {}", raw, e.getMessage());
            return null;
        }
    }


}