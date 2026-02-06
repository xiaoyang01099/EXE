package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModArmors;
import org.jetbrains.annotations.NotNull;

public class FaceEffectLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation EYE_TEXTURE = new ResourceLocation("ex_enigmaticlegacy","textures/models/nebula_eyes.png");
    private static final int FULL_BRIGHT = 0xF000F0;

    public FaceEffectLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!shouldRenderFace(helmet)) return;

        poseStack.pushPose();
        this.getParentModel().head.translateAndRotate(poseStack);

        poseStack.translate(0.0, -0.25, -0.2501);
        poseStack.scale(0.5F, 0.5F, 0.5F);

        renderEndPortalOnFace(poseStack, buffer);
        renderEyes(poseStack, buffer, player);
        poseStack.popPose();
    }

    private void renderEyes(PoseStack poseStack, MultiBufferSource buffer, AbstractClientPlayer player) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        poseStack.pushPose();
        poseStack.translate(0.0, 0.15, -0.0001);
        // 速率
        float velocity = 0.005F;
        //饱和度
        float saturation = 1.0F;
        //大小
        float size = 0.4F;
        //透明度
        float alpha = 0.7F;

        float time = (player.tickCount + Minecraft.getInstance().getFrameTime()) * velocity;
        float hue = time % 1.0F;

        int color = Mth.hsvToRgb(hue, 1.0F, 1.0F);
        float r = ((color >> 16) & 0xFF) / 255F * saturation;
        float g = ((color >> 8)  & 0xFF) / 255F * saturation;
        float b = ( color        & 0xFF) / 255F * saturation;


        VertexConsumer vc = buffer.getBuffer(
                RenderType.entityTranslucent(EYE_TEXTURE)
        );

        Matrix4f mat = poseStack.last().pose();
        Matrix3f nrm = poseStack.last().normal();


        vc.vertex(mat, -size, -size, 0)
                .color(r, g, b, alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(FULL_BRIGHT).normal(nrm, 0, 0, 1).endVertex();

        vc.vertex(mat, -size,  size, 0)
                .color(r, g, b, alpha)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(FULL_BRIGHT).normal(nrm, 0, 0, 1).endVertex();

        vc.vertex(mat,  size,  size, 0)
                .color(r, g, b, alpha)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(FULL_BRIGHT).normal(nrm, 0, 0, 1).endVertex();

        vc.vertex(mat,  size, -size, 0)
                .color(r, g, b, alpha)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(FULL_BRIGHT).normal(nrm, 0, 0, 1).endVertex();
        poseStack.popPose();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderEndPortalOnFace(PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.endPortal());
        Matrix4f matrix = poseStack.last().pose();

        float size = 0.5F;
        vertexConsumer.vertex(matrix, -size, -size, 0).endVertex();
        vertexConsumer.vertex(matrix, -size,  size, 0).endVertex();
        vertexConsumer.vertex(matrix,  size,  size, 0).endVertex();
        vertexConsumer.vertex(matrix,  size, -size, 0).endVertex();
    }

    private boolean shouldRenderFace(ItemStack helmet) {
        return helmet != null && helmet.getItem() == ModArmors.NEBULA_HELMET.get();
    }
}
