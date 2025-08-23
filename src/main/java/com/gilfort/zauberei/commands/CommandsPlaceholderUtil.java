package com.gilfort.zauberei.commands;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/** Utility for replacing placeholders in command strings. */
public class CommandsPlaceholderUtil {
    public static String apply(String cmd, ServerPlayer player, BlockPos pos) {
        Level level = player.level();
        String result = cmd;
        result = result.replace("{player}", player.getGameProfile().getName());
        result = result.replace("{x}", Integer.toString(pos.getX()));
        result = result.replace("{y}", Integer.toString(pos.getY()));
        result = result.replace("{z}", Integer.toString(pos.getZ()));
        result = result.replace("{dim}", level.dimension().location().toString());
        return result;
    }
}
