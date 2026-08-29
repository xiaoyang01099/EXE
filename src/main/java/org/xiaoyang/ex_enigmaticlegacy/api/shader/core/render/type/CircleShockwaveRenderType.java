package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.bloom.LightningVertexFormat;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.CircleShockwaveShader;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.texture.EXETextureAtlas;

public class CircleShockwaveRenderType extends RenderType {
    public static final ShaderStateShard CIRCLE_SHOCKWAVE_SHADER_STATE = new ShaderStateShard(CircleShockwaveShader::getShader); // 法阵冲击波 shader 状态。
    public static final TextureStateShard EXE_ATLAS_TEXTURE = new TextureStateShard(EXETextureAtlas.EXE_TOOL_ATLAS_LOCATION, false, false); // 法阵冲击波使用 AkatZumaTool 自定义图集。
    public static final TransparencyStateShard ADDITIVE_TRANSPARENCY = new TransparencyStateShard(
            "circle_shockwave_additive_transparency",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            },
            () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }
    );

    public static final RenderType CIRCLE_SHOCKWAVE_RENDER_TYPE = create(
            "circle_shockwave_render_type",
            LightningVertexFormat.FORMAT,
            VertexFormat.Mode.QUADS,
            4096,
            false,
            false,
            CompositeState.builder()
                    .setShaderState(CIRCLE_SHOCKWAVE_SHADER_STATE)
                    .setTextureState(EXE_ATLAS_TEXTURE)
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public CircleShockwaveRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                                     boolean affectsCrumbling, boolean sortOnUpload,
                                     Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType getRenderType() {
        return CIRCLE_SHOCKWAVE_RENDER_TYPE;
    }
}
