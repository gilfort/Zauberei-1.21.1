package com.gilfort.zauberei.item.armor;

import com.gilfort.zauberei.entity.armor.magiccloth.MagicclothArmorModel;
import com.gilfort.zauberei.entity.armor.magiccloth.MagicclothArmorRenderer;
import com.gilfort.zauberei.item.ZaubereiItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * Magiccloth armor piece (school uniform), rendered with GeckoLib.
 */
public class MagicclothArmorItem extends ExtendedArmorItem {

    /**
     * Creates a Magiccloth armor piece.
     *
     * @param slot     the armor type
     * @param settings the item properties
     */
    public MagicclothArmorItem(Type slot, Properties settings) {
        super(ZaubereiArmorMaterials.MAGICCLOTH, slot, settings);
    }

    /**
     * Whether this is one of the alternative ({@code _alt}) pieces with their own model and texture.
     *
     * @return true for the alternative pieces
     */
    public boolean isAlt() {
        return this == ZaubereiItems.MAGICCLOTH_HELMET_ALT.get()
                || this == ZaubereiItems.MAGICCLOTH_CHESTPLATE_ALT.get()
                || this == ZaubereiItems.MAGICCLOTH_LEGGINGS_ALT.get()
                || this == ZaubereiItems.MAGICCLOTH_BOOTS_ALT.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new MagicclothArmorRenderer(new MagicclothArmorModel());
    }
}
