package com.adaptiveattacks;

import com.adaptiveattacks.event.AttackLifecycleHandler;
import com.adaptiveattacks.event.AttackTickHandler;
import com.adaptiveboss.registry.AdaptiveBossRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Entry point for the Adaptive Attacks addon mod.
 *
 * <p>Registers the Warden as a tracked boss (in addition to the base mod's
 * Wither and Ender Dragon defaults) and wires the ability system event handlers.
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

        NeoForge.EVENT_BUS.register(new AttackLifecycleHandler());
        NeoForge.EVENT_BUS.register(new AttackTickHandler());

        LOGGER.info("[AdaptiveAttacks] Initialised. Warden registered as adaptive boss.");
    }
}
