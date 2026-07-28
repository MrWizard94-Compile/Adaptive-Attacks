package com.adaptiveattacks.attack.dragon;

import com.adaptiveattacks.ability.BossAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Dragon ability (health &lt; 25%): rapidly fires dragon fireballs aimed at each
 * active player's current position.
 *
 * <p>Three fireballs are launched per player with a slight angular spread to make
 * them harder to dodge. Activates while below the health threshold.
 */
public final class HomingFireballAbility implements BossAbility {

    private static final int COOLDOWN = 100;
    private static final float HEALTH_THRESHOLD = 0.25f;
    private static final double FIREBALL_SPEED = 0.6;
    private static final double SPAWN_OFFSET_Y = 2.0;
    private static final double[] SPREAD = {-0.08, 0.0, 0.08};

    @Override
    @NotNull
    public String id() {
        return "homing_fireball";
    }

    @Override
    public boolean canActivate(
            @NotNull final Mob boss,
            @NotNull final BossArchetype archetype,
            final float healthPct) {
        return boss instanceof EnderDragon && healthPct < HEALTH_THRESHOLD;
    }

    @Override
    public void onActivate(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers) {
        if (activePlayers.isEmpty()) {
            return;
        }
        final EnderDragon dragon = (EnderDragon) boss;
        for (final ServerPlayer target : activePlayers) {
            fireAtPlayer(dragon, level, target);
        }
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN;
    }

    private static void fireAtPlayer(
            @NotNull final EnderDragon dragon,
            @NotNull final ServerLevel level,
            @NotNull final ServerPlayer target) {
        final Vec3 origin = new Vec3(dragon.getX(), dragon.getY() + SPAWN_OFFSET_Y, dragon.getZ());
        final Vec3 baseDir = target.position().subtract(origin).normalize();
        for (final double spread : SPREAD) {
            final Vec3 dir = baseDir.add(spread, 0.0, spread).normalize();
            final DragonFireball fireball = new DragonFireball(EntityType.DRAGON_FIREBALL, level);
            fireball.moveTo(origin.x, origin.y, origin.z);
            fireball.setOwner(dragon);
            fireball.setDeltaMovement(dir.scale(FIREBALL_SPEED));
            level.addFreshEntity(fireball);
        }
    }
}
