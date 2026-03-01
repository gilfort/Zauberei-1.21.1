package com.gilfort.zauberei.network;

import com.gilfort.zauberei.Zauberei;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkSetup {

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                OpenSetsGuiPayload.TYPE,
                OpenSetsGuiPayload.STREAM_CODEC,
                // Lambda statt Methoden-Referenz → ClientPayloadHandler wird erst
                // geladen, wenn das Packet tatsächlich auf dem Client ankommt
                (payload, context) -> ClientPayloadHandler.handleOpenSetsGui(payload, context)
        );
    }
}
