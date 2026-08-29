package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.BattoSlashShader;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.texture.EXETextureAtlas;

public class BattoSlashRenderType extends RenderType {
    public static final ShaderStateShard SHADER_STATE = new ShaderStateShard(BattoSlashShader::getShader);
    public static final TextureStateShard EXE_ATLAS_TEXTURE = new TextureStateShard(EXETextureAtlas.EXE_TOOL_ATLAS_LOCATION, false, false); // 拔刀斩使用 AkatZumaTool 自定义图集。
    public static final TransparencyStateShard STANDARD_ALPHA_TRANSPARENCY = new TransparencyStateShard(
            "batto_slash_standard_alpha_transparency",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                        GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
                );
            },
            () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }
    );
    public static final RenderType RENDER_TYPE = create(
            "batto_slash_render_type",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            16384,
            false,
            false,
            CompositeState.builder()
                    .setShaderState(SHADER_STATE)
                    .setTextureState(EXE_ATLAS_TEXTURE)
                    .setTransparencyState(STANDARD_ALPHA_TRANSPARENCY)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public BattoSlashRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType getRenderType() {
        return RENDER_TYPE;
    }
}
