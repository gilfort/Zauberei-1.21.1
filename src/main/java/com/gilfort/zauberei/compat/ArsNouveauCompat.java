package com.gilfort.zauberei.compat;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.armor.MagicclothArmorItemArs;
import com.gilfort.zauberei.util.ItemPropertiesHelper;
import com.hollingsworth.arsnouveau.common.items.data.ArmorPerkHolder;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public class ArsNouveauCompat {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Zauberei.MODID);

    //Ars Armor:
    public static final DeferredHolder<Item, Item> MAGICCLOTH_HELMET_ARS = ITEMS.register("magiccloth_helmet_perks",
            ()-> new MagicclothArmorItemArs(ArmorItem.Type.HELMET, ItemPropertiesHelper.equipment(1)
                    .durability(ArmorItem.Type.HELMET.getDurability(26)).component(DataComponentRegistry.ARMOR_PERKS, new ArmorPerkHolder())));
    public static final DeferredHolder<Item, Item> MAGICCLOTH_CHESTPLATE_ARS = ITEMS.register("magiccloth_chestplate_perks",
            ()-> new MagicclothArmorItemArs(ArmorItem.Type.CHESTPLATE, ItemPropertiesHelper.equipment(1)
                    .durability(ArmorItem.Type.CHESTPLATE.getDurability(26)).component(DataComponentRegistry.ARMOR_PERKS, new ArmorPerkHolder())));
    public static final DeferredHolder<Item, Item> MAGICCLOTH_LEGGINGS_ARS = ITEMS.register("magiccloth_leggings_perks",
            ()-> new MagicclothArmorItemArs(ArmorItem.Type.LEGGINGS, ItemPropertiesHelper.equipment(1)
                    .durability(ArmorItem.Type.LEGGINGS.getDurability(26)).component(DataComponentRegistry.ARMOR_PERKS, new ArmorPerkHolder())));
    public static final DeferredHolder<Item, Item> MAGICCLOTH_BOOTS_ARS = ITEMS.register("magiccloth_boots_perks",
            ()-> new MagicclothArmorItemArs(ArmorItem.Type.BOOTS, ItemPropertiesHelper.equipment(1)
                    .durability(ArmorItem.Type.BOOTS.getDurability(26)).component(DataComponentRegistry.ARMOR_PERKS, new ArmorPerkHolder())));



    public static void init(IEventBus modEventBus){
        ITEMS.register(modEventBus);
        LOGGER.info("[Zauberei] Initializing Ars Nouveau Compatibility...");

    }
}
