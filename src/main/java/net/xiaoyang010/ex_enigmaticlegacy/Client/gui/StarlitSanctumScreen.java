package net.xiaoyang010.ex_enigmaticlegacy.Client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Container.StarlitSanctumMenu;

import java.util.HashMap;

public class StarlitSanctumScreen extends AbstractContainerScreen<StarlitSanctumMenu> {
    private final static HashMap<String, Object> guistate = StarlitSanctumMenu.guistate;
    private final Level world;
    private final int x, y, z;
    private final Player entity;

    public StarlitSanctumScreen(StarlitSanctumMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;

        // 设置GUI尺寸
        // 玩家物品栏从Y=400开始，到大约Y=480结束
        this.imageWidth = 512;
        this.imageHeight = 490; // 足够容纳所有元素，不会太高
    }

    private static final ResourceLocation texture = new ResourceLocation("ex_enigmaticlegacy:textures/gui/container/starlit.png");

    @Override
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        super.render(ms, mouseX, mouseY, partialTicks);
        this.renderTooltip(ms, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack ms, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 绘制主背景 - 512x512的背景图片
        RenderSystem.setShaderTexture(0, texture);
        this.blit(ms, this.leftPos, this.topPos, 0, 0, 512, 512, 512, 512);

        // 绘制玩家物品栏背景
        drawPlayerInventoryBackground(ms);

        // 绘制主要网格区域的边框
        drawMainGridBackground(ms);

        // 绘制特殊槽位的背景
        drawSpecialSlotsBackground(ms);

        RenderSystem.disableBlend();
    }

    /**
     * 绘制玩家物品栏背景
     */
    private void drawPlayerInventoryBackground(PoseStack ms) {
        // 绘制玩家物品栏区域的背景
        int invStartX = this.leftPos + 175;
        int invStartY = this.topPos + 570;

        // 主物品栏背景 (3x9)
        fill(ms, invStartX - 4, invStartY - 4, invStartX + 162 + 4, invStartY + 54 + 4, 0x88000000);

        // 快速物品栏背景 (1x9)
        fill(ms, invStartX - 4, invStartY + 58 - 4, invStartX + 162 + 4, invStartY + 58 + 18 + 4, 0x88000000);
    }

    /**
     * 绘制主要网格区域的背景
     */
    private void drawMainGridBackground(PoseStack ms) {
        // 主要网格区域的背景
        int startX = this.leftPos + 31;
        int startY = this.topPos + 55;
        int gridWidth = 21 * 18;
        int gridHeight = 27 * 18;

        // 绘制网格边框
        fill(ms, startX - 2, startY - 2, startX + gridWidth + 2, startY + gridHeight + 2, 0x44FFFFFF);
    }

    /**
     * 绘制特殊槽位的背景
     */
    private void drawSpecialSlotsBackground(PoseStack ms) {
        // 左侧输入槽背景
        int leftInputX = this.leftPos + 65 - 1;
        int leftInputY = this.topPos + 18 - 1;
        fill(ms, leftInputX, leftInputY, leftInputX + 18, leftInputY + 18, 0x664A90E2);

        // 右侧输入槽背景
        int rightInputX = this.leftPos + 429 - 1;
        int rightInputY = this.topPos + 18 - 1;
        fill(ms, rightInputX, rightInputY, rightInputX + 18, rightInputY + 18, 0x664A90E2);

        // 中间输出槽背景
        int outputX = this.leftPos + 247 - 1;
        int outputY = this.topPos + 18 - 1;
        fill(ms, outputX, outputY, outputX + 18, outputY + 18, 0x6650E3C2);
    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, b, c);
    }

    @Override
    public void containerTick() {
        super.containerTick();
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // 绘制GUI标题
        drawString(poseStack, this.font, "Starlit Sanctum", 200, 20, 0xFFFFFF);

        // 绘制特殊槽位标签 - 调整到正确位置
        drawString(poseStack, this.font, "Input", 70, 20, 0x4A90E2);   // 左侧输入槽
        drawString(poseStack, this.font, "Output", 235, 20, 0x50E3C2); // 中间输出槽
        drawString(poseStack, this.font, "Input", 415, 20, 0x4A90E2);  // 右侧输入槽

        // 绘制物品栏标签
        drawString(poseStack, this.font, "Inventory", 175, 560, 0x404040);
    }

    @Override
    public void onClose() {
        super.onClose();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void init() {
        super.init();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);

        // 确保GUI居中显示
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int x, int y) {
        super.renderTooltip(poseStack, x, y);

        // 可以在这里添加自定义工具提示
        // 比如显示槽位信息、容量等
    }

    /**
     * 检查鼠标是否在特殊槽位上
     */
    private boolean isMouseOverSpecialSlot(double mouseX, double mouseY) {
        // 检查左侧输入槽
        if (isInRect(mouseX, mouseY, this.leftPos + 74, this.topPos + 35, 16, 16)) {
            return true;
        }
        // 检查右侧输入槽
        if (isInRect(mouseX, mouseY, this.leftPos + 420, this.topPos + 35, 16, 16)) {
            return true;
        }
        // 检查输出槽
        if (isInRect(mouseX, mouseY, this.leftPos + 247, this.topPos + 35, 16, 16)) {
            return true;
        }
        return false;
    }

    private boolean isInRect(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}