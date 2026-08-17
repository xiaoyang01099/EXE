package org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import vazkii.botania.client.core.helper.CoreShaders;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.entity.GaiaGuardianEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GaiaGuardianLateRenderQueue {
    private record Entry(
            GaiaGuardianEntity entity,
            HumanoidModel<GaiaGuardianEntity> model,
            List<RenderLayer<GaiaGuardianEntity, HumanoidModel<GaiaGuardianEntity>>> layers,
            ResourceLocation texture,
            Matrix4f pose,
            Matrix3f normal,
            Matrix4f modelView,
            Matrix4f projection,
            float yaw,
            float partialTicks,
            int packedLight,
            float grainIntensity,
            float disfiguration
    ) {}

    private static final Map<Integer, Entry> ENTRIES = new LinkedHashMap<>();
    private static boolean deferThisFrame;

    public static void beginFrame(EXERenderFrameState.Snapshot snapshot) {
        ENTRIES.clear();
        deferThisFrame = snapshot.shaderPackActive();
    }

    public static boolean shouldDefer() {
        return deferThisFrame;
    }

    public static void enqueue(
            GaiaGuardianEntity entity,
            HumanoidModel<GaiaGuardianEntity> model,
            List<RenderLayer<GaiaGuardianEntity, HumanoidModel<GaiaGuardianEntity>>> layers,
            ResourceLocation texture,
            PoseStack poseStack,
            float yaw,
            float partialTicks,
            int packedLight,
            float grainIntensity,
            float disfiguration
    ) {
        if (!deferThisFrame) {
            return;
        }

        PoseStack.Pose currentPose = poseStack.last();
        ENTRIES.put(
                entity.getId(),
                new Entry(
                        entity,
                        model,
                        layers,
                        texture,
                        new Matrix4f(currentPose.pose()),
                        new Matrix3f(currentPose.normal()),
                        new Matrix4f(RenderSystem.getModelViewMatrix()),
                        new Matrix4f(RenderSystem.getProjectionMatrix()),
                        yaw,
                        partialTicks,
                        packedLight,
                        grainIntensity,
                        disfiguration
                )
        );
    }

    public static void renderAfterLevel() {
        if (!deferThisFrame || ENTRIES.isEmpty()) {
            ENTRIES.clear();
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            ENTRIES.clear();
            return;
        }

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();

        try {
            EXELatePassState.prepare();

            for (Entry entry : ENTRIES.values()) {
                if (entry.entity().isRemoved()) {
                    continue;
                }

                modelViewStack.last().pose().set(entry.modelView());
                RenderSystem.applyModelViewMatrix();
                RenderSystem.setProjectionMatrix(
                        new Matrix4f(entry.projection()),
                        VertexSorting.DISTANCE_TO_ORIGIN
                );

                ShaderInstance shader = CoreShaders.doppleganger();
                if (shader != null) {
                    shader.safeGetUniform("BotaniaGrainIntensity").set(entry.grainIntensity());
                    shader.safeGetUniform("BotaniaDisfiguration").set(entry.disfiguration());
                }

                PoseStack entityPoseStack = new PoseStack();
                entityPoseStack.last().pose().set(entry.pose());
                entityPoseStack.last().normal().set(entry.normal());

                entry.model().renderToBuffer(
                        entityPoseStack,
                        buffers.getBuffer(RenderHelper.getDopplegangerLayer(entry.texture())),
                        entry.packedLight(),
                        OverlayTexture.NO_OVERLAY,
                        1.0F, 1.0F, 1.0F, 1.0F
                );

                for (RenderLayer<GaiaGuardianEntity, HumanoidModel<GaiaGuardianEntity>> layer : entry.layers()) {
                    layer.render(
                            entityPoseStack,
                            buffers,
                            entry.packedLight(),
                            entry.entity(),
                            0, 0,
                            entry.partialTicks(),
                            0,
                            entry.yaw(),
                            0
                    );
                }

                buffers.endBatch();
            }
        } finally {
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.DISTANCE_TO_ORIGIN);
            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();
            EXELatePassState.finish();
            ENTRIES.clear();
        }
    }

    public static void endFrame() {
        ENTRIES.clear();
        deferThisFrame = false;
    }

    private GaiaGuardianLateRenderQueue() {}
}