package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import vazkii.botania.mixin.client.RenderTypeAccessor;

public final class SpecialRenderHelper extends RenderType {
    public static final RenderType RAINBOW_MANA_WATER;
    public static final RenderType POLYCHROME_COLLAPSE_PRISM;
    public static final RenderType COSMIC_BACKGROUND;
    public static final RenderType EVIL_WATER;
    public static final RenderType STARRY_SKY;
    public static final RenderType BLACK_HOLE;
    public static final RenderType ANDROMEDA;
    public static final RenderType KATANA;
    public static final RenderType PRISMA;
    public static final RenderType STAR_LINE;

    private SpecialRenderHelper(String string, VertexFormat vertexFormat, VertexFormat.Mode mode,
                                int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
        super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
        throw new UnsupportedOperationException();
    }

    private static RenderType makeLayer(String name, VertexFormat format, VertexFormat.Mode mode,
                                        int bufSize, boolean hasCrumbling, boolean sortOnUpload,
                                        CompositeState glState) {
        return RenderTypeAccessor.create(name, format, mode, bufSize, hasCrumbling, sortOnUpload, glState);
    }

    private static RenderType makeLayer(String name, VertexFormat format, VertexFormat.Mode mode,
                                        int bufSize, CompositeState glState) {
        return makeLayer(name, format, mode, bufSize, false, false, glState);
    }

    static {
        RenderType.CompositeState glState = CompositeState.builder().setShaderState(POSITION_COLOR_SHADER).setWriteMaskState(COLOR_WRITE).setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY).createCompositeState(false);

//        CompositeState glState = CompositeState.builder()
//                .setTextureState(BLOCK_SHEET_MIPPED)
//                .setShaderState(new ShaderStateShard(SpecialCoreShaders::rainbowManaWater))
//                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
//                .setOutputState(ITEM_ENTITY_TARGET)
//                .setLightmapState(LIGHTMAP)
//                .createCompositeState(false);
        RAINBOW_MANA_WATER = makeLayer(Exe.MODID + ":rainbow_mana_water",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 128, glState);

        glState = CompositeState.builder()
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::polychromeCollapsePrismOverlay))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);
        POLYCHROME_COLLAPSE_PRISM = makeLayer(Exe.MODID + ":polychrome_collapse_prism",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 128, glState);

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
        COSMIC_BACKGROUND = makeLayer(Exe.MODID + ":cosmic_background",
                DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, glState);

        glState = CompositeState.builder()
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::evilWater))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);
        EVIL_WATER = makeLayer(Exe.MODID + ":evil_water",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 128, glState);

        glState = CompositeState.builder()
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::getStarrySkyShader))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);
        STARRY_SKY = makeLayer(Exe.MODID + ":starry_sky",
                DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, glState);

        glState = CompositeState.builder()
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::getBlackHoleShader))
                .setTextureState(NO_TEXTURE)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .createCompositeState(false);
        BLACK_HOLE = makeLayer(Exe.MODID + ":blackhole",
                DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, glState);

        glState = CompositeState.builder()
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::getAndromedaShader))
                .setTextureState(new TextureStateShard(
                        new ResourceLocation("ex_enigmaticlegacy", "textures/atlas/noise.png"),
                        false,
                        false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .createCompositeState(false);
        ANDROMEDA = makeLayer(Exe.MODID + ":andromeda",
                DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, glState);

        glState = CompositeState.builder()
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::getKatanaShader))
                .setTextureState(NO_TEXTURE)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .createCompositeState(false);
        KATANA = makeLayer(Exe.MODID + ":katana",
                DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, glState);

        glState = CompositeState.builder()
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::getPrismaShader))
                .setTextureState(NO_TEXTURE)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .createCompositeState(false);
        PRISMA = makeLayer(Exe.MODID + ":prisma",
                DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, glState);

        glState = CompositeState.builder()
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::getStarLineShader))
                .setTextureState(NO_TEXTURE)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .createCompositeState(false);
        STAR_LINE = makeLayer(Exe.MODID + ":star_line",
                DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, glState);
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