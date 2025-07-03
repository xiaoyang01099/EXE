package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.function.Consumer;

public class SpecialCoreShaders {

    private static ShaderInstance rainbowManaWater;

    public static void init(ResourceManager resourceManager,
                            Consumer<Pair<ShaderInstance, Consumer<ShaderInstance>>> registerShader) throws IOException {
        registerShader.accept(Pair.of(
                new ShaderInstance(resourceManager, "rainbow_mana__water", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                inst -> rainbowManaWater = inst)
        );
    }

    public static ShaderInstance rainbowManaWater() {
        return rainbowManaWater;
    }
}
