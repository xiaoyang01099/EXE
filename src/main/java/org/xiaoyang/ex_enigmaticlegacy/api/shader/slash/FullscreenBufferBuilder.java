package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexFormat;

final class FullscreenBufferBuilder {
    private final int initialSize;
    private BufferBuilder builder;

    FullscreenBufferBuilder(int initialSize) {
        this.initialSize = initialSize;
    }

    BufferBuilder begin(VertexFormat.Mode mode, VertexFormat format) {
        if (builder == null || builder.building()) {
            builder = new BufferBuilder(initialSize);
        }
        builder.begin(mode, format);
        return builder;
    }

    static void drawWithShaderOrDiscard(BufferBuilder builder) {
        BufferBuilder.RenderedBuffer rendered = builder.endOrDiscardIfEmpty();
        if (rendered != null) {
            BufferUploader.drawWithShader(rendered);
        }
    }
}
