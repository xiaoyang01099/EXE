package org.xiaoyang.ex_enigmaticlegacy.api.shader;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yuo.endless.Client.Lib.PerspectiveModelState;
import com.yuo.endless.Client.Model.IItemRenderer;
import com.yuo.endless.Client.Model.WrappedItemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class EndPortalHaloBakedModel extends WrappedItemModel implements IItemRenderer {


    public enum HaloStyle { HALO, SURFACE }
    private final int size;
    private final boolean pulse;
    private final boolean animated;
    private final HaloStyle style;
    private final List<BakedQuad> normalQuads = new ArrayList<>();
    private final List<BakedQuad> portalQuads = new ArrayList<>();
    private static final RandomSource RANDOM = RandomSource.create();

    public EndPortalHaloBakedModel(BakedModel wrapped, BakedModel maskModel,
                                   int size, boolean pulse,
                                   boolean animated, HaloStyle style) {
        super(wrapped);
        this.size     = size;
        this.pulse    = pulse;
        this.animated = animated;
        this.style    = style;

        collectQuads(wrapped, normalQuads);
        if (maskModel != null) {
            collectQuads(maskModel, portalQuads);
        }
    }

    private void collectQuads(BakedModel model, List<BakedQuad> targetList) {
        targetList.addAll(model.getQuads(null, null, RANDOM));
        for (Direction dir : Direction.values()) {
            targetList.addAll(model.getQuads(null, dir, RANDOM));
        }
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack pStack, MultiBufferSource source, int packedLight, int packedOverlay) {
        VertexConsumer normalBuffer = source.getBuffer(RenderType.cutout());
        renderQuads(pStack, normalBuffer, this.normalQuads, packedLight, packedOverlay);

        if (!this.portalQuads.isEmpty()) {
            if (this.style == HaloStyle.SURFACE) {
                renderSurfaceEffect(pStack, source);
            } else if (this.style == HaloStyle.HALO) {
                renderHaloEffect(pStack, source, transformType);
            }
        }

        if (this.pulse) {
            renderPulseEffect(stack, pStack, source, packedLight, packedOverlay);
        }
    }

    private void renderSurfaceEffect(PoseStack pStack, MultiBufferSource source) {
        VertexConsumer portalBuffer = source.getBuffer(RenderType.endPortal());

        pStack.pushPose();
        pStack.translate(0.5, 0.5, 0.5);
        pStack.scale(1.01f, 1.01f, 1.01f);
        pStack.translate(-0.5, -0.5, -0.5);
        renderQuadsPortal(pStack, portalBuffer, this.portalQuads);
        pStack.popPose();

        pStack.pushPose();
        pStack.translate(0.5, 0.5, 0.5);
        pStack.scale(0.99f, 0.99f, 0.99f);
        pStack.translate(-0.5, -0.5, -0.5);
        renderQuadsPortal(pStack, portalBuffer, this.portalQuads);
        pStack.popPose();
    }

    private void renderHaloEffect(PoseStack pStack, MultiBufferSource source, ItemDisplayContext transformType) {
        pStack.pushPose();

        if (transformType == ItemDisplayContext.GUI) {
            pStack.translate(0, 0, -0.01);
        } else {
            pStack.translate(0, 0, -0.05);
        }

        float scale = 1.0F + (this.size / 8.0F);
        pStack.translate(0.5, 0.5, 0.5);
        pStack.scale(scale, scale, scale);

        if (this.animated) {
            float time = (System.currentTimeMillis() % 8000L) / 8000.0F;
            pStack.mulPose(
                    new org.joml.Quaternionf().rotationZ((float)(time * 2.0F * Math.PI))
            );
        }

        pStack.translate(-0.5, -0.5, -0.5);

        VertexConsumer portalBuffer = source.getBuffer(RenderType.endPortal());
        renderQuadsPortal(pStack, portalBuffer, this.portalQuads);

        pStack.popPose();
    }

    private void renderPulseEffect(ItemStack stack, PoseStack pStack,
                                   MultiBufferSource source,
                                   int packedLight, int packedOverlay) {
        pStack.pushPose();

        double time  = System.currentTimeMillis() / 1000.0;
        double scale = 0.95 + Math.sin(time * Math.PI) * 0.05;
        double trans = (1.0 - scale) / 2.0;

        pStack.translate(trans, trans, 0.01);
        pStack.scale((float) scale, (float) scale, 1.0F);

        VertexConsumer buffer = source.getBuffer(RenderType.cutout());
        renderQuads(pStack, buffer, this.normalQuads, packedLight, packedOverlay);

        pStack.popPose();
    }

    private void renderQuads(PoseStack pStack, VertexConsumer buffer, List<BakedQuad> quads, int packedLight, int packedOverlay) {
        for (BakedQuad quad : quads) {
            buffer.putBulkData(
                    pStack.last(), quad,
                    new float[]{1.0F, 1.0F, 1.0F, 1.0F},
                    1.0F, 1.0F, 1.0F,
                    1.0F,
                    new int[]{packedLight, packedLight, packedLight, packedLight},
                    packedOverlay,
                    false
            );
        }
    }

    private void renderQuadsPortal(PoseStack pStack, VertexConsumer buffer,
                                   List<BakedQuad> quads) {
        Matrix4f pose = pStack.last().pose();
        for (BakedQuad quad : quads) {
            int[] data = quad.getVertices();
            for (int i = 0; i < 4; i++) {
                int base = i * 8;
                float x = Float.intBitsToFloat(data[base]);
                float y = Float.intBitsToFloat(data[base + 1]);
                float z = Float.intBitsToFloat(data[base + 2]);

                Vector3f pos = pose.transformPosition(x, y, z, new Vector3f());

                buffer.vertex(pos.x, pos.y, pos.z)
                        .color(255, 255, 255, 255)
                        .endVertex();
            }
        }
    }

    @Override
    public @Nullable PerspectiveModelState getModelState() {
        if (this.parentState instanceof PerspectiveModelState pms) {
            return pms;
        }
        return null;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return this.wrapped.usesBlockLight();
    }
}