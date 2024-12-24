package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.SpectriteCrystalEntity;

public class SpectriteCrystalRenderer extends EntityRenderer<SpectriteCrystalEntity> {

    // 定义动画帧的纹理数组
    private static final ResourceLocation[] SPECTRITE_CRYSTAL_TEXTURES = new ResourceLocation[]{
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/0.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/1.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/2.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/3.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/4.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/5.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/6.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/7.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/8.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/9.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/10.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/11.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/12.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/13.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/14.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/15.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/16.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/17.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/18.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/19.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/20.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/21.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/22.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/23.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/24.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/25.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/26.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/27.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/28.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/29.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/30.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/31.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/32.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/33.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/34.png"),
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/spectrite_crystal/35.png")
    };

    // 每帧持续时间（与mcmeta文件中的frametime相对应）
    private static final int FRAME_DURATION = 2;

    private final ModelPart glass;
    private final ModelPart cube;
    private final ModelPart base;

    public SpectriteCrystalRenderer(EntityRendererProvider.Context context) {
        super(context);
        ModelPart modelpart = context.bakeLayer(ModelLayers.END_CRYSTAL);
        this.glass = modelpart.getChild("glass");
        this.cube = modelpart.getChild("cube");
        this.base = modelpart.getChild("base");
    }

    private int getCurrentFrame(float gameTime, float partialTicks) {
        // 计算帧的基础索引
        float interpolatedTime = (gameTime + partialTicks) / FRAME_DURATION;
        return (int) (interpolatedTime % SPECTRITE_CRYSTAL_TEXTURES.length);
    }

    private float getInterpolationFactor(float gameTime, float partialTicks) {
        // 获取当前的插值系数，用于在帧之间平滑过渡
        float interpolatedTime = (gameTime + partialTicks) / FRAME_DURATION;
        return interpolatedTime - (int) interpolatedTime;  // 保留小数部分，表示两个帧之间的过渡百分比
    }

    @Override
    public void render(SpectriteCrystalEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // 获取游戏时间的帧和插值
        float gameTime = entity.level.getGameTime();
        int currentFrame = getCurrentFrame(gameTime, partialTicks);
        float interpolationFactor = getInterpolationFactor(gameTime, partialTicks);

        // 获取当前帧和下一帧
        int nextFrame = (currentFrame + 1) % SPECTRITE_CRYSTAL_TEXTURES.length;

        // 在当前帧和下一帧之间插值
        ResourceLocation currentTexture = SPECTRITE_CRYSTAL_TEXTURES[currentFrame];
        ResourceLocation nextTexture = SPECTRITE_CRYSTAL_TEXTURES[nextFrame];

        // 渲染当前帧
        RenderType renderType = RenderType.entityCutoutNoCull(currentTexture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        // 自定义渲染逻辑
        float f = calculateCustomY(entity, partialTicks);
        float rotation = ((float) entity.tickCount + partialTicks) * 3.0F;

        poseStack.pushPose();
        poseStack.scale(2.0F, 2.0F, 2.0F);
        poseStack.translate(0.0D, -0.5D, 0.0D);

        int overlay = OverlayTexture.NO_OVERLAY;
        this.base.render(poseStack, vertexConsumer, packedLight, overlay);

        poseStack.mulPose(Vector3f.YP.rotationDegrees(rotation));
        poseStack.translate(0.0D, (double) (1.5F + f / 2.0F), 0.0D);
        poseStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));

        this.glass.render(poseStack, vertexConsumer, packedLight, overlay);
        poseStack.scale(0.875F, 0.875F, 0.875F);
        poseStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(rotation));

        this.glass.render(poseStack, vertexConsumer, packedLight, overlay);
        poseStack.scale(0.875F, 0.875F, 0.875F);
        poseStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(rotation));

        this.cube.render(poseStack, vertexConsumer, packedLight, overlay);
        poseStack.popPose();

        /// 添加光束渲染代码
        if (entity.healingTarget != null) {
            float targetX = (float)entity.healingTarget.getX();
            float targetY = (float)entity.healingTarget.getY() + entity.healingTarget.getBbHeight() / 2;
            float targetZ = (float)entity.healingTarget.getZ();
            float sourceX = (float)entity.getX();
            float sourceY = (float)entity.getY() + 1.0F;
            float sourceZ = (float)entity.getZ();

            float dx = targetX - sourceX;
            float dy = targetY - sourceY;
            float dz = targetZ - sourceZ;

            // 渲染彩虹光束
            renderBeam(poseStack, bufferSource, partialTicks, entity.tickCount,
                    sourceX, sourceY, sourceZ,
                    dx, dy, dz,
                    packedLight);
        }

        poseStack.popPose();
    }

    private void renderBeam(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks,
                            int tickCount, float sourceX, float sourceY, float sourceZ,
                            float dx, float dy, float dz, int packedLight) {
        float length = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);

        poseStack.pushPose();
        poseStack.translate(sourceX, sourceY, sourceZ);

        // 计算旋转角度
        float yRot = (float)Math.atan2(dz, dx);
        float xRot = (float)Math.atan2(dy, Math.sqrt(dx * dx + dz * dz));
        poseStack.mulPose(Vector3f.YP.rotation(-yRot));
        poseStack.mulPose(Vector3f.XP.rotation(xRot));

        float time = (float)tickCount + partialTicks;

        // 渲染彩虹螺旋光束
        renderRainbowHelix(poseStack, bufferSource, time, length, packedLight);

        poseStack.popPose();
    }

    private void renderRainbowHelix(PoseStack poseStack, MultiBufferSource bufferSource,
                                    float time, float length, int packedLight) {
        PoseStack.Pose pose = poseStack.last();
        float beamWidth = 0.15F; // 光束宽度
        int segments = 60; // 螺旋段数
        float rotationSpeed = 0.5F; // 螺旋旋转速度
        float waveAmplitude = 0.3F; // 波浪幅度
        float waveFrequency = 3.0F; // 波浪频率

        // 彩虹颜色数组
        float[][] rainbowColors = {
                {1.0F, 0.0F, 0.0F}, // 红
                {1.0F, 0.5F, 0.0F}, // 橙
                {1.0F, 1.0F, 0.0F}, // 黄
                {0.0F, 1.0F, 0.0F}, // 绿
                {0.0F, 0.0F, 1.0F}, // 蓝
                {0.29F, 0.0F, 0.51F}, // 靛
                {0.58F, 0.0F, 0.83F}  // 紫
        };

        // 为每个螺旋线渲染一条彩虹光束
        for (int spiralIndex = 0; spiralIndex < 2; spiralIndex++) {
            float spiralOffset = spiralIndex * (float)Math.PI; // 两条螺旋线的相位差

            for (int i = 0; i < segments; i++) {
                float progress = i / (float)segments;
                float nextProgress = (i + 1) / (float)segments;

                // 计算螺旋线位置
                float angle1 = progress * 20.0F * (float)Math.PI + time * rotationSpeed + spiralOffset;
                float angle2 = nextProgress * 20.0F * (float)Math.PI + time * rotationSpeed + spiralOffset;

                float wave1 = Mth.sin(progress * waveFrequency * (float)Math.PI + time) * waveAmplitude;
                float wave2 = Mth.sin(nextProgress * waveFrequency * (float)Math.PI + time) * waveAmplitude;

                // 当前段的颜色
                int colorIndex = (int)((progress * rainbowColors.length + time * 0.5F) % rainbowColors.length);
                float[] color = rainbowColors[colorIndex];

                // 获取专门的光束渲染器
                VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lightning());

                // 渲染螺旋线段
                float x1 = Mth.cos(angle1) * beamWidth;
                float z1 = Mth.sin(angle1) * beamWidth;
                float x2 = Mth.cos(angle2) * beamWidth;
                float z2 = Mth.sin(angle2) * beamWidth;

                float y1 = progress * length + wave1;
                float y2 = nextProgress * length + wave2;

                // 添加顶点
                vertexConsumer.vertex(pose.pose(), x1, y1, z1)
                        .color(color[0], color[1], color[2], 0.7F)
                        .uv(0, 0)
                        .overlayCoords(OverlayTexture.NO_OVERLAY)
                        .uv2(packedLight)
                        .normal(pose.normal(), 1, 0, 0)
                        .endVertex();

                vertexConsumer.vertex(pose.pose(), x2, y2, z2)
                        .color(color[0], color[1], color[2], 0.7F)
                        .uv(0, 1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY)
                        .uv2(packedLight)
                        .normal(pose.normal(), 1, 0, 0)
                        .endVertex();

                // 添加连接线
                vertexConsumer.vertex(pose.pose(), x1, y1, z1)
                        .color(color[0], color[1], color[2], 0.7F)
                        .uv(0, 0)
                        .overlayCoords(OverlayTexture.NO_OVERLAY)
                        .uv2(packedLight)
                        .normal(pose.normal(), 0, 1, 0)
                        .endVertex();

                vertexConsumer.vertex(pose.pose(), x2, y2, z2)
                        .color(color[0], color[1], color[2], 0.7F)
                        .uv(1, 0)
                        .overlayCoords(OverlayTexture.NO_OVERLAY)
                        .uv2(packedLight)
                        .normal(pose.normal(), 0, 1, 0)
                        .endVertex();
            }
        }
    }

    private static float calculateCustomY(SpectriteCrystalEntity crystal, float partialTicks) {
        float f = (float) crystal.tickCount + partialTicks;
        float f1 = Mth.sin(f * 0.2F) / 2.0F + 0.5F;
        f1 = (f1 * f1 + f1) * 0.4F;
        return f1 - 1.4F;
    }

    @Override
    public ResourceLocation getTextureLocation(SpectriteCrystalEntity entity) {
        return SPECTRITE_CRYSTAL_TEXTURES[0]; // 默认的纹理占位
    }

    private static final float SIN_45 = (float) Math.sin(Math.PI / 4);
}
