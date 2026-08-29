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
import vazkii.botania.client.core.helper.CoreShaders;
import vazkii.botania.client.render.block_entity.PylonBlockEntityRenderer;
import vazkii.botania.mixin.client.RenderTypeAccessor;

public final class SpecialRenderHelper extends RenderType {
    public static final RenderType RAINBOW_MANA_WATER;
    public static final RenderType POLYCHROME_COLLAPSE_PRISM;
    public static final RenderType COSMIC_BACKGROUND;
    public static final RenderType EVIL_WATER;
    public static final RenderType STARRY_SKY;
    public static final RenderType BLACK_HOLE;
    public static final RenderType ANDROMEDA;
    public static final RenderType STAR_LINE;

    public static final RenderType ENCHANTER_RUNE_AFTER_LEVEL ;
    public static final RenderType RAINBOW_MANA_WATER_AFTER_LEVEL;
    public static final RenderType POLYCHROME_COLLAPSE_PRISM_AFTER_LEVEL;
    public static final RenderType MANA_POOL_WATER_AFTER_LEVEL;
    public static final RenderType TERRA_PLATE_AFTER_LEVEL;
    public static final RenderType MANA_PYLON_GLOW_AFTER_LEVEL;
    public static final RenderType NATURA_PYLON_GLOW_AFTER_LEVEL;
    public static final RenderType GAIA_PYLON_GLOW_AFTER_LEVEL;
    public static final RenderType END_PORTAL_AFTER_LEVEL;

    private SpecialRenderHelper(String name, VertexFormat format, VertexFormat.Mode mode,
                                int bufferSize, boolean crumbling, boolean sortOnUpload,
                                Runnable setup, Runnable clear) {
        super(name, format, mode, bufferSize, crumbling, sortOnUpload, setup, clear);
        throw new UnsupportedOperationException("Should not be instantiated");
    }

    private static RenderType makeLayer(String name, VertexFormat format, VertexFormat.Mode mode,
                                        int bufferSize, boolean crumbling, boolean sortOnUpload,
                                        CompositeState state) {
        return RenderTypeAccessor.create(
                name, format, mode, bufferSize, crumbling, sortOnUpload, state
        );
    }

    private static RenderType makeLayer(String name, VertexFormat format, VertexFormat.Mode mode,
                                        int bufferSize, CompositeState state) {
        return makeLayer(name, format, mode, bufferSize, false, false, state);
    }

    private static RenderType pylonAfterLevel(String name, ResourceLocation texture) {
        CompositeState state = CompositeState.builder()
                .setShaderState(new ShaderStateShard(CoreShaders::pylon))
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setOutputState(MAIN_TARGET)
                .createCompositeState(true);
        return makeLayer(Exe.MODID + ":" + name,
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 128, state);
    }

    private static final RenderStateShard.ShaderStateShard BLADE_SHADER =
            new RenderStateShard.ShaderStateShard(SpecialCoreShaders::getBladeShader);
    private static final RenderStateShard.ShaderStateShard SLASH_SHADER =
            new RenderStateShard.ShaderStateShard(SpecialCoreShaders::getSlashShader);

    private static final RenderType.CompositeState BLADE_STATE =
            RenderType.CompositeState.builder()
                    .setShaderState(BLADE_SHADER)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setOverlayState(RenderStateShard.NO_OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false);

    private static final RenderType.CompositeState SLASH_STATE =
            RenderType.CompositeState.builder()
                    .setShaderState(SLASH_SHADER)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setOverlayState(RenderStateShard.NO_OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false);

    public static final RenderType BLADE = RenderType.create(
            "ex_enigmaticlegacy_blade",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            BLADE_STATE
    );
    public static final RenderType SLASH = RenderType.create(
            "ex_enigmaticlegacy_slash",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            SLASH_STATE
    );

    static {
        CompositeState state = CompositeState.builder()
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::rainbowManaWater))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);
        RAINBOW_MANA_WATER = makeLayer(Exe.MODID + ":rainbow_mana_water",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 128, state);

        state = CompositeState.builder()
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::polychromeCollapsePrismOverlay))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);
        POLYCHROME_COLLAPSE_PRISM = makeLayer(Exe.MODID + ":polychrome_collapse_prism",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 128, state);

        state = CompositeState.builder()
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
                DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, state);

        state = CompositeState.builder()
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::evilWater))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);
        EVIL_WATER = makeLayer(Exe.MODID + ":evil_water",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 128, state);

        state = CompositeState.builder()
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::getStarrySkyShader))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);
        STARRY_SKY = makeLayer(Exe.MODID + ":starry_sky",
                DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, state);

        state = CompositeState.builder()
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::getBlackHoleShader))
                .setTextureState(NO_TEXTURE)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .createCompositeState(false);
        BLACK_HOLE = makeLayer(Exe.MODID + ":blackhole",
                DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, state);

        state = CompositeState.builder()
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::getAndromedaShader))
                .setTextureState(new TextureStateShard(
                        new ResourceLocation(Exe.MODID, "textures/atlas/noise.png"), false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .createCompositeState(false);
        ANDROMEDA = makeLayer(Exe.MODID + ":andromeda",
                DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, state);

        state = CompositeState.builder()
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::getStarLineShader))
                .setTextureState(NO_TEXTURE)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .createCompositeState(false);
        STAR_LINE = makeLayer(Exe.MODID + ":star_line",
                DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, state);

        state = CompositeState.builder()
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::rainbowManaWater))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(MAIN_TARGET)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);
        RAINBOW_MANA_WATER_AFTER_LEVEL = makeLayer(Exe.MODID + ":rainbow_mana_water_after_level",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 128, state);

        state = CompositeState.builder()
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setShaderState(new ShaderStateShard(SpecialCoreShaders::polychromeCollapsePrismOverlay))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(MAIN_TARGET)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);
        POLYCHROME_COLLAPSE_PRISM_AFTER_LEVEL = makeLayer(Exe.MODID + ":polychrome_collapse_prism_after_level",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 128, state);

        state = CompositeState.builder()
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setShaderState(new ShaderStateShard(CoreShaders::manaPool))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(MAIN_TARGET)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);
        MANA_POOL_WATER_AFTER_LEVEL = makeLayer(Exe.MODID + ":mana_pool_water_after_level",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 128, state);

        state = CompositeState.builder()
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setShaderState(new ShaderStateShard(CoreShaders::enchanter))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(MAIN_TARGET)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);
        ENCHANTER_RUNE_AFTER_LEVEL = makeLayer(Exe.MODID + ":enchanter_rune_after_level",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 128, state);

        state = CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_END_PORTAL_SHADER)
                .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                                .add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
                                .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
                                .build())
                .createCompositeState(false);
        END_PORTAL_AFTER_LEVEL = makeLayer(Exe.MODID + ":end_portal_after_level",
                DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 256, false, false, state);

        state = CompositeState.builder()
                .setTextureState(BLOCK_SHEET_MIPPED)
                .setShaderState(new ShaderStateShard(CoreShaders::terraPlate))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(MAIN_TARGET)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false);
        TERRA_PLATE_AFTER_LEVEL = makeLayer(Exe.MODID + ":terra_plate_after_level",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 128, state);

        MANA_PYLON_GLOW_AFTER_LEVEL = pylonAfterLevel(
                "mana_pylon_glow_after_level", PylonBlockEntityRenderer.MANA_TEXTURE);
        NATURA_PYLON_GLOW_AFTER_LEVEL = pylonAfterLevel(
                "natura_pylon_glow_after_level", PylonBlockEntityRenderer.NATURA_TEXTURE);
        GAIA_PYLON_GLOW_AFTER_LEVEL = pylonAfterLevel(
                "gaia_pylon_glow_after_level", PylonBlockEntityRenderer.GAIA_TEXTURE);
    }

    public static void renderIcon(PoseStack poseStack, VertexConsumer buffer, int x, int y,
                                  TextureAtlasSprite icon, int width, int height, float alpha) {
        Matrix4f matrix = poseStack.last().pose();
        int fullBright = 0xF000F0;
        buffer.vertex(matrix, x, y + height, 0).color(1, 1, 1, alpha)
                .uv(icon.getU0(), icon.getV1()).uv2(fullBright).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).color(1, 1, 1, alpha)
                .uv(icon.getU1(), icon.getV1()).uv2(fullBright).endVertex();
        buffer.vertex(matrix, x + width, y, 0).color(1, 1, 1, alpha)
                .uv(icon.getU1(), icon.getV0()).uv2(fullBright).endVertex();
        buffer.vertex(matrix, x, y, 0).color(1, 1, 1, alpha)
                .uv(icon.getU0(), icon.getV0()).uv2(fullBright).endVertex();
    }
}