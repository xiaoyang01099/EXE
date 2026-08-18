package org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.RainBowCosmicBakeModel;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.RainbowAvaritiaShaders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class EXERainbowCosmicItemLateRenderQueue {

    private record Entry(
            RainBowCosmicBakeModel renderer,
            ItemStack stack,
            ItemDisplayContext context,
            Matrix4f pose,
            Matrix3f normal,
            Matrix4f modelView,
            Matrix4f projection,
            int packedLight,
            int packedOverlay
    ) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static boolean deferThisFrame;

    public static void beginFrame(EXERenderFrameState.Snapshot snap) {
        ENTRIES.clear();
        deferThisFrame = snap.shaderPackActive();
    }

    public static void endFrame() {
        ENTRIES.clear();
        deferThisFrame = false;
    }

    public static boolean shouldDefer() {
        return deferThisFrame;
    }

    public static void enqueue(RainBowCosmicBakeModel renderer,ItemStack stack,
                               ItemDisplayContext context,
                               PoseStack poseStack,
                               int packedLight,
                               int packedOverlay) {
        if (!deferThisFrame) return;
        PoseStack.Pose pose = poseStack.last();
        ENTRIES.add(new Entry(
                renderer,
                stack.copy(),
                context,
                new Matrix4f(pose.pose()),
                new Matrix3f(pose.normal()),
                new Matrix4f(RenderSystem.getModelViewMatrix()),
                new Matrix4f(RenderSystem.getProjectionMatrix()),
                packedLight,
                packedOverlay
        ));
    }

    public static void renderAfterLevel() {
        if (!deferThisFrame || ENTRIES.isEmpty()) {
            if (!deferThisFrame) ENTRIES.clear();
            return;
        }
        if (Minecraft.getInstance().level == null) {
            ENTRIES.clear();
            return;
        }

        MultiBufferSource.BufferSource buffers =Minecraft.getInstance().renderBuffers().bufferSource();
        Matrix4f prevProj = new Matrix4f(RenderSystem.getProjectionMatrix());
        PoseStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushPose();

        try {
            EXELatePassState.prepare();
            Iterator<Entry> it = ENTRIES.iterator();
            while (it.hasNext()) {
                Entry e = it.next();

                mvStack.last().pose().set(e.modelView());
                RenderSystem.applyModelViewMatrix();
                RenderSystem.setProjectionMatrix(
                        new Matrix4f(e.projection()), VertexSorting.DISTANCE_TO_ORIGIN);

                PoseStack ps = new PoseStack();
                ps.last().pose().set(e.pose());
                ps.last().normal().set(e.normal());

                e.renderer().renderCosmicLayer(e.stack(), e.context(), ps, buffers, e.packedLight(), e.packedOverlay());

                buffers.endBatch(RainbowAvaritiaShaders.RAINBOW_COSMIC_RENDER_TYPE);
                it.remove();
            }
        } finally {
            RenderSystem.setProjectionMatrix(prevProj, VertexSorting.DISTANCE_TO_ORIGIN);
            mvStack.popPose();
            RenderSystem.applyModelViewMatrix();
            EXELatePassState.finish();
            ENTRIES.clear();
        }
    }

    private EXERainbowCosmicItemLateRenderQueue() {}
}