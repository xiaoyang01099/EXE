package net.xiaoyang010.ex_enigmaticlegacy.Client;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.ArrayList;
import java.util.List;

public class ConfigHandler {
    public static List<String> lockWorldNameNebulaRod = new ArrayList<>();
    public static int nebulaWandCooldownTick = 20;
    public static int nebulaRodManaCost = 100;
    public static int limitXZCoords = 32;
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.BooleanValue useManaChargerAnimation;

    static {
        BUILDER.comment("Client Settings");
        BUILDER.push("client");

        useManaChargerAnimation = BUILDER
                .comment("Activating the charging animation for the Mana Charger")
                .define("manaChargerLighting", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC);
    }

    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        // Config 会自动加载，无需额外处理
    }

    public static void onReload(final ModConfigEvent.Reloading configEvent) {
        // Config 会自动重新加载，无需额外处理
    }
}