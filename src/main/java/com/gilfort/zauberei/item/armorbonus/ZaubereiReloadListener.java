package com.gilfort.zauberei.item.armorbonus;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.armor.ArmorEffects;
import com.google.gson.*;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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

            ArmorSetData data = GSON.fromJson(json, ArmorSetData.class);
            ArmorSetDataRegistry.put(major, year, material, data);
            Zauberei.LOGGER.info("Loaded effects for {}; {}; {}", major, year, material);
        }
    }
}

//
//    // New Gson instance
//    private static final Gson GSON = new Gson();
//
//    // Konstruktor: Übergebe Pfad, ab dem gesucht werden soll, hier "materials"
//    public ZaubereiReloadListener() {
//        super(GSON, FMLPaths.CONFIGDIR + File.separator + "zauberei" + File.separator + "set_armor");
//    }
//
//    @Override
//    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
//        // objects ist eine Map<ResourceLocation, JsonElement>
//        // -> Enthält alle JSON-Dateien, die im Ordner "set_armor/" gefunden wurden
//
//        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
//            ResourceLocation loc = entry.getKey();
//            JsonObject json = entry.getValue().getAsJsonObject();
//            Zauberei.LOGGER.info("Gefundene JSON: {} Inhalt: {}", loc, json);
//
//            String path = loc.getPath();
//
//            String[] parts = path.split("/");
//            if (parts.length < 3) {
//                Zauberei.LOGGER.error("Invalid path: {}", path);
//                continue;
//            }
//
//            String major = parts[0];
//            int year;
//            try {
//                year = Integer.parseInt(parts[1]);
//            } catch (NumberFormatException e) {
//                Zauberei.LOGGER.error("Invalid year: {} - skipping file {}", parts[1], path);
//                continue;
//            }
//
//            String armorMaterial = parts[2];
//
//            String fileName = parts[2];
//            if (fileName.endsWith(".json")) {
//                fileName = fileName.substring(0, fileName.length() - 5);
//            }
//
//            ArmorSetData data;
//            try {
//                data = GSON.fromJson(json, ArmorSetData.class);
//            } catch (JsonSyntaxException e) {
//                Zauberei.LOGGER.error("Invalid JSON: {} - skipping file {}", e.getMessage(), path);
//                continue;
//            }
//
//
//            Zauberei.LOGGER.info("Parsed major={}, year={}, armor={}", major, year, armorMaterial);
//
//            ArmorSetDataRegistry.put(major, year, armorMaterial, data);
//        }
//
//    }
//
//}