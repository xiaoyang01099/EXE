package org.xiaoyang.ex_enigmaticlegacy.Item.res;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

public class OmegaCore extends Item {

    private static boolean isTimeStopped = false;
    private static int timeStopTicks = 0; // 时停计时器
    private static UUID timeStopPlayerUUID = null; // 跟踪当前时停的玩家

    public OmegaCore() {
        super(new Properties().stacksTo(1).fireResistant());
    }
    
    @Mod.EventBusSubscriber
    public static class TimeStopHandler {

        @SubscribeEvent
        public static void onPlayerLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
            Player player = event.getEntity();

            if (isTimeStopped && !player.getUUID().equals(timeStopPlayerUUID)) {
                player.sendSystemMessage(Component.translatable("时停已激活，你无法使用时停。"));
                return;
            }

            if (player.isShiftKeyDown() && isHoldingOmegaCore(player)) {
                isTimeStopped = !isTimeStopped;

                if (isTimeStopped) {
                    timeStopPlayerUUID = player.getUUID();
                    timeStopTicks = 1200;
                    player.sendSystemMessage(Component.translatable("「「世界（ザ・ワールド）」ッ！時よ止まれ！」"));
                } else {
                    player.sendSystemMessage(Component.translatable("時は動き出す"));
                    timeStopPlayerUUID = null;
                }
            }
        }

        private static void broadcastMessage(Level level, String message) {
            for (Player player : level.players()) {
                player.sendSystemMessage(Component.translatable(message));
            }
        }

        private static boolean isHoldingOmegaCore(Player player) {
            ItemStack mainHandItem = player.getMainHandItem();
            ItemStack offHandItem = player.getOffhandItem();

            return mainHandItem.getItem() instanceof OmegaCore || offHandItem.getItem() instanceof OmegaCore;
        }

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                OmegaCore.updateTimeStop();
            }
        }
    }

    public static boolean isTimeStopped() {
        return isTimeStopped;
    }

    public static UUID getTimeStopPlayerUUID() {
        return timeStopPlayerUUID;
    }

    public static Player getTimeStopPlayer(Level level) {
        if (timeStopPlayerUUID == null) {
            return null;
        }

        for (Player player : level.players()) {
            if (player.getUUID().equals(timeStopPlayerUUID)) {
                return player;
            }
        }
        return null;
    }

    public static void updateTimeStop() {
        if (isTimeStopped && timeStopTicks > 0) {
            timeStopTicks--;
        } else if (isTimeStopped && timeStopTicks <= 0) {
            isTimeStopped = false;
            timeStopPlayerUUID = null;
        }
    }
}
