package net.xiaoyang010.ex_enigmaticlegacy.Effect;

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

    // 瞬移范围设置
    private static final int MIN_TELEPORT_RANGE = 3;
    private static final int MAX_TELEPORT_RANGE = 15;
    private static final int MAX_ATTEMPTS = 20; // 寻找安全位置的最大尝试次数

    public RandomTeleport() {
        super(MobEffectCategory.HARMFUL, 0x8B00FF); // 紫色效果
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && !player.level.isClientSide) {
            performRandomTeleport(player, amplifier);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 基础间隔40tick（2秒），每级减少5tick，最短间隔10tick（0.5秒）
        int interval = Math.max(1, 100 - (amplifier * 5));
        return duration % interval == 0;
    }

    /**
     * 执行随机瞬移
     * @param player 玩家
     * @param amplifier 效果等级
     */
    private void performRandomTeleport(Player player, int amplifier) {
        Level level = player.level;
        BlockPos currentPos = player.blockPosition();

        // 根据等级调整瞬移范围，等级越高范围越大
        int range = Math.min(MAX_TELEPORT_RANGE, MIN_TELEPORT_RANGE + amplifier * 2);

        BlockPos targetPos = findSafeTeleportLocation(level, currentPos, range);

        if (targetPos != null) {
            // 执行瞬移
            Vec3 targetVec = new Vec3(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);

            // 播放瞬移音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

            // 瞬移玩家
            player.teleportTo(targetVec.x, targetVec.y, targetVec.z);

            // 在目标位置播放音效
            level.playSound(null, targetVec.x, targetVec.y, targetVec.z,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

            // 重置下落距离
            player.fallDistance = 0.0F;
        }
    }

    /**
     * 寻找安全的瞬移位置
     * @param level 世界
     * @param centerPos 中心位置
     * @param range 搜索范围
     * @return 安全的瞬移位置，如果找不到则返回null
     */
    private BlockPos findSafeTeleportLocation(Level level, BlockPos centerPos, int range) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            // 在范围内随机选择位置
            int x = centerPos.getX() + random.nextInt(range * 2 + 1) - range;
            int z = centerPos.getZ() + random.nextInt(range * 2 + 1) - range;

            // 寻找合适的Y坐标（从当前位置上下搜索）
            for (int yOffset = -5; yOffset <= 10; yOffset++) {
                int y = centerPos.getY() + yOffset;
                BlockPos testPos = new BlockPos(x, y, z);

                if (isSafeTeleportLocation(level, testPos)) {
                    return testPos;
                }
            }
        }

        return null; // 找不到安全位置
    }

    /**
     * 检查位置是否安全用于瞬移
     * @param level 世界
     * @param pos 位置
     * @return 是否安全
     */
    private boolean isSafeTeleportLocation(Level level, BlockPos pos) {
        // 检查位置是否在世界边界内
        if (pos.getY() < level.getMinBuildHeight() || pos.getY() > level.getMaxBuildHeight() - 2) {
            return false;
        }

        BlockPos feetPos = pos;
        BlockPos headPos = pos.above();
        BlockPos floorPos = pos.below();

        BlockState feetBlock = level.getBlockState(feetPos);
        BlockState headBlock = level.getBlockState(headPos);
        BlockState floorBlock = level.getBlockState(floorPos);

        // 检查脚部和头部位置是否为空气或可通过的方块
        boolean feetClear = !feetBlock.getMaterial().blocksMotion() ||
                feetBlock.getMaterial().isLiquid();
        boolean headClear = !headBlock.getMaterial().blocksMotion() ||
                headBlock.getMaterial().isLiquid();

        // 检查脚下是否有支撑（不是空气、岩浆或虚空）
        boolean hasSupport = floorBlock.getMaterial().isSolid() &&
                !floorBlock.getMaterial().isLiquid();

        // 额外检查：不要瞬移到岩浆里
        boolean notInLava = !feetBlock.getMaterial().isLiquid() ||
                !feetBlock.getMaterial().toString().contains("lava");

        return feetClear && headClear && hasSupport && notInLava;
    }
}