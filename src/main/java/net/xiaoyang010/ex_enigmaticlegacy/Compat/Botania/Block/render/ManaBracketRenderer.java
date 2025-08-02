package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.ManaBracketTile;

public class ManaBracketRenderer implements BlockEntityRenderer<ManaBracketTile> {

    public ManaBracketRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ManaBracketTile charger, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (charger.getLevel() == null || !charger.getLevel().isLoaded(charger.getBlockPos()))
            return;

        ItemStack stack = charger.getItemHandler().getStackInSlot(0);
        if (stack.isEmpty())
            return;

        poseStack.pushPose();
        {
            poseStack.translate(0.5D, 0.5D, 0.5D);

            poseStack.mulPose(Vector3f.XP.rotationDegrees(90));

            poseStack.scale(0.5f, 0.5f, 0.5f);

            poseStack.translate(0.D, 0.0D, 0.6D);

            poseStack.mulPose(Vector3f.ZP.rotationDegrees(charger._rotation));

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack,
                    ItemTransforms.TransformType.FIXED,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    buffer,
                    0
            );
        }
        poseStack.popPose();
    }
}