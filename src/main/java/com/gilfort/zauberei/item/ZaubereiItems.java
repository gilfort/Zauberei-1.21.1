package com.gilfort.zauberei.item;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.custom.WandItem;
import net.minecraft.resources.ResourceLocation;
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
            () -> new ZaubereiMagicclothArmorItem(ArmorItem.Type.HELMET));
    public static final DeferredHolder<Item, Item> MAGICCLOTH_CHESTPLATE = ITEMS.register("magiccloth_chestplate",
            () -> new ZaubereiMagicclothArmorItem(ArmorItem.Type.CHESTPLATE));
    public static final DeferredHolder<Item, Item> MAGICCLOTH_LEGGINGS = ITEMS.register("magiccloth_leggings",
            () -> new ZaubereiMagicclothArmorItem(ArmorItem.Type.LEGGINGS));
    public static final DeferredHolder<Item, Item> MAGICCLOTH_BOOTS = ITEMS.register("magiccloth_boots",
            () -> new ZaubereiMagicclothArmorItem(ArmorItem.Type.BOOTS));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

