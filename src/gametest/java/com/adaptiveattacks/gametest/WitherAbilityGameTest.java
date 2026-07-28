package com.adaptiveattacks.gametest;

import com.adaptiveattacks.attack.wither.FreezingDarkAbility;
import com.adaptiveattacks.attack.wither.ShatterWaveAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.jetbrains.annotations.NotNull;

/**
 * In-game tests for passive wither abilities:
 * {@link FreezingDarkAbility} and {@link ShatterWaveAbility}.
 *
 * <p>Both abilities are passive companions to {@link com.adaptiveattacks.attack.wither.NuclearWinterAbility}
 * and must never self-activate; {@code canActivate} must always return {@code false}.
 */
@GameTestHolder("adaptiveattacks")
public final class WitherAbilityGameTest {

    /** Spawn position inside the 9x9x9 empty-platform structure. */
    private static final BlockPos SPAWN_POS = new BlockPos(4, 2, 4);

    private WitherAbilityGameTest() { }

    /**
     * Verifies that {@link FreezingDarkAbility#canActivate} returns {@code false} for
     * every {@link BossArchetype} value, confirming the ability is purely passive.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void freezingDarkIsPassive(@NotNull final GameTestHelper helper) {
        final WitherBoss wither = helper.spawn(EntityType.WITHER, SPAWN_POS);
        final FreezingDarkAbility ability = new FreezingDarkAbility();

        for (final BossArchetype archetype : BossArchetype.values()) {
            if (ability.canActivate(wither, archetype, 0.5f)) {
                helper.fail(
                        "FreezingDarkAbility.canActivate must always return false"
                        + " (passive), but returned true for archetype " + archetype);
                return;
            }
        }

        helper.succeed();
    }

    /**
     * Verifies that {@link ShatterWaveAbility#canActivate} returns {@code false} for
     * every {@link BossArchetype} value, confirming the ability is purely passive.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void shatterWaveIsPassive(@NotNull final GameTestHelper helper) {
        final WitherBoss wither = helper.spawn(EntityType.WITHER, SPAWN_POS);
        final ShatterWaveAbility ability = new ShatterWaveAbility();

        for (final BossArchetype archetype : BossArchetype.values()) {
            if (ability.canActivate(wither, archetype, 0.5f)) {
                helper.fail(
                        "ShatterWaveAbility.canActivate must always return false"
                        + " (passive), but returned true for archetype " + archetype);
                return;
            }
        }

        helper.succeed();
    }
}
