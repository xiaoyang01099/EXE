package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;

public class StreetLightFlowerTile extends TileEntityGeneratingFlower {
    // 常量定义
    private static final int RANGE = 8;          // 作用范围
    private static final int MAX_MANA = 1000;    // 最大魔力值
    private static final int BASE_GENERATION = 1; // 基础生成量

    public StreetLightFlowerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        // 只在服务端且魔力未满时工作
        if (!level.isClientSide && getMana() < getMaxMana()) {
            // 获取当前游戏时间(0-24000为一个完整的日夜循环)
            long dayTime = level.getDayTime() % 24000;
            // 判断是否为夜晚(13000-23000为夜晚时间)
            boolean isNight = dayTime >= 13000 && dayTime < 23000;

            // 如果不是夜晚，直接返回不工作
            if (!isNight) {
                return;
            }

            // 计算范围内方块的光照
            int totalLight = 0;
            int checkedBlocks = 0;

            // 遍历以自身为中心的范围
            for (BlockPos pos : BlockPos.betweenClosed(
                    getEffectivePos().offset(-RANGE, -2, -RANGE),
                    getEffectivePos().offset(RANGE, 2, RANGE))) {
                // 获取方块的亮度
                int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
                if (blockLight > 0) {
                    totalLight += blockLight;
                    checkedBlocks++;
                }
            }

            // 如果有光照方块，计算平均亮度并生成魔力
            if (checkedBlocks > 0) {
                int averageLight = totalLight / checkedBlocks;
                int manaGen = BASE_GENERATION * averageLight;
                addMana(manaGen);
            }
        }
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new GeneratingWandHud(this)).cast());
    }

    @Override
    public int getColor() {
        return 0xFFFFFF;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }
}