package org.xiaoyang.ex_enigmaticlegacy.Compat.Projecte;

import moze_intel.projecte.api.event.EMCRemapEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EMCEventHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEMCRemap(EMCRemapEvent event) {
        EMCHelper.forceCleanupEMCMap();
    }
}