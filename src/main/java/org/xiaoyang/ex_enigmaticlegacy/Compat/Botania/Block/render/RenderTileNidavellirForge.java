package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.xiaoyang.ex_enigmaticlegacy.Client.model.ModelNidavellirForge;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.NidavellirForgeBlock;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.tile.NidavellirForgeTile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RenderTileNidavellirForge implements BlockEntityRenderer<NidavellirForgeTile> {
    private List<ItemStack> itemList = null;
    private static final ResourceLocation TEXTURE = new ResourceLocation("ex_enigmaticlegacy:textures/item/entity/nidavellir_forge.png");
    private final ModelNidavellirForge model;

    public RenderTileNidavellirForge(BlockEntityRendererProvider.Context context) {
        this.model = new ModelNidavellirForge(context.bakeLayer(ModelNidavellirForge.LAYER_LOCATION));
    }

    @Override
    public void render(NidavellirForgeTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        float worldTime = 0.0F;
        int meta = 2;
        float invRender = 0.0F;

        Level level = tile.getLevel();

        if (tile != null && level != null) {
            worldTime = (level.getGameTime() + partialTicks)
                    + new Random(tile.getBlockPos().getX() ^ tile.getBlockPos().getY() ^ tile.getBlockPos().getZ()).nextInt(360);
            meta = tile.getBlockState().getValue(NidavellirForgeBlock.FACING).get2DDataValue();
        } else {
            invRender = 0.0875F;
        }

        float indetY = (float) (Math.sin(worldTime / 18.0F) / 24.0F);

        poseStack.pushPose();
        poseStack.translate(0.5D - invRender, 1.5D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90F * meta));

        VertexConsumer vertexConsumer = buffer.getBuffer(model.renderType(TEXTURE));

        poseStack.translate(0.0F, indetY, 0.0F);
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();

        if (itemList == null) {
            itemList = new ArrayList<>();
            for (int i = 0; i < tile.getContainerSize(); i++) {
                itemList.add(ItemStack.EMPTY);
            }
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.675F - indetY, 0.5F);
        poseStack.scale(0.2F, 0.225F, 0.225F);

        for (int i = 1; i < tile.getContainerSize(); i++) {
            ItemStack stack = tile.getItem(i);
            if (!stack.isEmpty()) {
                poseStack.pushPose();
                switch (i) {
                    case 1 -> poseStack.translate( 0.4F, 0.0F,  0.0F);
                    case 2 -> poseStack.translate(-0.4F, 0.0F, -0.3F);
                    case 3 -> poseStack.translate(-0.4F, 0.0F,  0.3F);
                    case 4 -> poseStack.translate( 0.4F, 0.0F, -0.3F);
                    case 5 -> poseStack.translate( 0.4F, 0.0F,  0.3F);
                    case 6 -> poseStack.translate( 0.0F, 0.0F,  0.5F);
                }

                itemRenderer.renderStatic(
                        stack,
                        ItemDisplayContext.GROUND,
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        poseStack,
                        buffer,
                        level,
                        i
                );
                poseStack.popPose();
            }
        }

        poseStack.popPose();

        ItemStack mainStack = tile.getItem(0);
        if (!mainStack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.915F - indetY, 0.5F);
            poseStack.scale(0.45F, 0.45F, 0.45F);

            itemRenderer.renderStatic(
                    mainStack,
                    ItemDisplayContext.GROUND,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    level,
                    0
            );

            poseStack.popPose();
        }
    }
}