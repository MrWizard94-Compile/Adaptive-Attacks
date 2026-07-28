package com.adaptiveattacks.attack.generic.hybrid;

import com.adaptiveattacks.ability.BossAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * HYBRID ability: links two players with an energy tether for 10 seconds.
 * If the linked players move more than 10 blocks apart, both take continuous
 * lightning damage every half-second.
 *
 * <p>A particle chain visualises the link each tick. Activates in the
 * UNPREDICTABLE archetype when at least two players are present.
 */
public final class MagneticLinkAbility implements BossAbility {

    private static final int COOLDOWN = 300;
    private static final int LINK_DURATION = 200;
    private static final double MAX_DIST = 10.0;
    private static final float LINK_DAMAGE = 3.0f;
    private static final int DAMAGE_INTERVAL = 10;
    private static final int PARTICLE_STEPS = 6;

    @Nullable
    private UUID linked1;
    @Nullable
    private UUID linked2;
    private int linkDuration = -1;

    @Override
    @NotNull
    public String id() {
        return "magnetic_link";
    }

    @Override
    public boolean canActivate(
            @NotNull final Mob boss,
            @NotNull final BossArchetype archetype,
            final float healthPct) {
        return archetype == BossArchetype.UNPREDICTABLE && linked1 == null;
    }

    @Override
    public void onActivate(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers) {
        if (activePlayers.size() < 2) {
            return;
        }
        linked1 = activePlayers.get(0).getUUID();
        linked2 = activePlayers.get(1).getUUID();
        linkDuration = LINK_DURATION;
    }

    @Override
    public void onTick(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers,
            final int currentTick) {
        if (linked1 == null || linked2 == null || linkDuration < 0) {
            return;
        }
        linkDuration--;
        if (linkDuration <= 0) {
            linked1 = null;
            linked2 = null;
            linkDuration = -1;
            return;
        }
        final ServerPlayer p1 = level.getServer().getPlayerList().getPlayer(linked1);
        final ServerPlayer p2 = level.getServer().getPlayerList().getPlayer(linked2);
        if (p1 == null || p2 == null) {
            linked1 = null;
            linked2 = null;
            linkDuration = -1;
            return;
        }
        spawnLinkParticles(level, p1, p2);
        if (p1.distanceTo(p2) > MAX_DIST && currentTick % DAMAGE_INTERVAL == 0) {
            p1.hurt(level.damageSources().magic(), LINK_DAMAGE);
            p2.hurt(level.damageSources().magic(), LINK_DAMAGE);
        }
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN;
    }

    private static void spawnLinkParticles(
            @NotNull final ServerLevel level,
            @NotNull final ServerPlayer p1,
            @NotNull final ServerPlayer p2) {
        for (int i = 1; i < PARTICLE_STEPS; i++) {
            final double t = (double) i / PARTICLE_STEPS;
            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    p1.getX() + (p2.getX() - p1.getX()) * t,
                    p1.getY() + (p2.getY() - p1.getY()) * t + 1.0,
                    p1.getZ() + (p2.getZ() - p1.getZ()) * t,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
