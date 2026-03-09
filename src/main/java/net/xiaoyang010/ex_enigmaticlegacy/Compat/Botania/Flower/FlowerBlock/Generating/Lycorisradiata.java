package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerBlock.Generating;


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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;

import java.util.Random;


public class Lycorisradiata extends FlowerBlock {
    private static final int WATER_CHECK_RADIUS = 2;
    private static final int PARTICLE_COUNT = 40;

    public Lycorisradiata() {

        super(MobEffects.MOVEMENT_SPEED,100, Properties.of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)
                .lightLevel(state -> 15)
                .explosionResistance(1200.0F));
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

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double angle = 2.0 * Math.PI * i / PARTICLE_COUNT;
            double radius = 0.5 * random.nextDouble();
            double x = pos.getX() + 0.5 + Math.cos(angle) * radius;
            double y = pos.getY() + 0.2 + random.nextDouble() * 0.8;
            double z = pos.getZ() + 0.5 + Math.sin(angle) * radius;

            DustParticleOptions redParticle = new DustParticleOptions(
                    new Vector3f(0.8F, 0.0F, 0.0F),
                    1.0F
            );
            level.sendParticles(redParticle,
                    x, y, z, 1, 0, 0, 0, 0);

            if (random.nextFloat() > 0.85) {
                level.sendParticles(ParticleTypes.END_ROD,
                        x, y, z, 1,
                        random.nextGaussian() * 0.02,
                        random.nextGaussian() * 0.02,
                        random.nextGaussian() * 0.02,
                        0.01);
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (isNearWater(level, pos)) {
            spawnDeathParticles(level, pos);
            level.removeBlock(pos, false);
        } else {
            level.scheduleTick(pos, this, 20);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        level.scheduleTick(pos, this, 20);
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, net.minecraft.core.Direction face) {
        return false;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, net.minecraft.core.Direction face) {
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerRenderLayer() {
        ItemBlockRenderTypes.setRenderLayer(ModBlockss.LYCORISRADIATA.get(), renderType -> renderType == RenderType.cutout());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState soil = worldIn.getBlockState(pos.below());
        return soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK) || soil.is(ModBlockss.BLOCKNATURE.get());
    }
}
