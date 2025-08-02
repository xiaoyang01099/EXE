package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.ModelDiceFate;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.TileBoardFate;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModModelLayers;
import vazkii.botania.client.core.handler.ClientTickHandler;

import java.util.Random;

public class RenderTileBoardFate implements BlockEntityRenderer<TileBoardFate> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ex_enigmaticlegacy:textures/entity/dice/dice_fate.png");
    private final ModelDiceFate model;

    public RenderTileBoardFate(BlockEntityRendererProvider.Context context) {
        this.model = new ModelDiceFate(context.bakeLayer(ModModelLayers.DICE_FATE));
    }

    @Override
    public void render(TileBoardFate tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        double time = tile.getLevel() == null ? 0.0 : (double)(ClientTickHandler.ticksInGame + partialTicks);
        if (tile != null) {
            time += (double)(new Random((long)(tile.getBlockPos().getX() ^ tile.getBlockPos().getY() ^ tile.getBlockPos().getZ()))).nextInt(360);
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            float yaw = mc.player.getYHeadRot();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-yaw + 90.0F));
        }

        for (int i = 0; i < tile.getContainerSize(); i++) {
            if (!tile.getItem(i).isEmpty()) {
                renderDice(tile, i, time, partialTicks, poseStack, bufferSource, packedLight, packedOverlay);
            }
        }

        poseStack.popPose();
    }

    private void renderDice(TileBoardFate tile, int slot, double time, float partialTicks,
                            PoseStack poseStack, MultiBufferSource bufferSource,
                            int packedLight, int packedOverlay) {

        time += (double)((float)slot * 83.256F);

        poseStack.pushPose();

        float dropAnim = 1.0F - Math.min(150.0F, (float)(tile.clientTick[slot] * tile.clientTick[slot]) * 1.42F + partialTicks) / 150.0F;
        dropAnim = Math.min(1.0F, Math.max(dropAnim, 0.0F));
        float alpha = (float)Math.cos((double)dropAnim);

        float posX = 0.0F;
        float posZ = 0.0F;

        switch (slot) {
            case 0: // 右上角
                posX = 0.16F + 0.08F * dropAnim;
                posZ = 0.16F + 0.08F * dropAnim;
                break;
            case 1: // 左上角
                posX = -0.16F - 0.08F * dropAnim;
                posZ = 0.16F + 0.08F * dropAnim;
                break;
            case 2: // 右下角
                posX = 0.16F + 0.08F * dropAnim;
                posZ = -0.16F - 0.08F * dropAnim;
                break;
            case 3: // 左下角
                posX = -0.16F - 0.08F * dropAnim;
                posZ = -0.16F - 0.08F * dropAnim;
                break;
        }

        poseStack.translate(posX, 0.02F + Math.sin(time / 12.0) / 48.0 + (0.28F * dropAnim), posZ);

        poseStack.scale(0.25F, 0.25F, 0.25F);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));

        float dropAngle = 70.0F * dropAnim;
        float rotX = 0.0F, rotY = 0.0F, rotZ = 0.0F;

        switch (tile.slotChance[slot]) {
            case 1:
                rotX = 180.0F + dropAngle;
                rotY = 0.0F + dropAngle;
                rotZ = 0.0F;
                break;
            case 2:
                rotX = 0.0F + dropAngle;
                rotY = 0.0F + dropAngle;
                rotZ = 0.0F;
                break;
            case 3:
                rotX = 90.0F + dropAngle;
                rotY = 0.0F + dropAngle;
                rotZ = 0.0F;
                break;
            case 4:
                rotX = 270.0F + dropAngle;
                rotY = 0.0F + dropAngle;
                rotZ = 0.0F;
                break;
            case 5:
                rotX = 0.0F + dropAngle;
                rotY = 0.0F + dropAngle;
                rotZ = 270.0F;
                break;
            case 6:
                rotX = 0.0F + dropAngle;
                rotY = 0.0F + dropAngle;
                rotZ = 90.0F;
                break;
            default:
                rotX = 0.0F + dropAngle;
                rotY = 0.0F + dropAngle;
                rotZ = 0.0F;
                break;
        }

        model.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, rotX, rotY, rotZ);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(TileBoardFate tile) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}