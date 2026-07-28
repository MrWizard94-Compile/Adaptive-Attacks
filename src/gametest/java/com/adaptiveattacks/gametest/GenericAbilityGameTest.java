package com.adaptiveattacks.gametest;

import com.adaptiveattacks.attack.generic.hybrid.ChaosSwapAbility;
import com.adaptiveattacks.attack.generic.hybrid.DividingWallAbility;
import com.adaptiveattacks.attack.generic.hybrid.MagneticLinkAbility;
import com.adaptiveattacks.attack.generic.kite.ReactiveSpikesAbility;
import com.adaptiveattacks.attack.generic.kite.RepulsionNovaAbility;
import com.adaptiveattacks.attack.generic.kite.ToxicExhaustAbility;
import com.adaptiveattacks.attack.generic.rush.GapCloserAbility;
import com.adaptiveattacks.attack.generic.rush.KineticHarpoonAbility;
import com.adaptiveattacks.attack.generic.rush.ShadowStepAbility;
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
 * In-game tests for all nine generic abilities across the Rush, Kite and Hybrid
 * families. Generic abilities do not check entity type; a Wither is used as a
 * representative boss mob throughout.
 */
@GameTestHolder("adaptiveattacks")
public final class GenericAbilityGameTest {

    /** Spawn position inside the 9x9x9 empty-platform structure. */
    private static final BlockPos SPAWN_POS = new BlockPos(4, 2, 4);

    private GenericAbilityGameTest() { }

    // ── Rush abilities ────────────────────────────────────────────────────────

    /**
     * Verifies that {@link GapCloserAbility#canActivate} returns {@code true} for
     * {@link BossArchetype#AGGRESSOR} and {@code false} for all other archetypes.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void gapCloserGatesOnAggressor(@NotNull final GameTestHelper helper) {
        final WitherBoss wither = helper.spawn(EntityType.WITHER, SPAWN_POS);
        final GapCloserAbility ability = new GapCloserAbility();

        if (!ability.canActivate(wither, BossArchetype.AGGRESSOR, 0.5f)) {
            helper.fail("GapCloserAbility.canActivate should return true for AGGRESSOR");
            return;
        }

        for (final BossArchetype archetype : BossArchetype.values()) {
            if (archetype == BossArchetype.AGGRESSOR) {
                continue;
            }
            if (ability.canActivate(wither, archetype, 0.5f)) {
                helper.fail(
                        "GapCloserAbility.canActivate should return false for archetype "
                        + archetype);
                return;
            }
        }

        helper.succeed();
    }

    /**
     * Verifies that {@link KineticHarpoonAbility#canActivate} returns {@code true}
     * only for {@link BossArchetype#AGGRESSOR}.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void kineticHarpoonGatesOnAggressor(@NotNull final GameTestHelper helper) {
        final WitherBoss wither = helper.spawn(EntityType.WITHER, SPAWN_POS);
        final KineticHarpoonAbility ability = new KineticHarpoonAbility();

        if (!ability.canActivate(wither, BossArchetype.AGGRESSOR, 0.5f)) {
            helper.fail("KineticHarpoonAbility.canActivate should return true for AGGRESSOR");
            return;
        }

        if (ability.canActivate(wither, BossArchetype.KITER, 0.5f)) {
            helper.fail("KineticHarpoonAbility.canActivate should return false for KITER");
            return;
        }

        helper.succeed();
    }

    /**
     * Verifies that {@link ShadowStepAbility#canActivate} returns {@code true} for
     * {@link BossArchetype#AGGRESSOR} on a fresh instance and {@code false} for all
     * other archetypes.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void shadowStepGatesOnAggressor(@NotNull final GameTestHelper helper) {
        final WitherBoss wither = helper.spawn(EntityType.WITHER, SPAWN_POS);
        final ShadowStepAbility ability = new ShadowStepAbility();

        // Fresh instance has countdown < 0; AGGRESSOR must be accepted
        if (!ability.canActivate(wither, BossArchetype.AGGRESSOR, 0.5f)) {
            helper.fail(
                    "ShadowStepAbility.canActivate should return true"
                    + " for AGGRESSOR on a fresh instance");
            return;
        }

        if (ability.canActivate(wither, BossArchetype.BALANCED, 0.5f)) {
            helper.fail(
                    "ShadowStepAbility.canActivate should return false for BALANCED");
            return;
        }

        helper.succeed();
    }

    // ── Kite abilities ────────────────────────────────────────────────────────

    /**
     * Verifies that {@link ReactiveSpikesAbility#canActivate} returns {@code true}
     * only for {@link BossArchetype#KITER} on a fresh instance and {@code false} for
     * all other archetypes.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void reactiveSpikesGatesOnKiterArchetype(
            @NotNull final GameTestHelper helper) {
        final WitherBoss wither = helper.spawn(EntityType.WITHER, SPAWN_POS);
        final ReactiveSpikesAbility ability = new ReactiveSpikesAbility();

        if (!ability.canActivate(wither, BossArchetype.KITER, 0.5f)) {
            helper.fail(
                    "ReactiveSpikesAbility.canActivate should return true"
                    + " for KITER on a fresh instance");
            return;
        }

        if (ability.canActivate(wither, BossArchetype.AGGRESSOR, 0.5f)) {
            helper.fail(
                    "ReactiveSpikesAbility.canActivate should return false for AGGRESSOR");
            return;
        }

        helper.succeed();
    }

    /**
     * Verifies that {@link ReactiveSpikesAbility#onActivate} registers the boss UUID
     * in the active set, that {@code canActivate} returns {@code false} while the
     * ability is active, and that {@link ReactiveSpikesAbility#onBossRemoved} fully
     * clears that state.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void reactiveSpikesTracksActiveSet(@NotNull final GameTestHelper helper) {
        final WitherBoss wither = helper.spawn(EntityType.WITHER, SPAWN_POS);
        final ReactiveSpikesAbility ability = new ReactiveSpikesAbility();

        ability.onActivate(wither, helper.getLevel(), List.of());

        if (!ReactiveSpikesAbility.isActive(wither.getUUID())) {
            ReactiveSpikesAbility.onBossRemoved(wither.getUUID());
            helper.fail("isActive should return true immediately after onActivate");
            return;
        }

        // While counting down, canActivate must return false even for KITER
        if (ability.canActivate(wither, BossArchetype.KITER, 0.5f)) {
            ReactiveSpikesAbility.onBossRemoved(wither.getUUID());
            helper.fail(
                    "canActivate should return false while the reactive-spikes"
                    + " countdown is active");
            return;
        }

        ReactiveSpikesAbility.onBossRemoved(wither.getUUID());

        if (ReactiveSpikesAbility.isActive(wither.getUUID())) {
            helper.fail("isActive should return false after onBossRemoved");
            return;
        }

        helper.succeed();
    }

    /**
     * Verifies that {@link RepulsionNovaAbility#canActivate} returns {@code true} only
     * for {@link BossArchetype#KITER}.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void repulsionNovaGatesOnKiter(@NotNull final GameTestHelper helper) {
        final WitherBoss wither = helper.spawn(EntityType.WITHER, SPAWN_POS);
        final RepulsionNovaAbility ability = new RepulsionNovaAbility();

        if (!ability.canActivate(wither, BossArchetype.KITER, 0.5f)) {
            helper.fail("RepulsionNovaAbility.canActivate should return true for KITER");
            return;
        }

        if (ability.canActivate(wither, BossArchetype.AGGRESSOR, 0.5f)) {
            helper.fail(
                    "RepulsionNovaAbility.canActivate should return false for AGGRESSOR");
            return;
        }

        helper.succeed();
    }

    /**
     * Verifies that {@link ToxicExhaustAbility#canActivate} returns {@code true} only
     * for {@link BossArchetype#KITER}.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void toxicExhaustGatesOnKiter(@NotNull final GameTestHelper helper) {
        final WitherBoss wither = helper.spawn(EntityType.WITHER, SPAWN_POS);
        final ToxicExhaustAbility ability = new ToxicExhaustAbility();

        if (!ability.canActivate(wither, BossArchetype.KITER, 0.5f)) {
            helper.fail("ToxicExhaustAbility.canActivate should return true for KITER");
            return;
        }

        if (ability.canActivate(wither, BossArchetype.BALANCED, 0.5f)) {
            helper.fail(
                    "ToxicExhaustAbility.canActivate should return false for BALANCED");
            return;
        }

        helper.succeed();
    }

    // ── Hybrid abilities ──────────────────────────────────────────────────────

    /**
     * Verifies that {@link ChaosSwapAbility#canActivate} returns {@code true} only
     * for {@link BossArchetype#UNPREDICTABLE}.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void chaosSwapGatesOnUnpredictable(@NotNull final GameTestHelper helper) {
        final WitherBoss wither = helper.spawn(EntityType.WITHER, SPAWN_POS);
        final ChaosSwapAbility ability = new ChaosSwapAbility();

        if (!ability.canActivate(wither, BossArchetype.UNPREDICTABLE, 0.5f)) {
            helper.fail(
                    "ChaosSwapAbility.canActivate should return true for UNPREDICTABLE");
            return;
        }

        if (ability.canActivate(wither, BossArchetype.BALANCED, 0.5f)) {
            helper.fail(
                    "ChaosSwapAbility.canActivate should return false for BALANCED");
            return;
        }

        helper.succeed();
    }

    /**
     * Verifies that {@link DividingWallAbility#canActivate} returns {@code true} only
     * for {@link BossArchetype#UNPREDICTABLE}.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void dividingWallGatesOnUnpredictable(@NotNull final GameTestHelper helper) {
        final WitherBoss wither = helper.spawn(EntityType.WITHER, SPAWN_POS);
        final DividingWallAbility ability = new DividingWallAbility();

        if (!ability.canActivate(wither, BossArchetype.UNPREDICTABLE, 0.5f)) {
            helper.fail(
                    "DividingWallAbility.canActivate should return true for UNPREDICTABLE");
            return;
        }

        if (ability.canActivate(wither, BossArchetype.KITER, 0.5f)) {
            helper.fail(
                    "DividingWallAbility.canActivate should return false for KITER");
            return;
        }

        helper.succeed();
    }

    /**
     * Verifies that {@link MagneticLinkAbility#canActivate} returns {@code true} for
     * {@link BossArchetype#UNPREDICTABLE} when no link is active and {@code false} for
     * all other archetypes.
     *
     * @param helper the game-test helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void magneticLinkGatesOnUnpredictable(@NotNull final GameTestHelper helper) {
        final WitherBoss wither = helper.spawn(EntityType.WITHER, SPAWN_POS);
        final MagneticLinkAbility ability = new MagneticLinkAbility();

        // Fresh instance: no link active — must accept UNPREDICTABLE
        if (!ability.canActivate(wither, BossArchetype.UNPREDICTABLE, 0.5f)) {
            helper.fail(
                    "MagneticLinkAbility.canActivate should return true for UNPREDICTABLE"
                    + " when no link is active");
            return;
        }

        if (ability.canActivate(wither, BossArchetype.BALANCED, 0.5f)) {
            helper.fail(
                    "MagneticLinkAbility.canActivate should return false for BALANCED");
            return;
        }

        helper.succeed();
    }
}
