package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.FlowerTile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;

public class AstralKillopTile extends TileEntityGeneratingFlower {
    public AstralKillopTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASTRAL_KILLOP_TILE.get(), pos, state);
    }

    @Override
    public int getMaxMana() {
        return 0;
    }

    @Override
    public int getColor() {
        return 0x800080;
    }


    @Nullable
    @Override
    public RadiusDescriptor getRadius() {
        return null;
    }
}
