package net.xiaoyang010.ex_enigmaticlegacy.api.test.yuhua;

import codechicken.lib.render.shader.CCShaderInstance;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader.AvaritiaShaders;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class YuHuaShaders {
    public static boolean inventoryRender = false;
    public static int renderTime;
    public static float renderFrame;
    public static float[] COSMIC_UVS = new float[40];
    public static CCShaderInstance yuhuaShader;
    public static Uniform yuhuaTime;
    public static Uniform yuhuaYaw;
    public static Uniform yuhuaPitch;
    public static Uniform yuhuaExternalScale;
    public static Uniform yuhuaOpacity;
    public static Uniform yuhuaUVs;

    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;

        try {
            IEventBus eventbus = FMLJavaModLoadingContext.get().getModEventBus();
            eventbus.addListener(YuHuaShaders::onRegisterShaders);

            MinecraftForge.EVENT_BUS.addListener(YuHuaShaders::onRenderTick);
            MinecraftForge.EVENT_BUS.addListener(YuHuaShaders::clientTick);
            MinecraftForge.EVENT_BUS.addListener(YuHuaShaders::renderTick);

            initialized = true;

        } catch (Exception e) {
            ExEnigmaticlegacyMod.LOGGER.error("Failed to initialize YuHuaShaders", e);
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void onRegisterShaders(RegisterShadersEvent event) {
        ResourceManager resourceManager = event.getResourceManager();

        try {
            event.registerShader(
                    CCShaderInstance.create(
                            resourceManager,
                            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "yuhua"),
                            DefaultVertexFormat.NEW_ENTITY
                    ),
                    e -> {
                        yuhuaShader        = (CCShaderInstance) e;
                        yuhuaTime          = Objects.requireNonNull(yuhuaShader.getUniform("time"));
                        yuhuaYaw           = Objects.requireNonNull(yuhuaShader.getUniform("yaw"));
                        yuhuaPitch         = Objects.requireNonNull(yuhuaShader.getUniform("pitch"));
                        yuhuaExternalScale = Objects.requireNonNull(yuhuaShader.getUniform("externalScale"));
                        yuhuaOpacity       = Objects.requireNonNull(yuhuaShader.getUniform("opacity"));
                        yuhuaUVs           = Objects.requireNonNull(yuhuaShader.getUniform("cosmicuvs"));
                        yuhuaShader.onApply(() -> {});
                    }
            );
        } catch (Exception e) {
            ExEnigmaticlegacyMod.LOGGER.error("yuhua shader error", e);
        }
    }

    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!initialized) return;

        if (event.phase == TickEvent.Phase.START) {
            TextureAtlasSprite[] sprites = AvaritiaShaders.COSMIC_SPRITES;
            for (int i = 0; i < sprites.length; i++) {
                TextureAtlasSprite sprite = sprites[i];
                if (sprite != null) {
                    COSMIC_UVS[i * 4 + 0] = sprite.getU0();
                    COSMIC_UVS[i * 4 + 1] = sprite.getV0();
                    COSMIC_UVS[i * 4 + 2] = sprite.getU1();
                    COSMIC_UVS[i * 4 + 3] = sprite.getV1();
                }
            }
            if (yuhuaUVs != null) {
                yuhuaUVs.set(COSMIC_UVS);
            }
        }
    }

    public static void apply(float yaw, float pitch, float scale, float opacity) {
        if (yuhuaShader == null) return;
        float t = renderTime + renderFrame;
        yuhuaTime.set(t);
        yuhuaYaw.set(yaw);
        yuhuaPitch.set(pitch);
        yuhuaExternalScale.set(scale);
        yuhuaOpacity.set(opacity);
        yuhuaShader.apply();
    }

    public static void clear() {
        if (yuhuaShader == null) return;
        yuhuaShader.clear();
    }

    static void clientTick(TickEvent.ClientTickEvent event) {
        if (!Minecraft.getInstance().isPaused()
                && event.phase == TickEvent.Phase.END) {
            renderTime++;
        }
    }

    static void renderTick(TickEvent.RenderTickEvent event) {
        if (!Minecraft.getInstance().isPaused()
                && event.phase == TickEvent.Phase.START) {
            renderFrame = event.renderTickTime;
        }
    }
}