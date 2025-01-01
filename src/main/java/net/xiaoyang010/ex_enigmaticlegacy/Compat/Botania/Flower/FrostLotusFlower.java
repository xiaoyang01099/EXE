package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlocks;

import java.util.function.Supplier;

public class FrostLotusFlower extends FlowerBlock {
    public FrostLotusFlower(Supplier<MobEffect> effectSupplier, int p_53513_, Properties p_53514_) {
        super(MobEffects.MOVEMENT_SPEED,100, BlockBehaviour.Properties.of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS));
    }


    @OnlyIn(Dist.CLIENT)
    public static void registerRenderLayer() {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FROST_LOTUS.get(),
                renderType -> renderType == RenderType.cutout());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        // 检查下方方块
        BlockState soil = worldIn.getBlockState(pos.below());

        // 检查当前位置是否有水
        boolean hasWater = worldIn.getFluidState(pos).getType() == Fluids.WATER;

        // 允许在水中或特定方块上生存
        return hasWater || soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK)
                || soil.is(ModBlocks.BLOCKNATURE.get());
    }
}
