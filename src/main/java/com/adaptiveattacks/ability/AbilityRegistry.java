package com.adaptiveattacks.ability;

import com.adaptiveattacks.attack.dragon.DragonOverdriveAbility;
import com.adaptiveattacks.attack.dragon.HomingFireballAbility;
import com.adaptiveattacks.attack.dragon.VulnerableExhaustionAbility;
import com.adaptiveattacks.attack.generic.hybrid.ChaosSwapAbility;
import com.adaptiveattacks.attack.generic.hybrid.DividingWallAbility;
import com.adaptiveattacks.attack.generic.hybrid.MagneticLinkAbility;
import com.adaptiveattacks.attack.generic.kite.ReactiveSpikesAbility;
import com.adaptiveattacks.attack.generic.kite.RepulsionNovaAbility;
import com.adaptiveattacks.attack.generic.kite.ToxicExhaustAbility;
import com.adaptiveattacks.attack.generic.rush.GapCloserAbility;
import com.adaptiveattacks.attack.generic.rush.KineticHarpoonAbility;
import com.adaptiveattacks.attack.generic.rush.ShadowStepAbility;
import com.adaptiveattacks.attack.warden.EchoStrikeAbility;
import com.adaptiveattacks.attack.warden.EcholocationShockwaveAbility;
import com.adaptiveattacks.attack.warden.SculkSuffocationAbility;
import com.adaptiveattacks.attack.warden.TheSilenceAbility;
import com.adaptiveattacks.attack.wither.FreezingDarkAbility;
import com.adaptiveattacks.attack.wither.NuclearWinterAbility;
import com.adaptiveattacks.attack.wither.ShatterWaveAbility;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

/**
 * Builds the correct {@link AbilityScheduler} for a given boss entity.
 *
 * <p>All bosses receive the generic archetype-driven abilities. Boss-specific
 * abilities are appended based on entity type.
 */
public final class AbilityRegistry {

    private AbilityRegistry() {
        // utility class
    }

    /**
     * Creates an {@link AbilityScheduler} pre-loaded with the appropriate abilities
     * for the given boss.
     *
     * @param boss the boss entity being registered
     * @return a configured scheduler ready for ticking
     */
    @NotNull
    public static AbilityScheduler buildScheduler(@NotNull final Mob boss) {
        final List<BossAbility> abilities = new java.util.ArrayList<>();

        // Generic abilities — all registered bosses
        abilities.add(new KineticHarpoonAbility());
        abilities.add(new ShadowStepAbility());
        abilities.add(new GapCloserAbility());
        abilities.add(new RepulsionNovaAbility());
        abilities.add(new ToxicExhaustAbility());
        abilities.add(new ReactiveSpikesAbility());
        abilities.add(new MagneticLinkAbility());
        abilities.add(new ChaosSwapAbility());
        abilities.add(new DividingWallAbility());

        // Boss-specific abilities
        if (boss.getType() == EntityType.ENDER_DRAGON) {
            abilities.add(new DragonOverdriveAbility());
            abilities.add(new HomingFireballAbility());
            abilities.add(new VulnerableExhaustionAbility());
        } else if (boss.getType() == EntityType.WITHER) {
            abilities.add(new NuclearWinterAbility());
            abilities.add(new FreezingDarkAbility());
            abilities.add(new ShatterWaveAbility());
        } else if (boss.getType() == EntityType.WARDEN) {
            abilities.add(new EcholocationShockwaveAbility());
            abilities.add(new EchoStrikeAbility());
            abilities.add(new SculkSuffocationAbility());
            abilities.add(new TheSilenceAbility());
        }

        return new AbilityScheduler(abilities);
    }
}
