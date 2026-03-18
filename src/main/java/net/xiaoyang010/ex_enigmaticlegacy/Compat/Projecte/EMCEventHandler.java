package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte;

import moze_intel.projecte.api.event.EMCRemapEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public class EMCEventHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEMCRemap(EMCRemapEvent event) {
        EMCHelper.forceCleanupEMCMap();
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        EMCWandHandler.loadData();
    }

    @SubscribeEvent
    public static void onEmcRemap(EMCRemapEvent event) {
        EMCWandHandler.applyCustomValuesAfterRemap();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && !EMCWandHandler.customEmcValues.isEmpty()) {
            server.execute(() -> {
            });
        }
    }
}