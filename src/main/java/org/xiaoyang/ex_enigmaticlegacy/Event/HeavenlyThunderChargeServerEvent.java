package org.xiaoyang.ex_enigmaticlegacy.Event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Util.ServerExcaliburChargeTracker;
import org.xiaoyang.ex_enigmaticlegacy.Util.ServerKeyChargeTracker;
import org.xiaoyang.ex_enigmaticlegacy.Util.ServerSkillCooldowns;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

@Mod.EventBusSubscriber(modid = Exe.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HeavenlyThunderChargeServerEvent {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ServerKeyChargeTracker.tick(event.getServer());
        ServerExcaliburChargeTracker.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer receiver)) return;
        if (!(event.getTarget() instanceof ServerPlayer chargingPlayer)) return;
        ServerKeyChargeTracker.sendStateToPlayer(chargingPlayer, receiver);
        ServerExcaliburChargeTracker.sendStateToPlayer(chargingPlayer, receiver);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerKeyChargeTracker.stopHeavenlyThunder(player);
        ServerExcaliburChargeTracker.stop(player);
        ServerSkillCooldowns.clearPlayer(player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerKeyChargeTracker.stopHeavenlyThunder(player);
        ServerExcaliburChargeTracker.stop(player);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerSkillCooldowns.clearAll();
        ServerExcaliburChargeTracker.clearAll();
    }
}
