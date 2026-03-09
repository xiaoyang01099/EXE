package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.others;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntitySlimeCannonBall;

public class RendererSlimeCannonBall extends EntityRenderer<EntitySlimeCannonBall> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/entity/slime/slime.png");
    private final SlimeModel<EntitySlimeCannonBall> model;

    public RendererSlimeCannonBall(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME));
    }

    @Override
    public void render(EntitySlimeCannonBall entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float size = entity.getSlimeSize();
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));

        float scale = size * 0.999F;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0, -1.5, 0.0);

        model.prepareMobModel(entity, 0.0F, 0.0F, partialTick);
        model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTick, 0.0F, 0.0F);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        model.renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0.2F, 0.9F, 0.4F, 0.9F
        );

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntitySlimeCannonBall entity) {
        return TEXTURE;
    }
}