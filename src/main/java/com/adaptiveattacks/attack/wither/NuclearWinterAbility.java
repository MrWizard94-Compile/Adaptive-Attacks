package com.adaptiveattacks.attack.wither;

import com.adaptiveattacks.ability.BossAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.jetbrains.annotations.NotNull;

/**
 * Wither ability (health &lt; 50%): locks the Wither in place and floods the arena
 * with magical darkness for 10 seconds, triggering the three-phase Nuclear Winter sequence.
 *
 * <p>{@link FreezingDarkAbility} passively applies freeze/wither damage during this window.
 * {@link ShatterWaveAbility} fires a massive explosion at the end of the window.
 *
 * <p>Static state is shared between the three abilities via {@link #isActive(UUID)},
 * {@link #isEnding(UUID)}, and {@link #clearEnding(UUID)}.
 */
public final class NuclearWinterAbility implements BossAbility {

    private static final int COOLDOWN = 800;
    private static final int DURATION = 200;
    private static final int ENDING_TICKS = 10;
    private static final float HEALTH_THRESHOLD = 0.5f;

    private static final Set<UUID> ACTIVE_WITHERS = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> ENDING_WITHERS = ConcurrentHashMap.newKeySet();

    private int countdown = -1;

    /**
     * Returns whether the Wither with the given UUID is in the Nuclear Winter window.
     *
     * @param bossId the Wither's UUID
     * @return {@code true} if Nuclear Winter is active
     */
    public static boolean isActive(@NotNull final UUID bossId) {
        return ACTIVE_WITHERS.contains(bossId);
    }

    /**
     * Returns whether the Nuclear Winter is in its final ticks (Shatter Wave imminent).
     *
     * @param bossId the Wither's UUID
     * @return {@code true} if ending phase is active
     */
    public static boolean isEnding(@NotNull final UUID bossId) {
        return ENDING_WITHERS.contains(bossId);
    }

    /**
     * Clears the ending flag for a boss after the Shatter Wave has fired.
     *
     * @param bossId the Wither's UUID
     */
    public static void clearEnding(@NotNull final UUID bossId) {
        ENDING_WITHERS.remove(bossId);
    }

    /**
     * Removes a boss from all Nuclear Winter tracking sets (called on boss removal).
     *
     * @param bossId the boss UUID to clear
     */
    public static void onBossRemoved(@NotNull final UUID bossId) {
        ACTIVE_WITHERS.remove(bossId);
        ENDING_WITHERS.remove(bossId);
    }

    @Override
    @NotNull
    public String id() {
        return "nuclear_winter";
    }

    @Override
    public boolean canActivate(
            @NotNull final Mob boss,
            @NotNull final BossArchetype archetype,
            final float healthPct) {
        return boss instanceof WitherBoss && healthPct < HEALTH_THRESHOLD && countdown < 0;
    }

    @Override
    public void onActivate(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers) {
        ACTIVE_WITHERS.add(boss.getUUID());
        boss.setNoAi(true);
        countdown = DURATION;
        for (final ServerPlayer player : activePlayers) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DARKNESS, DURATION + 40, 0, false, false));
        }
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
        boss.getNavigation().stop();
        countdown--;
        if (countdown == ENDING_TICKS) {
            ENDING_WITHERS.add(boss.getUUID());
        }
        if (countdown <= 0) {
            ACTIVE_WITHERS.remove(boss.getUUID());
            boss.setNoAi(false);
            countdown = -1;
        }
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN;
    }
}
