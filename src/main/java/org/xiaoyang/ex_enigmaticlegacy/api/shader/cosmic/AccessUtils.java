package org.xiaoyang.ex_enigmaticlegacy.api.shader.cosmic;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

import static org.xiaoyang.ex_enigmaticlegacy.Exe.MODID;

public final class AccessUtils {
    public static final RenderStateShard.DepthTestStateShard EQUAL_DEPTH_TEST = RenderStateShardAccess.EQUAL_DEPTH_TEST;
    public static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY;
    public static final RenderStateShard.TextureStateShard BLOCK_SHEET_MIPPED = RenderStateShardAccess.BLOCK_SHEET_MIPPED;
    public static final RenderStateShard.LightmapStateShard LIGHT_MAP = RenderStateShardAccess.LIGHT_MAP;

    public static final class RenderStateShardAccess extends RenderStateShard {
        private static final LightmapStateShard LIGHT_MAP = RenderStateShard.LIGHTMAP;
        private static final DepthTestStateShard EQUAL_DEPTH_TEST = RenderStateShard.EQUAL_DEPTH_TEST;
        private static final TransparencyStateShard TRANSLUCENT_TRANSPARENCY = RenderStateShard.TRANSLUCENT_TRANSPARENCY;
        private static final TextureStateShard BLOCK_SHEET_MIPPED = RenderStateShard.BLOCK_SHEET_MIPPED;

        private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation(MODID, "textures/entity/in_sky.png");
        private static final ResourceLocation END_PORTAL_LOCATION = new ResourceLocation(MODID, "textures/entity/starry_sky.png");
        private static final RenderStateShard.DepthTestStateShard NO_DEPTH_TEST = RenderStateShard.NO_DEPTH_TEST;
        public static final RenderStateShard.DepthTestStateShard LEQUAL_DEPTH_TEST = RenderStateShard.LEQUAL_DEPTH_TEST; // BLOCK
        private static final RenderStateShard.DepthTestStateShard GREATER_DEPTH_TEST = RenderStateShard.GREATER_DEPTH_TEST;
        private static final RenderStateShard.TextureStateShard BLOCK_SHEET_MAPPED = RenderStateShard.BLOCK_SHEET_MIPPED;
        private static final RenderStateShard.TextureStateShard BLOCK_SHEET = RenderStateShard.BLOCK_SHEET;
        public static final RenderStateShard.CullStateShard NO_CULL = RenderStateShard.NO_CULL;
        private static final RenderStateShard.CullStateShard CULL = RenderStateShard.CULL;
        public static final RenderStateShard.OverlayStateShard OVERLAY = RenderStateShard.OVERLAY;
        public static final RenderStateShard.ShaderStateShard RENDERER_ENTITY_TRANSLUCENT = new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityCutoutNoCullShader);
        private static final RenderStateShard.ShaderStateShard EYES_LIGHT = new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEyesShader);
        private static final RenderStateShard.ShaderStateShard LIGHTNING_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLightningShader);
        private static final RenderStateShard.ShaderStateShard POSITION_COLOR_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader);
        private static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityCutoutNoCullShader);
        private static final RenderStateShard.ShaderStateShard RENDERTYPE_CUTOUT_MIPPED_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeCutoutMippedShader);
        public static final RenderStateShard.WriteMaskStateShard COLOR_WRITE = RenderStateShard.COLOR_WRITE;
        private static final RenderStateShard.WriteMaskStateShard DEPTH_WRITE = RenderStateShard.DEPTH_WRITE;
        public static final RenderStateShard.WriteMaskStateShard COLOR_DEPTH_WRITE = RenderStateShard.COLOR_DEPTH_WRITE;
        private static final RenderStateShard.LayeringStateShard VIEW_OFFSET_Z_LAYERING = RenderStateShard.VIEW_OFFSET_Z_LAYERING;
        private static final RenderStateShard.LayeringStateShard SHADER_LAYER_DEPTH_BIAS = new RenderStateShard.LayeringStateShard("adorablearmory_shader_layer_depth_bias", () -> {
            RenderSystem.polygonOffset(-1.0F, -32.0F);
            RenderSystem.enablePolygonOffset();
        }, () -> {
            RenderSystem.polygonOffset(0.0F, 0.0F);
            RenderSystem.disablePolygonOffset();
        });
        public static final RenderStateShard.TransparencyStateShard ADDITIVE_TRANSPARENCY = RenderStateShard.ADDITIVE_TRANSPARENCY;
        private static final RenderStateShard.EmptyTextureStateShard NO_TEXTURE = RenderStateShard.NO_TEXTURE;
        private static final RenderStateShard.TransparencyStateShard LIGHTNING_TRANSPARENCY = RenderStateShard.LIGHTNING_TRANSPARENCY;
        private static final RenderStateShard.OutputStateShard MAIN_TARGET = RenderStateShard.MAIN_TARGET;
        private static final RenderStateShard.OutputStateShard WEATHER_TARGET = new RenderStateShard.OutputStateShard("weather_target", () -> {
            if (Minecraft.useShaderTransparency()) {
                Objects.requireNonNull(Minecraft.getInstance().levelRenderer.getWeatherTarget()).bindWrite(false);
            }

        }, () -> {
            if (Minecraft.useShaderTransparency()) {
                Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            }
        });
        private static final ResourceLocation COSMIC_ATLAS;
        private static final RenderStateShard.TextureStateShard COSMIC_TEXTURE_STATE;
        private static final RenderStateShard.LayeringStateShard POLYGON_OFFSET_LAYERING;

        static {
            COSMIC_ATLAS = new ResourceLocation("ex_enigmaticlegacy", "textures/atlas/particles.png");
            COSMIC_TEXTURE_STATE = new RenderStateShard.TextureStateShard(COSMIC_ATLAS, true, true);
            POLYGON_OFFSET_LAYERING = new RenderStateShard.LayeringStateShard(
                    "polygon_offset_layering",
                    () -> {
                        RenderSystem.polygonOffset(-1.0F, -10.0F);
                        RenderSystem.enablePolygonOffset();
                    },
                    () -> {
                        RenderSystem.polygonOffset(0.0F, 0.0F);
                        RenderSystem.disablePolygonOffset();
                    }
            );
        }

        private static final RenderStateShard.TransparencyStateShard GUI_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("gui_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        });

        public static final RenderStateShard.TransparencyStateShard NO_TRANSPARENCY = RenderStateShard.NO_TRANSPARENCY;
        private static final RenderStateShard.OutputStateShard PARTICLES_TARGET = RenderStateShard.PARTICLES_TARGET;

        private static void bindShaderTexture(int slot, ResourceLocation location, boolean blur, boolean mipmap) {
            Minecraft.getInstance().getTextureManager().getTexture(location).setFilter(blur, mipmap);
            RenderSystem.setShaderTexture(slot, location);
        }

        private RenderStateShardAccess(String pName, Runnable pSetupState, Runnable pClearState) {
            super(pName, pSetupState, pClearState);
        }
    }
}