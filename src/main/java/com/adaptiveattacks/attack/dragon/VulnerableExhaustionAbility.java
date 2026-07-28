package com.adaptiveattacks.attack.dragon;

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
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import org.jetbrains.annotations.NotNull;

/**
 * Dragon ability (health &lt; 25%): forces the dragon to land at the centre portal
 * and enter a 10-second "Recharge" state — stationary with 2× incoming damage.
 *
 * <p>The {@link com.adaptiveattacks.event.AttackLifecycleHandler} reads
 * {@link #isVulnerable(UUID)} to double damage dealt to the dragon during this window.
 */
public final class VulnerableExhaustionAbility implements BossAbility {

    private static final int COOLDOWN = 800;
    private static final int DURATION = 200;
    private static final float HEALTH_THRESHOLD = 0.25f;

    private static final Set<UUID> VULNERABLE_DRAGONS = ConcurrentHashMap.newKeySet();

    private int countdown = -1;

    /**
     * Returns whether the given dragon is currently in its vulnerable recharge state.
     *
     * @param bossId the dragon's UUID
     * @return {@code true} if damage should be doubled
     */
    public static boolean isVulnerable(@NotNull final UUID bossId) {
        return VULNERABLE_DRAGONS.contains(bossId);
    }

    /**
     * Removes a boss from the vulnerable set (called on boss removal).
     *
     * @param bossId the boss UUID to clear
     */
    public static void onBossRemoved(@NotNull final UUID bossId) {
        VULNERABLE_DRAGONS.remove(bossId);
    }

    @Override
    @NotNull
    public String id() {
        return "vulnerable_exhaustion";
    }

    @Override
    public boolean canActivate(
            @NotNull final Mob boss,
            @NotNull final BossArchetype archetype,
            final float healthPct) {
        return boss instanceof EnderDragon
                && healthPct < HEALTH_THRESHOLD
                && countdown < 0;
    }

    @Override
    public void onActivate(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers) {
        final EnderDragon dragon = (EnderDragon) boss;
        dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING);
        dragon.addEffect(
                new MobEffectInstance(MobEffects.WEAKNESS, DURATION, 1, false, true));
        VULNERABLE_DRAGONS.add(boss.getUUID());
        countdown = DURATION;
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
        if (countdown <= 0) {
            VULNERABLE_DRAGONS.remove(boss.getUUID());
            countdown = -1;
        }
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN;
    }
}
