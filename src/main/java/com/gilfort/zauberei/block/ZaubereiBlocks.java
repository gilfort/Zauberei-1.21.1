package com.gilfort.zauberei.block;

import com.gilfort.zauberei.Zauberei;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ZaubereiBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Zauberei.MODID);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
