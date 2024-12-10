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

        // 渲染光柱，如果有光柱目标
        BlockPos beamTarget = entity.getBeamTarget();
        if (beamTarget != null) {
            float f3 = (float) beamTarget.getX() + 0.5F;
            float f4 = (float) beamTarget.getY() + 0.5F;
            float f5 = (float) beamTarget.getZ() + 0.5F;
            float dx = f3 - (float) entity.getX();
            float dy = f4 - (float) entity.getY();
            float dz = f5 - (float) entity.getZ();
            poseStack.translate(dx, dy, dz);
            // 渲染光束逻辑（可扩展）
        }

        poseStack.popPose();
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
