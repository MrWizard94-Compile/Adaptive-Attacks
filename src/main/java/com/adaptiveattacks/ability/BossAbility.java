package com.adaptiveattacks.ability;

import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a single special-attack ability that can be assigned to a boss.
 *
 * <p>Abilities are managed by an {@link AbilityScheduler}. The scheduler calls
 * {@link #onTick} every server tick and {@link #onActivate} when the ability
 * fires (cooldown elapsed and {@link #canActivate} returned {@code true}).
 */
public interface BossAbility {

    /**
     * Returns a unique string identifier for this ability, used for cooldown tracking.
     *
     * @return the ability identifier; never {@code null}
     */
    @NotNull String id();

    /**
     * Returns {@code true} when this ability should fire given the current game state.
     *
     * <p>Called by the scheduler only after the cooldown has elapsed.
     *
     * @param boss      the boss entity
     * @param archetype the boss's current combat archetype
     * @param healthPct boss health as a fraction of max health (0.0–1.0)
     * @return whether the ability should activate
     */
    boolean canActivate(
            @NotNull Mob boss,
            @NotNull BossArchetype archetype,
            float healthPct);

    /**
     * Executes the ability. Called once when the ability fires.
     *
     * @param boss           the boss entity
     * @param level          the server world
     * @param activePlayers  players currently engaged in combat with this boss
     */
    void onActivate(
            @NotNull Mob boss,
            @NotNull ServerLevel level,
            @NotNull List<ServerPlayer> activePlayers);

    /**
     * Called every server tick regardless of cooldown state.
     *
     * <p>Override for abilities requiring multi-tick effects such as delayed teleports
     * or ongoing links between players. Default implementation is a no-op.
     *
     * @param boss          the boss entity
     * @param level         the server world
     * @param activePlayers players currently engaged in combat with this boss
     * @param currentTick   the current server tick count
     */
    default void onTick(
            @NotNull Mob boss,
            @NotNull ServerLevel level,
            @NotNull List<ServerPlayer> activePlayers,
            int currentTick) {
        // no-op
    }

    /**
     * Returns the minimum number of ticks between consecutive activations.
     *
     * @return cooldown in ticks
     */
    int cooldownTicks();
}
