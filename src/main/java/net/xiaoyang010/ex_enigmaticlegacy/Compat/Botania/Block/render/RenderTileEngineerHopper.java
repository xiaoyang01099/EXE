package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.ModelEngineerHopper;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.TileEngineerHopper;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModModelLayers;
import vazkii.botania.client.core.handler.ClientTickHandler;

import java.util.Random;

public class RenderTileEngineerHopper implements BlockEntityRenderer<TileEngineerHopper> {
    private final ModelEngineerHopper model;
    private static final ResourceLocation texture = new ResourceLocation("ex_enigmaticlegacy", "textures/entity/engineer_hopper.png");

    public RenderTileEngineerHopper(BlockEntityRendererProvider.Context context) {
        this.model = new ModelEngineerHopper(context.bakeLayer(ModModelLayers.ENGINEER_HOPPER));
    }

    @Override
    public void render(TileEngineerHopper tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        double time = tile.getLevel() == null ? 0.0D : (double)(ClientTickHandler.ticksInGame + partialTicks);
        if (tile != null) {
            Random random = new Random((long)(tile.getBlockPos().getX() ^
                    tile.getBlockPos().getY() ^
                    tile.getBlockPos().getZ()));
            time += (double)random.nextInt(360);
        }

        poseStack.pushPose();

        poseStack.translate(0.5D, 1.1625D, 0.5D);
        poseStack.mulPose(com.mojang.math.Vector3f.XP.rotationDegrees(180.0F));


        poseStack.scale(0.7F, 0.7F, 0.7F);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));

        this.model.renderHopper(poseStack, vertexConsumer, packedLight, packedOverlay, time);

        poseStack.popPose();
    }
}