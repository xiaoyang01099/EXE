package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.block;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Block.SpectriteChest;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.SpectriteChestTile;

@OnlyIn(Dist.CLIENT)
public class SpectriteChestRenderer implements BlockEntityRenderer<SpectriteChestTile> {
    public static final Material SPECTRITE_CHEST_LOCATION = new Material(Sheets.CHEST_SHEET,
            new ResourceLocation("ex_enigmaticlegacy", "entity/chest/spectrite_chest"));

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public SpectriteChestRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart singleModel = context.bakeLayer(ModelLayers.CHEST);
        this.bottom = singleModel.getChild("bottom");
        this.lid = singleModel.getChild("lid");
        this.lock = singleModel.getChild("lock");
    }

    public static LayerDefinition createSingleBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("bottom",
                CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F),
                PartPose.ZERO);
        partdefinition.addOrReplaceChild("lid",
                CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F),
                PartPose.offset(0.0F, 9.0F, 1.0F));
        partdefinition.addOrReplaceChild("lock",
                CubeListBuilder.create().texOffs(0, 0).addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void render(SpectriteChestTile blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        Level level = blockEntity.getLevel();
        boolean flag = level != null;
        BlockState blockstate = flag ? blockEntity.getBlockState()
                : blockEntity.getBlockState().setValue(SpectriteChest.FACING, Direction.SOUTH);

        float f = blockstate.getValue(SpectriteChest.FACING).toYRot();

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-f));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        float lidAngle = blockEntity.getOpenNess(partialTick);
        lidAngle = 1.0F - lidAngle;
        lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;

        VertexConsumer vertexconsumer = SPECTRITE_CHEST_LOCATION.buffer(bufferSource, RenderType::entityCutout);

        this.render(poseStack, vertexconsumer, this.lid, this.lock, this.bottom,
                lidAngle, combinedLight, combinedOverlay);

        poseStack.popPose();
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
}