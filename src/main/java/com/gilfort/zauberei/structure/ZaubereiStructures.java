package com.gilfort.zauberei.structure;

import com.gilfort.zauberei.Zauberei;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ZaubereiStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Zauberei.MODID);

    public static final DeferredHolder<StructureType<?>,StructureType<JigsawStructure>> WITCHWOOD_SCHOOL = STRUCTURES.register("witchwood_school", ()-> structureTypeTyping(JigsawStructure.CODEC));

    public static <T extends Structure> StructureType<T> structureTypeTyping(MapCodec<T> structureCodec){
        return ()-> structureCodec;
    }
}
