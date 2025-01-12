package com.gilfort.zauberei.item;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.helpers.PlayerDataHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


public class ArmorEffects {

    private static final Map<String, JsonObject> DATA = new HashMap<>();

//    public static void register(IEventBus eventBus) {
//        NeoForge.EVENT_BUS.addListener(ArmorEffects::onPlayerTick);
//    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            String major = PlayerDataHelper.getMajor(player);
            int year = PlayerDataHelper.getYear(player);
            applySetBasedEffects(player, major);

        }
    }


    private static void applySetBasedEffects(Player player, String major) {
        Map<String, Integer> armorSetCounts = new HashMap<>();

        for (ItemStack stack : player.getArmorSlots()) {
            if (stack.getItem() instanceof ArmorItem armorItem) {
                // Falls `armorItem.getMaterial()` ein registriertes Material ist
                ResourceLocation materialResource = BuiltInRegistries.ARMOR_MATERIAL.getKey(armorItem.getMaterial().value());
                if (materialResource != null) {
                    String materialName = materialResource.getPath(); // z. B. "iron"

                    for (Map.Entry<String, JsonObject> entry : DATA.entrySet()) {
                        String armorSet = entry.getKey();
                        if (materialName.equalsIgnoreCase(armorSet)) {
                            armorSetCounts.put(armorSet, armorSetCounts.getOrDefault(armorSet, 0) + 1);
                            break;
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, Integer> entry : armorSetCounts.entrySet()) {
            String armorSet = entry.getKey();
            int piecesCount = entry.getValue();
            if (piecesCount > 0) {
                JsonObject setEffects = DATA.get(armorSet).getAsJsonObject(Integer.toString(piecesCount));
                applyEffectsAndAttributes(player, setEffects);
            }
        }
    }


    public static void loadArmorEffects(ResourceManager resourceManager) {
        try {
            Map<ResourceLocation, Resource> resources = resourceManager.listResources("majors", location -> location.getPath().endsWith(".json"));
            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                ResourceLocation resourceLocation = entry.getKey();
                Resource resource = entry.getValue();

                try (var reader = new InputStreamReader(resource.open())) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    // Extrahiere den Rüstungsmaterialtyp aus dem Dateinamen
                    String key = resourceLocation.getPath().substring(resourceLocation.getPath().lastIndexOf('/') + 1).replace(".json", "");
                    DATA.put(key, json);
                } catch (Exception e) {
                    Zauberei.LOGGER.error("Failed to load armor effects from {}", resourceLocation, e);
                }
            }
        } catch (Exception e) {
            Zauberei.LOGGER.error("Failed to load armor effects", e);
        }
    }

    //Save data in map
    public static JsonObject getData(String key) {
        return DATA.get(key);
    }


    private static void applyEffectsAndAttributes(Player player, JsonObject setEffects) {
        applyMobEffects(player, setEffects.getAsJsonArray("Effects"));
        applyAttributes(player, setEffects.getAsJsonObject("Attributes"));
    }

    private static void applyMobEffects(Player player, JsonArray effects) {
        for (JsonElement element : effects) {
            JsonObject effectData = element.getAsJsonObject();
            int amplifier = effectData.get("Amplifier").getAsInt();
            String effectName = effectData.get("Effect").getAsString();

            Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(effectName)).orElse(null);
            if (effect != null) {
                MobEffectInstance effectInstance = new MobEffectInstance(effect, 200, amplifier, false, false, false);
                player.addEffect(effectInstance);
            } else {
                Zauberei.LOGGER.warn("Unknown effect: {}", effectName);
            }
        }
    }

    private static void applyAttributes(Player player, JsonObject attributes) {
        for (Map.Entry<String, JsonElement> entry : attributes.entrySet()) {
            String AttributeName = entry.getKey();
            double value = entry.getValue().getAsDouble();

            Holder<Attribute> attribute = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(AttributeName)).orElse(null);
            if (attribute != null && player.getAttribute(attribute) != null) {
                AttributeInstance instance = player.getAttribute(attribute);
                ResourceLocation modifierLocation = ResourceLocation.parse(AttributeName);
                AttributeModifier oldModifier = instance.getModifier(modifierLocation);
                if (oldModifier != null) {
                    instance.removeModifier(oldModifier); // Remove old modifier
                }
                instance.addPermanentModifier(new AttributeModifier(modifierLocation, value, AttributeModifier.Operation.ADD_VALUE));
            } else {
                Zauberei.LOGGER.warn("Unknown attribute: {}", AttributeName);
            }
        }
    }
}