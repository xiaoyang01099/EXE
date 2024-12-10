package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.FlowerTile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;

import javax.annotation.Nullable;

public class NightshadeTile extends TileEntityGeneratingFlower {

	public NightshadeTile(BlockPos pos, BlockState state) {
		super(ModBlockEntities.NIGHTSHADE_TILE.get(), pos, state);
	}

	@Override
	public int getMaxMana() {
		return 100;
	}

	@Override
	public int getColor() {
		return 0x3D2A90;  // 设置花的颜色为深蓝色
	}

	@Nullable
	@Override
	public RadiusDescriptor getRadius() {
		return new RadiusDescriptor.Circle(getBlockPos(), 5);  // 设置半径为5格
	}

	// Prime版本的子类
	/*public static class Prime extends NightshadeTile {

		public Prime(BlockPos pos, BlockState state) {
			super(pos, state);
		}

		@Override
		public boolean isPrime() {
			return true;  // Prime花返回true
		}
	}*/
}
