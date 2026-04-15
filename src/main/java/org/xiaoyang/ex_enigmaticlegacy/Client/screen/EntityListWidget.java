package org.xiaoyang.ex_enigmaticlegacy.Client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.BiConsumer;

public class EntityListWidget extends ObjectSelectionList<EntityListWidget.EntityEntry> {
    private final BiConsumer<ResourceLocation, Boolean> toggleCallback;

    public EntityListWidget(Minecraft mc, int width, int height, int top, int bottom, int itemHeight, BiConsumer<ResourceLocation, Boolean> toggleCallback) {
        super(mc, width, height, top, bottom, itemHeight);
        this.toggleCallback = toggleCallback;
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
    }

    @Override
    public int getRowWidth() {
        return this.width - 10;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x0 + this.width - 6;
    }

    public void clearEntries() {
        super.clearEntries();
        this.setScrollAmount(0);
    }

    public static class EntityEntry extends ObjectSelectionList.Entry<EntityEntry> {
        private final EntityListWidget parent;
        private final ResourceLocation entityId;
        private boolean disabled;
        private final String displayName;
        private static final int BTN_W = 54;
        private static final int BTN_H = 14;

        public EntityEntry(EntityListWidget parent, ResourceLocation entityId, boolean disabled) {
            this.parent   = parent;
            this.entityId = entityId;
            this.disabled = disabled;
            this.displayName = resolveDisplayName(entityId);
        }

        private static String resolveDisplayName(ResourceLocation rl) {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(rl);
            if (type == null) return rl.toString();

            Component description = type.getDescription();
            String translated = description.getString();

            String translationKey = type.getDescriptionId();
            if (translated.equals(translationKey)) {
                return rl.toString();
            }
            return translated;
        }

        @Override
        public Component getNarration() {
            return Component.literal(displayName);
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            Minecraft mc = Minecraft.getInstance();

            int bgColor = (index % 2 == 0) ? 0x22FFFFFF : 0x11FFFFFF;
            graphics.fill(left, top, left + width, top + height, bgColor);
            if (isHovered) {
                graphics.fill(left, top, left + width, top + height, 0x22FFFFFF);
            }

            int btnX = left + width - BTN_W - 4;
            int btnY = top + (height - BTN_H) / 2;

            String label = displayName;
            int maxTextWidth = width - BTN_W - 16;
            if (mc.font.width(label) > maxTextWidth) {
                label = mc.font.plainSubstrByWidth(label, maxTextWidth - 6) + "...";
            }
            graphics.drawString(mc.font, label, left + 4, top + (height - mc.font.lineHeight) / 2, 0xFFEEEEEE);

            boolean hoveringBtn = mouseX >= btnX && mouseX <= btnX + BTN_W && mouseY >= btnY && mouseY <= btnY + BTN_H;

            int btnColor      = disabled ? 0xFFAA2222 : 0xFF22AA44;
            int btnHoverColor = disabled ? 0xFFCC3333 : 0xFF33CC55;

            graphics.fill(btnX, btnY, btnX + BTN_W, btnY + BTN_H, hoveringBtn ? btnHoverColor : btnColor);
            graphics.fill(btnX,btnY,btnX + BTN_W,btnY + 1,0xFF000000);
            graphics.fill(btnX,btnY + BTN_H - 1,btnX + BTN_W,btnY + BTN_H,0xFF000000);
            graphics.fill(btnX,btnY,btnX + 1,btnY + BTN_H,0xFF000000);
            graphics.fill(btnX + BTN_W - 1, btnY,btnX + BTN_W,btnY + BTN_H,0xFF000000);

            String btnText   = disabled ? "已禁用" : "已启用";
            int    textColor = disabled ? 0xFFFFAAAA : 0xFFAAFFAA;
            graphics.drawCenteredString(mc.font, btnText, btnX + BTN_W / 2, btnY + (BTN_H - mc.font.lineHeight) / 2 + 1, textColor);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) return false;

            int index = parent.children().indexOf(this);
            if (index < 0) return false;

            int rowTop   = parent.getRowTop(index);
            int rowLeft  = parent.getRowLeft();
            int rowWidth = parent.getRowWidth();

            int btnX = rowLeft + rowWidth - BTN_W - 4;
            int btnY = rowTop  + (parent.itemHeight - BTN_H) / 2;

            if (mouseX >= btnX && mouseX <= btnX + BTN_W
                    && mouseY >= btnY && mouseY <= btnY + BTN_H) {

                boolean newDisabledState = !disabled;
                disabled = newDisabledState;
                parent.toggleCallback.accept(entityId, newDisabledState);
                return true;
            }
            return false;
        }
    }
}