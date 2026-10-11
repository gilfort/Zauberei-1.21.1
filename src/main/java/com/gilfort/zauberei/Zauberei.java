package com.gilfort.zauberei;

import com.gilfort.zauberei.component.ComponentRegistry;
import com.gilfort.zauberei.compat.ArsNouveauCompat;
import com.gilfort.zauberei.creativetab.ZaubereiCreativeModeTabs;
import com.gilfort.zauberei.item.ZaubereiItems;
import com.gilfort.zauberei.item.armor.ZaubereiArmorMaterials;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Main entry point of the Zauberei mod. Wires all deferred registers, the optional Ars Nouveau
 * compatibility and the common config.
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
        ZaubereiArmorMaterials.register(modEventBus);
        ComponentRegistry.register(modEventBus);

        if (ModList.get().isLoaded("ars_nouveau")) {
            ArsNouveauCompat.init(modEventBus);
        } else {
            LOGGER.info("[Zauberei] Ars Nouveau not found, skipping compatibility setup.");
        }

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    /**
     * Creates a {@link ResourceLocation} in the {@value #MODID} namespace.
     *
     * @param path the path of the resource location
     * @return {@code zauberei:<path>}
     */
    public static ResourceLocation id(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
