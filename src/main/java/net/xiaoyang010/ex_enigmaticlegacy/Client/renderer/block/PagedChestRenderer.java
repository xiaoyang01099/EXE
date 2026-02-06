package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
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
import net.xiaoyang010.ex_enigmaticlegacy.Tile.PagedChestBlockTile;

@OnlyIn(Dist.CLIENT)
public class PagedChestRenderer implements BlockEntityRenderer<PagedChestBlockTile> {
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
    public void render(PagedChestBlockTile chest, float partialTick, PoseStack poseStack,
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