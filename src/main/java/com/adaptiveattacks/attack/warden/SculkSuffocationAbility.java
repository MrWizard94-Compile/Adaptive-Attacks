package com.adaptiveattacks.attack.warden;

import com.adaptiveattacks.ability.BossAbility;
import com.adaptiveboss.adaptation.BossArchetype;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Warden KITE ability: converts floor blocks in a 5-block radius to Sculk,
 * then applies Slowness III to players standing on sculk each tick.
 *
 * <p>Original blocks are restored after 10 seconds. Activates in the KITER archetype.
 */
public final class SculkSuffocationAbility implements BossAbility {

    private static final int COOLDOWN = 400;
    private static final int DURATION = 200;
    private static final int RADIUS = 5;
    private static final int EFFECT_DURATION = 40;

    private final Map<BlockPos, BlockState> savedBlocks = new ConcurrentHashMap<>();
    private int activeCountdown = -1;

    @Override
    @NotNull
    public String id() {
        return "sculk_suffocation";
    }

    @Override
    public boolean canActivate(
            @NotNull final Mob boss,
            @NotNull final BossArchetype archetype,
            final float healthPct) {
        return archetype == BossArchetype.KITER && activeCountdown < 0;
    }

    @Override
    public void onActivate(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers) {
        savedBlocks.clear();
        final BlockPos center = boss.blockPosition();
        final BlockState sculk = Blocks.SCULK.defaultBlockState();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                final BlockPos pos = center.offset(dx, -1, dz);
                final BlockState existing = level.getBlockState(pos);
                if (existing.isSolid() && !existing.is(Blocks.SCULK)) {
                    savedBlocks.put(pos, existing);
                    level.setBlock(pos, sculk, 3);
                }
            }
        }
        activeCountdown = DURATION;
    }

    @Override
    public void onTick(
            @NotNull final Mob boss,
            @NotNull final ServerLevel level,
            @NotNull final List<ServerPlayer> activePlayers,
            final int currentTick) {
        if (activeCountdown < 0) {
            return;
        }
        activeCountdown--;
        for (final ServerPlayer player : activePlayers) {
            final BlockState below = level.getBlockState(player.blockPosition().below());
            if (below.is(Blocks.SCULK)) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, EFFECT_DURATION, 2, false, false));
            }
        }
        if (activeCountdown <= 0) {
            restoreBlocks(level);
            activeCountdown = -1;
        }
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN;
    }

    private void restoreBlocks(@NotNull final ServerLevel level) {
        for (final Map.Entry<BlockPos, BlockState> entry : savedBlocks.entrySet()) {
            level.setBlock(entry.getKey(), entry.getValue(), 3);
        }
        savedBlocks.clear();
    }
}
