package org.xiaoyang.ex_enigmaticlegacy.api.exboapi;

import net.minecraft.core.BlockPos;
import vazkii.botania.api.block.WandBindable;

public interface IBoundRender extends WandBindable {
    BlockPos[] getBlocksCoord();
}