package com.adaptiveattacks.ability;

import com.adaptiveboss.AdaptiveBossMod;
import com.adaptiveboss.adaptation.AdaptationProfile;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

/**
 * Manages the lifecycle and cooldowns of all {@link BossAbility} instances for a single boss.
 *
 * <p>One scheduler is created per tracked boss on spawn and ticked each server tick by
 * {@link com.adaptiveattacks.event.AttackTickHandler}.
 */
public final class AbilityScheduler {

    private final List<BossAbility> abilities;
    private final Map<String, Integer> cooldowns = new ConcurrentHashMap<>();

    /**
     * Creates a scheduler with the given set of abilities.
     *
     * @param abilities the abilities to manage; copied defensively
     */
    public AbilityScheduler(@NotNull final List<BossAbility> abilities) {
        this.abilities = List.copyOf(abilities);
    }

    /**
     * Ticks all abilities. Must be called once per server tick.
     *
     * @param boss        the boss entity
     * @param level       the server world
     * @param currentTick the current server tick
     */
    public void tick(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            final int currentTick) {
        final AdaptationProfile profile =
                AdaptiveBossMod.getTracker().getAdaptationProfile(boss.getUUID());
        if (profile == null) {
            return;
        }
        final BossArchetype archetype = profile.getCurrentArchetype();
        final float healthPct = boss.getHealth() / boss.getMaxHealth();
        final List<ServerPlayer> players = resolvePlayers(boss.getUUID(), level, currentTick);

        for (final BossAbility ability : abilities) {
            ability.onTick(boss, level, players, currentTick);

            final int remaining = cooldowns.getOrDefault(ability.id(), 0);
            if (remaining > 0) {
                cooldowns.put(ability.id(), remaining - 1);
                continue;
            }
            if (ability.canActivate(boss, archetype, healthPct)) {
                ability.onActivate(boss, level, players);
                cooldowns.put(ability.id(), ability.cooldownTicks());
            }
        }
    }

    @NotNull
    private static List<ServerPlayer> resolvePlayers(
            @NotNull final UUID bossId,
            @NotNull final ServerLevel level,
            final int currentTick) {
        final Set<UUID> ids = AdaptiveBossMod.getTracker().getActivePlayers(bossId, currentTick);
        final List<ServerPlayer> players = new java.util.ArrayList<>();
        for (final UUID id : ids) {
            final ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }
}
