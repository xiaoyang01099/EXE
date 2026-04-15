package org.xiaoyang.ex_enigmaticlegacy.Font;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public class WaveNameTooltipComponent implements ClientTooltipComponent {
    private final WaveNameData data;

    public WaveNameTooltipComponent(WaveNameData data) {
        this.data = data;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public int getWidth(Font font) {
        return font.width(data.getRawText());
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        ModRarities.drawWaveNameWithStyle(
                graphics, font,
                data.getStack(),
                data.getRawText(),
                data.getStyle(),
                x, y, 255
        );
        graphics.pose().popPose();
    }
}