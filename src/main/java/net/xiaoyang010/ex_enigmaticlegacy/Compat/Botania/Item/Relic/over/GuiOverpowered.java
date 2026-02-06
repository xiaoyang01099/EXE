package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

public class GuiOverpowered extends AbstractContainerScreen<ContainerOverpowered> {
    private static final ResourceLocation BKG = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/gui/overflow/inventory.png");
    private static final ResourceLocation BKG_LARGE = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/gui/overflow/inventory_large.png");
    private static final ResourceLocation BKG_3X9 = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/gui/overflow/slots3x9.png");
    private static final ResourceLocation SLOT_TEXTURE = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/gui/overflow/inventory_slot.png");
    private final Player player;

    public GuiOverpowered(ContainerOverpowered container, Inventory inv, Component title) {
        super(container, inv, title);
        this.player = inv.player;
        this.imageWidth = ConfigHandler.PowerInventoryConfig.getWidth();
        this.imageHeight = ConfigHandler.PowerInventoryConfig.getHeight();
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        Minecraft mc = this.minecraft;
        if (mc == null) return;
        int padding = 6;

        // 末影珍珠解锁按钮
        if (!PlayerUnlockData.isEPearlUnlocked(player)) {
            this.addRenderableWidget(new GuiButtonUnlockPearl(
                    this.leftPos + padding,
                    this.topPos + padding,
                    player,
                    ConfigHandler.EXP_COST_PEARL.get()
            ));
        }

        // 排序/过滤/转储按钮
        if (ConfigHandler.MAX_SECTIONS.get() > 1) {
            int wid = 50;
            int localStart = leftPos + 70 + 2 * padding;

            this.addRenderableWidget(new GuiButtonSort(
                    localStart,
                    topPos + padding,
                    wid
            ));

            this.addRenderableWidget(new GuiButtonFilter(
                    localStart + wid + padding,
                    topPos + padding,
                    wid
            ));

            this.addRenderableWidget(new GuiButtonDump(
                    localStart + 2 * (wid + padding),
                    topPos + padding,
                    wid
            ));
        }

        // 末影箱解锁按钮
        if (!PlayerUnlockData.isEChestUnlocked(player)) {
            this.addRenderableWidget(new GuiButtonUnlockChest(
                    leftPos + ConfigHandler.INVO_WIDTH.get() - padding - GuiButtonUnlockChest.WIDTH,
                    topPos + padding,
                    player,
                    ConfigHandler.EXP_COST_ECHEST.get()
            ));
        }

        // 存储区域解锁按钮
        int expCost = ConfigHandler.EXP_COST_STORAGE_START.get();
        for (int i = 1; i <= ConfigHandler.MAX_SECTIONS.get(); i++) {
            if (!PlayerUnlockData.hasStorage(player, i)) {
                this.addRenderableWidget(new GuiButtonUnlockStorage(
                        leftPos + InventoryRenderer.xPosBtn(i),
                        topPos + InventoryRenderer.yPosBtn(i),
                        player,
                        expCost,
                        i
                ));
                break;
            }
            expCost += ConfigHandler.EXP_COST_STORAGE_INC.get();
        }

        // 旋转/交换按钮
        int w = 6, h = 8;
        for (int i = 1; i <= ConfigHandler.MAX_SECTIONS.get(); i++) {
            if (PlayerUnlockData.hasStorage(player, i)) {
                this.addRenderableWidget(new GuiButtonRotate(
                        leftPos + InventoryRenderer.xPosSwap(i),
                        topPos + InventoryRenderer.yPosSwap(i),
                        w, h, i
                ));
            }
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        ResourceLocation background = ConfigHandler.PowerInventoryConfig.isLargeScreen()
                ? BKG_LARGE : BKG;
        RenderSystem.setShaderTexture(0, background);

        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight,
                imageWidth, imageHeight);

        for (int i = 1; i <= ConfigHandler.PowerInventoryConfig.getMaxSections(); i++) {
            if (PlayerUnlockData.hasStorage(player, i)) {
                drawSlotSectionAt(poseStack,
                        leftPos + InventoryRenderer.xPosTexture(i),
                        topPos + InventoryRenderer.yPosTexture(i));
            }
        }

        if (PlayerUnlockData.isEChestUnlocked(player)) {
            drawSlotAt(poseStack, SlotEnderChest.posX, SlotEnderChest.posY);
        }

        if (PlayerUnlockData.isEPearlUnlocked(player)) {
            drawSlotAt(poseStack, SlotEnderPearl.posX, SlotEnderPearl.posY);
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        if (PlayerUnlockData.isEPearlUnlocked(player) &&
                menu.customInventory.enderPearlStack.isEmpty()) {

            RenderSystem.setShaderTexture(0, SlotEnderPearl.background);
            blit(poseStack, SlotEnderPearl.posX, SlotEnderPearl.posY,
                    0, 0, 16, 16, 16, 16);
        }

        if (PlayerUnlockData.isEChestUnlocked(player) &&
                menu.customInventory.enderChestStack.isEmpty()) {

            RenderSystem.setShaderTexture(0, SlotEnderChest.background);
            blit(poseStack, SlotEnderChest.posX, SlotEnderChest.posY,
                    0, 0, 16, 16, 16, 16);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);

        this.renderables.forEach(widget -> {
            if (widget instanceof GuiButtonUnlockStorage btn) {
                if (btn.isMouseOver(mouseX, mouseY)) {
                    this.renderTooltip(poseStack, btn.getTooltip(), mouseX, mouseY);
                }
            } else if (widget instanceof GuiButtonUnlockPearl btn) {
                if (btn.isMouseOver(mouseX, mouseY)) {
                    this.renderTooltip(poseStack, btn.getTooltip(), mouseX, mouseY);
                }
            } else if (widget instanceof GuiButtonUnlockChest btn) {
                if (btn.isMouseOver(mouseX, mouseY)) {
                    this.renderTooltip(poseStack, btn.getTooltip(), mouseX, mouseY);
                }
            }
        });
    }

    private void drawSlotSectionAt(PoseStack poseStack, int x, int y) {
        RenderSystem.setShaderTexture(0, BKG_3X9);
        blit(poseStack, x, y, 0, 0, Const.SLOTS_WIDTH, Const.SLOTS_HEIGHT,
                Const.SLOTS_WIDTH, Const.SLOTS_HEIGHT);
    }

    private void drawSlotAt(PoseStack poseStack, int x, int y) {
        RenderSystem.setShaderTexture(0, SLOT_TEXTURE);
        blit(poseStack, leftPos + x - 1, topPos + y - 1, 0, 0, Const.SQ, Const.SQ,
                Const.SQ, Const.SQ);
    }
}