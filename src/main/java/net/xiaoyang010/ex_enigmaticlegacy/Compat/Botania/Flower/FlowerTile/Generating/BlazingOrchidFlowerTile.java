package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;

import javax.annotation.Nullable;

public class BlazingOrchidFlowerTile extends TileEntityGeneratingFlower {
    private static final int RANGE = 10;
    private static final int MAX_MANA = 14500;

    public BlazingOrchidFlowerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if(!level.isClientSide && getMana() < getMaxMana()) {
            BlockPos checkPos = getEffectivePos().offset(
                    level.random.nextInt(RANGE * 2 + 1) - RANGE,
                    0,
                    level.random.nextInt(RANGE * 2 + 1) - RANGE
            );

            BlockState checkState = level.getBlockState(checkPos);
            BlockState resultState = null;
            int manaToAdd = 0;

            // 转换规则
            if(checkState.is(Blocks.LAVA)) {
                resultState = Blocks.MAGMA_BLOCK.defaultBlockState();
                manaToAdd = 350;
            } else if(checkState.is(Blocks.NETHERRACK)) {
                resultState = Blocks.COBBLESTONE.defaultBlockState();
                manaToAdd = 85;
            } else if(checkState.is(Blocks.NETHER_BRICKS)) {
                resultState = Blocks.BRICKS.defaultBlockState();
                manaToAdd = 270;
            } else if(checkState.is(Blocks.BASALT)) {
                resultState = Blocks.TUFF.defaultBlockState();
                manaToAdd = 340;
            } else if(isNetherStem(checkState)) {
                resultState = Blocks.OAK_LOG.defaultBlockState();
                manaToAdd = 300;
            } else if(isNetherWart(checkState)) {
                resultState = Blocks.OAK_WOOD.defaultBlockState();
                manaToAdd = 1200;
            } else if(checkState.is(Blocks.SOUL_SOIL)) {
                resultState = Blocks.DIRT.defaultBlockState();
                manaToAdd = 245;
            } else if(checkState.is(Blocks.SOUL_SAND)) {
                resultState = Blocks.SAND.defaultBlockState();
                manaToAdd = 245;
            } else if(checkState.is(Blocks.BLACKSTONE)) {
                resultState = Blocks.DEEPSLATE.defaultBlockState();
                manaToAdd = 300;
            } else if(isNetherFungus(checkState)) {
                resultState = Blocks.BROWN_MUSHROOM.defaultBlockState();
                manaToAdd = 130;
            } else if(checkState.is(Blocks.TWISTING_VINES) || checkState.is(Blocks.WEEPING_VINES)) {
                resultState = Blocks.VINE.defaultBlockState();
                manaToAdd = 140;
            }

            if(manaToAdd > 0 && resultState != null) {
                level.setBlockAndUpdate(checkPos, resultState);
                addMana(manaToAdd);

                Vec3 offset = level.getBlockState(getBlockPos()).getOffset(level, getBlockPos());
                double x = getBlockPos().getX() + offset.x;
                double y = getBlockPos().getY() + offset.y;
                double z = getBlockPos().getZ() + offset.z;

                BotaniaAPI.instance().sparkleFX(level,
                        x + 0.3 + Math.random() * 0.5,
                        y + 0.5 + Math.random() * 0.5,
                        z + 0.3 + Math.random() * 0.5,
                        1F, 0.3F, 0F, // 橙红色粒子
                        (float) Math.random(), 5);
            }
        }
    }

    private boolean isNetherStem(BlockState state) {
        return state.is(Blocks.CRIMSON_STEM) || state.is(Blocks.WARPED_STEM);
    }

    private boolean isNetherWart(BlockState state) {
        return state.is(Blocks.NETHER_WART_BLOCK) || state.is(Blocks.WARPED_WART_BLOCK);
    }

    private boolean isNetherFungus(BlockState state) {
        return state.is(Blocks.CRIMSON_FUNGUS) || state.is(Blocks.WARPED_FUNGUS);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(() -> new GeneratingWandHud(this)).cast());
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        return 0xFF4500; // 橙红色
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }
}