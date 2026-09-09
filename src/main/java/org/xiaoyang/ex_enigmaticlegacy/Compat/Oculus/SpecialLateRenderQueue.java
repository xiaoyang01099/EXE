package org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.xiaoyang.ex_enigmaticlegacy.Client.help.EXERenderHelper;

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
            buffers.endBatch(EXERenderHelper.END_PORTAL_AFTER_LEVEL);
            buffers.endBatch(EXERenderHelper.ENCHANTER_RUNE_AFTER_LEVEL);
            buffers.endBatch(EXERenderHelper.RAINBOW_MANA_WATER_AFTER_LEVEL);
            buffers.endBatch(EXERenderHelper.POLYCHROME_COLLAPSE_PRISM_AFTER_LEVEL);
            buffers.endBatch(EXERenderHelper.MANA_POOL_WATER_AFTER_LEVEL);
            buffers.endBatch(EXERenderHelper.TERRA_PLATE_AFTER_LEVEL);
            buffers.endBatch(EXERenderHelper.MANA_PYLON_GLOW_AFTER_LEVEL);
            buffers.endBatch(EXERenderHelper.NATURA_PYLON_GLOW_AFTER_LEVEL);
            buffers.endBatch(EXERenderHelper.GAIA_PYLON_GLOW_AFTER_LEVEL);
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
            add(fixedBuffers, EXERenderHelper.RAINBOW_MANA_WATER_AFTER_LEVEL);
            add(fixedBuffers, EXERenderHelper.POLYCHROME_COLLAPSE_PRISM_AFTER_LEVEL);
            add(fixedBuffers, EXERenderHelper.MANA_POOL_WATER_AFTER_LEVEL);
            add(fixedBuffers, EXERenderHelper.TERRA_PLATE_AFTER_LEVEL);
            add(fixedBuffers, EXERenderHelper.MANA_PYLON_GLOW_AFTER_LEVEL);
            add(fixedBuffers, EXERenderHelper.NATURA_PYLON_GLOW_AFTER_LEVEL);
            add(fixedBuffers, EXERenderHelper.GAIA_PYLON_GLOW_AFTER_LEVEL);
            add(fixedBuffers, EXERenderHelper.ENCHANTER_RUNE_AFTER_LEVEL);
            add(fixedBuffers, EXERenderHelper.END_PORTAL_AFTER_LEVEL);

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