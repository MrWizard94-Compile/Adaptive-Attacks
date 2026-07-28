package com.adaptiveattacks.attack.wither;

import com.adaptiveattacks.ability.BossAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.NotNull;

/**
 * Wither passive: fires a massive explosion at the end of the {@link NuclearWinterAbility}
 * window, clearing the darkness and resetting the Wither's AI to Rush state.
 *
 * <p>Triggered once when {@link NuclearWinterAbility#isEnding(UUID)} returns {@code true}.
 * Like {@link FreezingDarkAbility}, this ability never activates via the scheduler — it
 * runs entirely in {@link #onTick}.
 */
public final class ShatterWaveAbility implements BossAbility {

    private static final float EXPLOSION_RADIUS = 7.0f;

    @Override
    @NotNull
    public String id() {
        return "shatter_wave";
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
        if (!NuclearWinterAbility.isEnding(boss.getUUID())) {
            return;
        }
        NuclearWinterAbility.clearEnding(boss.getUUID());
        level.explode(
                boss,
                boss.getX(), boss.getY(), boss.getZ(),
                EXPLOSION_RADIUS,
                false,
                Level.ExplosionInteraction.MOB);
        // Remove darkness from surviving players
        for (final ServerPlayer player : activePlayers) {
            player.removeEffect(MobEffects.DARKNESS);
        }
    }

    @Override
    public int cooldownTicks() {
        return 0;
    }
}
