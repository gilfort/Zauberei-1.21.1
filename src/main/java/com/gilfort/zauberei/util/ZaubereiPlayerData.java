package com.gilfort.zauberei.util;

import com.gilfort.zauberei.Zauberei;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ZaubereiPlayerData {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Zauberei.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CompoundTag>> PLAYER_DATA =
            ATTACHMENT_TYPES.register("player_data", () ->
                    AttachmentType.<CompoundTag>builder(CompoundTag::new)
                            .serialize(CompoundTag.CODEC)
                            .copyOnDeath()   // Automatisches Kopieren beim Respawn
                            .build()
            );



}
