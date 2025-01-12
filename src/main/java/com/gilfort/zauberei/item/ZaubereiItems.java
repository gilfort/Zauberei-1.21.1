package com.gilfort.zauberei.item;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.custom.WandItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
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


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

