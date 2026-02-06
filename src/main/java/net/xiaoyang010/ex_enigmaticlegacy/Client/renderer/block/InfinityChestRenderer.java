package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Block.InfinityChest;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.InfinityChestTile;

@OnlyIn(Dist.CLIENT)
public class InfinityChestRenderer implements BlockEntityRenderer<InfinityChestTile> {
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public InfinityChestRenderer(BlockEntityRendererProvider.Context pContext) {
        ModelPart modelpart = pContext.bakeLayer(ModelLayers.CHEST);
        this.bottom = modelpart.getChild("bottom");
        this.lid = modelpart.getChild("lid");
        this.lock = modelpart.getChild("lock");
    }

    @Override
    public void render(InfinityChestTile chest, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        Level level = chest.getLevel();
        boolean flag = level != null;
        BlockState blockstate = flag ? chest.getBlockState() : ModBlockss.INFINITYCHEST.get().defaultBlockState().setValue(InfinityChest.FACING, Direction.SOUTH);
        Block block = blockstate.getBlock();
        if (block instanceof InfinityChest) {
            pPoseStack.pushPose();
            float f = blockstate.getValue(InfinityChest.FACING).toYRot();
            pPoseStack.translate(0.5, 0.5, 0.5);
            pPoseStack.mulPose(Vector3f.YP.rotationDegrees(-f));
            pPoseStack.translate(-0.5, -0.5, -0.5);

            float openNess = 1.0f - chest.getOpenNess(pPartialTick);
            openNess = 1.0f - openNess * openNess * openNess;
            Material material = this.getMaterial(chest);
            VertexConsumer vertexconsumer = material.buffer(pBufferSource, RenderType::entityCutout);
            this.render(pPoseStack, vertexconsumer, this.lid, this.lock, this.bottom, openNess, pPackedLight, pPackedOverlay);

            pPoseStack.popPose();
        }

    }

    private void render(PoseStack poseStack, VertexConsumer consumer, ModelPart lid, ModelPart lock, ModelPart bottom, float lidAngle, int light, int overlay) {
        lid.xRot = -(lidAngle * ((float) Math.PI / 2F));
        lock.xRot = lid.xRot;
        bottom.render(poseStack, consumer, light, overlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.06D, 0.0D);
        lid.render(poseStack, consumer, light, overlay);
        lock.render(poseStack, consumer, light, overlay);

        poseStack.popPose();
    }

    protected Material getMaterial(InfinityChestTile blockEntity) {
        return new Material(Sheets.CHEST_SHEET, new ResourceLocation(ExEnigmaticlegacyMod.MODID, "entity/chest/" + "infinity_chest"));
    }

    private static Material chestMaterial(String pChestName) {
        return null;
    }
}
