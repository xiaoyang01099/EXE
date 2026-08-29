package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.bloom.LightningVertexFormat;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.LightningShader;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.texture.EXETextureAtlas;

public class LightningRenderType extends RenderType {
    public static final ShaderStateShard COIN_LIGHTNING_SHADER_STATE = new ShaderStateShard(LightningShader::getShader); // 闪电 shader 状态。
    public static final TextureStateShard EXE_ATLAS_TEXTURE = new TextureStateShard(EXETextureAtlas.EXE_TOOL_ATLAS_LOCATION, false, false); // 闪电使用 AkatZumaTool 自定义图集。
    public static final TransparencyStateShard ADDITIVE_TRANSPARENCY = new TransparencyStateShard(
            "lightning_additive_transparency",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            },
            () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }
    );

    public static final RenderType COIN_LIGHTNING_RENDER_TYPE = create(
            "lightning_render_type",
            LightningVertexFormat.FORMAT,
            VertexFormat.Mode.QUADS,
            8192,
            false,
            false,
            CompositeState.builder()
                    .setShaderState(COIN_LIGHTNING_SHADER_STATE)
                    .setTextureState(EXE_ATLAS_TEXTURE)
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public LightningRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                               boolean affectsCrumbling, boolean sortOnUpload,
                               Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType getRenderType() {
        return COIN_LIGHTNING_RENDER_TYPE;
    }
}
