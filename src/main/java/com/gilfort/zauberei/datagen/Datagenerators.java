package com.gilfort.zauberei.datagen;

import com.gilfort.zauberei.Zauberei;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Registers all data providers. Run with {@code ./gradlew runData}; output goes to
 * {@code src/generated/resources/}. Tags are hand-written in {@code src/main/resources/data}.
 */
@EventBusSubscriber(modid = Zauberei.MODID)
public class Datagenerators {

    /**
     * Adds the client data providers (item models and client item definitions).
     *
     * @param event the client gather data event
     */
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        event.createProvider(ZaubereiModelProvider::new);
    }
}
