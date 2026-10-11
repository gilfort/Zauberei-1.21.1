package com.gilfort.zauberei.item.armor;

import com.gilfort.zauberei.compat.ArsNouveauPerkHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Magiccloth armor piece with Ars Nouveau perk slots. Only registered when Ars Nouveau is loaded.
 */
public class MagicclothArmorItemArs extends MagicclothArmorItem {

    /**
     * Creates a perk-enabled Magiccloth armor piece.
     *
     * @param slot     the armor type
     * @param settings the item properties
     */
    public MagicclothArmorItemArs(Type slot, Properties settings) {
        super(slot, settings);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level,
                              @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        // Perk ticking lives in the compat class so Ars Nouveau classes load only when present.
        if (!level.isClientSide() && entity instanceof LivingEntity living
                && ModList.get().isLoaded("ars_nouveau")) {
            ArsNouveauPerkHelper.tickPerks(stack, level, living, slotId);
        }
    }

    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        var modifiers = super.getDefaultAttributeModifiers(stack);
        if (ModList.get().isLoaded("ars_nouveau")) {
            modifiers = ArsNouveauPerkHelper.applyPerkModifiers(modifiers, stack, this.type);
        }
        return modifiers;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (ModList.get().isLoaded("ars_nouveau")) {
            ArsNouveauPerkHelper.appendPerkTooltip(stack, tooltip);
        }
    }
}
