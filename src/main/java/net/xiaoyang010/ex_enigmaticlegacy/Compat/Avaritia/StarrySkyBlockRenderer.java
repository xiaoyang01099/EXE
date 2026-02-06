package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialCoreShaders;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialRenderHelper;

public class StarrySkyBlockRenderer implements BlockEntityRenderer<StarrySkyBlockEntity> {

    public StarrySkyBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(StarrySkyBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {

        var shader = SpecialCoreShaders.getStarrySkyShader();
        if (shader != null) {
            float gameTime = blockEntity.getAnimationTime(partialTick);

            shader.safeGetUniform("Time").set(gameTime);

            BlockPos pos = blockEntity.getBlockPos();
            shader.safeGetUniform("BlockPos").set((float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
        }

        VertexConsumer consumer = bufferSource.getBuffer(SpecialRenderHelper.STARRY_SKY);

        Matrix4f matrix = poseStack.last().pose();
        BlockPos blockPos = blockEntity.getBlockPos();

        for (Direction direction : Direction.values()) {
            renderFace(matrix, consumer, direction, blockPos);
        }
    }

    private void renderFace(Matrix4f matrix, VertexConsumer consumer, Direction direction, BlockPos blockPos) {
        float minU = 0.0f, maxU = 1.0f;
        float minV = 0.0f, maxV = 1.0f;

        float wx = blockPos.getX();
        float wy = blockPos.getY();
        float wz = blockPos.getZ();

        switch (direction) {
            case DOWN -> {
                consumer.vertex(matrix, 0, 0, 0).uv(minU, minV).endVertex();
                consumer.vertex(matrix, 1, 0, 0).uv(maxU, minV).endVertex();
                consumer.vertex(matrix, 1, 0, 1).uv(maxU, maxV).endVertex();
                consumer.vertex(matrix, 0, 0, 1).uv(minU, maxV).endVertex();
            }
            case UP -> {
                consumer.vertex(matrix, 0, 1, 1).uv(minU, maxV).endVertex();
                consumer.vertex(matrix, 1, 1, 1).uv(maxU, maxV).endVertex();
                consumer.vertex(matrix, 1, 1, 0).uv(maxU, minV).endVertex();
                consumer.vertex(matrix, 0, 1, 0).uv(minU, minV).endVertex();
            }
            case NORTH -> {
                consumer.vertex(matrix, 0, 0, 0).uv(minU, maxV).endVertex();
                consumer.vertex(matrix, 0, 1, 0).uv(minU, minV).endVertex();
                consumer.vertex(matrix, 1, 1, 0).uv(maxU, minV).endVertex();
                consumer.vertex(matrix, 1, 0, 0).uv(maxU, maxV).endVertex();
            }
            case SOUTH -> {
                consumer.vertex(matrix, 1, 0, 1).uv(maxU, maxV).endVertex();
                consumer.vertex(matrix, 1, 1, 1).uv(maxU, minV).endVertex();
                consumer.vertex(matrix, 0, 1, 1).uv(minU, minV).endVertex();
                consumer.vertex(matrix, 0, 0, 1).uv(minU, maxV).endVertex();
            }
            case WEST -> {
                consumer.vertex(matrix, 0, 0, 1).uv(maxU, maxV).endVertex();
                consumer.vertex(matrix, 0, 1, 1).uv(maxU, minV).endVertex();
                consumer.vertex(matrix, 0, 1, 0).uv(minU, minV).endVertex();
                consumer.vertex(matrix, 0, 0, 0).uv(minU, maxV).endVertex();
            }
            case EAST -> {
                consumer.vertex(matrix, 1, 0, 0).uv(minU, maxV).endVertex();
                consumer.vertex(matrix, 1, 1, 0).uv(minU, minV).endVertex();
                consumer.vertex(matrix, 1, 1, 1).uv(maxU, minV).endVertex();
                consumer.vertex(matrix, 1, 0, 1).uv(maxU, maxV).endVertex();
            }
        }
    }
}
