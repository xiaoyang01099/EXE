package org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Model.SpecialRenderHelper;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SpecialLateRenderQueue {
    private static MultiBufferSource.BufferSource buffers;
    private static boolean deferThisFrame;

    private SpecialLateRenderQueue() {
    }

    public static void beginFrame(EXERenderFrameState.Snapshot snapshot) {
        buffers = null;
        deferThisFrame = snapshot.worldRenderActive() && snapshot.shaderPackActive();
    }

    public static boolean shouldDefer() {
        return deferThisFrame;
    }

    public static VertexConsumer getBuffer(RenderType renderType) {
        if (!deferThisFrame) {
            throw new IllegalStateException("Special late rendering is not active");
        }
        return getOrCreateBuffers().getBuffer(renderType);
    }

    public static VertexConsumer select(
            MultiBufferSource originalBuffers,
            RenderType originalType,
            RenderType afterLevelType
    ) {
        return deferThisFrame
                ? getBuffer(afterLevelType)
                : originalBuffers.getBuffer(originalType);
    }

    public static void renderAfterLevel() {
        if (!deferThisFrame || buffers == null) {
            return;
        }

        EXELatePassState.prepare();
        try {
            buffers.endBatch(SpecialRenderHelper.END_PORTAL_AFTER_LEVEL);
            buffers.endBatch(SpecialRenderHelper.ENCHANTER_RUNE_AFTER_LEVEL);
            buffers.endBatch(SpecialRenderHelper.RAINBOW_MANA_WATER_AFTER_LEVEL);
            buffers.endBatch(SpecialRenderHelper.POLYCHROME_COLLAPSE_PRISM_AFTER_LEVEL);
            buffers.endBatch(SpecialRenderHelper.MANA_POOL_WATER_AFTER_LEVEL);
            buffers.endBatch(SpecialRenderHelper.TERRA_PLATE_AFTER_LEVEL);
            buffers.endBatch(SpecialRenderHelper.MANA_PYLON_GLOW_AFTER_LEVEL);
            buffers.endBatch(SpecialRenderHelper.NATURA_PYLON_GLOW_AFTER_LEVEL);
            buffers.endBatch(SpecialRenderHelper.GAIA_PYLON_GLOW_AFTER_LEVEL);
        } finally {
            EXELatePassState.finish();
            buffers = null;
        }
    }

    public static void endFrame() {
        buffers = null;
        deferThisFrame = false;
    }

    private static MultiBufferSource.BufferSource getOrCreateBuffers() {
        if (buffers == null) {
            Map<RenderType, BufferBuilder> fixedBuffers = new LinkedHashMap<>();
            add(fixedBuffers, SpecialRenderHelper.RAINBOW_MANA_WATER_AFTER_LEVEL);
            add(fixedBuffers, SpecialRenderHelper.POLYCHROME_COLLAPSE_PRISM_AFTER_LEVEL);
            add(fixedBuffers, SpecialRenderHelper.MANA_POOL_WATER_AFTER_LEVEL);
            add(fixedBuffers, SpecialRenderHelper.TERRA_PLATE_AFTER_LEVEL);
            add(fixedBuffers, SpecialRenderHelper.MANA_PYLON_GLOW_AFTER_LEVEL);
            add(fixedBuffers, SpecialRenderHelper.NATURA_PYLON_GLOW_AFTER_LEVEL);
            add(fixedBuffers, SpecialRenderHelper.GAIA_PYLON_GLOW_AFTER_LEVEL);
            add(fixedBuffers, SpecialRenderHelper.ENCHANTER_RUNE_AFTER_LEVEL);
            add(fixedBuffers, SpecialRenderHelper.END_PORTAL_AFTER_LEVEL);

            buffers = MultiBufferSource.immediateWithBuffers(
                    fixedBuffers,
                    new BufferBuilder(256)
            );
        }
        return buffers;
    }

    private static void add(
            Map<RenderType, BufferBuilder> buffers,
            RenderType renderType
    ) {
        buffers.put(renderType, new BufferBuilder(256));
    }
}