package com.gilfort.zauberei.network;

import com.gilfort.zauberei.Zauberei;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * Empty client → server payload sent when the player tears the Introduction Letter.
 * The server validates that the player actually holds a letter.
 */
public record LetterButtonPayload() implements CustomPacketPayload {

    /** Payload type id. */
    public static final CustomPacketPayload.Type<LetterButtonPayload> TYPE =
            new CustomPacketPayload.Type<>(Zauberei.id("letterbuttonpayload"));

    /** Codec of the payload; it carries no data. */
    public static final StreamCodec<ByteBuf, LetterButtonPayload> STREAM_CODEC =
            StreamCodec.unit(new LetterButtonPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
