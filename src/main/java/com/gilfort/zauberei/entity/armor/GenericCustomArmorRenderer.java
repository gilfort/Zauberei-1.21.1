package com.gilfort.zauberei.entity.armor;

import com.gilfort.zauberei.component.ComponentRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.ArrayList;
import java.util.function.Function;

/**
 * GeckoLib armor renderer with "async" bones: bones that belong to one armor piece but follow another body
 * part (e.g. robe parts that move with the legs). Bones whose name starts with {@code alt} are only shown
 * when the stack carries {@link ComponentRegistry#CLOTHING_ALT}.
 *
 * @param <T> the armor item type
 */
public class GenericCustomArmorRenderer<T extends Item & GeoItem> extends GeoArmorRenderer<T> {

    /**
     * A bone rendered for {@link #itemSlot} that follows the body part returned by {@link #partToFollow}.
     */
    public class AsyncBone {
        @Nullable
        private GeoBone actualBone;
        private final String boneName;
        private final EquipmentSlot itemSlot;
        private final Function<HumanoidModel<?>, ModelPart> partToFollow;
        private final Vec3 partOffset;

        /**
         * Creates an async bone.
         *
         * @param boneName     name of the bone in the GeckoLib model
         * @param itemSlot     slot of the armor piece the bone belongs to
         * @param partToFollow body part whose rotation and position the bone copies
         * @param partOffset   offset added to the followed part's position
         */
        public AsyncBone(String boneName, EquipmentSlot itemSlot, Function<HumanoidModel<?>, ModelPart> partToFollow, Vec3 partOffset) {
            this.actualBone = null;
            this.boneName = boneName;
            this.itemSlot = itemSlot;
            this.partToFollow = partToFollow;
            this.partOffset = partOffset;
        }

        /**
         * Resolves the bone in the given model.
         *
         * @param model the GeckoLib model
         */
        public void grabBone(GeoModel<?> model) {
            this.actualBone = model.getBone(boneName).orElse(null);
        }

        /**
         * Shows the bone when its armor piece is rendered (alt bones only with the transmog component).
         *
         * @param currentSlot the slot currently rendered
         */
        public void applyVisibility(EquipmentSlot currentSlot) {
            if (currentSlot == this.itemSlot) {
                setBoneVisible(this.actualBone, isAllowed());
            }
        }

        private boolean isAllowed() {
            return !boneName.startsWith("alt") || GenericCustomArmorRenderer.this.getCurrentStack().has(ComponentRegistry.CLOTHING_ALT);
        }
    }

    /** All async bones of this renderer. */
    protected final ArrayList<AsyncBone> asyncBones;

    /**
     * Creates the renderer with the default async bones of the Magiccloth robe.
     *
     * @param model the armor model
     */
    public GenericCustomArmorRenderer(GeoModel<T> model) {
        super(model);
        this.asyncBones = new ArrayList<>();
        asyncBones.add(new AsyncBone("armorLeggingTorsoLayer", EquipmentSlot.LEGS, m -> m.body, Vec3.ZERO));
        asyncBones.add(new AsyncBone("armorTorsoExtensionRightLeg", EquipmentSlot.CHEST, m -> m.rightLeg, new Vec3(2, 12, 0)));
        asyncBones.add(new AsyncBone("armorTorsoExtensionLeftLeg", EquipmentSlot.CHEST, m -> m.leftLeg, new Vec3(-2, 12, 0)));
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Intentionally empty: bone positions are copied from the base model in applyBaseTransformations.
    }

    @Override
    protected void grabRelevantBones(BakedGeoModel bakedModel) {
        if (this.lastModel != bakedModel) {
            asyncBones.forEach(bone -> bone.grabBone(this.model));
        }
        super.grabRelevantBones(bakedModel);
    }

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        super.applyBoneVisibilityBySlot(currentSlot);
        asyncBones.forEach(bone -> bone.applyVisibility(currentSlot));
    }

    @Override
    public void applyBoneVisibilityByPart(EquipmentSlot currentSlot, ModelPart currentPart, HumanoidModel<?> model) {
        super.applyBoneVisibilityByPart(currentSlot, currentPart, model);
        asyncBones.forEach(bone -> {
            if (bone.isAllowed() && bone.itemSlot == currentSlot && currentPart == bone.partToFollow.apply(model)) {
                setBoneVisible(bone.actualBone, true);
            }
        });
    }

    @Override
    protected void applyBaseTransformations(HumanoidModel<?> baseModel) {
        super.applyBaseTransformations(baseModel);
        asyncBones.forEach(bone -> {
            if (bone.actualBone != null) {
                var bodyPart = bone.partToFollow.apply(baseModel);
                RenderUtil.matchModelPartRot(bodyPart, bone.actualBone);
                bone.actualBone.updatePosition((float) bone.partOffset.x + bodyPart.x,
                        (float) bone.partOffset.y - bodyPart.y,
                        (float) bone.partOffset.z + bodyPart.z);
            }
        });
    }

    @Override
    public void setAllVisible(boolean visible) {
        super.setAllVisible(visible);
        asyncBones.forEach(bone -> setBoneVisible(bone.actualBone, visible));
    }
}
