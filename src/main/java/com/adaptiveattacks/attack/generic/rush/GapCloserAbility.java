package com.adaptiveattacks.attack.generic.rush;

import com.adaptiveattacks.ability.BossAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * RUSH ability: launches the boss in a physics-based leap toward the nearest player,
 * rapidly closing the gap.
 *
 * <p>Activates in the AGGRESSOR archetype.
 */
public final class GapCloserAbility implements BossAbility {

    private static final int COOLDOWN = 160;
    private static final double LEAP_HORIZONTAL = 1.5;
    private static final double LEAP_VERTICAL = 0.7;

    @Override
    @NotNull
    public String id() {
        return "gap_closer";
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
        final ServerPlayer target = findNearest(boss, activePlayers);
        if (target == null) {
            return;
        }
        final Vec3 dir = target.position().subtract(boss.position()).normalize();
        boss.setDeltaMovement(dir.x * LEAP_HORIZONTAL, LEAP_VERTICAL, dir.z * LEAP_HORIZONTAL);
        boss.hurtMarked = true;
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN;
    }

    @org.jetbrains.annotations.Nullable
    private static ServerPlayer findNearest(
            @NotNull final Mob boss,
            @NotNull final List<ServerPlayer> players) {
        ServerPlayer nearest = null;
        double minDist = Double.MAX_VALUE;
        for (final ServerPlayer player : players) {
            final double dist = boss.distanceToSqr(player);
            if (dist < minDist) {
                minDist = dist;
                nearest = player;
            }
        }
        return nearest;
    }
}
