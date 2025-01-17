package com.gilfort.zauberei.entity.armor.magiccloth;

import com.gilfort.zauberei.entity.armor.GenericCustomArmorRenderer;
import com.gilfort.zauberei.item.armor.MagicclothArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.RenderUtil;

import javax.annotation.Nullable;

public class MagicclothArmorRenderer extends GenericCustomArmorRenderer<MagicclothArmorItem> {
    public GeoBone bodyHeadLayerBone = null;

    public MagicclothArmorRenderer(GeoModel<MagicclothArmorItem> model){
        super(model);
    }

    @Nullable
    public GeoBone getBodyHeadLayerBone(){
        return this.model.getBone("armorBodyHeadLayer").orElse(null);
    }

    @Override
    protected void grabRelevantBones(BakedGeoModel bakedModel){
        if(this.lastModel == bakedModel)
            this.bodyHeadLayerBone = getBodyHeadLayerBone();
        super.grabRelevantBones(bakedModel);
    }

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot){
        super.applyBoneVisibilityBySlot(currentSlot);
        if(currentSlot == EquipmentSlot.CHEST){
            setBoneVisible(this.bodyHeadLayerBone, true);
        }
    }

    @Override
    public void applyBoneVisibilityByPart(EquipmentSlot currentSlot, ModelPart currentPart, HumanoidModel<?> model){
        super.applyBoneVisibilityByPart(currentSlot, currentPart, model);
        if (currentPart == model.body && currentSlot == EquipmentSlot.CHEST) {
            setBoneVisible(this.bodyHeadLayerBone, true);
        }
    }

    @Override
    protected void applyBaseTransformations(HumanoidModel<?> baseModel) {
        super.applyBaseTransformations(baseModel);
        if(this.bodyHeadLayerBone != null){
            ModelPart bodyPart = baseModel.head;
            RenderUtil.matchModelPartRot(bodyPart, this.bodyHeadLayerBone);
            this.bodyHeadLayerBone.updatePosition(bodyPart.x, bodyPart.y, bodyPart.z);
        }
    }

    @Override
    public void setAllVisible(boolean pVisible){
        super.setAllVisible(pVisible);
        setBoneVisible(this.bodyHeadLayerBone, pVisible);
    }


}