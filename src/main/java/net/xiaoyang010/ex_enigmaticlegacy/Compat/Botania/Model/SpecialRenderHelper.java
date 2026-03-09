package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import vazkii.botania.client.core.helper.CoreShaders;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.mixin.client.AccessorRenderType;

public final class SpecialRenderHelper extends RenderType {

    public static final RenderType RAINBOW_MANA_WATER;
    public static final RenderType POLYCHROME_COLLAPSE_PRISM;
    public static final RenderType COSMIC_BACKGROUND;
    public static final RenderType EVIL_WATER;
    public static final RenderType STARRY_SKY;
    public static final RenderType BLACK_HOLE;

    private SpecialRenderHelper(String string, VertexFormat vertexFormat, VertexFormat.Mode mode,
                                int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
        super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
        throw new UnsupportedOperationException("Should not be instantiated");
    }

    private static RenderType makeLayer(String name, VertexFormat format, VertexFormat.Mode mode,
                                        int bufSize, boolean hasCrumbling, boolean sortOnUpload, CompositeState glState) {
        return AccessorRenderType.create(name, format, mode, bufSize, hasCrumbling, sortOnUpload, glState);
    }

    private static RenderType makeLayer(String name, VertexFormat format, VertexFormat.Mode mode,
                                        int bufSize, CompositeState glState) {
        return makeLayer(name, format, mode, bufSize, false, false, glState);
    }

    static {
        //彩虹魔力
        CompositeState glState = CompositeState.builder()
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::rainbowManaWater))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);

        RAINBOW_MANA_WATER = makeLayer(ExEnigmaticlegacyMod.MODID + ":rainbow_mana_water",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 128, glState);

        //多谱坍缩纹理
        glState = CompositeState.builder().setTextureState(BLOCK_SHEET_MIPPED)
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::polychromeCollapsePrismOverlay))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setLightmapState(LIGHTMAP).createCompositeState(false);
        POLYCHROME_COLLAPSE_PRISM = makeLayer(ExEnigmaticlegacyMod.MODID + "polychrome_collapse_prism",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 128, glState);

        //宇宙背景
        glState = CompositeState.builder()
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::cosmicBackground))
                .setTextureState(MultiTextureStateShard.builder()
                        .add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
                        .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
                        .build())
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setWriteMaskState(COLOR_WRITE)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .createCompositeState(false);

        COSMIC_BACKGROUND = makeLayer(ExEnigmaticlegacyMod.MODID + ":cosmic_background",
                DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, glState);

        //邪恶魔力
        glState = CompositeState.builder()
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::evilWater))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);

        EVIL_WATER = makeLayer(ExEnigmaticlegacyMod.MODID + "evil_water",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 128, glState);

        glState = CompositeState.builder()
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::getStarrySkyShader))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);

        STARRY_SKY = makeLayer(ExEnigmaticlegacyMod.MODID + ":starry_sky",
                DefaultVertexFormat.POSITION_TEX,
                VertexFormat.Mode.QUADS, 256, glState);

        glState = CompositeState.builder()
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::getBlackHoleShader))
                .setTextureState(NO_TEXTURE)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .createCompositeState(false);

        BLACK_HOLE = makeLayer(ExEnigmaticlegacyMod.MODID + ":blackhole",
                DefaultVertexFormat.POSITION_TEX,
                VertexFormat.Mode.QUADS, 256, glState);
    }


    public static void renderIcon(PoseStack ms, VertexConsumer buffer, int x, int y,
                                  TextureAtlasSprite icon, int width, int height, float alpha) {
        Matrix4f mat = ms.last().pose();
        int fullbright = 0xF000F0;
        buffer.vertex(mat, x, y + height, 0).color(1, 1, 1, alpha).uv(icon.getU0(), icon.getV1()).uv2(fullbright).endVertex();
        buffer.vertex(mat, x + width, y + height, 0).color(1, 1, 1, alpha).uv(icon.getU1(), icon.getV1()).uv2(fullbright).endVertex();
        buffer.vertex(mat, x + width, y, 0).color(1, 1, 1, alpha).uv(icon.getU1(), icon.getV0()).uv2(fullbright).endVertex();
        buffer.vertex(mat, x, y, 0).color(1, 1, 1, alpha).uv(icon.getU0(), icon.getV0()).uv2(fullbright).endVertex();
    }
}