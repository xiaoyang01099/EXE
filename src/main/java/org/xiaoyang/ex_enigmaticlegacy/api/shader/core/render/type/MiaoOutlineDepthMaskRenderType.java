package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.MiaoOutlineDepthMaskShader;

import java.util.HashMap;
import java.util.Map;

public class MiaoOutlineDepthMaskRenderType extends RenderType {
    public static final ShaderStateShard SHADER_STATE =
            new ShaderStateShard(MiaoOutlineDepthMaskShader::getShader);
    public static final ResourceLocation WHITE_TEXTURE = new ResourceLocation("minecraft", "textures/block/white_wool.png"); // 无纹理 fallback 采样用白图。
    public static final Map<ResourceLocation, RenderType> CACHE = new HashMap<>();

    public MiaoOutlineDepthMaskRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                                          boolean affectsCrumbling, boolean sortOnUpload,
                                          Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType getRenderType(ResourceLocation texture) {
        ResourceLocation safeTexture = texture == null ? WHITE_TEXTURE : texture;
        return CACHE.computeIfAbsent(safeTexture, MiaoOutlineDepthMaskRenderType::createRenderType);
    }

    public static RenderType createRenderType(ResourceLocation texture) {
        return create(
                "miao_outline_depth_mask_render_type",
                DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL,
                VertexFormat.Mode.TRIANGLES,
                4096,
                false,
                false,
                CompositeState.builder()
                        .setShaderState(SHADER_STATE)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(NO_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setCullState(NO_CULL)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false)
        );
    }
}
