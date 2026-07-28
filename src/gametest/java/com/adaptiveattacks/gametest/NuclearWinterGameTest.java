package com.adaptiveattacks.gametest;

import com.adaptiveattacks.attack.wither.NuclearWinterAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.jetbrains.annotations.NotNull;

/**
 * In-game tests for {@link NuclearWinterAbility} behaviour.
 *
 * <p>Tests verify that activation correctly sets NoAI on the Wither, registers the
 * UUID in the active-set, and that {@link NuclearWinterAbility#onBossRemoved} fully
 * clears that state.
 */
@GameTestHolder("adaptiveattacks")
public final class NuclearWinterGameTest {

    /** Spawn position inside the 9x9x9 empty-platform structure. */
    private static final BlockPos SPAWN_POS = new BlockPos(4, 2, 4);

    /** Health fraction that satisfies the {@code < 0.5f} activation guard. */
    private static final float LOW_HEALTH_PCT = 0.3f;

    private NuclearWinterGameTest() { }

    /**
     * Verifies that activating {@link NuclearWinterAbility} sets NoAI on the Wither
     * and records it in the static active-UUID set.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void nuclearWinterSetsNoAiOnWither(@NotNull final GameTestHelper helper) {
        final WitherBoss wither = helper.spawn(EntityType.WITHER, SPAWN_POS);
        wither.setHealth(wither.getMaxHealth() * LOW_HEALTH_PCT);

        final NuclearWinterAbility ability = new NuclearWinterAbility();

        if (!ability.canActivate(wither, BossArchetype.BALANCED, LOW_HEALTH_PCT)) {
            helper.fail("canActivate returned false for a Wither at 30% health");
            return;
        }

        ability.onActivate(wither, helper.getLevel(), List.of());

        if (!wither.isNoAi()) {
            NuclearWinterAbility.onBossRemoved(wither.getUUID());
            helper.fail("Wither NoAI flag was not set after NuclearWinter activation");
            return;
        }

        if (!NuclearWinterAbility.isActive(wither.getUUID())) {
            NuclearWinterAbility.onBossRemoved(wither.getUUID());
            helper.fail("NuclearWinterAbility.isActive is false immediately after onActivate");
            return;
        }

        // Clean up static state so other tests are not affected
        NuclearWinterAbility.onBossRemoved(wither.getUUID());
        helper.succeed();
    }

    /**
     * Verifies that {@link NuclearWinterAbility#onBossRemoved} fully clears the
     * active-UUID set so that {@link NuclearWinterAbility#isActive} returns
     * {@code false} afterwards.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void shatterWaveClearsEndingState(@NotNull final GameTestHelper helper) {
        final WitherBoss wither = helper.spawn(EntityType.WITHER, SPAWN_POS);
        wither.setHealth(wither.getMaxHealth() * LOW_HEALTH_PCT);

        final NuclearWinterAbility ability = new NuclearWinterAbility();
        ability.onActivate(wither, helper.getLevel(), List.of());

        if (!NuclearWinterAbility.isActive(wither.getUUID())) {
            helper.fail("isActive should be true immediately after onActivate");
            return;
        }

        NuclearWinterAbility.onBossRemoved(wither.getUUID());

        if (NuclearWinterAbility.isActive(wither.getUUID())) {
            helper.fail("isActive should be false after onBossRemoved");
            return;
        }

        helper.succeed();
    }
}
