package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerBlock.Generating;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating.EMCFlowerTile;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModIntegrationFlowers;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.subtile.TileEntitySpecialFlower;
import vazkii.botania.common.block.BlockSpecialFlower;

import java.util.Random;
import java.util.function.Supplier;

public class EMCFlower extends BlockSpecialFlower {


    public EMCFlower(MobEffect stewEffect, int stewDuration, Properties props, Supplier<BlockEntityType<? extends TileEntitySpecialFlower>> blockEntityType) {
        super(stewEffect, stewDuration, props, blockEntityType);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof EMCFlowerTile emcFlower) {
            emcFlower.onBlockPlacedBy(level, pos, state, placer, stack);
        }
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, Random rand) {
        emcParticles(state, world, pos, rand);
    }

    private static void emcParticles(BlockState state, Level world, BlockPos pos, Random rand) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof EMCFlowerTile flower) {
            double particleChance = 1.0 - (double) flower.getMana() / (double) flower.getMaxMana() / 3.5;

            if (rand.nextDouble() > particleChance) {
                double x = pos.getX() + 0.3 + rand.nextDouble() * 0.4;
                double y = pos.getY() + 0.5 + rand.nextDouble() * 0.4;
                double z = pos.getZ() + 0.3 + rand.nextDouble() * 0.4;

                world.addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0, 0.05, 0);

                if (rand.nextInt(4) == 0) {
                    world.addParticle(ParticleTypes.ENCHANT, x, y, z,
                            (rand.nextDouble() - 0.5) * 0.1,
                            0.1,
                            (rand.nextDouble() - 0.5) * 0.1);
                }
            }
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState soil = worldIn.getBlockState(pos.below());
        return soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerRenderLayer() {
        ItemBlockRenderTypes.setRenderLayer(ModIntegrationFlowers.EMC_FLOWER.get(),
                renderType -> renderType == RenderType.cutout());
    }
}