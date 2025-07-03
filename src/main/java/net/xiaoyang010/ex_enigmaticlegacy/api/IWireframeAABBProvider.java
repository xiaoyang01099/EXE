package net.xiaoyang010.ex_enigmaticlegacy.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public interface IWireframeAABBProvider {
    AABB getWireframeAABB(Level level, BlockPos pos);
}