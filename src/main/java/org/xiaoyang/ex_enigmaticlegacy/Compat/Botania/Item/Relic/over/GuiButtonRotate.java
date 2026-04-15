package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

public class GuiButtonRotate extends Button {
    private final int storageSection;

    public GuiButtonRotate(int x, int y, int width, int height, int section) {
        super(Button.builder(
                Component.literal("↻"),
                btn -> NetworkHandler.CHANNEL.sendToServer(new SwapInvoPacket(section))
        ).pos(x, y).size(width, height));
        this.storageSection = section;
    }
}