package net.xiaoyang010.ex_enigmaticlegacy.Client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Container.StarlitSanctumMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.StarlitSanctumTile;

import java.util.ArrayList;
import java.util.List;

public class StarlitSanctumScreen extends AbstractContainerScreen<StarlitSanctumMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ex_enigmaticlegacy:textures/gui/container/starlit.png");
    private static final int TEXTURE_WIDTH = 1024;
    private static final int TEXTURE_HEIGHT = 1024;
    private static final int GUI_WIDTH = 540;
    private static final int GUI_HEIGHT = 501;
    private static final int BAR_TEX_X_EMPTY = 540;
    private static final int BAR_TEX_X_FULL = 561;
    private static final int BAR_TEX_Y = 15;
    private static final int BAR_WIDTH = 12;
    private static final int BAR_HEIGHT = 400;
    private static final int BAR_POS_X = 550;
    private static final int BAR_POS_Y = 10;
    private static final int PROGRESS_BAR_X = 245;
    private static final int PROGRESS_BAR_Y = 55;
    private static final int PROGRESS_BAR_WIDTH = 30;
    private static final int PROGRESS_BAR_HEIGHT = 4;

    private float getScale() {
        if (ConfigHandler.starlitGuiScaleConfig != null) {
            return ConfigHandler.starlitGuiScaleConfig.get().floatValue();
        }
        return 0.7f;
    }

    private int getXOffset() {
        return (int) ((this.width - GUI_WIDTH * getScale()) / 2);
    }

    private int getYOffset() {
        return (int) ((this.height - GUI_HEIGHT * getScale()) / 2);
    }

    public StarlitSanctumScreen(StarlitSanctumMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = 0;
        this.topPos = 0;
    }

    @Override
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);

        int logicMouseX = (int) ((mouseX - getXOffset()) / getScale());
        int logicMouseY = (int) ((mouseY - getYOffset()) / getScale());

        ms.pushPose();
        ms.translate(getXOffset(), getYOffset(), 0);
        ms.scale(getScale(), getScale(), 1.0f);

        this.renderBg(ms, partialTicks, logicMouseX, logicMouseY);
        this.renderLabels(ms, logicMouseX, logicMouseY);

        ms.popPose();

        this.hoveredSlot = null;
        for (int i = 0; i < this.menu.slots.size(); ++i) {
            Slot slot = this.menu.slots.get(i);

            if (slot.isActive()) {
                RenderSlot(ms, slot);
                if (isHovering(slot, logicMouseX, logicMouseY)) {
                    this.hoveredSlot = slot;
                    ms.pushPose();
                    ms.translate(getXOffset(), getYOffset(), 0);
                    ms.scale(getScale(), getScale(), 1.0f);
                    RenderSlotHighlight(ms, slot.x, slot.y, 0);
                    ms.popPose();
                }
            }
        }

        ItemStack carried = this.menu.getCarried();
        if (!carried.isEmpty()) {
            RenderFloatingItem(carried, mouseX, mouseY, null);
        }

        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            this.renderTooltip(ms, this.hoveredSlot.getItem(), mouseX, mouseY);
        }

        if (isHovering(BAR_POS_X, BAR_POS_Y, BAR_WIDTH, BAR_HEIGHT, logicMouseX, logicMouseY)) {
            List<Component> tooltip = new ArrayList<>();
            long currentMana = this.menu.getManaLong();
            long maxMana = this.menu.getMaxManaLong();

            String currentStr = StarlitSanctumTile.formatMana(currentMana);
            String maxStr = StarlitSanctumTile.formatMana(maxMana);

            tooltip.add(new TranslatableComponent("gui.ex_enigmaticlegacy.starlit_sanctum.mana",
                    "§b" + currentStr, "§b" + maxStr));
            if (maxMana > 0) {
                double percent = (double) currentMana / maxMana * 100.0;
                tooltip.add(new TranslatableComponent("gui.ex_enigmaticlegacy.starlit_sanctum.mana_percent",
                        String.format("§7%.2f", percent)));
            }
            this.renderComponentTooltip(ms, tooltip, mouseX, mouseY);
        }
    }

    private void RenderSlot(PoseStack ms, Slot slot) {
        ItemStack itemstack = slot.getItem();
        String countString = null;
        ItemStack carried = this.menu.getCarried();

        if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !carried.isEmpty()) {
            if (this.quickCraftSlots.size() > 1 && AbstractContainerMenu.canItemQuickReplace(slot, carried, true) && this.menu.canDragTo(slot)) {
                ItemStack fakeStack = carried.copy();
                int existingCount = slot.getItem().isEmpty() ? 0 : slot.getItem().getCount();
                AbstractContainerMenu.getQuickCraftSlotCount(this.quickCraftSlots, this.quickCraftingType, fakeStack, existingCount);

                int limit = Math.min(fakeStack.getMaxStackSize(), slot.getMaxStackSize(fakeStack));
                if (fakeStack.getCount() > limit) {
                    countString = net.minecraft.ChatFormatting.YELLOW.toString() + limit;
                    fakeStack.setCount(limit);
                }

                itemstack = fakeStack;
            }
        }

        if (itemstack.isEmpty()) return;

        float itemScreenX = getXOffset() + (slot.x * getScale());
        float itemScreenY = getYOffset() + (slot.y * getScale());

        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.translate(itemScreenX, itemScreenY, 0);
        modelViewStack.scale(getScale(), getScale(), 1.0F);
        RenderSystem.applyModelViewMatrix();

        this.itemRenderer.renderAndDecorateItem(itemstack, 0, 0);
        this.itemRenderer.renderGuiItemDecorations(this.font, itemstack, 0, 0, countString);

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private void RenderFloatingItem(ItemStack stack, int mouseX, int mouseY, String text) {
        this.setBlitOffset(200);
        this.itemRenderer.blitOffset = 200.0F;

        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.translate(mouseX, mouseY, 0);
        modelViewStack.scale(getScale(), getScale(), 1.0F);
        RenderSystem.applyModelViewMatrix();

        this.itemRenderer.renderAndDecorateItem(stack, -8, -8);
        this.itemRenderer.renderGuiItemDecorations(this.font, stack, -8, -8, text);

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();

        this.setBlitOffset(0);
        this.itemRenderer.blitOffset = 0.0F;
    }

    public static void RenderSlotHighlight(PoseStack ms, int x, int y, int z) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        fill(ms, x, y, x + 16, y + 16, -2130706433);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

    @Override
    protected void renderBg(PoseStack ms, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        blit(ms, 0, 0, 0, 0, GUI_WIDTH, GUI_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        blit(ms, BAR_POS_X, BAR_POS_Y,
                BAR_TEX_X_EMPTY, BAR_TEX_Y,
                BAR_WIDTH, BAR_HEIGHT,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);

        long currentMana = this.menu.getManaLong();
        long maxMana = this.menu.getMaxManaLong();

        int fillHeight = 0;
        if (maxMana > 0) {
            double ratio = (double) currentMana / (double) maxMana;
            fillHeight = (int) (ratio * BAR_HEIGHT);

            fillHeight = Math.max(0, Math.min(fillHeight, BAR_HEIGHT));
        }

        if (fillHeight > 0) {
            int x = BAR_POS_X;
            int y = BAR_POS_Y + (BAR_HEIGHT - fillHeight);
            int v = BAR_TEX_Y + (BAR_HEIGHT - fillHeight);

            blit(ms, x, y,
                    BAR_TEX_X_FULL, v,
                    BAR_WIDTH, fillHeight,
                    TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        int progress = this.menu.getCraftingProgress();
        if (progress > 0) {
            fill(ms, PROGRESS_BAR_X - 1, PROGRESS_BAR_Y - 1,
                    PROGRESS_BAR_X + PROGRESS_BAR_WIDTH + 1, PROGRESS_BAR_Y + PROGRESS_BAR_HEIGHT + 1,
                    0xFF000000);

            fill(ms, PROGRESS_BAR_X, PROGRESS_BAR_Y,
                    PROGRESS_BAR_X + PROGRESS_BAR_WIDTH, PROGRESS_BAR_Y + PROGRESS_BAR_HEIGHT,
                    0xFF3C3C3C);

            int filledWidth = (PROGRESS_BAR_WIDTH * progress) / 100;
            if (filledWidth > 0) {
                int color1 = 0xFF00FF00;
                int color2 = 0xFF00CC00;
                fillGradient(ms, PROGRESS_BAR_X, PROGRESS_BAR_Y,
                        PROGRESS_BAR_X + filledWidth, PROGRESS_BAR_Y + PROGRESS_BAR_HEIGHT,
                        color1, color2);
            }

            String progressText = progress + "%";
            int textX = PROGRESS_BAR_X + (PROGRESS_BAR_WIDTH - this.font.width(progressText)) / 2;
            int textY = PROGRESS_BAR_Y + PROGRESS_BAR_HEIGHT + 2;
            this.font.draw(ms, progressText, textX, textY, 0xFFFFFFFF);
        }
    }

    private boolean isHovering(Slot slot, double mouseX, double mouseY) {
        return this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY);
    }

    private boolean isHovering(int rectX, int rectY, int rectW, int rectH, int mouseX, int mouseY) {
        return mouseX >= rectX && mouseX < rectX + rectW && mouseY >= rectY && mouseY < rectY + rectH;
    }

    private double getLogicX(double mouseX) {
        return (mouseX - getXOffset()) / getScale();
    }

    private double getLogicY(double mouseY) {
        return (mouseY - getYOffset()) / getScale();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(getLogicX(mouseX), getLogicY(mouseY), button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(getLogicX(mouseX), getLogicY(mouseY), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(getLogicX(mouseX), getLogicY(mouseY), button, dragX / getScale(), dragY / getScale());
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(getLogicX(mouseX), getLogicY(mouseY));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(getLogicX(mouseX), getLogicY(mouseY), delta);
    }

    @Override
    public void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
    }
}