package com.gilfort.zauberei.entity.armor.magiccloth;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.ZaubereiItems;
import com.gilfort.zauberei.item.armor.MagicclothArmorItem;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Constructor;


public class MagicclothArmorModel extends GeoModel<MagicclothArmorItem> {

    public MagicclothArmorModel(){
        super();

    }

    @Override
    public ResourceLocation getModelResource(MagicclothArmorItem object){
        if(object.isAlt()) {
            return createResourceLocation(Zauberei.MODID, "geo/magiccloth_armor_alt.geo.json");
        }else {
            return createResourceLocation(Zauberei.MODID, "geo/magiccloth_armor.geo.json");
        }
    }

    @Override
    public ResourceLocation getTextureResource(MagicclothArmorItem object){
        if(object.isAlt()) {
            return createResourceLocation(Zauberei.MODID, "textures/models/armor/magiccloth_alt.png");
        }else {
            return createResourceLocation(Zauberei.MODID, "textures/models/armor/magiccloth.png");
        }
    }

    @Override
    public ResourceLocation getAnimationResource(MagicclothArmorItem object){
        if(object.isAlt()) {
            return createResourceLocation(Zauberei.MODID, "animations/magiccloth_armor_animation_alt.json");
        }else {
            return createResourceLocation(Zauberei.MODID, "animations/magiccloth_armor_animation.json");
        }
    }

    private ResourceLocation createResourceLocation(String namespace, String path) {
        try {
            Constructor<ResourceLocation> constructor = ResourceLocation.class.getDeclaredConstructor(String.class, String.class);
            constructor.setAccessible(true);
            return constructor.newInstance(namespace, path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ResourceLocation", e);
        }
    }

}