package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerBlock.Functional;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;

public class DarkNightGrass extends FlowerBlock {
    public DarkNightGrass() {
        super(MobEffects.MOVEMENT_SPEED, 100, Properties.of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)
                .strength(-1.0F, 3600000.0F));
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return player != null && !player.isSpectator() && super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerRenderLayer() {
        ItemBlockRenderTypes.setRenderLayer(ModBlockss.DARK_NIGHT_GRASS.get(), renderType -> renderType == RenderType.cutout());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState soil = worldIn.getBlockState(pos.below());
        return soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK) || soil.is(ModBlockss.BLOCKNATURE.get());
    }
}

