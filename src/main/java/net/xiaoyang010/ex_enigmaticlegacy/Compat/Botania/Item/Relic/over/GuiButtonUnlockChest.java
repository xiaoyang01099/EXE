package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;

@OnlyIn(Dist.CLIENT)
public class GuiButtonUnlockChest extends GuiButtonUnlockExp {
    public static final int WIDTH = 70;
    public static final int HEIGHT = 20;
    private Component tooltip;

    public GuiButtonUnlockChest(int x, int y, Player player, int cost) {
        super(x, y, WIDTH, HEIGHT, player, cost);
        this.tooltip = EComponent.translatable("tooltip.powerinventory.ender_chest");
    }

    @Override
    public void onPress() {
        NetworkHandler.CHANNEL.sendToServer(new UnlockChestPacket());
    }

    public Component getTooltip() {
        return this.tooltip;
    }

    public void setTooltip(Component tooltip) {
        this.tooltip = tooltip;
    }
}