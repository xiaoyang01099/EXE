package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.block;

import codechicken.lib.render.shader.CCShaderInstance;
import codechicken.lib.render.shader.CCUniform;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.Objects;
import java.util.function.Function;
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
        event.registerShader(CCShaderInstance.create(event.getResourceManager(), new ResourceLocation("ex_enigmaticlegacy", "cosmic"), DefaultVertexFormat.BLOCK), (e) -> {
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
        public static void drawScreenPre(ScreenEvent.DrawScreenEvent.Pre e) {
            inventoryRender = true;
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void drawScreenPost(ScreenEvent.DrawScreenEvent.Post e) {
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

    private static class RenderStateShardAccess extends RenderStateShard {
        private static final DepthTestStateShard EQUAL_DEPTH_TEST;
        private static final DepthTestStateShard LEQUAL_DEPTH_TEST;
        private static final LightmapStateShard LIGHT_MAP;
        private static final TransparencyStateShard TRANSLUCENT_TRANSPARENCY;
        private static final TextureStateShard BLOCK_SHEET_MIPPED;
        private static final TextureStateShard BLOCK_SHEET;
        private static final CullStateShard NO_CULL;
        private static final ResourceLocation COSMIC_ATLAS;
        private static final TextureStateShard COSMIC_TEXTURE_STATE;
        private static final ShaderStateShard EYES_LIGHT;
        private static final WriteMaskStateShard COLOR_WRITE;
        protected static final WriteMaskStateShard COLOR_DEPTH_WRITE;
        private static final TransparencyStateShard ADDITIVE_TRANSPARENCY;
        private static final LayeringStateShard POLYGON_OFFSET_LAYERING;

        private RenderStateShardAccess(String pName, Runnable pSetupState, Runnable pClearState) {
            super(pName, pSetupState, pClearState);
        }

        static {
            EQUAL_DEPTH_TEST = RenderStateShard.EQUAL_DEPTH_TEST;
            LEQUAL_DEPTH_TEST = RenderStateShard.LEQUAL_DEPTH_TEST;
            LIGHT_MAP = RenderStateShard.LIGHTMAP;
            TRANSLUCENT_TRANSPARENCY = RenderStateShard.TRANSLUCENT_TRANSPARENCY;
            BLOCK_SHEET_MIPPED = RenderStateShard.BLOCK_SHEET_MIPPED;
            BLOCK_SHEET = RenderStateShard.BLOCK_SHEET;
            NO_CULL = RenderStateShard.NO_CULL;
            COSMIC_ATLAS = new ResourceLocation("ex_enigmaticlegacy", "textures/atlas/particles.png");
            COSMIC_TEXTURE_STATE = new TextureStateShard(COSMIC_ATLAS, true, true);
            EYES_LIGHT = new ShaderStateShard(GameRenderer::getRendertypeEndPortalShader);
            COLOR_WRITE = new WriteMaskStateShard(true, false);
            COLOR_DEPTH_WRITE = new WriteMaskStateShard(true, true);
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

            ADDITIVE_TRANSPARENCY = new TransparencyStateShard("additive_transparency", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(SourceFactor.ONE, DestFactor.ONE);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });
        }
    }
}