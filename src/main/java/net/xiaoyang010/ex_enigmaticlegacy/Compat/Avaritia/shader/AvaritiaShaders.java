package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader;

import codechicken.lib.render.shader.CCShaderInstance;
import codechicken.lib.texture.SpriteRegistryHelper;
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
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class AvaritiaShaders {
    public static boolean inventoryRender = false;
    static int renderTime;
    static float renderFrame;

    public static TextureAtlasSprite[] MASK_SPRITES = new TextureAtlasSprite[1];
    public static TextureAtlasSprite MASK;
    public static SpriteRegistryHelper MASK_HELPER; // 不再静态初始化

    public static TextureAtlasSprite[] MASK_SPRITES_INV = new TextureAtlasSprite[1];
    public static TextureAtlasSprite MASK_INV;
    public static SpriteRegistryHelper MASK_HELPER_INV; // 不再静态初始化

    public static TextureAtlasSprite[] WING_SPRITES = new TextureAtlasSprite[1];
    public static TextureAtlasSprite WING;
    public static SpriteRegistryHelper WING_HELPER; // 不再静态初始化

    public static SpriteRegistryHelper COSMIC_HELPER; // 不再静态初始化
    public static TextureAtlasSprite[] COSMIC_SPRITES = new TextureAtlasSprite[10];
    public static float[] COSMIC_UVS = new float[40];

    public static CCShaderInstance cosmicShader;
    public static CCShaderInstance cosmicShader2;

    public static Uniform cosmicTime;
    public static Uniform cosmicYaw;
    public static Uniform cosmicPitch;
    public static Uniform cosmicExternalScale;
    public static Uniform cosmicOpacity;
    public static Uniform cosmicUVs;

    public static Uniform cosmicTime2;
    public static Uniform cosmicYaw2;
    public static Uniform cosmicPitch2;
    public static Uniform cosmicExternalScale2;
    public static Uniform cosmicOpacity2;
    public static Uniform cosmicUVs2;

    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }

        try {
            MASK_HELPER = new SpriteRegistryHelper();
            MASK_HELPER_INV = new SpriteRegistryHelper();
            WING_HELPER = new SpriteRegistryHelper();
            COSMIC_HELPER = new SpriteRegistryHelper();

            IEventBus eventbus = FMLJavaModLoadingContext.get().getModEventBus();
            eventbus.addListener(AvaritiaShaders::onRegisterShaders);
            MinecraftForge.EVENT_BUS.addListener(AvaritiaShaders::onRenderTick);
            MinecraftForge.EVENT_BUS.addListener(AvaritiaShaders::clientTick);
            MinecraftForge.EVENT_BUS.addListener(AvaritiaShaders::renderTick);

            TextureAtlasSprite[] s = COSMIC_SPRITES;
            COSMIC_HELPER.addIIconRegister(registrar -> {
                for (int i = 0; i < 10; i++) {
                    int finalI = i;
                    registrar.registerSprite(shader("cosmic_" + finalI), e -> COSMIC_SPRITES[finalI] = e);
                }
            });

            MASK_HELPER.addIIconRegister(registrar -> registrar.registerSprite(mask("mask"), e -> MASK_SPRITES[0] = e));
            MASK_HELPER_INV.addIIconRegister(registrar -> registrar.registerSprite(mask("mask_inv"), e -> MASK_SPRITES_INV[0] = e));
            WING_HELPER.addIIconRegister(registrar -> registrar.registerSprite(mask("mask_wings"), e -> WING_SPRITES[0] = e));

            MASK = MASK_SPRITES[0];
            MASK_INV = MASK_SPRITES_INV[0];
            WING = WING_SPRITES[0];

            initialized = true;

        } catch (Exception e) {
            ExEnigmaticlegacyMod.LOGGER.error("Failed to initialize AvaritiaShaders", e);
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static TextureAtlasSprite[] getMaskSprites() {
        if (!initialized) {
            return new TextureAtlasSprite[1];
        }
        return MASK_SPRITES;
    }

    public static TextureAtlasSprite[] getMaskSpritesInv() {
        if (!initialized) {
            return new TextureAtlasSprite[1];
        }
        return MASK_SPRITES_INV;
    }

    public static TextureAtlasSprite[] getWingSprites() {
        if (!initialized) {
            return new TextureAtlasSprite[1];
        }
        return WING_SPRITES;
    }

    static ResourceLocation shader(String path) {
        return new ResourceLocation(ExEnigmaticlegacyMod.MODID, "shader/" + path);
    }

    static ResourceLocation mask(String path) {
        return new ResourceLocation(ExEnigmaticlegacyMod.MODID, "models/infinity_armor_" + path);
    }

    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!initialized) return;

        if (event.phase == TickEvent.Phase.START) {
            for (int i = 0; i < COSMIC_SPRITES.length; i++) {
                TextureAtlasSprite sprite = COSMIC_SPRITES[i];
                if (sprite != null) {
                    COSMIC_UVS[i * 4 + 0] = sprite.getU0();
                    COSMIC_UVS[i * 4 + 1] = sprite.getV0();
                    COSMIC_UVS[i * 4 + 2] = sprite.getU1();
                    COSMIC_UVS[i * 4 + 3] = sprite.getV1();
                }
            }
            if (cosmicUVs != null)
                cosmicUVs.set(COSMIC_UVS);
            if (cosmicUVs2 != null)
                cosmicUVs2.set(COSMIC_UVS);
        }
    }

    public static void onRegisterShaders(RegisterShadersEvent event) {
        ResourceManager resourceManager = event.getResourceManager();
        try {
            event.registerShader(CCShaderInstance.create(resourceManager, new ResourceLocation(ExEnigmaticlegacyMod.MODID, "cosmic"), DefaultVertexFormat.BLOCK), e -> {
                cosmicShader = (CCShaderInstance)e;
                cosmicTime = Objects.requireNonNull(cosmicShader.getUniform("time"));
                cosmicYaw = Objects.requireNonNull(cosmicShader.getUniform("yaw"));
                cosmicPitch = Objects.requireNonNull(cosmicShader.getUniform("pitch"));
                cosmicExternalScale = Objects.requireNonNull(cosmicShader.getUniform("externalScale"));
                cosmicOpacity = Objects.requireNonNull(cosmicShader.getUniform("opacity"));
                cosmicUVs = Objects.requireNonNull(cosmicShader.getUniform("cosmicuvs"));
                cosmicShader.onApply(() -> {
                });
            });
        } catch (Exception exception) {
            ExEnigmaticlegacyMod.LOGGER.error("cosmic shader error", exception);
        }
        try {
            event.registerShader(CCShaderInstance.create(resourceManager, new ResourceLocation(ExEnigmaticlegacyMod.MODID, "cosmic"), DefaultVertexFormat.NEW_ENTITY), e -> {
                cosmicShader2 = (CCShaderInstance) e;
                cosmicTime2 = Objects.requireNonNull(cosmicShader2.getUniform("time"));
                cosmicYaw2 = Objects.requireNonNull(cosmicShader2.getUniform("yaw"));
                cosmicPitch2 = Objects.requireNonNull(cosmicShader2.getUniform("pitch"));
                cosmicExternalScale2 = Objects.requireNonNull(cosmicShader2.getUniform("externalScale"));
                cosmicOpacity2 = Objects.requireNonNull(cosmicShader2.getUniform("opacity"));
                cosmicUVs2 = Objects.requireNonNull(cosmicShader2.getUniform("cosmicuvs"));
                cosmicShader2.onApply(() -> {
                });
            });
        } catch (Exception exception) {
            ExEnigmaticlegacyMod.LOGGER.error("cosmic shader2 error", exception);
        }
    }

    static void clientTick(TickEvent.ClientTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.END)
            renderTime++;
    }

    static void renderTick(TickEvent.RenderTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.START)
            renderFrame = event.renderTickTime;
    }
}