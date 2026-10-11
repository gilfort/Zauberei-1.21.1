package com.gilfort.zauberei.entity.armor.magiccloth;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.gilfort.zauberei.item.armor.MagicclothArmorItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * GeckoLib renderer of the Magiccloth armor.
 * <p>
 * Besides the default armor bones it renders "layer" bones that belong to one armor piece but follow another
 * body part: the robe hood ({@code armorBodyHeadLayer}, chest piece, follows the head) and the waistband of the
 * pants ({@code armorLeggingTorsoLayer}, leggings, follows the body).
 */
public class MagicclothArmorRenderer extends GeoArmorRenderer<MagicclothArmorItem, HumanoidRenderState> {

    /** Render state flag: whether the rendered piece is an alternative ({@code _alt}) piece. */
    public static final DataTicket<Boolean> IS_ALT = DataTicket.create("zauberei_magiccloth_is_alt", Boolean.class);

    /**
     * A bone rendered for {@link #slot} that copies the pose of the body part of {@link #followed}.
     *
     * @param boneName name of the bone in the GeckoLib model
     * @param slot     slot of the armor piece the bone belongs to
     * @param followed armor segment whose body part the bone follows
     */
    private record LayerBone(String boneName, EquipmentSlot slot, ArmorSegment followed) {
    }

    private static final List<LayerBone> LAYER_BONES = List.of(
            new LayerBone("armorBodyHeadLayer", EquipmentSlot.CHEST, ArmorSegment.HEAD),
            new LayerBone("armorLeggingTorsoLayer", EquipmentSlot.LEGS, ArmorSegment.CHEST));

    /** Creates the renderer with the {@link MagicclothArmorModel}. */
    public MagicclothArmorRenderer() {
        this(new MagicclothArmorModel());
    }

    private MagicclothArmorRenderer(GeoModel<MagicclothArmorItem> model) {
        super(model);
    }

    @Override
    public void addRenderData(MagicclothArmorItem animatable, @Nullable RenderData renderData,
                              HumanoidRenderState renderState, float partialTick) {
        renderState.addGeckolibData(IS_ALT, animatable.isAlt());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void adjustModelBonesForRender(RenderPassInfo<HumanoidRenderState> renderPassInfo, BoneSnapshots snapshots) {
        // Positions the default armor bones and runs setupAnim on the base model.
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        HumanoidRenderState renderState = renderPassInfo.renderState();
        EquipmentSlot slot = Objects.requireNonNull(renderState.getGeckolibData(CURRENT_SLOT));
        HumanoidModel baseModel = Objects.requireNonNull(renderState.getGeckolibData(BASE_MODEL));

        for (LayerBone layer : LAYER_BONES) {
            if (layer.slot() != slot) continue;
            snapshots.get(layer.boneName()).ifPresent(snapshot -> {
                ModelPart part = layer.followed().modelPartGetter.apply(baseModel);
                Vector3f pos = layer.followed().modelPartMatcher.apply(new Vector3f(part.x, part.y, part.z));
                snapshot.setRotX(-part.xRot)
                        .setRotY(-part.yRot)
                        .setRotZ(part.zRot)
                        .setTranslateX(pos.x)
                        .setTranslateY(pos.y)
                        .setTranslateZ(pos.z);
            });
        }
    }

    @Override
    public void submitRenderTasks(RenderPassInfo<HumanoidRenderState> renderPassInfo, OrderedSubmitNodeCollector renderTasks,
                                  @Nullable RenderType renderType) {
        if (renderType == null)
            return;

        if (renderPassInfo.model().isMissingno()) {
            submitMissingModelRender(renderPassInfo, renderTasks);
            return;
        }

        HumanoidRenderState humanoidState = renderPassInfo.renderState();
        EquipmentSlot slot = Objects.requireNonNull(humanoidState.getGeckolibData(CURRENT_SLOT));
        int packedLight = renderPassInfo.packedLight();
        int packedOverlay = renderPassInfo.packedOverlay();
        int renderColor = renderPassInfo.renderColor();

        // Same as GeoArmorRenderer#submitRenderTasks, plus the layer bones of this slot.
        renderTasks.submitCustomGeometry(renderPassInfo.poseStack(), renderType, (pose, vertexConsumer) -> {
            PoseStack poseStack = renderPassInfo.poseStack();

            poseStack.pushPose();
            poseStack.last().set(pose);
            renderPassInfo.renderPosed(() -> {
                for (ArmorSegment segment : getSegmentsForSlot(humanoidState, slot)) {
                    renderPassInfo.model().getBone(getBoneNameForSegment(humanoidState, segment))
                            .ifPresent(bone -> bone.positionAndRender(renderPassInfo, vertexConsumer, packedLight, packedOverlay, renderColor));
                }
                for (LayerBone layer : LAYER_BONES) {
                    if (layer.slot() != slot) continue;
                    renderPassInfo.model().getBone(layer.boneName())
                            .ifPresent(bone -> bone.positionAndRender(renderPassInfo, vertexConsumer, packedLight, packedOverlay, renderColor));
                }
            });
            poseStack.popPose();
        });
    }
}
