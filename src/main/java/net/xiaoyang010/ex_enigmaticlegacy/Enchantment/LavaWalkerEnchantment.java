package net.xiaoyang010.ex_enigmaticlegacy.Enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEnchantments;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class LavaWalkerEnchantment extends Enchantment {

    // 用于存储临时方块的位置和它们的生存时间
    private static final Map<BlockPos, Integer> COOLED_BLOCKS = new HashMap<>();
    // 定义方块的持续时间（以刻为单位）
    private static final int BLOCK_DURATION = 100; // 2秒

    public LavaWalkerEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }

    @Override
    public int getMinCost(int level) {
        return level * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 15;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.FROST_WALKER;
    }

    /**
     * 处理玩家移动时触发熔岩行者效果
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // 检查玩家是否有熔岩行者附魔
            int level = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.LAVA_WALKER.get(), event.player);

            if (level > 0) {
                freezeLava(event.player, level);
            }
        }

        // 更新冷却的方块
        updateCooledBlocks(event.player.level);
    }

    /**
     * 冷却玩家周围的岩浆
     */
    private static void freezeLava(LivingEntity entity, int level) {
        Level world = entity.level;
        BlockPos pos = entity.blockPosition();

        // 根据附魔等级计算范围
        int range = 1 + level;

        for (BlockPos blockPos : BlockPos.betweenClosed(
                pos.offset(-range, -1, -range),
                pos.offset(range, -1, range))) {

            // 检查这个位置是否有岩浆
            if (world.getBlockState(blockPos).getBlock() == Blocks.LAVA
                    && world.getBlockState(blockPos).getFluidState().isSource()) {

                // 检查上方方块，确保没有实体挡住
                if (world.getBlockState(blockPos.above()).isAir() ||
                        !world.getBlockState(blockPos.above()).getMaterial().blocksMotion()) {

                    // 将岩浆冷却为黑曜石
                    BlockState cooledState = Blocks.OBSIDIAN.defaultBlockState();

                    // 检查实体碰撞
                    if (world.isUnobstructed(cooledState, blockPos, CollisionContext.empty())) {
                        world.setBlockAndUpdate(blockPos, cooledState);

                        // 添加到临时方块映射中
                        COOLED_BLOCKS.put(blockPos.immutable(), BLOCK_DURATION);
                    }
                }
            }
        }
    }

    /**
     * 更新临时冷却的方块，当它们的时间结束时恢复为岩浆
     */
    private static void updateCooledBlocks(Level world) {
        if (!COOLED_BLOCKS.isEmpty()) {
            COOLED_BLOCKS.entrySet().removeIf(entry -> {
                BlockPos pos = entry.getKey();
                int timeLeft = entry.getValue() - 1;

                if (timeLeft <= 0) {
                    // 如果时间到了，恢复为岩浆
                    if (world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN) {
                        world.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
                    }
                    return true; // 从映射中移除
                } else {
                    entry.setValue(timeLeft); // 更新时间
                    return false;
                }
            });
        }
    }
}