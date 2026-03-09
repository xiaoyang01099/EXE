package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
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
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.ModelManaContainer;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.ManaContainerBlock;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.ManaContainerTile;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialMiscellaneousModels;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialRenderHelper;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModModelLayers;
import vazkii.botania.api.mana.IPoolOverlayProvider;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;

import javax.annotation.Nullable;

public class ManaContainerRenderer implements BlockEntityRenderer<ManaContainerTile> {
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/blocks/mana_container/mana_container.png");
    private static final ResourceLocation CREATIVE_TEXTURE = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/blocks/mana_container/creative_container.png");
    private static final ResourceLocation DILUTED_TEXTURE = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/blocks/mana_container/diluted_container.png");

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
    public void render(@Nullable ManaContainerTile pool, float f, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
        ms.pushPose();
        ms.translate(0.5F, 1.5F, 0.5F);

        int mana = pool == null ? cartMana : pool.getCurrentMana();
        int cap = pool == null ? -1 : pool.manaCap;
        if (cap == -1) {
            cap = ManaContainerTile.MAX_MANA_DILLUTED;
        }

        float waterLevel = (float) mana / (float) cap * 0.42F;//最大填充高度

        float s = 1F / 16F;
        float v = 1F / 8F;
        float w = -v * 2.4F;// 减小倍数=向右移，增大倍数=向左移

        if (pool != null) {
            Block below = pool.getLevel().getBlockState(pool.getBlockPos().below()).getBlock();
            if (below instanceof IPoolOverlayProvider overlayProvider) {
                var overlaySpriteId = overlayProvider.getIcon(pool.getLevel(), pool.getBlockPos());
                var overlayIcon = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(overlaySpriteId);
                ms.pushPose();
                float alpha = (float) ((Math.sin((ClientTickHandler.ticksInGame + f) / 20.0) + 1) * 0.3 + 0.2);
                ms.translate(-0.5F, -1F - 0.43F, -0.5F);
                ms.mulPose(Vector3f.XP.rotationDegrees(90F));
                ms.scale(s, s, s);

                VertexConsumer buffer = buffers.getBuffer(RenderHelper.ICON_OVERLAY);
                RenderHelper.renderIcon(ms, buffer, 0, 0, overlayIcon, 16, 16, alpha);

                ms.popPose();
            }
        }

        if (waterLevel > 0) {
            s = 1F / 256F * 9.8F;// 魔力液体大小
            ms.pushPose();
            ms.translate(w, -1F - (-0.2F - waterLevel), w);//魔力初始高度
            ms.mulPose(Vector3f.XP.rotationDegrees(90F));
            ms.scale(s, s, s);

            VertexConsumer buffer = buffers.getBuffer(SpecialRenderHelper.RAINBOW_MANA_WATER);
            SpecialRenderHelper.renderIcon(ms, buffer, 0, 0, SpecialMiscellaneousModels.INSTANCE.rainbowManaWater.sprite(), 16, 16, 1);

            ms.popPose();
        }
        ms.popPose();

        cartMana = -1;

        ms.pushPose();

        ms.translate(0.5, 1.5, 0.5);
        ms.mulPose(Vector3f.XP.rotationDegrees(180));

        ResourceLocation texture = getTextureForVariant(pool);
        ModelManaContainer model = getModelForVariant(pool);

        VertexConsumer solidConsumer = buffers.getBuffer(RenderType.entitySolid(texture));
        model.renderToBuffer(ms, solidConsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        ms.popPose();
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