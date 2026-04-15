package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

public class GuiButtonDump extends Button {

    public GuiButtonDump(int x, int y, int width) {
        super(Button.builder(
                Component.translatable("button.powerinventory.deposit"),
                btn -> NetworkHandler.CHANNEL.sendToServer(new DumpButtonPacket())
        ).pos(x, y).size(width, 20));
    }
}