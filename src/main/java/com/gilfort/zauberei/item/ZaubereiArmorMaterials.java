package com.gilfort.zauberei.item;

import com.gilfort.zauberei.Zauberei;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class ZaubereiArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> REGISTRY = DeferredRegister.create(BuiltInRegistries.ARMOR_MATERIAL, Zauberei.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MAGICCLOTH = REGISTRY.register(
            "magiccloth", () -> material(
                    "magiccloth",
                    new int[]{3, 6, 8, 3, 11},
                    10,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    2.0f,
                    0.0f,
                    Ingredient.of(ZaubereiItems.MAGICCLOTH)));

    private ZaubereiArmorMaterials() {
    }

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }

    private static ArmorMaterial material(String name, int[] protection, int i, Holder<SoundEvent> holder, float f, float g, Ingredient ingredient) {
        var map = new EnumMap<ArmorItem.Type, Integer>(ArmorItem.Type.class);
        map.put(ArmorItem.Type.BOOTS, protection[0]);
        map.put(ArmorItem.Type.LEGGINGS, protection[1]);
        map.put(ArmorItem.Type.CHESTPLATE, protection[2]);
        map.put(ArmorItem.Type.HELMET, protection[3]);
        map.put(ArmorItem.Type.BODY, protection[4]);
        return new ArmorMaterial(map, i, holder, () -> ingredient, List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Zauberei.MODID, name))), f, g);
    }

}
