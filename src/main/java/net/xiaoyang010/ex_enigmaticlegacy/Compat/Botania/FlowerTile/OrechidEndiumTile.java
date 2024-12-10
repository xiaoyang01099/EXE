package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.FlowerTile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;


public class OrechidEndiumTile extends TileEntityFunctionalFlower {


    public OrechidEndiumTile(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.ORECHIDENDIUMTILE.get(), blockPos, blockState);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        if (level != null && level.isClientSide() && level.getGameTime() % 5 == 0 && level.random.nextInt(3) == 0) {
        }

        if (level == null || level.isClientSide) {
            return;
        }

        double particleChance = 1F - (double) getMana() / (double) getMaxMana() / 3.5F;
        int color = getColor();
        float red = (color >> 16 & 0xFF) / 255F;
        float green = (color >> 8 & 0xFF) / 255F;
        float blue = (color & 0xFF) / 255F;

        if (Math.random() > particleChance) {
            Vec3 offset = level.getBlockState(getBlockPos()).getOffset(level, getBlockPos());
            double x = getBlockPos().getX() + offset.x;
            double y = getBlockPos().getY() + offset.y;
            double z = getBlockPos().getZ() + offset.z;
            BotaniaAPI.instance().sparkleFX(level, x + 0.3 + Math.random() * 0.5, y + 0.5 + Math.random() * 0.5, z + 0.3 + Math.random() * 0.5, red, green, blue, (float) Math.random(), 5);
        }
    }

    @Override
    public int getMaxMana() {
        return 0;
    }

    @Override
    public int getColor() {
        return 0x800080;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Rectangle(getEffectivePos(), new AABB(-8, -4, -8, 8, 4, 8));
    }


}
