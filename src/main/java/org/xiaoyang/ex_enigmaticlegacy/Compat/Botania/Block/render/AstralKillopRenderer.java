package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModItems;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.ExralCosmicRenderHelper;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.functional.AstralKillop;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.functional.AstralKillopTile;

public class AstralKillopRenderer implements BlockEntityRenderer<AstralKillopTile> {

    public AstralKillopRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull AstralKillopTile blockEntity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState blockState = blockEntity.getBlockState();

        if (!(blockState.getBlock() instanceof AstralKillop)) {
            return;
        }

        poseStack.pushPose();

        Vec3 offset = null;
        if (blockEntity.getLevel() != null) {
            offset = blockState.getOffset(blockEntity.getLevel(), blockEntity.getBlockPos());
        }
        poseStack.translate(offset.x, offset.y, offset.z);

        ItemStack stack = new ItemStack(ModItems.ASTRAL_KILLOP.get());

        ExralCosmicRenderHelper.renderFlower(blockState, poseStack, bufferSource, packedLight, packedOverlay, stack);

        poseStack.popPose();
    }
    @Override
    public boolean shouldRenderOffScreen(@NotNull AstralKillopTile blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}