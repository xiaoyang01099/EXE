package org.xiaoyang.ex_enigmaticlegacy.Event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.xiaoyang.ex_enigmaticlegacy.ConfigHandler;
import org.xiaoyang.ex_enigmaticlegacy.Font.ModRarities;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ItemEntityNameplateHandler {

    private static final List<PendingLabel> pendingLabels = new ArrayList<>();
    private static Matrix4f cachedViewProjMatrix = null;

    private static class PendingLabel {
        final ItemStack stack;
        final double worldX, worldY, worldZ;
        final double distance;

        PendingLabel(ItemStack stack, double x, double y, double z, double dist) {
            this.stack = stack;
            this.worldX = x;
            this.worldY = y;
            this.worldZ = z;
            this.distance = dist;
        }
    }

    @SubscribeEvent
    public static void onRenderLevelLast(RenderLevelStageEvent event) {
        if (!ConfigHandler.ItemNameplateConfig.isEnabled()) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        pendingLabels.clear();
        cachedViewProjMatrix = null;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        float partialTick = event.getPartialTick();

        Matrix4f view = new Matrix4f(event.getPoseStack().last().pose());
        Matrix4f proj = new Matrix4f(RenderSystem.getProjectionMatrix());
        proj.mul(view);
        cachedViewProjMatrix = proj;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity itemEntity)) continue;

            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty()) continue;
            if (!ModRarities.shouldAnimate(stack)) continue;

            double x = lerp(itemEntity.xOld, itemEntity.getX(), partialTick);
            double y = lerp(itemEntity.yOld, itemEntity.getY(), partialTick)
                    + itemEntity.getBbHeight() + 0.5;
            double z = lerp(itemEntity.zOld, itemEntity.getZ(), partialTick);

            double dist = camPos.distanceTo(new Vec3(x, y, z));
            if (dist > 64.0) continue;

            pendingLabels.add(new PendingLabel(
                    stack,
                    x - camPos.x, y - camPos.y, z - camPos.z,
                    dist
            ));
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!ConfigHandler.ItemNameplateConfig.isEnabled()) return;
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;
        if (pendingLabels.isEmpty() || cachedViewProjMatrix == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            pendingLabels.clear();
            return;
        }

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        Font font = mc.font;
        GuiGraphics guiGraphics = event.getGuiGraphics();

        for (PendingLabel label : pendingLabels) {
            Vector4f clip = new Vector4f(
                    (float) label.worldX,
                    (float) label.worldY,
                    (float) label.worldZ,
                    1.0F
            );
            cachedViewProjMatrix.transform(clip);

            if (clip.w() <= 0.0F) continue;

            float ndcX = clip.x() / clip.w();
            float ndcY = clip.y() / clip.w();

            if (ndcX < -1.5F || ndcX > 1.5F || ndcY < -1.5F || ndcY > 1.5F) continue;

            float scrX = (ndcX * 0.5F + 0.5F) * screenW;
            float scrY = (1.0F - (ndcY * 0.5F + 0.5F)) * screenH;

            int alpha;
            if (label.distance < 48.0) {
                alpha = 255;
            } else {
                alpha = (int) (255 * (1.0 - (label.distance - 48.0) / 16.0));
            }
            if (alpha <= 4) continue;
            alpha = Math.min(255, Math.max(4, alpha));

            String raw = ChatFormatting.stripFormatting(
                    label.stack.getHoverName().getString());
            if (raw == null || raw.isEmpty()) continue;

            int textW = font.width(raw);
            int drawX = (int) (scrX - textW / 2.0F);
            int drawY = (int) (scrY - font.lineHeight / 2.0F);

            guiGraphics.pose().pushPose();
            ModRarities.drawWaveName(guiGraphics, font, label.stack, drawX, drawY, alpha);
            guiGraphics.pose().popPose();
        }

        pendingLabels.clear();
    }

    private static double lerp(double a, double b, float t) {
        return a + (b - a) * t;
    }
}