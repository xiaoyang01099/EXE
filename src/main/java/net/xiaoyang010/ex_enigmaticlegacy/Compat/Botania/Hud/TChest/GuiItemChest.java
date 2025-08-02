// 更新到 Minecraft 1.18.2
package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud.TChest;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;

public class GuiItemChest extends AbstractContainerScreen<ContainerItemChest> {
    private static final ResourceLocation CHEST_GUI_TEXTURE =
            new ResourceLocation("textures/gui/container/generic_54.png");
    private int inventoryRows;

    public GuiItemChest(ContainerItemChest container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);

        int baseHeight = 222;
        int adjustment = baseHeight - 108;
        this.inventoryRows = container.itemChestInv.getContainerSize() / 9;
        this.imageHeight = adjustment + this.inventoryRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    // 在1.18.2中，热键处理方式不同，可能不需要这个方法
    // 如果需要禁用热键，可以重写 keyPressed 方法
    /*
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 禁用热键或自定义处理
        if (keyCode >= 49 && keyCode <= 57) { // 数字键1-9
            return false; // 不处理热键
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    */

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // 绘制容器标题
        this.font.draw(poseStack, this.title, 8.0F, 6.0F, 4210752);
        // 绘制玩家背包标题
        this.font.draw(poseStack, this.playerInventoryTitle, 8.0F, (float)this.inventoryLabelY, 4210752);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CHEST_GUI_TEXTURE);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 绘制容器上半部分（箱子格子）
        this.blit(poseStack, x, y, 0, 0, this.imageWidth, this.inventoryRows * 18 + 17);
        // 绘制容器下半部分（玩家背包）
        this.blit(poseStack, x, y + this.inventoryRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }
}