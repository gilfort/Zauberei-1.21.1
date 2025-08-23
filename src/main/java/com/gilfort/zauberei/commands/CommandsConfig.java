package com.gilfort.zauberei.commands;

import com.gilfort.zauberei.Zauberei;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Loads and validates the JSON configuration for the gateway command system.
 *
 * <pre>
 * {
 *   "aliases": {"alias": ["namespace:path", ...]},
 *   "tagRules": [{"anyOf": ["alias"], "allOf": ["alias"], "noneOf": ["alias"], "tag": "name"}],
 *   "tierRules": [{"anyOf": [...], "tier": 1}],
 *   "tierWindows": {"1": [minMinutes, maxMinutes], ...},
 *   "commands": [{
 *       "when": {"anyTags": ["tag"], "allTags": ["tag"], "minTier": 1},
 *       "pool": ["command without leading slash"],
 *       "position": "onPosition|nearby|away" // optional, defaults to nearby
 *   }]
 * }
 * </pre>
 */
public class CommandsConfig {
    public Map<String, List<ResourceLocation>> aliases = new HashMap<>();
    public List<TagRule> tagRules = new ArrayList<>();
    public List<TierRule> tierRules = new ArrayList<>();
    public Map<Integer, int[]> tierWindows = new HashMap<>();
    public List<CommandEntry> commands = new ArrayList<>();

    public static CommandsConfig load() {
        Path path = FMLPaths.CONFIGDIR.get().resolve("zauberei").resolve("commands.json5");
        File file = path.toFile();
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            Zauberei.LOGGER.warn("Commands config not found: {}", file.getAbsolutePath());
            return new CommandsConfig();
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ResourceLocation.class, (JsonDeserializer<ResourceLocation>)
                        (json, type, ctx) -> ResourceLocation.parse(json.getAsString()))
                .create();
        try (FileReader fr = new FileReader(file)) {
            JsonReader reader = new JsonReader(fr);
            reader.setLenient(true);
            JsonElement root = JsonParser.parseReader(reader);
            CommandsConfig cfg = gson.fromJson(root, CommandsConfig.class);
            cfg.postProcess();
            Zauberei.LOGGER.info("Loaded commands config: {} aliases, {} tagRules, {} tierRules, {} command entries",
                    cfg.aliases.size(), cfg.tagRules.size(), cfg.tierRules.size(), cfg.commands.size());
            return cfg;
        } catch (IOException | JsonParseException e) {
            Zauberei.LOGGER.error("Failed reading commands config {}: {}", file.getAbsolutePath(), e.getMessage());
            return new CommandsConfig();
        }
    }

    private void postProcess() {
        if (tierWindows.isEmpty()) {
            tierWindows.put(1, new int[]{1, 2});
        }
    }

    public static class TagRule {
        public List<String> anyOf;
        public List<String> allOf;
        public List<String> noneOf;
        public String tag;
    }

    public static class TierRule {
        public List<String> anyOf;
        public List<String> allOf;
        public List<String> noneOf;
        public int tier = 1;
    }

    public static class CommandEntry {
        public When when = new When();
        public List<String> pool = new ArrayList<>();
        public String position = "nearby";
    }

    public static class When {
        public List<String> anyTags;
        public List<String> allTags;
        public int minTier = 1;
    }
}
