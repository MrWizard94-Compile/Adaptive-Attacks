package com.adaptiveattacks.attack.warden;

import com.adaptiveattacks.ability.BossAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

/**
 * Warden passive: 0.5 seconds after a player is Tagged by
 * {@link EcholocationShockwaveAbility}, a mini sonic boom erupts under their feet.
 *
 * <p>Never activates via the scheduler — runs entirely in {@link #onTick},
 * consuming entries from {@link EcholocationShockwaveAbility#TAGGED_PLAYERS}.
 */
public final class EchoStrikeAbility implements BossAbility {

    @Override
    @NotNull
    public String id() {
        return "echo_strike";
    }

    @Override
    public boolean canActivate(
            @NotNull final Mob boss,
            @NotNull final BossArchetype archetype,
            final float healthPct) {
        return false;
    }

    @Override
    public void onActivate(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers) {
        // never called
    }

    @Override
    public void onTick(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers,
            final int currentTick) {
        final Map<UUID, Integer> tagged = EcholocationShockwaveAbility.TAGGED_PLAYERS;
        final Iterator<Map.Entry<UUID, Integer>> iter = tagged.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<UUID, Integer> entry = iter.next();
            final int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                iter.remove();
                strikePlayer(level, entry.getKey());
            } else {
                entry.setValue(remaining);
            }
        }
    }

    @Override
    public int cooldownTicks() {
        return 0;
    }

    private static void strikePlayer(
            @NotNull final ServerLevel level,
            @NotNull final UUID playerId) {
        final ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
        if (player == null) {
            return;
        }
        final LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
        bolt.moveTo(player.getX(), player.getY(), player.getZ());
        bolt.setVisualOnly(false);
        level.addFreshEntity(bolt);
    }
}
