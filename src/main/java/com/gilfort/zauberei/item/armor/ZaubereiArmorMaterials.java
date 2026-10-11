package com.gilfort.zauberei.item.armor;

import com.gilfort.zauberei.Zauberei;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

/**
 * Armor materials of the mod. Since 1.21.2 armor materials are plain records applied through
 * {@link net.minecraft.world.item.Item.Properties#humanoidArmor}, not registry entries.
 */
public final class ZaubereiArmorMaterials {

    /** Equipment asset id of the Magiccloth armor. Rendering is done by GeckoLib, not by an equipment asset file. */
    public static final ResourceKey<EquipmentAsset> MAGICCLOTH_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, Zauberei.id("magiccloth"));

    /** Magiccloth: leather-like defense, enchantability 15, repairable with leather. */
    public static final ArmorMaterial MAGICCLOTH = new ArmorMaterial(
            26,
            defense(1, 3, 2, 1),
            15,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0F,
            0.0F,
            ItemTags.REPAIRS_LEATHER_ARMOR,
            MAGICCLOTH_ASSET);

    private ZaubereiArmorMaterials() {
    }

    /**
     * Builds a defense map per armor type.
     *
     * @param helmet     defense of the helmet
     * @param chestplate defense of the chestplate
     * @param leggings   defense of the leggings
     * @param boots      defense of the boots
     * @return the defense map
     */
    public static Map<ArmorType, Integer> defense(int helmet, int chestplate, int leggings, int boots) {
        EnumMap<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(ArmorType.HELMET, helmet);
        map.put(ArmorType.CHESTPLATE, chestplate);
        map.put(ArmorType.LEGGINGS, leggings);
        map.put(ArmorType.BOOTS, boots);
        map.put(ArmorType.BODY, 0);
        return map;
    }
}
