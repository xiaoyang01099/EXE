package org.xiaoyang.ex_enigmaticlegacy.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class DragonCrystalBlock extends Block {
    public DragonCrystalBlock() {
        super(Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(3f, 10f)
                .requiresCorrectToolForDrops());
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
