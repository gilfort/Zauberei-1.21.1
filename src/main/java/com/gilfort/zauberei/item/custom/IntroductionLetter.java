package com.gilfort.zauberei.item.custom;

import com.gilfort.zauberei.client.ClientHooks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

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
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            ClientHooks.openLetterScreen();
        }
        return InteractionResult.CONSUME;
    }
}
