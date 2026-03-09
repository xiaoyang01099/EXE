package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE)
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            ResourceKey<Level> currentDimension = level.dimension();
            ServerPlayer serverPlayer = (ServerPlayer) player;
            ServerLevel targetLevel;
            BlockPos targetPos;

            if (currentDimension == Level.OVERWORLD) {
                targetLevel = serverPlayer.getServer().getLevel(Level.OVERWORLD);
                targetPos = serverPlayer.getRespawnPosition();

                if (targetPos == null) {
                    targetPos = targetLevel.getSharedSpawnPos();
                }
            } else {
                targetLevel = serverPlayer.getServer().getLevel(Level.OVERWORLD);
                targetPos = targetLevel.getSharedSpawnPos();
            }

            if (targetLevel != null) {
                ((ServerLevel) level).sendParticles(ParticleTypes.PORTAL,
                        player.getX(),
                        player.getY() + 1.0D,
                        player.getZ(),
                        50,
                        0.5D,
                        0.5D,
                        0.5D,
                        0.1D
                );

                level.playSound(null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );

                serverPlayer.teleportTo(targetLevel,
                        targetPos.getX() + 0.5D,
                        targetPos.getY() + 0.5D,
                        targetPos.getZ() + 0.5D,
                        serverPlayer.getYRot(),
                        serverPlayer.getXRot()
                );

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

        player.getCooldowns().addCooldown(this, 100);

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(new TranslatableComponent("tooltip.return_item.usage")
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}