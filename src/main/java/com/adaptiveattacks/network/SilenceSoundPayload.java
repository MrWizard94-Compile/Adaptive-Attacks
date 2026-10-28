package com.adaptiveattacks.network;

import com.adaptiveattacks.AdaptiveAttacksMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Clientbound payload that mutes local sound playback for a duration
 * (Warden {@code the_silence} ultimate).
 *
 * @param durationTicks how long client-side silence lasts
 */
public record SilenceSoundPayload(int durationTicks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SilenceSoundPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(AdaptiveAttacksMod.MOD_ID, "silence_sound"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SilenceSoundPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    SilenceSoundPayload::durationTicks,
                    SilenceSoundPayload::new);

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Applies silence on the logical client only. Uses reflective invoke so the
     * client-only {@code SilenceClientState} class is never loaded on a dedicated server.
     *
     * @param payload the silence duration payload
     * @param context payload context
     */
    public static void handle(
            @NotNull final SilenceSoundPayload payload,
            @NotNull final IPayloadContext context) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        final int ticks = payload.durationTicks();
        context.enqueueWork(() -> invokeClientActivate(ticks));
    }

    private static void invokeClientActivate(final int durationTicks) {
        try {
            final Class<?> state = Class.forName("com.adaptiveattacks.client.SilenceClientState");
            state.getMethod("activate", int.class).invoke(null, durationTicks);
        } catch (final ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to activate client silence state", ex);
        }
    }
}
