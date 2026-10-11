package com.gilfort.zauberei.item;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.armor.MagicclothArmorItem;
import com.gilfort.zauberei.item.armor.ZaubereiArmorMaterials;
import com.gilfort.zauberei.item.custom.IntroductionLetter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers all items of the mod (except the Ars Nouveau variants, see
 * {@code com.gilfort.zauberei.compat.ArsNouveauCompat}).
 */
public class ZaubereiItems {

    /** Register for the items of the mod. */
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Zauberei.MODID);

    /** Magiccloth crafting material. */
    public static final DeferredItem<Item> MAGICCLOTH = ITEMS.registerSimpleItem("magiccloth");

    /** Introduction Letter that grants the starting items. */
    public static final DeferredItem<IntroductionLetter> INTRODUCTIONLETTER = ITEMS.registerItem("introductionletter",
            IntroductionLetter::new, properties -> properties.stacksTo(1));

    /** Magiccloth hood. */
    public static final DeferredItem<MagicclothArmorItem> MAGICCLOTH_HELMET = armor("magiccloth_helmet", ArmorType.HELMET, false);
    /** Magiccloth robe. */
    public static final DeferredItem<MagicclothArmorItem> MAGICCLOTH_CHESTPLATE = armor("magiccloth_chestplate", ArmorType.CHESTPLATE, false);
    /** Magiccloth pants. */
    public static final DeferredItem<MagicclothArmorItem> MAGICCLOTH_LEGGINGS = armor("magiccloth_leggings", ArmorType.LEGGINGS, false);
    /** Magiccloth boots. */
    public static final DeferredItem<MagicclothArmorItem> MAGICCLOTH_BOOTS = armor("magiccloth_boots", ArmorType.BOOTS, false);

    /** Alternative Magiccloth helmet (different model). */
    public static final DeferredItem<MagicclothArmorItem> MAGICCLOTH_HELMET_ALT = armor("magiccloth_helmet_alt", ArmorType.HELMET, true);
    /** Alternative Magiccloth chestplate (different model). */
    public static final DeferredItem<MagicclothArmorItem> MAGICCLOTH_CHESTPLATE_ALT = armor("magiccloth_chestplate_alt", ArmorType.CHESTPLATE, true);
    /** Alternative Magiccloth leggings (different model). */
    public static final DeferredItem<MagicclothArmorItem> MAGICCLOTH_LEGGINGS_ALT = armor("magiccloth_leggings_alt", ArmorType.LEGGINGS, true);
    /** Alternative Magiccloth boots (different model). */
    public static final DeferredItem<MagicclothArmorItem> MAGICCLOTH_BOOTS_ALT = armor("magiccloth_boots_alt", ArmorType.BOOTS, true);

    private static DeferredItem<MagicclothArmorItem> armor(String name, ArmorType type, boolean alt) {
        return ITEMS.registerItem(name, properties -> new MagicclothArmorItem(properties, alt),
                properties -> properties.stacksTo(1).humanoidArmor(ZaubereiArmorMaterials.MAGICCLOTH, type));
    }

    /**
     * Wires the item register to the mod event bus.
     *
     * @param eventBus the mod event bus
     */
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
