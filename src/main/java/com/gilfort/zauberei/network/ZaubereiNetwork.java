package com.gilfort.zauberei.network;

import com.gilfort.zauberei.Zauberei;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers the custom network payloads of the mod.
 * FML routes {@link RegisterPayloadHandlersEvent} (a mod bus event) to the mod bus automatically.
 */
@EventBusSubscriber(modid = Zauberei.MODID)
public class ZaubereiNetwork {

    /**
     * Registers all payload types and their handlers.
     *
     * @param event the payload registration event
     */
    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                LetterButtonPayload.TYPE,
                LetterButtonPayload.STREAM_CODEC,
                ServerPayloadHandler::handleIntroductionLetter
        );
    }
}
