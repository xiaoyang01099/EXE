package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.functional;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlocks;
import org.xiaoyang.ex_enigmaticlegacy.api.test.curse.CurseAbilityHandler;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.forge.block.ForgeSpecialFlowerBlock;

import java.util.function.Supplier;

public class CurseThistle extends ForgeSpecialFlowerBlock {

    public CurseThistle(MobEffect stewEffect, int stewDuration, Properties props, Supplier<BlockEntityType<? extends SpecialFlowerBlockEntity>> blockEntityType) {
        super(stewEffect, stewDuration, props, blockEntityType);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return player != null && !player.isSpectator() && super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerRenderLayer() {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.CURSET_THISTLE.get(), renderType -> renderType == RenderType.cutout());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState soil = worldIn.getBlockState(pos.below());
        if (!(soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK) || soil.is(ModBlocks.BLOCKNATURE.get()))) {
            return false;
        }
        // 诅咒蓟必须处于七咒之戒持有者的影响范围内才能存活。
        if (!(worldIn instanceof Level level)) {
            return false;
        }
        return level.getEntitiesOfClass(Player.class,
                        new net.minecraft.world.phys.AABB(pos).inflate(8.0D))
                .stream().anyMatch(CurseAbilityHandler.INSTANCE::hasFullCurses);
    }
}
