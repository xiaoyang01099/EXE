package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.renderer.RenderType;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.TridentPlusGlowShader;

public class TridentPlusGlowRenderType extends RenderType {
    private static final ShaderStateShard TRIDENT_PLUS_GLOW_SHADER_STATE =
            new ShaderStateShard(TridentPlusGlowShader::getShader);

    private static final TextureStateShard TRIDENT_TEXTURE =
            new TextureStateShard(TridentModel.TEXTURE, false, false);

    private static final TransparencyStateShard ADDITIVE_TRANSPARENCY =
            new TransparencyStateShard(
                    "trident_plus_glow_additive_transparency",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    }
            );

    private static final RenderType TRIDENT_PLUS_GLOW_RENDER_TYPE = create(
            "trident_plus_glow_render_type",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            CompositeState.builder()
                    .setShaderState(TRIDENT_PLUS_GLOW_SHADER_STATE)
                    .setTextureState(TRIDENT_TEXTURE)
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public TridentPlusGlowRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                                     boolean affectsCrumbling, boolean sortOnUpload,
                                     Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType getRenderType() {
        return TRIDENT_PLUS_GLOW_RENDER_TYPE;
    }
}
