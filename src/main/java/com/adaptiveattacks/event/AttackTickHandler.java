package com.adaptiveattacks.event;

import com.adaptiveattacks.ability.AbilityScheduler;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Ticks all active {@link AbilityScheduler} instances once per server tick.
 */
public final class AttackTickHandler {

    /**
     * Called every server tick post-update. Iterates all living tracked bosses
     * and advances their ability schedulers.
     *
     * @param event the server post-tick event
     */
    @SubscribeEvent
    public void onServerTick(@NotNull final ServerTickEvent.Post event) {
        final int currentTick = event.getServer().getTickCount();
        for (final Map.Entry<UUID, AbilityScheduler> entry
                : AttackLifecycleHandler.SCHEDULERS.entrySet()) {
            final Mob mob = AttackLifecycleHandler.BOSS_MOBS.get(entry.getKey());
            if (mob == null || !mob.isAlive()) {
                continue;
            }
            if (!(mob.level() instanceof ServerLevel level)) {
                continue;
            }
            entry.getValue().tick(mob, level, currentTick);
        }
    }
}
