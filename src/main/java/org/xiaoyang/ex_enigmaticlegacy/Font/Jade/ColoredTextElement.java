package org.xiaoyang.ex_enigmaticlegacy.Font.Jade;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;

import java.util.Objects;

public class ColoredTextElement extends Element {
    private final FormattedText text;
    private final int color;

    public ColoredTextElement(FormattedText text, int color) {
        this.text = text;
        this.color = color;
    }

    public ColoredTextElement(Component component) {
        this.text = component;
        int extracted = extractColor(component);
        this.color = extracted;
    }

    @Override
    public Vec2 getSize() {
        float width = (float) Math.max(DisplayHelper.font().width(this.text), 0);
        Objects.requireNonNull(DisplayHelper.font());
        return new Vec2(width, (float) (9 + 1));
    }

    @Override
    public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
        DisplayHelper.INSTANCE.drawText(guiGraphics, this.text, x, y, this.color);
    }

    @Override
    public @Nullable String getMessage() {
        return this.text.getString();
    }

    private static int extractColor(Component component) {
        if (component.getStyle() != null && component.getStyle().getColor() != null) {
            return 0xFF000000 | component.getStyle().getColor().getValue();
        }
        for (Component sibling : component.getSiblings()) {
            if (sibling.getStyle() != null && sibling.getStyle().getColor() != null) {
                return 0xFF000000 | sibling.getStyle().getColor().getValue();
            }
        }
        return 0xFFFFFFFF;
    }
}