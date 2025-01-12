package com.gilfort.zauberei.item.armorbonus;

import com.gilfort.zauberei.Zauberei;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

// Ein Listener, der alle JSON-Dateien in data/<modid>/materials/ einliest
public class ZaubereiReloadListener extends SimpleJsonResourceReloadListener {


    // New Gson instance
    private static final Gson GSON = new Gson();

//    private final Map<String, ArmorSetData> armorSetDataMap = new HashMap<>();

    // Konstruktor: Übergebe Pfad, ab dem gesucht werden soll, hier "materials"
    public ZaubereiReloadListener() {
        super(GSON, "set_armor");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        // objects ist eine Map<ResourceLocation, JsonElement>
        // -> Enthält alle JSON-Dateien, die im Ordner "set_armor/" gefunden wurden

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            ResourceLocation loc = entry.getKey();
            JsonObject json = entry.getValue().getAsJsonObject();
            Zauberei.LOGGER.info("Gefundene JSON: {} Inhalt: {}", loc, json);

            String path = loc.getPath();
//            String subPath = path.substring("set_armor/".length());

            String[] parts = path.split("/");
            if (parts.length < 3) {
                Zauberei.LOGGER.error("Invalid path: {}", path);
                continue;
            }

            String major = parts[0];
            int year;
            try {
                year = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                Zauberei.LOGGER.error("Invalid year: {} - skipping file {}", parts[1], path);
                continue;
            }

            String armorMaterial = parts[2];

            String fileName = parts[2];
            if (fileName.endsWith(".json")) {
                fileName = fileName.substring(0, fileName.length() - 5);
            }

            ArmorSetData data;
            try {
                data = GSON.fromJson(json, ArmorSetData.class);
            } catch (JsonSyntaxException e) {
                Zauberei.LOGGER.error("Invalid JSON: {} - skipping file {}", e.getMessage(), path);
                continue;
            }


            Zauberei.LOGGER.info("Parsed major={}, year={}, armor={}", major, year, armorMaterial);

            ArmorSetDataRegistry.put(major, year, armorMaterial, data);
        }

    }

}