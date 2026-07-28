package com.adaptiveattacks.attack.generic.hybrid;

import com.adaptiveattacks.ability.BossAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * HYBRID ability: raises a wall of lingering fire-and-slowness clouds between
 * the two outermost players, physically splitting the group.
 *
 * <p>The wall is formed by a line of {@link AreaEffectCloud} entities
 * perpendicular to the axis connecting the two players.
 * Activates in the UNPREDICTABLE archetype when at least two players are present.
 */
public final class DividingWallAbility implements BossAbility {

    private static final int COOLDOWN = 320;
    private static final int WALL_SEGMENTS = 5;
    private static final float CLOUD_RADIUS = 1.8f;
    private static final int CLOUD_DURATION = 120;
    private static final int EFFECT_DURATION = 60;
    private static final double WALL_HALF_WIDTH = 4.0;

    @Override
    @NotNull
    public String id() {
        return "dividing_wall";
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
        final Vec3 midpoint = p1.position().add(p2.position()).scale(0.5);
        // Perpendicular direction (rotate axis 90° around Y)
        final Vec3 axis = p2.position().subtract(p1.position()).normalize();
        final Vec3 perp = new Vec3(-axis.z, 0.0, axis.x);

        for (int i = 0; i <= WALL_SEGMENTS; i++) {
            final double t = (i - WALL_SEGMENTS / 2.0) * (WALL_HALF_WIDTH / (WALL_SEGMENTS / 2.0));
            final Vec3 cloudPos = midpoint.add(perp.scale(t));
            spawnWallCloud(level, cloudPos);
        }
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN;
    }

    private static void spawnWallCloud(
            @NotNull final ServerLevel level,
            @NotNull final Vec3 pos) {
        final AreaEffectCloud cloud = new AreaEffectCloud(level, pos.x, pos.y, pos.z);
        cloud.setRadius(CLOUD_RADIUS);
        cloud.setDuration(CLOUD_DURATION);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFFECT_DURATION, 2));
        level.addFreshEntity(cloud);
    }
}
