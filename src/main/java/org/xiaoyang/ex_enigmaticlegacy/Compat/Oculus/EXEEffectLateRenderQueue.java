package org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Client.particle.effect.Effect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class EXEEffectLateRenderQueue {

    private record Entry(
            Effect effect,
            Matrix4f pose,
            Matrix3f normal,
            Matrix4f modelView,
            Matrix4f projection,
            float partialTicks
    ) {}

    private static final List<Entry> QUEUE = new ArrayList<>();
    private static boolean deferThisFrame;

    public static void beginFrame(EXERenderFrameState.Snapshot snapshot) {
        QUEUE.clear();
        deferThisFrame = snapshot.shaderPackActive();
    }

    public static void endFrame() {
        QUEUE.clear();
        deferThisFrame = false;
    }

    public static boolean shouldDefer() {
        return deferThisFrame;
    }

    public static void enqueue(Effect effect, PoseStack poseStack, float partialTicks) {
        if (!deferThisFrame) return;

        PoseStack.Pose pose = poseStack.last();
        QUEUE.add(new Entry(
                effect,
                new Matrix4f(pose.pose()),
                new Matrix3f(pose.normal()),
                new Matrix4f(RenderSystem.getModelViewMatrix()),
                new Matrix4f(RenderSystem.getProjectionMatrix()),
                partialTicks
        ));
    }

    public static void renderAfterLevel() {
        if (!deferThisFrame || QUEUE.isEmpty()) {
            QUEUE.clear();
            return;
        }

        if (Minecraft.getInstance().level == null) {
            QUEUE.clear();
            return;
        }

        Matrix4f prevProj = new Matrix4f(RenderSystem.getProjectionMatrix());
        PoseStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushPose();

        try {
            EXELatePassState.prepare();
            Iterator<Entry> it = QUEUE.iterator();

            while (it.hasNext()) {
                Entry entry = it.next();

                mvStack.last().pose().set(entry.modelView());
                RenderSystem.applyModelViewMatrix();
                RenderSystem.setProjectionMatrix(
                        new Matrix4f(entry.projection()), VertexSorting.DISTANCE_TO_ORIGIN
                );

                PoseStack effectPoseStack = new PoseStack();
                effectPoseStack.last().pose().set(entry.pose());
                effectPoseStack.last().normal().set(entry.normal());
                entry.effect().renderTotal(effectPoseStack, entry.partialTicks());

                it.remove();
            }
        } finally {
            RenderSystem.setProjectionMatrix(prevProj, VertexSorting.DISTANCE_TO_ORIGIN);
            mvStack.popPose();
            RenderSystem.applyModelViewMatrix();
            EXELatePassState.finish();
            QUEUE.clear();
        }
    }

    private EXEEffectLateRenderQueue() {}
}