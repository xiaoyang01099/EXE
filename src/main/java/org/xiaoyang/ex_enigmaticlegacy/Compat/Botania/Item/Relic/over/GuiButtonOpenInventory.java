package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

@OnlyIn(Dist.CLIENT)
public class GuiButtonOpenInventory extends Button {
    public static final int WIDTH = 28;
    public static final int HEIGHT = 30;
    private static final ResourceLocation BUTTON_TEXTURE =
            new ResourceLocation(Exe.MODID, "textures/gui/overflow/tab_button.png");
    private static final ResourceLocation BUTTON_DARK_TEXTURE =
            new ResourceLocation(Exe.MODID, "textures/gui/overflow/tab_button_dark.png");

    public GuiButtonOpenInventory(int x, int y) {
        super(Button.builder(
                Component.empty(),
                GuiButtonOpenInventory::onPress
        ).pos(x, y).size(WIDTH, HEIGHT));
        this.setTooltip(Tooltip.create(
                Component.translatable("tooltip.powerinventory.open")
        ));
    }

    private static void onPress(Button button) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!ItemPowerRing.isRingEquipped(mc.player)) {
            mc.player.displayClientMessage(
                    Component.translatable("message.powerinventory.no_ring"),
                    true
            );
            return;
        }

        NetworkHandler.CHANNEL.sendToServer(new OpenInventoryPacket());
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;

        ResourceLocation texture = this.isHovered() ? BUTTON_TEXTURE : BUTTON_DARK_TEXTURE;
        guiGraphics.blit(texture, this.getX(), this.getY(), 0, 0,
                this.width, this.height, this.width, this.height);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible && this.active
                && mouseX >= this.getX() && mouseY >= this.getY()
                && mouseX < this.getX() + this.width
                && mouseY < this.getY() + this.height;
    }

    public static boolean isRingEquipped(Player player) {
        return ItemPowerRing.isRingEquipped(player);
    }
}