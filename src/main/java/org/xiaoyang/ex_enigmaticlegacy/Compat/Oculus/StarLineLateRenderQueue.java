package org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.xiaoyang.ex_enigmaticlegacy.Client.renderer.special.starline.StarLineRenderer;

public final class StarLineLateRenderQueue {

    private record Entry(
            double camX, double camY, double camZ,
            double px, double py, double pz,
            Vector2f velocity,
            float revealSeconds,
            Matrix4f modelView,
            Matrix4f projection,
            PoseStack.Pose pose
    ) {}

    private static Entry pending;
    private static boolean deferThisFrame;

    public static void beginFrame(EXERenderFrameState.Snapshot snap) {
        pending = null;
        deferThisFrame = snap.shaderPackActive();
    }

    public static boolean shouldDefer() {
        return deferThisFrame;
    }

    public static void enqueue(PoseStack poseStack,double camX, double camY, double camZ, double px, double py, double pz, Vector2f velocity, float revealSeconds) {
        if (!deferThisFrame) return;
        pending = new Entry(
                camX, camY, camZ, px, py, pz,
                new Vector2f(velocity),
                revealSeconds,
                new Matrix4f(RenderSystem.getModelViewMatrix()),
                new Matrix4f(RenderSystem.getProjectionMatrix()),
                poseStack.last()
        );
    }

    public static void renderAfterLevel(PoseStack poseStack) {
        if (!deferThisFrame || pending == null) {
            pending = null;
            return;
        }
        if (Minecraft.getInstance().level == null) {
            pending = null;
            return;
        }

        Entry e = pending;
        Matrix4f prevProj = new Matrix4f(RenderSystem.getProjectionMatrix());
        PoseStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushPose();
        mvStack.last().pose().set(e.modelView());
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f(e.projection()), VertexSorting.DISTANCE_TO_ORIGIN);

        try {
            EXELatePassState.prepare();
            StarLineRenderer.renderShield(
                    poseStack,
                    e.camX(), e.camY(), e.camZ(),
                    e.px(), e.py(), e.pz(),
                    e.velocity(), e.revealSeconds());StarLineRenderer.renderFootprint(
                    poseStack,
                    e.camX(), e.camY(), e.camZ(),
                    e.px(), e.py(), e.pz(),
                    e.velocity(), e.revealSeconds());
        } finally {
            mvStack.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(prevProj, VertexSorting.DISTANCE_TO_ORIGIN);
            EXELatePassState.finish();
            pending = null;
        }
    }

    public static void endFrame() {
        pending = null;
        deferThisFrame = false;
    }

    private StarLineLateRenderQueue() {}
}