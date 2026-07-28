package com.adaptiveattacks.attack.generic.kite;

import com.adaptiveattacks.ability.BossAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * KITE ability: emits a 360° shockwave that flings all players within 12 blocks
 * away from the boss.
 *
 * <p>Activates in the KITER archetype.
 */
public final class RepulsionNovaAbility implements BossAbility {

    private static final int COOLDOWN = 180;
    private static final double RADIUS = 12.0;
    private static final double PUSH_FORCE = 2.2;
    private static final double PUSH_VERTICAL = 0.5;

    @Override
    @NotNull
    public String id() {
        return "repulsion_nova";
    }

    @Override
    public boolean canActivate(
            @NotNull final Mob boss,
            @NotNull final BossArchetype archetype,
            final float healthPct) {
        return archetype == BossArchetype.KITER;
    }

    @Override
    public void onActivate(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers) {
        level.sendParticles(
                ParticleTypes.EXPLOSION_EMITTER,
                boss.getX(), boss.getY() + 1.0, boss.getZ(),
                1, 0.0, 0.0, 0.0, 0.0);

        for (final ServerPlayer player : activePlayers) {
            if (boss.distanceTo(player) > RADIUS) {
                continue;
            }
            final Vec3 dir = player.position().subtract(boss.position()).normalize();
            player.push(dir.x * PUSH_FORCE, PUSH_VERTICAL, dir.z * PUSH_FORCE);
            player.hurtMarked = true;
        }
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN;
    }
}
