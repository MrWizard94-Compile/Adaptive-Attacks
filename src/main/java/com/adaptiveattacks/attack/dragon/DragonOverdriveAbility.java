package com.adaptiveattacks.attack.dragon;

import com.adaptiveattacks.ability.BossAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import org.jetbrains.annotations.NotNull;

/**
 * Dragon ability (health &lt; 25%): forces the dragon into a rapid strafing flight loop.
 *
 * <p>Pushes the Ender Dragon into {@code STRAFE_PLAYER} phase, causing high-speed
 * passes over the island. Works in conjunction with {@link HomingFireballAbility} and
 * {@link VulnerableExhaustionAbility} which trigger on separate cooldowns.
 */
public final class DragonOverdriveAbility implements BossAbility {

    private static final int COOLDOWN = 200;
    private static final float HEALTH_THRESHOLD = 0.25f;

    @Override
    @NotNull
    public String id() {
        return "dragon_overdrive";
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
        final EnderDragon dragon = (EnderDragon) boss;
        dragon.getPhaseManager().setPhase(EnderDragonPhase.STRAFE_PLAYER);
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN;
    }
}
