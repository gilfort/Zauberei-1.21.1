package com.gilfort.zauberei.item.armor;

import com.geckolib.renderer.GeoArmorRenderer;
import com.gilfort.zauberei.client.ClientHooks;

/**
 * Magiccloth armor piece (school uniform), rendered with GeckoLib.
 */
public class MagicclothArmorItem extends ExtendedArmorItem {

    private final boolean alt;

    /**
     * Creates a Magiccloth armor piece.
     *
     * @param properties the item properties, including {@link Properties#humanoidArmor}
     * @param alt        whether this is an alternative ({@code _alt}) piece with its own model and texture
     */
    public MagicclothArmorItem(Properties properties, boolean alt) {
        super(properties);
        this.alt = alt;
    }

    /**
     * Whether this is one of the alternative ({@code _alt}) pieces with their own model and texture.
     *
     * @return true for the alternative pieces
     */
    public boolean isAlt() {
        return this.alt;
    }

    @Override
    protected GeoArmorRenderer<?, ?> createArmorRenderer() {
        // Goes through ClientHooks so the renderer class is never resolved on a dedicated server.
        return ClientHooks.createMagicclothArmorRenderer();
    }
}
