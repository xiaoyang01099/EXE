package net.xiaoyang010.ex_enigmaticlegacy.Config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigHandler {

    // 原有静态字段，从配置中同步
    public static List<String> lockWorldNameNebulaRod = new ArrayList<>();
    public static int nebulaWandCooldownTick = 20;
    public static int nebulaRodManaCost = 100;
    public static int limitXZCoords = 30000;
    public static int maxDictariusCount = 64;
    public static int sprawlRodMaxArea = 64;
    public static double sprawlRodSpeed = 1.5;

    // AstralKillop 配置静态字段
    public static int astralKillopManaCost = 100;
    public static int astralKillopNuggetDay = 14;
    public static int astralKillopEffectDay = 25;
    public static int astralKillopRange = 5;
    public static int astralKillopEffectDropCount = 4;
    public static int astralKillopEffectDuration = 12000;
    public static int astralKillopEffectLevel = 29;
    public static int astralKillopMaxMana = 10000;

    public static boolean enableDragonArmorOverlay = true; //龙甲头盔覆盖层开关

    // ForgeConfigSpec 构建器
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec.DoubleValue sprawlRodSpeedConfig;
    public static ForgeConfigSpec.IntValue sprawlRodMaxAreaConfig;
    public static ForgeConfigSpec.IntValue MANA_COST_PER_DAMAGE;
    public static ForgeConfigSpec.IntValue TIMELESS_IVY_EXP_COST;
    public static ForgeConfigSpec.BooleanValue TRY_REPAIR_TO_FULL;
    public static ForgeConfigSpec.BooleanValue ONLY_REPAIR_EQUIPMENTS;
    public static ForgeConfigSpec.IntValue REPAIR_TICK;

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec COMMON_CONFIG;

    // 客户端配置
    public static ForgeConfigSpec.BooleanValue useManaChargerAnimation;
    public static ForgeConfigSpec.BooleanValue enableDragonArmorOverlayConfig;

    // 通用配置 (服务端和客户端都需要)
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> lockWorldNameNebulaRodConfig;
    public static ForgeConfigSpec.IntValue nebulaWandCooldownTickConfig;
    public static ForgeConfigSpec.IntValue nebulaRodManaCostConfig;
    public static ForgeConfigSpec.IntValue limitXZCoordsConfig;
    public static ForgeConfigSpec.IntValue maxDictariusCountConfig;

    // AstralKillop 配置
    public static ForgeConfigSpec.IntValue astralKillopManaCostConfig;
    public static ForgeConfigSpec.IntValue astralKillopNuggetDayConfig;
    public static ForgeConfigSpec.IntValue astralKillopEffectDayConfig;
    public static ForgeConfigSpec.IntValue astralKillopRangeConfig;
    public static ForgeConfigSpec.IntValue astralKillopEffectDropCountConfig;
    public static ForgeConfigSpec.IntValue astralKillopEffectDurationConfig;
    public static ForgeConfigSpec.IntValue astralKillopEffectLevelConfig;
    public static ForgeConfigSpec.IntValue astralKillopMaxManaConfig;

    static {
        // === 客户端配置 ===
        CLIENT_BUILDER.comment("Client Settings");
        CLIENT_BUILDER.push("client");

        COMMON_BUILDER.comment("Timeless Ivy settings").push("common");

        sprawlRodSpeedConfig = COMMON_BUILDER
                .comment("Speed multiplier for Sprawl Rod projectiles",
                        "Higher values make the seed projectile fly faster")
                .defineInRange("sprawlSpeed", 1.5, 0.1, 5.0);

        sprawlRodMaxAreaConfig = COMMON_BUILDER
                .comment("Changes the area of effect of a projectile created with Rod of Sprawl")
                .defineInRange("sprawlMaxArea", 64, 1, 256);

        MANA_COST_PER_DAMAGE = COMMON_BUILDER
                .comment("Set the Mana cost per point of durability.(default: 200)")
                .defineInRange("mana_cost_per_damage", 200, 0, Integer.MAX_VALUE);

        TIMELESS_IVY_EXP_COST = COMMON_BUILDER
                .comment("Set the experience cost for attaching Timeless Ivy to an item.(default: 10)")
                .defineInRange("timeless_ivy_exp_cost", 10, 0, Integer.MAX_VALUE);

        TRY_REPAIR_TO_FULL = COMMON_BUILDER
                .comment("Set whether to attempt full durability repair each time.(default: true)")
                .define("try_repair_to_full", true);

        ONLY_REPAIR_EQUIPMENTS = COMMON_BUILDER
                .comment("Set whether to only attempt to repair equipped items. (Default: false)")
                .define("only_repair_equipments", false);

        REPAIR_TICK = COMMON_BUILDER
                .comment("Set the number of ticks between repair attempts. (Default: 1)")
                .defineInRange("repair_tick", 1, 1, Integer.MAX_VALUE);

        COMMON_BUILDER.pop();
        COMMON_CONFIG = COMMON_BUILDER.build();

        useManaChargerAnimation = CLIENT_BUILDER
                .comment("Activating the charging animation for the Mana Charger")
                .define("manaChargerLighting", true);

        // 龙甲头盔覆盖层配置
        enableDragonArmorOverlayConfig = CLIENT_BUILDER
                .comment("Enable/disable the Dragon Crystal Armor helmet overlay effect",
                        "Set to false to disable the visual overlay when wearing full dragon armor set")
                .define("enableDragonArmorOverlay", true);

        CLIENT_BUILDER.pop(); // client
        CLIENT_SPEC = CLIENT_BUILDER.build();

        // === 通用配置 ===
        COMMON_BUILDER.comment("Common Settings");
        COMMON_BUILDER.push("common");

        // Nebula Rod 配置部分
        COMMON_BUILDER.comment("Nebula Rod Settings").push("nebulaRod");

        lockWorldNameNebulaRodConfig = COMMON_BUILDER
                .comment("List of world names where Nebula Rod is disabled",
                        "World names are dimension identifiers like 'minecraft:overworld', 'minecraft:the_nether', etc.")
                .defineList("lockWorldNames",
                        Arrays.asList(), // 默认空列表
                        obj -> obj instanceof String);

        nebulaWandCooldownTickConfig = COMMON_BUILDER
                .comment("Cooldown in ticks between mana consumption (20 ticks = 1 second)")
                .defineInRange("cooldownTicks", 20, 1, 200);

        nebulaRodManaCostConfig = COMMON_BUILDER
                .comment("Mana cost per tick when using Nebula Rod")
                .defineInRange("manaCost", 100, 1, 10000);

        limitXZCoordsConfig = COMMON_BUILDER
                .comment("Maximum X/Z coordinate limit for teleportation")
                .defineInRange("coordinateLimit", 30000, 1000, 30000000);

        COMMON_BUILDER.pop(); // nebulaRod

        // Flower 配置部分
        COMMON_BUILDER.comment("Dictarius Settings").push("flower");

        maxDictariusCountConfig = COMMON_BUILDER
                .comment("Maximum number of Dictarius flowers allowed near each other",
                        "When this limit is exceeded, the flower will be destroyed")
                .defineInRange("maxDictariusCount", 64, 1, 256);

        // AstralKillop 花朵配置
        COMMON_BUILDER.comment("AstralKillop Flower Settings").push("astralKillop");

        astralKillopManaCostConfig = COMMON_BUILDER
                .comment("Mana cost per day for AstralKillop flower")
                .defineInRange("manaCost", 100, 1, 1000);

        astralKillopNuggetDayConfig = COMMON_BUILDER
                .comment("Day number when AstralKillop drops Astral Nugget instead of Pile")
                .defineInRange("nuggetDay", 14, 1, 100);

        astralKillopEffectDayConfig = COMMON_BUILDER
                .comment("Day number when AstralKillop gives special effect and resets counter")
                .defineInRange("effectDay", 25, 1, 100);

        astralKillopRangeConfig = COMMON_BUILDER
                .comment("Effect range for AstralKillop flower (blocks)")
                .defineInRange("range", 5, 1, 32);

        astralKillopEffectDropCountConfig = COMMON_BUILDER
                .comment("Number of items dropped on effect day")
                .defineInRange("effectDropCount", 4, 1, 64);

        astralKillopEffectDurationConfig = COMMON_BUILDER
                .comment("Duration of resistance effect in ticks (20 ticks = 1 second)")
                .defineInRange("effectDuration", 12000, 200, 72000); // 10秒到1小时

        astralKillopEffectLevelConfig = COMMON_BUILDER
                .comment("Level of resistance effect (0-29, where 29 = level 30)")
                .defineInRange("effectLevel", 29, 0, 29);

        astralKillopMaxManaConfig = COMMON_BUILDER
                .comment("Maximum mana capacity for AstralKillop flower")
                .defineInRange("maxMana", 10000, 1000, 100000);

        COMMON_BUILDER.pop(); // astralKillop
        COMMON_BUILDER.pop(); // flower

        COMMON_BUILDER.comment("Board of Fate Configuration")
                .push("board_of_fate");

        COMMON_BUILDER.comment("Relic Configuration")
                .push("relics");

        COMMON_BUILDER.pop(); // common
        COMMON_SPEC = COMMON_BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }

    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        if (configEvent.getConfig().getSpec() == COMMON_SPEC) {
            syncCommonConfig();
        } else if (configEvent.getConfig().getSpec() == CLIENT_SPEC) {
            syncClientConfig();
        }
    }


    public static void onReload(final ModConfigEvent.Reloading configEvent) {
        if (configEvent.getConfig().getSpec() == COMMON_SPEC) {
            syncCommonConfig();
        } else if (configEvent.getConfig().getSpec() == CLIENT_SPEC) {
            syncClientConfig();
        }
    }

    public static void syncCommonConfig() {
        lockWorldNameNebulaRod = new ArrayList<>(lockWorldNameNebulaRodConfig.get());
        nebulaWandCooldownTick = nebulaWandCooldownTickConfig.get();
        nebulaRodManaCost = nebulaRodManaCostConfig.get();
        limitXZCoords = limitXZCoordsConfig.get();
        maxDictariusCount = maxDictariusCountConfig.get();
        sprawlRodMaxArea = sprawlRodMaxAreaConfig.get();
        sprawlRodSpeed = sprawlRodSpeedConfig.get();

        // 同步 AstralKillop 配置
        astralKillopManaCost = astralKillopManaCostConfig.get();
        astralKillopNuggetDay = astralKillopNuggetDayConfig.get();
        astralKillopEffectDay = astralKillopEffectDayConfig.get();
        astralKillopRange = astralKillopRangeConfig.get();
        astralKillopEffectDropCount = astralKillopEffectDropCountConfig.get();
        astralKillopEffectDuration = astralKillopEffectDurationConfig.get();
        astralKillopEffectLevel = astralKillopEffectLevelConfig.get();
        astralKillopMaxMana = astralKillopMaxManaConfig.get();
    }

    public static void syncClientConfig() {
        enableDragonArmorOverlay = enableDragonArmorOverlayConfig.get();
    }

    public static class NebulaRodConfig {
        public static List<String> getLockWorldNames() {
            return lockWorldNameNebulaRod;
        }

        public static double getSprawlRodSpeed() {
            return sprawlRodSpeed;
        }

        public static int getSprawlRodMaxArea() {
            return sprawlRodMaxArea;
        }

        public static int getCooldownTicks() {
            return nebulaWandCooldownTick;
        }

        public static int getManaCost() {
            return nebulaRodManaCost;
        }

        public static int getCoordinateLimit() {
            return limitXZCoords;
        }

        public static boolean isWorldLocked(String worldName) {
            return lockWorldNameNebulaRod.contains(worldName);
        }
    }

    public static class FlowerConfig {
        public static int getMaxDictariusCount() {
            return maxDictariusCount;
        }

        // AstralKillop 配置获取方法
        public static int getAstralKillopManaCost() {
            return astralKillopManaCost;
        }

        public static int getAstralKillopNuggetDay() {
            return astralKillopNuggetDay;
        }

        public static int getAstralKillopEffectDay() {
            return astralKillopEffectDay;
        }

        public static int getAstralKillopRange() {
            return astralKillopRange;
        }

        public static int getAstralKillopEffectDropCount() {
            return astralKillopEffectDropCount;
        }

        public static int getAstralKillopEffectDuration() {
            return astralKillopEffectDuration;
        }

        public static int getAstralKillopEffectLevel() {
            return astralKillopEffectLevel;
        }

        public static int getAstralKillopMaxMana() {
            return astralKillopMaxMana;
        }
    }

    public static class ClientConfig {
        public static boolean isDragonArmorOverlayEnabled() {
            return enableDragonArmorOverlay;
        }
    }
}