package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.ModelDiceFate;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.TileGameBoard;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModModelLayers;

import java.util.Random;

public class RenderTileGameBoard implements BlockEntityRenderer<TileGameBoard> {
    private static final ResourceLocation TEXTURE_PLAYER = new ResourceLocation("ex_enigmaticlegacy", "textures/entity/dice/game_dice.png");
    private static final ResourceLocation TEXTURE_ENEMY = new ResourceLocation("ex_enigmaticlegacy", "textures/entity/dice/game_dice_enemy.png");
    private final ModelDiceFate model;

    public RenderTileGameBoard(BlockEntityRendererProvider.Context context) {
        this.model = new ModelDiceFate(context.bakeLayer(ModModelLayers.DICE_FATE));
    }

    @Override
    public void render(TileGameBoard tile, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        double time = tile.getLevel() == null ? 0.0F :
                (Minecraft.getInstance().level.getGameTime() + partialTick);

        if (tile != null) {
            Random random = new Random(
                    tile.getBlockPos().getX() ^ tile.getBlockPos().getY() ^ tile.getBlockPos().getZ()
            );
            time += random.nextInt(360);
        }

        poseStack.pushPose();

        poseStack.translate(0.5D, 0.0D, 0.5D);

        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer != null && mc.gameRenderer.getMainCamera() != null) {
            float yRot = mc.gameRenderer.getMainCamera().getYRot();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F - yRot));
        }

        for (int i = 0; i < tile.slotChance.length; ++i) {
            if (tile.slotChance[i] != 0) {
                renderDice(tile, i, time, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            }
        }

        poseStack.popPose();
    }

    private void renderDice(TileGameBoard tile, int slot, double time, float partialTick,
                            PoseStack poseStack, MultiBufferSource bufferSource,
                            int packedLight, int packedOverlay) {

        poseStack.pushPose();

        double slotTime = time + (slot * 83.256F);

        float dropAnim = 1.0F - Math.min(150.0F,
                (float)(tile.clientTick[slot] * tile.clientTick[slot]) * 1.42F + partialTick) / 150.0F;
        dropAnim = Math.min(1.0F, Math.max(dropAnim, 0.0F));

        float alpha = (float)Math.cos(dropAnim);

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

        poseStack.translate(
                posX,
                0.02F + Math.sin(slotTime / 12.0F) / 48.0F + (0.28F * dropAnim),
                posZ
        );

        poseStack.scale(0.25F, 0.25F, 0.25F);

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

        model.setRotationAngles(rotX, rotY, rotZ);

        ResourceLocation texture = slot > 1 ? TEXTURE_ENEMY : TEXTURE_PLAYER;
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));

        model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay,
                1.0F, 1.0F, 1.0F, alpha);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(TileGameBoard tile) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}