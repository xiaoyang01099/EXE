package net.xiaoyang010.ex_enigmaticlegacy.api.test.yuhua;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.weapon.WIP.BladeFallenStar;

@Mod.EventBusSubscriber(
        modid = ExEnigmaticlegacyMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class YuhuaServerEvents {

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END
                && event.side == LogicalSide.SERVER) {
            BladeFallenStar.tickYuhuaEntities(event.world);
        }
    }
}