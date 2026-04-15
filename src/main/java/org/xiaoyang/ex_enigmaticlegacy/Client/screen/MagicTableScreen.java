package org.xiaoyang.ex_enigmaticlegacy.Client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.xiaoyang.ex_enigmaticlegacy.Container.MagicTableMenu;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.MagicTableConvertPacket;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.MagicTableCustomAmountPacket;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.MagicTableGearPacket;
import org.xiaoyang.ex_enigmaticlegacy.Tile.TileMagicTable;

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

        this.gearLeftButton = Button.builder(
                Component.literal("<"),
                btn -> NetworkHandler.CHANNEL.sendToServer(
                        new MagicTableGearPacket(menu.getBlockEntity().getBlockPos(), false))
        ).bounds(x + 8, y + 110, 15, 15).build();
        this.addRenderableWidget(gearLeftButton);

        this.gearRightButton = Button.builder(
                Component.literal(">"),
                btn -> NetworkHandler.CHANNEL.sendToServer(
                        new MagicTableGearPacket(menu.getBlockEntity().getBlockPos(), true))
        ).bounds(x + 55, y + 110, 15, 15).build();
        this.addRenderableWidget(gearRightButton);

        this.convertButton = Button.builder(
                Component.translatable("gui.ex_enigmaticlegacy.magic_table.button.convert"),
                btn -> NetworkHandler.CHANNEL.sendToServer(
                        new MagicTableConvertPacket(menu.getBlockEntity().getBlockPos()))
        ).bounds(x + 8, y + 128, 62, 18).build();
        this.addRenderableWidget(convertButton);

        this.customAmountField = new EditBox(
                this.font, x + 8, y + 56, 60, 14,
                Component.translatable("gui.ex_enigmaticlegacy.magic_table.field.amount"));
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
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        int progressScaled = this.menu.getProgressScaled();
        guiGraphics.blit(TEXTURE, x + 108, y + 85, 0, 234, progressScaled, 15);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, this.title, this.titleLabelX, this.titleLabelY, 0x404040);

        int infoX = 8;

        guiGraphics.drawString(font, Component.translatable("gui.ex_enigmaticlegacy.magic_table.label.emc",
                menu.getFormattedPlayerEmc()), infoX, 8, 0xFFFFFF);

        if (menu.syncedOutputCount > 0) {
            guiGraphics.drawString(font, Component.translatable("gui.ex_enigmaticlegacy.magic_table.label.output_count",
                    menu.getFormattedOutputCount()), infoX, 20, 0xFFFFFF);
        }

        if (menu.syncedInputCount > 0) {
            guiGraphics.drawString(font, Component.translatable("gui.ex_enigmaticlegacy.magic_table.label.input_count",
                    menu.getFormattedInputCount()), infoX, 32, 0xFFFFFF);
        }

        if (menu.getBlockEntity() != null && menu.getBlockEntity().getCurrentRecipe() != null) {
            guiGraphics.drawString(font, Component.translatable("gui.ex_enigmaticlegacy.magic_table.label.cost",
                            MagicTableMenu.formatBigInteger(menu.getBlockEntity().getCurrentRecipe().getEmcCost())),
                    infoX, 44, 0xFFFFFF);
        }

        guiGraphics.drawString(font, Component.translatable("gui.ex_enigmaticlegacy.magic_table.label.gear",
                TileMagicTable.getGearName(menu.convertGear)), 23, 100, 0xFFFFFF);

        if (menu.syncedOutputCount > 0 && menu.syncedItemEmc > 0) {
            guiGraphics.drawString(font, Component.translatable("gui.ex_enigmaticlegacy.magic_table.label.preview",
                    menu.getFormattedConvertEmc()), infoX, 56, 0xFFFFFF);
        }

        if (menu.syncedItemEmc > 0) {
            guiGraphics.drawString(font, Component.translatable("gui.ex_enigmaticlegacy.magic_table.label.unit_price",
                    MagicTableMenu.formatLargeNumber(menu.syncedItemEmc)), infoX, 68, 0xFFFFFF);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderVirtualSlots(guiGraphics, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        boolean isCustomGear = (menu.convertGear == 5);
        if (customAmountField != null) {
            customAmountField.setVisible(isCustomGear);
        }

        if (convertButton != null && convertButton.isHoveredOrFocused()) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("gui.ex_enigmaticlegacy.magic_table.tooltip.convert_desc"));
            tooltip.add(Component.translatable("gui.ex_enigmaticlegacy.magic_table.tooltip.current_gear",
                    TileMagicTable.getGearName(menu.convertGear)));
            if (menu.syncedOutputCount > 0 && menu.syncedItemEmc > 0) {
                tooltip.add(Component.translatable("gui.ex_enigmaticlegacy.magic_table.tooltip.expected_emc",
                        menu.getFormattedConvertEmc()));
            }
            if (menu.syncedItemEmc <= 0) {
                tooltip.add(Component.translatable("gui.ex_enigmaticlegacy.magic_table.tooltip.no_emc_value"));
            }
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }

    private void renderVirtualSlots(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderVirtualSlotItem(guiGraphics, MagicTableMenu.SLOT_INPUT, menu.syncedInputCount);
        renderVirtualSlotItem(guiGraphics, MagicTableMenu.SLOT_OUTPUT, menu.syncedOutputCount);
    }

    private void renderVirtualSlotItem(GuiGraphics guiGraphics, int slotIndex, long realCount) {
        if (realCount <= 0) return;

        Slot slot = this.menu.slots.get(slotIndex);
        ItemStack displayStack = slot.getItem();
        if (displayStack.isEmpty()) return;

        int slotX = this.leftPos + slot.x;
        int slotY = this.topPos + slot.y;

        guiGraphics.renderItem(displayStack, slotX, slotY);

        String countText;
        if (realCount <= 1) {
            countText = null;
        } else if (realCount <= 999) {
            countText = String.valueOf(realCount);
        } else {
            countText = MagicTableMenu.formatLargeNumber(realCount);
        }

        if (countText != null) {
            guiGraphics.drawString(font, countText, slotX + 18 - font.width(countText) + 1,
                    slotY + 9, 0x000000, false);
            guiGraphics.drawString(font, countText, slotX + 18 - font.width(countText) - 1,
                    slotY + 9, 0x000000, false);
            guiGraphics.drawString(font, countText, slotX + 18 - font.width(countText),
                    slotY + 10, 0x000000, false);
            guiGraphics.drawString(font, countText, slotX + 18 - font.width(countText),
                    slotY + 8, 0x000000, false);
            guiGraphics.drawString(font, countText, slotX + 18 - font.width(countText),
                    slotY + 9, 0xFFFFFF, false);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (customAmountField != null && customAmountField.isFocused()) {
            if (keyCode == 256) {
                customAmountField.setFocused(false);
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