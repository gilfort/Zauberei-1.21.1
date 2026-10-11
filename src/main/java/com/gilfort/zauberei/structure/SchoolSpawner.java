package com.gilfort.zauberei.structure;

import com.gilfort.zauberei.Config;
import com.gilfort.zauberei.Zauberei;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.Optional;

/**
 * Places the Witchwood School template above the world spawn when a new world is created
 * (only if {@code spawn_witchwood_school} is enabled).
 */
@EventBusSubscriber(modid = Zauberei.MODID)
public class SchoolSpawner {

    /** Template id of the school ({@code data/zauberei/structure/witchwood_school.nbt}). */
    public static final ResourceLocation TEMPLATE = Zauberei.id("witchwood_school");

    /** Corner position the template is placed at; the school is centered around x/z 0. */
    private static final BlockPos ORIGIN = new BlockPos(-16, 244, -23);

    /**
     * Fires once per newly created world while its spawn position is chosen.
     *
     * @param event the spawn position event
     */
    @SubscribeEvent
    public static void onCreateSpawnPosition(LevelEvent.CreateSpawnPosition event) {
        if (!Config.SPAWN_WITCHWOOD_SCHOOL.get()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;

        if (place(level, ORIGIN)) {
            Zauberei.LOGGER.info("[Zauberei] Placed Witchwood School at {}.", ORIGIN);
        }
    }

    /**
     * Places the school template without rotation or entities.
     *
     * @param level the target level
     * @param pos   the corner position of the template
     * @return true if the template was found and placed
     */
    public static boolean place(ServerLevel level, BlockPos pos) {
        Optional<StructureTemplate> template = level.getStructureManager().get(TEMPLATE);
        if (template.isEmpty()) {
            Zauberei.LOGGER.warn("[Zauberei] Structure template not found: {}", TEMPLATE);
            return false;
        }

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setIgnoreEntities(true)
                .setRotation(Rotation.NONE)
                .setMirror(Mirror.NONE);
        // Flag 2: send block updates to clients without neighbor updates.
        return template.get().placeInWorld(level, pos, pos, settings, level.getRandom(), 2);
    }
}
