package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;
import vazkii.botania.common.block.ModBlocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 嗜春兰 - Spring Devourer Orchid
 * 通过吞噬植物的生命力来产生魔力
 */
public class SpringDevourerTile extends TileEntityGeneratingFlower {

    private static final String TAG_MANA_GENERATION_COOLDOWN = "manaGenCooldown";
    private static final String TAG_DECAY_CHANCE = "decayChance";

    private static final int RANGE = 5; // 检测范围
    private static final int MAX_MANA = 6500; // 最大魔力存储
    private static final int BASE_MANA_PER_GROWTH = 160; // 每次植物生长产生的基础魔力
    private static final int MANA_GENERATION_COOLDOWN = 40; // 产魔冷却时间（tick）
    private static final float BASE_DECAY_CHANCE = 0.15f; // 基础衰变几率

    private int manaGenerationCooldown = 0;
    private float decayChance = BASE_DECAY_CHANCE;

    public SpringDevourerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (manaGenerationCooldown > 0) {
            manaGenerationCooldown--;
        }

        if (!getLevel().isClientSide && manaGenerationCooldown <= 0) {
            updateDecayChance();
            checkForPlantGrowth();
        }
    }

    /**
     * 更新衰变几率基于附近的蕴魔土和自然水晶
     */
    private void updateDecayChance() {
        decayChance = BASE_DECAY_CHANCE;
        int manaDirtCount = 0;
        int natureCrystalCount = 0;

        // 检查附近的方块
        for (BlockPos pos : BlockPos.betweenClosed(
                getBlockPos().offset(-RANGE, -2, -RANGE),
                getBlockPos().offset(RANGE, 2, RANGE))) {

            BlockState state = getLevel().getBlockState(pos);

            // 检查蕴魔土
            if (state.is(ModBlocks.enchantedSoil)) {
                manaDirtCount++;
            }

            // 检查自然水晶
            if (state.is(ModBlocks.naturaPylon)){
                natureCrystalCount++;
            }
        }

        // 蕴魔土增加衰变几率，自然水晶减少衰变几率
        decayChance += manaDirtCount * 0.05f;
        decayChance = Math.max(0.05f, decayChance - natureCrystalCount * 0.03f);
    }

    /**
     * 检查附近植物生长并产生魔力
     */
    private void checkForPlantGrowth() {
        List<BlockPos> growablePositions = new ArrayList<>();
        int totalPlants = 0;

        // 扫描范围内的所有位置
        for (BlockPos pos : BlockPos.betweenClosed(
                getBlockPos().offset(-RANGE, -1, -RANGE),
                getBlockPos().offset(RANGE, 2, RANGE))) {

            if (pos.equals(getBlockPos())) continue;

            BlockState state = getLevel().getBlockState(pos);
            Block block = state.getBlock();

            // 检查是否是可生长的植物
            if (isGrowablePlant(state, pos)) {
                totalPlants++;
                if (canGrow(state, pos)) {
                    growablePositions.add(pos.immutable());
                }
            }
        }

        // 如果有可生长的植物，选择一个进行"生长"并产魔
        if (!growablePositions.isEmpty() && getMana() < getMaxMana()) {
            Random random = getLevel().getRandom();
            BlockPos targetPos = growablePositions.get(random.nextInt(growablePositions.size()));

            // 计算魔力产生量（基于附近植物数量）
            int manaToGenerate = BASE_MANA_PER_GROWTH + (totalPlants * 8);

            // 蕴魔土额外加成
            int manaDirtBonus = countNearbyManaDirt();
            manaToGenerate += manaDirtBonus * 20;

            // 产生魔力
            addMana(manaToGenerate);

            // 可能导致植物衰变
            if (random.nextFloat() < decayChance) {
                causePlantDecay(targetPos);
            }

            // 设置冷却
            manaGenerationCooldown = MANA_GENERATION_COOLDOWN;

            // 播放音效
            getLevel().playSound(null, getBlockPos(), SoundEvents.GRASS_BREAK,
                    SoundSource.BLOCKS, 0.2F, 0.8F + random.nextFloat() * 0.4F);

            sync();
        }
    }

    /**
     * 检查方块是否是可生长的植物
     */
    private boolean isGrowablePlant(BlockState state, BlockPos pos) {
        Block block = state.getBlock();

        // 作物
        if (block instanceof CropBlock) {
            return true;
        }

        // 可骨粉催熟的植物
        if (block instanceof BonemealableBlock) {
            return ((BonemealableBlock) block).isValidBonemealTarget(getLevel(), pos, state, false);
        }

        // 树苗
        if (block.toString().contains("sapling")) {
            return true;
        }

        // 甘蔗、仙人掌、竹子等
        if (block == Blocks.SUGAR_CANE || block == Blocks.CACTUS ||
                block == Blocks.BAMBOO || block == Blocks.KELP_PLANT) {
            return true;
        }

        return false;
    }

    /**
     * 检查植物是否可以生长
     */
    private boolean canGrow(BlockState state, BlockPos pos) {
        Block block = state.getBlock();

        // 作物未完全成熟
        if (block instanceof CropBlock) {
            CropBlock crop = (CropBlock) block;
            return !crop.isMaxAge(state);
        }

        // 可骨粉催熟
        if (block instanceof BonemealableBlock) {
            BonemealableBlock bonemealable = (BonemealableBlock) block;
            return bonemealable.isValidBonemealTarget(getLevel(), pos, state, false);
        }

        // 甘蔗、仙人掌等高度限制植物
        if (block == Blocks.SUGAR_CANE || block == Blocks.CACTUS || block == Blocks.BAMBOO) {
            return getLevel().getBlockState(pos.above()).isAir() &&
                    countVerticalSameBlocks(pos, block) < 3;
        }

        return true;
    }

    /**
     * 计算垂直方向相同方块的数量
     */
    private int countVerticalSameBlocks(BlockPos pos, Block block) {
        int count = 1;
        BlockPos checkPos = pos.below();
        while (getLevel().getBlockState(checkPos).getBlock() == block) {
            count++;
            checkPos = checkPos.below();
        }
        return count;
    }

    /**
     * 导致植物衰变
     */
    private void causePlantDecay(BlockPos pos) {
        BlockState state = getLevel().getBlockState(pos);
        Block block = state.getBlock();
        Random random = getLevel().getRandom();

        float decayType = random.nextFloat();

        if (decayType < 0.4f) {
            // 反向生长（降低生长阶段）
            if (block instanceof CropBlock) {
                CropBlock crop = (CropBlock) block;
                IntegerProperty ageProperty = crop.getAgeProperty();
                int currentAge = state.getValue(ageProperty);
                if (currentAge > 0) {
                    getLevel().setBlock(pos, state.setValue(ageProperty, Math.max(0, currentAge - 1)), 3);
                }
            }
        } else if (decayType < 0.7f) {
            // 变成枯死灌木
            getLevel().setBlock(pos, Blocks.DEAD_BUSH.defaultBlockState(), 3);
        } else {
            // 直接消失
            getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }

        // 粒子效果
        if (getLevel() instanceof ServerLevel) {
            ((ServerLevel) getLevel()).sendParticles(
                    net.minecraft.core.particles.ParticleTypes.SMOKE,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    5, 0.2, 0.2, 0.2, 0.02
            );
        }
    }

    /**
     * 计算附近蕴魔土数量
     */
    private int countNearbyManaDirt() {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
                getBlockPos().offset(-RANGE, -2, -RANGE),
                getBlockPos().offset(RANGE, 2, RANGE))) {

            if (getLevel().getBlockState(pos).is(ModBlocks.enchantedSoil)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        return 0x7FCC19;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getBlockPos(), RANGE);
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        manaGenerationCooldown = cmp.getInt(TAG_MANA_GENERATION_COOLDOWN);
        decayChance = cmp.getFloat(TAG_DECAY_CHANCE);
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt(TAG_MANA_GENERATION_COOLDOWN, manaGenerationCooldown);
        cmp.putFloat(TAG_DECAY_CHANCE, decayChance);
    }
}