package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

public final class ExralCosmicRenderHelper {
    private ExralCosmicRenderHelper() {
    }

    public static void renderBlockQuads(BlockState blockState, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay, ItemStack stack) {
        renderBlockQuads(blockState, poseStack, buffers, packedLight, packedOverlay, stack, 1.0F);
    }

    public static void renderBlockQuads(BlockState blockState, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay, ItemStack stack, float opacity) {
        if (EXEShaders.cosmicShader == null ||
                EXEShaders.cosmicTime == null ||
                EXEShaders.cosmicYaw == null ||
                EXEShaders.cosmicPitch == null ||
                EXEShaders.cosmicExternalScale == null ||
                EXEShaders.cosmicOpacity == null ||
                EXEShaders.cosmicUVs == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        setupShaderUniforms(mc, opacity);
        setupCosmicTextures(mc);

        BakedModel model = mc.getBlockRenderer().getBlockModel(blockState);
        List<BakedQuad> allQuads = getAllQuads(model, blockState, mc);

        if (!allQuads.isEmpty()) {
            VertexConsumer consumer = buffers.getBuffer(EXEShaders.COSMIC_BLOCK_RENDER_TYPE);
            mc.getItemRenderer().renderQuadList(poseStack, consumer, allQuads, stack, packedLight, packedOverlay);
        }
    }

    public static void renderFlower(BlockState blockState, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay, ItemStack stack) {
        if (EXEShaders.cosmicShader == null || EXEShaders.cosmicUVs == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        setupShaderUniforms(mc, 1.0F);
        setupCosmicTextures(mc);

        BakedModel model = mc.getBlockRenderer().getBlockModel(blockState);
        List<BakedQuad> allQuads = getAllQuads(model, blockState, mc);

        if (!allQuads.isEmpty()) {
            RenderType renderType = isFlowerBlock(blockState) ?
                    createFlowerCosmicRenderType() : EXEShaders.COSMIC_FLOWER_BLOCK_RENDER_TYPE;

            VertexConsumer consumer = buffers.getBuffer(renderType);
            mc.getItemRenderer().renderQuadList(poseStack, consumer, allQuads, stack, packedLight, packedOverlay);
        }
    }

    private static void setupShaderUniforms(Minecraft mc, float opacity) {
        float yaw = 0.0F;
        float pitch = 0.0F;
        float scale = EXEShaders.inventoryRender ? 100.0F : 1.0F;

        if (!EXEShaders.inventoryRender && mc.player != null) {
            yaw = (float)((double)(mc.player.getYRot() * 2.0F) * Math.PI / 360.0D);
            pitch = -((float)((double)(mc.player.getXRot() * 2.0F) * Math.PI / 360.0D));
        }

        EXEShaders.cosmicTime.set((float)(System.currentTimeMillis() - (long) EXEShaders.renderTime) / 2000.0F);
        EXEShaders.cosmicYaw.set(yaw);
        EXEShaders.cosmicPitch.set(pitch);
        EXEShaders.cosmicExternalScale.set(scale);
        EXEShaders.cosmicOpacity.set(opacity);
    }

    private static void setupCosmicTextures(Minecraft mc) {
        for(int i = 0; i < 10; ++i) {
            TextureAtlasSprite sprite = mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .getSprite(ExEnigmaticlegacyMod.path("shader/cosmic_" + i));
            EXEShaders.COSMIC_UVS[i * 4] = sprite.getU0();
            EXEShaders.COSMIC_UVS[i * 4 + 1] = sprite.getV0();
            EXEShaders.COSMIC_UVS[i * 4 + 2] = sprite.getU1();
            EXEShaders.COSMIC_UVS[i * 4 + 3] = sprite.getV1();
        }
        EXEShaders.cosmicUVs.glUniformF(false, EXEShaders.COSMIC_UVS);
    }

    private static List<BakedQuad> getAllQuads(BakedModel model, BlockState blockState, Minecraft mc) {
        List<BakedQuad> allQuads = new ArrayList<>();

        for(Direction direction : Direction.values()) {
            if (mc.level != null) {
                allQuads.addAll(model.getQuads(blockState, direction, mc.level.random));
            }
        }

        if (mc.level != null) {
            allQuads.addAll(model.getQuads(blockState, null, mc.level.random));
        }

        return allQuads;
    }

    private static boolean isFlowerBlock(BlockState blockState) {
        return blockState.getBlock() instanceof FlowerBlock;
    }

    private static RenderType createFlowerCosmicRenderType() {
        return EXEShaders.COSMIC_FLOWER_BLOCK_RENDER_TYPE;
    }
}