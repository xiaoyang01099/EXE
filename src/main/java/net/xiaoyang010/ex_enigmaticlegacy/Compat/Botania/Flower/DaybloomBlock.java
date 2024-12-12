package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower;


import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlocks;

public class DaybloomBlock extends FlowerBlock {

    public DaybloomBlock() {
        super(MobEffects.MOVEMENT_SPEED, 100, BlockBehaviour.Properties.of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS));

    }


    @Override
    public int getEffectDuration() {
        return 100;
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerRenderLayer() {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.DAYBLOOM.get(), renderType -> renderType == RenderType.cutout());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState soil = worldIn.getBlockState(pos.below());
        return soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK) || soil.is(ModBlocks.BLOCKNATURE.get());
    }
}
