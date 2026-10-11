package com.gilfort.zauberei.item.custom;

import com.gilfort.zauberei.client.ClientHooks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * The Introduction Letter. Using it opens the letter screen; tearing it there grants the
 * starting items configured in {@code letter_items}.
 */
public class IntroductionLetter extends Item {

    /**
     * Creates the letter item.
     *
     * @param properties the item properties
     */
    public IntroductionLetter(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                           @NotNull InteractionHand hand) {
        if (level.isClientSide()) {
            ClientHooks.openLetterScreen();
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }
}
