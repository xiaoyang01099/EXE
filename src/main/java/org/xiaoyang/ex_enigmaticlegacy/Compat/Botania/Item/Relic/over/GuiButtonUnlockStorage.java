package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;


@OnlyIn(Dist.CLIENT)
public class GuiButtonUnlockStorage extends GuiButtonUnlockExp {
    public static final int height = 20;
    public static final int width = 70;
    private final int storageIndex;

    public GuiButtonUnlockStorage(int x, int y, Player player, int cost, int storageIndex) {
        super(x, y, width, height, player, cost);
        this.storageIndex = storageIndex;
        this.setTooltip(Tooltip.create(
                Component.translatable("tooltip.powerinventory.storage")
        ));
    }

    @Override
    public void onPress() {
        NetworkHandler.CHANNEL.sendToServer(new UnlockStoragePacket(this.storageIndex));
    }
}