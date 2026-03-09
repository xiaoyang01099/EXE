package net.xiaoyang010.ex_enigmaticlegacy.api.test.res;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialMiscellaneousModels;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialRenderHelper;
import vazkii.botania.api.mana.IPoolOverlayProvider;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.helper.MathHelper;

import javax.annotation.Nullable;
import java.util.Random;

public class RenderTileCursedPool implements BlockEntityRenderer<TileCursedManaPool> {

    public static int cartMana = -1;
    private final BlockRenderDispatcher blockRenderDispatcher;

    public RenderTileCursedPool(BlockEntityRendererProvider.Context ctx) {
        this.blockRenderDispatcher = ctx.getBlockRenderDispatcher();
    }

    @Override
    public void render(@Nullable TileCursedManaPool pool, float f, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
        ms.pushPose();

        boolean fab = pool != null && ((BlockCursedManaPool) pool.getBlockState().getBlock()).variant == BlockCursedManaPool.Variant.CORRUPTED;

        if (fab) {
            float time = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks;
            time += new Random(pool.getBlockPos().getX() ^ pool.getBlockPos().getY() ^ pool.getBlockPos().getZ()).nextInt(100000);
            time *= 0.005F;
            int poolColor = ColorHelper.getColorValue(pool.getCursedColor());
            int color = MathHelper.multiplyColor(Mth.hsvToRgb(Mth.frac(time), 0.6F, 1F), poolColor);

            int red = (color & 0xFF0000) >> 16;
            int green = (color & 0xFF00) >> 8;
            int blue = color & 0xFF;
            BlockState state = pool.getBlockState();
            BakedModel model = blockRenderDispatcher.getBlockModel(state);
            VertexConsumer buffer = buffers.getBuffer(ItemBlockRenderTypes.getRenderType(state, false));
            blockRenderDispatcher.getModelRenderer()
                    .renderModel(ms.last(), buffer, state, model, red / 255F, green / 255F, blue / 255F, light, overlay);
        }

        ms.translate(0.5F, 1.5F, 0.5F);

        int mana = pool == null ? cartMana : pool.getCurrentCursedMana();
        int cap = pool == null ? TileCursedManaPool.MAX_MANA : pool.getMaxCursedMana();

        float waterLevel = (float) mana / (float) cap * 0.4F;

        float s = 1F / 16F;
        float v = 1F / 8F;
        float w = -v * 3.5F;

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
            s = 1F / 256F * 14F;
            ms.pushPose();
            ms.translate(w, -1F - (0.43F - waterLevel), w);
            ms.mulPose(Vector3f.XP.rotationDegrees(90F));
            ms.scale(s, s, s);

            VertexConsumer buffer = buffers.getBuffer(SpecialRenderHelper.EVIL_WATER);
            RenderHelper.renderIcon(ms, buffer, 0, 0, SpecialMiscellaneousModels.INSTANCE.evilManaWater.sprite(), 16, 16, 1);

            ms.popPose();
        }
        ms.popPose();

        cartMana = -1;
    }

}
