package net.xiaoyang010.ex_enigmaticlegacy.api.test.res;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialMiscellaneousModels;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.MiscellaneousModels;

import javax.annotation.Nonnull;
import java.util.Random;

public class RenderCursedSpreader implements BlockEntityRenderer<TileCursedManaSpreader> {

    public RenderCursedSpreader(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(@Nonnull TileCursedManaSpreader spreader, float partialTicks, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);
        Quaternion transform = Vector3f.YP.rotationDegrees(spreader.rotationX + 90.0F);
        transform.mul(Vector3f.XP.rotationDegrees(spreader.rotationY));
        ms.mulPose(transform);
        ms.translate(-0.5, -0.5, -0.5);
        double time = (double) ((float) ClientTickHandler.ticksInGame + partialTicks);
        float r = 1.0F;
        float g = 1.0F;
        float b = 1.0F;
        if (spreader.getVariant() == BlockCursedManaSpreader.Variant.CURSED) {
            int color = Mth.hsvToRgb((float) ((time * 2.0 + (double) (new Random((long) spreader.getBlockPos().hashCode())).nextInt(10000)) % 360.0) / 360.0F, 0.4F, 0.9F);
            r = (float) (color >> 16 & 255) / 255.0F;
            g = (float) (color >> 8 & 255) / 255.0F;
            b = (float) (color & 255) / 255.0F;
        }

        VertexConsumer buffer = buffers.getBuffer(ItemBlockRenderTypes.getRenderType(spreader.getBlockState(), false));
        BakedModel spreaderModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(spreader.getBlockState());
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ms.last(), buffer, spreader.getBlockState(), spreaderModel, r, g, b, light, overlay);
        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);
        ms.mulPose(Vector3f.YP.rotationDegrees((float) time % 360.0F));
        ms.translate(-0.5, -0.5, -0.5);
        ms.translate(0.0, (double) ((float) Math.sin(time / 20.0) * 0.05F), 0.0);
        BakedModel core = this.getCoreModel(spreader);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ms.last(), buffer, spreader.getBlockState(), core, 1.0F, 1.0F, 1.0F, light, overlay);
        ms.popPose();
        ItemStack stack = spreader.getItemHandler().getStackInSlot(0);
        if (!stack.isEmpty()) {
            ms.pushPose();
            ms.translate(0.5, 0.5, 0.09399999678134918);
            ms.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            ms.mulPose(Vector3f.XP.rotationDegrees(180.0F));
            ms.scale(0.997F, 0.997F, 1.0F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.NONE, light, overlay, ms, buffers, 0);
            ms.popPose();
        }

        BakedModel scaffolding;
        if (spreader.paddingColor != null) {
            ms.pushPose();
            ms.translate(0.5, 0.5, 0.5);
            ms.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            ms.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            ms.translate(-0.5, -0.5, -0.5);
            scaffolding = this.getPaddingModel(spreader.paddingColor);
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ms.last(), buffer, spreader.getBlockState(), scaffolding, r, g, b, light, overlay);
            ms.popPose();
        }

        ms.popPose();
        if ((Boolean) spreader.getBlockState().getValue(BotaniaStateProps.HAS_SCAFFOLDING)) {
            scaffolding = this.getScaffoldingModel(spreader);
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ms.last(), buffer, spreader.getBlockState(), scaffolding, r, g, b, light, overlay);
        }

    }

    private BakedModel getCoreModel(TileCursedManaSpreader tile) {
        BakedModel var10000;
        switch (tile.getVariant()) {
            case CURSED -> var10000 = SpecialMiscellaneousModels.INSTANCE.cursedSpreaderCore;
            default -> throw new IncompatibleClassChangeError();
        }

        return var10000;
    }

    private BakedModel getPaddingModel(DyeColor color) {
        return MiscellaneousModels.INSTANCE.spreaderPaddings.get(color);
    }

    private BakedModel getScaffoldingModel(TileCursedManaSpreader tile) {
        BakedModel var10000 = switch (tile.getVariant()) {
            case CURSED -> SpecialMiscellaneousModels.INSTANCE.cursedSpreaderScaffolding;
            default -> throw new IncompatibleClassChangeError();
        };

        return var10000;
    }
}