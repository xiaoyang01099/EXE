package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.List;

/**
 * 炼金向日葵 - Alchemy Sunflower
 * 通过消耗魔力为附近的玩家产生EMC
 */
public class AlchemySunflowerTile extends TileEntityFunctionalFlower {

    private static final String TAG_ENHANCED_MODE = "enhancedMode";
    private static final String TAG_ENHANCEMENT_TIME = "enhancementTime";
    private static final String TAG_ACCUMULATED_EMC = "accumulatedEmc";

    // ====== EMC产量配置区域 ======
    //高产量配置：普通模式2000 EMC/秒，强化模式8000 EMC/秒
    private static final int MANA_TO_EMC_RATIO = 1; // 1 mana = 1 EMC (超高转换效率)
    private static final int ENHANCED_MANA_TO_EMC_RATIO = 1; // 强化模式：1 mana = 1 EMC (保持1:1比例)
    private static final int BASE_MANA_COST_PER_TICK = 2000; // 基础每秒消耗2000 mana = 2000 EMC
    private static final int ENHANCED_MANA_COST_PER_TICK = 8000; // 强化模式每秒消耗8000 mana = 8000 EMC

    // 强化模式配置
    private static final int ENHANCEMENT_COST = 50000; // 激活强化模式的mana消耗（增加以匹配高消耗）
    private static final int ENHANCEMENT_DURATION = 12000; // 强化持续时间(10分钟)
    private static final int AUTO_ENHANCE_THRESHOLD = 100000; // 自动强化的mana阈值（增加以匹配高消耗）

    // 范围配置
    private static final int BASE_RANGE = 5; // 基础范围5格
    private static final int ENHANCED_RANGE = 8; // 强化范围8格

    // 状态变量
    private boolean enhancedMode = false;
    private int enhancementTimeLeft = 0;
    private long accumulatedEmc = 0; // 累积的EMC，达到整数时分发

    public AlchemySunflowerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<AlchemySunflowerTile> {
        public FunctionalWandHud(AlchemySunflowerTile flower) {
            super(flower);
        }
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (level == null || level.isClientSide) {
            return;
        }

        // 更新强化模式状态
        updateEnhancedMode();

        // 自动检查是否可以激活强化模式
        checkAutoEnhancement();

        // 执行EMC生成
        if (ticksExisted % 20 == 0) { // 每秒执行一次
            generateEMC();
        }

        // 粒子效果
        if (ticksExisted % 10 == 0) {
            spawnParticles();
        }
    }

    /**
     * 更新强化模式状态
     */
    private void updateEnhancedMode() {
        if (enhancedMode) {
            enhancementTimeLeft--;
            if (enhancementTimeLeft <= 0) {
                enhancedMode = false;
                setChanged();

                // 播放强化结束音效
                if (level != null) {
                    level.playSound(null, getBlockPos(), SoundEvents.BEACON_DEACTIVATE,
                            SoundSource.BLOCKS, 0.5f, 1.0f);
                }
            }
        }
    }

    /**
     * 检查并自动激活强化模式
     */
    private void checkAutoEnhancement() {
        if (!enhancedMode && getMana() >= AUTO_ENHANCE_THRESHOLD) {
            activateEnhancement();
        }
    }

    /**
     * 激活强化模式
     */
    public void activateEnhancement() {
        if (!enhancedMode && getMana() >= ENHANCEMENT_COST) {
            addMana(-ENHANCEMENT_COST);
            enhancedMode = true;
            enhancementTimeLeft = ENHANCEMENT_DURATION;
            setChanged();

            // 播放强化激活音效
            if (level != null) {
                level.playSound(null, getBlockPos(), SoundEvents.BEACON_ACTIVATE,
                        SoundSource.BLOCKS, 1.0f, 1.2f);
            }

        }
    }

    /**
     * 生成EMC
     */
    private void generateEMC() {
        int manaCostPerSecond = enhancedMode ? ENHANCED_MANA_COST_PER_TICK : BASE_MANA_COST_PER_TICK;

        if (getMana() < manaCostPerSecond) {
            return;
        }

        // 消耗mana
        addMana(-manaCostPerSecond);

        // 计算产生的EMC
        int ratio = enhancedMode ? ENHANCED_MANA_TO_EMC_RATIO : MANA_TO_EMC_RATIO;
        double emcGenerated = (double) manaCostPerSecond / ratio;

        // 累积EMC
        accumulatedEmc += (long)(emcGenerated * 1000); // 乘以1000保持精度

        // 检查是否可以分发EMC
        if (accumulatedEmc >= 1000) { // 达到1 EMC
            long emcToDistribute = accumulatedEmc / 1000;
            accumulatedEmc %= 1000;

            distributeEMC(emcToDistribute);
        }
    }

    /**
     * 分发EMC给附近玩家
     */
    private void distributeEMC(long totalEmc) {
        int range = enhancedMode ? ENHANCED_RANGE : BASE_RANGE;
        AABB searchArea = new AABB(
                getEffectivePos().offset(-range, -range, -range),
                getEffectivePos().offset(range, range, range)
        );

        List<Player> players = null;
        if (level != null) {
            players = level.getEntitiesOfClass(Player.class, searchArea);
        }

        if (players == null || players.isEmpty()) {
            return;
        }

        // 平均分配EMC
        long emcPerPlayer = totalEmc / players.size();
        if (emcPerPlayer == 0) {
            return;
        }

        for (Player player : players) {
            giveEMCToPlayer(player, emcPerPlayer);
        }

        // 播放音效
        level.playSound(null, getBlockPos(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.BLOCKS, 0.3f, 1.5f);
    }

    /**
     * 给玩家添加EMC
     */
    private void giveEMCToPlayer(Player player, long emc) {
        try {
            // 获取玩家的知识提供者能力
            LazyOptional<IKnowledgeProvider> knowledgeCap = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);

            knowledgeCap.ifPresent(knowledge -> {
                // 获取当前EMC
                BigInteger currentEmc = knowledge.getEmc();

                // 添加新的EMC
                BigInteger newEmc = currentEmc.add(BigInteger.valueOf(emc));

                // 设置新的EMC值
                knowledge.setEmc(newEmc);

                // 同步到客户端
                knowledge.sync((ServerPlayer) player);
            });

        } catch (Exception e) {
            // 如果ProjectE API出现问题，静默处理
            // 可以在这里添加日志记录
        }
    }

    /**
     * 生成粒子效果
     */
    private void spawnParticles() {
        if (level instanceof ServerLevel serverLevel) {
            // 基础粒子效果
            double x = getBlockPos().getX() + 0.5;
            double y = getBlockPos().getY() + 0.7;
            double z = getBlockPos().getZ() + 0.5;

            if (enhancedMode) {
                // 强化模式：更多金色粒子（匹配高产量）
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        x, y, z, 8, 0.4, 0.2, 0.4, 0.05);

                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        x, y, z, 6, 0.3, 0.2, 0.3, 0.03);

                // 额外的金色粒子
                serverLevel.sendParticles(ParticleTypes.FIREWORK,
                        x, y, z, 4, 0.2, 0.1, 0.2, 0.02);
            } else {
                // 普通模式：更多绿色粒子（匹配高产量）
                serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                        x, y, z, 5, 0.3, 0.2, 0.3, 0.03);

                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        x, y, z, 3, 0.2, 0.1, 0.2, 0.02);
            }
        }
    }

    @Override
    public RadiusDescriptor getRadius() {
        int range = enhancedMode ? ENHANCED_RANGE : BASE_RANGE;
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), range);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap,
                LazyOptional.of(() -> new FunctionalWandHud(this)).cast());
    }

    @Override
    public int getMaxMana() {
        return 200000; // 大幅增加mana容量以支持高消耗模式
    }

    @Override
    public int getColor() {
        if (enhancedMode) {
            return 0xFFD700;
        } else {
            return 0x4CAF50;
        }
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        enhancedMode = cmp.getBoolean(TAG_ENHANCED_MODE);
        enhancementTimeLeft = cmp.getInt(TAG_ENHANCEMENT_TIME);
        accumulatedEmc = cmp.getLong(TAG_ACCUMULATED_EMC);
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putBoolean(TAG_ENHANCED_MODE, enhancedMode);
        cmp.putInt(TAG_ENHANCEMENT_TIME, enhancementTimeLeft);
        cmp.putLong(TAG_ACCUMULATED_EMC, accumulatedEmc);
    }

    // Getter方法
    public boolean isEnhanced() {
        return enhancedMode;
    }

    public int getEnhancementTimeLeft() {
        return enhancementTimeLeft;
    }

    public double getCurrentEmcRate() {
        int manaCost = enhancedMode ? ENHANCED_MANA_COST_PER_TICK : BASE_MANA_COST_PER_TICK;
        int ratio = enhancedMode ? ENHANCED_MANA_TO_EMC_RATIO : MANA_TO_EMC_RATIO;
        return (double) manaCost / ratio; // EMC/秒
    }
}