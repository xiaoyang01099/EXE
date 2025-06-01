package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RainbowGeneratingFlowerTile extends TileEntityGeneratingFlower {
    private static final int RANGE = 50;
    private static final int BASE_MANA = 1000;
    private static final int MIN_REQUIRED_FLOWERS = 15; // 至少需要的不同种类产能花数量
    private static final int ACTIVE_MANA_MULTIPLIER = 5;

    private int lastTickFlowerCount = 0;
    private int activeFlowerCount = 0;
    private int tickCounter = 0;

    private final Map<BlockPos, Integer> previousManaLevels = new HashMap<>();

    private final Set<String> excludedFlowerClasses = new HashSet<>();

    {
        excludedFlowerClasses.add(AsgardFlowerTile.class.getName());
    }

    public RainbowGeneratingFlowerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if(!level.isClientSide) {
            tickCounter++;
            if(tickCounter >= 10) {
                tickCounter = 0;
                updateFlowerCounts();
            }

            if(lastTickFlowerCount >= MIN_REQUIRED_FLOWERS) {
                int manaToAdd;
                if(activeFlowerCount > 0) {
                    manaToAdd = calculateMana(lastTickFlowerCount) * ACTIVE_MANA_MULTIPLIER;
                } else {
                    manaToAdd = calculateMana(lastTickFlowerCount);
                }
                addMana(manaToAdd);

                if(tickCounter % 2 == 0) {
                    spawnRainbowParticles();
                }
            }
        }
    }

    private void updateFlowerCounts() {
        Set<Class<? extends TileEntityGeneratingFlower>> flowerClasses = new HashSet<>();
        Set<BlockPos> currentFlowers = new HashSet<>();
        Map<Class<?>, Boolean> flowerWorkingState = new HashMap<>();
        activeFlowerCount = 0;

        for(BlockPos checkPos : BlockPos.betweenClosed(
                getBlockPos().offset(-RANGE, -2, -RANGE),
                getBlockPos().offset(RANGE, 2, RANGE))) {

            if(checkPos.equals(getBlockPos())) continue;

            BlockEntity tile = level.getBlockEntity(checkPos);
            if(tile instanceof TileEntityGeneratingFlower) {
                TileEntityGeneratingFlower flower = (TileEntityGeneratingFlower) tile;
                String className = flower.getClass().getName();

                if(excludedFlowerClasses.contains(className)) {
                    continue;
                }

                boolean isWorking = isFlowerGeneratingMana(flower, checkPos);
                currentFlowers.add(checkPos);

                Class<? extends TileEntityGeneratingFlower> flowerClass = flower.getClass();
                flowerClasses.add(flowerClass);

                if(isWorking) {
                    flowerWorkingState.put(flowerClass, true);
                }
            }
        }

        lastTickFlowerCount = flowerClasses.size();

        for(Class<?> flowerClass : flowerClasses) {
            if(flowerWorkingState.getOrDefault(flowerClass, false)) {
                activeFlowerCount++;
            }
        }

        previousManaLevels.keySet().retainAll(currentFlowers);
    }

    private boolean isFlowerGeneratingMana(TileEntityGeneratingFlower flower, BlockPos pos) {
        int currentMana = flower.getMana();
        int previousMana = previousManaLevels.getOrDefault(pos, -1);

        previousManaLevels.put(pos, currentMana);

        if(previousMana == -1) {
            return false;
        }

        return currentMana > previousMana;
    }

    private void spawnRainbowParticles() {
        Vec3 offset = level.getBlockState(getBlockPos()).getOffset(level, getBlockPos());
        double x = getBlockPos().getX() + offset.x;
        double y = getBlockPos().getY() + offset.y;
        double z = getBlockPos().getZ() + offset.z;

        float hue = (System.currentTimeMillis() % 6000) / 6000f;
        float[] rgb = hsvToRgb(hue, 1.0f, 1.0f);

        float brightness = activeFlowerCount > 0 ? 1.0F : 0.5F;

        BotaniaAPI.instance().sparkleFX(level,
                x + 0.3 + Math.random() * 0.5,
                y + 0.5 + Math.random() * 0.5,
                z + 0.3 + Math.random() * 0.5,
                rgb[0], rgb[1], rgb[2],
                brightness, 5);
    }

    @Override
    public @Nullable RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }

    private int calculateMana(int flowerCount) {
        // 基础魔力值 + 额外魔力奖励基于不同花种类的数量
        return BASE_MANA + (flowerCount * 200);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new GeneratingWandHud(this)).cast());
    }

    @Override
    public int getMaxMana() {
        return 100000;
    }

    @Override
    public int getColor() {
        float saturation = activeFlowerCount > 0 ? 1.0f : 0.6f;
        float brightness = activeFlowerCount > 0 ? 1.0f : 0.8f;

        float hue = (System.currentTimeMillis() % 6000) / 6000f;
        float[] rgb = hsvToRgb(hue, saturation, brightness);

        int r = Math.round(rgb[0] * 255);
        int g = Math.round(rgb[1] * 255);
        int b = Math.round(rgb[2] * 255);

        return (r << 16) | (g << 8) | b;
    }

    private float[] hsvToRgb(float h, float s, float v) {
        float r = 0, g = 0, b = 0;

        int i = (int)(h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);

        switch(i % 6) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            case 5: r = v; g = p; b = q; break;
        }

        return new float[]{r, g, b};
    }
}