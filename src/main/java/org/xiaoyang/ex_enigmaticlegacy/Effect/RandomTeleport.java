package org.xiaoyang.ex_enigmaticlegacy.Effect;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class RandomTeleport extends MobEffect {
    private final Random random = new Random();
    private static final int MIN_TELEPORT_RANGE = 3;
    private static final int MAX_TELEPORT_RANGE = 15;
    private static final int MAX_ATTEMPTS = 20;

    public RandomTeleport() {
        super(MobEffectCategory.HARMFUL, 0x8B00FF);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && !player.level.isClientSide) {
            performRandomTeleport(player, amplifier);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int interval = Math.max(1, 100 - (amplifier * 5));
        return duration % interval == 0;
    }

    private void performRandomTeleport(Player player, int amplifier) {
        Level level = player.level;
        BlockPos currentPos = player.blockPosition();

        int range = Math.min(MAX_TELEPORT_RANGE, MIN_TELEPORT_RANGE + amplifier * 2);

        BlockPos targetPos = findSafeTeleportLocation(level, currentPos, range);

        if (targetPos != null) {
            Vec3 targetVec = new Vec3(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

            player.teleportTo(targetVec.x, targetVec.y, targetVec.z);

            level.playSound(null, targetVec.x, targetVec.y, targetVec.z,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

            player.fallDistance = 0.0F;
        }
    }

    private BlockPos findSafeTeleportLocation(Level level, BlockPos centerPos, int range) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            int x = centerPos.getX() + random.nextInt(range * 2 + 1) - range;
            int z = centerPos.getZ() + random.nextInt(range * 2 + 1) - range;

            for (int yOffset = -5; yOffset <= 10; yOffset++) {
                int y = centerPos.getY() + yOffset;
                BlockPos testPos = new BlockPos(x, y, z);

                if (isSafeTeleportLocation(level, testPos)) {
                    return testPos;
                }
            }
        }

        return null;
    }

    private boolean isSafeTeleportLocation(Level level, BlockPos pos) {
        if (pos.getY() < level.getMinBuildHeight() || pos.getY() > level.getMaxBuildHeight() - 2) {
            return false;
        }

        BlockPos headPos = pos.above();
        BlockPos floorPos = pos.below();

        BlockState feetBlock = level.getBlockState(pos);
        BlockState headBlock = level.getBlockState(headPos);
        BlockState floorBlock = level.getBlockState(floorPos);

        boolean feetClear = !feetBlock.blocksMotion() || feetBlock.liquid();
        boolean headClear = !headBlock.blocksMotion() || headBlock.liquid();

        boolean hasSupport = floorBlock.isSolid() && !floorBlock.liquid();

        boolean notInLava = !feetBlock.getFluidState().is(net.minecraft.tags.FluidTags.LAVA);

        return feetClear && headClear && hasSupport && notInLava;
    }
}