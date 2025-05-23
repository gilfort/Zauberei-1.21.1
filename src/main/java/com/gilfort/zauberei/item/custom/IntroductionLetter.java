package com.gilfort.zauberei.item.custom;

import com.gilfort.zauberei.guis.LetterGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class IntroductionLetter extends Item {
    public IntroductionLetter(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            Minecraft.getInstance().setScreen(new LetterGUI(player.getItemInHand(hand)));
        }
    return InteractionResultHolder.consume(player.getItemInHand(hand));
    }
}
