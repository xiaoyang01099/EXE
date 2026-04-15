package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.xiaoyang.ex_enigmaticlegacy.ConfigHandler;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.List;

public class GuiOverpowered extends AbstractContainerScreen<ContainerOverpowered> {
    private static final ResourceLocation BKG = new ResourceLocation(Exe.MODID, "textures/gui/overflow/inventory.png");
    private static final ResourceLocation BKG_LARGE = new ResourceLocation(Exe.MODID, "textures/gui/overflow/inventory_large.png");
    private static final ResourceLocation BKG_3X9 = new ResourceLocation(Exe.MODID, "textures/gui/overflow/slots3x9.png");
    private static final ResourceLocation SLOT_TEXTURE = new ResourceLocation(Exe.MODID, "textures/gui/overflow/inventory_slot.png");
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

        if (!PlayerUnlockData.isEPearlUnlocked(player)) {
            this.addRenderableWidget(new GuiButtonUnlockPearl(
                    this.leftPos + padding,
                    this.topPos + padding,
                    player,
                    ConfigHandler.EXP_COST_PEARL.get()
            ));
        }

        if (ConfigHandler.MAX_SECTIONS.get() > 1) {
            int wid = 50;
            int localStart = leftPos + 70 + 2 * padding;

            this.addRenderableWidget(new GuiButtonSort(localStart, topPos + padding, wid));
            this.addRenderableWidget(new GuiButtonFilter(localStart + wid + padding, topPos + padding, wid));
            this.addRenderableWidget(new GuiButtonDump(localStart + 2 * (wid + padding), topPos + padding, wid));
        }

        if (!PlayerUnlockData.isEChestUnlocked(player)) {
            this.addRenderableWidget(new GuiButtonUnlockChest(
                    leftPos + ConfigHandler.INVO_WIDTH.get() - padding - GuiButtonUnlockChest.WIDTH,
                    topPos + padding,
                    player,
                    ConfigHandler.EXP_COST_ECHEST.get()
            ));
        }

        int expCost = ConfigHandler.EXP_COST_STORAGE_START.get();
        for (int i = 1; i <= ConfigHandler.MAX_SECTIONS.get(); i++) {
            if (!PlayerUnlockData.hasStorage(player, i)) {
                this.addRenderableWidget(new GuiButtonUnlockStorage(
                        leftPos + InventoryRenderer.xPosBtn(i),
                        topPos + InventoryRenderer.yPosBtn(i),
                        player, expCost, i
                ));
                break;
            }
            expCost += ConfigHandler.EXP_COST_STORAGE_INC.get();
        }

        for (int i = 1; i <= ConfigHandler.MAX_SECTIONS.get(); i++) {
            if (PlayerUnlockData.hasStorage(player, i)) {
                this.addRenderableWidget(new GuiButtonRotate(
                        leftPos + InventoryRenderer.xPosSwap(i),
                        topPos + InventoryRenderer.yPosSwap(i),
                        6, 8, i
                ));
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ResourceLocation background = ConfigHandler.PowerInventoryConfig.isLargeScreen()
                ? BKG_LARGE : BKG;

        guiGraphics.blit(background, leftPos, topPos, 0, 0,
                imageWidth, imageHeight,
                imageWidth, imageHeight);
        for (int i = 1; i <= ConfigHandler.PowerInventoryConfig.getMaxSections(); i++) {
            if (PlayerUnlockData.hasStorage(player, i)) {
                drawSlotSectionAt(guiGraphics,
                        leftPos + InventoryRenderer.xPosTexture(i),
                        topPos + InventoryRenderer.yPosTexture(i));
            }
        }
        if (PlayerUnlockData.isEChestUnlocked(player)) {
            drawSlotAt(guiGraphics, SlotEnderChest.posX, SlotEnderChest.posY);
        }
        if (PlayerUnlockData.isEPearlUnlocked(player)) {
            drawSlotAt(guiGraphics, SlotEnderPearl.posX, SlotEnderPearl.posY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (PlayerUnlockData.isEPearlUnlocked(player) &&
                menu.customInventory.enderPearlStack.isEmpty()) {
            guiGraphics.blit(SlotEnderPearl.background,
                    SlotEnderPearl.posX, SlotEnderPearl.posY,
                    0, 0, 16, 16);
        }

        if (PlayerUnlockData.isEChestUnlocked(player) &&
                menu.customInventory.enderChestStack.isEmpty()) {
            guiGraphics.blit(SlotEnderChest.background,
                    SlotEnderChest.posX, SlotEnderChest.posY,
                    0, 0, 16, 16);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void drawSlotSectionAt(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(BKG_3X9, x, y, 0, 0,
                Const.SLOTS_WIDTH, Const.SLOTS_HEIGHT,
                Const.SLOTS_WIDTH, Const.SLOTS_HEIGHT);
    }
    private void drawSlotAt(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(SLOT_TEXTURE, leftPos + x - 1, topPos + y - 1, 0, 0,
                Const.SQ, Const.SQ,
                Const.SQ, Const.SQ);
    }
}