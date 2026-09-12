package org.xiaoyang.ex_enigmaticlegacy.Client.help;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.cosmic.RainbowAvaritiaShaders;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.*;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.slash.*;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.bladeflash.BladeFlashRenderer;

import java.io.IOException;
import java.util.function.Consumer;

public class EXECoreShaders {
    private static ShaderInstance rainbowManaWater;
    private static ShaderInstance polychromeCollapsePrismOverlay;
    public static ShaderInstance COSMIC_BACKGROUND;
    public static ShaderInstance evilWater;
    private static ShaderInstance starrySkyShader;
    private static ShaderInstance blackhole;
    private static ShaderInstance andromeda;
    private static ShaderInstance starLine;
    private static ShaderInstance slashShader;
    private static ShaderInstance bladeShader;
    public static ShaderInstance boltShader;
    public static ShaderInstance edgemask;
    public static ShaderInstance edgecomposite;
    public static ShaderInstance coffin;
    private static final ResourceLocation TRUE_DEMON_PARTICLE_SHADER = Exe.path("true_demon_glow_particle");
    private static final ResourceLocation TRUE_DEMON_PARTICLE_WHITE_SHADER = Exe.path("true_demon_glow_particle_white");
    private static final ResourceLocation TRUE_DEMON_PARTICLE_MAGENTA_SHADER = Exe.path("true_demon_glow_particle_magenta");
    private static final ResourceLocation TRUE_DEMON_STAR_PARTICLE_SHADER = Exe.path("true_demon_star_particle");
    private static final ResourceLocation SCREEN_GLASS_SHARD_SHADER = Exe.path("screen_glass_shard");
    private static final ResourceLocation DIMENSIONAL_SLASH_BLUR_SHADER = Exe.path("slash_blur");
    private static final ResourceLocation DIMENSIONAL_SLASH_COMPOSITE_SHADER = Exe.path("slash_composite");
    private static final ResourceLocation DIMENSIONAL_SLASH_DIRECTIONAL_UV_SHADER = Exe.path("slash_distortion");
    private static final ResourceLocation DIMENSIONAL_SLASH_FREEZE_SHADER = Exe.path("slash_freeze");
    private static final ResourceLocation DIMENSIONAL_SLASH_SCREEN_FX_SHADER = Exe.path("slash_screen");
    private static final ResourceLocation DIMENSIONAL_SLASH_BREAK_IMPACT_SHADER = Exe.path("slash_impact");
    private static final ResourceLocation DIMENSIONAL_SLASH_TOP_CHROMA_SHADER = Exe.path("slash_chroma");
    private static final ResourceLocation DIMENSIONAL_SLASH_WORLD_SHARD_SHADER = Exe.path("slash_shard");
    private static ShaderInstance trueDemonParticleShader;
    private static ShaderInstance trueDemonParticleWhiteShader;
    private static ShaderInstance trueDemonParticleMagentaShader;
    private static ShaderInstance trueDemonStarParticleShader;

    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        registerShader(event, TRUE_DEMON_PARTICLE_SHADER, DefaultVertexFormat.PARTICLE, shader -> trueDemonParticleShader = shader);
        registerShader(event, TRUE_DEMON_PARTICLE_WHITE_SHADER, DefaultVertexFormat.PARTICLE, shader -> trueDemonParticleWhiteShader = shader);
        registerShader(event, TRUE_DEMON_PARTICLE_MAGENTA_SHADER, DefaultVertexFormat.PARTICLE, shader -> trueDemonParticleMagentaShader = shader);
        registerShader(event, TRUE_DEMON_STAR_PARTICLE_SHADER, DefaultVertexFormat.PARTICLE, shader -> trueDemonStarParticleShader = shader);
        registerShader(event, SCREEN_GLASS_SHARD_SHADER, DefaultVertexFormat.POSITION_COLOR_TEX, ScreenEffects::shaderLoaded);
        registerShader(event, DIMENSIONAL_SLASH_BLUR_SHADER, DefaultVertexFormat.POSITION_TEX, BloomRenderer::blurShaderLoaded);
        registerShader(event, DIMENSIONAL_SLASH_COMPOSITE_SHADER, DefaultVertexFormat.POSITION_TEX, BloomRenderer::compositeShaderLoaded);
        registerShader(event, DIMENSIONAL_SLASH_DIRECTIONAL_UV_SHADER, DefaultVertexFormat.POSITION_TEX, DistortionRenderer::shaderLoaded);
        registerShader(event, DIMENSIONAL_SLASH_FREEZE_SHADER, DefaultVertexFormat.POSITION_TEX, FreezeRenderer::shaderLoaded);
        registerShader(event, DIMENSIONAL_SLASH_SCREEN_FX_SHADER, DefaultVertexFormat.POSITION_TEX, ScreenEffects::screenFxShaderLoaded);
        registerShader(event, DIMENSIONAL_SLASH_BREAK_IMPACT_SHADER, DefaultVertexFormat.POSITION_TEX, ScreenEffects::breakImpactShaderLoaded);
        registerShader(event, DIMENSIONAL_SLASH_TOP_CHROMA_SHADER, DefaultVertexFormat.POSITION_TEX, ChromaRenderer::shaderLoaded);
        registerShader(event, DIMENSIONAL_SLASH_WORLD_SHARD_SHADER, DefaultVertexFormat.NEW_ENTITY, WorldShardRenderer::shaderLoaded);
        event.registerShader(TrailRibbonShader.reloadShaders(event.getResourceProvider()), TrailRibbonShader::onLoad);
        event.registerShader(BeamShader.reloadShaders(event.getResourceProvider()), BeamShader::onLoad);
        event.registerShader(LightningShader.reloadShaders(event.getResourceProvider()), LightningShader::onLoad);
        event.registerShader(ShockwaveShader.reloadShaders(event.getResourceProvider()), ShockwaveShader::onLoad);
        event.registerShader(CircleShockwaveShader.reloadShaders(event.getResourceProvider()), CircleShockwaveShader::onLoad);
        event.registerShader(StarJudgementCircleShader.reloadShaders(event.getResourceProvider()), StarJudgementCircleShader::onLoad);
        event.registerShader(SwordAuraShader.reloadShaders(event.getResourceProvider()), SwordAuraShader::onLoad);
        event.registerShader(DimensionSlashStrikeShader.reloadShaders(event.getResourceProvider()), DimensionSlashStrikeShader::onLoad);
        event.registerShader(BattoSlashShader.reloadShaders(event.getResourceProvider()), BattoSlashShader::onLoad);
        event.registerShader(MiaoOutlineDepthMaskShader.reloadShaders(event.getResourceProvider()), MiaoOutlineDepthMaskShader::onLoad);
        event.registerShader(TridentPlusGlowShader.reloadShaders(event.getResourceProvider()), TridentPlusGlowShader::onLoad);
        event.registerShader(FlySwordHeldShader.reloadShaders(event.getResourceProvider()), FlySwordHeldShader::onLoad);
        event.registerShader(SmokeParticleShader.reloadShaders(event.getResourceProvider()), SmokeParticleShader::onLoad);
        event.registerShader(GoldenSpiralShader.reloadShaders(event.getResourceProvider()), GoldenSpiralShader::onLoad);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation(Exe.MODID, "coffin"), DefaultVertexFormat.POSITION_COLOR_TEX), shader -> coffin = shader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation(Exe.MODID, "edgemask"), DefaultVertexFormat.POSITION_COLOR_TEX), shader -> edgemask = shader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation(Exe.MODID, "edgecomposite"), DefaultVertexFormat.POSITION_TEX), shader -> {edgecomposite = shader;});
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation(Exe.MODID, "bolt"), DefaultVertexFormat.POSITION_COLOR_TEX), shader -> boltShader = shader);
        BladeFlashRenderer.INSTANCE.reloaded();
        BladeFlashRenderer.FIRST_PERSON.reloaded();
    }

    private static void registerShader(RegisterShadersEvent event, ResourceLocation id, VertexFormat format, Consumer<ShaderInstance> onLoaded) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), id, format), onLoaded);

    }

    public static void init(ResourceProvider resourceProvider, Consumer<Pair<ShaderInstance, Consumer<ShaderInstance>>> registerShader) throws IOException {
        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "blade",
                        DefaultVertexFormat.POSITION_TEX),
                inst -> bladeShader = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "slash",
                        DefaultVertexFormat.POSITION_TEX),
                inst -> slashShader = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "blackhole",
                        DefaultVertexFormat.POSITION_TEX),
                inst -> blackhole = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "starry_sky",
                        DefaultVertexFormat.POSITION_TEX),
                inst -> starrySkyShader = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "rainbow_mana__water",
                        DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                inst -> rainbowManaWater = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "polychrome__collapse_prism",
                        DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                inst -> polychromeCollapsePrismOverlay = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "cosmic_background",
                        DefaultVertexFormat.POSITION_COLOR_TEX),
                inst -> COSMIC_BACKGROUND = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "evil_water",
                        DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                inst -> evilWater = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "andromeda",
                        DefaultVertexFormat.POSITION_TEX),
                inst -> andromeda = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "star_line",
                        DefaultVertexFormat.POSITION_TEX),
                inst -> starLine = inst)
        );
    }

    public static ShaderInstance getTrueDemonParticleShader() {
        return trueDemonParticleShader;
    }

    public static ShaderInstance getTrueDemonParticleWhiteShader() {
        return trueDemonParticleWhiteShader;
    }

    public static ShaderInstance getTrueDemonParticleMagentaShader() {
        return trueDemonParticleMagentaShader;
    }

    public static ShaderInstance getTrueDemonStarParticleShader() {
        return trueDemonStarParticleShader;
    }

    public static ShaderInstance getSlashShader() {
        return slashShader;
    }

    public static ShaderInstance getBladeShader() {
        return bladeShader;
    }

    public static ShaderInstance getStarLineShader(){
        return starLine;
    }

    public static ShaderInstance getBlackHoleShader() {
        return blackhole;
    }

    public static ShaderInstance getStarrySkyShader() {
        return starrySkyShader;
    }

    public static ShaderInstance evilWater() {
        return evilWater;
    }

    public static ShaderInstance rainbowManaWater() {
        return rainbowManaWater;
    }

    public static ShaderInstance getAndromedaShader() {
        return andromeda;
    }

    public static ShaderInstance polychromeCollapsePrismOverlay() {
        return polychromeCollapsePrismOverlay;
    }

    public static ShaderInstance cosmicBackground() {
        return COSMIC_BACKGROUND;
    }

    public static boolean setCosmicShader(float scale, float alpha, int cosmicType, Vector4f color, boolean isGUI) {
        Minecraft mc = Minecraft.getInstance();
        float yaw = 0.0f;
        float pitch = 0.0f;
        if (mc.player != null) {
            yaw = (float)((double)(mc.player.getYRot() * 2.0f) * Math.PI / 360.0);
            pitch = -((float)((double)(mc.player.getXRot() * 2.0f) * Math.PI / 360.0));
        }
        RainbowAvaritiaShaders.cosmicTime.set((float)(System.currentTimeMillis() - (long)RainbowAvaritiaShaders.renderTime) / 2000.0f);
        if (!isGUI) {
            RainbowAvaritiaShaders.cosmicYaw.set(yaw);
            RainbowAvaritiaShaders.cosmicPitch.set(pitch);
        } else {
            RainbowAvaritiaShaders.cosmicYaw.set(180.0f);
            RainbowAvaritiaShaders.cosmicPitch.set(0.0f);
        }
        RainbowAvaritiaShaders.cosmicExternalScale.set(scale);
        RainbowAvaritiaShaders.cosmicOpacity.set(alpha);
//        RainbowAvaritiaShaders.cosmicUseType.set(cosmicType);
//        if (color == null) {
//            RainbowAvaritiaShaders.cosmicColor.set(new Vector4f(0.0f, 0.02f, 0.03f, 1.0f));
//        } else {
//            RainbowAvaritiaShaders.cosmicColor.set(color);
//        }
        for (int i = 0; i < 10; ++i) {
            TextureAtlasSprite sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(Exe.path("item/misc/cosmic_" + i));
            RainbowAvaritiaShaders.COSMIC_UVS[i * 4] = sprite.getU0();
            RainbowAvaritiaShaders.COSMIC_UVS[i * 4 + 1] = sprite.getV0();
            RainbowAvaritiaShaders.COSMIC_UVS[i * 4 + 2] = sprite.getU1();
            RainbowAvaritiaShaders.COSMIC_UVS[i * 4 + 3] = sprite.getV1();
        }
        RainbowAvaritiaShaders.cosmicUVs.set(RainbowAvaritiaShaders.COSMIC_UVS);
        return true;
    }

    public static boolean setCosmicShader(float scale, float alpha, int cosmicType, Vector4f color) {
        setCosmicShader(scale, alpha, cosmicType, color, false);
        return true;
    }

    public static Vector2f getScreenSize() {
        Minecraft mc = Minecraft.getInstance();
        try {
            Window wh = mc.getWindow();
            int width = wh.getWidth();
            int height = wh.getHeight();
            if (width > 0 && height > 0) {
                return new Vector2f((float) width, height);
            }
        } catch (Exception e) {
            System.err.println("Failed to get screen size from Minecraft window: " + e.getMessage());
        }
        try {
            GLFWVidMode vidMode;
            long monitor = GLFW.glfwGetPrimaryMonitor();
            if (monitor != 0L && (vidMode = GLFW.glfwGetVideoMode(monitor)) != null && vidMode.width() > 0 && vidMode.height() > 0) {
                return new Vector2f((float) vidMode.width(), vidMode.height());
            }
        } catch (Exception e) {
            System.err.println("Failed to get screen size from monitor: " + e.getMessage());
        }
        System.err.println("All methods to get screen size failed, using default: 1920x1080");
        return new Vector2f(1920.0f, 1080.0f);
    }
}