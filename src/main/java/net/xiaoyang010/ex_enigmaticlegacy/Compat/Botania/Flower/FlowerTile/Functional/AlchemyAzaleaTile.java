package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional;

import com.mojang.blaze3d.vertex.PoseStack;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

import java.math.BigInteger;
import java.util.List;

public class AlchemyAzaleaTile extends TileEntityFunctionalFlower {

    private static final String TAG_PLACEMENT_TIME = "placementTime";
    private static final String TAG_ACCUMULATED_EMC = "accumulatedEmc";
    private static final String TAG_INITIALIZED = "initialized";
    private static final String TAG_LAST_MULTIPLIER = "lastMultiplier";
    private static final String TAG_LAST_UPDATE = "lastUpdate";
    private static final String TAG_CURRENT_MULTIPLIER = "currentMultiplier";
    private static final String TAG_CURRENT_EMC_RATE = "currentEmcRate";
    private static final String TAG_CURRENT_MANA_COST = "currentManaCost";

    // 重新平衡的配置
    private static final int BASE_EMC_PER_SECOND = 1000; // 降低基础产出
    private static final int MAX_MULTIPLIER = 2147483647; // 安全的最大倍数 (2^30)
    private static final int TICKS_PER_DAY = 6000; // MC一天的tick数
    private static final int MANA_COST_BASE = 100; // 基础魔力消耗，不随倍数线性增长
    private static final int RANGE = 8; // 影响范围
    private static final int MAX_DAYS_FOR_CALCULATION = 30; // 用于计算的最大天数

    // 状态变量
    private long placementTime = -1;
    private long accumulatedEmc = 0;
    private boolean initialized = false;
    private int lastMultiplier = 1;
    private long lastUpdate = 0; // 用于确保时间连续性

    // 客户端缓存数据（用于HUD显示）
    private int cachedMultiplier = 1;
    private long cachedEmcRate = BASE_EMC_PER_SECOND;
    private int cachedManaCost = MANA_COST_BASE;


    public AlchemyAzaleaTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<AlchemyAzaleaTile> {
        public FunctionalWandHud(AlchemyAzaleaTile flower) {
            super(flower);
        }

        @Override
        public void renderHUD(PoseStack ms, Minecraft mc) {
            super.renderHUD(ms, mc);

            int days = flower.getDaysElapsed();
            int multiplier = flower.getDisplayMultiplier();
            long ticksToNext = flower.getTicksToNextUpgrade();

            String upgradeInfo;
            if (ticksToNext == -1 || multiplier >= MAX_MULTIPLIER) {
                upgradeInfo = "§6Max Level (" + multiplier + "x)";
            } else {
                int secondsToNext = (int) (ticksToNext / 20);
                int minutesToNext = secondsToNext / 60;
                int hoursToNext = minutesToNext / 60;

                if (hoursToNext > 0) {
                    upgradeInfo = String.format("§eNext upgrade: %dh %dm", hoursToNext, minutesToNext % 60);
                } else if (minutesToNext > 0) {
                    upgradeInfo = String.format("§eNext upgrade: %dm %ds", minutesToNext, secondsToNext % 60);
                } else {
                    upgradeInfo = String.format("§eNext upgrade: %ds", secondsToNext);
                }
            }

            String statusInfo = String.format("§aDays: %d | Multiplier: %dx | Rate: %,d EMC/s",
                    days, multiplier, flower.getDisplayEmcRate());

            String manaInfo = String.format("§bMana: %d/%d | Cost: %d/s",
                    flower.getMana(), flower.getMaxMana(), flower.getDisplayManaCost());

            mc.font.draw(ms, statusInfo, 10, 40, 0xFFFFFF);
            mc.font.draw(ms, upgradeInfo, 10, 52, 0xFFFFFF);
            mc.font.draw(ms, manaInfo, 10, 64, 0xFFFFFF);
        }
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (level == null) {
            return;
        }

        if (level.isClientSide) {
            updateClientCache();
            return;
        }

        if (!initialized) {
            if (placementTime == -1) {
                placementTime = level.getGameTime();
                lastUpdate = placementTime;
                setChanged();
            }
            initialized = true;
            setChanged();
        }

        if (ticksExisted % 20 == 0) {
            generateEMC();
        }

        if (ticksExisted % 10 == 0) {
            spawnParticles();
        }

        if (ticksExisted % 40 == 0) {
            syncToClient();
        }
    }

    private void updateClientCache() {
        if (ticksExisted % 10 == 0) {
            cachedMultiplier = getCurrentMultiplier();
            cachedEmcRate = getCurrentEmcRate();
            cachedManaCost = getCurrentManaCost();
        }
    }

    private void syncToClient() {
        if (level instanceof ServerLevel) {
            cachedMultiplier = getCurrentMultiplier();
            cachedEmcRate = getCurrentEmcRate();
            cachedManaCost = getCurrentManaCost();

            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    private int getCurrentMultiplier() {
        if (level == null || placementTime == -1) return 1;

        long currentTime = level.getGameTime();
        long existTime = currentTime - placementTime;

        // 防止时间回退
        if (existTime < 0) {
            placementTime = currentTime;
            existTime = 0;
        }

        int daysElapsed = (int) (existTime / TICKS_PER_DAY);

        daysElapsed = Math.max(0, daysElapsed);

        if (daysElapsed >= MAX_DAYS_FOR_CALCULATION) {
            return 2147483647; // 2^30，避免后续计算溢出
        }

        // 安全的2^n计算
        int multiplier = 1;
        for (int i = 0; i < daysElapsed && i < 30; i++) { // 限制循环次数防止无限循环
            if (multiplier > 2147483647 / 2) { // 防止下一次乘法溢出
                return 2147483647;
            }
            multiplier *= 2;
        }

        return Math.min(multiplier, 2147483647);
    }

    public long getCurrentEmcRate() {
        int multiplier = getCurrentMultiplier();
        long emcRate = (long) BASE_EMC_PER_SECOND * multiplier;
        return Math.min(emcRate, Long.MAX_VALUE / 100);
    }

    public int getCurrentManaCost() {
        int multiplier = getCurrentMultiplier();

        if (multiplier >= MAX_MULTIPLIER) {
            return 500000;
        }

        // 使用对数增长：基础消耗 + log2(倍数) * 50
        if (multiplier <= 1) {
            return MANA_COST_BASE;
        }
        return MANA_COST_BASE + (int)(Math.log(multiplier) / Math.log(2) * 50);
    }

    public int getDisplayMultiplier() {
        if (level != null && level.isClientSide) {
            return cachedMultiplier;
        }
        return getCurrentMultiplier();
    }

    public long getDisplayEmcRate() {
        if (level != null && level.isClientSide) {
            return cachedEmcRate;
        }
        return getCurrentEmcRate();
    }

    public int getDisplayManaCost() {
        if (level != null && level.isClientSide) {
            return cachedManaCost;
        }
        return getCurrentManaCost();
    }

    private void generateEMC() {
        int currentMultiplier = getCurrentMultiplier();

        if (currentMultiplier > lastMultiplier && currentMultiplier < MAX_MULTIPLIER) {
            if (level != null) {
                level.playSound(null, getBlockPos(), SoundEvents.PLAYER_LEVELUP,
                        SoundSource.BLOCKS, 1.0f, 1.0f + (Math.min(currentMultiplier, 10) * 0.1f));
            }

            if (level instanceof ServerLevel serverLevel) {
                double x = getBlockPos().getX() + 0.5;
                double y = getBlockPos().getY() + 0.7;
                double z = getBlockPos().getZ() + 0.5;

                serverLevel.sendParticles(ParticleTypes.FIREWORK, x, y, z, 20, 0.8, 0.5, 0.8, 0.2);
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 15, 0.6, 0.4, 0.6, 0.1);
            }
        }

        if (currentMultiplier >= MAX_MULTIPLIER && lastMultiplier < MAX_MULTIPLIER) {
            level.playSound(null, getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                    SoundSource.BLOCKS, 1.0f, 1.2f);

            if (level instanceof ServerLevel serverLevel) {
                double x = getBlockPos().getX() + 0.5;
                double y = getBlockPos().getY() + 0.7;
                double z = getBlockPos().getZ() + 0.5;

                // 达到最大等级的特殊粒子效果
                serverLevel.sendParticles(ParticleTypes.FIREWORK, x, y, z, 50, 1.0, 0.8, 1.0, 0.3);
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 30, 1.0, 0.6, 1.0, 0.2);
                serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 25, 0.8, 0.5, 0.8, 0.15);
            }
        }

        lastMultiplier = currentMultiplier;

        long emcPerSecond = getCurrentEmcRate();
        int manaCost = getCurrentManaCost();

        if (getMana() < manaCost) {
            return;
        }

        addMana(-manaCost);
        accumulatedEmc += emcPerSecond;

        if (accumulatedEmc > 0) {
            distributeEMC(accumulatedEmc);
            accumulatedEmc = 0;
            setChanged();
        }
    }

    private void distributeEMC(long totalEmc) {
        AABB searchArea = new AABB(
                getEffectivePos().offset(-RANGE, -RANGE, -RANGE),
                getEffectivePos().offset(RANGE, RANGE, RANGE)
        );

        List<Player> players = null;
        if (level != null) {
            players = level.getEntitiesOfClass(Player.class, searchArea);
        }
        if (players == null || players.isEmpty()) return;

        long emcPerPlayer = totalEmc / players.size();
        if (emcPerPlayer <= 0) return;

        for (Player player : players) {
            giveEMCToPlayer(player, emcPerPlayer);
        }
    }

    private void giveEMCToPlayer(Player player, long emc) {
        try {
            LazyOptional<IKnowledgeProvider> knowledgeCap = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);

            knowledgeCap.ifPresent(knowledge -> {
                BigInteger currentEmc = knowledge.getEmc();
                BigInteger newEmc = currentEmc.add(BigInteger.valueOf(emc));
                knowledge.setEmc(newEmc);
                if (player instanceof ServerPlayer serverPlayer) {
                    knowledge.sync(serverPlayer);
                }
            });
        } catch (Exception e) {
        }
    }

    private void spawnParticles() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        double x = getBlockPos().getX() + 0.5;
        double y = getBlockPos().getY() + 0.7;
        double z = getBlockPos().getZ() + 0.5;

        int multiplier = getCurrentMultiplier();
        long ticksToNext = getTicksToNextUpgrade();

        if (multiplier >= MAX_MULTIPLIER) {
            serverLevel.sendParticles(ParticleTypes.FIREWORK, x, y, z, 15, 0.8, 0.5, 0.8, 0.15);
            serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 12, 0.6, 0.4, 0.6, 0.1);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, x, y, z, 20, 0.8, 0.6, 0.8, 0.25);
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 8, 0.4, 0.3, 0.4, 0.08);
            return;
        }

        if (ticksToNext != -1 && ticksToNext <= 600) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 5, 0.3, 0.3, 0.3, 0.1);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, x, y, z, 8, 0.5, 0.3, 0.5, 0.15);

            if (ticksToNext <= 200) {
                serverLevel.sendParticles(ParticleTypes.FIREWORK, x, y, z, 3, 0.2, 0.2, 0.2, 0.05);
            }
        }

        if (multiplier >= 32) {
            serverLevel.sendParticles(ParticleTypes.FIREWORK, x, y, z, 10, 0.5, 0.3, 0.5, 0.1);
            serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 8, 0.4, 0.2, 0.4, 0.05);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, x, y, z, 15, 0.6, 0.4, 0.6, 0.2);
        } else if (multiplier >= 8) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 6, 0.4, 0.2, 0.4, 0.05);
            serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 4, 0.3, 0.2, 0.3, 0.03);
        } else {
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
        int multiplier = getCurrentMultiplier();

        if (multiplier >= MAX_MULTIPLIER) {
            return 5000000; // 500万魔力，足够消耗10秒
        }

        if (multiplier <= 1) {
            return 10000;
        }

        // 高倍数时增加更多容量
        int baseCapacity = 10000;
        int logBonus = (int)(Math.log(multiplier) / Math.log(2) * 5000);

        // 当倍数很高时（接近最大值），给予额外的容量缓冲
        if (multiplier >= 1000000) { // 100万倍数以上
            return baseCapacity + logBonus + 2000000; // 额外200万魔力
        } else if (multiplier >= 100000) { // 10万倍数以上
            return baseCapacity + logBonus + 1000000; // 额外100万魔力
        } else if (multiplier >= 10000) { // 1万倍数以上
            return baseCapacity + logBonus + 500000; // 额外50万魔力
        }

        return baseCapacity + logBonus;
    }

    @Override
    public int getColor() {
        int multiplier = getCurrentMultiplier();

        if (multiplier >= MAX_MULTIPLIER) return 0xFF00FF; // 最大等级：紫色
        if (multiplier >= 32) return 0xFF6600; // 橙色
        if (multiplier >= 16) return 0xFFD700;  // 金色
        if (multiplier >= 8) return 0x00BFFF;  // 天蓝色
        if (multiplier >= 4) return 0x4CAF50; // 绿色
        return 0x4CAF50; // 默认绿色
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writeToPacketNBT(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        readFromPacketNBT(tag);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        placementTime = cmp.getLong(TAG_PLACEMENT_TIME);
        accumulatedEmc = cmp.getLong(TAG_ACCUMULATED_EMC);
        initialized = cmp.getBoolean(TAG_INITIALIZED);
        lastMultiplier = cmp.getInt(TAG_LAST_MULTIPLIER);
        lastUpdate = cmp.getLong(TAG_LAST_UPDATE);

        // 读取缓存数据
        cachedMultiplier = cmp.getInt(TAG_CURRENT_MULTIPLIER);
        cachedEmcRate = cmp.getLong(TAG_CURRENT_EMC_RATE);
        cachedManaCost = cmp.getInt(TAG_CURRENT_MANA_COST);
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putLong(TAG_PLACEMENT_TIME, placementTime);
        cmp.putLong(TAG_ACCUMULATED_EMC, accumulatedEmc);
        cmp.putBoolean(TAG_INITIALIZED, initialized);
        cmp.putInt(TAG_LAST_MULTIPLIER, lastMultiplier);
        cmp.putLong(TAG_LAST_UPDATE, lastUpdate);

        cmp.putInt(TAG_CURRENT_MULTIPLIER, cachedMultiplier);
        cmp.putLong(TAG_CURRENT_EMC_RATE, cachedEmcRate);
        cmp.putInt(TAG_CURRENT_MANA_COST, cachedManaCost);
    }

    public int getDaysElapsed() {
        if (level == null || placementTime == -1) return 0;
        long existTime = level.getGameTime() - placementTime;
        return Math.max(0, (int) (existTime / TICKS_PER_DAY));
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap,
                LazyOptional.of(() -> new FunctionalWandHud(this)).cast());
    }

    public long getTicksToNextUpgrade() {
        if (getCurrentMultiplier() >= MAX_MULTIPLIER || placementTime == -1) return -1;

        int currentDays = getDaysElapsed();
        long nextUpgradeTime = placementTime + ((long) (currentDays + 1) * TICKS_PER_DAY);

        return level == null ? 0 : Math.max(0, nextUpgradeTime - level.getGameTime());
    }
}