package com.gilfort.zauberei.entity.armor.magiccloth;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.armor.MagicclothArmorItem;
import net.minecraft.resources.Identifier;

/**
 * GeckoLib model of the Magiccloth armor; the alternative pieces use their own geometry, texture and animation.
 * Whether a render pass is for an alternative piece is read from {@link MagicclothArmorRenderer#IS_ALT}.
 */
public class MagicclothArmorModel extends GeoModel<MagicclothArmorItem> {

    // GeckoLib 5 ids: relative to assets/zauberei/geckolib/models|animations/, without the file suffix.
    private static final Identifier MODEL = Zauberei.id("armor/magiccloth");
    private static final Identifier MODEL_ALT = Zauberei.id("armor/magiccloth_alt");
    private static final Identifier ANIMATION = Zauberei.id("armor/magiccloth");
    private static final Identifier ANIMATION_ALT = Zauberei.id("armor/magiccloth_alt");
    // Textures are full resource paths.
    private static final Identifier TEXTURE = Zauberei.id("textures/models/armor/magiccloth.png");
    private static final Identifier TEXTURE_ALT = Zauberei.id("textures/models/armor/magiccloth_alt.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return isAlt(renderState) ? MODEL_ALT : MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return isAlt(renderState) ? TEXTURE_ALT : TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(MagicclothArmorItem item) {
        return item.isAlt() ? ANIMATION_ALT : ANIMATION;
    }

    private static boolean isAlt(GeoRenderState renderState) {
        return Boolean.TRUE.equals(renderState.getGeckolibData(MagicclothArmorRenderer.IS_ALT));
    }
}
