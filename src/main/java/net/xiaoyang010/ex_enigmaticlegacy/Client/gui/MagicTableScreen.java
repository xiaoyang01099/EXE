package net.xiaoyang010.ex_enigmaticlegacy.Client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Container.MagicTableMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.MagicTableConvertPacket;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.MagicTableCustomAmountPacket;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.MagicTableGearPacket;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileMagicTable;

import java.util.ArrayList;
import java.util.List;

public class MagicTableScreen extends AbstractContainerScreen<MagicTableMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ex_enigmaticlegacy", "textures/gui/container/magic_table_gui.png");
    private Button convertButton;
    private Button gearLeftButton;
    private Button gearRightButton;
    private EditBox customAmountField;

    public MagicTableScreen(MagicTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 234;
    }

    @Override
    protected void init() {
        super.init();

        this.inventoryLabelY = this.imageHeight - 94;
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.gearLeftButton = new Button(
                x + 8, y + 110, 15, 15,
                new TextComponent("<"),
                btn -> NetworkHandler.CHANNEL.sendToServer(
                        new MagicTableGearPacket(menu.getBlockEntity().getBlockPos(), false))
        );
        this.addRenderableWidget(gearLeftButton);

        this.gearRightButton = new Button(
                x + 55, y + 110, 15, 15,
                new TextComponent(">"),
                btn -> NetworkHandler.CHANNEL.sendToServer(
                        new MagicTableGearPacket(menu.getBlockEntity().getBlockPos(), true))
        );
        this.addRenderableWidget(gearRightButton);

        this.convertButton = new Button(
                x + 8, y + 128, 62, 18,
                new TranslatableComponent("gui.ex_enigmaticlegacy.magic_table.button.convert"),
                btn -> NetworkHandler.CHANNEL.sendToServer(
                        new MagicTableConvertPacket(menu.getBlockEntity().getBlockPos()))
        );
        this.addRenderableWidget(convertButton);

        this.customAmountField = new EditBox(
                this.font, x + 8, y + 56, 60, 14,
                new TranslatableComponent("gui.ex_enigmaticlegacy.magic_table.field.amount"));
        this.customAmountField.setMaxLength(19);
        this.customAmountField.setValue("1");
        this.customAmountField.setVisible(false);
        this.customAmountField.setResponder(text -> {
            try {
                long amount = Long.parseLong(text);
                if (amount > 0) {
                    NetworkHandler.CHANNEL.sendToServer(
                            new MagicTableCustomAmountPacket(
                                    menu.getBlockEntity().getBlockPos(), amount));
                }
            } catch (NumberFormatException ignored) {}
        });
        this.addRenderableWidget(customAmountField);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        this.blit(poseStack, x, y, 0, 0, this.imageWidth, this.imageHeight);
        int progressScaled = this.menu.getProgressScaled();
        this.blit(poseStack, x + 108, y + 85, 0, 234, progressScaled, 15);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, this.title, this.titleLabelX, this.titleLabelY, 0x404040);

        int infoX = 8;

        Component emcText = new TranslatableComponent("gui.ex_enigmaticlegacy.magic_table.label.emc",
                menu.getFormattedPlayerEmc());
        this.font.draw(poseStack, emcText, infoX, 8, 0xFFFFFF);

        if (menu.syncedOutputCount > 0) {
            Component countText = new TranslatableComponent("gui.ex_enigmaticlegacy.magic_table.label.output_count",
                    menu.getFormattedOutputCount());
            this.font.draw(poseStack, countText, infoX, 20, 0xFFFFFF);
        }

        if (menu.syncedInputCount > 0) {
            Component inputCountText = new TranslatableComponent("gui.ex_enigmaticlegacy.magic_table.label.input_count",
                    menu.getFormattedInputCount());
            this.font.draw(poseStack, inputCountText, infoX, 32, 0xFFFFFF);
        }

        if (menu.getBlockEntity() != null && menu.getBlockEntity().getCurrentRecipe() != null) {
            Component costText = new TranslatableComponent("gui.ex_enigmaticlegacy.magic_table.label.cost",
                    MagicTableMenu.formatBigInteger(menu.getBlockEntity().getCurrentRecipe().getEmcCost()));
            this.font.draw(poseStack, costText, infoX, 44, 0xFFFFFF);
        }

        Component gearText = new TranslatableComponent("gui.ex_enigmaticlegacy.magic_table.label.gear",
                TileMagicTable.getGearName(menu.convertGear));
        this.font.draw(poseStack, gearText, 23, 100, 0xFFFFFF);

        if (menu.syncedOutputCount > 0 && menu.syncedItemEmc > 0) {
            Component previewText = new TranslatableComponent("gui.ex_enigmaticlegacy.magic_table.label.preview",
                    menu.getFormattedConvertEmc());
            this.font.draw(poseStack, previewText, infoX, 56, 0xFFFFFF);
        }

        if (menu.syncedItemEmc > 0) {
            Component itemEmcText = new TranslatableComponent("gui.ex_enigmaticlegacy.magic_table.label.unit_price",
                    MagicTableMenu.formatLargeNumber(menu.syncedItemEmc));
            this.font.draw(poseStack, itemEmcText, infoX, 68, 0xFFFFFF);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderVirtualSlots(poseStack, mouseX, mouseY);
        this.renderTooltip(poseStack, mouseX, mouseY);
        boolean isCustomGear = (menu.convertGear == 5);
        if (customAmountField != null) {
            customAmountField.setVisible(isCustomGear);
        }

        if (convertButton != null && convertButton.isHoveredOrFocused()) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(new TranslatableComponent("gui.ex_enigmaticlegacy.magic_table.tooltip.convert_desc"));
            tooltip.add(new TranslatableComponent("gui.ex_enigmaticlegacy.magic_table.tooltip.current_gear",
                    TileMagicTable.getGearName(menu.convertGear)));
            if (menu.syncedOutputCount > 0 && menu.syncedItemEmc > 0) {
                tooltip.add(new TranslatableComponent("gui.ex_enigmaticlegacy.magic_table.tooltip.expected_emc",
                        menu.getFormattedConvertEmc()));
            }
            if (menu.syncedItemEmc <= 0) {
                tooltip.add(new TranslatableComponent("gui.ex_enigmaticlegacy.magic_table.tooltip.no_emc_value"));
            }
            this.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
        }
    }

    private void renderVirtualSlots(PoseStack poseStack, int mouseX, int mouseY) {
        renderVirtualSlotItem(poseStack, MagicTableMenu.SLOT_INPUT, menu.syncedInputCount);
        renderVirtualSlotItem(poseStack, MagicTableMenu.SLOT_OUTPUT, menu.syncedOutputCount);
    }

    private void renderVirtualSlotItem(PoseStack poseStack, int slotIndex, long realCount) {
        if (realCount <= 0) return;

        Slot slot = this.menu.slots.get(slotIndex);
        ItemStack displayStack = slot.getItem();
        if (displayStack.isEmpty()) return;

        int slotX = this.leftPos + slot.x;
        int slotY = this.topPos + slot.y;

        this.setBlitOffset(100);
        this.itemRenderer.blitOffset = 100.0F;

        RenderSystem.enableDepthTest();
        this.itemRenderer.renderAndDecorateItem(displayStack, slotX, slotY);

        String countText;
        if (realCount <= 1) {
            countText = null;
        } else if (realCount <= 999) {
            countText = String.valueOf(realCount);
        } else {
            countText = MagicTableMenu.formatLargeNumber(realCount);
        }

        if (countText != null) {
            poseStack.pushPose();
            poseStack.translate(0, 0, 200);

            int textWidth = this.font.width(countText);
            int textX = slotX + 17 - textWidth;
            int textY = slotY + 9;

            this.font.draw(poseStack, countText, textX + 1, textY, 0x000000);
            this.font.draw(poseStack, countText, textX - 1, textY, 0x000000);
            this.font.draw(poseStack, countText, textX, textY + 1, 0x000000);
            this.font.draw(poseStack, countText, textX, textY - 1, 0x000000);
            this.font.draw(poseStack, countText, textX, textY, 0xFFFFFF);

            poseStack.popPose();
        }

        this.itemRenderer.blitOffset = 0.0F;
        this.setBlitOffset(0);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (customAmountField != null && customAmountField.isFocused()) {
            if (keyCode == 256) {
                customAmountField.setFocus(false);
                return true;
            }
            return customAmountField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (customAmountField != null && customAmountField.isFocused()) {
            return customAmountField.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }
}