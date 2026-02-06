package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import java.util.Map;

public class PolychromeCollapsePrismDrawable implements IDrawable {

    private final Map<Character, IDrawable> blockMap;
    private final String[][] structure;

    public PolychromeCollapsePrismDrawable(Map<Character, IDrawable> blockMap, String[][] structure) {
        this.blockMap = blockMap;
        this.structure = structure;
    }

    @Override
    public int getWidth() {
        return 100;
    }

    @Override
    public int getHeight() {
        return 100;
    }

    @Override
    public void draw(PoseStack ms, int xOffset, int yOffset) {
        ms.pushPose();

        float scale = 0.35f;

        ms.translate(xOffset , yOffset , 100.0F);
        ms.scale(scale, scale, 1.0f);

        for (int layerIndex = 2; layerIndex >= 0; layerIndex--) {
            String[] layer = structure[layerIndex];

            int layerHeightOffset = (2 - layerIndex) * 12;

            for (int row = 0; row < 15; row++) {
                for (int col = 0; col < 15; col++) {

                    char blockChar = layer[row].charAt(col);

                    if (blockChar == '_') continue;

                    IDrawable drawable = blockMap.get(blockChar);
                    if (drawable != null) {
                        ms.pushPose();

                        int relX = col - 7;
                        int relY = row - 7;

                        int drawX = (relX - relY) * 12;
                        int drawY = (relX + relY) * 6;

                        ms.translate(drawX + 50, drawY - layerHeightOffset + 30, (row + col + (2-layerIndex)) * 5);

                        drawable.draw(ms, 0, 0);

                        ms.popPose();
                    }
                }
            }
        }

        ms.popPose();
    }
}