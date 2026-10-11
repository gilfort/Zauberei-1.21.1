package com.gilfort.zauberei.datagen;

import com.gilfort.zauberei.Zauberei;
import net.minecraft.data.DataGenerator;
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
     * Adds the data providers to the generator.
     *
     * @param event the gather data event
     */
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(event.includeClient(),
                new ZaubereiItemModelProvider(generator.getPackOutput(), event.getExistingFileHelper()));
    }
}
