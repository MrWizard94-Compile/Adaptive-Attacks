package com.adaptiveattacks.attack.generic.rush;

import com.adaptiveattacks.ability.BossAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * RUSH ability: fires a particle chain and pulls the most distant player
 * 15 blocks toward the boss.
 *
 * <p>Only triggers when at least one player is farther than 15 blocks away.
 * Activates in the AGGRESSOR archetype.
 */
public final class KineticHarpoonAbility implements BossAbility {

    private static final int COOLDOWN = 200;
    private static final double PULL_SPEED = 1.5;
    private static final double MIN_DIST_SQ = 15.0 * 15.0;
    private static final int PARTICLE_STEPS = 8;

    @Override
    @NotNull
    public String id() {
        return "kinetic_harpoon";
    }

    @Override
    public boolean canActivate(
            @NotNull final Mob boss,
            @NotNull final BossArchetype archetype,
            final float healthPct) {
        return archetype == BossArchetype.AGGRESSOR;
    }

    @Override
    public void onActivate(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers) {
        if (activePlayers.isEmpty()) {
            return;
        }
        final ServerPlayer target = findFurthest(boss, activePlayers);
        if (target == null || boss.distanceToSqr(target) < MIN_DIST_SQ) {
            return;
        }
        final Vec3 dir = boss.position().subtract(target.position()).normalize();
        target.setDeltaMovement(dir.scale(PULL_SPEED));
        target.hurtMarked = true;
        spawnChainParticles(level, boss.position(), target.position());
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN;
    }

    @Nullable
    private static ServerPlayer findFurthest(
            @NotNull final Mob boss,
            @NotNull final List<ServerPlayer> players) {
        ServerPlayer farthest = null;
        double maxDist = 0.0;
        for (final ServerPlayer player : players) {
            final double dist = boss.distanceToSqr(player);
            if (dist > maxDist) {
                maxDist = dist;
                farthest = player;
            }
        }
        return farthest;
    }

    private static void spawnChainParticles(
            @NotNull final ServerLevel level,
            @NotNull final Vec3 from,
            @NotNull final Vec3 to) {
        for (int i = 0; i <= PARTICLE_STEPS; i++) {
            final double t = (double) i / PARTICLE_STEPS;
            level.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    from.x + (to.x - from.x) * t,
                    from.y + (to.y - from.y) * t + 1.0,
                    from.z + (to.z - from.z) * t,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
