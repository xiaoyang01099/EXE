package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.DimensionSlashStrikeShader;

public class DimensionSlashStrikeRenderType extends RenderType {
    public static final ShaderStateShard SHADER_STATE = new ShaderStateShard(DimensionSlashStrikeShader::getShader);

    public static final TransparencyStateShard ADDITIVE_TRANSPARENCY =
            new TransparencyStateShard(
                    "dimension_slash_additive_transparency",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    }
            );

    public static final RenderType RENDER_TYPE = create(
            "dimension_slash_strike_render_type",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            8192,
            false,
            false,
            CompositeState.builder()
                    .setShaderState(SHADER_STATE)
                    .setTextureState(NO_TEXTURE)
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public DimensionSlashStrikeRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                                          boolean affectsCrumbling, boolean sortOnUpload,
                                          Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType getRenderType() {
        return RENDER_TYPE;
    }
}
