package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader;

import codechicken.lib.render.shader.CCShaderInstance;
import codechicken.lib.render.shader.CCUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.AccessUtils;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RainbowAvaritiaShaders {
    public static final float[] COSMIC_UVS = new float[40];
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

    static {
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

    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(
                    CCShaderInstance.create(
                            event.getResourceManager(),
                            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "rainbow_cosmic"),
                            DefaultVertexFormat.BLOCK
                    ),
                    e -> {
                        cosmicShader = (CCShaderInstance)e;
                        cosmicTime = Objects.requireNonNull(cosmicShader.getUniform("time"));
                        cosmicYaw = Objects.requireNonNull(cosmicShader.getUniform("yaw"));
                        cosmicPitch = Objects.requireNonNull(cosmicShader.getUniform("pitch"));
                        cosmicExternalScale = Objects.requireNonNull(cosmicShader.getUniform("externalScale"));
                        cosmicOpacity = Objects.requireNonNull(cosmicShader.getUniform("opacity"));
                        cosmicUVs = Objects.requireNonNull(cosmicShader.getUniform("cosmicuvs"));
                        FogColor = cosmicShader.getUniform("FogColor");

                        cosmicTime.set((float)renderTime + renderFrame);

                        cosmicShader.onApply(() -> {
                            cosmicTime.set((float)renderTime + renderFrame);

                            if (FogColor != null) {
                                float hue = ((float) Util.getMillis() / 5000.0F) % 1.0F;
                                int rgb = Mth.hsvToRgb(hue * 6.0F, 1.0F, 1.0F);

                                float r = ((rgb >> 16) & 0xFF) / 255.0F;
                                float g = ((rgb >> 8) & 0xFF) / 255.0F;
                                float b = (rgb & 0xFF) / 255.0F;

                                FogColor.set(r, g, b, 1.0F);
                            }
                        });

                        ExEnigmaticlegacyMod.LOGGER.info("Rainbow Cosmic Shader loaded successfully!");
                    }
            );
        } catch (Exception exception) {
            ExEnigmaticlegacyMod.LOGGER.error("Failed to load rainbow cosmic shader", exception);
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