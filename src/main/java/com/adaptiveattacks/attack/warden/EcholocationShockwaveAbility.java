package com.adaptiveattacks.attack.warden;

import com.adaptiveattacks.ability.BossAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

/**
 * Warden ability: releases a visible sonar pulse. Any player who is moving
 * (velocity above threshold) when the pulse hits them is Tagged with Glowing.
 *
 * <p>Tagged player UUIDs are stored in {@link #TAGGED_PLAYERS} with a 10-tick
 * countdown. {@link EchoStrikeAbility} reads this map to fire delayed explosions.
 */
public final class EcholocationShockwaveAbility implements BossAbility {

    private static final int COOLDOWN = 160;
    private static final double PULSE_RADIUS = 20.0;
    private static final double MOVE_THRESHOLD_SQ = 0.07 * 0.07;
    private static final int GLOW_DURATION = 60;
    /** Ticks after tagging before EchoStrike fires. */
    static final int ECHO_STRIKE_DELAY = 10;

    /** Tagged player UUID → ticks remaining until EchoStrike fires. */
    static final Map<UUID, Integer> TAGGED_PLAYERS = new ConcurrentHashMap<>();

    /**
     * Removes all tags for a boss's targets (called on boss removal).
     * Tags are player-scoped, so they are cleared globally on any boss removal.
     * In practice only one Warden is tracked at a time.
     */
    public static void clearAllTags() {
        TAGGED_PLAYERS.clear();
    }

    @Override
    @NotNull
    public String id() {
        return "echolocation_shockwave";
    }

    @Override
    public boolean canActivate(
            @NotNull final Mob boss,
            @NotNull final BossArchetype archetype,
            final float healthPct) {
        return true;
    }

    @Override
    public void onActivate(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers) {
        spawnPulse(level, boss);
        for (final ServerPlayer player : activePlayers) {
            if (boss.distanceTo(player) > PULSE_RADIUS) {
                continue;
            }
            if (player.getDeltaMovement().lengthSqr() > MOVE_THRESHOLD_SQ) {
                player.addEffect(
                        new MobEffectInstance(MobEffects.GLOWING, GLOW_DURATION, 0, false, true));
                TAGGED_PLAYERS.put(player.getUUID(), ECHO_STRIKE_DELAY);
            }
        }
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN;
    }

    private static void spawnPulse(
            @NotNull final ServerLevel level,
            @NotNull final Mob boss) {
        final int rings = 12;
        final double radius = 6.0;
        for (int i = 0; i < rings; i++) {
            final double angle = (2.0 * Math.PI * i) / rings;
            level.sendParticles(
                    ParticleTypes.SONIC_BOOM,
                    boss.getX() + Math.cos(angle) * radius,
                    boss.getY() + 1.0,
                    boss.getZ() + Math.sin(angle) * radius,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
