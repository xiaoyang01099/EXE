package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader.RainbowAvaritiaShaders;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;


@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AvaritiaClient {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRegisterShaders(RegisterShadersEvent event) {
        RainbowAvaritiaShaders.onRegisterShaders(event);
    }
}