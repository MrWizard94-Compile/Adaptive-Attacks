package com.adaptiveattacks.gametest;

import com.adaptiveattacks.attack.warden.EchoStrikeAbility;
import com.adaptiveattacks.attack.warden.EcholocationShockwaveAbility;
import com.adaptiveattacks.attack.warden.SculkSuffocationAbility;
import com.adaptiveattacks.attack.warden.TheSilenceAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.jetbrains.annotations.NotNull;

/**
 * In-game tests for all four warden-specific abilities:
 * {@link EchoStrikeAbility}, {@link EcholocationShockwaveAbility},
 * {@link SculkSuffocationAbility}, and {@link TheSilenceAbility}.
 */
@GameTestHolder("adaptiveattacks")
public final class WardenAbilityGameTest {

    /** Spawn position inside the 9x9x9 empty-platform structure. */
    private static final BlockPos SPAWN_POS = new BlockPos(4, 2, 4);

    private WardenAbilityGameTest() { }

    /**
     * Verifies that {@link EchoStrikeAbility#canActivate} always returns {@code false}
     * for every archetype — the ability is passive and triggered by
     * {@link EcholocationShockwaveAbility}'s TAGGED_PLAYERS map.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void echoStrikeIsPassive(@NotNull final GameTestHelper helper) {
        final Warden warden = (Warden) helper.spawn(EntityType.WARDEN, SPAWN_POS);
        final EchoStrikeAbility ability = new EchoStrikeAbility();

        for (final BossArchetype archetype : BossArchetype.values()) {
            if (ability.canActivate(warden, archetype, 0.5f)) {
                helper.fail(
                        "EchoStrikeAbility.canActivate must always return false"
                        + " (passive), but returned true for archetype " + archetype);
                return;
            }
        }

        helper.succeed();
    }

    /**
     * Verifies that {@link EcholocationShockwaveAbility#canActivate} returns {@code true}
     * for every archetype, and that {@link EcholocationShockwaveAbility#clearAllTags}
     * completes without error.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void echolocationShockwaveAlwaysActivates(@NotNull final GameTestHelper helper) {
        final Warden warden = (Warden) helper.spawn(EntityType.WARDEN, SPAWN_POS);
        final EcholocationShockwaveAbility ability = new EcholocationShockwaveAbility();

        for (final BossArchetype archetype : BossArchetype.values()) {
            if (!ability.canActivate(warden, archetype, 0.5f)) {
                helper.fail(
                        "EcholocationShockwaveAbility.canActivate must always return true,"
                        + " but returned false for archetype " + archetype);
                return;
            }
        }

        // clearAllTags must not throw even when the map is empty
        EcholocationShockwaveAbility.clearAllTags();

        helper.succeed();
    }

    /**
     * Verifies that {@link SculkSuffocationAbility#canActivate} gates on the
     * {@link BossArchetype#KITER} archetype and on the internal countdown being
     * negative, and that it returns {@code false} while the countdown is active.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void sculkSuffocationGatesOnArchetype(@NotNull final GameTestHelper helper) {
        final Warden warden = (Warden) helper.spawn(EntityType.WARDEN, SPAWN_POS);
        final SculkSuffocationAbility ability = new SculkSuffocationAbility();

        // Fresh instance: countdown is negative — should activate for KITER
        if (!ability.canActivate(warden, BossArchetype.KITER, 0.5f)) {
            helper.fail(
                    "SculkSuffocationAbility.canActivate should return true"
                    + " for KITER archetype on a fresh instance");
            return;
        }

        // Any non-KITER archetype must return false
        if (ability.canActivate(warden, BossArchetype.AGGRESSOR, 0.5f)) {
            helper.fail(
                    "SculkSuffocationAbility.canActivate should return false"
                    + " for non-KITER archetype");
            return;
        }

        // After activation the countdown becomes non-negative; must return false even for KITER
        ability.onActivate(warden, helper.getLevel(), List.of());
        if (ability.canActivate(warden, BossArchetype.KITER, 0.5f)) {
            helper.fail(
                    "SculkSuffocationAbility.canActivate should return false"
                    + " while the ability is counting down");
            return;
        }

        helper.succeed();
    }

    /**
     * Verifies the full lifecycle of {@link TheSilenceAbility}: {@code canActivate}
     * returns {@code true} when the warden is not yet silenced, {@code false} after
     * {@code onActivate}, and {@code true} again after
     * {@link TheSilenceAbility#onBossRemoved}.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void theSilenceGatesOnActiveSet(@NotNull final GameTestHelper helper) {
        final Warden warden = (Warden) helper.spawn(EntityType.WARDEN, SPAWN_POS);
        final TheSilenceAbility ability = new TheSilenceAbility();

        // Should be activatable before any silence is applied
        if (!ability.canActivate(warden, BossArchetype.BALANCED, 0.5f)) {
            TheSilenceAbility.onBossRemoved(warden.getUUID());
            helper.fail(
                    "TheSilenceAbility.canActivate should return true"
                    + " when the warden is not yet silenced");
            return;
        }

        ability.onActivate(warden, helper.getLevel(), List.of());

        // UUID is now in SILENCED_WARDENS — must not activate again
        if (ability.canActivate(warden, BossArchetype.BALANCED, 0.5f)) {
            TheSilenceAbility.onBossRemoved(warden.getUUID());
            helper.fail(
                    "TheSilenceAbility.canActivate should return false"
                    + " immediately after activation");
            return;
        }

        TheSilenceAbility.onBossRemoved(warden.getUUID());

        // Should be activatable again after the UUID is removed
        if (!ability.canActivate(warden, BossArchetype.BALANCED, 0.5f)) {
            helper.fail(
                    "TheSilenceAbility.canActivate should return true"
                    + " after onBossRemoved clears the silenced set");
            return;
        }

        helper.succeed();
    }
}
