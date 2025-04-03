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
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Block.PagedChestBlock;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModModelLayers;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.PagedChestBlockEntity;

@OnlyIn(Dist.CLIENT)
public class PagedChestRenderer implements BlockEntityRenderer<PagedChestBlockEntity> {
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private final ResourceLocation texture;

    public PagedChestRenderer(BlockEntityRendererProvider.Context context) {
        this.texture = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/entity/chest/multipage_chest.png");

        ModelPart modelpart = context.bakeLayer(ModelLayers.CHEST);
        this.bottom = modelpart.getChild("bottom");
        this.lid = modelpart.getChild("lid");
        this.lock = modelpart.getChild("lock");
    }

    @Override
    public void render(PagedChestBlockEntity chest, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = chest.getLevel();
        boolean flag = level != null;
        BlockState blockstate = flag ? chest.getBlockState()
                : ModBlockss.PAGED_CHEST.get().defaultBlockState().setValue(PagedChestBlock.FACING, Direction.SOUTH);
        Block block = blockstate.getBlock();

        if (block instanceof PagedChestBlock) {
            poseStack.pushPose();
            float f = blockstate.getValue(PagedChestBlock.FACING).toYRot();
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-f));
            poseStack.translate(-0.5D, -0.5D, -0.5D);

            float openness = chest.getOpenNess(partialTick);
            openness = 1.0F - openness;
            openness = 1.0F - openness * openness * openness;

            VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutout(texture));
            this.render(poseStack, vertexconsumer, this.lid, this.lock, this.bottom,
                    openness, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private void render(PoseStack poseStack, VertexConsumer consumer, ModelPart lidPart, ModelPart lockPart, ModelPart bottomPart, float lidAngle, int packedLight, int packedOverlay) {
        lidPart.xRot = -(lidAngle * 1.5707964F);
        lockPart.xRot = lidPart.xRot;
        bottomPart.render(poseStack, consumer, packedLight, packedOverlay);
        lockPart.render(poseStack, consumer, packedLight, packedOverlay);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.06D, 0.0D);
        lidPart.render(poseStack, consumer, packedLight, packedOverlay);
        poseStack.popPose();
    }
}