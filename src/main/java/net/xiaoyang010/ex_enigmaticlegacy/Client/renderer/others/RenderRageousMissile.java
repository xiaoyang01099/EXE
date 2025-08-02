package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.others;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ParticleEngine;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityRageousMissile;

import javax.annotation.Nonnull;
import java.util.Random;

public class RenderRageousMissile extends EntityRenderer<EntityRageousMissile> {
    private static final ResourceLocation ORB_TEXTURE = ParticleEngine.PARTICLE_TEXTURE;
    private final Random random = new Random();

    public RenderRageousMissile(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(@Nonnull EntityRageousMissile entity, float yaw, float partialTicks,
                       @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int light) {

        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);


        float frameTime = (float)(1 + entity.tickCount % 6) / 8.0F;
        float minU = frameTime;
        float maxU = frameTime + 0.125F;

        float minV = entity.isRed() ? 0.75F : 0.875F;
        float maxV = minV + 0.125F;

        float alpha = 0.8F;

        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));

        float bob = Mth.sin((float)entity.tickCount / 5.0F) * 0.2F + 0.2F;
        float scale = 1.0F + bob;
        poseStack.scale(scale, scale, scale);

        RenderType renderType = RenderType.entityTranslucent(ORB_TEXTURE);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();

        int brightness = 220;

        addVertex(vertexConsumer, matrix4f, matrix3f, -0.5F, -0.5F, 0.0F, minU, maxV, brightness, alpha);
        addVertex(vertexConsumer, matrix4f, matrix3f,  0.5F, -0.5F, 0.0F, maxU, maxV, brightness, alpha);
        addVertex(vertexConsumer, matrix4f, matrix3f,  0.5F,  0.5F, 0.0F, maxU, minV, brightness, alpha);
        addVertex(vertexConsumer, matrix4f, matrix3f, -0.5F,  0.5F, 0.0F, minU, minV, brightness, alpha);

        poseStack.popPose();
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normalMatrix,
                           float x, float y, float z, float u, float v, int light, float alpha) {
        consumer.vertex(matrix, x, y, z)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normalMatrix, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull EntityRageousMissile entity) {
        return ORB_TEXTURE;
    }
}