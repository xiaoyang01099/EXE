package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;

public class testss {
    private static final ResourceLocation EYE_TEXTURE = new ResourceLocation("ex_enigmaticlegacy","textures/models/nebula_eyes.png"); // 自定义眼睛纹理
    public static void onPlayerRender(RenderPlayerEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);

        if (shouldRenderFace(helmet)) {
            float partialTick = event.getPartialTick();

            // 使用正确的头部旋转值
            float headYaw = Mth.lerp(partialTick, player.yHeadRotO, player.yHeadRot);
            float headPitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            // 调整到玩家头部位置（非眼睛位置）
            poseStack.translate(0, player.getEyeHeight(), 0); // 降低位置到面部区域
            // 应用头部旋转
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-headYaw));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(headPitch));

            // 渲染效果;
            renderEndPortalOnFace(poseStack, event.getMultiBufferSource(), partialTick);

            poseStack.popPose();
        }
    }
    private static void renderEndPortalOnFace(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks) {
        RenderType renderType = RenderType.endPortal();
        VertexConsumer buffer = bufferSource.getBuffer(renderType);

        poseStack.pushPose();

        // 将平面贴在玩家脸上，朝向玩家面朝方向
        float size = 0.6F; // 控制大小
        float offset = 0.2F; // 控制离脸的距离（不能太近否则会z-fighting）

        // 设置位置和旋转：将平面贴在玩家面前
        poseStack.translate(0, 0, offset); // 向前移动一点，避免z-fighting
        poseStack.scale(size, size, 1F); // 控制大小

        Matrix4f matrix = poseStack.last().pose();

        // 渲染一个竖直的平面
        renderFace(matrix, buffer,
                -0.5F, 0.5F,   // x 范围
                -0.5F, 0.5F,   // y 范围
                0, 0, 0, 0);   // z 全部相同（竖直面）

        poseStack.popPose();
    }

    private static void renderFace(Matrix4f matrix, VertexConsumer buffer, float x0, float x1, float y0, float y1, float z0, float z1, float z2, float z3) {
        buffer.vertex(matrix, x0, y0, z0).endVertex();
        buffer.vertex(matrix, x1, y0, z1).endVertex();
        buffer.vertex(matrix, x1, y1, z2).endVertex();
        buffer.vertex(matrix, x0, y1, z3).endVertex();
    }

    private static boolean shouldRenderFace(ItemStack helmet) {
        return true;
        //return helmet != null && helmet.getItem() == ModArmors.NEBULA_HELMET.get();
    }
/*    private static void renderEyes(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderTexture(0, EYE_TEXTURE);

        float time = 0;
        if (Minecraft.getInstance().player != null) {
            time = (Minecraft.getInstance().player.tickCount + partialTicks) * 0.1f;
        }
        float pulse = (Mth.sin(time) + 1) * 0.25f + 0.5f;

        // 获取彩虹色
        Color rgb = Color.getHSBColor(time % 1.0f, 1.0f, 1.0f);
        float r = rgb.getRed() / 255f;
        float g = rgb.getGreen() / 255f;
        float b = rgb.getBlue() / 255f;
        float alpha = 0.7f + pulse * 0.3f;

        poseStack.pushPose();
        // 应用眼睛位置的整体调整
        poseStack.translate(0, -0.35f, -0.25f); // 与背景同步
        poseStack.scale(0.4f, 0.4f, 0.4f); // 关键缩放

        // 双眼位置偏移
        float eyeSpacing = 0.5f; // 间距调整
        float eyeSize = 0.5f; // 单个眼睛大小
        poseStack.pushPose();
        poseStack.translate(-eyeSpacing, 0, 0);
        renderSingleEye(poseStack.last().pose(), bufferSource, r, g, b, alpha, eyeSize, FULL_BRIGHT);
        poseStack.popPose();

        poseStack.popPose();

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void renderSingleEye(Matrix4f matrix, MultiBufferSource bufferSource,
                                        float r, float g, float b, float alpha,
                                        float size, int packedLight) {
        float halfSize = size / 2;
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.eyes(EYE_TEXTURE));

        // 修正UV坐标匹配纹理
        buffer.vertex(matrix, -halfSize, -halfSize, 0)
                .color(r, g, b, alpha)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 0, 1)
                .endVertex();

        buffer.vertex(matrix, -halfSize, halfSize, 0)
                .color(r, g, b, alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 0, 1)
                .endVertex();

        buffer.vertex(matrix, halfSize, halfSize, 0)
                .color(r, g, b, alpha)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 0, 1)
                .endVertex();

        buffer.vertex(matrix, halfSize, -halfSize, 0)
                .color(r, g, b, alpha)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 0, 1)
                .endVertex();
    }*/
}
