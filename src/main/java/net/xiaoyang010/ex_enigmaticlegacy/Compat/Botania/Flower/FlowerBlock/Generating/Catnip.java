package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerBlock.Generating;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating.CatnipTile;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import vazkii.botania.api.subtile.TileEntitySpecialFlower;
import vazkii.botania.common.block.BlockSpecialFlower;

import java.util.function.Supplier;

public class Catnip extends BlockSpecialFlower {

    public Catnip(MobEffect stewEffect, int stewDuration, Properties props, Supplier<BlockEntityType<? extends TileEntitySpecialFlower>> blockEntityType) {
        super(stewEffect, stewDuration, props, blockEntityType);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CatnipTile catnipTile) {
            catnipTile.onClicked(player, hand);
            return InteractionResult.SUCCESS;
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerRenderLayer() {
        ItemBlockRenderTypes.setRenderLayer(ModBlockss.CATNIP.get(), renderType -> renderType == RenderType.cutout());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState soil = worldIn.getBlockState(pos.below());
        return soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK) || soil.is(ModBlockss.BLOCKNATURE.get());
    }
}

