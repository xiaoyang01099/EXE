package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.PolychromeCollapsePrismTile;
import vazkii.botania.client.core.handler.ClientTickHandler;

import java.util.Random;
import javax.annotation.Nonnull;

/**
 * 增强版多色崩解棱镜渲染器
 * 结合植物魔法风格和更华丽的视觉效果
 */
public class PolychromeCollapsePrismRenderer implements BlockEntityRenderer<PolychromeCollapsePrismTile> {

    // 棱镜顶部覆盖纹理
    private static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation(
            ExEnigmaticlegacyMod.MODID, "textures/blocks/polychrome/polychrome_collapse_prism_overlay.png");

    // 符文纹理
    private static final ResourceLocation RUNE_TEXTURE = new ResourceLocation(
            ExEnigmaticlegacyMod.MODID, "textures/items/res/focus_infusion.png");

    // 光柱纹理
    private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation(
            "textures/entity/beacon_beam.png");

    // 粒子纹理
    private static final ResourceLocation SPARKLE_TEXTURE = new ResourceLocation(
            ExEnigmaticlegacyMod.MODID, "textures/particle/polychrome_sparkle.png");

    private final Random random = new Random();

    public PolychromeCollapsePrismRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull PolychromeCollapsePrismTile prism, float partialTicks, PoseStack ms,
                       MultiBufferSource buffers, int light, int overlay) {

        // 获取完成度
        float completion = prism.getCompletion();

        // 获取游戏时间
        Level level = prism.getLevel();
        long gameTime = level != null ? level.getGameTime() : 0;
        float animTime = ClientTickHandler.total();

        // 顶部覆盖纹理的透明度
        float alphaMod = Math.min(1.0F, completion / 0.1F);

        // 1. 渲染类似植物魔法的顶部覆盖纹理
        renderTerraPlateEffect(prism, partialTicks, ms, buffers, alphaMod, overlay);

        // 当有进度时渲染更多效果
        if (completion > 0) {
            ms.pushPose();

            // 偏移到方块中心，略高于方块顶部
            ms.translate(0.5F, 0.1F, 0.5F);

            // 2. 渲染浮动符文环
            if (completion > 0.05f) {
                renderRuneRing(prism, ms, buffers, gameTime, partialTicks, completion, light, overlay);
            }

            // 3. 渲染中心光柱
            if (completion > 0.2f) {
                renderCentralBeam(prism, ms, buffers, gameTime, partialTicks, completion, light, overlay);
            }

            // 4. 渲染连接光线
            if (completion > 0.5f) {
                renderConnectingBeams(prism, ms, buffers, gameTime, partialTicks, completion, light, overlay);
            }

            // 5. 渲染漂浮颗粒
            renderSparkles(prism, ms, buffers, animTime, partialTicks, completion, light, overlay);

            ms.popPose();
        }
    }

    /**
     * 渲染类似泰拉板的顶部效果
     */
    private void renderTerraPlateEffect(PolychromeCollapsePrismTile prism, float partialTicks, PoseStack ms,
                                        MultiBufferSource buffers, float alphaMod, int overlay) {
        ms.pushPose();

        // 旋转为平面
        ms.translate(0.5F, 1.01F / 16F, 0.5F);
        ms.mulPose(Vector3f.XP.rotationDegrees(90F));

        // 创建呼吸式闪烁效果
        float alpha = (float) ((Math.sin((ClientTickHandler.ticksInGame + partialTicks) / 8D) + 1D) / 5D + 0.6D) * alphaMod;

        // 创建旋转效果
        ms.mulPose(Vector3f.ZP.rotationDegrees((ClientTickHandler.ticksInGame + partialTicks) * 0.5F % 360F));

        // 获取渲染缓冲
        VertexConsumer buffer = buffers.getBuffer(RenderType.text(OVERLAY_TEXTURE));

        // 渲染覆盖纹理
        float size = 0.9F; // 稍小于方块
        renderSquare(ms, buffer, size, 1.0F, 1.0F, 1.0F, alpha);

        ms.popPose();
    }

    /**
     * 渲染浮动符文环
     */
    private void renderRuneRing(PolychromeCollapsePrismTile prism, PoseStack ms, MultiBufferSource buffers,
                                long gameTime, float partialTicks, float completion, int light, int overlay) {
        ms.pushPose();

        // 符文环旋转
        float angle = (gameTime + partialTicks) / 20.0F;
        ms.mulPose(Vector3f.YP.rotationDegrees(angle * 3));

        // 符文环大小随完成度缓慢增大
        float scale = 1.0F + 0.3F * Mth.sin((gameTime + partialTicks) / 20.0F);
        scale *= Math.min(1.0F, completion * 2.0F);

        ms.scale(scale, scale, scale);

        // 获取渲染类型
        VertexConsumer vertexConsumer = buffers.getBuffer(RenderType.text(RUNE_TEXTURE));
        Matrix4f matrix4f = ms.last().pose();

        // 渲染8个符文，均匀分布在一个圆环上
        int runeCount = 8;
        for (int i = 0; i < runeCount; i++) {
            float runeAngle = i * (float) Math.PI * 2.0F / runeCount;
            float radius = 0.8F + 0.05F * Mth.sin((gameTime + i * 100) / 400.0F);
            float x = Mth.sin(runeAngle) * radius;
            float z = Mth.cos(runeAngle) * radius;

            float runeSize = 0.25F;

            // 符文的位置随时间有轻微波动
            float offsetY = 0.1F * Mth.sin((gameTime + i * 100) / 10.0F);

            // 彩虹渐变色
            float hue = ((gameTime / 20.0F + i * (1.0F / runeCount)) % 1.0F);
            float[] rgb = hsvToRgb(hue, 0.9F, 1.0F);
            float alpha = (0.5F + 0.5F * Mth.sin((gameTime + i * 100) / 200.0F)) * completion;

            ms.pushPose();
            ms.translate(x, offsetY, z);

            // 使符文面向中心
            float yRot = (float) Math.toDegrees(Math.atan2(x, z));
            ms.mulPose(Vector3f.YP.rotationDegrees(yRot));

            // 渲染符文四边形
            vertexConsumer.vertex(matrix4f, -runeSize, -runeSize, 0).color(rgb[0], rgb[1], rgb[2], alpha).uv(0, 0).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
            vertexConsumer.vertex(matrix4f, -runeSize, runeSize, 0).color(rgb[0], rgb[1], rgb[2], alpha).uv(0, 1).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
            vertexConsumer.vertex(matrix4f, runeSize, runeSize, 0).color(rgb[0], rgb[1], rgb[2], alpha).uv(1, 1).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
            vertexConsumer.vertex(matrix4f, runeSize, -runeSize, 0).color(rgb[0], rgb[1], rgb[2], alpha).uv(1, 0).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();

            ms.popPose();
        }

        ms.popPose();
    }

    /**
     * 渲染中心光柱
     */
    private void renderCentralBeam(PolychromeCollapsePrismTile prism, PoseStack ms, MultiBufferSource buffers,
                                   long gameTime, float partialTicks, float completion, int light, int overlay) {
        ms.pushPose();

        // 光束高度随完成度增加
        float height = 6.0F * completion;

        // 光束颜色随时间变化 - 彩虹渐变
        float hue = (gameTime % 120) / 120.0F;
        float[] rgb = hsvToRgb(hue, 0.8F, 1.0F);
        float alpha = 0.7F * completion;

        // 获取渲染类型 - 使用信标光束渲染类型
        VertexConsumer vertexConsumer = buffers.getBuffer(RenderType.beaconBeam(BEAM_TEXTURE, true));
        Matrix4f matrix4f = ms.last().pose();

        // 渲染一个上升的光柱
        float beamWidth = 0.15F + 0.1F * completion;

        // 调整位置，从底部向上延伸
        ms.translate(0, 0, 0);

        // 添加旋转效果
        ms.mulPose(Vector3f.YP.rotationDegrees((gameTime / 2) % 360));

        // 光束底部
        vertexConsumer.vertex(matrix4f, -beamWidth, 0, -beamWidth).color(rgb[0], rgb[1], rgb[2], alpha).uv(0, 0).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, -beamWidth, 0, beamWidth).color(rgb[0], rgb[1], rgb[2], alpha).uv(0, 1).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, beamWidth, 0, beamWidth).color(rgb[0], rgb[1], rgb[2], alpha).uv(1, 1).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, beamWidth, 0, -beamWidth).color(rgb[0], rgb[1], rgb[2], alpha).uv(1, 0).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();

        // 光束顶部
        vertexConsumer.vertex(matrix4f, -beamWidth, height, -beamWidth).color(rgb[0], rgb[1], rgb[2], alpha).uv(0, 0).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, -beamWidth, height, beamWidth).color(rgb[0], rgb[1], rgb[2], alpha).uv(0, 1).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, beamWidth, height, beamWidth).color(rgb[0], rgb[1], rgb[2], alpha).uv(1, 1).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, beamWidth, height, -beamWidth).color(rgb[0], rgb[1], rgb[2], alpha).uv(1, 0).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();

        ms.popPose();

        // 渲染第二个反向旋转的光柱，增加视觉效果
        ms.pushPose();
        ms.translate(0, 0, 0);
        ms.mulPose(Vector3f.YP.rotationDegrees(-((gameTime / 3) % 360)));

        float innerBeamWidth = beamWidth * 0.6F;
        float innerAlpha = alpha * 1.3F;

        // 内层光束 - 使用反相颜色
        float[] innerRgb = hsvToRgb((hue + 0.5F) % 1.0F, 0.9F, 1.0F);

        // 光束底部
        vertexConsumer.vertex(matrix4f, -innerBeamWidth, 0, -innerBeamWidth).color(innerRgb[0], innerRgb[1], innerRgb[2], innerAlpha).uv(0, 0).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, -innerBeamWidth, 0, innerBeamWidth).color(innerRgb[0], innerRgb[1], innerRgb[2], innerAlpha).uv(0, 1).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, innerBeamWidth, 0, innerBeamWidth).color(innerRgb[0], innerRgb[1], innerRgb[2], innerAlpha).uv(1, 1).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, innerBeamWidth, 0, -innerBeamWidth).color(innerRgb[0], innerRgb[1], innerRgb[2], innerAlpha).uv(1, 0).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();

        // 光束顶部
        vertexConsumer.vertex(matrix4f, -innerBeamWidth, height, -innerBeamWidth).color(innerRgb[0], innerRgb[1], innerRgb[2], innerAlpha).uv(0, 0).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, -innerBeamWidth, height, innerBeamWidth).color(innerRgb[0], innerRgb[1], innerRgb[2], innerAlpha).uv(0, 1).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, innerBeamWidth, height, innerBeamWidth).color(innerRgb[0], innerRgb[1], innerRgb[2], innerAlpha).uv(1, 1).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, innerBeamWidth, height, -innerBeamWidth).color(innerRgb[0], innerRgb[1], innerRgb[2], innerAlpha).uv(1, 0).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();

        ms.popPose();
    }

    /**
     * 渲染连接到多方块结构其他部分的光线
     */
    private void renderConnectingBeams(PolychromeCollapsePrismTile prism, PoseStack ms, MultiBufferSource buffers,
                                       long gameTime, float partialTicks, float completion, int light, int overlay) {
        // 使用随机数生成器确保每次相同的位置
        random.setSeed(prism.getBlockPos().asLong());

        // 渲染连接光束 - 使用闪电渲染类型
        VertexConsumer vertexConsumer = buffers.getBuffer(RenderType.lightning());

        // 创建射向结构关键点的光线
        int beamCount = 8 + Math.round(completion * 8); // 随着完成度增加光线数量
        for (int i = 0; i < beamCount; i++) {
            ms.pushPose();

            // 计算目标点的位置（以棱镜为中心的多方块结构中的某个点）
            float angle = i * (float) Math.PI * 2.0F / beamCount + ((gameTime / 400.0F) % (float) Math.PI * 2.0F);

            // 距离中心的距离
            float distance = 1.5F + random.nextFloat() * 2.5F;

            // 目标位置
            float targetX = Mth.sin(angle) * distance;
            float targetY = -0.1F + random.nextFloat() * 0.3F - 0.15F;
            float targetZ = Mth.cos(angle) * distance;

            // 光线振幅
            float amplitude = 0.05F + 0.05F * Mth.sin((gameTime + i * 100) / 10.0F);

            // 光线颜色 - 彩虹渐变
            float hue = ((gameTime / 50.0F + i * (1.0F / beamCount)) % 1.0F);
            float[] rgb = hsvToRgb(hue, 0.8F, 1.0F);
            float alpha = (0.5F + 0.3F * Mth.sin((gameTime + i * 100) / 200.0F)) * completion;

            // 光线段数
            int segments = 8 + Math.round(distance * 3);

            Matrix4f matrix4f = ms.last().pose();

            for (int j = 0; j < segments; j++) {
                float t1 = (float) j / segments;
                float t2 = (float) (j + 1) / segments;

                // 振荡幅度随距离增加
                float waveAmplitude = amplitude * t1 * 6.0F;

                // 光线路径公式 - 使用二次贝塞尔曲线
                float x1 = targetX * t1 * t1;
                float y1 = targetY * t1 + 0.2F * (1 - t1); // 从略高于中心点向目标移动
                float z1 = targetZ * t1 * t1;

                float x2 = targetX * t2 * t2;
                float y2 = targetY * t2 + 0.2F * (1 - t2);
                float z2 = targetZ * t2 * t2;

                // 添加正弦波动使光线看起来更为不规则
                float phase = (gameTime / 50.0F) % (float) Math.PI * 2.0F;
                float offX1 = waveAmplitude * Mth.sin(t1 * 30.0F + phase);
                float offZ1 = waveAmplitude * Mth.cos(t1 * 30.0F + phase);
                float offX2 = waveAmplitude * Mth.sin(t2 * 30.0F + phase);
                float offZ2 = waveAmplitude * Mth.cos(t2 * 30.0F + phase);

                // 光线宽度
                float width = 0.03F;

                // 颜色沿光线渐变
                float segmentHue = (hue + t1 * 0.5F) % 1.0F;
                float[] segmentRgb = hsvToRgb(segmentHue, 0.8F, 1.0F);
                float segmentAlpha = alpha * (1.0F - t1 * 0.5F); // 靠近源头更亮

                // 渲染线段
                vertexConsumer.vertex(matrix4f, x1 + offX1, y1, z1 + offZ1).color(segmentRgb[0], segmentRgb[1], segmentRgb[2], segmentAlpha).uv(0, 0).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
                vertexConsumer.vertex(matrix4f, x1 + offX1, y1 + width, z1 + offZ1).color(segmentRgb[0], segmentRgb[1], segmentRgb[2], segmentAlpha).uv(0, 1).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
                vertexConsumer.vertex(matrix4f, x2 + offX2, y2 + width, z2 + offZ2).color(segmentRgb[0], segmentRgb[1], segmentRgb[2], segmentAlpha).uv(1, 1).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
                vertexConsumer.vertex(matrix4f, x2 + offX2, y2, z2 + offZ2).color(segmentRgb[0], segmentRgb[1], segmentRgb[2], segmentAlpha).uv(1, 0).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
            }

            ms.popPose();
        }
    }

    /**
     * 渲染漂浮颗粒
     */
    private void renderSparkles(PolychromeCollapsePrismTile prism, PoseStack ms, MultiBufferSource buffers,
                                float animTime, float partialTicks, float completion, int light, int overlay) {
        random.setSeed(prism.getBlockPos().asLong() + (long)(animTime * 10));

        // 获取渲染缓冲
        VertexConsumer buffer = buffers.getBuffer(RenderType.text(SPARKLE_TEXTURE));
        Matrix4f matrix4f = ms.last().pose();

        // 根据完成度渲染更多颗粒
        int particleCount = 5 + Math.round(completion * 20);

        for (int i = 0; i < particleCount; i++) {
            ms.pushPose();

            // 颗粒位置计算 - 在圆形区域内随机分布，高度随时间变化
            float radius = 0.8F * random.nextFloat();
            float angle = (float) (random.nextFloat() * Math.PI * 2);
            float x = Mth.sin(angle) * radius;
            float z = Mth.cos(angle) * radius;

            // 高度基于sin函数，但每个颗粒有不同的周期
            float period = 100 + random.nextFloat() * 100;
            float heightOffset = (i % 3) * 0.1F; // 层级化展示
            float y = 0.2F + heightOffset + 0.2F * Mth.sin((animTime + i * 30) / period);

            // 移动到颗粒位置
            ms.translate(x, y, z);

            // 使颗粒始终面向摄像机
            ms.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());

            // 颗粒大小随机但与完成度有关
            float size = (0.03F + random.nextFloat() * 0.02F) * (0.8F + completion * 0.4F);
            ms.scale(size, size, size);

            // 颗粒颜色 - 根据位置和时间变化
            float hue = ((animTime / 100.0F + x * 0.1F + z * 0.1F) % 1.0F);
            float[] rgb = hsvToRgb(hue, 0.7F, 1.0F);

            // 透明度随时间脉动
            float alpha = (0.6F + 0.4F * Mth.sin((animTime + i * 20) / (period / 3))) * completion;

            // 渲染颗粒
            buffer.vertex(matrix4f, -1, -1, 0).color(rgb[0], rgb[1], rgb[2], alpha).uv(0, 0).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
            buffer.vertex(matrix4f, -1, 1, 0).color(rgb[0], rgb[1], rgb[2], alpha).uv(0, 1).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
            buffer.vertex(matrix4f, 1, 1, 0).color(rgb[0], rgb[1], rgb[2], alpha).uv(1, 1).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
            buffer.vertex(matrix4f, 1, -1, 0).color(rgb[0], rgb[1], rgb[2], alpha).uv(1, 0).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();

            ms.popPose();
        }
    }

    /**
     * 渲染一个带颜色的正方形
     */
    private void renderSquare(PoseStack ms, VertexConsumer buffer, float size, float r, float g, float b, float alpha) {
        float halfSize = size / 2;
        Matrix4f matrix4f = ms.last().pose();

        // 添加缺少的顶点属性：光照值和法线向量
        int lightValue = 0xF000F0; // 使用最大亮度

        // 渲染四边形，确保所有属性都被设置
        buffer.vertex(matrix4f, -halfSize, -halfSize, 0)
                .color(r, g, b, alpha)
                .uv(0, 0)
                .uv2(lightValue) // 光照值
                .normal(0, 0, 1) // 法线向量
                .endVertex();

        buffer.vertex(matrix4f, -halfSize, halfSize, 0)
                .color(r, g, b, alpha)
                .uv(0, 1)
                .uv2(lightValue)
                .normal(0, 0, 1)
                .endVertex();

        buffer.vertex(matrix4f, halfSize, halfSize, 0)
                .color(r, g, b, alpha)
                .uv(1, 1)
                .uv2(lightValue)
                .normal(0, 0, 1)
                .endVertex();

        buffer.vertex(matrix4f, halfSize, -halfSize, 0)
                .color(r, g, b, alpha)
                .uv(1, 0)
                .uv2(lightValue)
                .normal(0, 0, 1)
                .endVertex();
    }

    /**
     * HSV颜色转RGB颜色
     */
    private float[] hsvToRgb(float h, float s, float v) {
        float[] rgb = new float[3];

        int i = (int)(h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);

        switch (i % 6) {
            case 0: rgb[0] = v; rgb[1] = t; rgb[2] = p; break;
            case 1: rgb[0] = q; rgb[1] = v; rgb[2] = p; break;
            case 2: rgb[0] = p; rgb[1] = v; rgb[2] = t; break;
            case 3: rgb[0] = p; rgb[1] = q; rgb[2] = v; break;
            case 4: rgb[0] = t; rgb[1] = p; rgb[2] = v; break;
            case 5: rgb[0] = v; rgb[1] = p; rgb[2] = q; break;
        }

        return rgb;
    }
}