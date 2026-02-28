package com.gilfort.zauberei.item;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.armor.MagicclothArmorItem;
import com.gilfort.zauberei.item.custom.IntroductionLetter;
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

    public static final DeferredItem<Item> INTRODUCTIONLETTER = ITEMS.register("introductionletter",
            () -> new IntroductionLetter(new Item.Properties().stacksTo(1)));

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


    //Alt-Versions
    public static final DeferredHolder<Item, Item> MAGICCLOTH_HELMET_ALT = ITEMS.register("magiccloth_helmet_alt",
            ()-> new MagicclothArmorItem(ArmorItem.Type.HELMET, ItemPropertiesHelper.equipment(1)
                    .durability(ArmorItem.Type.HELMET.getDurability(26))));
    public static final DeferredHolder<Item, Item> MAGICCLOTH_CHESTPLATE_ALT = ITEMS.register("magiccloth_chestplate_alt",
            ()-> new MagicclothArmorItem(ArmorItem.Type.CHESTPLATE, ItemPropertiesHelper.equipment(1)
                    .durability(ArmorItem.Type.CHESTPLATE.getDurability(26))));
    public static final DeferredHolder<Item, Item> MAGICCLOTH_LEGGINGS_ALT = ITEMS.register("magiccloth_leggings_alt",
            ()-> new MagicclothArmorItem(ArmorItem.Type.LEGGINGS, ItemPropertiesHelper.equipment(1)
                    .durability(ArmorItem.Type.LEGGINGS.getDurability(26))));
    public static final DeferredHolder<Item, Item> MAGICCLOTH_BOOTS_ALT = ITEMS.register("magiccloth_boots_alt",
            ()-> new MagicclothArmorItem(ArmorItem.Type.BOOTS, ItemPropertiesHelper.equipment(1)
                    .durability(ArmorItem.Type.BOOTS.getDurability(26))));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

