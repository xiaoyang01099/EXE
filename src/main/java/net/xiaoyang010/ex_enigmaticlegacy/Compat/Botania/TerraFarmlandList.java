package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TerraFarmlandList {
    private final Block block;
    private final BlockState blockState;

    public TerraFarmlandList(Block block, BlockState blockState) {
        this.block = block;
        this.blockState = blockState;
    }

    public TerraFarmlandList(Block block) {
        this.block = block;
        this.blockState = block.defaultBlockState();
    }

    public Block getBlock() {
        return this.block;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Deprecated
    public int getMeta() {
        return Block.getId(this.blockState);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        TerraFarmlandList that = (TerraFarmlandList) obj;
        return block.equals(that.block) && blockState.equals(that.blockState);
    }

    @Override
    public int hashCode() {
        return 31 * block.hashCode() + blockState.hashCode();
    }

    @Override
    public String toString() {
        return "TerraFarmlandList{" +
                "block=" + block +
                ", blockState=" + blockState +
                '}';
    }
}