package com.gilfort.zauberei.item.armor;

import com.gilfort.zauberei.component.ComponentRegistry;
import com.gilfort.zauberei.entity.armor.magiccloth.MagicclothArmorModel;
import com.gilfort.zauberei.entity.armor.magiccloth.MagicclothArmorRenderer;
import com.gilfort.zauberei.item.ZaubereiItems;
import com.gilfort.zauberei.item.armorbonus.ArmorSetData;
import com.gilfort.zauberei.item.armorbonus.ArmorSetDataRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import com.gilfort.zauberei.helpers.PlayerDataHelper;

import java.util.List;
import java.util.Map;

import static com.gilfort.zauberei.component.ComponentRegistry.MAJOR;
import static com.gilfort.zauberei.component.ComponentRegistry.YEAR;


public class MagicclothArmorItem extends ExtendedArmorItem {


    public MagicclothArmorItem(Type slot, Properties settings) {
        super(ZaubereiArmorMaterials.MAGICCLOTH, slot, settings);

    }

    public boolean isAlt() {
        return this.equals(ZaubereiItems.MAGICCLOTH_HELMET_ALT.get()) || this.equals(ZaubereiItems.MAGICCLOTH_CHESTPLATE_ALT.get()) || this.equals(ZaubereiItems.MAGICCLOTH_LEGGINGS_ALT.get()) || this.equals(ZaubereiItems.MAGICCLOTH_BOOTS_ALT.get());
    }

    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new MagicclothArmorRenderer(new MagicclothArmorModel());
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.literal("§b[SHIFT to view Bonus Effects]").withStyle(ChatFormatting.AQUA));
            return;
        }

        tooltip.add(Component.literal("§b[Bonus Effects]").withStyle(ChatFormatting.AQUA));

        Player player = Minecraft.getInstance().player;

        var majorType = ComponentRegistry.MAJOR.value();
        var yearType = ComponentRegistry.YEAR.value();
        if (!stack.has(majorType) || !stack.has(yearType)) {
            tooltip.add(Component.literal("§7(Keine Set-Daten)"));
            return;
        }

        String major = stack.get(majorType);
        Integer yearObj = stack.get(yearType);
        if (yearObj == null) {
            return;
        }
        int year = yearObj;

        String materialName = BuiltInRegistries.ARMOR_MATERIAL.getKey(this.getMaterial().value()).getPath();
        ArmorSetData data = ArmorSetDataRegistry.getData(major.toLowerCase(), year, materialName);

        int wornParts = 0;
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (!armorStack.isEmpty() && armorStack.getItem() instanceof ArmorItem armorItem) {
                ResourceLocation mat = BuiltInRegistries.ARMOR_MATERIAL.getKey(armorItem.getMaterial().value());
                if (mat != null && mat.getPath().equals(materialName)) {
                    wornParts++;
                }
            }
        }

        if (data != null) {
            ArmorSetData.PartData partData = data.getParts().get(wornParts + "Part");

            if (partData != null && partData.getEffects() != null) {
                for (ArmorSetData.EffectData effect : partData.getEffects()) {
                    MobEffect mobEffect = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse(effect.getEffect()));
                    Component effectName = Component.translatable(mobEffect.getDisplayName().getString());
                    int level = effect.getAmplifier() + 1;
                    Component levelRoman = Component.translatable("enchantment.level." +level);
                    tooltip.add(Component.literal("- ")
                            .append(effectName)
                            .append(" ")
                            .append(levelRoman)
                            .withStyle(ChatFormatting.DARK_PURPLE));
                }
            }

            if (partData != null && partData.getAttributes() != null) {
                tooltip.add(Component.literal("§b[Bonus-Attributes]").withStyle(ChatFormatting.AQUA));
                for (Map.Entry<String, ArmorSetData.AttributeData> attr : partData.getAttributes().entrySet()) {

                    String key = attr.getKey();
                    ArmorSetData.AttributeData attributeData = attr.getValue();
                    Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.parse(attr.getKey()));
                    if (attribute == null) continue;
                    Component attributeName = Component.translatable(attribute.getDescriptionId());
                    double rawvalue = attr.getValue().getValue();
                    String modifier = attr.getValue().getModifier();
                    String displayValue;

                    if(modifier.equalsIgnoreCase("multiply")||modifier.equalsIgnoreCase("multiply_base")||modifier.equalsIgnoreCase("multiply_total")){

                        displayValue = String.format("+%.0f%%", rawvalue*100);

                    }else{
                        displayValue = (rawvalue == (long)rawvalue)
                                ?String.format("+%d", (long)rawvalue)
                                :String.format("+%.2f", rawvalue);
                    }


                    tooltip.add(attributeName.copy()
                            .append(" ")
                            .append(Component.literal(displayValue))
                            .withStyle(ChatFormatting.GREEN));
                }
            }
        }
    }
}

