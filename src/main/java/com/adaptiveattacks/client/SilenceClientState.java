package com.adaptiveattacks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

/**
 * Client-side silence window for Warden {@code the_silence}.
 *
 * <p>While active, {@link SilenceSoundHandler} nulls outgoing {@link SoundInstance}s
 * so ambient / music / hostile audio is muted for the duration.
 */
@OnlyIn(Dist.CLIENT)
public final class SilenceClientState {

    private static int remainingTicks;

    private SilenceClientState() {
        // utility
    }

    /**
     * Starts or refreshes the silence window and stops currently playing sounds.
     *
     * @param durationTicks duration in client ticks
     */
    public static void activate(final int durationTicks) {
        remainingTicks = Math.max(remainingTicks, Math.max(0, durationTicks));
        stopPlayingSounds();
    }

    /** Clears silence immediately. */
    public static void clear() {
        remainingTicks = 0;
    }

    /** @return {@code true} while silence is active */
    public static boolean isActive() {
        return remainingTicks > 0;
    }

    /** Decrements the silence countdown by one tick. */
    public static void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    /** Stops currently playing category sounds (keeps UI/master usable). */
    public static void stopPlayingSounds() {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager() == null) {
            return;
        }
        for (final SoundSource source : SoundSource.values()) {
            if (source == SoundSource.MASTER) {
                continue;
            }
            mc.getSoundManager().stop(null, source);
        }
    }

    /**
     * @param sound candidate sound
     * @return the sound, or {@code null} when silence should block it
     */
    @Nullable
    public static SoundInstance filter(@Nullable final SoundInstance sound) {
        if (!isActive() || sound == null) {
            return sound;
        }
        if (sound.getSource() == SoundSource.MASTER) {
            return sound;
        }
        return null;
    }
}
