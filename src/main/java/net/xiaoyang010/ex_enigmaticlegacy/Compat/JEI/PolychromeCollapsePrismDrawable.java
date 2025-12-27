package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import java.util.Map;

/**
 * 多彩坍缩棱镜的完整多方块结构绘制器
 * 完整展示 15x15x3 的所有方块，使用等距投影
 */
public class PolychromeCollapsePrismDrawable implements IDrawable {

    private final Map<Character, IDrawable> blockDrawables;
    private final String[][] structure;
    private final int blockSize;
    private final float scale;

    public PolychromeCollapsePrismDrawable(Map<Character, IDrawable> blockDrawables, String[][] structure) {
        this.blockDrawables = blockDrawables;
        this.structure = structure;
        this.blockSize = 4; // 每个方块的基础显示大小
        this.scale = 0.5f;  // 整体缩放比例
    }

    @Override
    public int getWidth() {
        return 200; // 足够宽以容纳 15x15 的结构
    }

    @Override
    public int getHeight() {
        return 150; // 足够高以容纳三层叠加
    }

    @Override
    public void draw(PoseStack ms, int xOffset, int yOffset) {
        ms.pushPose();

        // 使用等距投影绘制所有三层
        // 从底层到顶层，从后到前绘制以实现正确的遮挡

        // 第 0 层（底层 - 塔柱层）
        drawIsometricLayer(ms, xOffset, yOffset, structure[0], 0, 0x40FFFFFF);

        // 第 1 层（中层 - 魔力池层）
        drawIsometricLayer(ms, xOffset, yOffset, structure[1], 1, 0x80FFFFFF);

        // 第 2 层（顶层 - 主仪式层）
        drawIsometricLayer(ms, xOffset, yOffset, structure[2], 2, 0xFFFFFFFF);

        ms.popPose();
    }

    /**
     * 使用等距投影绘制单个层
     * @param layer 层的高度（0-2）
     * @param tint 色调，用于区分不同层
     */
    private void drawIsometricLayer(PoseStack ms, int baseX, int baseY, String[] layerData, int layer, int tint) {
        // 等距投影参数
        final double ISO_ANGLE = Math.toRadians(30); // 等距角度
        final int LAYER_HEIGHT = 12; // 每层的垂直偏移

        // 从后到前，从右到左绘制，确保正确的遮挡关系
        for (int z = 14; z >= 0; z--) {
            for (int x = 14; x >= 0; x--) {
                char blockChar = layerData[z].charAt(x);

                if (blockChar != '_') {
                    IDrawable drawable = blockDrawables.get(blockChar);
                    if (drawable != null) {
                        // 计算等距投影坐标
                        // x 轴向右下，z 轴向左下
                        double isoX = (x - z) * Math.cos(ISO_ANGLE) * blockSize * scale;
                        double isoY = (x + z) * Math.sin(ISO_ANGLE) * blockSize * scale - layer * LAYER_HEIGHT;

                        int finalX = (int) (baseX + isoX + 100); // 居中偏移
                        int finalY = (int) (baseY + isoY + 20);

                        ms.pushPose();

                        // 缩放方块
                        ms.translate(finalX, finalY, layer * 10);
                        ms.scale(scale, scale, 1.0f);

                        // 绘制方块
                        drawable.draw(ms, 0, 0);

                        // 特殊高亮处理
                        if (blockChar == 'P') {
                            // 棱镜额外发光效果
                            ms.pushPose();
                            ms.translate(-2, -2, 5);
                            ms.scale(1.3f, 1.3f, 1.0f);
                            drawable.draw(ms, 0, 0);
                            ms.popPose();
                        }

                        ms.popPose();
                    }
                }
            }
        }
    }

    /**
     * 绘制方块的边框（用于调试或强调）
     */
    private void drawBlockOutline(PoseStack ms, int x, int y, int color) {
        // 可以使用 GuiComponent.fill 绘制边框
        // 这里省略具体实现
    }
}