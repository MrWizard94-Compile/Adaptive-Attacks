package com.adaptiveattacks.event;

import com.adaptiveattacks.ability.AbilityRegistry;
import com.adaptiveattacks.ability.AbilityScheduler;
import com.adaptiveattacks.attack.dragon.VulnerableExhaustionAbility;
import com.adaptiveattacks.attack.generic.kite.ReactiveSpikesAbility;
import com.adaptiveattacks.attack.warden.EcholocationShockwaveAbility;
import com.adaptiveattacks.attack.warden.TheSilenceAbility;
import com.adaptiveattacks.attack.wither.NuclearWinterAbility;
import com.adaptiveboss.registry.AdaptiveBossRegistry;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handles boss spawn/death lifecycle and intercepts damage events for reactive abilities.
 *
 * <p>Registers with low event priority so the base mod's {@code BossLifecycleHandler}
 * processes entity joins first, ensuring bosses are already tracked when SCHEDULERS
 * are created here.
 */
public final class AttackLifecycleHandler {

    /** Shared boss mob references, used by {@link AttackTickHandler} for ticking. */
    static final Map<UUID, Mob> BOSS_MOBS = new ConcurrentHashMap<>();

    /** Per-boss ability SCHEDULERS. */
    static final Map<UUID, AbilityScheduler> SCHEDULERS = new ConcurrentHashMap<>();

    /**
     * Creates an ability scheduler for any newly spawned registered boss.
     *
     * @param event the entity join event
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityJoin(@NotNull final EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }
        if (!AdaptiveBossRegistry.isRegistered(mob.getType())) {
            return;
        }
        BOSS_MOBS.put(mob.getUUID(), mob);
        SCHEDULERS.put(mob.getUUID(), AbilityRegistry.buildScheduler(mob));
    }

    /**
     * Removes the scheduler and cleans up static ability state when a boss dies.
     *
     * @param event the living death event
     */
    @SubscribeEvent
    public void onBossDeath(@NotNull final LivingDeathEvent event) {
        final UUID id = event.getEntity().getUUID();
        if (SCHEDULERS.remove(id) == null) {
            return;
        }
        BOSS_MOBS.remove(id);
        ReactiveSpikesAbility.onBossRemoved(id);
        VulnerableExhaustionAbility.onBossRemoved(id);
        NuclearWinterAbility.onBossRemoved(id);
        TheSilenceAbility.onBossRemoved(id);
        EcholocationShockwaveAbility.clearAllTags();
    }

    /**
     * Intercepts pre-damage events to apply reactive spikes reflection and
     * vulnerable-exhaustion damage doubling.
     *
     * @param event the pre-damage event
     */
    @SubscribeEvent
    public void onLivingDamagedPre(@NotNull final LivingDamageEvent.Pre event) {
        final LivingEntity entity = event.getEntity();

        // Reactive spikes: reflect 25% of melee damage back to the attacker
        if (entity instanceof Mob mob && ReactiveSpikesAbility.isActive(mob.getUUID())) {
            final Entity direct = event.getSource().getDirectEntity();
            final Entity indirect = event.getSource().getEntity();
            if (direct != null && direct == indirect && direct instanceof LivingEntity attacker) {
                attacker.hurt(
                        entity.level().damageSources().thorns(mob),
                        event.getNewDamage() * 0.25f);
                final Vec3 dir = attacker.position().subtract(mob.position()).normalize();
                attacker.push(dir.x * 1.5, 0.3, dir.z * 1.5);
                attacker.hurtMarked = true;
            }
        }

        // Vulnerable exhaustion: double incoming damage to a recharging dragon
        if (VulnerableExhaustionAbility.isVulnerable(entity.getUUID())) {
            event.setNewDamage(event.getNewDamage() * 2.0f);
        }
    }
}
