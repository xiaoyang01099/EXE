package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.block.ExralCosmicRenderHelper;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.StarlitSanctumTile;
import org.jetbrains.annotations.NotNull;

public class StarlitSanctumRenderer implements BlockEntityRenderer<StarlitSanctumTile> {

    public StarlitSanctumRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(@NotNull StarlitSanctumTile astralBlockEntity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource source, int packedLight, int packedOverlay) {
        BlockState blockState = astralBlockEntity.getBlockState();
        ItemStack stack = new ItemStack(ModItems.ASTRAL_BLOCK.get());
        poseStack.pushPose();
        poseStack.translate((double)0.5F, (double)0.5F, (double)0.5F);
        poseStack.scale(1.0011123F, 1.0011123F, 1.0011123F);
        poseStack.translate((double)-0.5F, (double)-0.5F, (double)-0.5F);
        ExralCosmicRenderHelper.renderBlockQuads(blockState, poseStack, source, packedLight, packedOverlay, stack);
        poseStack.popPose();
    }

    public boolean shouldRenderOffScreen(@NotNull StarlitSanctumTile astralBlockEntity) {
        return true;
    }
}