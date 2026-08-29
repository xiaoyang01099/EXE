package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.bloom.LightningVertexFormat;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.ShockwaveShader;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.texture.EXETextureAtlas;

public class ShockwaveRenderType extends RenderType {
    public static final ShaderStateShard SHOCKWAVE_SHADER_STATE = new ShaderStateShard(ShockwaveShader::getShader);
    public static final TextureStateShard EXE_ATLAS_TEXTURE = new TextureStateShard(EXETextureAtlas.EXE_TOOL_ATLAS_LOCATION, false, false);
    public static final TransparencyStateShard ADDITIVE_TRANSPARENCY = new TransparencyStateShard(
            "shockwave_additive_transparency",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            },
            () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }
    );

    public static final RenderType SHOCKWAVE_RENDER_TYPE = create(
            "shockwave_render_type",
            LightningVertexFormat.FORMAT,
            VertexFormat.Mode.QUADS,
            4096,
            false,
            false,
            CompositeState.builder()
                    .setShaderState(SHOCKWAVE_SHADER_STATE)
                    .setTextureState(EXE_ATLAS_TEXTURE)
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public ShockwaveRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                               boolean affectsCrumbling, boolean sortOnUpload,
                               Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType getRenderType() {
        return SHOCKWAVE_RENDER_TYPE;
    }
}
