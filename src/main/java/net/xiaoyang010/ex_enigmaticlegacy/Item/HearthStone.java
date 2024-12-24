package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

import javax.annotation.Nullable;
import java.util.List;

public class HearthStone extends Item {
    public HearthStone() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE)
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // 获取玩家当前所在维度
            ResourceKey<Level> currentDimension = level.dimension();
            ServerPlayer serverPlayer = (ServerPlayer) player;
            ServerLevel targetLevel;
            BlockPos targetPos;

            if (currentDimension == Level.OVERWORLD) {
                // 如果在主世界，传送到重生点
                targetLevel = serverPlayer.getServer().getLevel(Level.OVERWORLD);
                targetPos = serverPlayer.getRespawnPosition();

                // 如果没有重生点，使用世界出生点
                if (targetPos == null) {
                    targetPos = targetLevel.getSharedSpawnPos();
                }
            } else {
                // 如果在其他维度，传送回主世界出生点
                targetLevel = serverPlayer.getServer().getLevel(Level.OVERWORLD);
                targetPos = targetLevel.getSharedSpawnPos();
            }

            // 执行传送
            if (targetLevel != null) {
                // 添加传送前的粒子效果
                ((ServerLevel)level).sendParticles(ParticleTypes.PORTAL,
                        player.getX(),
                        player.getY() + 1.0D,
                        player.getZ(),
                        50,  // 粒子数量
                        0.5D,  // X轴扩散
                        0.5D,  // Y轴扩散
                        0.5D,  // Z轴扩散
                        0.1D   // 粒子速度
                );

                // 播放传送开始音效
                level.playSound(null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );

                // 执行传送
                serverPlayer.teleportTo(targetLevel,
                        targetPos.getX() + 0.5D,
                        targetPos.getY() + 0.5D,
                        targetPos.getZ() + 0.5D,
                        serverPlayer.getYRot(),
                        serverPlayer.getXRot()
                );

                // 传送后在目标位置也添加粒子效果
                targetLevel.sendParticles(ParticleTypes.PORTAL,
                        targetPos.getX() + 0.5D,
                        targetPos.getY() + 1.0D,
                        targetPos.getZ() + 0.5D,
                        50,
                        0.5D,
                        0.5D,
                        0.5D,
                        0.1D
                );
            }
        }

        // 添加使用冷却
        player.getCooldowns().addCooldown(this, 100); // 5秒冷却时间

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(new TextComponent("右键使用返回重生点或主世界")
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}