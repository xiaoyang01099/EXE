package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.AlphirinePortal;
import vazkii.botania.client.core.handler.ClientTickHandler;

import java.util.Random;

public class AlphirinePortalRenderer extends EntityRenderer<AlphirinePortal> {
    private static final ResourceLocation PORTAL_TEXTURE = new ResourceLocation("ex_enigmaticlegacy", "textures/entity/alphirine_portal.png");
    private static final int ANIMATION_FRAMES = 16;
    private static final int FRAME_TIME = 3;

    public AlphirinePortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(AlphirinePortal entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        double worldTime = entity.getLevel() == null ? 0.0D : (double)(ClientTickHandler.ticksInGame + partialTicks);
        if (entity != null) {
            worldTime += (double)(new Random((long)((int)entity.getX() ^ (int)entity.getY() ^ (int)entity.getZ())).nextInt(360));
        }

        float burn = Math.min(1.0F, (float)entity.tickCount * 0.0561F);

        poseStack.pushPose();

        burn = Math.max(0.0F, (float)(burn + Math.sin(worldTime / 3.2D) / 9.0D));
        float scale = burn / 3.15F;
        poseStack.scale(scale, scale, scale);

        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-entity.getXRot()));

        poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)worldTime * 2.0F));

        renderPortalEffect(entity, poseStack, buffer, packedLight, burn, worldTime);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderPortalEffect(AlphirinePortal entity, PoseStack poseStack, MultiBufferSource buffer,
                                    int packedLight, float alpha, double worldTime) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(PORTAL_TEXTURE));

        float size = 0.5F;

        int currentFrame = (int)((worldTime / FRAME_TIME) % ANIMATION_FRAMES);
        float frameHeight = 1.0F / ANIMATION_FRAMES;

        float u0 = 0.0F;
        float v0 = currentFrame * frameHeight;
        float u1 = 1.0F;
        float v1 = (currentFrame + 1) * frameHeight;

        renderPortalQuad(vertexConsumer, poseStack, size, packedLight, alpha, 1.0F, 1.0F, 1.0F, u0, v0, u1, v1);
    }

    private void renderPortalQuad(VertexConsumer vertexConsumer, PoseStack poseStack, float size,
                                  int packedLight, float alpha, float r, float g, float b,
                                  float u0, float v0, float u1, float v1) {
        addVertex(vertexConsumer, poseStack, -size, -size, 0, u0, v0, packedLight, alpha, r, g, b);
        addVertex(vertexConsumer, poseStack, size, -size, 0, u1, v0, packedLight, alpha, r, g, b);
        addVertex(vertexConsumer, poseStack, size, size, 0, u1, v1, packedLight, alpha, r, g, b);
        addVertex(vertexConsumer, poseStack, -size, size, 0, u0, v1, packedLight, alpha, r, g, b);

        addVertex(vertexConsumer, poseStack, -size, size, 0, u0, v1, packedLight, alpha, r, g, b);
        addVertex(vertexConsumer, poseStack, size, size, 0, u1, v1, packedLight, alpha, r, g, b);
        addVertex(vertexConsumer, poseStack, size, -size, 0, u1, v0, packedLight, alpha, r, g, b);
        addVertex(vertexConsumer, poseStack, -size, -size, 0, u0, v0, packedLight, alpha, r, g, b);
    }

    private void addVertex(VertexConsumer vertexConsumer, PoseStack poseStack,
                           float x, float y, float z, float u, float v, int packedLight,
                           float alpha, float r, float g, float b) {
        vertexConsumer.vertex(poseStack.last().pose(), x, y, z)
                .color(r, g, b, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(poseStack.last().normal(), 0.0F, 0.0F, 1.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(AlphirinePortal entity) {
        return PORTAL_TEXTURE;
    }
}