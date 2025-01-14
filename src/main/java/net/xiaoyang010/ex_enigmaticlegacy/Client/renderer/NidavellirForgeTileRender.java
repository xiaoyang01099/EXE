package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer;/*package net.xiaoyang010.ex_enigmaticlegacy.Client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.ModelNidavellirForge;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.NidavellirForgeTile;
import vazkii.botania.client.core.handler.ClientTickHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NidavellirForgeTileRender implements BlockEntityRenderer<NidavellirForgeTile> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("advancedbotany", "textures/model/nidavellir_forge.png");
    private final ModelNidavellirForge model;
    private List<ItemEntity> entityList = null;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public NidavellirForgeTileRender(BlockEntityRendererProvider.Context context) {
        this.model = new ModelNidavellirForge(context.bakeLayer(ModelNidavellirForge.LAYER_LOCATION));
    }


    @Override
    public void render(NidavellirForgeTile tile, float partialTicks, PoseStack ms, MultiBufferSource buffers,
                       int light, int overlay) {
        double worldTime = 0;
        float invRender = 0;
        int meta = 2;

        if (tile != null && tile.getLevel() != null) {
            worldTime = ClientTickHandler.ticksInGame + partialTicks +
                    new Random(tile.getBlockPos().asLong()).nextInt(360);
            Direction facing = (Direction) tile.getBlockState().getValue(tile.getBlockState().getBlock().getStateDefinition().getProperty("facing"));
            if (facing != null) {
                meta = facing.get2DDataValue();
            }
            if (facing != null) {
                meta = facing.get2DDataValue();
            }
        } else {
            invRender = 0.0875F;
        }

        float indetY = (float) (Math.sin(worldTime / 18.0F) / 24.0F);

        // 渲染基座模型
        ms.pushPose();
        ms.translate(-invRender, 0, 0);
        ms.translate(0.5F, 1.5F, 0.5F);
        ms.mulPose(Vector3f.XP.rotationDegrees(180));
        ms.mulPose(Vector3f.YP.rotationDegrees(90 * meta));

        VertexConsumer buffer = buffers.getBuffer(RenderType.entitySolid(TEXTURE));
        model.renderBottom(ms, buffer, light, overlay);

        ms.translate(0, indetY, 0);
        model.renderTop(ms, buffer, light, overlay);
        ms.popPose();

        if (entityList == null && tile.getLevel() != null) {
            List<ItemEntity> list = new ArrayList<>();
            for (int i = 0; i < tile.getContainerSize(); i++) {
                ItemEntity entity = new ItemEntity(tile.getLevel(),
                        tile.getBlockPos().getX(),
                        tile.getBlockPos().getY(),
                        tile.getBlockPos().getZ(),
                        ItemStack.EMPTY); // 添加默认的空物品堆
                entity.setPickUpDelay(40);
                list.add(entity);
            }
            entityList = list;
        } else if (entityList == null) {
            entityList = new ArrayList<>();
            for (int i = 0; i < tile.getContainerSize(); i++) {
                entityList.add(null);
            }
        }

        // 渲染输入物品
        ms.pushPose();
        ms.translate(0.5F, 0.675F - indetY, 0.5F);
        ms.scale(0.2F, 0.225F, 0.225F);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        for (int i = 1; i < entityList.size(); i++) {
            ms.pushPose();
            ItemStack stack = tile.getItem(i);
            if (!stack.isEmpty()) {
                switch (i) {
                    case 1 -> ms.translate(0.15F, 0, 0);
                    case 2 -> ms.translate(-0.15F, 0, -0.15F);
                    case 3 -> ms.translate(-0.15F, 0, 0.15F);
                }

                entityList.get(i).setItem(stack);
                ms.mulPose(Vector3f.YP.rotationDegrees((float) worldTime));
                itemRenderer.renderStatic(stack, ItemTransforms.TransformType.GROUND,
                        light, overlay, ms, buffers, 0);
            }
            ms.popPose();
        }
        ms.popPose();

        // 渲染输出物品
        ItemStack outputStack = tile.getItem(0);
        if (!outputStack.isEmpty()) {
            ms.pushPose();
            ms.translate(0.5F, 0.915F - indetY, 0.5F);
            ms.scale(0.45F, 0.45F, 0.45F);
            entityList.get(0).setItem(outputStack);
            ms.mulPose(Vector3f.YP.rotationDegrees((float) worldTime * 2));
            itemRenderer.renderStatic(outputStack, ItemTransforms.TransformType.GROUND,
                    light, overlay, ms, buffers, 0);
            ms.popPose();
        }
    }
}*/