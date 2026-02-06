package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.client.gui.components.Button;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;

public class GuiButtonFilter extends Button {

    public GuiButtonFilter(int x, int y, int width) {
        super(x, y, width, 20,
                EComponent.translatable("button.powerinventory.filter"),
                GuiButtonFilter::onPress);
    }

    private static void onPress(Button button) {
        NetworkHandler.CHANNEL.sendToServer(new FilterButtonPacket());
    }
}