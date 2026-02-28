package com.gilfort.zauberei.item.armor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

public class MagicclothArmorItemArs extends MagicclothArmorItem {

    public MagicclothArmorItemArs(Type slot, Properties settings) {
        super(slot, settings);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level,
                              @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        // Delegiere Perk-Ticking an Compat-Klasse, NUR wenn AN geladen
        if (!level.isClientSide() && entity instanceof LivingEntity living
                && ModList.get().isLoaded("ars_nouveau")) {
            com.gilfort.zauberei.compat.ArsNouveauPerkHelper
                    .tickPerks(stack, level, living, slotId);
        }
    }

    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        var modifiers = super.getDefaultAttributeModifiers(stack);
        // Delegiere Perk-Attribute an Compat-Klasse, NUR wenn AN geladen
        if (ModList.get().isLoaded("ars_nouveau")) {
            modifiers = com.gilfort.zauberei.compat.ArsNouveauPerkHelper
                    .applyPerkModifiers(modifiers, stack, this.type);
        }
        return modifiers;
    }
}
