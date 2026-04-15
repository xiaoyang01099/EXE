package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

public class GuiButtonSort extends Button {
    public GuiButtonSort(int x, int y, int width) {
        super(Button.builder(
                Component.translatable("button.powerinventory.sort"),
                btn -> NetworkHandler.CHANNEL.sendToServer(new SortPacket())
        ).pos(x, y).size(width, 20));
    }
}