package org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Client.renderer.bolt.TrueBolt;
import org.xiaoyang.ex_enigmaticlegacy.Client.help.EXECoreShaders;

public final class EXETrueBoltLateRenderQueue {
    private record Entry(
            Matrix4f pose,
            Matrix3f normal,
            Matrix4f modelView,
            Matrix4f projection,
            Vec3 cameraPosition,
            float partialTick) {
    }
    private static Entry entry;
    private static boolean deferThisFrame;

    public static void beginFrame(EXERenderFrameState.Snapshot snapshot) {
        entry = null;
        deferThisFrame = snapshot.worldRenderActive() && snapshot.shaderPackActive();
    }

    public static boolean shouldDefer() {
        return deferThisFrame && !EXERenderFrameState.isShadowPass();
    }

    public static void enqueue(PoseStack poseStack, Vec3 cameraPosition, float partialTick) {
        if (!shouldDefer()
                || poseStack == null
                || cameraPosition == null
                || EXECoreShaders.boltShader == null) {
            return;
        }

        PoseStack.Pose currentPose = poseStack.last();

        entry = new Entry(
                new Matrix4f(currentPose.pose()),
                new Matrix3f(currentPose.normal()),
                new Matrix4f(RenderSystem.getModelViewMatrix()),
                new Matrix4f(RenderSystem.getProjectionMatrix()),
                cameraPosition,
                partialTick
        );
    }

    public static void renderAfterLevel() {
        Entry renderEntry = entry;

        if (!deferThisFrame || renderEntry == null) {
            entry = null;
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || EXECoreShaders.boltShader == null) {
            entry = null;
            return;
        }

        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();

        try {
            EXELatePassState.prepare();
            modelViewStack.last()
                    .pose()
                    .set(renderEntry.modelView());
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(
                    new Matrix4f(renderEntry.projection()),
                    VertexSorting.DISTANCE_TO_ORIGIN
            );
            PoseStack boltPoseStack = new PoseStack();

            boltPoseStack.last()
                    .pose()
                    .set(renderEntry.pose());

            boltPoseStack.last()
                    .normal()
                    .set(renderEntry.normal());

            TrueBolt.Manager.renderAll(
                    boltPoseStack,
                    renderEntry.cameraPosition(),
                    renderEntry.partialTick()
            );
        } finally {
            RenderSystem.setProjectionMatrix(
                    previousProjection,
                    VertexSorting.DISTANCE_TO_ORIGIN
            );

            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();

            EXELatePassState.finish();

            entry = null;
        }
    }

    public static void endFrame() {
        entry = null;
        deferThisFrame = false;
    }

    private EXETrueBoltLateRenderQueue() {
    }
}