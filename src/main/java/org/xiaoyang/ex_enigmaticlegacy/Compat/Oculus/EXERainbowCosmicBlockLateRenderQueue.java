package org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.EXEShaders;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.ExralCosmicRenderHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class EXERainbowCosmicBlockLateRenderQueue {

    private record Entry(
            BlockState blockState,
            ItemStack stack,
            Matrix4f pose,
            Matrix3f normal,
            Matrix4f modelView,
            Matrix4f projection,
            int packedLight,
            int packedOverlay,
            float opacity,
            boolean flower
    ) {
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static boolean deferThisFrame;

    public static void beginFrame(EXERenderFrameState.Snapshot snapshot) {
        ENTRIES.clear();
        deferThisFrame = snapshot.shaderPackActive();
    }

    public static void endFrame() {
        ENTRIES.clear();
        deferThisFrame = false;
    }

    public static boolean shouldDefer() {
        return deferThisFrame;
    }

    public static void enqueue(
            BlockState blockState,
            ItemStack stack,
            PoseStack poseStack,
            int packedLight,
            int packedOverlay,
            float opacity,
            boolean flower
    ) {
        if (!deferThisFrame) {
            return;
        }

        PoseStack.Pose currentPose = poseStack.last();

        ENTRIES.add(
                new Entry(
                        blockState,
                        stack.copy(),
                        new Matrix4f(currentPose.pose()),
                        new Matrix3f(currentPose.normal()),
                        new Matrix4f(RenderSystem.getModelViewMatrix()),
                        new Matrix4f(RenderSystem.getProjectionMatrix()),
                        packedLight,
                        packedOverlay,
                        opacity,
                        flower
                )
        );
    }

    public static void renderAfterLevel() {
        if (!deferThisFrame || ENTRIES.isEmpty()) {
            if (!deferThisFrame) {
                ENTRIES.clear();
            }
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            ENTRIES.clear();
            return;
        }

        MultiBufferSource.BufferSource buffers =
                mc.renderBuffers().bufferSource();

        Matrix4f previousProjection =
                new Matrix4f(RenderSystem.getProjectionMatrix());

        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();

        try {
            EXELatePassState.prepare();

            Iterator<Entry> iterator = ENTRIES.iterator();

            while (iterator.hasNext()) {
                Entry entry = iterator.next();

                modelViewStack.last().pose().set(entry.modelView());
                RenderSystem.applyModelViewMatrix();

                RenderSystem.setProjectionMatrix(
                        new Matrix4f(entry.projection()),
                        VertexSorting.DISTANCE_TO_ORIGIN
                );

                PoseStack blockPoseStack = new PoseStack();
                blockPoseStack.last().pose().set(entry.pose());
                blockPoseStack.last().normal().set(entry.normal());

                if (entry.flower()) {
                    ExralCosmicRenderHelper.renderDeferredFlower(
                            entry.blockState(),
                            blockPoseStack,
                            buffers,
                            entry.packedLight(),
                            entry.packedOverlay(),
                            entry.stack()
                    );
                } else {
                    ExralCosmicRenderHelper.renderDeferredBlock(
                            entry.blockState(),
                            blockPoseStack,
                            buffers,
                            entry.packedLight(),
                            entry.packedOverlay(),
                            entry.stack(),
                            entry.opacity()
                    );
                }

                buffers.endBatch(
                        EXEShaders.COSMIC_BLOCK_AFTER_LEVEL_RENDER_TYPE
                );

                iterator.remove();
            }
        } finally {
            RenderSystem.setProjectionMatrix(
                    previousProjection,
                    VertexSorting.DISTANCE_TO_ORIGIN
            );

            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();

            EXELatePassState.finish();
            ENTRIES.clear();
        }
    }

    private EXERainbowCosmicBlockLateRenderQueue() {
    }
}