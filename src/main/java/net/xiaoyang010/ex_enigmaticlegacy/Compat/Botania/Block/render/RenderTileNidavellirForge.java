package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.ModelNidavellirForge;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.NidavellirForgeBlock;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.NidavellirForgeTile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RenderTileNidavellirForge implements BlockEntityRenderer<NidavellirForgeTile> {
    private List<ItemEntity> entityList = null;
    private static final ResourceLocation TEXTURE = new ResourceLocation("ex_enigmaticlegacy:textures/entity/nidavellir_forge.png");
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

        if (tile != null && tile.getLevel() != null) {
            worldTime = (tile.getLevel().getGameTime() + partialTicks)
                    + new Random(tile.getBlockPos().getX() ^ tile.getBlockPos().getY() ^ tile.getBlockPos().getZ()).nextInt(360);
            meta = tile.getBlockState().getValue(NidavellirForgeBlock.FACING).get2DDataValue();
        } else {
            invRender = 0.0875F;
        }

        float indetY = (float) (Math.sin(worldTime / 18.0F) / 24.0F);

        poseStack.pushPose();
        poseStack.translate(0.5D - invRender, 1.5D, 0.5D);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180F));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(90F * meta));

        VertexConsumer vertexConsumer = buffer.getBuffer(model.renderType(TEXTURE));

        poseStack.translate(0.0F, indetY, 0.0F);
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();

        if (entityList == null) {
            List<ItemEntity> list = new ArrayList<>();
            for (int i = 0; i < tile.getContainerSize(); i++) {
                list.add(new ItemEntity(tile.getLevel(),
                        tile.getBlockPos().getX(),
                        tile.getBlockPos().getY(),
                        tile.getBlockPos().getZ(),
                        ItemStack.EMPTY));
            }
            entityList = list;
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();


        poseStack.pushPose();
        poseStack.translate(0.5F, 0.675F - indetY, 0.5F);
        poseStack.scale(0.2F, 0.225F, 0.225F);

        for (int i = 1; i < entityList.size(); i++) {
            poseStack.pushPose();
            ItemStack stack = tile.getItem(i);
            if (!stack.isEmpty()) {
                switch (i) {
                    case 1 -> poseStack.translate(0.4F, 0.0F, 0.0F);       // 右 (更远)
                    case 2 -> poseStack.translate(-0.4F, 0.0F, -0.3F);     // 左后 (更远)
                    case 3 -> poseStack.translate(-0.4F, 0.0F, 0.3F);      // 左前 (更远)
                    case 4 -> poseStack.translate(0.4F, 0.0F, -0.3F);      // 右后 (更远)
                    case 5 -> poseStack.translate(0.4F, 0.0F, 0.3F);       // 右前 (更远)
                    case 6 -> poseStack.translate(0.0F, 0.0F, 0.5F);       // 正前 (更远)
                }

                ItemEntity entity = entityList.get(i);
                entity.setItem(stack);
                entity.tickCount = (int) tile.getLevel().getGameTime();
                itemRenderer.renderStatic(stack, ItemTransforms.TransformType.GROUND,
                        packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, 0);
            }
            poseStack.popPose();
        }

        poseStack.popPose();

        ItemStack mainStack = tile.getItem(0);
        if (!mainStack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.915F - indetY, 0.5F);
            poseStack.scale(0.45F, 0.45F, 0.45F);

            ItemEntity mainEntity = entityList.get(0);
            mainEntity.setItem(mainStack);
            mainEntity.tickCount = (int) tile.getLevel().getGameTime();
            itemRenderer.renderStatic(mainStack, ItemTransforms.TransformType.GROUND,
                    packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, 0);

            poseStack.popPose();
        }
    }
}