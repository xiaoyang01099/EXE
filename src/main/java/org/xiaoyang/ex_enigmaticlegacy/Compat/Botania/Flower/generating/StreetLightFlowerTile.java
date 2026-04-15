package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.generating;

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
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;


public class StreetLightFlowerTile extends GeneratingFlowerBlockEntity {
    private static final int RANGE = 8;
    private static final int MAX_MANA = 1000;
    private static final int BASE_GENERATION = 1;

    public StreetLightFlowerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (!level.isClientSide && getMana() < getMaxMana()) {
            long dayTime = level.getDayTime() % 24000;
            boolean isNight = dayTime >= 13000 && dayTime < 23000;

            if (!isNight) {
                return;
            }

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

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>(this)).cast());
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