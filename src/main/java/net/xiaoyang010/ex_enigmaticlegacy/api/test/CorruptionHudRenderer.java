package net.xiaoyang010.ex_enigmaticlegacy.api.test;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.res.ManaCorruptionManager;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.res.TileManaConverter;


/**
 * 污染等级 HUD 显示
 */
public class CorruptionHudRenderer {

    private static final int DETECTION_RANGE = 16;

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
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
            renderCorruptionWarning(event.getMatrixStack(), mc, maxCorruption, corruptionSource, playerPos);
        }
    }

    private void renderCorruptionWarning(PoseStack poseStack, Minecraft mc, int corruption, BlockPos source, BlockPos playerPos) {
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

        GuiComponent.fill(poseStack, barX - 2, barY - 2, barX + barWidth + 2, barY + barHeight + 2, 0x80000000);

        int fillWidth = (int) (barWidth * (corruption / 100.0));
        GuiComponent.fill(poseStack, barX, barY, barX + fillWidth, barY + barHeight, colorWithAlpha);

        String text = "污染等级: " + corruption + "% (" + level.name() + ")";
        mc.font.drawShadow(poseStack, text, barX, barY - 12, color);

        String distanceText = String.format("距离: %.1fm", distance);
        mc.font.drawShadow(poseStack, distanceText, barX, barY + barHeight + 2, 0xFFFFFF);

        if (level == ManaCorruptionManager.CorruptionLevel.EXTREME) {
            if (mc.level.getGameTime() % 20 < 10) {
                String warning = "§c§l警告: 极度污染!";
                int warningX = screenWidth / 2 - mc.font.width(warning) / 2;
                mc.font.drawShadow(poseStack, warning, warningX, screenHeight / 2 - 50, 0xFFFF0000);
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

