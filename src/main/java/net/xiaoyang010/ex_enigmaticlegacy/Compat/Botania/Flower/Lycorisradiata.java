package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower;


import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlocks;

import java.util.Random;


public class Lycorisradiata extends FlowerBlock {
    private static final int WATER_CHECK_RADIUS = 2;
    // 死亡时产生的粒子数量
    private static final int PARTICLE_COUNT = 40;

    public Lycorisradiata() {

        super(MobEffects.MOVEMENT_SPEED,100, BlockBehaviour.Properties.of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)
                .lightLevel(state -> 15) // 添加发光性，光照等级为15
                .explosionResistance(1200.0F)); // 防爆属性，阻力值设为1200.0F
    }

    private boolean isNearWater(Level level, BlockPos pos) {
        // 在指定半径范围内检查每个方块
        for (int x = -WATER_CHECK_RADIUS; x <= WATER_CHECK_RADIUS; x++) {
            for (int y = -WATER_CHECK_RADIUS; y <= WATER_CHECK_RADIUS; y++) {
                for (int z = -WATER_CHECK_RADIUS; z <= WATER_CHECK_RADIUS; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    if (level.getBlockState(checkPos).getBlock() == Blocks.WATER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void spawnDeathParticles(ServerLevel level, BlockPos pos) {
        Random random = level.getRandom();

        // 在圆形范围内生成粒子
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            // 计算粒子的圆形分布位置
            double angle = 2.0 * Math.PI * i / PARTICLE_COUNT;
            double radius = 0.5 * random.nextDouble();
            double x = pos.getX() + 0.5 + Math.cos(angle) * radius;
            double y = pos.getY() + 0.2 + random.nextDouble() * 0.8;
            double z = pos.getZ() + 0.5 + Math.sin(angle) * radius;

            // 生成红色的粒子云
            DustParticleOptions redParticle = new DustParticleOptions(
                    new Vector3f(0.8F, 0.0F, 0.0F), // RGB颜色值
                    1.0F  // 粒子大小
            );
            level.sendParticles(redParticle,
                    x, y, z, 1, 0, 0, 0, 0);

            // 30%的概率生成星星效果
            if (random.nextFloat() > 0.85) {
                level.sendParticles(ParticleTypes.END_ROD,
                        x, y, z, 1,
                        random.nextGaussian() * 0.02, // X方向随机速度
                        random.nextGaussian() * 0.02, // Y方向随机速度
                        random.nextGaussian() * 0.02, // Z方向随机速度
                        0.01); // 粒子大小
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (isNearWater(level, pos)) {
            // 如果检测到水源，生成粒子效果并移除方块
            spawnDeathParticles(level, pos);
            level.removeBlock(pos, false);
        } else {
            // 如果没有检测到水源，继续安排下一次检测
            level.scheduleTick(pos, this, 20);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        // 20 ticks后执行第一次检测（约1秒）
        level.scheduleTick(pos, this, 20);
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, net.minecraft.core.Direction face) {
        return false; // 防止植物被点燃
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, net.minecraft.core.Direction face) {
        return 0; // 火焰蔓延速度为0，防止火焰蔓延到植物
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerRenderLayer() {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.LYCORISRADIATA.get(), renderType -> renderType == RenderType.cutout());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState soil = worldIn.getBlockState(pos.below());
        return soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK) || soil.is(ModBlocks.BLOCKNATURE.get());
    }
}
