package com.adaptiveattacks.gametest;

import com.adaptiveattacks.AdaptiveAttacksMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Registers all Adaptive Attacks game-test classes with NeoForge's test runner.
 *
 * <p>Wired via the mod event bus so tests are picked up by
 * {@code ./gradlew runGameTestServer}.
 */
@EventBusSubscriber(modid = AdaptiveAttacksMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class AdaptiveAttacksGameTests {

    private AdaptiveAttacksGameTests() { }

    /**
     * Registers game-test holder classes.
     *
     * @param event the registration event
     */
    @SubscribeEvent
    public static void register(@NotNull final RegisterGameTestsEvent event) {
        event.register(NuclearWinterGameTest.class);
        event.register(HomingFireballGameTest.class);
        event.register(DragonAbilityGameTest.class);
        event.register(WitherAbilityGameTest.class);
        event.register(WardenAbilityGameTest.class);
        event.register(GenericAbilityGameTest.class);
    }
}
