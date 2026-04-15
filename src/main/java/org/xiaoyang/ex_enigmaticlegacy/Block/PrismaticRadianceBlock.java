package org.xiaoyang.ex_enigmaticlegacy.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;

public class PrismaticRadianceBlock extends Block {
    public PrismaticRadianceBlock() {
        super(Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(5f, 10f).requiresCorrectToolForDrops());
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter world, BlockPos pos, FluidState fluidstate) {
        return true;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 15;
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() instanceof TieredItem) {
            TieredItem tool = (TieredItem) heldItem.getItem();
            return tool.getTier().getLevel() >= 2;
        }

        return false;
    }
}
