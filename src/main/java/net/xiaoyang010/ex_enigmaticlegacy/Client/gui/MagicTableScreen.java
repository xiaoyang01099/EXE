package net.xiaoyang010.ex_enigmaticlegacy.Client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.xiaoyang010.ex_enigmaticlegacy.Container.MagicTableMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;

public class MagicTableScreen extends AbstractContainerScreen<MagicTableMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ex_enigmaticlegacy", "textures/gui/container/magic_table_gui.png");

    // GUI尺寸常量
    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 256;

    // 箭头相关常量
    private static final int ARROW_X = 102;
    private static final int ARROW_Y = 83;
    private static final int ARROW_WIDTH = 22;
    private static final int ARROW_HEIGHT = 15;
    private static final int ARROW_TEXTURE_X = 256;
    private static final int ARROW_TEXTURE_Y = 0;

    public MagicTableScreen(MagicTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        // 设置GUI尺寸
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;

        // 设置玩家背包标签位置（如果需要显示）
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        // 渲染背景（变暗效果）
        this.renderBackground(poseStack);

        // 调用父类渲染方法
        super.render(poseStack, mouseX, mouseY, partialTick);

        // 渲染物品提示框
        this.renderTooltip(poseStack, mouseX, mouseY);

        // 渲染箭头区域的工具提示
        renderArrowTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        // 设置渲染系统
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        // 计算GUI左上角位置
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 渲染主要背景纹理
        this.blit(poseStack, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // 渲染箭头（假设箭头在纹理的右侧区域）
        renderArrow(poseStack, x, y, partialTick);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // 渲染标题文字
        this.font.draw(poseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 0x404040);

        // 渲染玩家背包标签（如果需要）
        this.font.draw(poseStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 0x404040);

        // 可以添加其他文字标签，比如：
        // this.font.draw(poseStack, Component.translatable("gui.magic_table.input"), 78, 68, 0x404040);
        // this.font.draw(poseStack, Component.translatable("gui.magic_table.output"), 160, 68, 0x404040);
    }

    @Override
    protected void init() {
        super.init();

        // 可以在这里添加按钮或其他GUI组件
        // 例如：
        // this.addRenderableWidget(new Button(...));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 处理鼠标点击事件
        // 可以添加自定义的点击逻辑

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 处理键盘按键事件
        // 可以添加快捷键逻辑

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void containerTick() {
        // 每tick调用一次，可以用于更新GUI状态
        super.containerTick();

        // 可以在这里添加动画或状态更新逻辑
    }

    // 渲染箭头工具提示
    private void renderArrowTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        // 检查鼠标是否悬停在箭头上
        if (isHovering(ARROW_X, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT, mouseX, mouseY)) {
            boolean hasInput = !this.menu.getSlot(0).getItem().isEmpty();

            if (hasInput) {
                // 显示合成进度或状态信息
                this.renderTooltip(poseStack, EComponent.translatable("gui.magic_table.crafting"), mouseX, mouseY);
            } else {
                // 显示需要材料的提示
                this.renderTooltip(poseStack, EComponent.translatable("gui.magic_table.need_materials"), mouseX, mouseY);
            }
        }
    }

    // 渲染箭头方法
    private void renderArrow(PoseStack poseStack, int guiLeft, int guiTop, float partialTick) {
        // 箭头位置
        int arrowX = guiLeft + ARROW_X;
        int arrowY = guiTop + ARROW_Y;

        // 检查是否有合成进度或物品
        boolean hasInput = !this.menu.getSlot(0).getItem().isEmpty(); // 输入槽
        boolean canCraft = hasInput; // 可以根据实际逻辑调整

        if (canCraft) {
            // 渲染动态箭头
            long time = System.currentTimeMillis();
            int animationFrame = (int)((time / 150) % 3); // 每150ms切换一帧，3帧循环

            // 渲染箭头背景（空箭头）
            this.blit(poseStack, arrowX, arrowY, ARROW_TEXTURE_X, ARROW_TEXTURE_Y, ARROW_WIDTH, ARROW_HEIGHT);

            // 渲染箭头进度动画
            // 假设纹理布局：y=0是空箭头，y=15,30,45是动画帧
            this.blit(poseStack, arrowX, arrowY, ARROW_TEXTURE_X, ARROW_TEXTURE_Y + 15 + (animationFrame * 15), ARROW_WIDTH, ARROW_HEIGHT);
        } else {
            // 渲染静态空箭头
            this.blit(poseStack, arrowX, arrowY, ARROW_TEXTURE_X, ARROW_TEXTURE_Y, ARROW_WIDTH, ARROW_HEIGHT);
        }
    }

    // 辅助方法：检查鼠标是否在指定区域内
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        int leftX = this.leftPos + x;
        int topY = this.topPos + y;
        return mouseX >= leftX && mouseX < leftX + width && mouseY >= topY && mouseY < topY + height;
    }

    // 辅助方法：渲染进度条（如果需要）
    protected void renderProgressBar(PoseStack poseStack, int x, int y, int progress, int maxProgress) {
        if (maxProgress > 0) {
            int progressWidth = progress * 24 / maxProgress;
            this.blit(poseStack, x, y, 176, 14, progressWidth + 1, 16);
        }
    }
}