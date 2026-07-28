package com.adaptiveattacks.attack.generic.kite;

import com.adaptiveattacks.ability.BossAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

/**
 * KITE ability: leaves a lingering poison-and-wither cloud at the boss's current position
 * as it retreats.
 *
 * <p>Activates in the KITER archetype.
 */
public final class ToxicExhaustAbility implements BossAbility {

    private static final int COOLDOWN = 140;
    private static final float CLOUD_RADIUS = 3.0f;
    private static final int CLOUD_DURATION = 200;
    private static final int EFFECT_DURATION = 80;

    @Override
    @NotNull
    public String id() {
        return "toxic_exhaust";
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
        final AreaEffectCloud cloud =
                new AreaEffectCloud(level, boss.getX(), boss.getY(), boss.getZ());
        cloud.setRadius(CLOUD_RADIUS);
        cloud.setDuration(CLOUD_DURATION);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        cloud.addEffect(new MobEffectInstance(MobEffects.POISON, EFFECT_DURATION, 1));
        cloud.addEffect(new MobEffectInstance(MobEffects.WITHER, EFFECT_DURATION, 0));
        level.addFreshEntity(cloud);
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN;
    }
}
