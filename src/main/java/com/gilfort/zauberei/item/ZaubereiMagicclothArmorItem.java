package com.gilfort.zauberei.item;

import com.gilfort.zauberei.Zauberei;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;

import javax.annotation.Nullable;
import javax.swing.text.html.parser.Entity;
import java.util.function.Consumer;

public class ZaubereiMagicclothArmorItem extends ArmorItem implements GeoItem {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public ZaubereiMagicclothArmorItem(Type type){
        super (ZaubereiArmorMaterials.MAGICCLOTH, type, new Properties().durability(type.getDurability(33)));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<ZaubereiMagicclothArmorItem>(this, "controller", 20, this::predicate));
    }

    private PlayState predicate(AnimationState<ZaubereiMagicclothArmorItem> extendedArmorItemAnimationState) {
        extendedArmorItemAnimationState.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T livingEntity, ItemStack itemStack, @Nullable EquipmentSlot equipmentSlot,
                                                                                 @Nullable HumanoidModel<T> original) {
                if (this.renderer == null) {
                    this.renderer = supplyRenderer();
                }
                return this.renderer;
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return null;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
