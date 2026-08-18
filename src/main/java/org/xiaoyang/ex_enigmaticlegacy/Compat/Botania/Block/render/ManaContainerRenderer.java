package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import org.xiaoyang.ex_enigmaticlegacy.Client.model.ModelManaContainer;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.ManaContainerBlock;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.tile.ManaContainerTile;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Model.SpecialMiscellaneousModels;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Model.SpecialRenderHelper;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.SpecialLateRenderQueue;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModModelLayers;
import vazkii.botania.api.mana.PoolOverlayProvider;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;

import javax.annotation.Nullable;

public class ManaContainerRenderer implements BlockEntityRenderer<ManaContainerTile> {
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(Exe.MODID, "textures/block/mana_container/mana_container.png");
    private static final ResourceLocation CREATIVE_TEXTURE = new ResourceLocation(Exe.MODID, "textures/block/mana_container/creative_container.png");
    private static final ResourceLocation DILUTED_TEXTURE = new ResourceLocation(Exe.MODID, "textures/block/mana_container/diluted_container.png");

    public static int cartMana = -1;
    private final BlockRenderDispatcher blockRenderDispatcher;
    private final ModelManaContainer defaultModel;
    private final ModelManaContainer creativeModel;
    private final ModelManaContainer dilutedModel;

    public ManaContainerRenderer(BlockEntityRendererProvider.Context context) {
        this.defaultModel = new ModelManaContainer(context.bakeLayer(ModModelLayers.MANA_CONTAINER));
        this.creativeModel = new ModelManaContainer(context.bakeLayer(ModModelLayers.CREATIVE_CONTAINER));
        this.dilutedModel = new ModelManaContainer(context.bakeLayer(ModModelLayers.DILUTED_CONTAINER));
        this.blockRenderDispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(@Nullable ManaContainerTile pool, float f, PoseStack ms,
                       MultiBufferSource buffers, int light, int overlay) {

        ms.pushPose();
        ms.translate(0.5, 1.5, 0.5);
        ms.mulPose(Axis.XP.rotationDegrees(180));

        ResourceLocation texture = getTextureForVariant(pool);
        ModelManaContainer model = getModelForVariant(pool);

        VertexConsumer solidConsumer = buffers.getBuffer(RenderType.entitySolid(texture));
        model.renderToBuffer(ms, solidConsumer, light, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
        ms.popPose();

        ms.pushPose();
        ms.translate(0.5F, 1.5F, 0.5F);

        int mana = pool == null ? cartMana : pool.getCurrentMana();
        int cap = pool == null ? -1 : pool.manaCap;
        if (cap == -1) {
            cap = ManaContainerTile.MAX_MANA_DILLUTED;
        }

        float waterLevel = (float) mana / (float) cap * 0.42F;

        float s = 1F / 16F;
        float v = 1F / 8F;
        float w = -v * 2.4F;

        if (pool != null) {
            Block below = pool.getLevel().getBlockState(pool.getBlockPos().below()).getBlock();
            if (below instanceof PoolOverlayProvider overlayProvider) {
                var overlaySpriteId = overlayProvider.getIcon(pool.getLevel(), pool.getBlockPos());
                var overlayIcon = Minecraft.getInstance()
                        .getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(overlaySpriteId);
                ms.pushPose();
                float alpha = (float) ((Math.sin((ClientTickHandler.ticksInGame + f) / 20.0) + 1)
                        * 0.3 + 0.2);
                ms.translate(-0.5F, -1F - 0.43F, -0.5F);
                ms.mulPose(Axis.XP.rotationDegrees(90F));
                ms.scale(s, s, s);

                VertexConsumer buffer = buffers.getBuffer(RenderHelper.ICON_OVERLAY);
                RenderHelper.renderIconFullBright(ms, buffer, overlayIcon, 0xFFFFFF, alpha);
                ms.popPose();
            }
        }
        
        if (waterLevel > 0) {
            s = 1F / 256F * 9.8F;
            ms.pushPose();
            ms.translate(w, -1F - (-0.2F - waterLevel), w);
            ms.mulPose(Axis.XP.rotationDegrees(90F));
            ms.scale(s, s, s);

            VertexConsumer buffer = SpecialLateRenderQueue.select(buffers, SpecialRenderHelper.RAINBOW_MANA_WATER, SpecialRenderHelper.RAINBOW_MANA_WATER_AFTER_LEVEL);
            SpecialRenderHelper.renderIcon(ms, buffer, 0, 0, SpecialMiscellaneousModels.INSTANCE.rainbowManaWater.sprite(), 16, 16, 0.5F);
            ms.popPose();
        }

        ms.popPose();

        cartMana = -1;
    }

    private ResourceLocation getTextureForVariant(@Nullable ManaContainerTile pool) {
        if (pool != null && pool.getBlockState().getBlock() instanceof ManaContainerBlock block) {
            return switch (block.variant) {
                case CREATIVE -> CREATIVE_TEXTURE;
                case DILUTED -> DILUTED_TEXTURE;
                default -> DEFAULT_TEXTURE;
            };
        }
        return DEFAULT_TEXTURE;
    }

    private ModelManaContainer getModelForVariant(@Nullable ManaContainerTile pool) {
        if (pool != null && pool.getBlockState().getBlock() instanceof ManaContainerBlock block) {
            return switch (block.variant) {
                case CREATIVE -> creativeModel;
                case DILUTED -> dilutedModel;
                default -> defaultModel;
            };
        }
        return defaultModel;
    }
}