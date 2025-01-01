package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Future.tile;

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
import vazkii.botania.api.subtile.TileEntitySpecialFlower;
import vazkii.botania.common.block.subtile.generating.SubTileEndoflame;
import vazkii.botania.common.block.subtile.generating.SubTileHydroangeas;

public class RainbowGeneratingFlower extends TileEntityGeneratingFlower {
    private static final int RANGE = 8;
    private static final int BASE_MANA = 1000;

    public RainbowGeneratingFlower(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if(!level.isClientSide) {
            if(checkGeneratingFlowers()) {
                // 产生魔力
                addMana(calculateMana());

                // 产生特效
                Vec3 offset = level.getBlockState(getBlockPos()).getOffset(level, getBlockPos());
                double x = getBlockPos().getX() + offset.x;
                double y = getBlockPos().getY() + offset.y;
                double z = getBlockPos().getZ() + offset.z;

                BotaniaAPI.instance().sparkleFX(level,
                        x + 0.3 + Math.random() * 0.5,
                        y + 0.5 + Math.random() * 0.5,
                        z + 0.3 + Math.random() * 0.5,
                        1F, 0F, 0F, // RGB颜色可以随时间变化实现彩虹效果
                        1F, 5);
            }
        }
    }

    @Override
    public @Nullable RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), 5);
    }

    private boolean checkGeneratingFlowers() {
        boolean hasEndoflame = false;
        boolean hasHydroangeas = false;
        boolean hasRafflowsia = false;
        // 添加其他需要检查的花

        for(BlockPos checkPos : BlockPos.betweenClosed(
                getBlockPos().offset(-RANGE, -2, -RANGE),
                getBlockPos().offset(RANGE, 2, RANGE))) {

            BlockEntity tile = level.getBlockEntity(checkPos);
            if(tile instanceof TileEntitySpecialFlower) {
                TileEntitySpecialFlower flower = (TileEntitySpecialFlower) tile;

                // 检查各种花的类型
                if(flower instanceof SubTileEndoflame) {
                    hasEndoflame = true;
                }
                if(flower instanceof SubTileHydroangeas) {
                    hasHydroangeas = true;
                }
                // 添加其他花的检查
            }
        }

        return hasEndoflame && hasHydroangeas && hasRafflowsia;
    }

    private int calculateMana() {
        return BASE_MANA; // 可以根据周围工作的花的数量调整
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
        // 可以根据时间变化返回不同的颜色实现彩虹效果
        return 0xFF0000;
    }
}