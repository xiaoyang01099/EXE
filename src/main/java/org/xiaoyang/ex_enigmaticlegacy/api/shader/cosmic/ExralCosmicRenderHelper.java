package org.xiaoyang.ex_enigmaticlegacy.api.shader.cosmic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus.EXERainbowCosmicBlockLateRenderQueue;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.ArrayList;
import java.util.List;

public final class ExralCosmicRenderHelper {

    private ExralCosmicRenderHelper() {
    }

    public static void renderBlockQuads(BlockState blockState, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay, ItemStack stack) {
        renderBlockQuads(blockState, poseStack, buffers, packedLight, packedOverlay, stack, 1.0F);
    }

    public static void renderBlockQuads(BlockState blockState, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay, ItemStack stack, float opacity) {
        if (!isShaderAvailable()) {
            return;
        }

        if (EXERainbowCosmicBlockLateRenderQueue.shouldDefer()) {
            EXERainbowCosmicBlockLateRenderQueue.enqueue(blockState, stack, poseStack, packedLight, packedOverlay, opacity, false);
            return;
        }

        renderNow(blockState, poseStack, buffers, packedLight, packedOverlay, stack, opacity, EXEShaders.COSMIC_BLOCK_RENDER_TYPE
        );
    }

    public static void renderFlower(BlockState blockState, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay, ItemStack stack) {
        if (!isShaderAvailable()) {
            return;
        }

        if (EXERainbowCosmicBlockLateRenderQueue.shouldDefer()) {
            EXERainbowCosmicBlockLateRenderQueue.enqueue(blockState, stack, poseStack, packedLight, packedOverlay, 1.0F, true);
            return;
        }

        renderNow(blockState, poseStack, buffers, packedLight, packedOverlay, stack, 1.0F, EXEShaders.COSMIC_FLOWER_BLOCK_RENDER_TYPE
        );
    }


    public static void renderDeferredBlock(BlockState blockState, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay, ItemStack stack, float opacity) {
        renderNow(blockState, poseStack, buffers, packedLight, packedOverlay, stack, opacity, EXEShaders.COSMIC_BLOCK_AFTER_LEVEL_RENDER_TYPE);
    }

    public static void renderDeferredFlower(BlockState blockState, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay, ItemStack stack) {
        renderNow(blockState, poseStack, buffers, packedLight, packedOverlay, stack, 1.0F, EXEShaders.COSMIC_BLOCK_AFTER_LEVEL_RENDER_TYPE);
    }

    private static void renderNow(BlockState blockState, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay, ItemStack stack, float opacity, RenderType renderType) {
        if (!isShaderAvailable()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        setupShaderUniforms(mc, opacity);
        setupCosmicTextures(mc);

        BakedModel model = mc.getBlockRenderer().getBlockModel(blockState);
        List<BakedQuad> quads = getAllQuads(model, blockState, mc);

        if (quads.isEmpty()) {
            return;
        }

        VertexConsumer consumer = buffers.getBuffer(renderType);

        mc.getItemRenderer().renderQuadList(
                poseStack,
                consumer,
                quads,
                stack,
                packedLight,
                packedOverlay
        );
    }

    private static boolean isShaderAvailable() {
        return EXEShaders.cosmicShader != null
                && EXEShaders.cosmicTime != null
                && EXEShaders.cosmicYaw != null
                && EXEShaders.cosmicPitch != null
                && EXEShaders.cosmicExternalScale != null
                && EXEShaders.cosmicOpacity != null
                && EXEShaders.cosmicUVs != null;
    }

    private static void setupShaderUniforms(Minecraft mc, float opacity) {
        float yaw = 0.0F;
        float pitch = 0.0F;
        float scale = EXEShaders.inventoryRender ? 100.0F : 1.0F;

        if (!EXEShaders.inventoryRender && mc.player != null) {
            yaw = (float) Math.toRadians(mc.player.getYRot());
            pitch = -(float) Math.toRadians(mc.player.getXRot());
        }

        EXEShaders.cosmicTime.set(
                (float) EXEShaders.renderTime + EXEShaders.renderFrame
        );
        EXEShaders.cosmicYaw.set(yaw);
        EXEShaders.cosmicPitch.set(pitch);
        EXEShaders.cosmicExternalScale.set(scale);
        EXEShaders.cosmicOpacity.set(opacity);
    }

    private static void setupCosmicTextures(Minecraft mc) {
        TextureAtlas atlas = mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);

        for (int i = 0; i < 10; ++i) {
            TextureAtlasSprite sprite = atlas.getSprite(
                    Exe.path("shader/cosmic_" + i)
            );

            int offset = i * 4;
            EXEShaders.COSMIC_UVS[offset] = sprite.getU0();
            EXEShaders.COSMIC_UVS[offset + 1] = sprite.getV0();
            EXEShaders.COSMIC_UVS[offset + 2] = sprite.getU1();
            EXEShaders.COSMIC_UVS[offset + 3] = sprite.getV1();
        }

        EXEShaders.cosmicUVs.set(EXEShaders.COSMIC_UVS);
    }

    private static List<BakedQuad> getAllQuads(BakedModel model, BlockState blockState, Minecraft mc) {
        List<BakedQuad> quads = new ArrayList<>();

        if (mc.level == null) {
            return quads;
        }

        for (Direction direction : Direction.values()) {
            quads.addAll(model.getQuads(blockState, direction, mc.level.random));
        }

        quads.addAll(model.getQuads(blockState, null, mc.level.random));

        return quads;
    }
}