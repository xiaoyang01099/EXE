package net.xiaoyang010.ex_enigmaticlegacy.Capability;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class YuhuaRenderType {

    private static RenderType CACHED = null;

    public static RenderType getYuhuaEntityRenderType() {
        if (CACHED != null) return CACHED;

        CACHED = RenderType.create(
                "yuhua_cosmic_entity",
                DefaultVertexFormat.NEW_ENTITY,   // ← 必须与注册格式一致
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(
                                () -> YuHuaShaders.yuhuaShader
                        ))
                        .setTextureState(new RenderStateShard.TextureStateShard(
                                InventoryMenu.BLOCK_ATLAS, false, false
                        ))
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        //.setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE) // ★ 写入深度，防止原版模型覆盖
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                        .createCompositeState(false)
        );

        return CACHED;
    }

    public static void invalidateCache() {
        CACHED = null;
    }
}