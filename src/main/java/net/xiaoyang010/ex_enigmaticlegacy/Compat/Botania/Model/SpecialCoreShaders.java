package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.function.Consumer;

public class SpecialCoreShaders {

    private static ShaderInstance rainbowManaWater;
    private static ShaderInstance polychromeCollapsePrismOverlay;
    public static ShaderInstance COSMIC_BACKGROUND;
    public static ShaderInstance evilWater;
    private static ShaderInstance starrySkyShader;
    private static ShaderInstance blackhole;


    public static void init(ResourceManager resourceManager,
                            Consumer<Pair<ShaderInstance, Consumer<ShaderInstance>>> registerShader) throws IOException {

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceManager, "blackhole", DefaultVertexFormat.POSITION_TEX),
                inst -> blackhole = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceManager, "starry_sky", DefaultVertexFormat.POSITION_TEX),
                inst -> starrySkyShader = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceManager, "rainbow_mana__water", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                inst -> rainbowManaWater = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceManager, "polychrome__collapse_prism", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                inst -> polychromeCollapsePrismOverlay = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceManager, "cosmic_background", DefaultVertexFormat.POSITION_COLOR_TEX),
                inst -> COSMIC_BACKGROUND = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceManager, "evil_water", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                inst -> evilWater = inst)
        );


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

    public static ShaderInstance polychromeCollapsePrismOverlay() {
        return polychromeCollapsePrismOverlay;
    }

    public static ShaderInstance cosmicBackground() {
        return COSMIC_BACKGROUND;
    }
}
