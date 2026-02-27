package com.gilfort.zauberei.item.armor;

import com.gilfort.zauberei.component.ComponentRegistry;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Map;
import java.util.Set;

/**
 * Universal tooltip handler for the armor set effect system.
 * Uses NeoForge's ItemTooltipEvent to display set bonuses on ANY ArmorItem,
 * not just MagicclothArmorItem. This enables tooltips on vanilla and modded armor.
 *
 * Only shows tooltip content if the item actually belongs to a registered set tag.
 * Items with no set definition receive no tooltip additions at all.
 *
 * @see ArmorEffects — writes MAJOR/YEAR DataComponents to all worn ArmorItems
 * @see ArmorSetDataRegistry — provides set definitions per major/year/tag
 * @see <a href="https://github.com/gilfort/Zauberei-1.21.1/issues/16">Issue #16</a>
 */
@OnlyIn(Dist.CLIENT)
public class ArmorSetTooltipHandler {

    /**
     * Register this handler on the NeoForge event bus.
     * Call from ClientModEvents.onClientSetup().
     */
    public static void register() {
        NeoForge.EVENT_BUS.addListener(ArmorSetTooltipHandler::onItemTooltip);
    }

    /**
     * Fires for every item tooltip.
     * Guards:
     *   1. Only ArmorItems
     *   2. Only items that belong to at least one registered set tag
     *   3. SHIFT-to-reveal
     */
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        // Guard 1: Only ArmorItems
        if (!(stack.getItem() instanceof ArmorItem)) return;

        // Guard 2: Does this item belong to ANY registered set tag?
        // If not → show absolutely nothing, don't pollute the tooltip.
        if (!ArmorSetDataRegistry.isItemInAnyRegisteredTag(stack)) return;

        // From here on we know: this item has a potential set bonus.

        // Guard 3: SHIFT-to-reveal
        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(Component.literal("[SHIFT to view Set Bonus]")
                    .withStyle(ChatFormatting.AQUA));
            return;
        }

        // --- Determine Major and Year ---
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        var majorType = ComponentRegistry.MAJOR.value();
        var yearType = ComponentRegistry.YEAR.value();

        String major = stack.has(majorType) ? stack.get(majorType) : null;
        Integer yearObj = stack.has(yearType) ? stack.get(yearType) : null;

        // Fallback: read from currently worn armor stacks
        // (ArmorEffects writes MAJOR/YEAR to all worn ArmorItems every 60 ticks)
        if (major == null || yearObj == null) {
            for (ItemStack worn : player.getArmorSlots()) {
                if (worn.has(majorType) && worn.has(yearType)) {
                    major = worn.get(majorType);
                    yearObj = worn.get(yearType);
                    break;
                }
            }
        }

        // Still no data: player is wearing nothing at all → generic hint
        if (major == null || yearObj == null) {
            event.getToolTip().add(Component.literal("[Set Bonus available — equip to see details]")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        int year = yearObj;

        // --- Lookup registered tags for this major/year ---
        Set<String> registeredTags = ArmorSetDataRegistry.getRegisteredTags(major.toLowerCase(), year);
        if (registeredTags.isEmpty()) {
            // Item is in a tag globally, but not for this player's major/year
            event.getToolTip().add(Component.literal("[No set bonus for your current Major]")
                    .withStyle(ChatFormatting.GRAY));
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
            if (!stack.is(tagKey)) continue;

            // Count worn pieces
            int wornParts = 0;
            for (ItemStack armorStack : player.getArmorSlots()) {
                if (!armorStack.isEmpty() && armorStack.is(tagKey)) {
                    wornParts++;
                }
            }

            ArmorSetData data = ArmorSetDataRegistry.getData(major.toLowerCase(), year, tagString);
            if (data == null || data.getParts() == null) continue;

            // Determine the maximum part threshold defined in the set
            int maxParts = data.getParts().keySet().stream()
                    .map(k -> k.replace("Part", ""))
                    .mapToInt(s -> {
                        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
                    })
                    .max()
                    .orElse(0);

            // Header with progress: [Set Bonus 2/4]
            event.getToolTip().add(Component.literal("[Set Bonus " + wornParts + "/" + maxParts + "]")
                    .withStyle(ChatFormatting.AQUA));

            ArmorSetData.PartData partData = data.getParts().get(wornParts + "Part");

            // No bonus for this exact count → show next threshold hint
            if (partData == null) {
                int nextThreshold = -1;
                for (int i = wornParts + 1; i <= maxParts; i++) {
                    if (data.getParts().containsKey(i + "Part")) {
                        nextThreshold = i;
                        break;
                    }
                }
                if (nextThreshold > 0) {
                    event.getToolTip().add(Component.literal(
                                    "  Equip " + (nextThreshold - wornParts) + " more piece(s) for a bonus")
                            .withStyle(ChatFormatting.GRAY));
                }
                printedAny = true;
                continue;
            }

            printedAny = true;


            // --- Render Effects ---
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

                    event.getToolTip().add(Component.literal("- ")
                            .append(effectName)
                            .append(" ")
                            .append(levelRoman)
                            .withStyle(ChatFormatting.DARK_PURPLE));
                }
            }

            // --- Render Attributes ---
            if (partData.getAttributes() != null && !partData.getAttributes().isEmpty()) {
                event.getToolTip().add(Component.literal("[Bonus Attributes]")
                        .withStyle(ChatFormatting.AQUA));

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

                    event.getToolTip().add(attributeName.copy()
                            .append(" ")
                            .append(Component.literal(displayValue))
                            .withStyle(ChatFormatting.GREEN));
                }
            }
        }

        if (!printedAny) {
            // Item is in a tag for this major/year, but player wears 0 matching pieces
            event.getToolTip().add(Component.literal("(Equip pieces to activate set bonus)")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
