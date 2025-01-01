package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Container.DimensionalMirrorContainer;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

import javax.annotation.Nullable;
import java.util.List;

public class DimensionalMirror extends Item {
    public DimensionalMirror() {
        super(new Item.Properties()
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // 播放星星粒子效果
            spawnStarParticles((ServerPlayer) player);

            NetworkHooks.openGui((ServerPlayer) player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return new TextComponent("维度传送镜");
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
        // 生成旋转的星星粒子
        double radius = 1.0;  // 粒子环绕半径
        int particleCount = 20;  // 每圈的粒子数量

        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double x = player.getX() + radius * Math.cos(angle);
            double z = player.getZ() + radius * Math.sin(angle);

            // 在玩家周围生成星星粒子
            player.getLevel().sendParticles(ParticleTypes.END_ROD,  // 使用末地烛粒子（看起来像星星）
                    x,
                    player.getY() + 1.0,
                    z,
                    1,  // 每个位置生成1个粒子
                    0, 0.05, 0,  // 粒子的运动速度
                    0.02  // 粒子的速度
            );

            // 额外添加一些随机的星星粒子
            player.getLevel().sendParticles(ParticleTypes.ENCHANT,  // 附魔台的粒子效果
                    player.getX() + (player.getLevel().getRandom().nextDouble() - 0.5) * 2,
                    player.getY() + player.getLevel().getRandom().nextDouble() * 2,
                    player.getZ() + (player.getLevel().getRandom().nextDouble() - 0.5) * 2,
                    1,
                    0, 0, 0,
                    0.1
            );
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(new TextComponent("右键打开传送界面").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}