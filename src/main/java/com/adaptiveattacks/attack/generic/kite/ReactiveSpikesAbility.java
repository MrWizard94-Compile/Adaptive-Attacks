package com.adaptiveattacks.attack.generic.kite;

import com.adaptiveattacks.ability.BossAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

/**
 * KITE ability: grants the boss reactive spikes for 4 seconds. While active,
 * melee attackers take 25% thorns damage and strong knockback.
 *
 * <p>The {@link com.adaptiveattacks.event.AttackLifecycleHandler} reads
 * {@link #isActive(UUID)} to intercept incoming melee damage and reflect it.
 * Activates in the KITER archetype.
 */
public final class ReactiveSpikesAbility implements BossAbility {

    private static final int COOLDOWN = 200;
    private static final int DURATION = 80;

    private static final Set<UUID> ACTIVE_BOSSES = ConcurrentHashMap.newKeySet();

    private int activeCountdown = -1;

    /**
     * Returns whether a boss currently has reactive spikes active.
     *
     * @param bossId the boss UUID
     * @return {@code true} if spikes are active
     */
    public static boolean isActive(@NotNull final UUID bossId) {
        return ACTIVE_BOSSES.contains(bossId);
    }

    /**
     * Removes a boss from the active-spikes set (called on boss removal).
     *
     * @param bossId the boss UUID to clear
     */
    public static void onBossRemoved(@NotNull final UUID bossId) {
        ACTIVE_BOSSES.remove(bossId);
    }

    @Override
    @NotNull
    public String id() {
        return "reactive_spikes";
    }

    @Override
    public boolean canActivate(
            @NotNull final Mob boss,
            @NotNull final BossArchetype archetype,
            final float healthPct) {
        return archetype == BossArchetype.KITER && activeCountdown < 0;
    }

    @Override
    public void onActivate(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers) {
        ACTIVE_BOSSES.add(boss.getUUID());
        activeCountdown = DURATION;
    }

    @Override
    public void onTick(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers,
            final int currentTick) {
        if (activeCountdown <= 0) {
            return;
        }
        activeCountdown--;
        if (activeCountdown == 0) {
            ACTIVE_BOSSES.remove(boss.getUUID());
            activeCountdown = -1;
        }
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN;
    }
}
