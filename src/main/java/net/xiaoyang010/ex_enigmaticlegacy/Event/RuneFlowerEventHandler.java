package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Hybrid.TileEntityRuneFlower;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.item.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RuneFlowerEventHandler {
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getWorld().isClientSide()) return;

        Level level = (Level) event.getWorld();
        BlockPos pos = event.getPos();
        var player = event.getPlayer();

        if (player == null) return;

        TileEntityRuneFlower activeFlower = findActiveSinFlower(level, pos, player.getUUID());

        if (activeFlower != null) {
            if (RANDOM.nextFloat() < 0.15f) {
                spawnBonusDrops(level, pos, event.getState());
            }
        }
    }

    /**
     * 寻找激活了七宗罪效果的符文花
     */
    private static TileEntityRuneFlower findActiveSinFlower(Level level, BlockPos pos, UUID playerUUID) {
        for (BlockPos checkPos : BlockPos.betweenClosed(
                pos.offset(-12, -12, -12),
                pos.offset(12, 12, 12))) {

            BlockEntity be = level.getBlockEntity(checkPos);
            if (be instanceof TileEntityRuneFlower flower) {
                if (flower.hasPlayerSinEffect(playerUUID)) {
                    return flower;
                }
            }
        }

        return null;
    }

    /**
     * 生成额外掉落物
     */
    private static void spawnBonusDrops(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        List<ItemStack> bonusDrops = new ArrayList<>();

        // 根据方块类型决定额外掉落
        if (state.is(BlockTags.LOGS) ||
                state.is(BlockTags.LEAVES)) {
            // 木头/树叶：掉落魔力珍珠
            if (RANDOM.nextFloat() < 0.3f) {
                bonusDrops.add(new ItemStack(ModItems.manaPearl, 1));
            }
        } else if (state.is(BlockTags.BASE_STONE_OVERWORLD)) {
            // 石头：掉落魔力粉
            if (RANDOM.nextFloat() < 0.2f) {
                bonusDrops.add(new ItemStack(ModItems.manaPowder, RANDOM.nextInt(3) + 1));
            }
        } else if (isOre(state)) {
            // 矿石：更高概率掉落魔力钻石
            if (RANDOM.nextFloat() < 0.05f) {
                bonusDrops.add(new ItemStack(ModItems.manaDiamond, 1));
            } else if (RANDOM.nextFloat() < 0.15f) {
                bonusDrops.add(new ItemStack(ModItems.manaPearl, RANDOM.nextInt(2) + 1));
            }
        }

        for (ItemStack drop : bonusDrops) {
            ItemEntity itemEntity = new ItemEntity(level,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    drop);

            itemEntity.setDeltaMovement(
                    (RANDOM.nextDouble() - 0.5) * 0.1,
                    RANDOM.nextDouble() * 0.2,
                    (RANDOM.nextDouble() - 0.5) * 0.1
            );

            level.addFreshEntity(itemEntity);

            spawnBonusDropParticles(serverLevel, pos);
        }
    }

    /**
     * 检查是否为矿石
     */
    private static boolean isOre(BlockState state) {
        return state.is(BlockTags.IRON_ORES) ||
                state.is(BlockTags.GOLD_ORES) ||
                state.is(BlockTags.COPPER_ORES) ||
                state.is(BlockTags.DIAMOND_ORES) ||
                state.is(BlockTags.EMERALD_ORES) ||
                state.is(BlockTags.LAPIS_ORES) ||
                state.is(BlockTags.REDSTONE_ORES) ||
                state.is(BlockTags.COAL_ORES);
    }

    /**
     * 播放额外掉落的粒子效果
     */
    private static void spawnBonusDropParticles(ServerLevel level, BlockPos pos) {
        BotaniaAPI.instance().sparkleFX(level,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                0.8F, 0.2F, 0.8F,
                1.5F, 20);
    }
}