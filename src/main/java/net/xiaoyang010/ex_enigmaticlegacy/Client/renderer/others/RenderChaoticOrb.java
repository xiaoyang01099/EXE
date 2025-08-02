package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.others;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ParticleEngine;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityChaoticOrb;
import org.lwjgl.opengl.GL11;
import java.util.Random;

public class RenderChaoticOrb extends EntityRenderer<EntityChaoticOrb> {

    private static final int[] COLORS = new int[]{
            16777215, 16777086, 16727041, 37119, 40960, 15650047, 5592439
    };

    public RenderChaoticOrb(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(EntityChaoticOrb entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        poseStack.pushPose();

        renderOrbSphere(entity, poseStack, partialTicks);
        renderOrbCore(entity, poseStack, partialTicks);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private void renderOrbSphere(EntityChaoticOrb entity, PoseStack poseStack, float partialTicks) {
        Random random = new Random(entity.getId());

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float f1 = (float)entity.tickCount / 80.0F;
        float f3 = 0.9F;
        float f2 = 0.0F;

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < 12; ++i) {
            poseStack.pushPose();

            poseStack.mulPose(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F + f1 * 360.0F));

            float fa = random.nextFloat() * 20.0F + 5.0F + f2 * 10.0F;
            float f4 = random.nextFloat() * 2.0F + 1.0F + f2 * 2.0F;

            fa /= 30.0F / ((float)Math.min(entity.tickCount, 10) / 10.0F);
            f4 /= 30.0F / ((float)Math.min(entity.tickCount, 10) / 10.0F);

            Matrix4f matrix = poseStack.last().pose();

            float centerAlpha = 1.0F - f2;

            int colorIndex = i / 2 + 1;
            int color = COLORS[colorIndex % COLORS.length];
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;

            buffer.vertex(matrix, 0.0F, 0.0F, 0.0F)
                    .color(1.0F, 1.0F, 1.0F, centerAlpha)
                    .endVertex();

            buffer.vertex(matrix, (float)(-0.866 * f4), fa, (float)(-0.5F * f4))
                    .color(r, g, b, 0.0F)
                    .endVertex();

            buffer.vertex(matrix, (float)(0.866 * f4), fa, (float)(-0.5F * f4))
                    .color(r, g, b, 0.0F)
                    .endVertex();

            poseStack.popPose();
        }

        tessellator.end();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableTexture();
    }

    private void renderOrbCore(EntityChaoticOrb entity, PoseStack poseStack, float partialTicks) {
        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.disableDepthTest();

        RenderSystem.setShaderTexture(0, ParticleEngine.PARTICLE_TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        float f2 = (float)(entity.tickCount % 13) / 16.0F;
        float f3 = f2 + 0.0625F;
        float f4 = 0.125F;
        float f5 = f4 + 0.0625F;

        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - this.entityRenderDispatcher.camera.getYRot()));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-this.entityRenderDispatcher.camera.getXRot()));
        poseStack.scale(0.5F, 0.5F, 0.5F);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        float alpha = 0.8F;

        buffer.vertex(matrix, -0.5F, -0.5F, 0.0F).uv(f2, f5).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, 0.5F, -0.5F, 0.0F).uv(f3, f5).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, 0.5F, 0.5F, 0.0F).uv(f3, f4).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix, -0.5F, 0.5F, 0.0F).uv(f2, f4).color(1.0F, 1.0F, 1.0F, alpha).endVertex();

        tessellator.end();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityChaoticOrb entity) {
        return ParticleEngine.PARTICLE_TEXTURE;
    }
}