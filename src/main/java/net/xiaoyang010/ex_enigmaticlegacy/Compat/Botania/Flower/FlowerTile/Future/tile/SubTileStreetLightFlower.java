package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Future.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;

public class SubTileStreetLightFlower extends TileEntityGeneratingFlower {
    private static final int RANGE = 4;
    private static final int MAX_MANA = 1000;
    private static final int BASE_GENERATION = 1;

    public SubTileStreetLightFlower(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (!level.isClientSide && getMana() < getMaxMana()) {
            int totalLight = 0;
            int checkedBlocks = 0;

            for (BlockPos pos : BlockPos.betweenClosed(
                    getEffectivePos().offset(-RANGE, -2, -RANGE),
                    getEffectivePos().offset(RANGE, 2, RANGE))) {
                int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
                if (blockLight > 0) {
                    totalLight += blockLight;
                    checkedBlocks++;
                }
            }

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

    @Override
    public int getColor() {
        return 0xFFFF00;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }
}