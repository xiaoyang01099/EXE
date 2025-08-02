package net.xiaoyang010.ex_enigmaticlegacy.World.ritual;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.CatMewEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CatMewRitual {
    // 仪式台基础范围
    public static final float RITUAL_RANGE = 9F;
    public static final int RITUAL_HEIGHT = 5;

    // 仪式台四个角的水晶柱位置
    private static final List<BlockPos> CRYSTAL_LOCATIONS = ImmutableList.of(
            new BlockPos(4, 1, 4),
            new BlockPos(4, 1, -4),
            new BlockPos(-4, 1, 4),
            new BlockPos(-4, 1, -4)
    );

    // 定义仪式台结构
    public static final Supplier<IMultiblock> RITUAL_MULTIBLOCK = Suppliers.memoize(() -> {
        var ritualBase = PatchouliAPI.get().predicateMatcher(ModBlockss.PRISMATICRADIANCEBLOCK.get(),
                state -> state.is(ModBlockss.PRISMATICRADIANCEBLOCK.get()));

        return PatchouliAPI.get().makeMultiblock(
                new String[][] {
                        {
                                "P_______P",
                                "_________",
                                "_________",
                                "_________",
                                "____A____",
                                "_________",
                                "_________",
                                "_________",
                                "P_______P"
                        },
                        {
                                "_________",
                                "_________",
                                "_________",
                                "___RRR___",
                                "___RBR___",
                                "___RRR___",
                                "_________",
                                "_________",
                                "_________"
                        }
                },
                'P', ModEntities.SPECTRITE_CRYSTAL.get(),
                'R', ModBlockss.PRISMATICRADIANCEBLOCK.get(),
                'B', Blocks.BEACON,
                'A', Blocks.ANCIENT_DEBRIS
        );
    });

    public static boolean spawn(Player player, ItemStack stack, Level world, BlockPos pos) {
        // 基础检查
        if (!basicChecks(player, world)) {
            return false;
        }

        // 检查仪式水晶
        List<BlockPos> invalidCrystals = checkRitualCrystals(world, pos);
        if (!invalidCrystals.isEmpty()) {
            if (!world.isClientSide) {
                player.sendMessage(new TranslatableComponent("ritual.missing_crystals")
                        .withStyle(ChatFormatting.RED), player.getUUID());
            }
            return false;
        }

        // 检查仪式平台结构
        List<BlockPos> invalidStructure = checkRitualStructure(world, pos);
        if (!invalidStructure.isEmpty()) {
            if (!world.isClientSide) {
                player.sendMessage(new TranslatableComponent("ritual.invalid_structure")
                        .withStyle(ChatFormatting.RED), player.getUUID());
            }
            return false;
        }

        // 所有检查通过,开始生成
        if (!world.isClientSide) {
            // 消耗物品
            stack.shrink(1);

            // 播放仪式音效与粒子
            playRitualEffects(world, pos);

            // 生成Boss
            CatMewEntity catMew = ModEntities.KIND_MIAO.get().create(world);
            catMew.moveTo(pos.getX() + 0.5, pos.getY() + 3, pos.getZ() + 0.5);
            catMew.setHealth(catMew.getMaxHealth());

            world.addFreshEntity(catMew);
        }

        return true;
    }

    // 基础检查
    private static boolean basicChecks(Player player, Level world) {
        // 检查难度
        if (world.getDifficulty() == Difficulty.PEACEFUL) {
            if (!world.isClientSide) {
                player.sendMessage(new TranslatableComponent("ritual.peaceful_difficulty")
                        .withStyle(ChatFormatting.RED), player.getUUID());
            }
            return false;
        }

        // 检查是否已有Boss存在
        if (countExistingBosses(world, player.blockPosition()) > 0) {
            if (!world.isClientSide) {
                player.sendMessage(new TranslatableComponent("ritual.boss_exists")
                        .withStyle(ChatFormatting.RED), player.getUUID());
            }
            return false;
        }

        return true;
    }

    // 检查仪式水晶
    private static List<BlockPos> checkRitualCrystals(Level world, BlockPos center) {
        List<BlockPos> invalidPositions = new ArrayList<>();

        for (BlockPos offset : CRYSTAL_LOCATIONS) {
            BlockPos crystalPos = center.offset(offset);
            BlockState state = world.getBlockState(crystalPos);

            if (!state.is((HolderSet<Block>) ModEntities.SPECTRITE_CRYSTAL.get())) {
                invalidPositions.add(crystalPos);
            }
        }

        return invalidPositions;
    }

    // 检查仪式平台结构
    private static List<BlockPos> checkRitualStructure(Level world, BlockPos center) {
        List<BlockPos> invalidPositions = new ArrayList<>();
        int range = (int) Math.ceil(RITUAL_RANGE);

        // 检查地面结构
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                if (Math.abs(x) == 4 && Math.abs(z) == 4) {
                    continue; // 跳过水晶柱位置
                }

                BlockPos pos = center.offset(x, 0, z);
                BlockState state = world.getBlockState(pos);

                // 检查中心和外圈
                if ((x == 0 && z == 0) || MathHelper.outOfCircle(x, z)) {
                    if (!state.is(ModBlockss.PRISMATICRADIANCEBLOCK.get())) {
                        invalidPositions.add(pos);
                    }
                }
            }
        }

        // 检查信标和远古残骸
        if (!world.getBlockState(center).is(Blocks.BEACON)) {
            invalidPositions.add(center);
        }
        if (!world.getBlockState(center.above()).is(Blocks.ANCIENT_DEBRIS)) {
            invalidPositions.add(center.above());
        }

        return invalidPositions;
    }

    // 播放仪式特效
    private static void playRitualEffects(Level world, BlockPos pos) {
        if (world instanceof ServerLevel serverLevel) {
            // 播放音效
            world.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);

            // 生成粒子效果
            for (BlockPos crystal : CRYSTAL_LOCATIONS) {
                BlockPos crystalPos = pos.offset(crystal);
                serverLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        crystalPos.getX() + 0.5,
                        crystalPos.getY() + 1.5,
                        crystalPos.getZ() + 0.5,
                        50, 0.5, 0.5, 0.5, 0.1
                );
            }

            // 中心的升腾粒子
            for (int i = 0; i < 360; i += 8) {
                double angle = Math.toRadians(i);
                double x = pos.getX() + 0.5 + Math.cos(angle) * 2;
                double z = pos.getZ() + 0.5 + Math.sin(angle) * 2;

                serverLevel.sendParticles(
                        ParticleTypes.END_ROD,
                        x, pos.getY() + 1, z,
                        1, 0, 0.5, 0, 0.1
                );
            }
        }
    }

    // 统计已存在的Boss数量
    private static int countExistingBosses(Level world, BlockPos source) {
        return world.getEntitiesOfClass(CatMewEntity.class,
                        new AABB(source).inflate(64.0D))
                .size();
    }

    // 工具类:判断是否在圆形范围外
    private static class MathHelper {
        public static boolean outOfCircle(int x, int z) {
            return Math.sqrt(x * x + z * z) > RITUAL_RANGE;
        }
    }
}