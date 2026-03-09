package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
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
import net.xiaoyang010.ex_enigmaticlegacy.Client.ModParticleTypes;

import java.util.Random;

public class FluffyDandelionBlock extends FlowerBlock {

    public FluffyDandelionBlock() {
        super(MobEffects.MOVEMENT_SPEED, 100, Properties.of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)
                .explosionResistance(1200.0F)); // 防爆属性，阻力值设为1200.0F
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
        ItemBlockRenderTypes.setRenderLayer(ModBlockss.FLUFFY_DANDELION.get(), renderType -> renderType == RenderType.cutout());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState soil = worldIn.getBlockState(pos.below());
        return soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK) || soil.is(ModBlockss.BLOCKNATURE.get());
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, Random random) {
        super.animateTick(state, world, pos, random);

        // 检查天气，如果是晴天则生成粒子
        if (!world.isRaining() && !world.isThundering() && !world.isRainingAt(pos)) {
            if (random.nextInt(5) == 0) { // 控制粒子生成的频率
                double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D;
                double y = pos.getY() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D;
                double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D;

                // 粒子运动方向和速度模拟风的影响
                double motionX = (random.nextDouble() - 0.5D) * 0.01D;
                double motionY = random.nextDouble() * 0.02D;
                double motionZ = (random.nextDouble() - 0.5D) * 0.01D;

                // 添加粒子
                world.addParticle(ModParticleTypes.DANDELION_FLUFF.get(), x, y, z, motionX, motionY, motionZ);
            }
        }
    }
}
