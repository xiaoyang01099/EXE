package net.xiaoyang010.ex_enigmaticlegacy.Event;


import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.curio.HolyRing;

@Mod.EventBusSubscriber
public class ColorToolTipBar {
    @SubscribeEvent
    public static void onTooltip(RenderTooltipEvent.Color event) {
        float hue = (float) Util.getMillis() / 5000.0F % 1.0F;
        float c =0xff000000| Mth.hsvToRgb(hue, 2.0F, 1.5F);
        int cd= (int)0xff000000| Mth.hsvToRgb(hue, 2.0F, 0.1F);
        if (event.getItemStack().getItem() instanceof HolyRing) {
            event.setBorderEnd((int)c);
            event.setBorderStart((int)c/*Mth.hsvToRgb(hue, 2.0F, 1.5F)*/);
            event.setBackground(cd);
        }
    }
}
