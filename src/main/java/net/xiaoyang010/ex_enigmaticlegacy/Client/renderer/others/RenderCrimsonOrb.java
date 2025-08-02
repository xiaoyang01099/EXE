package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.others;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ParticleEngine;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityCrimsonOrb;

public class RenderCrimsonOrb extends EntityRenderer<EntityCrimsonOrb> {
    private static final ResourceLocation CRIMSON_ORB_TEXTURE = ParticleEngine.PARTICLE_TEXTURE;

    public RenderCrimsonOrb(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(EntityCrimsonOrb entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        poseStack.pushPose();

        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));

        float bob = Mth.sin((float)entity.tickCount / 5.0F) * 0.2F + 0.2F;
        float scale = 1.0F + bob;
        poseStack.scale(scale, scale, scale);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));

        float frameIndex = (float)(1 + entity.tickCount % 6);
        float u1 = frameIndex / 8.0F;
        float u2 = u1 + 0.125F;

        float v1, v2;
        if (entity.isRed()) {
            v1 = 0.75F;
            v2 = v1 + 0.125F;
        } else {
            v1 = 0.875F;
            v2 = v1 + 0.125F;
        }

        PoseStack.Pose pose = poseStack.last();
        float size = 0.5F;

        float red = 1.0F;
        float green = 1.0F;
        float blue = 1.0F;
        float alpha = 0.8F;

        if (entity.isRed()) {
            red = 1.0F;
            green = 0.3F;
            blue = 0.3F;
        }

        vertexConsumer.vertex(pose.pose(), -size, -size, 0.0F)
                .color(red, green, blue, alpha)
                .uv(u1, v2)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(pose.pose(), size, -size, 0.0F)
                .color(red, green, blue, alpha)
                .uv(u2, v2)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(pose.pose(), size, size, 0.0F)
                .color(red, green, blue, alpha)
                .uv(u2, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(pose.pose(), -size, size, 0.0F)
                .color(red, green, blue, alpha)
                .uv(u1, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCrimsonOrb entity) {
        return CRIMSON_ORB_TEXTURE;
    }
}