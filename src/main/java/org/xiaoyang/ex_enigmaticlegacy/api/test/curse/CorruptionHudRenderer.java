package org.xiaoyang.ex_enigmaticlegacy.api.test.curse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.xiaoyang.ex_enigmaticlegacy.api.test.curse.res.ManaCorruptionManager;
import org.xiaoyang.ex_enigmaticlegacy.api.test.curse.res.TileManaConverter;


public class CorruptionHudRenderer {

    private static final int DETECTION_RANGE = 16;

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGuiOverlayEvent.Post event) {
        if (!VanillaGuiOverlay.HOTBAR.id().equals(event.getOverlay().id())) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;

        if (player == null || level == null) return;

        BlockPos playerPos = player.blockPosition();
        AABB bounds = new AABB(playerPos).inflate(DETECTION_RANGE);

        int maxCorruption = 0;
        BlockPos corruptionSource = null;

        for (BlockPos pos : BlockPos.betweenClosed(
                (int) bounds.minX, (int) bounds.minY, (int) bounds.minZ,
                (int) bounds.maxX, (int) bounds.maxY, (int) bounds.maxZ)) {

            BlockEntity tile = level.getBlockEntity(pos);

            if (tile instanceof TileManaConverter converter) {
                int corruption = converter.getCorruptionLevel();
                if (corruption > maxCorruption) {
                    maxCorruption = corruption;
                    corruptionSource = pos.immutable();
                }
            }
        }

        if (maxCorruption > 0) {
            renderCorruptionWarning(event.getGuiGraphics(), mc, maxCorruption, corruptionSource, playerPos);
        }
    }

    private void renderCorruptionWarning(GuiGraphics gui, Minecraft mc, int corruption, BlockPos source, BlockPos playerPos) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        ManaCorruptionManager.CorruptionLevel level = ManaCorruptionManager.CorruptionLevel.fromValue(corruption);

        double distance = Math.sqrt(playerPos.distSqr(source));

        int color = getCorruptionColor(level);
        int alpha = Math.min(255, (int) (255 * (corruption / 100.0)));
        int colorWithAlpha = (alpha << 24) | (color & 0xFFFFFF);

        int barWidth = 100;
        int barHeight = 10;
        int barX = screenWidth - barWidth - 10;
        int barY = 10;

        gui.fill(barX - 2, barY - 2, barX + barWidth + 2, barY + barHeight + 2, 0x80000000);

        int fillWidth = (int) (barWidth * (corruption / 100.0));
        gui.fill(barX, barY, barX + fillWidth, barY + barHeight, colorWithAlpha);

        String text = "污染等级: " + corruption + "% (" + level.name() + ")";
        gui.drawString(mc.font, text, barX, barY - 12, color, true);

        String distanceText = String.format("距离: %.1fm", distance);
        gui.drawString(mc.font, distanceText, barX, barY + barHeight + 2, 0xFFFFFF, true);

        if (level == ManaCorruptionManager.CorruptionLevel.EXTREME) {
            if (mc.level.getGameTime() % 20 < 10) {
                String warning = "§c§l警告: 极度污染!";
                int warningX = screenWidth / 2 - mc.font.width(warning) / 2;
                gui.drawString(mc.font, warning, warningX, screenHeight / 2 - 50, 0xFFFF0000, true);
            }
        }
    }

    private int getCorruptionColor(ManaCorruptionManager.CorruptionLevel level) {
        switch (level) {
            case NONE: return 0x00FF00;      // 绿色
            case LOW: return 0xFFFF00;       // 黄色
            case MEDIUM: return 0xFFA500;    // 橙色
            case HIGH: return 0xFF0000;      // 红色
            case EXTREME: return 0x8B0000;   // 深红色
            default: return 0xFFFFFF;
        }
    }
}
