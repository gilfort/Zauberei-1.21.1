package com.gilfort.zauberei.compat;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.armor.MagicclothArmorItemArs;
import com.gilfort.zauberei.util.ItemPropertiesHelper;
import com.hollingsworth.arsnouveau.api.perk.PerkSlot;
import com.hollingsworth.arsnouveau.api.registry.PerkRegistry;
import com.hollingsworth.arsnouveau.common.items.data.ArmorPerkHolder;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Optional Ars Nouveau integration: registers Magiccloth armor variants with perk slots.
 * Only touched when Ars Nouveau is loaded.
 */
public class ArsNouveauCompat {

    /** Register for the Ars Nouveau specific items. */
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Zauberei.MODID);

    /** Magiccloth hood with perk slots. */
    public static final DeferredHolder<Item, Item> MAGICCLOTH_HELMET_ARS = ITEMS.register("magiccloth_helmet_perks",
            () -> new MagicclothArmorItemArs(ArmorItem.Type.HELMET, ItemPropertiesHelper.equipment(1)
                    .durability(ArmorItem.Type.HELMET.getDurability(26)).component(DataComponentRegistry.ARMOR_PERKS, new ArmorPerkHolder())));
    /** Magiccloth robe with perk slots. */
    public static final DeferredHolder<Item, Item> MAGICCLOTH_CHESTPLATE_ARS = ITEMS.register("magiccloth_chestplate_perks",
            () -> new MagicclothArmorItemArs(ArmorItem.Type.CHESTPLATE, ItemPropertiesHelper.equipment(1)
                    .durability(ArmorItem.Type.CHESTPLATE.getDurability(26)).component(DataComponentRegistry.ARMOR_PERKS, new ArmorPerkHolder())));
    /** Magiccloth pants with perk slots. */
    public static final DeferredHolder<Item, Item> MAGICCLOTH_LEGGINGS_ARS = ITEMS.register("magiccloth_leggings_perks",
            () -> new MagicclothArmorItemArs(ArmorItem.Type.LEGGINGS, ItemPropertiesHelper.equipment(1)
                    .durability(ArmorItem.Type.LEGGINGS.getDurability(26)).component(DataComponentRegistry.ARMOR_PERKS, new ArmorPerkHolder())));
    /** Magiccloth boots with perk slots. */
    public static final DeferredHolder<Item, Item> MAGICCLOTH_BOOTS_ARS = ITEMS.register("magiccloth_boots_perks",
            () -> new MagicclothArmorItemArs(ArmorItem.Type.BOOTS, ItemPropertiesHelper.equipment(1)
                    .durability(ArmorItem.Type.BOOTS.getDurability(26)).component(DataComponentRegistry.ARMOR_PERKS, new ArmorPerkHolder())));

    /**
     * Registers the compat items and the perk provider setup.
     *
     * @param modEventBus the mod event bus
     */
    public static void init(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        Zauberei.LOGGER.info("[Zauberei] Initializing Ars Nouveau compatibility...");
        modEventBus.addListener(ArsNouveauCompat::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // One inner list per armor tier; each entry is one perk slot of that tier.
            List<List<PerkSlot>> slots = List.of(
                    List.of(PerkSlot.ONE, PerkSlot.TWO, PerkSlot.TWO)
            );
            List<List<PerkSlot>> slotsLow = List.of(
                    List.of(PerkSlot.ONE, PerkSlot.TWO)
            );
            List<List<PerkSlot>> slotsChestplate = List.of(
                    List.of(PerkSlot.ONE, PerkSlot.ONE, PerkSlot.THREE)
            );

            PerkRegistry.registerPerkProvider(MAGICCLOTH_HELMET_ARS.get(), slotsLow);
            PerkRegistry.registerPerkProvider(MAGICCLOTH_CHESTPLATE_ARS.get(), slotsChestplate);
            PerkRegistry.registerPerkProvider(MAGICCLOTH_LEGGINGS_ARS.get(), slots);
            PerkRegistry.registerPerkProvider(MAGICCLOTH_BOOTS_ARS.get(), slotsLow);
            Zauberei.LOGGER.info("[Zauberei] Registered Magiccloth perk providers for 4 armor pieces.");
        });
    }
}
