package com.gilfort.zauberei.util;

import net.minecraft.world.item.Item;

/**
 * Factory methods for common {@link Item.Properties}.
 */
public class ItemPropertiesHelper {

    /**
     * Item properties for equipment with the given maximum stack size.
     *
     * @param stackSize the maximum stack size
     * @return new item properties
     */
    public static Item.Properties equipment(int stackSize) {
        return new Item.Properties().stacksTo(stackSize);
    }
}
