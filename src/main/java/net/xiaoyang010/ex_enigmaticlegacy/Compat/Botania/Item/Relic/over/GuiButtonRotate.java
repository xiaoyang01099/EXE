package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.client.gui.components.Button;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;

public class GuiButtonRotate extends Button {

    private final int storageSection;

    public GuiButtonRotate(int x, int y, int width, int height, int section) {
        super(x, y, width, height,
                EComponent.literal("↻"),
                button -> ((GuiButtonRotate)button).onPress());
        this.storageSection = section;
    }

    public void onPress() {
        NetworkHandler.CHANNEL.sendToServer(new SwapInvoPacket(storageSection));
    }
}