package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.others;

import java.util.Random;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ParticleEngine;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx.UtilsFX;

public class RenderEldritchOrb extends EntityRenderer<Entity> {
    private Random random = new Random();

    private static final int[] COLORS = new int[]{
            16777215, 16777086, 16727041, 37119, 40960, 15650047, 5592439
    };

    public RenderEldritchOrb(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(Entity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       net.minecraft.client.renderer.MultiBufferSource bufferSource, int packedLight) {
        this.renderEntityAt(entity, poseStack, partialTicks);
    }

    public void renderEntityAt(Entity entity, PoseStack poseStack, float partialTicks) {
        this.random.setSeed(187L);

        poseStack.pushPose();

        float f1 = (float)entity.tickCount / 80.0F;
        float f3 = 0.9F;
        float f2 = 0.0F;
        Random random = new Random((long)entity.getId());

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        poseStack.pushPose();

        for(int i = 0; i < 12; ++i) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F + f1 * 360.0F));

            buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

            float fa = random.nextFloat() * 20.0F + 5.0F + f2 * 10.0F;
            float f4 = random.nextFloat() * 2.0F + 1.0F + f2 * 2.0F;
            fa /= 30.0F / ((float)Math.min(entity.tickCount, 10) / 10.0F);
            f4 /= 30.0F / ((float)Math.min(entity.tickCount, 10) / 10.0F);

            Matrix4f matrix = poseStack.last().pose();

            int centerColor = 16777215;
            float centerAlpha = 1.0F - f2;
            buffer.vertex(matrix, 0.0F, 0.0F, 0.0F).color(
                    (centerColor >> 16 & 255) / 255.0F,
                    (centerColor >> 8 & 255) / 255.0F,
                    (centerColor & 255) / 255.0F,
                    centerAlpha
            ).endVertex();

            int outerColor = COLORS.length > 5 ? COLORS[5] : COLORS[0];
            float outerAlpha = 0.0F;

            buffer.vertex(matrix, (float)(-0.866 * f4), fa, (float)(-0.5F * f4)).color(
                    (outerColor >> 16 & 255) / 255.0F,
                    (outerColor >> 8 & 255) / 255.0F,
                    (outerColor & 255) / 255.0F,
                    outerAlpha
            ).endVertex();

            buffer.vertex(matrix, (float)(0.866 * f4), fa, (float)(-0.5F * f4)).color(
                    (outerColor >> 16 & 255) / 255.0F,
                    (outerColor >> 8 & 255) / 255.0F,
                    (outerColor & 255) / 255.0F,
                    outerAlpha
            ).endVertex();

            buffer.vertex(matrix, 0.0F, fa, (float)(1.0F * f4)).color(
                    (outerColor >> 16 & 255) / 255.0F,
                    (outerColor >> 8 & 255) / 255.0F,
                    (outerColor & 255) / 255.0F,
                    outerAlpha
            ).endVertex();

            buffer.vertex(matrix, (float)(-0.866 * f4), fa, (float)(-0.5F * f4)).color(
                    (outerColor >> 16 & 255) / 255.0F,
                    (outerColor >> 8 & 255) / 255.0F,
                    (outerColor & 255) / 255.0F,
                    outerAlpha
            ).endVertex();

            tessellator.end();
        }

        poseStack.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();

        poseStack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 771);

        UtilsFX.bindTexture(ParticleEngine.PARTICLE_TEXTURE);

        f2 = (float)(entity.tickCount % 13) / 16.0F;
        f3 = f2 + 0.0624375F;
        float f4 = 0.1875F;
        float f5 = f4 + 0.0624375F;
        float f6 = 1.0F;
        float f7 = 0.5F;
        float f8 = 0.5F;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - this.entityRenderDispatcher.camera.getYRot()));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-this.entityRenderDispatcher.camera.getXRot()));
        poseStack.scale(1.0F, 1.0F, 1.0F);

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        buffer.vertex(matrix, 0.0F - f7, 0.0F - f8, 0.0F).uv(f2, f5).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, f6 - f7, 0.0F - f8, 0.0F).uv(f3, f5).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, f6 - f7, 1.0F - f8, 0.0F).uv(f3, f4).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, 0.0F - f7, 1.0F - f8, 0.0F).uv(f2, f4).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();

        tessellator.end();

        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return ParticleEngine.PARTICLE_TEXTURE;
    }
}