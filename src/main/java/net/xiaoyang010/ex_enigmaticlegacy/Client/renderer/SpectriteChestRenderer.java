package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
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
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.SpectriteChestTile;

@OnlyIn(Dist.CLIENT)
public class SpectriteChestRenderer implements BlockEntityRenderer<SpectriteChestTile> {
    public static final Material SPECTRITE_CHEST_LOCATION = new Material(Sheets.CHEST_SHEET,
            new ResourceLocation("ex_enigmaticlegacy", "entity/chest/spectrite_chest"));
    public static final Material SPECTRITE_CHEST_LEFT_LOCATION = new Material(Sheets.CHEST_SHEET,
            new ResourceLocation("ex_enigmaticlegacy", "entity/chest/spectrite_chest_left"));
    public static final Material SPECTRITE_CHEST_RIGHT_LOCATION = new Material(Sheets.CHEST_SHEET,
            new ResourceLocation("ex_enigmaticlegacy", "entity/chest/spectrite_chest_right"));

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private final ModelPart doubleLeftLid;
    private final ModelPart doubleLeftBottom;
    private final ModelPart doubleLeftLock;
    private final ModelPart doubleRightLid;
    private final ModelPart doubleRightBottom;
    private final ModelPart doubleRightLock;
    private final ChestBlock block;

    public SpectriteChestRenderer(BlockEntityRendererProvider.Context context) {
        // 单箱子模型
        ModelPart singleModel = context.bakeLayer(ModelLayers.CHEST);
        this.bottom = singleModel.getChild("bottom");
        this.lid = singleModel.getChild("lid");
        this.lock = singleModel.getChild("lock");

        // 大箱子左边模型
        ModelPart leftModel = context.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT);
        this.doubleLeftBottom = leftModel.getChild("bottom");
        this.doubleLeftLid = leftModel.getChild("lid");
        this.doubleLeftLock = leftModel.getChild("lock");

        // 大箱子右边模型
        ModelPart rightModel = context.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT);
        this.doubleRightBottom = rightModel.getChild("bottom");
        this.doubleRightLid = rightModel.getChild("lid");
        this.doubleRightLock = rightModel.getChild("lock");

        this.block = (ChestBlock) ModBlockss.SPECTRITE_CHEST.get();
    }

    public static LayerDefinition createSingleBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        partdefinition.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public static LayerDefinition createDoubleBodyRightLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        partdefinition.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(15.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public static LayerDefinition createDoubleBodyLeftLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(0.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        partdefinition.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void render(SpectriteChestTile blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        Level level = blockEntity.getLevel();
        boolean flag = level != null;
        BlockState blockstate = flag ? blockEntity.getBlockState()
                : block.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        ChestType chesttype = blockstate.hasProperty(ChestBlock.TYPE)
                ? blockstate.getValue(ChestBlock.TYPE)
                : ChestType.SINGLE;
        Block block = blockstate.getBlock();

        if (block instanceof AbstractChestBlock<?>) {
            AbstractChestBlock<?> abstractchestblock = (AbstractChestBlock<?>) block;
            boolean isDouble = chesttype != ChestType.SINGLE;
            float f = blockstate.getValue(ChestBlock.FACING).toYRot();

            poseStack.pushPose();
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-f));
            poseStack.translate(-0.5D, -0.5D, -0.5D);

            DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> neighborCombineResult;
            if (flag) {
                neighborCombineResult = abstractchestblock.combine(blockstate, level,
                        blockEntity.getBlockPos(), true);
            } else {
                neighborCombineResult = DoubleBlockCombiner.Combiner::acceptNone;
            }

            float lidAngle = neighborCombineResult.apply(ChestBlock.opennessCombiner(blockEntity))
                    .get(partialTick);
            lidAngle = 1.0F - lidAngle;
            lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;

            Material material;
            if (isDouble) {
                if (chesttype == ChestType.LEFT) {
                    material = SPECTRITE_CHEST_LEFT_LOCATION;
                } else {
                    material = SPECTRITE_CHEST_RIGHT_LOCATION;
                }
            } else {
                material = SPECTRITE_CHEST_LOCATION;
            }

            VertexConsumer vertexconsumer = material.buffer(bufferSource, RenderType::entityCutout);

            if (isDouble) {
                if (chesttype == ChestType.LEFT) {
                    this.render(poseStack, vertexconsumer, this.doubleLeftLid, this.doubleLeftLock, this.doubleLeftBottom,
                            lidAngle, combinedLight, combinedOverlay);
                } else {
                    this.render(poseStack, vertexconsumer, this.doubleRightLid, this.doubleRightLock, this.doubleRightBottom,
                            lidAngle, combinedLight, combinedOverlay);
                }
            } else {
                this.render(poseStack, vertexconsumer, this.lid, this.lock, this.bottom,
                        lidAngle, combinedLight, combinedOverlay);
            }

            poseStack.popPose();
        }
    }

    private void render(PoseStack poseStack, VertexConsumer consumer, ModelPart lid,
                        ModelPart lock, ModelPart bottom, float lidAngle, int light, int overlay) {
        lid.xRot = -(lidAngle * ((float)Math.PI / 2F));
        lock.xRot = lid.xRot;
        bottom.render(poseStack, consumer, light, overlay);
        lock.render(poseStack, consumer, light, overlay);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.06D, 0.0D);
        lid.render(poseStack, consumer, light, overlay);
        poseStack.popPose();
    }
}