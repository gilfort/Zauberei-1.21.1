package com.gilfort.zauberei.component;

import com.gilfort.zauberei.Zauberei;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

/**
 * Registers the data component types of the mod.
 */
public class ComponentRegistry {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Zauberei.MODID);

    /**
     * Wires the component register to the mod event bus.
     *
     * @param eventBus the mod event bus
     */
    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(
            String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return COMPONENTS.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }

    /** Marks a Magiccloth armor piece that renders its alternative ("alt") bones. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> CLOTHING_ALT =
            register("transmog",
                    builder -> builder
                            .persistent(Unit.CODEC)
                            .networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
                            .cacheEncoding());

    /**
     * Legacy component {@code zauberei:major}, written onto worn armor by the removed set effect system.
     * Never written anymore; it stays registered only so that item stacks saved by older versions still
     * load — without it, every armor piece carrying it would be dropped when a world is loaded.
     *
     * @deprecated legacy data only, do not use
     */
    @Deprecated
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> LEGACY_MAJOR =
            register("major",
                    builder -> builder
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                            .cacheEncoding());

    /**
     * Legacy component {@code zauberei:year}, see {@link #LEGACY_MAJOR}.
     *
     * @deprecated legacy data only, do not use
     */
    @Deprecated
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> LEGACY_YEAR =
            register("year",
                    builder -> builder
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
                            .cacheEncoding());
}
