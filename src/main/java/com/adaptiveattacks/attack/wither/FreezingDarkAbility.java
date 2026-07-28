package com.adaptiveattacks.attack.wither;

import com.adaptiveattacks.ability.BossAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.NotNull;

/**
 * Wither passive: during the {@link NuclearWinterAbility} window, players standing in
 * darkness (block light &lt; 7) take freeze damage and Wither II every second.
 *
 * <p>This ability never "activates" via the scheduler — it exclusively uses
 * {@link #onTick} to react to the active Nuclear Winter state. Players who stay
 * near a torch (block light ≥ 7) are unaffected.
 */
public final class FreezingDarkAbility implements BossAbility {

    private static final int TICK_INTERVAL = 20;
    private static final int MIN_LIGHT = 7;
    private static final float FREEZE_DAMAGE = 2.0f;
    private static final int WITHER_DURATION = 60;

    @Override
    @NotNull
    public String id() {
        return "freezing_dark";
    }

    @Override
    public boolean canActivate(
            @NotNull final Mob boss,
            @NotNull final BossArchetype archetype,
            final float healthPct) {
        return false;
    }

    @Override
    public void onActivate(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers) {
        // never called
    }

    @Override
    public void onTick(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers,
            final int currentTick) {
        if (!NuclearWinterAbility.isActive(boss.getUUID())) {
            return;
        }
        if (currentTick % TICK_INTERVAL != 0) {
            return;
        }
        for (final ServerPlayer player : activePlayers) {
            final int blockLight = level.getBrightness(LightLayer.BLOCK, player.blockPosition());
            if (blockLight < MIN_LIGHT) {
                player.hurt(level.damageSources().freeze(), FREEZE_DAMAGE);
                player.addEffect(
                        new MobEffectInstance(MobEffects.WITHER, WITHER_DURATION, 1, false, true));
            }
        }
    }

    @Override
    public int cooldownTicks() {
        return 0;
    }
}
