package com.gilfort.zauberei.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;

/**
 * Small shared helpers.
 */
public class ZaubereiUtils {

    /**
     * Puts the stack into the player's inventory, or drops it at the player's feet if the inventory is full.
     *
     * @param player the receiving player
     * @param stack  the stack to give
     */
    public static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false, Prediction.SERVER_ONLY);
        }
    }
}
