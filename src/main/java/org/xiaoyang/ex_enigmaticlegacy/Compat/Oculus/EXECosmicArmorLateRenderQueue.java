package org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yuo.endless.client.AvaritiaShaders;
import com.yuo.endless.client.model.InfinityArmorModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class EXECosmicArmorLateRenderQueue {
    private static final List<ArmorPartRender> QUEUE = new ArrayList<>();
    private static final List<WingRender> WING_QUEUE = new ArrayList<>();
    private static final List<PlayerLayerRender> PLAYER_LAYER_QUEUE = new ArrayList<>();

    public static void enqueuePart(
            PoseStack poseStack,
            ModelPart part,
            ResourceLocation texture,
            int light,
            int overlay,
            float r, float g, float b, float a
    ) {
        PoseStack.Pose pose = poseStack.last();
        QUEUE.add(new ArmorPartRender(
                new Matrix4f(pose.pose()),
                new Matrix3f(pose.normal()),
                part, texture, light, overlay, r, g, b, a
        ));
    }

    public static void enqueueWing(
            PoseStack poseStack,
            InfinityArmorModel model,
            ResourceLocation texture,
            int light,
            int overlay,
            float r, float g, float b, float a
    ) {
        PoseStack.Pose pose = poseStack.last();
        WING_QUEUE.add(new WingRender(
                new Matrix4f(pose.pose()),
                new Matrix3f(pose.normal()),
                model, texture, light, overlay, r, g, b, a
        ));
    }

    public static void enqueuePlayerLayer(
            PoseStack poseStack,
            net.minecraft.client.model.PlayerModel<net.minecraft.world.entity.player.Player> playerModel,
            int light
    ) {
        PoseStack.Pose pose = poseStack.last();
        PLAYER_LAYER_QUEUE.add(new PlayerLayerRender(
                new Matrix4f(pose.pose()),
                new Matrix3f(pose.normal()),
                List.of(
                        playerModel.head, playerModel.hat, playerModel.body,
                        playerModel.leftArm, playerModel.rightArm,
                        playerModel.leftLeg, playerModel.rightLeg
                ),
                light
        ));
    }

    public static void renderAfterLevel() {
        if (QUEUE.isEmpty() && WING_QUEUE.isEmpty() && PLAYER_LAYER_QUEUE.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        try {
            EXELatePassState.prepare();

            for (ArmorPartRender render : QUEUE) {
                render.execute(buffers);
            }

            for (WingRender render : WING_QUEUE) {
                render.execute(buffers);
            }

            if (!PLAYER_LAYER_QUEUE.isEmpty()) {
                AvaritiaShaders.cosmicOpacity.set(2.0F);
                for (PlayerLayerRender render : PLAYER_LAYER_QUEUE) {
                    render.execute(buffers);
                }
            }

            buffers.endBatch();
        } finally {
            EXELatePassState.finish();QUEUE.clear();
            WING_QUEUE.clear();
            PLAYER_LAYER_QUEUE.clear();
        }
    }

    public static void endFrame() {
        QUEUE.clear();
        WING_QUEUE.clear();
        PLAYER_LAYER_QUEUE.clear();
    }

    // -------------------------------------------------------------------------

    private static class ArmorPartRender {
        final Matrix4f poseMatrix;
        final Matrix3f normalMatrix;
        final ModelPart part;
        final ResourceLocation texture;
        final int light, overlay;
        final float r, g, b, a;

        ArmorPartRender(Matrix4f p, Matrix3f n, ModelPart part, ResourceLocation texture,int light, int overlay, float r, float g, float b, float a) {
            this.poseMatrix = p;
            this.normalMatrix = n;
            this.part = part;
            this.texture = texture;
            this.light = light;
            this.overlay = overlay;
            this.r = r; this.g = g; this.b = b; this.a = a;
        }

        void execute(MultiBufferSource bufferSource) {
            PoseStack stack = new PoseStack();
            stack.last().pose().mul(this.poseMatrix);
            stack.last().normal().mul(this.normalMatrix);

            VertexConsumer buffer = InfinityArmorModel.material(texture)
                    .buffer(bufferSource, InfinityArmorModel::mask2);
            part.render(stack, buffer, light, overlay, r, g, b, a);
        }
    }

    private static class WingRender {
        final Matrix4f poseMatrix;
        final Matrix3f normalMatrix;
        final InfinityArmorModel model;
        final ResourceLocation texture;
        final int light, overlay;
        final float r, g, b, a;

        WingRender(Matrix4f p, Matrix3f n, InfinityArmorModel model, ResourceLocation texture,
                   int light, int overlay, float r, float g, float b, float a) {
            this.poseMatrix = p;
            this.normalMatrix = n;
            this.model = model;
            this.texture = texture;
            this.light = light;
            this.overlay = overlay;
            this.r = r; this.g = g; this.b = b; this.a = a;
        }

        void execute(MultiBufferSource bufferSource) {
            PoseStack stack = new PoseStack();
            stack.last().pose().mul(this.poseMatrix);
            stack.last().normal().mul(this.normalMatrix);

            VertexConsumer buffer = InfinityArmorModel.material(texture)
                    .buffer(bufferSource, InfinityArmorModel::mask2);
            model.renderToBufferWing(stack, buffer, light, overlay, r, g, b, a);
        }
    }

    private static class PlayerLayerRender {
        final Matrix4f poseMatrix;
        final Matrix3f normalMatrix;
        final List<ModelPart> parts;
        final int light;

        PlayerLayerRender(Matrix4f p, Matrix3f n, List<ModelPart> parts, int light) {
            this.poseMatrix = p;
            this.normalMatrix = n;
            this.parts = parts;
            this.light = light;
        }

        void execute(MultiBufferSource bufferSource) {
            PoseStack stack = new PoseStack();
            stack.last().pose().mul(this.poseMatrix);
            stack.last().normal().mul(this.normalMatrix);

            VertexConsumer buffer = InfinityArmorModel.material(InfinityArmorModel.MASK_INV)
                    .buffer(bufferSource, InfinityArmorModel::mask2);
            for (ModelPart part : parts) {
                part.render(stack, buffer, light, OverlayTexture.NO_OVERLAY,
                        1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    private EXECosmicArmorLateRenderQueue() {}
}