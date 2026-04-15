package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

import java.util.List;


@OnlyIn(Dist.CLIENT)
public class GuiButtonUnlockPearl extends GuiButtonUnlockExp {
    private static final int WIDTH = 70;
    private static final int HEIGHT = 20;

    public GuiButtonUnlockPearl(int x, int y, Player player, int cost) {
        super(x, y, WIDTH, HEIGHT, player, cost);
        this.setTooltip(Tooltip.create(
                Component.translatable("tooltip.powerinventory.pearl")
        ));
    }

    @Override
    public void onPress() {
        NetworkHandler.CHANNEL.sendToServer(new UnlockPearlPacket());
    }
}