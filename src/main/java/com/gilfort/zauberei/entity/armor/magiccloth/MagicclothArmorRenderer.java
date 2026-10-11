package com.gilfort.zauberei.entity.armor.magiccloth;

import com.gilfort.zauberei.entity.armor.GenericCustomArmorRenderer;
import com.gilfort.zauberei.item.armor.MagicclothArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.RenderUtil;

/**
 * Renderer of the Magiccloth armor. Adds the hood layer bone ({@code armorBodyHeadLayer}) that belongs to
 * the chest piece but follows the head.
 */
public class MagicclothArmorRenderer extends GenericCustomArmorRenderer<MagicclothArmorItem> {

    /** Hood layer bone of the chest piece, or {@code null} if the model has none. */
    @Nullable
    public GeoBone bodyHeadLayerBone = null;

    /**
     * Creates the renderer.
     *
     * @param model the armor model
     */
    public MagicclothArmorRenderer(GeoModel<MagicclothArmorItem> model) {
        super(model);
    }

    /**
     * Looks up the hood layer bone in the current model.
     *
     * @return the bone, or {@code null} if the model has none
     */
    @Nullable
    public GeoBone getBodyHeadLayerBone() {
        return this.model.getBone("armorBodyHeadLayer").orElse(null);
    }

    @Override
    protected void grabRelevantBones(BakedGeoModel bakedModel) {
        if (this.lastModel == bakedModel)
            this.bodyHeadLayerBone = getBodyHeadLayerBone();
        super.grabRelevantBones(bakedModel);
    }

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        super.applyBoneVisibilityBySlot(currentSlot);
        if (currentSlot == EquipmentSlot.CHEST) {
            setBoneVisible(this.bodyHeadLayerBone, true);
        }
    }

    @Override
    public void applyBoneVisibilityByPart(EquipmentSlot currentSlot, ModelPart currentPart, HumanoidModel<?> model) {
        super.applyBoneVisibilityByPart(currentSlot, currentPart, model);
        if (currentPart == model.body && currentSlot == EquipmentSlot.CHEST) {
            setBoneVisible(this.bodyHeadLayerBone, true);
        }
    }

    @Override
    protected void applyBaseTransformations(HumanoidModel<?> baseModel) {
        super.applyBaseTransformations(baseModel);
        if (this.bodyHeadLayerBone != null) {
            ModelPart head = baseModel.head;
            RenderUtil.matchModelPartRot(head, this.bodyHeadLayerBone);
            this.bodyHeadLayerBone.updatePosition(head.x, head.y, head.z);
        }
    }

    @Override
    public void setAllVisible(boolean visible) {
        super.setAllVisible(visible);
        setBoneVisible(this.bodyHeadLayerBone, visible);
    }
}
