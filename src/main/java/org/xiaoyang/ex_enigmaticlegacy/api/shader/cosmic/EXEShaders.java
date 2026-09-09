package org.xiaoyang.ex_enigmaticlegacy.api.shader.cosmic;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.yuo.endless.client.lib.CCShaderInstance;
import com.yuo.endless.client.lib.CCUniform;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.util.Objects;
import java.util.function.Function;

import static org.xiaoyang.ex_enigmaticlegacy.Exe.MODID;

@SuppressWarnings("removal")
@EventBusSubscriber(
        modid = "ex_enigmaticlegacy",
        value = {Dist.CLIENT},
        bus = Bus.MOD
)

public final class EXEShaders {
    public static final float[] COSMIC_UVS = new float[40];
    public static boolean inventoryRender = false;
    public static int renderTime;
    public static float tick;
    public static float renderFrame;
    public static CCShaderInstance cosmicShader;
    public static CCUniform cosmicTime;
    public static CCUniform cosmicYaw;
    public static CCUniform cosmicPitch;
    public static CCUniform cosmicExternalScale;
    public static CCUniform cosmicOpacity;
    public static CCUniform cosmicUVs;
    public static final RenderType COSMIC_RENDER_TYPE;
    public static final RenderType COSMIC_BLOCK_RENDER_TYPE;
    public static final RenderType COSMIC_ENTITY_RENDER_TYPE;
    public static final RenderType COSMIC_FLOWER_BLOCK_RENDER_TYPE;
    private static final Function<ResourceLocation, RenderType> EYES;

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        event.registerShader(CCShaderInstance.create(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("ex_enigmaticlegacy", "cosmic"), DefaultVertexFormat.BLOCK), (e) -> {
            cosmicShader = (CCShaderInstance)e;
            cosmicTime = (CCUniform)Objects.requireNonNull(cosmicShader.getUniform("time"));
            cosmicYaw = (CCUniform)Objects.requireNonNull(cosmicShader.getUniform("yaw"));
            cosmicPitch = (CCUniform)Objects.requireNonNull(cosmicShader.getUniform("pitch"));
            cosmicExternalScale = (CCUniform)Objects.requireNonNull(cosmicShader.getUniform("externalScale"));
            cosmicOpacity = (CCUniform)Objects.requireNonNull(cosmicShader.getUniform("opacity"));
            cosmicUVs = (CCUniform)Objects.requireNonNull(cosmicShader.getUniform("cosmicuvs"));
            cosmicTime.set((float)renderTime + renderFrame);
            cosmicShader.onApply(() -> cosmicTime.set((float)renderTime + renderFrame));
        });
    }

    public static RenderType eyes(ResourceLocation resourceLocation) {
        return (RenderType)EYES.apply(resourceLocation);
    }

    public static void uploadCommonUniformsForParticles() {
        if (cosmicShader == null) return;

        if (cosmicTime != null) {
            cosmicTime.set((float) renderTime + renderFrame);
        }

        if (cosmicUVs != null) {
            cosmicUVs.set(COSMIC_UVS);
        }

        if (cosmicExternalScale != null) cosmicExternalScale.set(1.0F);
        if (cosmicOpacity != null) cosmicOpacity.set(0.78F);
    }

    @EventBusSubscriber(
            modid = "ex_enigmaticlegacy",
            value = {Dist.CLIENT},
            bus = Bus.FORGE
    )
    public static class ForgeEvents {
        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (!Minecraft.getInstance().isPaused() && event.phase == Phase.END) {
                ++renderTime;
                ++tick;
                if (tick >= 720.0F) {
                    tick = 0.0F;
                }
            }
        }

        @SubscribeEvent
        public static void renderTick(TickEvent.RenderTickEvent event) {
            if (!Minecraft.getInstance().isPaused() && event.phase == Phase.START) {
                renderFrame = event.renderTickTime;
            }
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void drawScreenPre(ScreenEvent.Render.Pre e) {
            inventoryRender = true;
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void drawScreenPost(ScreenEvent.Render.Post e) {
            inventoryRender = false;
        }
    }

    static {
        COSMIC_RENDER_TYPE = RenderType.create("ex_enigmaticlegacy:cosmic",
                DefaultVertexFormat.BLOCK,
                Mode.QUADS,
                2097152,
                true,
                false,
                CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> cosmicShader))
                        .setDepthTestState(RenderStateShardAccess.EQUAL_DEPTH_TEST)
                        .setLightmapState(RenderStateShardAccess.LIGHT_MAP)
                        .setTransparencyState(RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY)
                        .setTextureState(RenderStateShardAccess.BLOCK_SHEET_MIPPED)
                        .createCompositeState(true));

        COSMIC_BLOCK_RENDER_TYPE = RenderType.create("ex_enigmaticlegacy:cosmic_block",
                DefaultVertexFormat.BLOCK,
                Mode.QUADS,
                2097152,
                true,
                false, CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> cosmicShader))
                        .setDepthTestState(RenderStateShardAccess.LEQUAL_DEPTH_TEST)
                        .setLightmapState(RenderStateShardAccess.LIGHT_MAP)
                        .setTransparencyState(RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY)
                        .setTextureState(RenderStateShardAccess.BLOCK_SHEET_MIPPED)
                        .createCompositeState(true));

        COSMIC_ENTITY_RENDER_TYPE = RenderType.create("ex_enigmaticlegacy:cosmic_entity",
                DefaultVertexFormat.NEW_ENTITY,
                Mode.QUADS, 2097152,
                false,
                true, CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> cosmicShader))
                        .setDepthTestState(RenderStateShardAccess.LEQUAL_DEPTH_TEST)
                        .setLightmapState(RenderStateShardAccess.LIGHT_MAP)
                        .setTransparencyState(RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY)
                        .setTextureState(RenderStateShardAccess.COSMIC_TEXTURE_STATE)
                        .setWriteMaskState(RenderStateShardAccess.COLOR_DEPTH_WRITE)
                        .createCompositeState(false));

        COSMIC_FLOWER_BLOCK_RENDER_TYPE = RenderType.create(
                "ex_enigmaticlegacy:cosmic_flower_block",
                DefaultVertexFormat.BLOCK,
                Mode.QUADS,
                2097152,
                true,
                false,
                CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> cosmicShader))
                        .setDepthTestState(RenderStateShardAccess.LEQUAL_DEPTH_TEST)
                        .setLightmapState(RenderStateShardAccess.LIGHT_MAP)
                        .setTransparencyState(RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY)
                        .setTextureState(RenderStateShardAccess.BLOCK_SHEET_MIPPED)
                        .setWriteMaskState(RenderStateShardAccess.COLOR_DEPTH_WRITE)
                        .setLayeringState(RenderStateShardAccess.POLYGON_OFFSET_LAYERING)
                        .createCompositeState(true)
        );

        EYES = Util.memoize((function) -> {
            RenderStateShard.TextureStateShard textureStateShard = new RenderStateShard.TextureStateShard(function, false, false);
            return RenderType.create("eyes_light", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, false, true, CompositeState.builder().setShaderState(RenderStateShardAccess.EYES_LIGHT).setTextureState(textureStateShard).setTransparencyState(RenderStateShardAccess.ADDITIVE_TRANSPARENCY).setWriteMaskState(RenderStateShardAccess.COLOR_WRITE).createCompositeState(false));
        });
    }

    public static final RenderType DIMENSIONAL_SLASH_CORE = RenderType.create("dimensional_slash_core", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 8192, false, true, RenderType.CompositeState.builder()
            .setShaderState(RenderStateShardAccess.LIGHTNING_SHADER)
            .setTransparencyState(RenderStateShardAccess.LIGHTNING_TRANSPARENCY)
            .setCullState(RenderStateShardAccess.NO_CULL)
            .setDepthTestState(RenderStateShardAccess.LEQUAL_DEPTH_TEST)
            .setOutputState(RenderStateShardAccess.MAIN_TARGET)
            .setWriteMaskState(RenderStateShardAccess.COLOR_WRITE)
            .createCompositeState(false)
    );

    public static final RenderType DIMENSIONAL_SLASH_INK_CORE = RenderType.create("dimensional_slash_ink_core", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 8192, false, true, RenderType.CompositeState.builder()
            .setShaderState(RenderStateShardAccess.POSITION_COLOR_SHADER)
            .setTransparencyState(RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY)
            .setCullState(RenderStateShardAccess.NO_CULL)
            .setDepthTestState(RenderStateShardAccess.LEQUAL_DEPTH_TEST)
            .setOutputState(RenderStateShardAccess.MAIN_TARGET)
            .setWriteMaskState(RenderStateShardAccess.COLOR_WRITE)
            .createCompositeState(false)
    );

    public static final RenderType DIMENSIONAL_SLASH_ENTITY_PIERCE = RenderType.create("dimensional_slash_entity_pierce", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 8192, false, true, RenderType.CompositeState.builder()
            .setShaderState(RenderStateShardAccess.LIGHTNING_SHADER)
            .setTransparencyState(RenderStateShardAccess.LIGHTNING_TRANSPARENCY)
            .setCullState(RenderStateShardAccess.NO_CULL)
            .setDepthTestState(RenderStateShardAccess.NO_DEPTH_TEST)
            .setOutputState(RenderStateShardAccess.MAIN_TARGET)
            .setWriteMaskState(RenderStateShardAccess.COLOR_WRITE)
            .createCompositeState(false)
    );

    public static final RenderType DIMENSIONAL_SLASH_ENTITY_PIERCE_INK = RenderType.create("dimensional_slash_entity_pierce_ink", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 8192, false, true, RenderType.CompositeState.builder()
            .setShaderState(RenderStateShardAccess.POSITION_COLOR_SHADER)
            .setTransparencyState(RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY)
            .setCullState(RenderStateShardAccess.NO_CULL)
            .setDepthTestState(RenderStateShardAccess.NO_DEPTH_TEST)
            .setOutputState(RenderStateShardAccess.MAIN_TARGET)
            .setWriteMaskState(RenderStateShardAccess.COLOR_WRITE)
            .createCompositeState(false)
    );

    public static final RenderType COSMIC_BLOCK_AFTER_LEVEL_RENDER_TYPE = RenderType.create(MODID + ":cosmic_block_after_level", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false, RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(() -> cosmicShader))
            .setDepthTestState(RenderStateShardAccess.LEQUAL_DEPTH_TEST)
            .setLightmapState(RenderStateShardAccess.LIGHT_MAP)
            .setTransparencyState(RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY)
            .setTextureState(RenderStateShardAccess.BLOCK_SHEET_MAPPED)
            .setLayeringState(RenderStateShardAccess.SHADER_LAYER_DEPTH_BIAS)
            .setOutputState(RenderStateShardAccess.MAIN_TARGET)
            .setWriteMaskState(RenderStateShardAccess.COLOR_WRITE)
            .createCompositeState(false));

    private static class RenderStateShardAccess extends RenderStateShard {
        private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation(MODID,"textures/entity/in_sky.png");
        private static final ResourceLocation END_PORTAL_LOCATION = new ResourceLocation(MODID,"textures/entity/starry_sky.png");
        private static final RenderStateShard.DepthTestStateShard NO_DEPTH_TEST = RenderStateShard.NO_DEPTH_TEST;
        private static final RenderStateShard.DepthTestStateShard EQUAL_DEPTH_TEST = RenderStateShard.EQUAL_DEPTH_TEST; // ITEM
        private static final RenderStateShard.DepthTestStateShard LEQUAL_DEPTH_TEST = RenderStateShard.LEQUAL_DEPTH_TEST; // BLOCK
        private static final RenderStateShard.DepthTestStateShard GREATER_DEPTH_TEST = RenderStateShard.GREATER_DEPTH_TEST;
        public static final RenderStateShard.LightmapStateShard LIGHT_MAP = RenderStateShard.LIGHTMAP;
        private static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = RenderStateShard.TRANSLUCENT_TRANSPARENCY;
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
        private static final RenderStateShard.WriteMaskStateShard COLOR_DEPTH_WRITE = RenderStateShard.COLOR_DEPTH_WRITE;
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
        private static final TextureStateShard BLOCK_SHEET_MIPPED;
        private static final ResourceLocation COSMIC_ATLAS;
        private static final TextureStateShard COSMIC_TEXTURE_STATE;
        private static final LayeringStateShard POLYGON_OFFSET_LAYERING;

        static {
            BLOCK_SHEET_MIPPED = RenderStateShard.BLOCK_SHEET_MIPPED;
            COSMIC_ATLAS = new ResourceLocation("ex_enigmaticlegacy", "textures/atlas/particles.png");
            COSMIC_TEXTURE_STATE = new TextureStateShard(COSMIC_ATLAS, true, true);
            POLYGON_OFFSET_LAYERING = new LayeringStateShard(
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