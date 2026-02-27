package com.gilfort.zauberei.creativetab;


import com.gilfort.zauberei.Config;
import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.ZaubereiItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ZaubereiCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Zauberei.MODID);

    public static final Supplier<CreativeModeTab> ZAUBEREI_CREATIVE_TAB = CREATIVE_MODE_TAB.register("zauberei_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ZaubereiItems.MAGICCLOTH.get()))
                    .title(Component.translatable("creativetab.zauberei.zauberei_tab"))
                    .displayItems((itemDisplayParameters, output) -> {



                        // Items are only shown
                        // when enable_magical_armor = true in the config.
                        // This also prevents JEI/EMI from listing them, since those
                        // mods source their item lists from Creative Tabs.
                        if (Config.ENABLE_MAGICAL_ARMOR.get()) {

                            //Base Items
                            output.accept(ZaubereiItems.MAGICCLOTH);

                            //Letter
                            output.accept(ZaubereiItems.INTRODUCTIONLETTER);

                            //Armor
                            output.accept(ZaubereiItems.MAGICCLOTH_HELMET.get());
                            output.accept(ZaubereiItems.MAGICCLOTH_CHESTPLATE.get());
                            output.accept(ZaubereiItems.MAGICCLOTH_LEGGINGS.get());
                            output.accept(ZaubereiItems.MAGICCLOTH_BOOTS.get());

                            output.accept(ZaubereiItems.MAGICCLOTH_HELMET_ALT.get());
                            output.accept(ZaubereiItems.MAGICCLOTH_CHESTPLATE_ALT.get());
                            output.accept(ZaubereiItems.MAGICCLOTH_LEGGINGS_ALT.get());
                            output.accept(ZaubereiItems.MAGICCLOTH_BOOTS_ALT.get());
                        }

                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
