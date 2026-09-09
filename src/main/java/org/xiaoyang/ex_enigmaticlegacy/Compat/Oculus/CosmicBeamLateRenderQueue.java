package org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.cosmic.CosmicBeamRenderer;

import java.util.ArrayList;
import java.util.List;

public final class CosmicBeamLateRenderQueue {
    private static final List<CosmicBeamRenderer.CosmicBeam> PENDING = new ArrayList<>();
    private static boolean deferThisFrame;

    public static void beginFrame(EXERenderFrameState.Snapshot snap) {
        PENDING.clear();
        deferThisFrame = snap.shaderPackActive();
    }

    public static boolean shouldDefer() {
        return deferThisFrame;
    }

    public static void enqueue(CosmicBeamRenderer.CosmicBeam beam) {
        if (!deferThisFrame) return;
        PENDING.add(beam);
    }

    public static void renderAfterLevel(PoseStack poseStack, float partialTick) {
        if (!deferThisFrame || PENDING.isEmpty()) {PENDING.clear();
            return;
        }
        if (Minecraft.getInstance().level == null) {PENDING.clear();
            return;
        }

        Matrix4f prevProj = new Matrix4f(RenderSystem.getProjectionMatrix());
        PoseStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushPose();

        try {
            EXELatePassState.prepare();
            CosmicBeamRenderer.renderDeferredBeams(poseStack, partialTick);
        } finally {
            mvStack.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(prevProj, VertexSorting.DISTANCE_TO_ORIGIN);
            EXELatePassState.finish();
            PENDING.clear();
        }
    }

    public static void endFrame() {
        PENDING.clear();
        deferThisFrame = false;
    }

    private CosmicBeamLateRenderQueue() {}
}