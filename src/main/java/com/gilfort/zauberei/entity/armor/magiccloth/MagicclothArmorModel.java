package com.gilfort.zauberei.entity.armor.magiccloth;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.armor.MagicclothArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model of the Magiccloth armor; the alternative pieces use their own geometry, texture and animation.
 */
public class MagicclothArmorModel extends GeoModel<MagicclothArmorItem> {

    private static final ResourceLocation MODEL = Zauberei.id("geo/magiccloth_armor.geo.json");
    private static final ResourceLocation MODEL_ALT = Zauberei.id("geo/magiccloth_armor_alt.geo.json");
    private static final ResourceLocation TEXTURE = Zauberei.id("textures/models/armor/magiccloth.png");
    private static final ResourceLocation TEXTURE_ALT = Zauberei.id("textures/models/armor/magiccloth_alt.png");
    private static final ResourceLocation ANIMATION = Zauberei.id("animations/magiccloth_armor_animation.json");
    private static final ResourceLocation ANIMATION_ALT = Zauberei.id("animations/magiccloth_armor_animation_alt.json");

    @Override
    public ResourceLocation getModelResource(MagicclothArmorItem item) {
        return item.isAlt() ? MODEL_ALT : MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MagicclothArmorItem item) {
        return item.isAlt() ? TEXTURE_ALT : TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MagicclothArmorItem item) {
        return item.isAlt() ? ANIMATION_ALT : ANIMATION;
    }
}
