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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MagicclothArmorItem extends ExtendedArmorItem {

    public MagicclothArmorItem(Type slot, Properties settings) {
        super(ZaubereiArmorMaterials.MAGICCLOTH, slot, settings);
    }

    public boolean isAlt() {
        return this.equals(ZaubereiItems.MAGICCLOTH_HELMET_ALT.get())
                || this.equals(ZaubereiItems.MAGICCLOTH_CHESTPLATE_ALT.get())
                || this.equals(ZaubereiItems.MAGICCLOTH_LEGGINGS_ALT.get())
                || this.equals(ZaubereiItems.MAGICCLOTH_BOOTS_ALT.get());
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
        if (player == null) return;

        var majorType = ComponentRegistry.MAJOR.value();
        var yearType = ComponentRegistry.YEAR.value();

        if (!stack.has(majorType) || !stack.has(yearType)) {
            tooltip.add(Component.literal("§7(No set data yet)"));
            return;
        }

        String major = stack.get(majorType);
        Integer yearObj = stack.get(yearType);
        if (major == null || yearObj == null) {
            tooltip.add(Component.literal("§7(No set data yet)"));
            return;
        }
        int year = yearObj;

        Set<String> registeredTags = ArmorSetDataRegistry.getRegisteredTags(major.toLowerCase(), year);
        if (registeredTags.isEmpty()) {
            tooltip.add(Component.literal("§7(No set definitions found)"));
            return;
        }

        boolean printedAny = false;

        for (String tagString : registeredTags) {
            ResourceLocation tagLoc;
            try {
                tagLoc = ResourceLocation.parse(tagString);
            } catch (Exception e) {
                continue;
            }

            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagLoc);

            // Only show sets this item belongs to
            if (!stack.is(tagKey)) {
                continue;
            }

            // Count worn parts for this tag
            int wornParts = 0;
            for (ItemStack armorStack : player.getArmorSlots()) {
                if (!armorStack.isEmpty() && armorStack.is(tagKey)) {
                    wornParts++;
                }
            }

            ArmorSetData data = ArmorSetDataRegistry.getData(major.toLowerCase(), year, tagString);
            if (data == null || data.getParts() == null) continue;

            ArmorSetData.PartData partData = data.getParts().get(wornParts + "Part");
            if (partData == null) {
                // allow only 4Part definitions etc.
                continue;
            }

            printedAny = true;

            // Effects
            if (partData.getEffects() != null && !partData.getEffects().isEmpty()) {
                for (ArmorSetData.EffectData effect : partData.getEffects()) {
                    ResourceLocation effectLoc;
                    try {
                        effectLoc = ResourceLocation.parse(effect.getEffect());
                    } catch (Exception e) {
                        continue;
                    }

                    MobEffect mobEffect = BuiltInRegistries.MOB_EFFECT.get(effectLoc);
                    if (mobEffect == null) continue;

                    Component effectName = mobEffect.getDisplayName();
                    int level = effect.getAmplifier() + 1;
                    Component levelRoman = Component.translatable("enchantment.level." + level);

                    tooltip.add(Component.literal("- ")
                            .append(effectName)
                            .append(" ")
                            .append(levelRoman)
                            .withStyle(ChatFormatting.DARK_PURPLE));
                }
            }

            // Attributes
            if (partData.getAttributes() != null && !partData.getAttributes().isEmpty()) {
                tooltip.add(Component.literal("§b[Bonus-Attributes]").withStyle(ChatFormatting.AQUA));

                for (Map.Entry<String, ArmorSetData.AttributeData> attr : partData.getAttributes().entrySet()) {
                    ResourceLocation attrLoc;
                    try {
                        attrLoc = ResourceLocation.parse(attr.getKey());
                    } catch (Exception e) {
                        continue;
                    }

                    Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(attrLoc);
                    if (attribute == null) continue;

                    Component attributeName = Component.translatable(attribute.getDescriptionId());

                    double rawValue = attr.getValue().getValue();
                    String modifier = attr.getValue().getModifier();

                    String displayValue;
                    if (modifier != null && (modifier.equalsIgnoreCase("multiply")
                            || modifier.equalsIgnoreCase("multiply_base")
                            || modifier.equalsIgnoreCase("multiply_total"))) {
                        displayValue = String.format("+%.0f%%", rawValue * 100);
                    } else {
                        displayValue = (rawValue == (long) rawValue)
                                ? String.format("+%d", (long) rawValue)
                                : String.format("+%.2f", rawValue);
                    }

                    tooltip.add(attributeName.copy()
                            .append(" ")
                            .append(Component.literal(displayValue))
                            .withStyle(ChatFormatting.GREEN));
                }
            }
        }

        if (!printedAny) {
            tooltip.add(Component.literal("§7(No matching set for this item)"));
        }
    }
}
