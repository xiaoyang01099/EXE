package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.bloom;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class LightningVertexFormat {
    public static final VertexFormat FORMAT = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
            .put("UV0", DefaultVertexFormat.ELEMENT_UV0)
            .put("Color", DefaultVertexFormat.ELEMENT_COLOR)
            .put("BloomColor", DefaultVertexFormat.ELEMENT_UV2)
            .build());

    private LightningVertexFormat() {
    }
}
