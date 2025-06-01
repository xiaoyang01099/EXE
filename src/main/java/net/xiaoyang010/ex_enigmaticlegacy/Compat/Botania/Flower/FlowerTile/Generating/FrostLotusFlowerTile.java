package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional.FrostBlossomTile;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;

public class FrostLotusFlowerTile extends TileEntityGeneratingFlower {
    private static final String TAG_CONVERTING = "converting";
    private static final String TAG_TICKS = "ticksRemaining";
    private static final String TAG_POS = "convertingPos";

    private static final int RANGE = 5;
    private static final int MAX_MANA = 15000;
    private static final int BLOCKS_PER_TICK = 3;
    private static final int CONVERSION_TIME = 20;

    private final boolean[] isConverting = new boolean[BLOCKS_PER_TICK];
    private final int[] ticksRemaining = new int[BLOCKS_PER_TICK];
    private final BlockPos[] convertingPositions = new BlockPos[BLOCKS_PER_TICK];

    public FrostLotusFlowerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (getLevel().isClientSide) {
            for (int i = 0; i < BLOCKS_PER_TICK; i++) {
                if (isConverting[i] && convertingPositions[i] != null) {
                    BlockPos coords = convertingPositions[i];
                    SparkleParticleData data = SparkleParticleData.sparkle((float) Math.random(), 1F, 1F, 1F, 5);
                    level.addParticle(data,
                            coords.getX() + Math.random(),
                            coords.getY() + Math.random(),
                            coords.getZ() + Math.random(),
                            0, 0, 0);
                }
            }
            return;
        }

        if (getMana() < getMaxMana()) {
            Biome biome = getLevel().getBiome(getEffectivePos()).value();

            boolean isSnowArea = biome.getBaseTemperature() <= 0.15f ||
                    biome.getPrecipitation().equals(Biome.Precipitation.SNOW) ||
                    (!FrostBlossomTile.SNOW_FLOWER_POSITIONS.isEmpty() &&
                            biome.getPrecipitation().equals(Biome.Precipitation.RAIN));

            if (!isSnowArea) {
                return;
            }

//            if (biome.getBaseTemperature() > 0.15f || !biome.getPrecipitation().equals(Biome.Precipitation.SNOW)) {
//                return;
//            }

            boolean didAny = false;
            for (int i = 0; i < BLOCKS_PER_TICK; i++) {
                if (!isConverting[i]) {
                    BlockPos checkPos = getEffectivePos().offset(
                            getLevel().getRandom().nextInt(RANGE * 2 + 1) - RANGE,
                            getLevel().getRandom().nextInt(5) - 2,
                            getLevel().getRandom().nextInt(RANGE * 2 + 1) - RANGE
                    );

                    if (canConvert(checkPos)) {
                        isConverting[i] = true;
                        ticksRemaining[i] = CONVERSION_TIME;
                        convertingPositions[i] = checkPos;
                        didAny = true;
                    }
                } else if (ticksRemaining[i] > 0) {
                    ticksRemaining[i]--;
                    if (ticksRemaining[i] == 0) {
                        if (processConversion(convertingPositions[i])) {
                            addFinishParticles(convertingPositions[i]);
                            didAny = true;
                        }
                        isConverting[i] = false;
                        convertingPositions[i] = null;
                    }
                }
            }

            if (didAny) {
                sync();
            }
        }
    }

    private void addFinishParticles(BlockPos pos) {
        for (int i = 0; i < 25; i++) {
            WispParticleData data = WispParticleData.wisp((float) Math.random() / 2F, 1F, 1F, 1F);
            getLevel().addParticle(data,
                    pos.getX() + Math.random(),
                    pos.getY() + Math.random() + 0.5D,
                    pos.getZ() + Math.random(),
                    0, 0, 0);
        }
    }

    private boolean canConvert(BlockPos pos) {
        return getConversionResult(getLevel().getBlockState(pos)) != null;
    }

    private boolean processConversion(BlockPos pos) {
        BlockState state = getLevel().getBlockState(pos);
        ConversionResult result = getConversionResult(state);

        if (result != null && getMana() + result.manaGain <= getMaxMana()) {
            if (getLevel().setBlockAndUpdate(pos, result.resultState)) {
                addMana(result.manaGain);
                return true;
            }
        }
        return false;
    }

    private static class ConversionResult {
        final BlockState resultState;
        final int manaGain;

        ConversionResult(BlockState resultState, int manaGain) {
            this.resultState = resultState;
            this.manaGain = manaGain;
        }
    }

    private ConversionResult getConversionResult(BlockState state) {
        if (state.is(Blocks.BLUE_ICE)) {
            return new ConversionResult(Blocks.PACKED_ICE.defaultBlockState(), 610);
        } else if (state.is(Blocks.PACKED_ICE)) {
            return new ConversionResult(Blocks.ICE.defaultBlockState(), 550);
        } else if (state.is(Blocks.ICE)) {
            return new ConversionResult(Blocks.SNOW_BLOCK.defaultBlockState(), 300);
        } else if (state.is(Blocks.SNOW_BLOCK)) {
            return new ConversionResult(Blocks.SNOW.defaultBlockState(), 265);
        } else if (state.is(Blocks.SNOW)) {
            if (state.getFluidState().getType() == Fluids.WATER) {
                return new ConversionResult(Blocks.WATER.defaultBlockState(), 120);
            }
            return new ConversionResult(Blocks.AIR.defaultBlockState(), 120);
        }
        return null;
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        for (int i = 0; i < BLOCKS_PER_TICK; i++) {
            isConverting[i] = cmp.getBoolean(TAG_CONVERTING + i);
            ticksRemaining[i] = cmp.getInt(TAG_TICKS + i);
            if (cmp.contains(TAG_POS + "X" + i)) {
                int x = cmp.getInt(TAG_POS + "X" + i);
                int y = cmp.getInt(TAG_POS + "Y" + i);
                int z = cmp.getInt(TAG_POS + "Z" + i);
                convertingPositions[i] = new BlockPos(x, y, z);
            } else {
                convertingPositions[i] = null;
            }
        }
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        for (int i = 0; i < BLOCKS_PER_TICK; i++) {
            cmp.putBoolean(TAG_CONVERTING + i, isConverting[i]);
            cmp.putInt(TAG_TICKS + i, ticksRemaining[i]);
            if (convertingPositions[i] != null) {
                cmp.putInt(TAG_POS + "X" + i, convertingPositions[i].getX());
                cmp.putInt(TAG_POS + "Y" + i, convertingPositions[i].getY());
                cmp.putInt(TAG_POS + "Z" + i, convertingPositions[i].getZ());
            }
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(
                cap,
                LazyOptional.of(() -> new GeneratingWandHud<>(this)).cast()
        );
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        return 0x89CFF0;  // 冰蓝色
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }
}