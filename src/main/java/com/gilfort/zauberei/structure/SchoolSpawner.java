package com.gilfort.zauberei.structure;


import com.gilfort.zauberei.Config;
import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.util.StructureDebugUtil;
import com.jcraft.jorbis.Block;
import net.minecraft.client.telemetry.events.WorldLoadEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.jfr.event.WorldLoadFinishedEvent;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.WorldData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.Optional;

@EventBusSubscriber(modid = Zauberei.MODID)
public class SchoolSpawner {

    private static boolean placed = false;

    @SubscribeEvent
    public static void onServerStarting(LevelEvent.CreateSpawnPosition event) {
        if(placed) return;
        if(!Config.SPAWN_WITCHWOOD_SCHOOL.get()) return;
        if(!(event.getLevel() instanceof ServerLevel level)) return;
        if(!level.dimension().location().equals(ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"))) return;

        place(level, new BlockPos(-16, 244, -23));

        Zauberei.LOGGER.info("[Zauberei] Placing Witchwood School at 0|0 (high up!)");
        placed = true;
    }

    public static final ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Zauberei.MODID, "witchwood_school");

    public static void place(ServerLevel level, BlockPos pos){


        StructureTemplateManager manager = level.getStructureManager();
        Optional<StructureTemplate> optionalTemplate = manager.get(location);

        if(optionalTemplate.isEmpty()){
            Zauberei.LOGGER.warn("[Zauberei] Structure template not found: {}", location);
            return;
        }
        StructureTemplate template = optionalTemplate.get();

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setIgnoreEntities(true)
                .setRotation(Rotation.NONE)
                .setMirror(Mirror.NONE);

        template.placeInWorld(level, pos, pos, settings, level.getRandom(), 2);
    }
}
