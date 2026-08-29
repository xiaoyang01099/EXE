package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.TrailRibbonShader;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.texture.EXETextureAtlas;

public class TrailRibbonRenderType extends RenderType{
    private static final ShaderStateShard TrailRibbon_SHADER_STATE = new ShaderStateShard(() -> TrailRibbonShader.TrailRibbonShader);
    private static final TransparencyStateShard ADDITIVE_TRANSPARENCY = new TransparencyStateShard(
            "additive_transparency",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            },
            () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }
    );

    static RenderType TrailRibbon_RenderType;

    static {
        TrailRibbon_RenderType = create(
                "trail_ribbon_render_type",
                DefaultVertexFormat.POSITION_COLOR_TEX,
                VertexFormat.Mode.QUADS,
                256,
                true,
                false,
                CompositeState.builder()
                        .setShaderState(TrailRibbon_SHADER_STATE)
                        .setTextureState(new TextureStateShard(EXETextureAtlas.EXE_TOOL_ATLAS_LOCATION, false, true))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(true)
        );
    }

    public TrailRibbonRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                    boolean affectsCrumbling, boolean sortOnUpload,
                    Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType getRenderType() {
        return TrailRibbon_RenderType;
    }
}
