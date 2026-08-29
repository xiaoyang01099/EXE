package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.StarJudgementCircleShader;

public class StarJudgementCircleRenderType extends RenderType {
    private static final ShaderStateShard STAR_JUDGEMENT_CIRCLE_SHADER_STATE =
            new ShaderStateShard(StarJudgementCircleShader::getShader);

    private static final TransparencyStateShard ADDITIVE_TRANSPARENCY =
            new TransparencyStateShard(
                    "star_judgement_circle_additive_transparency",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    }
            );

    private static final RenderType STAR_JUDGEMENT_CIRCLE_RENDER_TYPE = create(
            "star_judgement_circle_render_type",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            2048,
            false,
            false,
            CompositeState.builder()
                    .setShaderState(STAR_JUDGEMENT_CIRCLE_SHADER_STATE)
                    .setTextureState(NO_TEXTURE)
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public StarJudgementCircleRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                                         boolean affectsCrumbling, boolean sortOnUpload,
                                         Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType getRenderType() {
        return STAR_JUDGEMENT_CIRCLE_RENDER_TYPE;
    }
}
