package com.adaptiveattacks.attack.warden;

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
import org.jetbrains.annotations.NotNull;

/**
 * Warden ultimate ability: the Warden becomes perfectly still, gains Strength II,
 * and blinds all nearby players for 12 seconds.
 *
 * <p>The "silence" effect (muting game audio) is a server-side approximation via
 * Blindness. True audio muting would require a client-side network packet, which
 * is not implemented here.
 */
public final class TheSilenceAbility implements BossAbility {

    private static final int COOLDOWN = 600;
    private static final int DURATION = 240;
    private static final double SILENCE_RADIUS = 30.0;

    private static final Set<UUID> SILENCED_WARDENS = ConcurrentHashMap.newKeySet();

    /**
     * Removes a boss from the silenced set (called on boss removal).
     *
     * @param bossId the boss UUID to clear
     */
    public static void onBossRemoved(@NotNull final UUID bossId) {
        SILENCED_WARDENS.remove(bossId);
    }

    @Override
    @NotNull
    public String id() {
        return "the_silence";
    }

    @Override
    public boolean canActivate(
            @NotNull final Mob boss,
            @NotNull final BossArchetype archetype,
            final float healthPct) {
        return !SILENCED_WARDENS.contains(boss.getUUID());
    }

    @Override
    public void onActivate(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers) {
        SILENCED_WARDENS.add(boss.getUUID());
        boss.addEffect(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, DURATION, 1, false, true));
        for (final ServerPlayer player : activePlayers) {
            if (boss.distanceTo(player) <= SILENCE_RADIUS) {
                player.addEffect(
                        new MobEffectInstance(MobEffects.BLINDNESS, DURATION, 0, false, true));
            }
        }
    }

    @Override
    public void onTick(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers,
            final int currentTick) {
        if (!SILENCED_WARDENS.contains(boss.getUUID())) {
            return;
        }
        if (!boss.hasEffect(MobEffects.DAMAGE_BOOST)) {
            SILENCED_WARDENS.remove(boss.getUUID());
        }
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN;
    }
}
