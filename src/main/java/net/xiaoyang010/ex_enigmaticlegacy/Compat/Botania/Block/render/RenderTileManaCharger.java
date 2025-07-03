package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.ModelManaCharger;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.ManaChargerTile;
import vazkii.botania.client.core.handler.ClientTickHandler;

import java.util.Random;

public class RenderTileManaCharger implements BlockEntityRenderer<ManaChargerTile> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("ex_enigmaticlegacy:textures/entity/mana_charger.png");
    private final ModelManaCharger model;
    public ManaChargerTile charger;

    public RenderTileManaCharger(BlockEntityRendererProvider.Context context) {
        this.model = new ModelManaCharger(context.bakeLayer(ModelManaCharger.LAYER_LOCATION));
    }

    @Override
    public void render(ManaChargerTile tile, float partialTicks, PoseStack ms, MultiBufferSource buffers,
                       int light, int overlay) {
        this.charger = tile;
        float time = tile.getLevel() != null ?
                ClientTickHandler.ticksInGame + partialTicks :
                0f;

        if (tile != null) {
            time += new Random(tile.getBlockPos().getX() ^
                    tile.getBlockPos().getY() ^
                    tile.getBlockPos().getZ())
                    .nextInt(360);
        }

        ms.pushPose();

        ms.translate(0.5D, 1.65D, 0.5D);
        ms.scale(1F, -1F, -1F);

        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(TEXTURE));
        model.render(this, ms, time);

        ms.popPose();
    }

    public void renderItemStack(ItemStack stack, PoseStack ms) {
        if (!stack.isEmpty()) {
            ms.pushPose();

            float scale = 0.25F;
            ms.scale(scale, scale, scale);
            ms.scale(2.0F, 2.0F, 2.0F);

            if (stack.getItem() instanceof BlockItem) {
                ms.scale(0.5F, 0.5F, 0.5F);
                ms.translate(1.0F, 1.1F, 0.0F);
            }

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack,
                    ItemTransforms.TransformType.GROUND,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    ms,
                    this.getMultiBufferSource(),
                    0
            );

            if (stack.getItem() instanceof BlockItem) {
                ms.translate(-1.0F, -1.1F, 0.0F);
                ms.scale(2.0F, 2.0F, 2.0F);
            }

            ms.scale(1.0F / scale, 1.0F / scale, 1.0F / scale);

            ms.popPose();
        }
    }

    public MultiBufferSource getMultiBufferSource() {
        return Minecraft.getInstance().renderBuffers().bufferSource();
    }

    public VertexConsumer getBuffer() {
        return this.getMultiBufferSource().getBuffer(
                RenderType.entityTranslucent(TEXTURE)
        );
    }

    public int getPackedLight() {
        return 15728880;
    }

    public int getPackedOverlay() {
        return OverlayTexture.NO_OVERLAY;
    }
}