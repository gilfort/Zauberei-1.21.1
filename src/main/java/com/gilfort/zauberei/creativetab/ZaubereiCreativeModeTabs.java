package com.gilfort.zauberei.creativetab;

import com.gilfort.zauberei.Config;
import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.compat.ArsNouveauCompat;
import com.gilfort.zauberei.item.ZaubereiItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registers the creative mode tab of the mod.
 */
public class ZaubereiCreativeModeTabs {

    /** Register for the creative mode tabs of the mod. */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Zauberei.MODID);

    /** The main Zauberei tab. */
    public static final Supplier<CreativeModeTab> ZAUBEREI_CREATIVE_TAB = CREATIVE_MODE_TAB.register("zauberei_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ZaubereiItems.MAGICCLOTH.get()))
                    .title(Component.translatable("itemGroup.zauberei.zauberei_tab"))
                    .displayItems((parameters, output) -> {
                        // Items are only listed when enable_magical_armor is true. JEI/EMI source their
                        // item lists from creative tabs, so this hides them there as well.
                        if (!Config.ENABLE_MAGICAL_ARMOR.get()) {
                            return;
                        }

                        output.accept(ZaubereiItems.MAGICCLOTH);
                        output.accept(ZaubereiItems.INTRODUCTIONLETTER);

                        output.accept(ZaubereiItems.MAGICCLOTH_HELMET.get());
                        output.accept(ZaubereiItems.MAGICCLOTH_CHESTPLATE.get());
                        output.accept(ZaubereiItems.MAGICCLOTH_LEGGINGS.get());
                        output.accept(ZaubereiItems.MAGICCLOTH_BOOTS.get());

                        output.accept(ZaubereiItems.MAGICCLOTH_HELMET_ALT.get());
                        output.accept(ZaubereiItems.MAGICCLOTH_CHESTPLATE_ALT.get());
                        output.accept(ZaubereiItems.MAGICCLOTH_LEGGINGS_ALT.get());
                        output.accept(ZaubereiItems.MAGICCLOTH_BOOTS_ALT.get());

                        if (ModList.get().isLoaded("ars_nouveau")) {
                            output.accept(ArsNouveauCompat.MAGICCLOTH_HELMET_ARS.get());
                            output.accept(ArsNouveauCompat.MAGICCLOTH_CHESTPLATE_ARS.get());
                            output.accept(ArsNouveauCompat.MAGICCLOTH_LEGGINGS_ARS.get());
                            output.accept(ArsNouveauCompat.MAGICCLOTH_BOOTS_ARS.get());
                        }
                    }).build());

    /**
     * Wires the tab register to the mod event bus.
     *
     * @param eventBus the mod event bus
     */
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
