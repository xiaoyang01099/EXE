package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Client.ModParticleTypes;
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

import java.util.Random;

public class AsgardFlowerTile extends TileEntityGeneratingFlower {
    private static final int RANGE = 8;
    private static final Logger log = LoggerFactory.getLogger(AsgardFlowerTile.class);

    public AsgardFlowerTile(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.ASGARDANDELIONTILE.get(), blockPos, blockState);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        if (level != null && level.isClientSide() && level.getGameTime() % 5 == 0 && level.random.nextInt(3) == 0) {
            aaa(level, getBlockPos(), level.random);
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



        // 每5个tick增加魔力
        long gameTime = level.getGameTime();
        if (gameTime % 5 == 0) {
            addMana(Integer.MAX_VALUE); // 添加魔力
        }


        // 阻止周围产能花枯萎
        for (int dx = -RANGE; dx <= RANGE; dx++) {
            for (int dz = -RANGE; dz <= RANGE; dz++) {
                BlockPos pos = getEffectivePos().offset(dx, 0, dz);
                BlockEntity tile = level.getBlockEntity(pos);
                if (tile instanceof TileEntityGeneratingFlower) {
                    TileEntityGeneratingFlower flower = (TileEntityGeneratingFlower) tile;
                    if (flower.isRemoved()) {
                        flower.ticksExisted = 0; // 重置枯萎计时
                    }
                }
            }
        }
    }

    @Override
    public boolean triggerEvent(int mun, int data) {
        if (mun == 0){
            Entity e = getLevel().getEntity(0);
            Level level1 = null;
            if (e != null) {
                level1 = e.level;
                Random random = level1.getRandom();
                BlockPos pos = this.getBlockPos();
                aaa(level1, pos, random);
            }

        }

        return super.triggerEvent(mun, data);
    }

    private static void aaa(Level level, BlockPos pos, Random random) {

        if (!level.isRaining() && !level.isThundering() && !level.isRainingAt(pos)) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D;
            double y = pos.getY() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D;

            double motionX = (random.nextDouble() - 0.5D) * 0.01D;
            double motionY = random.nextDouble() * 0.02D;
            double motionZ = (random.nextDouble() - 0.5D) * 0.01D;

            // 添加粒子
            //  for (int i = 0; i < 10; i++) //粒子数量控制
                level.addParticle(ModParticleTypes.ASGARDANDELION.get(), x, y, z, motionX, motionY, motionZ);
        }
    }

    @Override
    public int getColor() {
        return 0x5EF2FF; // 颜色设置
    }

    // 最大魔力值
    @Override
    public int getMaxMana() {
        return Integer.MAX_VALUE;
    }

    // 作用范围
    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Rectangle(getEffectivePos(), new AABB(-8, -4, -8, 8, 4, 8));
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
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new GeneratingWandHud(this)).cast());
    }
}