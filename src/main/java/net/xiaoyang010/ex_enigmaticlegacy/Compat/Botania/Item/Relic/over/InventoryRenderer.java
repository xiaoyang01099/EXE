package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;


public class InventoryRenderer {

    private static final int TOP_SPACE = 12 + Const.SQ;
    private static final int LEFT = 7;
    private static final int PAD = 4;
    private static final int SEGS_LEFT = 10;

    private static final int CENTER_HORIZ =
            Const.SLOTS_WIDTH / 2 - GuiButtonUnlockStorage.width / 2;
    private static final int CENTER_VERT =
            TOP_SPACE - Const.SLOTS_HEIGHT / 2 - GuiButtonUnlockStorage.height / 2;


    private static int rowFromSegment(int segment) {
        if (segment <= SEGS_LEFT) {
            return (int) Math.ceil(((double) segment) / 2);
        } else {
            return segment - SEGS_LEFT;
        }
    }

    private static int colFromSegment(int segment) {
        if (segment <= SEGS_LEFT) {
            return (segment % 2 == 0) ? 2 : 1;
        } else {
            return 3;
        }
    }


    public static int xPosBtn(int segment) {
        int col = colFromSegment(segment);
        return (col - 1) * Const.SLOTS_WIDTH + CENTER_HORIZ;
    }

    public static int yPosBtn(int segment) {
        int row = rowFromSegment(segment);
        return row * Const.SLOTS_HEIGHT + CENTER_VERT;
    }

    public static int xPosTexture(int segment) {
        int col = colFromSegment(segment);
        return LEFT + (col - 1) * (PAD + Const.SLOTS_WIDTH);
    }


    public static int yPosTexture(int segment) {
        int row = rowFromSegment(segment);
        return TOP_SPACE + (row - 1) * (PAD + Const.SLOTS_HEIGHT);
    }


    public static int xPosSlotsStart(int segment) {
        int col = colFromSegment(segment);
        return 2 * PAD + (col - 1) * (Const.SLOTS_WIDTH + PAD);
    }

    public static int yPosSlotsStart(int segment) {
        int row = rowFromSegment(segment);
        int topLimit = TOP_SPACE + 1;
        return topLimit + (row - 1) * (Const.SLOTS_HEIGHT + PAD);
    }

    public static int xPosSwap(int segment) {
        int col = colFromSegment(segment);
        return 2 + (col - 1) * (Const.SLOTS_WIDTH + PAD);
    }

    public static int yPosSwap(int segment) {
        int row = rowFromSegment(segment);
        return 30 + (row - 1) * (Const.SLOTS_HEIGHT + PAD);
    }
}