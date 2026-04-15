package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;

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
    private static ShaderInstance katana;
    private static ShaderInstance prisma;
    private static ShaderInstance starLine;

    public static void init(ResourceProvider resourceProvider,
                            Consumer<Pair<ShaderInstance, Consumer<ShaderInstance>>> registerShader) throws IOException {

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "blackhole", DefaultVertexFormat.POSITION_TEX),
                inst -> blackhole = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "starry_sky", DefaultVertexFormat.POSITION_TEX),
                inst -> starrySkyShader = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "rainbow_mana__water", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                inst -> rainbowManaWater = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "polychrome__collapse_prism", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                inst -> polychromeCollapsePrismOverlay = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "cosmic_background", DefaultVertexFormat.POSITION_COLOR_TEX),
                inst -> COSMIC_BACKGROUND = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "evil_water", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                inst -> evilWater = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "andromeda", DefaultVertexFormat.POSITION_TEX),
                inst -> andromeda = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "katana",
                        DefaultVertexFormat.POSITION_TEX),
                inst -> katana = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "prisma",
                        DefaultVertexFormat.POSITION_TEX),
                inst -> prisma = inst)
        );

        registerShader.accept(Pair.of(
                new ShaderInstance(resourceProvider, "star_line",
                        DefaultVertexFormat.POSITION_TEX),
                inst -> starLine = inst)
        );
    }

    public static ShaderInstance getKatanaShader() {
        return katana;
    }

    public static ShaderInstance getPrismaShader(){
        return prisma;
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
}