package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Model;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.RainbowAvaritiaShaders;

import java.io.IOException;
import java.util.function.Consumer;

public class SpecialCoreShaders {
    private static ShaderInstance rainbowManaWater;
    private static ShaderInstance polychromeCollapsePrismOverlay;
    public static ShaderInstance COSMIC_BACKGROUND;
    public static ShaderInstance evilWater;
    private static ShaderInstance starrySkyShader;
    private static ShaderInstance blackhole;
    private static ShaderInstance andromeda;
    private static ShaderInstance starLine;
    public static ShaderInstance warpWorld;
    public static ShaderInstance warpShader;
    public static ShaderInstance blade;


    public static void init(ResourceProvider resourceProvider,
                            Consumer<Pair<ShaderInstance, Consumer<ShaderInstance>>> registerShader) throws IOException {

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "blade",
                        DefaultVertexFormat.POSITION_TEX),
                inst -> blade = inst)
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

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "warp_world",
                        DefaultVertexFormat.BLOCK),
                shader -> warpWorld = shader)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "warp",
                        DefaultVertexFormat.BLOCK),
                shader -> warpShader = shader)
        );
    }

    public static ShaderInstance getBladeShader() {
        return blade;
    }

    public static ShaderInstance getWarpShader() {
        return warpShader;
    }

    public static ShaderInstance getWarpWorldShader() {
        return warpWorld;
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