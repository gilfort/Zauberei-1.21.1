package com.gilfort.zauberei.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

public class WandItem extends Item {
    public WandItem(Properties properties) {
        super(properties);
    }


    //added tooltip for item
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltipp.zauberei.wand"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
