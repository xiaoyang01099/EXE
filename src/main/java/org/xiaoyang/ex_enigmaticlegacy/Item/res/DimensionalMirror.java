package org.xiaoyang.ex_enigmaticlegacy.Item.res;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.xiaoyang.ex_enigmaticlegacy.Container.DimensionalMirrorContainer;


import javax.annotation.Nullable;
import java.util.List;

public class DimensionalMirror extends Item {
    public DimensionalMirror() {
        super(new Properties());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            spawnStarParticles((ServerPlayer) player);

            NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("item.dimension.mirror");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                    return new DimensionalMirrorContainer(containerId, inventory, player);
                }
            });
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    private void spawnStarParticles(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        double radius = 1.0;
        int particleCount = 20;

        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double x = player.getX() + radius * Math.cos(angle);
            double z = player.getZ() + radius * Math.sin(angle);

            level.sendParticles(ParticleTypes.END_ROD,
                    x,
                    player.getY() + 1.0,
                    z,
                    1,
                    0, 0.05, 0,
                    0.02
            );

            level.sendParticles(ParticleTypes.ENCHANT,
                    player.getX() + (level.getRandom().nextDouble() - 0.5) * 2,
                    player.getY() + level.getRandom().nextDouble() * 2,
                    player.getZ() + (level.getRandom().nextDouble() - 0.5) * 2,
                    1,
                    0, 0, 0,
                    0.1
            );
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.dimension.mirror.usage")
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}