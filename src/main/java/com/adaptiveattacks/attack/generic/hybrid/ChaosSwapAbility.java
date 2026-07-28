package com.adaptiveattacks.attack.generic.hybrid;

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
 * HYBRID ability: instantly swaps the positions of two random active players,
 * completely disorienting the team's formation.
 *
 * <p>Activates in the UNPREDICTABLE archetype when at least two players are present.
 */
public final class ChaosSwapAbility implements BossAbility {

    private static final int COOLDOWN = 260;

    @Override
    @NotNull
    public String id() {
        return "chaos_swap";
    }

    @Override
    public boolean canActivate(
            @NotNull final Mob boss,
            @NotNull final BossArchetype archetype,
            final float healthPct) {
        return archetype == BossArchetype.UNPREDICTABLE;
    }

    @Override
    public void onActivate(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers) {
        if (activePlayers.size() < 2) {
            return;
        }
        final ServerPlayer p1 = activePlayers.get(0);
        final ServerPlayer p2 = activePlayers.get(1);
        final Vec3 pos1 = p1.position();
        final Vec3 pos2 = p2.position();

        spawnSwapParticles(level, pos1);
        spawnSwapParticles(level, pos2);

        p1.teleportTo(pos2.x, pos2.y, pos2.z);
        p2.teleportTo(pos1.x, pos1.y, pos1.z);
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN;
    }

    private static void spawnSwapParticles(
            @NotNull final ServerLevel level,
            @NotNull final Vec3 pos) {
        level.sendParticles(
                ParticleTypes.PORTAL,
                pos.x, pos.y + 1.0, pos.z,
                30, 0.3, 0.5, 0.3, 0.1);
    }
}
