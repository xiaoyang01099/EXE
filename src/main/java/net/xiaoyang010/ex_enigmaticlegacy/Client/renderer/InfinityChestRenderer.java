package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
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
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlocks;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.InfinityChestEntity;

@OnlyIn(Dist.CLIENT)
public class InfinityChestRenderer implements BlockEntityRenderer<InfinityChestEntity> {
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public InfinityChestRenderer(BlockEntityRendererProvider.Context pContext) {
        ModelPart modelpart = pContext.bakeLayer(ModelLayers.CHEST);
        this.bottom = modelpart.getChild("bottom");
        this.lid = modelpart.getChild("lid");
        this.lock = modelpart.getChild("lock");
    }

    public static LayerDefinition createSingleBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        partdefinition.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void render(InfinityChestEntity chest, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        Level level = chest.getLevel();
        boolean flag = level != null;
        BlockState blockstate = flag ? chest.getBlockState() : ModBlocks.INFINITYCHEST.get().defaultBlockState().setValue(InfinityChest.FACING, Direction.SOUTH);
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

    private void render(PoseStack pPoseStack, VertexConsumer pConsumer, ModelPart pLidPart, ModelPart pLockPart, ModelPart pBottomPart, float pLidAngle, int pPackedLight, int pPackedOverlay) {
        pLidPart.xRot = -(pLidAngle * 1.5707964F);
        pLockPart.xRot = pLidPart.xRot;
        pLidPart.render(pPoseStack, pConsumer, pPackedLight, pPackedOverlay);
        pLockPart.render(pPoseStack, pConsumer, pPackedLight, pPackedOverlay);
        pBottomPart.render(pPoseStack, pConsumer, pPackedLight, pPackedOverlay);
    }

    protected Material getMaterial(InfinityChestEntity blockEntity) {
        return new Material(Sheets.CHEST_SHEET, new ResourceLocation(ExEnigmaticlegacyMod.MODID, "entity/chest/" + "infinity_chest"));
    }

    private static Material chestMaterial(String pChestName) {
        return null;
    }
}
