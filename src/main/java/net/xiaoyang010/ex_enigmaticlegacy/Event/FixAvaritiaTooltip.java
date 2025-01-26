package net.xiaoyang010.ex_enigmaticlegacy.Event;

import morph.avaritia.item.tools.InfinitySwordItem;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Util.ColorText;

@Mod.EventBusSubscriber
public class FixAvaritiaTooltip {
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof InfinitySwordItem) {
            event.getToolTip().clear();
            event.getToolTip().add(Component.nullToEmpty("§c寰宇支配之剑"));
            event.getToolTip().add(Component.nullToEmpty(""));
            event.getToolTip().add(Component.nullToEmpty(ColorText.getGray("在主手时:")));
            event.getToolTip().add(Component.nullToEmpty(ColorText.GetColor1("infinity")+ColorText.GetGreen(" 攻击伤害")));
            event.getToolTip().add(Component.nullToEmpty(ColorText.GetGreen("1.6 攻击速度")));
        }
    }
}
