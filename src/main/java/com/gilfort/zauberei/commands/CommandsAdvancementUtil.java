package com.gilfort.zauberei.commands;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Utility around player advancements. */
public class CommandsAdvancementUtil {
    public static boolean hasAdv(ServerPlayer player, ResourceLocation id) {
        AdvancementHolder holder = player.server.getAdvancements().get(id);
        if (holder == null) {
            return false;
        }
        return player.getAdvancements().getOrStartProgress(holder).isDone();
    }
}
