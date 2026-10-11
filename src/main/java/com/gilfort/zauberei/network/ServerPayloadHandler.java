package com.gilfort.zauberei.network;

import com.gilfort.zauberei.Config;
import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.item.ZaubereiItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.gilfort.zauberei.util.ZaubereiUtils.giveOrDrop;

/**
 * Handles payloads received on the server side.
 */
public class ServerPayloadHandler {

    /**
     * Consumes the Introduction Letter held by the player and hands out the configured starting items.
     * Does nothing if the player holds no letter in either hand.
     *
     * @param data    the (empty) payload
     * @param context the payload context
     */
    public static void handleIntroductionLetter(LetterButtonPayload data, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();

        InteractionHand hand;
        if (player.getMainHandItem().is(ZaubereiItems.INTRODUCTIONLETTER.get())) {
            hand = InteractionHand.MAIN_HAND;
        } else if (player.getOffhandItem().is(ZaubereiItems.INTRODUCTIONLETTER.get())) {
            hand = InteractionHand.OFF_HAND;
        } else {
            Zauberei.LOGGER.warn("[Zauberei] {} sent a letter payload without holding an Introduction Letter.",
                    player.getGameProfile().getName());
            return;
        }
        player.setItemInHand(hand, ItemStack.EMPTY);

        for (ItemStack startingItem : Config.getLetterItems()) {
            giveOrDrop(player, startingItem);
        }

        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
