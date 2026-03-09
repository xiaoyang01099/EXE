package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader;

import codechicken.lib.render.shader.CCShaderInstance;
import codechicken.lib.render.shader.CCUniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.AccessUtils;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import org.lwjgl.opengl.GL13;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, value = Dist.CLIENT)
public final class RainbowAvaritiaShaders {
    public static final float[] COSMIC_UVS = new float[40];
    public static final float[] PARTICLE_COSMIC_UVS = new float[40];
    public static boolean inventoryRender = false;
    public static int renderTime;
    public static float renderFrame;

    public static CCShaderInstance cosmicShader;
    public static CCUniform cosmicTime;
    public static CCUniform cosmicYaw;
    public static CCUniform cosmicPitch;
    public static CCUniform cosmicExternalScale;
    public static CCUniform cosmicOpacity;
    public static CCUniform cosmicUVs;
    public static CCUniform FogColor;
    public static RenderType RAINBOW_COSMIC_RENDER_TYPE;

    public static CCShaderInstance particleCosmicShader;
    public static CCUniform particleCosmicTime;
    public static CCUniform particleCosmicYaw;
    public static CCUniform particleCosmicPitch;
    public static CCUniform particleCosmicExternalScale;
    public static CCUniform particleCosmicOpacity;
    public static CCUniform particleCosmicUVs;
    public static CCUniform particleFogColor;

    private static final ResourceLocation[] COSMIC_LOCATIONS = new ResourceLocation[10];
    private static boolean cosmicUVsInitialized = false;
    private static boolean particleCosmicUVsInitialized = false;

    static {
        for (int i = 0; i < 10; i++) {
            COSMIC_LOCATIONS[i] = new ResourceLocation(ExEnigmaticlegacyMod.MODID, "shader/cosmic_" + i);
        }

        RAINBOW_COSMIC_RENDER_TYPE = RenderType.create(
                "ex_enigmaticlegacy:rainbow_cosmic",
                DefaultVertexFormat.BLOCK,
                VertexFormat.Mode.QUADS,
                2097152,
                true,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> cosmicShader))
                        .setDepthTestState(AccessUtils.EQUAL_DEPTH_TEST)
                        .setLightmapState(AccessUtils.LIGHT_MAP)
                        .setTransparencyState(AccessUtils.TRANSLUCENT_TRANSPARENCY)
                        .setTextureState(AccessUtils.BLOCK_SHEET_MIPPED)
                        .createCompositeState(true)
        );
    }

    @SubscribeEvent
    public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
        ResourceLocation atlasLocation = event.getAtlas().location();

        if (atlasLocation.equals(TextureAtlas.LOCATION_BLOCKS) ||
                atlasLocation.equals(TextureAtlas.LOCATION_PARTICLES)) {
            for (ResourceLocation location : COSMIC_LOCATIONS) {
                event.addSprite(location);
            }
        }
    }

    @SubscribeEvent
    public static void onTextureStitchPost(TextureStitchEvent.Post event) {
        ResourceLocation atlasLocation = event.getAtlas().location();

        if (atlasLocation.equals(TextureAtlas.LOCATION_BLOCKS)) {
            initCosmicUVs(event.getAtlas());
        }

        if (atlasLocation.equals(TextureAtlas.LOCATION_PARTICLES)) {
            initParticleCosmicUVs(event.getAtlas());
        }
    }

    private static void initCosmicUVs(TextureAtlas atlas) {
        for (int i = 0; i < 10; i++) {
            TextureAtlasSprite sprite = atlas.getSprite(COSMIC_LOCATIONS[i]);

            COSMIC_UVS[i * 4] = sprite.getU0();
            COSMIC_UVS[i * 4 + 1] = sprite.getV0();
            COSMIC_UVS[i * 4 + 2] = sprite.getU1();
            COSMIC_UVS[i * 4 + 3] = sprite.getV1();
        }

        cosmicUVsInitialized = true;

        if (cosmicUVs != null) {
            cosmicUVs.set(COSMIC_UVS);
        }
    }

    private static void initParticleCosmicUVs(TextureAtlas atlas) {
        for (int i = 0; i < 10; i++) {
            TextureAtlasSprite sprite = atlas.getSprite(COSMIC_LOCATIONS[i]);

            PARTICLE_COSMIC_UVS[i * 4] = sprite.getU0();
            PARTICLE_COSMIC_UVS[i * 4 + 1] = sprite.getV0();
            PARTICLE_COSMIC_UVS[i * 4 + 2] = sprite.getU1();
            PARTICLE_COSMIC_UVS[i * 4 + 3] = sprite.getV1();
        }

        particleCosmicUVsInitialized = true;

        if (particleCosmicUVs != null) {
            particleCosmicUVs.set(PARTICLE_COSMIC_UVS);
        }
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(
                    CCShaderInstance.create(
                            event.getResourceManager(),
                            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "rainbow_cosmic"),
                            DefaultVertexFormat.BLOCK
                    ),
                    e -> {
                        cosmicShader = (CCShaderInstance) e;
                        cosmicTime = Objects.requireNonNull(cosmicShader.getUniform("time"));
                        cosmicYaw = Objects.requireNonNull(cosmicShader.getUniform("yaw"));
                        cosmicPitch = Objects.requireNonNull(cosmicShader.getUniform("pitch"));
                        cosmicExternalScale = Objects.requireNonNull(cosmicShader.getUniform("externalScale"));
                        cosmicOpacity = Objects.requireNonNull(cosmicShader.getUniform("opacity"));
                        cosmicUVs = Objects.requireNonNull(cosmicShader.getUniform("cosmicuvs"));
                        FogColor = cosmicShader.getUniform("FogColor");

                        if (cosmicUVsInitialized) {
                            cosmicUVs.set(COSMIC_UVS);
                        }

                        cosmicTime.set((float) renderTime + renderFrame);

                        cosmicShader.onApply(() -> {
                            cosmicTime.set((float) renderTime + renderFrame);

                            if (FogColor != null) {
                                float hue = ((float) Util.getMillis() / 5000.0F) % 1.0F;
                                int rgb = Mth.hsvToRgb(hue * 6.0F, 1.0F, 1.0F);

                                float r = ((rgb >> 16) & 0xFF) / 255.0F;
                                float g = ((rgb >> 8) & 0xFF) / 255.0F;
                                float b = (rgb & 0xFF) / 255.0F;

                                FogColor.set(r, g, b, 1.0F);
                            }
                        });
                    }
            );

            event.registerShader(
                    CCShaderInstance.create(
                            event.getResourceManager(),
                            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "rainbow_cosmic_particle"),
                            DefaultVertexFormat.PARTICLE
                    ),
                    e -> {
                        particleCosmicShader = (CCShaderInstance) e;
                        particleCosmicTime = Objects.requireNonNull(particleCosmicShader.getUniform("time"));
                        particleCosmicYaw = Objects.requireNonNull(particleCosmicShader.getUniform("yaw"));
                        particleCosmicPitch = Objects.requireNonNull(particleCosmicShader.getUniform("pitch"));
                        particleCosmicExternalScale = Objects.requireNonNull(particleCosmicShader.getUniform("externalScale"));
                        particleCosmicOpacity = Objects.requireNonNull(particleCosmicShader.getUniform("opacity"));
                        particleCosmicUVs = Objects.requireNonNull(particleCosmicShader.getUniform("cosmicuvs"));
                        particleFogColor = particleCosmicShader.getUniform("FogColor");

                        if (particleCosmicUVsInitialized) {
                            particleCosmicUVs.set(PARTICLE_COSMIC_UVS);
                        }

                        particleCosmicTime.set((float) renderTime + renderFrame);

                        particleCosmicShader.onApply(() -> {
                            particleCosmicTime.set((float) renderTime + renderFrame);

                            Minecraft mc = Minecraft.getInstance();
                            if (mc.player != null) {
                                float yaw = (float) Math.toRadians(mc.player.getYRot());
                                float pitch = (float) Math.toRadians(mc.player.getXRot());
                                particleCosmicYaw.set(yaw);
                                particleCosmicPitch.set(pitch);
                            }

                            particleCosmicExternalScale.set(1.0F);
                            particleCosmicOpacity.set(1.0F);

                            if (particleCosmicUVsInitialized) {
                                particleCosmicUVs.set(PARTICLE_COSMIC_UVS);
                            }

                            if (particleFogColor != null) {
                                float hue = ((float) Util.getMillis() / 5000.0F) % 1.0F;
                                int rgb = Mth.hsvToRgb(hue * 6.0F, 1.0F, 1.0F);

                                float r = ((rgb >> 16) & 0xFF) / 255.0F;
                                float g = ((rgb >> 8) & 0xFF) / 255.0F;
                                float b = (rgb & 0xFF) / 255.0F;

                                particleFogColor.set(r, g, b, 1.0F);
                            }
                        });
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.END) {
            ++renderTime;
        }
    }

    @SubscribeEvent
    public static void renderTick(TickEvent.RenderTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.START) {
            renderFrame = event.renderTickTime;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void drawScreenPre(final ScreenEvent.DrawScreenEvent.Pre e) {
        RainbowAvaritiaShaders.inventoryRender = true;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void drawScreenPost(final ScreenEvent.DrawScreenEvent.Post e) {
        RainbowAvaritiaShaders.inventoryRender = false;
    }
}