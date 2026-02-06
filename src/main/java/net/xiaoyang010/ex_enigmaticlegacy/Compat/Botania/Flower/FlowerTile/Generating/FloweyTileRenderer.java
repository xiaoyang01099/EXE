package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class FloweyTileRenderer implements BlockEntityRenderer<FloweyTile> {

    public FloweyTileRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FloweyTile tile, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (tile.getCollectedSouls().isEmpty()) return;

        double time = tile.getLevel().getGameTime() + partialTick;
        double breathOffset = Math.sin(time * 0.05) * 0.15;

        float rotationAngle = tile.getRotationAngle();
        int soulCount = tile.getCollectedSoulsCount();

        int index = 0;
        for (Map.Entry<String, ItemStack> entry : tile.getCollectedSouls().entrySet()) {
            ItemStack stack = entry.getValue();

            double angle = rotationAngle + (index * 2 * Math.PI / soulCount);
            double radius = tile.getOrbitRadius();
            double height = tile.getOrbitHeight();

            double xOffset = Math.cos(angle) * radius;
            double yOffset = height + breathOffset;
            double zOffset = Math.sin(angle) * radius;

            poseStack.pushPose();

            poseStack.translate(0.5 + xOffset, yOffset, 0.5 + zOffset);

            poseStack.mulPose(Vector3f.YP.rotationDegrees((float) (time * 2) % 360));

            float scale = 0.5F + (float) Math.sin(time * 0.1 + index) * 0.1F;
            poseStack.scale(scale, scale, scale);

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack,
                    ItemTransforms.TransformType.GROUND,
                    combinedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    0
            );

            poseStack.popPose();

            index++;
        }
    }
}