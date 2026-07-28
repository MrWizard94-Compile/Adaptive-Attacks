package com.adaptiveattacks.gametest;

import com.adaptiveattacks.attack.dragon.DragonOverdriveAbility;
import com.adaptiveattacks.attack.dragon.VulnerableExhaustionAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.jetbrains.annotations.NotNull;

/**
 * In-game tests for dragon-specific ability behaviour:
 * {@link DragonOverdriveAbility} and {@link VulnerableExhaustionAbility}.
 */
@GameTestHolder("adaptiveattacks")
public final class DragonAbilityGameTest {

    /** Spawn position inside the 9x9x9 empty-platform structure. */
    private static final BlockPos SPAWN_POS = new BlockPos(4, 2, 4);

    /** Health fraction below the 25 % activation threshold. */
    private static final float BELOW_THRESHOLD = 0.10f;

    /** Health fraction at or above the 25 % activation threshold. */
    private static final float ABOVE_THRESHOLD = 0.50f;

    private DragonAbilityGameTest() { }

    /**
     * Verifies that {@link DragonOverdriveAbility#canActivate} returns {@code true}
     * for an EnderDragon below 25 % health and {@code false} at or above 25 %.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void dragonOverdriveGatesOnHealth(@NotNull final GameTestHelper helper) {
        final EnderDragon dragon =
                (EnderDragon) helper.spawn(EntityType.ENDER_DRAGON, SPAWN_POS);
        dragon.setHealth(dragon.getMaxHealth() * BELOW_THRESHOLD);

        final DragonOverdriveAbility ability = new DragonOverdriveAbility();

        if (!ability.canActivate(dragon, BossArchetype.BALANCED, BELOW_THRESHOLD)) {
            helper.fail("DragonOverdriveAbility.canActivate should return true below 25 % health");
            return;
        }

        if (ability.canActivate(dragon, BossArchetype.BALANCED, ABOVE_THRESHOLD)) {
            helper.fail("DragonOverdriveAbility.canActivate should return false at or above 25 % health");
            return;
        }

        helper.succeed();
    }

    /**
     * Verifies that {@link VulnerableExhaustionAbility#canActivate} returns {@code true}
     * for a fresh ability instance below 25 % health and {@code false} at or above 25 %.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void vulnerableExhaustionGatesOnHealth(@NotNull final GameTestHelper helper) {
        final EnderDragon dragon =
                (EnderDragon) helper.spawn(EntityType.ENDER_DRAGON, SPAWN_POS);
        dragon.setHealth(dragon.getMaxHealth() * BELOW_THRESHOLD);

        final VulnerableExhaustionAbility ability = new VulnerableExhaustionAbility();

        if (!ability.canActivate(dragon, BossArchetype.BALANCED, BELOW_THRESHOLD)) {
            helper.fail(
                    "VulnerableExhaustionAbility.canActivate should return true"
                    + " for a fresh instance below 25 % health");
            return;
        }

        if (ability.canActivate(dragon, BossArchetype.BALANCED, ABOVE_THRESHOLD)) {
            helper.fail(
                    "VulnerableExhaustionAbility.canActivate should return false"
                    + " at or above 25 % health");
            return;
        }

        helper.succeed();
    }

    /**
     * Verifies that {@link VulnerableExhaustionAbility#onActivate} populates the
     * vulnerable-dragons set and that {@link VulnerableExhaustionAbility#onBossRemoved}
     * clears it.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void vulnerableExhaustionClearsOnRemoval(@NotNull final GameTestHelper helper) {
        final EnderDragon dragon =
                (EnderDragon) helper.spawn(EntityType.ENDER_DRAGON, SPAWN_POS);
        dragon.setHealth(dragon.getMaxHealth() * BELOW_THRESHOLD);

        final VulnerableExhaustionAbility ability = new VulnerableExhaustionAbility();
        ability.onActivate(dragon, helper.getLevel(), List.of());

        if (!VulnerableExhaustionAbility.isVulnerable(dragon.getUUID())) {
            VulnerableExhaustionAbility.onBossRemoved(dragon.getUUID());
            helper.fail("isVulnerable should return true immediately after onActivate");
            return;
        }

        // While the ability is active the countdown is non-negative, so canActivate must
        // return false even below the health threshold.
        if (ability.canActivate(dragon, BossArchetype.BALANCED, BELOW_THRESHOLD)) {
            VulnerableExhaustionAbility.onBossRemoved(dragon.getUUID());
            helper.fail(
                    "canActivate should return false while the ability is counting down");
            return;
        }

        VulnerableExhaustionAbility.onBossRemoved(dragon.getUUID());

        if (VulnerableExhaustionAbility.isVulnerable(dragon.getUUID())) {
            helper.fail("isVulnerable should return false after onBossRemoved");
            return;
        }

        helper.succeed();
    }
}
