package net.xiaoyang010.ex_enigmaticlegacy.api;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public interface IPreventBreakInCreative {
    class EventHandler {
        static {
            MinecraftForge.EVENT_BUS.register(EventHandler.class);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onHarvestCheck(PlayerEvent.HarvestCheck event) {
            Player player = event.getPlayer();

            if (player.isCreative() &&
                    player.getMainHandItem().getItem() instanceof IPreventBreakInCreative) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
            Player player = event.getPlayer();

            if (player.isCreative() &&
                    player.getMainHandItem().getItem() instanceof IPreventBreakInCreative) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
            Player player = event.getPlayer();

            if (player.isCreative() &&
                    event.getItemStack().getItem() instanceof IPreventBreakInCreative) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            Player player = event.getPlayer();

            if (player.isCreative() &&
                    player.getMainHandItem().getItem() instanceof IPreventBreakInCreative) {
                event.setCanceled(true);
            }
        }

        public static void init() {
        }
    }
}
