package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.ModelCrystalCube;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.ManaCrystalCubeBlockTile;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.item.ModItems;

import java.util.Objects;

public class RenderTileManaCrystalCube implements BlockEntityRenderer<ManaCrystalCubeBlockTile> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/crystal_cube.png");
    private final ModelCrystalCube model;
    private ItemEntity entity = null;
    private final ItemRenderer itemRenderer;
    private final BlockRenderDispatcher blockRenderDispatcher;
    public static BakedModel cubeModel = null;

    public RenderTileManaCrystalCube(BlockEntityRendererProvider.Context context) {
        this.model = new ModelCrystalCube();
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.blockRenderDispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(ManaCrystalCubeBlockTile blockEntity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        if (this.entity == null) {
            this.entity = new ItemEntity(Objects.requireNonNull(blockEntity.getLevel()),
                    blockEntity.getBlockPos().getX(),
                    blockEntity.getBlockPos().getY(),
                    blockEntity.getBlockPos().getZ(),
                    new ItemStack(ModItems.twigWand));
        }

        this.entity.setTicksFrozen(ClientTickHandler.ticksInGame);
        float time = ClientTickHandler.ticksInGame + partialTicks;
        float worldTicks = blockEntity.getLevel() == null ? 0.0F : time;

        float bobOffset = time / 20.0F;
        poseStack.translate(0, Math.sin(bobOffset) * 0.1F + 0.1F, 0);

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.5D, 0.5D);
        poseStack.scale(1.0F, -1.0F, -1.0F);

        VertexConsumer solidConsumer = bufferSource.getBuffer(RenderType.entitySolid(TEXTURE));
        model.renderBase(poseStack, solidConsumer, combinedLight, combinedOverlay);

        poseStack.translate(0.0F, (float) Math.sin(worldTicks / 20.0F * 1.55) * 0.05F, 0.0F);

        if (blockEntity.getLevel() != null) {
            poseStack.pushPose();
            float scale = 0.5F;
            poseStack.translate(0.0F, 0.8F, 0.0F);
            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));

            Minecraft.getInstance().getEntityRenderDispatcher().render(
                    this.entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks,
                    poseStack, bufferSource, combinedLight);

            poseStack.popPose();
        }

        if (cubeModel != null) {
            poseStack.pushPose();
            poseStack.translate(-0.5F, 0.25F, -0.5F);
            VertexConsumer buffer = bufferSource.getBuffer(Sheets.translucentCullBlockSheet());
            blockRenderDispatcher.getModelRenderer().renderModel(
                    poseStack.last(), buffer, null, cubeModel, 1, 1, 1, combinedLight, combinedOverlay);
            poseStack.popPose();
        }

        int[] mana = blockEntity.getManaAround();
        if (mana[1] > 0 && blockEntity.getLevel() != null) {
            Minecraft mc = Minecraft.getInstance();
            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            Vec3 blockPos = new Vec3(
                    blockEntity.getBlockPos().getX() + 0.5,
                    blockEntity.getBlockPos().getY() + 0.5,
                    blockEntity.getBlockPos().getZ() + 0.5
            );

            double relX = cameraPos.x - blockPos.x;
            double relZ = cameraPos.z - blockPos.z;
            double relY = cameraPos.y - blockPos.y;

            int faceIndex;

            double horizontalDist = Math.sqrt(relX * relX + relZ * relZ);

            if (Math.abs(relY) > horizontalDist * 1.5) {
                float yaw = mc.gameRenderer.getMainCamera().getYRot();
                yaw = (yaw % 360 + 360) % 360;

                if (yaw >= 315 || yaw < 45) {
                    faceIndex = 2;
                } else if (yaw >= 45 && yaw < 135) {
                    faceIndex = 1;
                } else if (yaw >= 135 && yaw < 225) {
                    faceIndex = 0;
                } else {
                    faceIndex = 3;
                }
            } else {
                if (Math.abs(relX) > Math.abs(relZ)) {
                    faceIndex = relX > 0 ? 3 : 1;
                } else {

                    faceIndex = relZ > 0 ? 0 : 2;
                }
            }

            String manaStr = String.format("%,d / %,d", mana[0], mana[1]);
            int color = 0x30D5C8;
            int colorShade = (color & 16579836) >> 2 | color & -16777216;

            float s = 1F / 64F;
            poseStack.scale(s, s, s);
            int textWidth = mc.font.width(manaStr);

            poseStack.translate(0F, 55F, 0F);
            float translation = -16.5F;

            poseStack.pushPose();

            poseStack.mulPose(Vector3f.YP.rotationDegrees(90F * faceIndex));
            poseStack.translate(0F, 0F, translation);
            poseStack.translate(0F, 0F, -0.01F);

            mc.font.drawInBatch(
                    manaStr, -textWidth / 2, 0, color, false,
                    poseStack.last().pose(), bufferSource, true, 0, combinedLight);

            poseStack.translate(0F, 0F, 0.1F);
            mc.font.drawInBatch(
                    manaStr, -textWidth / 2 + 1, 1, colorShade, false,
                    poseStack.last().pose(), bufferSource, true, 0, combinedLight);

            poseStack.popPose();

            poseStack.scale(1F / s, 1F / s, 1F / s);
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        VertexConsumer glassConsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
        model.renderCube(poseStack, glassConsumer, combinedLight, combinedOverlay, 0.6F);

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}