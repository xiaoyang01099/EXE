package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.internal.IManaNetwork;
import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;

public class FloweyTile extends TileEntityGeneratingFlower {
    private static final int RANGE = 8;
    private static final Logger log = LoggerFactory.getLogger(FloweyTile.class);

    public FloweyTile(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.FLOWEYTILE.get(), blockPos, blockState);
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

        emptyManaIntoCollector();

        long gameTime = level.getGameTime();
        if (gameTime % 5 == 0) {
            addMana(10000);
        }

        for (int dx = -RANGE; dx <= RANGE; dx++) {
            for (int dz = -RANGE; dz <= RANGE; dz++) {
                BlockPos pos = getEffectivePos().offset(dx, 0, dz);
                BlockEntity tile = level.getBlockEntity(pos);
                if (tile instanceof TileEntityGeneratingFlower) {
                    TileEntityGeneratingFlower flower = (TileEntityGeneratingFlower) tile;
                    if (flower.isRemoved()) {
                        flower.ticksExisted = 0;
                    }
                }
            }
        }
    }

    @Override
    public int getMaxMana() {
        return 10000;
    }

    @Override
    public int getColor() {
        return 0xFFFF00;
    }

    @Override
    @Nullable
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getBlockPos(), 5);
    }

    @javax.annotation.Nullable
    public BlockPos findClosestTarget() {
        IManaNetwork network = BotaniaAPI.instance().getManaNetworkInstance();
        IManaCollector closestCollector = network.getClosestCollector(this.getBlockPos(), this.getLevel(), this.getBindingRadius());
        return closestCollector == null ? null : closestCollector.getManaReceiverPos();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(() -> new GeneratingWandHud(this)).cast());
    }
}
