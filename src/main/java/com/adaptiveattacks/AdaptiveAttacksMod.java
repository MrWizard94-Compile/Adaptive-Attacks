package com.adaptiveattacks;

import com.adaptiveattacks.event.AttackLifecycleHandler;
import com.adaptiveattacks.event.AttackTickHandler;
import com.adaptiveattacks.network.SilenceSoundPayload;
import com.adaptiveboss.registry.AdaptiveBossRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Entry point for the Adaptive Attacks addon mod.
 *
 * <p>Registers the Warden as a tracked boss (also registered by Adaptive Boss 1.1+)
 * and wires the ability system plus silence networking.
 */
@Mod("adaptiveattacks")
public final class AdaptiveAttacksMod {

    /** Mod ID constant. */
    public static final String MOD_ID = "adaptiveattacks";

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Initialises the addon, registers the Warden boss, and wires event handlers.
     *
     * @param modEventBus the mod-lifecycle event bus
     * @param container   the mod container
     */
    public AdaptiveAttacksMod(
            @NotNull final IEventBus modEventBus,
            @NotNull final ModContainer container) {
        AdaptiveBossRegistry.registerBoss(EntityType.WARDEN);

        modEventBus.addListener(this::registerPayloads);

        NeoForge.EVENT_BUS.register(new AttackLifecycleHandler());
        NeoForge.EVENT_BUS.register(new AttackTickHandler());

        LOGGER.info("[AdaptiveAttacks] Initialised. Warden registered; silence payload ready.");
    }

    private void registerPayloads(@NotNull final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                SilenceSoundPayload.TYPE,
                SilenceSoundPayload.STREAM_CODEC,
                SilenceSoundPayload::handle);
    }
}
