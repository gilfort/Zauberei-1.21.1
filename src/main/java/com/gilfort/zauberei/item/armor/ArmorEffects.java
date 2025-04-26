package com.gilfort.zauberei.item.armor;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.helpers.PlayerDataHelper;
import com.gilfort.zauberei.item.armorbonus.ArmorSetData;
import com.gilfort.zauberei.item.armorbonus.ArmorSetDataRegistry;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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

import java.util.*;

import static com.gilfort.zauberei.component.ComponentRegistry.MAJOR;
import static com.gilfort.zauberei.component.ComponentRegistry.YEAR;


public class ArmorEffects {

    private static final Map<String, JsonObject> DATA = new HashMap<>();

    public static void register(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(ArmorEffects::onPlayerTick);
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
            player = (ServerPlayer) event.getEntity();
            String major = PlayerDataHelper.getMajor(player);
            int year = PlayerDataHelper.getYear(player);
            applySetBasedEffects(player, major);

            for (ItemStack stack : player.getArmorSlots()) {
                if (stack.getItem() instanceof ArmorItem) {
                    stack.set(MAJOR.value(), major);
                    stack.set(YEAR.value(),  year);
                }
            }

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
                    armorSetCounts.merge(materialName, 1, Integer::sum);
                }
            }
        }

        applyEffectsFromJson(player, armorSetCounts);
    }

    private static void applyEffectsFromJson(Player player, Map<String, Integer> armorCounts) {
        int year = PlayerDataHelper.getYear((ServerPlayer) player);
        String major = PlayerDataHelper.getMajor((ServerPlayer) player);

        if (armorCounts.isEmpty()) {
            removeOldZaubereiModifiers(player);
        }

        for (Map.Entry<String, Integer> entry : armorCounts.entrySet()) {
            String materialName = entry.getKey();
            int count = entry.getValue();

            ArmorSetData data = ArmorSetDataRegistry.getData(major.toLowerCase(), year, materialName);
            if (data == null) {
                continue;
            }
            ArmorSetData.PartData partData = data.getParts().get(count + "Part");
            if (partData == null) {
                continue;
            }
            applySetEffects(player, partData);
            applySetAttributes(player, partData);
        }

    }


    private static void applySetAttributes(Player player, ArmorSetData.PartData partData) {


        if (partData.getAttributes() == null) {
            return;
        }

        removeOldZaubereiModifiers(player);

        for (Map.Entry<String, Integer> entry : partData.getAttributes().entrySet()) {
            String attributeName = entry.getKey();
            double value = entry.getValue();


            Holder.Reference<Attribute> attributeHolder = getAttributeHolder(attributeName);


            AttributeInstance attributeInstance = player.getAttribute(attributeHolder);
            if (attributeInstance == null) {
                continue;
            }

            ResourceLocation modifierId = makeModifierId(attributeName);

            AttributeModifier modifier = new AttributeModifier(
                    modifierId,
                    value,
                    AttributeModifier.Operation.ADD_VALUE
            );

            attributeInstance.addTransientModifier(modifier);

        }

    }

    private static void removeOldZaubereiModifiers(Player player) {
        for (Attribute attribute : BuiltInRegistries.ATTRIBUTE) {
            Optional<ResourceKey<Attribute>> optionalKey = BuiltInRegistries.ATTRIBUTE.getResourceKey(attribute);
            if (optionalKey.isEmpty()) {
                return;
            }

            Holder.Reference<Attribute> attributeHolder = BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(optionalKey.get());
            AttributeInstance attributeInstance = player.getAttribute(attributeHolder);
            if (attributeInstance == null) {
                continue;
            }

            for (AttributeModifier modifier : attributeInstance.getModifiers()) {
                ResourceLocation id = modifier.id();
                if (id.getNamespace().equals(Zauberei.MODID)) {
                    attributeInstance.removeModifier(modifier);

                }
            }
        }
    }

    private static Holder.Reference<Attribute> getAttributeHolder(String attributeName) {
        ResourceLocation attributeLoc = tryMakeResourceLocation(attributeName);
        if (attributeLoc == null) {
            return null;
        }

        ResourceLocation loc;
        try {
            loc = ResourceLocation.parse(attributeName);
        } catch (Exception e) {
            return null;
        }

        Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(loc);

        Optional<ResourceKey<Attribute>> optionalKey = BuiltInRegistries.ATTRIBUTE.getResourceKey(attribute);
        if (optionalKey.isEmpty()) {
            return null;
        }

        return BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(optionalKey.get());
    }

    public static ResourceLocation makeModifierId(String attributeName) {
        int index = attributeName.indexOf(":");
        if (index == -1) {
            return null;
        }
        attributeName = attributeName.substring(index + 1);
        String path = attributeName + "_bonus";
        return ResourceLocation.fromNamespaceAndPath(Zauberei.MODID, path);
    }


    private static void applySetEffects(Player player, ArmorSetData.PartData partData) {
        if (partData.getEffects() == null) {
            return;
        }
        for (ArmorSetData.EffectData effectData : partData.getEffects()) {
            ResourceLocation effectLoc = tryMakeResourceLocation(effectData.getEffect());
            if (effectLoc == null) {
                continue;
            }

            MobEffect mobEffect = BuiltInRegistries.MOB_EFFECT.get(effectLoc);
            if (mobEffect == null) {
                return;
            }
            int duration = 200;
            int amplifier = effectData.getAmplifier();

            Optional<ResourceKey<MobEffect>> optionalKey = BuiltInRegistries.MOB_EFFECT.getResourceKey(mobEffect);
            Holder.Reference<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(optionalKey.get());

            player.addEffect(new MobEffectInstance(effectHolder, duration, amplifier, false, false, true));
        }
    }

    private static ResourceLocation tryMakeResourceLocation(String effectName) {
        if (!effectName.contains(":")) {
            effectName = "minecraft:" + effectName;
        }
        try {
            return ResourceLocation.parse(effectName);
        } catch (Exception e) {
            Zauberei.LOGGER.error("Invalid effect name: " + effectName);
            return null;
        }
    }
}