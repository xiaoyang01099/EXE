package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

import java.math.BigInteger;
import java.util.List;

/**
 * 炼金杜鹃花 - Alchemy Azalea
 * 基于放置时间递增EMC产出，时间越长产出越高
 */
public class AlchemyAzaleaTile extends TileEntityFunctionalFlower {

    private static final String TAG_PLACEMENT_TIME = "placementTime";
    private static final String TAG_ACCUMULATED_EMC = "accumulatedEmc";

    // 时间递增EMC配置
    private static final int BASE_EMC_PER_SECOND = 8000; // 初始每秒8000 EMC
    private static final int MAX_MULTIPLIER = 100; // 最大倍数
    private static final int TICKS_PER_DAY = 24000; // MC一天的tick数
    private static final int MANA_COST_PER_EMC = 1; // 每1EMC消耗1mana
    private static final int RANGE = 8; // 影响范围

    // 状态变量
    private long placementTime = 0;
    private long accumulatedEmc = 0;

    public AlchemyAzaleaTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<AlchemyAzaleaTile> {
        public FunctionalWandHud(AlchemyAzaleaTile flower) {
            super(flower);
        }
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (level == null || level.isClientSide) {
            return;
        }

        // 初始化放置时间
        if (placementTime == 0) {
            placementTime = level.getGameTime();
            setChanged();
        }

        // 每秒执行EMC生成
        if (ticksExisted % 20 == 0) {
            generateEMC(ticksExisted);
        }

        // 每0.5秒产生粒子效果
        if (ticksExisted % 10 == 0) {
            spawnParticles();
        }
    }

    /**
     * 计算当前的EMC倍数
     */
    private int getCurrentMultiplier() {
        if (level == null) return 1;

        long existTime = level.getGameTime() - placementTime;
        int daysElapsed = (int) (existTime / TICKS_PER_DAY);

        // 每天翻倍，最高100倍
        int multiplier = (int) Math.pow(2, daysElapsed);
        return Math.min(multiplier, MAX_MULTIPLIER);
    }

    /**
     * 获取当前EMC产出率
     */
    public int getCurrentEmcRate() {
        return BASE_EMC_PER_SECOND * getCurrentMultiplier();
    }

    /**
     * 生成EMC
     */
    private void generateEMC(int tick) {
        int day = Math.min(tick / 360, 100) + 1;
        int emcPerSecond = Math.min(8000 * (int)Math.pow(2, day), 1000000);  //EMC

        int manaCost = Math.toIntExact(emcPerSecond * MANA_COST_PER_EMC);
        if (getMana() < manaCost) {
            return;
        }

        addMana(-manaCost);
        accumulatedEmc += emcPerSecond;

        if (accumulatedEmc >= 1) {
            distributeEMC(accumulatedEmc);
            accumulatedEmc = 0;
        }
    }

    /**
     * 分发EMC给附近玩家
     */
    private void distributeEMC(long totalEmc) {
        AABB searchArea = new AABB(
                getEffectivePos().offset(-RANGE, -RANGE, -RANGE),
                getEffectivePos().offset(RANGE, RANGE, RANGE)
        );

        List<Player> players = null;
        if (level != null) {
            players = level.getEntitiesOfClass(Player.class, searchArea);
        }
        if (players.isEmpty()) return;

        long emcPerPlayer = totalEmc / players.size();
        if (emcPerPlayer == 0) return;

        for (Player player : players) {
            giveEMCToPlayer(player, emcPerPlayer);
        }

        //level.playSound(null, getBlockPos(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                //SoundSource.BLOCKS, 0.3f, 1.5f);
    }

    /**
     * 给玩家添加EMC
     */
    private void giveEMCToPlayer(Player player, long emc) {
        try {
            LazyOptional<IKnowledgeProvider> knowledgeCap = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);

            knowledgeCap.ifPresent(knowledge -> {
                BigInteger currentEmc = knowledge.getEmc();
                BigInteger newEmc = currentEmc.add(BigInteger.valueOf(emc));
                knowledge.setEmc(newEmc);
                knowledge.sync((ServerPlayer) player);
            });
        } catch (Exception e) {
            // 静默处理ProjectE API异常
        }
    }

    /**
     * 生成粒子效果
     */
    private void spawnParticles() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        double x = getBlockPos().getX() + 0.5;
        double y = getBlockPos().getY() + 0.7;
        double z = getBlockPos().getZ() + 0.5;

        int multiplier = getCurrentMultiplier();

        if (multiplier >= 50) {
            // 高倍数：炫酷粒子
            serverLevel.sendParticles(ParticleTypes.FIREWORK, x, y, z, 10, 0.5, 0.3, 0.5, 0.1);
            serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 8, 0.4, 0.2, 0.4, 0.05);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, x, y, z, 15, 0.6, 0.4, 0.6, 0.2);
        } else if (multiplier >= 10) {
            // 中等倍数：金色粒子
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 6, 0.4, 0.2, 0.4, 0.05);
            serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 4, 0.3, 0.2, 0.3, 0.03);
        } else {
            // 低倍数：基础粒子
            serverLevel.sendParticles(ParticleTypes.COMPOSTER, x, y, z, 3, 0.3, 0.2, 0.3, 0.03);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 2, 0.2, 0.1, 0.2, 0.02);
        }
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }

    @Override
    public int getMaxMana() {
        return 1000000;
    }

    @Override
    public int getColor() {
        int multiplier = getCurrentMultiplier();

        if (multiplier >= 50) return 0xFF00FF; // 紫色
        if (multiplier >= 20) return 0xFF6600; // 橙色
        if (multiplier >= 10) return 0xFFD700; // 金色
        if (multiplier >= 4) return 0x00BFFF;  // 天蓝色
        return 0x4CAF50; // 绿色
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        placementTime = cmp.getLong(TAG_PLACEMENT_TIME);
        accumulatedEmc = cmp.getLong(TAG_ACCUMULATED_EMC);
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putLong(TAG_PLACEMENT_TIME, placementTime);
        cmp.putLong(TAG_ACCUMULATED_EMC, accumulatedEmc);
    }

    // 工具方法
    public int getDaysElapsed() {
        if (level == null) return 0;
        long existTime = level.getGameTime() - placementTime;
        return (int) (existTime / TICKS_PER_DAY);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap,
                LazyOptional.of(() -> new AlchemyAzaleaTile.FunctionalWandHud(this)).cast());
    }

    public long getTicksToNextUpgrade() {
        if (getCurrentMultiplier() >= MAX_MULTIPLIER) return -1;

        int currentDays = getDaysElapsed();
        long nextUpgradeTime = placementTime + ((long) (currentDays + 1) * TICKS_PER_DAY);

        return level == null ? 0 : Math.max(0, nextUpgradeTime - level.getGameTime());
    }
}