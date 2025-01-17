package com.gilfort.zauberei.item;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.armor.MagicclothArmorItem;
import com.gilfort.zauberei.item.custom.WandItem;
import com.gilfort.zauberei.util.ItemPropertiesHelper;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ZaubereiItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Zauberei.MODID);

    //setting up classless items
    public static final DeferredItem<Item> MAGICCLOTH = ITEMS.register("magiccloth",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BLUE_GEM = ITEMS.register("blue_gem",
            () -> new Item(new Item.Properties()));

    //setting up special itemclasses
    public static final DeferredItem<Item> WAND = ITEMS.register("wand",
            () -> new WandItem(new Item.Properties()));

    //setting up armor items
    public static final DeferredHolder<Item, Item> MAGICCLOTH_HELMET = ITEMS.register("magiccloth_helmet",
            ()-> new MagicclothArmorItem(ArmorItem.Type.HELMET, ItemPropertiesHelper.equipment(1)
                    .durability(ArmorItem.Type.HELMET.getDurability(26))));
    public static final DeferredHolder<Item, Item> MAGICCLOTH_CHESTPLATE = ITEMS.register("magiccloth_chestplate",
            ()-> new MagicclothArmorItem(ArmorItem.Type.CHESTPLATE, ItemPropertiesHelper.equipment(1)
                    .durability(ArmorItem.Type.CHESTPLATE.getDurability(26))));
    public static final DeferredHolder<Item, Item> MAGICCLOTH_LEGGINGS = ITEMS.register("magiccloth_leggings",
            ()-> new MagicclothArmorItem(ArmorItem.Type.LEGGINGS, ItemPropertiesHelper.equipment(1)
                    .durability(ArmorItem.Type.LEGGINGS.getDurability(26))));
    public static final DeferredHolder<Item, Item> MAGICCLOTH_BOOTS = ITEMS.register("magiccloth_boots",
            ()-> new MagicclothArmorItem(ArmorItem.Type.BOOTS, ItemPropertiesHelper.equipment(1)
                    .durability(ArmorItem.Type.BOOTS.getDurability(26))));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

