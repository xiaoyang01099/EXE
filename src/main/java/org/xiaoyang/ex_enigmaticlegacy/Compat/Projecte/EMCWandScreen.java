package org.xiaoyang.ex_enigmaticlegacy.Compat.Projecte;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EMCWandScreen extends AbstractContainerScreen<EMCWandMenu> {
    private static final int GUI_WIDTH = 280;
    private static final int GUI_HEIGHT = 230;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 28;
    private static final int GRID_COLS = 12;
    private static final int GRID_ROWS = 8;
    private static final int CELL_SIZE = 20;
    private static final int SCROLL_BAR_X = GRID_X + GRID_COLS * CELL_SIZE + 6;
    private static final int SCROLL_BAR_WIDTH = 16;

    private static final List<ItemEmcEntry> allItems = new ArrayList<>();
    private final List<ItemEmcEntry> filteredItems = new ArrayList<>();
    private static boolean isAllMode = false;

    private int scrollOffset = 0;
    private int maxScroll = 0;

    private EditBox searchBox;
    private EditBox emcInputBox;
    private Button modeToggleButton;
    private Button confirmButton;
    private Button cancelButton;
    private Button scrollUpButton;
    private Button scrollDownButton;

    private ItemEmcEntry selectedItem = null;
    private boolean showEmcDialog = false;
    private String searchText = "";
    private boolean isDraggingScrollbar = false;
    private static final int DIALOG_W = 200;
    private static final int DIALOG_H = 80;

    public EMCWandScreen(EMCWandMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelY = -999;
        this.titleLabelY = -999;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.searchBox = new EditBox(this.font, x + 8, y + 8, 150, 14,
                Component.translatable("gui.ex_enigmaticlegacy.emc_wand.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(true);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(0xFFFFFF);
        this.searchBox.setValue(this.searchText);
        this.searchBox.setResponder(text -> {
            this.searchText = text;
            this.scrollOffset = 0;
            updateFilteredItems();
        });
        this.addRenderableWidget(searchBox);

        this.modeToggleButton = Button.builder(
                getModeButtonText(),
                btn -> {
                    isAllMode = !isAllMode;
                    btn.setMessage(getModeButtonText());
                    requestItemList();
                }
        ).bounds(x + 165, y + 6, 82, 18).build();
        this.addRenderableWidget(modeToggleButton);

        int scrollBtnX = x + SCROLL_BAR_X;
        this.scrollUpButton = Button.builder(
                Component.literal("▲"),
                btn -> { if (scrollOffset > 0) scrollOffset--; }
        ).bounds(scrollBtnX, y + GRID_Y, SCROLL_BAR_WIDTH, SCROLL_BAR_WIDTH).build();
        this.addRenderableWidget(scrollUpButton);

        this.scrollDownButton = Button.builder(
                Component.literal("▼"),
                btn -> { if (scrollOffset < maxScroll) scrollOffset++; }
        ).bounds(scrollBtnX, y + GRID_Y + GRID_ROWS * CELL_SIZE - SCROLL_BAR_WIDTH,
                SCROLL_BAR_WIDTH, SCROLL_BAR_WIDTH).build();
        this.addRenderableWidget(scrollDownButton);

        String previousEmcValue = (this.emcInputBox != null) ? this.emcInputBox.getValue() : "";

        this.emcInputBox = new EditBox(this.font, 0, 0, 130, 16,
                Component.translatable("gui.ex_enigmaticlegacy.emc_wand.emc_value"));
        this.emcInputBox.setMaxLength(19);
        this.emcInputBox.setVisible(false);
        this.addRenderableWidget(emcInputBox);

        this.confirmButton = Button.builder(
                Component.translatable("gui.ex_enigmaticlegacy.emc_wand.confirm"),
                btn -> confirmSetEmc()
        ).bounds(0, 0, 70, 18).build();
        this.confirmButton.visible = false;
        this.addRenderableWidget(confirmButton);

        this.cancelButton = Button.builder(
                Component.translatable("gui.ex_enigmaticlegacy.emc_wand.cancel"),
                btn -> closeEmcDialog()
        ).bounds(0, 0, 70, 18).build();
        this.cancelButton.visible = false;
        this.addRenderableWidget(cancelButton);

        if (showEmcDialog && selectedItem != null) {
            int dx = getDialogX();
            int dy = getDialogY();

            this.emcInputBox.x = dx + 40;
            this.emcInputBox.y = dy + 32;
            this.emcInputBox.setVisible(true);
            this.emcInputBox.setFocused(true);
            this.emcInputBox.setValue(previousEmcValue);

            this.confirmButton.x = dx + 20;
            this.confirmButton.y = dy + 55;
            this.confirmButton.visible = true;

            this.cancelButton.x = dx + DIALOG_W - 90;
            this.cancelButton.y = dy + 55;
            this.cancelButton.visible = true;

            this.searchBox.active = false;
            this.modeToggleButton.active = false;
            this.scrollUpButton.active = false;
            this.scrollDownButton.active = false;
        }

        if (allItems.isEmpty()) {
            requestItemList();
        } else {
            updateFilteredItems();
        }
    }

    private Component getModeButtonText() {
        return isAllMode
                ? Component.translatable("gui.ex_enigmaticlegacy.emc_wand.mode.all")
                : Component.translatable("gui.ex_enigmaticlegacy.emc_wand.mode.unset");
    }

    private void requestItemList() {
        NetworkHandler.CHANNEL.sendToServer(new EMCWandRequestPacket(isAllMode));
    }

    public static void handleItemListPacket(List<EMCWandItemListPacket.ItemEmcData> items, boolean allMode, boolean isAppend, boolean isFinal) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof EMCWandScreen screen)) return;

        if (!isAppend) {
            allItems.clear();
        }

        for (var data : items) {
            ResourceLocation rl = ResourceLocation.tryParse(data.itemId());
            if (rl != null) {
                Item item = ForgeRegistries.ITEMS.getValue(rl);
                if (item != null) {
                    allItems.add(new ItemEmcEntry(data.itemId(), item, data.emcValue()));
                }
            }
        }

        if (isFinal) {
            isAllMode = allMode;
            screen.modeToggleButton.setMessage(screen.getModeButtonText());
            screen.scrollOffset = 0;
            screen.updateFilteredItems();
        }
    }

    public static void handleResultPacket(String itemId, long emcValue, boolean success) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof EMCWandScreen screen) {
            if (success) {
                screen.closeEmcDialog();
                screen.requestItemList();
            }
        }
    }

    private void updateFilteredItems() {
        filteredItems.clear();
        String search = searchText.toLowerCase(Locale.ROOT).trim();

        for (ItemEmcEntry entry : allItems) {
            if (search.isEmpty()) {
                filteredItems.add(entry);
                continue;
            }
            String itemName = entry.item.getDescription().getString();
            String itemIdLower = entry.itemId.toLowerCase(Locale.ROOT);
            String itemNameLower = itemName.toLowerCase(Locale.ROOT);
            if (itemNameLower.contains(search) || itemIdLower.contains(search)) {
                filteredItems.add(entry);
            }
        }

        int totalRows = (int) Math.ceil((double) filteredItems.size() / GRID_COLS);
        maxScroll = Math.max(0, totalRows - GRID_ROWS);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    private int getDialogX() { return (this.width - DIALOG_W) / 2; }
    private int getDialogY() { return (this.height - DIALOG_H) / 2; }

    private void repositionDialogWidgets() {
        int dx = getDialogX();
        int dy = getDialogY();

        this.emcInputBox.x = dx + 40;
        this.emcInputBox.y = dy + 32;

        this.confirmButton.x = dx + 20;
        this.confirmButton.y = dy + 55;

        this.cancelButton.x = dx + DIALOG_W - 90;
        this.cancelButton.y = dy + 55;
    }

    private void openEmcDialog(ItemEmcEntry entry) {
        this.selectedItem = entry;
        this.showEmcDialog = true;

        repositionDialogWidgets();

        this.emcInputBox.setVisible(true);
        this.emcInputBox.setFocused(true);
        this.emcInputBox.setValue(entry.emcValue > 0 ? String.valueOf(entry.emcValue) : "");

        this.confirmButton.visible = true;
        this.cancelButton.visible = true;

        this.searchBox.setFocused(false);
        this.searchBox.active = false;
        this.modeToggleButton.active = false;
        this.scrollUpButton.active = false;
        this.scrollDownButton.active = false;
    }

    private void closeEmcDialog() {
        this.selectedItem = null;
        this.showEmcDialog = false;
        this.emcInputBox.setVisible(false);
        this.emcInputBox.setFocused(false);
        this.emcInputBox.setValue("");
        this.confirmButton.visible = false;
        this.cancelButton.visible = false;
        this.searchBox.active = true;
        this.modeToggleButton.active = true;
        this.scrollUpButton.active = true;
        this.scrollDownButton.active = true;
    }

    private void confirmSetEmc() {
        if (selectedItem == null) return;
        String text = emcInputBox.getValue().trim();
        if (text.isEmpty()) return;
        try {
            long value = Long.parseLong(text);
            if (value >= 0) {
                NetworkHandler.CHANNEL.sendToServer(
                        new EMCWandSetValuePacket(selectedItem.itemId, value));
            }
        } catch (NumberFormatException ignored) {}
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, 0xCC1A1A2E);

        guiGraphics.fill(x - 1, y - 1, x + GUI_WIDTH + 1, y, 0xFF6644AA);
        guiGraphics.fill(x - 1, y + GUI_HEIGHT, x + GUI_WIDTH + 1, y + GUI_HEIGHT + 1, 0xFF6644AA);
        guiGraphics.fill(x - 1, y, x, y + GUI_HEIGHT, 0xFF6644AA);
        guiGraphics.fill(x + GUI_WIDTH, y, x + GUI_WIDTH + 1, y + GUI_HEIGHT, 0xFF6644AA);

        int gridRight = x + GRID_X + GRID_COLS * CELL_SIZE;
        int gridBottom = y + GRID_Y + GRID_ROWS * CELL_SIZE;
        guiGraphics.fill(x + GRID_X - 1, y + GRID_Y - 1, gridRight + 1, gridBottom + 1, 0xFF333355);

        for (int row = 0; row <= GRID_ROWS; row++) {
            int lineY = y + GRID_Y + row * CELL_SIZE;
            guiGraphics.fill(x + GRID_X, lineY, gridRight, lineY + 1, 0xFF555577);
        }
        for (int col = 0; col <= GRID_COLS; col++) {
            int lineX = x + GRID_X + col * CELL_SIZE;
            guiGraphics.fill(lineX, y + GRID_Y, lineX + 1, gridBottom, 0xFF555577);
        }

        int scrollTrackX = x + SCROLL_BAR_X;
        int scrollTrackTop = y + GRID_Y + SCROLL_BAR_WIDTH + 2;
        int scrollTrackBottom = y + GRID_Y + GRID_ROWS * CELL_SIZE - SCROLL_BAR_WIDTH - 2;
        guiGraphics.fill(scrollTrackX, scrollTrackTop,
                scrollTrackX + SCROLL_BAR_WIDTH, scrollTrackBottom, 0xFF222244);

        if (maxScroll > 0) {
            int trackHeight = scrollTrackBottom - scrollTrackTop;
            int thumbHeight = Math.max(10, trackHeight / (maxScroll + GRID_ROWS));
            int thumbY = scrollTrackTop + (int) ((double) scrollOffset / maxScroll * (trackHeight - thumbHeight));
            guiGraphics.fill(scrollTrackX + 2, thumbY,
                    scrollTrackX + SCROLL_BAR_WIDTH - 2, thumbY + thumbHeight, 0xFF8866CC);
            guiGraphics.fill(scrollTrackX + 2, thumbY,
                    scrollTrackX + SCROLL_BAR_WIDTH - 2, thumbY + 1, 0xFFAA88EE);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.drawString(font, this.title, x + 8, y + GUI_HEIGHT - 14, 0xFFFFFF);
        String countText = filteredItems.size() + " items";
        guiGraphics.drawString(font, countText,
                x + GUI_WIDTH - this.font.width(countText) - 8, y + GUI_HEIGHT - 14, 0xAAAAAA);

        renderItemGrid(guiGraphics, x, y, mouseX, mouseY);

        if (showEmcDialog) {
            guiGraphics.fill(0, 0, this.width, this.height, 0xAA000000);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 400);

            renderEmcDialogOverlay(guiGraphics);

            emcInputBox.render(guiGraphics, mouseX, mouseY, partialTick);
            confirmButton.render(guiGraphics, mouseX, mouseY, partialTick);
            cancelButton.render(guiGraphics, mouseX, mouseY, partialTick);

            guiGraphics.pose().popPose();
        } else {
            renderItemTooltip(guiGraphics, x, y, mouseX, mouseY);
        }
    }

    private void renderItemGrid(GuiGraphics guiGraphics, int guiX, int guiY, int mouseX, int mouseY) {
        int startIndex = scrollOffset * GRID_COLS;

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = startIndex + row * GRID_COLS + col;
                if (index >= filteredItems.size()) return;

                ItemEmcEntry entry = filteredItems.get(index);
                int cellX = guiX + GRID_X + col * CELL_SIZE + 2;
                int cellY = guiY + GRID_Y + row * CELL_SIZE + 2;

                if (selectedItem != null && selectedItem.itemId.equals(entry.itemId)) {
                    guiGraphics.fill(cellX - 2, cellY - 2, cellX + 18, cellY + 18, 0x6600FF00);
                }

                if (!showEmcDialog && mouseX >= cellX && mouseX < cellX + 16
                        && mouseY >= cellY && mouseY < cellY + 16) {
                    guiGraphics.fill(cellX - 1, cellY - 1, cellX + 17, cellY + 17, 0x44FFFFFF);
                }

                guiGraphics.renderItem(new ItemStack(entry.item), cellX, cellY);

                if (entry.emcValue > 0) {
                    guiGraphics.fill(cellX + 13, cellY, cellX + 16, cellY + 3, 0xFF00FF00);
                } else {
                    guiGraphics.fill(cellX + 13, cellY, cellX + 16, cellY + 3, 0xFFFF4444);
                }
            }
        }
    }

    private void renderEmcDialogOverlay(GuiGraphics guiGraphics) {
        if (selectedItem == null) return;

        int dx = getDialogX();
        int dy = getDialogY();

        guiGraphics.fill(dx - 3, dy - 3, dx + DIALOG_W + 3, dy + DIALOG_H + 3, 0xFF8866CC);
        guiGraphics.fill(dx - 2, dy - 2, dx + DIALOG_W + 2, dy + DIALOG_H + 2, 0xFF6644AA);
        guiGraphics.fill(dx, dy, dx + DIALOG_W, dy + DIALOG_H, 0xFF2A2A3E);

        guiGraphics.drawString(font, "Set EMC Value", dx + 8, dy + 6, 0xFFFFFF);

        String itemName = selectedItem.item.getDescription().getString();
        if (itemName.length() > 28) itemName = itemName.substring(0, 26) + "...";
        guiGraphics.drawString(font, itemName, dx + 8, dy + 20, 0xFFFF88);
        guiGraphics.drawString(font, "EMC:", dx + 10, dy + 36, 0xCCCCCC);
    }

    private void renderItemTooltip(GuiGraphics guiGraphics, int guiX, int guiY, int mouseX, int mouseY) {
        if (showEmcDialog) return;

        int startIndex = scrollOffset * GRID_COLS;
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = startIndex + row * GRID_COLS + col;
                if (index >= filteredItems.size()) return;

                int cellX = guiX + GRID_X + col * CELL_SIZE + 2;
                int cellY = guiY + GRID_Y + row * CELL_SIZE + 2;

                if (mouseX >= cellX && mouseX < cellX + 16 && mouseY >= cellY && mouseY < cellY + 16) {
                    ItemEmcEntry entry = filteredItems.get(index);
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(entry.item.getDescription());
                    tooltip.add(Component.literal("§7" + entry.itemId));
                    if (entry.emcValue > 0) {
                        tooltip.add(Component.literal("§6EMC: §f" + formatEmc(entry.emcValue)));
                    } else {
                        tooltip.add(Component.literal("§cNo EMC value"));
                    }
                    tooltip.add(Component.literal("§eClick to edit EMC"));
                    guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                    return;
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (showEmcDialog) {
            int dx = getDialogX();
            int dy = getDialogY();
            boolean inDialog = mouseX >= dx - 3 && mouseX <= dx + DIALOG_W + 3
                    && mouseY >= dy - 3 && mouseY <= dy + DIALOG_H + 3;
            if (inDialog) {
                if (emcInputBox.isMouseOver(mouseX, mouseY)) {
                    emcInputBox.mouseClicked(mouseX, mouseY, button);
                    emcInputBox.setFocused(true);
                    return true;
                }
                if (confirmButton.isMouseOver(mouseX, mouseY)) {
                    confirmButton.mouseClicked(mouseX, mouseY, button);
                    return true;
                }
                if (cancelButton.isMouseOver(mouseX, mouseY)) {
                    cancelButton.mouseClicked(mouseX, mouseY, button);
                    return true;
                }
                return true;
            }
            closeEmcDialog();
            return true;
        }

        if (searchBox != null && searchBox.isMouseOver(mouseX, mouseY)) {
            searchBox.mouseClicked(mouseX, mouseY, button);
            searchBox.setFocused(true);
            return true;
        } else if (searchBox != null) {
            searchBox.setFocused(false);
        }

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        int scrollTrackX = x + SCROLL_BAR_X;
        int scrollTrackTop = y + GRID_Y + SCROLL_BAR_WIDTH + 2;
        int scrollTrackBottom = y + GRID_Y + GRID_ROWS * CELL_SIZE - SCROLL_BAR_WIDTH - 2;

        if (mouseX >= scrollTrackX && mouseX <= scrollTrackX + SCROLL_BAR_WIDTH
                && mouseY >= scrollTrackTop && mouseY <= scrollTrackBottom && maxScroll > 0) {
            isDraggingScrollbar = true;
            updateScrollFromMouse(mouseY, scrollTrackTop, scrollTrackBottom);
            return true;
        }

        int startIndex = scrollOffset * GRID_COLS;
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = startIndex + row * GRID_COLS + col;
                if (index >= filteredItems.size()) break;
                int cellX = x + GRID_X + col * CELL_SIZE + 2;
                int cellY = y + GRID_Y + row * CELL_SIZE + 2;
                if (mouseX >= cellX && mouseX < cellX + 16 && mouseY >= cellY && mouseY < cellY + 16) {
                    openEmcDialog(filteredItems.get(index));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDraggingScrollbar && maxScroll > 0) {
            int y = (this.height - this.imageHeight) / 2;
            int scrollTrackTop = y + GRID_Y + SCROLL_BAR_WIDTH + 2;
            int scrollTrackBottom = y + GRID_Y + GRID_ROWS * CELL_SIZE - SCROLL_BAR_WIDTH - 2;
            updateScrollFromMouse(mouseY, scrollTrackTop, scrollTrackBottom);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDraggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateScrollFromMouse(double mouseY, int trackTop, int trackBottom) {
        double ratio = (mouseY - trackTop) / (trackBottom - trackTop);
        ratio = Math.max(0, Math.min(1, ratio));
        scrollOffset = (int) Math.round(ratio * maxScroll);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!showEmcDialog) {
            if (delta > 0 && scrollOffset > 0) { scrollOffset--; return true; }
            else if (delta < 0 && scrollOffset < maxScroll) { scrollOffset++; return true; }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            if (showEmcDialog) { closeEmcDialog(); return true; }
            if (searchBox != null && searchBox.isFocused()) { searchBox.setFocused(false); return true; }
            this.onClose();
            return true;
        }
        if (keyCode == 257 && showEmcDialog) { confirmSetEmc(); return true; }
        if (showEmcDialog && emcInputBox != null && emcInputBox.isFocused())
            return emcInputBox.keyPressed(keyCode, scanCode, modifiers);
        if (searchBox != null && searchBox.isFocused())
            return searchBox.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (showEmcDialog && emcInputBox != null && emcInputBox.isFocused()) {
            if (Character.isDigit(codePoint)) return emcInputBox.charTyped(codePoint, modifiers);
            return true;
        }
        if (searchBox != null && searchBox.isFocused())
            return searchBox.charTyped(codePoint, modifiers);
        return super.charTyped(codePoint, modifiers);
    }

    private static String formatEmc(long value) {
        if (value < 1000) return String.valueOf(value);
        if (value < 1_000_000) return String.format("%.1fK", value / 1000.0);
        if (value < 1_000_000_000L) return String.format("%.1fM", value / 1_000_000.0);
        if (value < 1_000_000_000_000L) return String.format("%.1fB", value / 1_000_000_000.0);
        return String.format("%.1fT", value / 1_000_000_000_000.0);
    }

    public record ItemEmcEntry(String itemId, Item item, long emcValue) {}
}