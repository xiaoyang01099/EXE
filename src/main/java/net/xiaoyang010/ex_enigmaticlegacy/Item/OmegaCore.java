package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

import java.util.UUID;

public class OmegaCore extends Item {

    private static boolean isTimeStopped = false;
    private static int timeStopTicks = 0; // 时停计时器
    private static UUID timeStopPlayerUUID = null; // 跟踪当前时停的玩家

    public OmegaCore() {
        super(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM).stacksTo(1).fireResistant().rarity(ModRarities.MIRACLE));
    }

    @Override
    public boolean hasCraftingRemainingItem() {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemstack) {
        return new ItemStack(this);
    }

    @Mod.EventBusSubscriber
    public static class TimeStopHandler {

        @SubscribeEvent
        public static void onPlayerLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
            Player player = event.getPlayer();
            Level level = player.getLevel(); // 获取玩家所在的世界

            // 如果已经在时停中，其他玩家不能再激活时停
            if (isTimeStopped && !player.getUUID().equals(timeStopPlayerUUID)) {
                player.sendMessage(new TextComponent("时停已激活，你无法使用时停。"), player.getUUID());
                return;
            }

            // 检查玩家手持的物品是否为 OmegaCore
            if (player.isShiftKeyDown() && isHoldingOmegaCore(player)) {
                // 切换时停状态
                isTimeStopped = !isTimeStopped;

                if (isTimeStopped) {
                    // 开启时停，记录启动时停的玩家
                    timeStopPlayerUUID = player.getUUID();
                    timeStopTicks = 1200; // 50秒（20 ticks = 1秒，50秒 = 1200 ticks）
                    player.sendMessage(new TextComponent("「「世界（ザ・ワールド）」ッ！時よ止まれ！」"), player.getUUID());
                } else {
                    // 关闭时停
                    player.sendMessage(new TextComponent("時は動き出す"), player.getUUID());
                    timeStopPlayerUUID = null; // 重置时停玩家
                }
            }
        }

        // 广播消息给所有玩家
        private static void broadcastMessage(Level level, String message) {
            for (Player player : level.players()) {
                player.sendMessage(new TextComponent(message), player.getUUID());
            }
        }

        // 检查玩家是否手持 OmegaCore
        private static boolean isHoldingOmegaCore(Player player) {
            ItemStack mainHandItem = player.getMainHandItem();
            ItemStack offHandItem = player.getOffhandItem();

            // 检查主手或副手是否持有 OmegaCore
            return mainHandItem.getItem() instanceof OmegaCore || offHandItem.getItem() instanceof OmegaCore;
        }

        // 更新时停倒计时
        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                OmegaCore.updateTimeStop(); // 更新时停状态
            }
        }

    }

    // 是否时停
    public static boolean isTimeStopped() {
        return isTimeStopped;
    }

    // 获取时停的玩家UUID
    public static UUID getTimeStopPlayerUUID() {
        return timeStopPlayerUUID;
    }

    // 获取当前时停玩家对象
    public static Player getTimeStopPlayer(Level level) {
        if (timeStopPlayerUUID == null) {
            return null;
        }

        // 在当前世界查找对应UUID的玩家
        for (Player player : level.players()) {
            if (player.getUUID().equals(timeStopPlayerUUID)) {
                return player;
            }
        }
        return null;
    }

    // 更新时停状态
    public static void updateTimeStop() {
        if (isTimeStopped && timeStopTicks > 0) {
            timeStopTicks--;
        } else if (isTimeStopped && timeStopTicks <= 0) {
            // 时停时间结束，恢复正常
            isTimeStopped = false;
            timeStopPlayerUUID = null;
        }
    }
}
