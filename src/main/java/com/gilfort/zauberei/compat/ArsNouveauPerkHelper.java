package com.gilfort.zauberei.compat;

import com.hollingsworth.arsnouveau.api.perk.ITickablePerk;
import com.hollingsworth.arsnouveau.api.perk.PerkInstance;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.common.perk.RepairingPerk;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

public class ArsNouveauPerkHelper {

    public static void tickPerks(ItemStack stack, Level level, LivingEntity entity, int slotId) {
        // Nur ticken wenn das Item in einem Armor-Slot liegt
        if (slotId < Inventory.INVENTORY_SIZE || slotId >= Inventory.INVENTORY_SIZE + 4) return;

        RepairingPerk.attemptRepair(stack, entity);
        var perkHolder = PerkUtil.getPerkHolder(stack);
        if (perkHolder == null) return;

        for (PerkInstance instance : perkHolder.getPerkInstances(stack)) {
            if (instance.getPerk() instanceof ITickablePerk tickable) {
                tickable.tick(stack, level, entity, instance);
            }
        }
    }

    public static ItemAttributeModifiers applyPerkModifiers(
            ItemAttributeModifiers modifiers, ItemStack stack, ArmorItem.Type type) {
        var perkHolder = PerkUtil.getPerkHolder(stack);
        if (perkHolder == null) return modifiers;

        for (PerkInstance instance : perkHolder.getPerkInstances(stack)) {
            modifiers = instance.getPerk().applyAttributeModifiers(
                    modifiers, stack, instance.getSlot().value(),
                    EquipmentSlotGroup.bySlot(type.getSlot())
            );
        }
        // Optional: Hier könntest du auch eigene Zauberei-spezifische
        // Attribute hinzufügen, analog zu AN's Mana-Bonus
        return modifiers;
    }
}

