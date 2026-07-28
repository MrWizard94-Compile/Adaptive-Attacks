package com.adaptiveattacks.attack.generic.rush;

import com.adaptiveattacks.ability.BossAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * RUSH ability: grants the boss invisibility for 2 seconds then teleports it
 * directly behind the most distant player.
 *
 * <p>The teleport fires 40 ticks (2 s) after activation, coordinated via
 * {@link #onTick}. Only one shadow step can be in progress at a time.
 */
public final class ShadowStepAbility implements BossAbility {

    private static final int COOLDOWN = 300;
    private static final int DELAY_TICKS = 40;
    private static final double BEHIND_DIST = 2.0;

    private int countdown = -1;
    @Nullable
    private ServerPlayer pendingTarget;

    @Override
    @NotNull
    public String id() {
        return "shadow_step";
    }

    @Override
    public boolean canActivate(
            @NotNull final Mob boss,
            @NotNull final BossArchetype archetype,
            final float healthPct) {
        return archetype == BossArchetype.AGGRESSOR && countdown < 0;
    }

    @Override
    public void onActivate(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers) {
        if (activePlayers.isEmpty()) {
            return;
        }
        pendingTarget = findFurthest(boss, activePlayers);
        if (pendingTarget == null) {
            return;
        }
        boss.addEffect(
                new MobEffectInstance(MobEffects.INVISIBILITY, DELAY_TICKS + 5, 0, false, false));
        countdown = DELAY_TICKS;
    }

    @Override
    public void onTick(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers,
            final int currentTick) {
        if (countdown < 0) {
            return;
        }
        countdown--;
        if (countdown == 0 && pendingTarget != null) {
            final Vec3 look = pendingTarget.getLookAngle();
            final Vec3 dest = pendingTarget.position().subtract(look.scale(BEHIND_DIST));
            boss.teleportTo(dest.x, dest.y, dest.z);
            pendingTarget = null;
            countdown = -1;
        }
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
}
