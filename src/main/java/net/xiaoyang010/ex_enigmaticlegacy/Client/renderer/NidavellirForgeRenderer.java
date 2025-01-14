package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer;/*package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.NidavellirForgeTile;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlocks;

public class NidavellirForgeRenderer extends BlockEntityWithoutLevelRenderer {
    private final NidavellirForgeTile forge = new NidavellirForgeTile(BlockPos.ZERO,
            ModBlocks.NIDAVELLIR_FORGE.get().defaultBlockState());

    public NidavellirForgeRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transform,
                             PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        ms.pushPose();
        ms.translate(0.5F, 0.375F, 0.5F);
        ms.scale(1.25F, 1.25F, 1.25F);
        ms.popPose();
    }
}*/