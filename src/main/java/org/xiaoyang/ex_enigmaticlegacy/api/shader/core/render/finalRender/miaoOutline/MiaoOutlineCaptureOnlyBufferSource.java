package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class MiaoOutlineCaptureOnlyBufferSource implements MultiBufferSource {
    private final MiaoOutlineCapturedMaskBuffer maskBuffer;

    public MiaoOutlineCaptureOnlyBufferSource(int entityId) {
        this.maskBuffer = MiaoOutlineTargetMaskStore.beginCapture(entityId);
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        Optional<ResourceLocation> texture = MiaoOutlineRenderTypeTextureResolver.resolve(renderType);
        if (!MiaoOutlineRenderTypeFilter.shouldCapture(renderType, texture)) {
            return MiaoOutlineDiscardVertexConsumer.INSTANCE;
        }
        MiaoOutlineCapturedBatch batch = maskBuffer.beginBatch(renderType.mode(), texture.orElse(null));
        return new MiaoOutlineCaptureOnlyVertexConsumer(batch);
    }
}
