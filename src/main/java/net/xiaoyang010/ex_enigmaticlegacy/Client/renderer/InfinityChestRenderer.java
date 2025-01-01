package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Block.InfinityChest;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.InfinityChestEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InfinityChestRenderer implements BlockEntityRenderer<InfinityChestEntity> {
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    // 定义箱子材质对应的Material数组
    private static final Material[] INFINITY_CHEST_TEXTURES = new Material[] {
            new Material(Sheets.CHEST_SHEET, ExEnigmaticlegacyMod.getRL("textures/entity/chest/infinity_chest/0.png")),
            new Material(Sheets.CHEST_SHEET, ExEnigmaticlegacyMod.getRL("textures/entity/chest/infinity_chest/1.png")),
            new Material(Sheets.CHEST_SHEET, ExEnigmaticlegacyMod.getRL("textures/entity/chest/infinity_chest/2.png")),
            new Material(Sheets.CHEST_SHEET, ExEnigmaticlegacyMod.getRL("textures/entity/chest/infinity_chest/3.png")),
            new Material(Sheets.CHEST_SHEET, ExEnigmaticlegacyMod.getRL("textures/entity/chest/infinity_chest/4.png")),
            new Material(Sheets.CHEST_SHEET, ExEnigmaticlegacyMod.getRL("textures/entity/chest/infinity_chest/5.png")),
            new Material(Sheets.CHEST_SHEET, ExEnigmaticlegacyMod.getRL("textures/entity/chest/infinity_chest/6.png")),
            new Material(Sheets.CHEST_SHEET, ExEnigmaticlegacyMod.getRL("textures/entity/chest/infinity_chest/7.png")),
            new Material(Sheets.CHEST_SHEET, ExEnigmaticlegacyMod.getRL("textures/entity/chest/infinity_chest/8.png"))
    };

    // 动画帧顺序，基于mcmeta文件
    private static final int[] ANIMATION_FRAMES = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 8, 8, 7, 6, 5, 4, 3, 2, 1, 0, 0
    };

    // 每帧持续时间
    private static final int FRAME_TIME = 2;  // 每帧2个tick

    public InfinityChestRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart modelPart = context.bakeLayer(ModelLayers.CHEST);
        this.bottom = modelPart.getChild("bottom");
        this.lid = modelPart.getChild("lid");
        this.lock = modelPart.getChild("lock");
    }

    // 获取当前动画帧的纹理索引
    private int getAnimationFrame(InfinityChestEntity blockEntity, float partialTicks) {
        long time = blockEntity.getLevel().getGameTime();
        int frameIndex = (int) ((time / FRAME_TIME) % ANIMATION_FRAMES.length); // 当前的动画帧索引
        return ANIMATION_FRAMES[frameIndex];  // 获取对应的纹理帧
    }

    @Override
    public void render(InfinityChestEntity blockEntity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        Level level = blockEntity.getLevel();
        boolean hasLevel = level != null;
        BlockState blockState = hasLevel ? blockEntity.getBlockState() :
                Blocks.CHEST.defaultBlockState().setValue(InfinityChest.FACING, Direction.SOUTH);
        Direction direction = blockState.getValue(InfinityChest.FACING);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-direction.toYRot()));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        float openness = blockEntity.getOpenNess(partialTicks);
        float lidAngle = 1.0F - openness;
        lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;

        // 渲染动画贴图
        int textureIndex = getAnimationFrame(blockEntity, partialTicks);
        Material currentMaterial = INFINITY_CHEST_TEXTURES[textureIndex];
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(currentMaterial.texture()));

        // 渲染箱子
        lid.xRot = -(lidAngle * ((float)Math.PI / 2F));
        lock.xRot = lid.xRot;

        lid.render(poseStack, vertexConsumer, combinedLight, combinedOverlay);
        lock.render(poseStack, vertexConsumer, combinedLight, combinedOverlay);
        bottom.render(poseStack, vertexConsumer, combinedLight, combinedOverlay);

        poseStack.popPose();
    }


    // 渲染箱子各部分
    private void renderChest(PoseStack poseStack, VertexConsumer vertexConsumer, ModelPart lid, ModelPart lock, ModelPart bottom, float openness, int light, int overlay) {
        lid.xRot = -(openness * (float) Math.PI / 2F);  // 计算盖子的旋转角度
        lock.xRot = lid.xRot;  // 锁与盖子的旋转角度一致

        // 渲染每个部件
        lid.render(poseStack, vertexConsumer, light, overlay);
        lock.render(poseStack, vertexConsumer, light, overlay);
        bottom.render(poseStack, vertexConsumer, light, overlay);
    }
}
