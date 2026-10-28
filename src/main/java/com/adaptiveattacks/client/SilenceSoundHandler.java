package com.adaptiveattacks.client;

import com.adaptiveattacks.AdaptiveAttacksMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Client event hooks for Warden silence audio muting.
 */
@EventBusSubscriber(modid = AdaptiveAttacksMod.MOD_ID, value = Dist.CLIENT)
public final class SilenceSoundHandler {

    private SilenceSoundHandler() {
        // utility
    }

    /**
     * Blocks non-MASTER sounds while silence is active.
     *
     * @param event play-sound event
     */
    @SubscribeEvent
    public static void onPlaySound(@NotNull final PlaySoundEvent event) {
        event.setSound(SilenceClientState.filter(event.getSound()));
    }

    /**
     * Advances the silence countdown each client tick.
     *
     * @param event client tick event
     */
    @SubscribeEvent
    public static void onClientTick(@NotNull final ClientTickEvent.Post event) {
        SilenceClientState.tick();
    }
}
