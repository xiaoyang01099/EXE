package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.client.Minecraft;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialRenderHelper;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialCoreShaders;

public class CosmicBlockRenderer implements BlockEntityRenderer<CosmicBlockEntity> {

    public CosmicBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CosmicBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int light, int overlay) {
        var shader = SpecialCoreShaders.cosmicBackground();
        if (shader != null) {
            float gameTime = (Minecraft.getInstance().level.getGameTime() + partialTicks) / 24000.0f;

            shader.safeGetUniform("Time").set(gameTime);

            shader.safeGetUniform("ColorCycle").set((float)Math.sin(gameTime * 0.5) * 0.5f + 0.5f);
        }

        VertexConsumer buffer = bufferSource.getBuffer(SpecialRenderHelper.COSMIC_BACKGROUND);

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        poseStack.scale(1.001f, 1.001f, 1.001f);
        poseStack.translate(-0.5, -0.5, -0.5);

        renderCosmicCube(poseStack, buffer, light, overlay);

        poseStack.popPose();
    }

    private void renderCosmicCube(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        Matrix4f matrix = poseStack.last().pose();

        float r = 1.0f;
        float g = 1.0f;
        float b = 1.0f;
        float a = 1.0f;

        renderFace(matrix, buffer, Direction.UP, r, g, b, a, light, overlay);
        renderFace(matrix, buffer, Direction.DOWN, r, g, b, a, light, overlay);
        renderFace(matrix, buffer, Direction.NORTH, r, g, b, a, light, overlay);
        renderFace(matrix, buffer, Direction.SOUTH, r, g, b, a, light, overlay);
        renderFace(matrix, buffer, Direction.EAST, r, g, b, a, light, overlay);
        renderFace(matrix, buffer, Direction.WEST, r, g, b, a, light, overlay);
    }

    private void renderFace(Matrix4f matrix, VertexConsumer buffer, Direction direction,
                            float r, float g, float b, float a, int light, int overlay) {

        float[][] vertices = getFaceVertices(direction);

        for (int i = 0; i < 4; i++) {
            float x = vertices[i][0];
            float y = vertices[i][1];
            float z = vertices[i][2];

            float u = (i == 1 || i == 2) ? 1.0f : 0.0f;
            float v = (i == 2 || i == 3) ? 1.0f : 0.0f;

            buffer.vertex(matrix, x, y, z)
                    .color(r, g, b, a)
                    .uv(u, v)
                    .endVertex();
        }
    }

    private float[][] getFaceVertices(Direction direction) {
        switch (direction) {
            case UP:
                return new float[][]{
                        {0, 1, 1}, {1, 1, 1}, {1, 1, 0}, {0, 1, 0}
                };
            case DOWN:
                return new float[][]{
                        {0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}
                };
            case NORTH:
                return new float[][]{
                        {1, 0, 0}, {0, 0, 0}, {0, 1, 0}, {1, 1, 0}
                };
            case SOUTH:
                return new float[][]{
                        {0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}
                };
            case WEST:
                return new float[][]{
                        {0, 0, 0}, {0, 0, 1}, {0, 1, 1}, {0, 1, 0}
                };
            case EAST:
                return new float[][]{
                        {1, 0, 1}, {1, 0, 0}, {1, 1, 0}, {1, 1, 1}
                };
            default:
                return new float[4][3];
        }
    }

    @Override
    public boolean shouldRenderOffScreen(CosmicBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}