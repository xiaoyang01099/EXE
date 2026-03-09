package net.xiaoyang010.ex_enigmaticlegacy.Util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;

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
    public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int blitOffset) {
        poseStack.pushPose();
        poseStack.translate(0, 0, 400);
        ModRarities.drawWaveNameWithStyle(
                poseStack, font,
                data.getStack(),
                data.getRawText(),
                data.getStyle(),
                x, y, 255
        );
        poseStack.popPose();
    }
}