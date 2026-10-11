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

    private static final Identifier MODEL = Zauberei.id("geo/magiccloth_armor.geo.json");
    private static final Identifier MODEL_ALT = Zauberei.id("geo/magiccloth_armor_alt.geo.json");
    private static final Identifier TEXTURE = Zauberei.id("textures/models/armor/magiccloth.png");
    private static final Identifier TEXTURE_ALT = Zauberei.id("textures/models/armor/magiccloth_alt.png");
    private static final Identifier ANIMATION = Zauberei.id("animations/magiccloth_armor_animation.json");
    private static final Identifier ANIMATION_ALT = Zauberei.id("animations/magiccloth_armor_animation_alt.json");

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
