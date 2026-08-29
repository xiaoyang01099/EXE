package org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.yuo.endless.client.model.InfinityArmorModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class EXEMobLateRenderQueue {
    private static final List<Entry> QUEUE = new ArrayList<>();
    private static boolean deferThisFrame;

    public static void beginFrame(EXERenderFrameState.Snapshot snapshot) {
        QUEUE.clear();

        deferThisFrame =
                snapshot.worldRenderActive()
                        && snapshot.shaderPackActive();
    }

    public static boolean shouldDefer() {
        return deferThisFrame;
    }

    public static void enqueue(
            EntityModel<?> model,
            PoseStack poseStack,
            LivingEntity entity,
            int packedLight,
            int packedOverlay
    ) {
        if (!deferThisFrame
                || model == null
                || entity == null
                || EXERenderFrameState.isShadowPass()) {
            return;
        }

        PoseStack.Pose pose = poseStack.last();

        QUEUE.add(
                new Entry(
                        model,
                        entity,
                        new Matrix4f(pose.pose()),
                        new Matrix3f(pose.normal()),
                        new Matrix4f(RenderSystem.getModelViewMatrix()),
                        new Matrix4f(RenderSystem.getProjectionMatrix()),
                        packedLight,
                        packedOverlay
                )
        );
    }

    public static void renderAfterLevel() {
        if (!deferThisFrame || QUEUE.isEmpty()) {
            QUEUE.clear();
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            QUEUE.clear();
            return;
        }

        MultiBufferSource.BufferSource buffers =
                MultiBufferSource.immediate(
                        new BufferBuilder(1_048_576)
                );

        Matrix4f previousProjection =
                new Matrix4f(RenderSystem.getProjectionMatrix());

        PoseStack modelViewStack =
                RenderSystem.getModelViewStack();

        modelViewStack.pushPose();

        try {
            EXELatePassState.prepare();

            for (Entry entry : QUEUE) {
                if (entry.entity.isRemoved()) {
                    continue;
                }

                modelViewStack.last()
                        .pose()
                        .set(entry.modelView);

                RenderSystem.applyModelViewMatrix();

                RenderSystem.setProjectionMatrix(
                        new Matrix4f(entry.projection),
                        VertexSorting.DISTANCE_TO_ORIGIN
                );

                PoseStack entityPoseStack = new PoseStack();

                entityPoseStack.last()
                        .pose()
                        .set(entry.pose);

                entityPoseStack.last()
                        .normal()
                        .set(entry.normal);

                ResourceLocation texture =
                        InfinityArmorModel.MASK_INV;

                VertexConsumerAccess.renderModel(
                        entry.model,
                        entityPoseStack,
                        buffers,
                        texture,
                        entry.packedLight,
                        entry.packedOverlay
                );
            }

            buffers.endBatch();
        } finally {
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.DISTANCE_TO_ORIGIN);

            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();

            EXELatePassState.finish();
            QUEUE.clear();
        }
    }

    public static void endFrame() {
        QUEUE.clear();
        deferThisFrame = false;
    }

    private record Entry(
            EntityModel<?> model,
            LivingEntity entity,
            Matrix4f pose,
            Matrix3f normal,
            Matrix4f modelView,
            Matrix4f projection,
            int packedLight,
            int packedOverlay
    ) {
    }

    private static final class VertexConsumerAccess {
        private static void renderModel(
                EntityModel<?> model,
                PoseStack poseStack,
                MultiBufferSource.BufferSource buffers,
                ResourceLocation texture,
                int packedLight,
                int packedOverlay
        ) {
            model.renderToBuffer(
                    poseStack,
                    InfinityArmorModel.material(texture).buffer(buffers, InfinityArmorModel::mask2),
                    packedLight,
                    packedOverlay,
                    0.84F,
                    1.0F,
                    0.95F,
                    0.8F
            );
        }
    }

    private EXEMobLateRenderQueue() {
    }
}