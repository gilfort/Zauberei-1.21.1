package com.gilfort.zauberei.helpers;

import com.gilfort.zauberei.Zauberei;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class PlayerDataHelper {
    
    private static final String MAJOR_TAG = "Major";
    private static final String YEAR_TAG = "Year";

    // Speichert den Major-Tag
    public static void setMajor(ServerPlayer player, String major) {
        CompoundTag persistentData = player.getPersistentData().getCompound(Zauberei.MODID);
        persistentData.putString(MAJOR_TAG, major);
        player.getPersistentData().put(Zauberei.MODID, persistentData);
        Zauberei.LOGGER.info("MajorTag set to " + major + "for" + player.getName().getString());
    }

    // Liest den Major-Tag
    public static String getMajor(ServerPlayer player) {
        CompoundTag persistentData = player.getPersistentData().getCompound(Zauberei.MODID);
        return persistentData.getString(MAJOR_TAG);
    }

    // Speichert den Year-Tag
    public static void setYear(ServerPlayer player, int year) {
        CompoundTag persistentData = player.getPersistentData().getCompound(Zauberei.MODID);
        persistentData.putInt(YEAR_TAG, year);
        player.getPersistentData().put(Zauberei.MODID, persistentData);
        Zauberei.LOGGER.info("YearTag set to " + year + "for" + player.getName().getString());
    }

    // Liest den Year-Tag
    public static int getYear(ServerPlayer player) {
        CompoundTag persistentData = player.getPersistentData().getCompound(Zauberei.MODID);
        return persistentData.getInt(YEAR_TAG);
    }
}