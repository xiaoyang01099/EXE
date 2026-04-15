package org.xiaoyang.ex_enigmaticlegacy.api.exboapi;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public interface IWireframeAABBProvider {
    AABB getWireframeAABB(Level level, BlockPos pos);
}