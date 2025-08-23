package com.gilfort.zauberei.commands;

import com.gilfort.zauberei.Zauberei;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

/**
 * Handles encoding and decoding of {@link GwState} to the player's persistent data.
 */
public class CommandsStateCodec {
    private static final String NBT_KEY = "gw";

    public static GwState load(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        CompoundTag mod = data.getCompound(Zauberei.MODID);
        if (!mod.contains(NBT_KEY)) {
            return new GwState();
        }
        CompoundTag tag = mod.getCompound(NBT_KEY);
        GwState state = new GwState();
        ListTag tags = tag.getList("tags", Tag.TAG_STRING);
        for (Tag t : tags) {
            state.activeTags.add(t.getAsString());
        }
        if (tag.contains("tier")) {
            state.tier = tag.getInt("tier");
        }
        if (tag.contains("next")) {
            state.nextInTicks = tag.getInt("next");
        }
        return state;
    }

    public static void save(ServerPlayer player, GwState state) {
        CompoundTag data = player.getPersistentData();
        CompoundTag mod = data.getCompound(Zauberei.MODID);
        CompoundTag tag = new CompoundTag();
        ListTag tags = new ListTag();
        for (String s : state.activeTags) {
            tags.add(StringTag.valueOf(s));
        }
        tag.put("tags", tags);
        tag.putInt("tier", state.tier);
        tag.putInt("next", state.nextInTicks);
        mod.put(NBT_KEY, tag);
        data.put(Zauberei.MODID, mod);
    }
}
