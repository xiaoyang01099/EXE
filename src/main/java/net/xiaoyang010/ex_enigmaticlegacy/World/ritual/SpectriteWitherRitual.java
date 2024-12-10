package net.xiaoyang010.ex_enigmaticlegacy.World.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel; // 导入正确的 ServerLevel
import net.minecraft.sounds.SoundEvents; // 导入 SoundEvents
import net.minecraft.sounds.SoundSource; // 导入 SoundSource 代替 SoundCategory
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType; // 导入 MobSpawnType 代替 SpawnReason
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class SpectriteWitherRitual {

    // 创建一个用于调度任务的调度器
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // 定义仪式结构的相对位置及对应的方块类型
    private static final Map<BlockPos, Block> RITUAL_STRUCTURE = new HashMap<>();

    static {
        // 这里将根据你的图片定义结构。以中心位置为基准，相对坐标 + 方块类型
        RITUAL_STRUCTURE.put(new BlockPos(0, 0, 0), Blocks.GREEN_WOOL); // 中心
        RITUAL_STRUCTURE.put(new BlockPos(1, 0, 0), Blocks.PURPLE_WOOL);
        RITUAL_STRUCTURE.put(new BlockPos(-1, 0, 0), Blocks.PURPLE_WOOL);
        RITUAL_STRUCTURE.put(new BlockPos(0, 0, 1), Blocks.PURPLE_WOOL);
        RITUAL_STRUCTURE.put(new BlockPos(0, 0, -1), Blocks.PURPLE_WOOL);
        // 更多的方块和相对位置，你需要根据图片中的结构自行添加
    }

    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isClientSide()) return;

        ServerLevel world = (ServerLevel) event.getWorld(); // 使用 ServerLevel
        BlockPos pos = event.getPos();
        Player player = event.getPlayer(); // 更改 PlayerEntity 为 Player
        ItemStack heldItem = player.getMainHandItem(); // 获取主手物品

        // 检查玩家是否持有金锭作为触发物品
        if (heldItem.getItem() != Items.GOLD_INGOT) return;

        // 检查方块排列是否符合仪式要求
        if (checkRitualConditions(world, pos)) {
            // 播放召唤动画（粒子效果和声音）
            playSummoningAnimation(world, pos);

            // 5秒后召唤生物
            scheduler.schedule(() -> {
                spawnCreature(world, pos.above());
            }, 5, TimeUnit.SECONDS);
        }
    }

    private static boolean checkRitualConditions(ServerLevel world, BlockPos centerPos) {
        // 遍历所有定义的结构
        for (Map.Entry<BlockPos, Block> entry : RITUAL_STRUCTURE.entrySet()) {
            BlockPos relativePos = entry.getKey(); // 获取相对位置
            Block expectedBlock = entry.getValue(); // 获取预期的方块类型

            BlockPos checkPos = centerPos.offset(relativePos); // 计算检查位置
            BlockState state = world.getBlockState(checkPos); // 获取该位置的方块状态
            if (state.getBlock() != expectedBlock) {
                return false; // 如果任何一个方块不符合预期，则返回 false
            }
        }
        return true; // 所有方块都符合预期
    }

    private static void playSummoningAnimation(ServerLevel world, BlockPos pos) {
        // 播放粒子效果
        world.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 50, 0.5, 0.5, 0.5, 0.0);
        world.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F); // 使用 SoundSource 代替 SoundCategory
    }

    private static void spawnCreature(ServerLevel world, BlockPos pos) {
        // 召唤自定义生物
        EntityType<?> entityType = ModEntities.SPECTRITE_WITHER.get();
        entityType.spawn(world, null, null, pos, MobSpawnType.EVENT, true, true); // 使用 MobSpawnType 代替 SpawnReason
    }
}
