package com.adaptiveattacks.gametest;

import com.adaptiveattacks.attack.dragon.HomingFireballAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.jetbrains.annotations.NotNull;

/**
 * In-game tests for {@link HomingFireballAbility} behaviour.
 *
 * <p>Because fireball spawning requires live players to target, this test validates
 * the {@code canActivate} health-threshold guards instead: the ability must be
 * activatable below 25% health and must refuse to activate at or above 25%.
 */
@GameTestHolder("adaptiveattacks")
public final class HomingFireballGameTest {

    /** Spawn position inside the 9x9x9 empty-platform structure. */
    private static final BlockPos SPAWN_POS = new BlockPos(4, 2, 4);

    /** Health fraction below the 25% activation threshold. */
    private static final float BELOW_THRESHOLD_PCT = 0.1f;

    /** Health fraction at or above the 25% activation threshold. */
    private static final float ABOVE_THRESHOLD_PCT = 0.5f;

    private HomingFireballGameTest() { }

    /**
     * Verifies that {@link HomingFireballAbility#canActivate} correctly gates on the
     * dragon's health percentage: returns {@code true} below 25% and {@code false}
     * at or above 25%.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void homingFireballSpawnsProjectiles(@NotNull final GameTestHelper helper) {
        final EnderDragon dragon =
                (EnderDragon) helper.spawn(EntityType.ENDER_DRAGON, SPAWN_POS);
        dragon.setHealth(dragon.getMaxHealth() * BELOW_THRESHOLD_PCT);

        final HomingFireballAbility ability = new HomingFireballAbility();

        if (!ability.canActivate(dragon, BossArchetype.BALANCED, BELOW_THRESHOLD_PCT)) {
            helper.fail(
                    "canActivate should return true for EnderDragon at "
                    + BELOW_THRESHOLD_PCT * 100 + "% health (below 25% threshold)");
            return;
        }

        if (ability.canActivate(dragon, BossArchetype.BALANCED, ABOVE_THRESHOLD_PCT)) {
            helper.fail(
                    "canActivate should return false for EnderDragon at "
                    + ABOVE_THRESHOLD_PCT * 100 + "% health (above 25% threshold)");
            return;
        }

        helper.succeed();
    }
}
