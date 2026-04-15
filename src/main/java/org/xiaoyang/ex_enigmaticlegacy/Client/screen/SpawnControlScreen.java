package org.xiaoyang.ex_enigmaticlegacy.Client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.PacketToggleSpawn;
import org.xiaoyang.ex_enigmaticlegacy.SpawnControlConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SpawnControlScreen extends Screen {
    private static final int GUI_WIDTH  = 320;
    private static final int GUI_HEIGHT = 240;
    
    private enum FilterMode {
        ALL("全部"),
        DISABLED("已禁用"),
        ENABLED("已启用");

        final String label;
        FilterMode(String label) { this.label = label; }

        FilterMode next() {
            FilterMode[] vals = values();
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    private FilterMode filterMode = FilterMode.ALL;
    private Button filterButton;
    private EditBox searchBox;
    private EntityListWidget entityList;
    private int guiX, guiY;

    private final List<ResourceLocation> allEntities = new ArrayList<>();

    public SpawnControlScreen() {
        super(Component.translatable("gui.spawncontrol.title"));
    }

    @Override
    protected void init() {
        guiX = (this.width  - GUI_WIDTH)  / 2;
        guiY = (this.height - GUI_HEIGHT) / 2;

        allEntities.clear();
        ForgeRegistries.ENTITY_TYPES.getKeys()
                .stream()
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .forEach(allEntities::add);

        searchBox = new EditBox(
                this.font,
                guiX + 6, guiY + 22,
                GUI_WIDTH - 74, 16,
                Component.literal("搜索...")
        );
        searchBox.setHint(Component.literal("搜索实体..."));
        searchBox.setMaxLength(100);
        searchBox.setResponder(this::onSearchChanged);
        this.addRenderableWidget(searchBox);

        filterButton = Button.builder(
                        Component.literal(filterMode.label),
                        btn -> {
                            filterMode = filterMode.next();
                            btn.setMessage(Component.literal(filterMode.label));
                            refreshList(searchBox.getValue());
                        })
                .bounds(guiX + GUI_WIDTH - 64, guiY + 22, 58, 16)
                .build();
        this.addRenderableWidget(filterButton);

        int listTop    = guiY + 44;
        int listBottom = guiY + GUI_HEIGHT - 26;
        int listHeight = listBottom - listTop;

        entityList = new EntityListWidget(
                this.minecraft,
                GUI_WIDTH - 4,
                listHeight,
                listTop,
                listBottom,
                18,
                this::onToggleEntity
        );
        entityList.setLeftPos(guiX + 2);

        refreshList("");
        this.addRenderableWidget(entityList);

        this.addRenderableWidget(
                Button.builder(Component.literal("关闭"), btn -> this.onClose())
                        .bounds(guiX + GUI_WIDTH / 2 - 30, guiY + GUI_HEIGHT - 22, 60, 16)
                        .build()
        );
    }

    private void onSearchChanged(String text) {
        refreshList(text);
    }

    private void refreshList(String filter) {
        entityList.clearEntries();
        String lower = filter.toLowerCase();
        for (ResourceLocation rl : allEntities) {
            boolean isDisabled = SpawnControlConfig.isDisabled(rl);

            if (filterMode == FilterMode.DISABLED && !isDisabled) continue;
            if (filterMode == FilterMode.ENABLED  &&  isDisabled) continue;
            if (!lower.isEmpty() && !rl.toString().toLowerCase().contains(lower)) continue;

            entityList.addEntry(
                    new EntityListWidget.EntityEntry(entityList, rl, isDisabled)
            );
        }
    }

    private void onToggleEntity(ResourceLocation entityId, boolean wantDisabled) {
        if (wantDisabled) {
            SpawnControlConfig.disableEntity(entityId);
        } else {
            SpawnControlConfig.enableEntity(entityId);
        }
        NetworkHandler.CHANNEL.sendToServer(new PacketToggleSpawn(entityId, wantDisabled));
        refreshList(searchBox.getValue());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        graphics.fill(guiX - 1, guiY - 1, guiX + GUI_WIDTH + 1, guiY + GUI_HEIGHT + 1, 0xFF444466);
        graphics.fill(guiX, guiY, guiX + GUI_WIDTH, guiY + GUI_HEIGHT, 0xFF1A1A2E);
        graphics.fill(guiX, guiY, guiX + GUI_WIDTH, guiY + 18, 0xFF16213E);

        graphics.drawCenteredString(this.font, this.title, guiX + GUI_WIDTH / 2, guiY + 5, 0xFFFFAA00);
        graphics.drawString(this.font, "实体ID",  guiX + 8,              guiY + 33, 0xFFAAAAAA);
        graphics.drawString(this.font, "状态",    guiX + GUI_WIDTH - 52, guiY + 33, 0xFFAAAAAA);
        graphics.fill(guiX + 4, guiY + 43, guiX + GUI_WIDTH - 4, guiY + 44, 0xFF555577);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}