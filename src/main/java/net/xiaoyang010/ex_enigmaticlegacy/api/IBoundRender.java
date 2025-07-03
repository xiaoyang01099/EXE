package net.xiaoyang010.ex_enigmaticlegacy.api;

import net.minecraft.core.BlockPos;
import vazkii.botania.api.block.IWandBindable;

public interface IBoundRender extends IWandBindable {
    BlockPos[] getBlocksCoord();
}