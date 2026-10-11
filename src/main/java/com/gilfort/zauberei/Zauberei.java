package com.gilfort.zauberei;

import com.gilfort.zauberei.component.ComponentRegistry;
import com.gilfort.zauberei.creativetab.ZaubereiCreativeModeTabs;
import com.gilfort.zauberei.item.ZaubereiItems;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Main entry point of the Zauberei mod. Wires all deferred registers and the common config.
 */
@Mod(Zauberei.MODID)
public class Zauberei {

    /** Mod id, must match {@code mod_id} in {@code gradle.properties}. */
    public static final String MODID = "zauberei";

    /** Shared logger of the mod. */
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Creates the mod instance. Called by FML with the mod event bus and container.
     *
     * @param modEventBus  the mod-specific event bus
     * @param modContainer the container of this mod
     */
    public Zauberei(IEventBus modEventBus, ModContainer modContainer) {
        ZaubereiItems.register(modEventBus);
        ZaubereiCreativeModeTabs.register(modEventBus);
        ComponentRegistry.register(modEventBus);

        // Ars Nouveau compat is disabled until Ars Nouveau has a 26.x release (see build.gradle).
        // if (net.neoforged.fml.ModList.get().isLoaded("ars_nouveau")) {
        //     com.gilfort.zauberei.compat.ArsNouveauCompat.init(modEventBus);
        // }

        modContainer.registerConfig(ModConfig.Type.LOCAL, Config.SPEC);
    }

    /**
     * Creates an {@link Identifier} in the {@value #MODID} namespace.
     *
     * @param path the path of the identifier
     * @return {@code zauberei:<path>}
     */
    public static Identifier id(@NotNull String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
