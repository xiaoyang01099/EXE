package net.xiaoyang010.ex_enigmaticlegacy.api.test.res;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.api.PoolCorruptionData;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.api.PoolCorruptionManager;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.mana.TilePool;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 魔力池净化器
 * 用于净化被污染的魔力池或将诅咒魔力池转回原版魔力池
 */
public class ItemManaPurificationCrystal extends Item {

    public ItemManaPurificationCrystal(Properties properties) {
        super(properties.stacksTo(1).durability(100));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) return InteractionResult.PASS;

        BlockEntity tile = level.getBlockEntity(pos);

        // ✅ 净化被污染的原版魔力池
        if (tile instanceof TilePool normalPool && !(tile instanceof TileCursedManaPool)) {
            return purifyNormalPool(level, pos, player, stack, normalPool);
        }

        // ✅ 将诅咒魔力池转回原版魔力池
        if (tile instanceof TileCursedManaPool cursedPool) {
            return convertToNormalPool(level, pos, player, stack, cursedPool);
        }

        return InteractionResult.PASS;
    }

    /**
     * ✅ 净化被污染的原版魔力池
     */
    private InteractionResult purifyNormalPool(Level level, BlockPos pos, Player player, ItemStack stack, TilePool pool) {
        PoolCorruptionData corruptionData = PoolCorruptionManager.getOrCreate(level, pos);

        if (corruptionData.getCorruption() <= 0) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        new TranslatableComponent("item.ex_enigmaticlegacy.purification_crystal.no_corruption")
                                .withStyle(ChatFormatting.GREEN),
                        true
                );
            }
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            // 净化
            int purified = Math.min(corruptionData.getCorruption(), 20);
            corruptionData.reduceCorruption(purified);

            // 消耗耐久
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(p.getUsedItemHand()));

            // 音效
            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.5F);
            player.displayClientMessage(
                    new TranslatableComponent("item.ex_enigmaticlegacy.purification_crystal.success",
                            purified, corruptionData.getCorruption())
                            .withStyle(ChatFormatting.AQUA),
                    true
            );
            // 如果完全净化,移除数据
            if (corruptionData.getCorruption() <= 0) {
                PoolCorruptionManager.remove(level, pos);
            }
        } else {
            // 客户端粒子效果
            spawnPurificationParticles(level, pos);
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * ✅ 将诅咒魔力池转回原版魔力池
     */
    private InteractionResult convertToNormalPool(Level level, BlockPos pos, Player player, ItemStack stack, TileCursedManaPool cursedPool) {
        if (!level.isClientSide) {
            // 保存魔力(损失50%)
            int cursedMana = cursedPool.getCurrentCursedMana();
            int normalMana = cursedMana / 2;

            // 替换方块
            BlockState normalPoolState = ModBlocks.manaPool.defaultBlockState();
            level.setBlockAndUpdate(pos, normalPoolState);

            // 设置魔力
            BlockEntity newTile = level.getBlockEntity(pos);
            if (newTile instanceof TilePool normalPool) {
                normalPool.receiveMana(normalMana);
            }

            // 消耗耐久
            stack.hurtAndBreak(10, player, p -> p.broadcastBreakEvent(p.getUsedItemHand()));

            // 音效
            level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 1.2F);
        } else {
            // 客户端粒子效果
            spawnConversionParticles(level, pos);
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * ✨ 净化粒子效果
     */
    private void spawnPurificationParticles(Level level, BlockPos pos) {
        for (int i = 0; i < 30; i++) {
            double x = pos.getX() + 0.5 + (Math.random() - 0.5) * 1.5;
            double y = pos.getY() + 0.5 + (Math.random() - 0.5) * 1.5;
            double z = pos.getZ() + 0.5 + (Math.random() - 0.5) * 1.5;

            WispParticleData data = WispParticleData.wisp(0.2F, 0.0F, 1.0F, 1.0F, true);
            level.addParticle(data, x, y, z, 0, 0.05, 0);
        }
    }

    /**
     * ✨ 转化粒子效果
     */
    private void spawnConversionParticles(Level level, BlockPos pos) {
        for (int i = 0; i < 50; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 1.5;
            double x = pos.getX() + 0.5 + Math.cos(angle) * radius;
            double y = pos.getY() + 0.5 + Math.random() * 2.0;
            double z = pos.getZ() + 0.5 + Math.sin(angle) * radius;

            // 从紫色渐变到蓝色
            float t = (float) i / 50;
            float r = 0.5F * (1 - t);
            float g = 0.0F + t;
            float b = 0.5F + 0.5F * t;

            WispParticleData data = WispParticleData.wisp(0.3F, r, g, b, true);
            level.addParticle(data, x, y, z, 0, 0.1, 0);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        int remaining = stack.getMaxDamage() - stack.getDamageValue();
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.purification_crystal.uses", remaining)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.purification_crystal.tooltip")
                .withStyle(ChatFormatting.AQUA));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getDamageValue() > 0;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00FFFF; // 青色
    }
}
