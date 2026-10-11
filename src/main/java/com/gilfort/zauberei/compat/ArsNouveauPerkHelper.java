package com.gilfort.zauberei.compat;

import com.hollingsworth.arsnouveau.api.perk.ITickablePerk;
import com.hollingsworth.arsnouveau.api.perk.PerkInstance;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.common.perk.RepairingPerk;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Ars Nouveau perk logic for {@link com.gilfort.zauberei.item.armor.MagicclothArmorItemArs}.
 * Kept in its own class so Ars Nouveau classes are only loaded when the mod is present.
 */
public class ArsNouveauPerkHelper {

    /**
     * Ticks repairing and all tickable perks of an armor stack while it is worn.
     *
     * @param stack  the armor stack
     * @param level  the level of the wearer
     * @param entity the wearer
     * @param slotId the inventory slot index of the stack
     */
    public static void tickPerks(ItemStack stack, Level level, LivingEntity entity, int slotId) {
        // Only tick while the item sits in one of the four armor slots.
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

    /**
     * Appends the Ars Nouveau perk tooltip of the stack.
     *
     * @param stack   the armor stack
     * @param tooltip the tooltip lines to extend
     */
    public static void appendPerkTooltip(ItemStack stack, List<Component> tooltip) {
        var data = stack.get(DataComponentRegistry.ARMOR_PERKS);
        if (data != null) {
            data.appendPerkTooltip(tooltip, stack);
        }
    }

    /**
     * Applies the attribute modifiers of all perks on the stack.
     *
     * @param modifiers the default modifiers of the item
     * @param stack     the armor stack
     * @param type      the armor type of the item
     * @return the modifiers including all perk modifiers
     */
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
        return modifiers;
    }
}
