package com.gilfort.zauberei.client;

import net.minecraft.client.Minecraft;

/**
 * Client-only entry points called from common code. Callers must make sure they run on the
 * client (e.g. behind {@code level.isClientSide()}), so this class is never loaded on a dedicated server.
 */
public final class ClientHooks {

    private ClientHooks() {
    }

    /** Opens the Introduction Letter screen. */
    public static void openLetterScreen() {
        Minecraft.getInstance().setScreen(new LetterScreen());
    }
}
