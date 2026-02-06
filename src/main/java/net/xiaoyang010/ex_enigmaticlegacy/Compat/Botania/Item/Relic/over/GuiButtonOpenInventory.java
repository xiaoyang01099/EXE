package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;

@OnlyIn(Dist.CLIENT)
public class GuiButtonOpenInventory extends Button {
    public static final int WIDTH = 28;
    public static final int HEIGHT = 30;
    private static final ResourceLocation BUTTON_TEXTURE =
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/gui/overflow/tab_button.png");
    private static final ResourceLocation BUTTON_DARK_TEXTURE =
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/gui/overflow/tab_button_dark.png");
    private boolean isHovered = false;

    public GuiButtonOpenInventory(int x, int y) {
        super(x, y, WIDTH, HEIGHT, Component.nullToEmpty(""), GuiButtonOpenInventory::onPress);
    }

    /**
     * 按钮点击事件
     */
    private static void onPress(Button button) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!isRingEquipped(mc.player)) {
            mc.player.displayClientMessage(
                    EComponent.translatable("message.powerinventory.no_ring"),
                    true
            );
            return;
        }

        NetworkHandler.CHANNEL.sendToServer(new OpenInventoryPacket());
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;

        this.isHovered = this.isMouseOver(mouseX, mouseY);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        ResourceLocation texture = this.isHovered ? BUTTON_TEXTURE : BUTTON_DARK_TEXTURE;
        RenderSystem.setShaderTexture(0, texture);

        blit(poseStack, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
    }

    @Override
    public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
        if (this.isHovered && this.visible) {
            Minecraft mc = Minecraft.getInstance();
            mc.screen.renderTooltip(poseStack,
                    EComponent.translatable("tooltip.powerinventory.open"),
                    mouseX, mouseY);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible && this.active &&
                mouseX >= this.x && mouseY >= this.y &&
                mouseX < this.x + this.width && mouseY < this.y + this.height;
    }

    public static boolean isRingEquipped(Player player) {
        return ItemPowerRing.isRingEquipped(player);
    }
}