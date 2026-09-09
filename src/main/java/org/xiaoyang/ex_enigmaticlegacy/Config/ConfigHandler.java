package org.xiaoyang.ex_enigmaticlegacy.Config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.xiaoyang.ex_enigmaticlegacy.Block.BlockPeacefulTable;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigHandler {
    public static List<Object> lockWorldNameNebulaRod = new ArrayList<>();
    public static int nebulaWandCooldownTick = 20;
    public static int nebulaRodManaCost = 100;
    public static int limitXZCoords = 30000;
    public static int maxDictariusCount = 64;
    public static int sprawlRodMaxArea = 64;
    public static int emcFlowerManaPerEMC = 300;
    public static int emcFlowerMaxMana = 10000;
    public static double sprawlRodSpeed = 1.5;

    public static boolean bladeFlashEnabled;
    public static boolean bladeFlashHeldSwordTrails;
    public static boolean bladeFlashDemoOnSwordSwing;
    public static double bladeFlashTrailSeconds;
    public static double bladeFlashDistortionPixels;
    public static double bladeFlashBrightness;
    public static int bladeFlashMaxTrails;
    public static int bladeFlashMaxSamplesPerTrail;

    public static int astralKillopManaCost = 100;
    public static int astralKillopNuggetDay = 14;
    public static int astralKillopEffectDay = 25;
    public static int astralKillopRange = 5;
    public static int astralKillopEffectDropCount = 4;
    public static int astralKillopEffectDuration = 12000;
    public static int astralKillopEffectLevel = 29;
    public static int astralKillopMaxMana = 10000;
    public static boolean enableDragonArmorOverlay = true;

    public static float starlitGuiScale = 0.7f;
    public static float starlitJeiScale = 0.7f;

    public static int spreaderMaxMana = 128000;
    public static int spreaderBurstMana = 32000;

    public static boolean peacefulTableInAllDifficulties = false;
    public static List<String> peacefulTableEntityBlacklist = new ArrayList<>();

    public static List<Object> lockEntityListToHorn = new ArrayList<>();

    public static boolean infinityPotatoDrawHalo = true;
    public static boolean itemNameplateEnabled = true;
    public static volatile boolean freezePlayers = true;

    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec COMMON_SPEC;

    public static ForgeConfigSpec.IntValue EXP_COST_PEARL;
    public static ForgeConfigSpec.IntValue EXP_COST_ECHEST;
    public static ForgeConfigSpec.IntValue EXP_COST_STORAGE_START;
    public static ForgeConfigSpec.IntValue EXP_COST_STORAGE_INC;

    public static ForgeConfigSpec.IntValue INVO_WIDTH;
    public static ForgeConfigSpec.IntValue INVO_HEIGHT;
    public static ForgeConfigSpec.IntValue MAX_SECTIONS;
    public static ForgeConfigSpec.BooleanValue IS_LARGE_SCREEN;
    public static ForgeConfigSpec.BooleanValue FREEZE_PLAYERS;

    @Mod.EventBusSubscriber(modid = Exe.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class FrostConfigEvents {
        @SubscribeEvent
        public static void changed(ModConfigEvent event) {
            if (event.getConfig().getSpec() == COMMON_SPEC && !(event instanceof ModConfigEvent.Unloading))
                freezePlayers = FREEZE_PLAYERS.get();
        }
    }

    public static ForgeConfigSpec.BooleanValue PERSIST_ON_DEATH;
    public static ForgeConfigSpec.BooleanValue SHOW_GUI_BUTTON;
    public static ForgeConfigSpec.BooleanValue REQUIRE_RING;
    public static ForgeConfigSpec.IntValue FILTER_RANGE;

    public static ForgeConfigSpec.BooleanValue ENABLED;
    public static ForgeConfigSpec.BooleanValue HELD_SWORD_TRAILS;
    public static ForgeConfigSpec.BooleanValue DEMO_ON_SWORD_SWING;
    public static ForgeConfigSpec.DoubleValue TRAIL_SECONDS;
    public static ForgeConfigSpec.DoubleValue DISTORTION_PIXELS;
    public static ForgeConfigSpec.DoubleValue BRIGHTNESS;
    public static ForgeConfigSpec.IntValue MAX_TRAILS;
    public static ForgeConfigSpec.IntValue MAX_SAMPLES;

    public static ForgeConfigSpec.IntValue emcFlowerManaPerEMCConfig;
    public static ForgeConfigSpec.IntValue emcFlowerMaxManaConfig;
    public static ForgeConfigSpec.DoubleValue sprawlRodSpeedConfig;
    public static ForgeConfigSpec.IntValue sprawlRodMaxAreaConfig;
    public static ForgeConfigSpec.IntValue MANA_COST_PER_DAMAGE;
    public static ForgeConfigSpec.IntValue TIMELESS_IVY_EXP_COST;
    public static ForgeConfigSpec.BooleanValue TRY_REPAIR_TO_FULL;
    public static ForgeConfigSpec.BooleanValue ONLY_REPAIR_EQUIPMENTS;
    public static ForgeConfigSpec.IntValue REPAIR_TICK;

    public static ForgeConfigSpec.BooleanValue enableDragonArmorOverlayConfig;
    public static ForgeConfigSpec.DoubleValue starlitGuiScaleConfig;
    public static ForgeConfigSpec.DoubleValue starlitJeiScaleConfig;

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> lockWorldNameNebulaRodConfig;
    public static ForgeConfigSpec.IntValue nebulaWandCooldownTickConfig;
    public static ForgeConfigSpec.IntValue nebulaRodManaCostConfig;
    public static ForgeConfigSpec.IntValue limitXZCoordsConfig;
    public static ForgeConfigSpec.IntValue maxDictariusCountConfig;

    public static ForgeConfigSpec.IntValue astralKillopManaCostConfig;
    public static ForgeConfigSpec.IntValue astralKillopNuggetDayConfig;
    public static ForgeConfigSpec.IntValue astralKillopEffectDayConfig;
    public static ForgeConfigSpec.IntValue astralKillopRangeConfig;
    public static ForgeConfigSpec.IntValue astralKillopEffectDropCountConfig;
    public static ForgeConfigSpec.IntValue astralKillopEffectDurationConfig;
    public static ForgeConfigSpec.IntValue astralKillopEffectLevelConfig;
    public static ForgeConfigSpec.IntValue astralKillopMaxManaConfig;

    public static ForgeConfigSpec.IntValue spreaderMaxManaConfig;
    public static ForgeConfigSpec.IntValue spreaderBurstManaConfig;

    public static ForgeConfigSpec.BooleanValue magicTableUseLongConfig;
    public static ForgeConfigSpec.BooleanValue peacefulTableInAllDifficultiesConfig;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> peacefulTableEntityBlacklistConfig;

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> lockEntityListToHornConfig;
    public static ForgeConfigSpec.BooleanValue itemNameplateEnabledConfig;
    public static ForgeConfigSpec.BooleanValue infinityPotatoDrawHaloConfig;
    private static volatile HashMap<String, Boolean> entityDamageWhitelistMap = new HashMap<>();

    static {
        CLIENT_BUILDER.comment("Client Settings").push("client");

        enableDragonArmorOverlayConfig = CLIENT_BUILDER
                .comment("Enable/disable the Dragon Crystal Armor helmet overlay effect")
                .define("enableDragonArmorOverlay", true);

        CLIENT_BUILDER.comment("Starlit Sanctum GUI Settings").push("starlit_gui");
        starlitGuiScaleConfig = CLIENT_BUILDER
                .comment(
                        "Scale factor for Starlit Sanctum GUI",
                        "Range: 0.1 to 1.0",
                        "Default: 0.7 (70% of original size)",
                        "1.0 = full size, 0.5 = half size"
                )
                .defineInRange("guiScale", 0.7, 0.1, 1.0);
        starlitJeiScaleConfig = CLIENT_BUILDER
                .comment(
                        "Scale factor for Starlit Sanctum JEI recipe display",
                        "Range: 0.1 to 1.0",
                        "Default: 0.7 (70% of original size)",
                        "Should match guiScale for consistency"
                )
                .defineInRange("jeiScale", 0.7, 0.1, 1.0);
        CLIENT_BUILDER.pop(); // starlit_gui

        CLIENT_BUILDER.comment("Item Nameplate Settings").push("item_nameplate");
        itemNameplateEnabledConfig = CLIENT_BUILDER
                .comment(
                        "Enable/disable the animated nameplate above rare item entities on the ground",
                        "Default: true"
                )
                .define("enabled", true);
        CLIENT_BUILDER.pop(); // item_nameplate

        CLIENT_BUILDER.comment("Infinity Potato Settings").push("infinity_potato");
        infinityPotatoDrawHaloConfig = CLIENT_BUILDER
                .comment(
                        "Enable/disable the halo effect rendered behind the Infinity Potato block",
                        "Default: true"
                )
                .define("drawHalo", true);
        CLIENT_BUILDER.pop(); // infinity_potato

        CLIENT_BUILDER.comment("Blade Flash Settings").push("blade_flash");
        ENABLED = CLIENT_BUILDER
                .comment("Enable Blade Flash rendering")
                .define("enabled", true);
        HELD_SWORD_TRAILS = CLIENT_BUILDER
                .comment("Continuously sample held items in ex_enigmaticlegacy:edgend, including walking and turning. The tag is empty by default.")
                .define("heldSwordTrails", true);
        DEMO_ON_SWORD_SWING = CLIENT_BUILDER
                .comment("Legacy synthetic arcs when heldSwordTrails is false. Both swings and V require an item in ex_enigmaticlegacy:edgend.")
                .define("demoOnSwordSwing", false);
        TRAIL_SECONDS = CLIENT_BUILDER
                .defineInRange("trailSeconds", 0.45, 0.05, 2.0);
        DISTORTION_PIXELS = CLIENT_BUILDER
                .defineInRange("distortionPixels", 20.0, 0.0, 80.0);
        BRIGHTNESS = CLIENT_BUILDER
                .defineInRange("brightness", 1.0, 0.0, 5.0);
        MAX_TRAILS = CLIENT_BUILDER
                .defineInRange("maxTrails", 64, 1, 256);
        MAX_SAMPLES = CLIENT_BUILDER
                .defineInRange("maxSamplesPerTrail", 128, 4, 256);
        CLIENT_BUILDER.pop(); // blade_flash


        CLIENT_BUILDER.pop(); // client
        CLIENT_SPEC = CLIENT_BUILDER.build();

        // ========== COMMON ==========
        COMMON_BUILDER.comment("Common Settings").push("common");
        COMMON_BUILDER.push("sunmakerFrost");
        FREEZE_PLAYERS = COMMON_BUILDER.comment("Allow the Sunmaker frost aura to freeze survival players. Disabling also releases frozen players.")
                .define("freezePlayers", true);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Peaceful Table Settings").push("peaceful_table");
        peacefulTableInAllDifficultiesConfig = COMMON_BUILDER
                .comment(
                        "Allow Peaceful Table to work in all difficulties",
                        "Default: false (only works in Peaceful difficulty)",
                        "Set to true to enable in all difficulties"
                )
                .define("peacefulTableInAllDifficulties", false);
        peacefulTableEntityBlacklistConfig = COMMON_BUILDER
                .comment(
                        "Entity blacklist for Peaceful Table - these entities will never be spawned",
                        "Enter full entity registry IDs",
                        "Format: [\"modid:entity_name\", \"modid:entity_name\"]",
                        "Example: [\"minecraft:wither\", \"botania:doppleganger\", \"alexsmobs:warped_mosco\"]"
                )
                .defineList(
                        "entityBlacklist",
                        Arrays.asList(
                                "botania:doppleganger"
                        ),
                        obj -> obj instanceof String s && !s.isEmpty()
                );
        COMMON_BUILDER.pop(); // peaceful_table

        COMMON_BUILDER.comment("Timeless Ivy settings").push("timeless_ivy");
        MANA_COST_PER_DAMAGE = COMMON_BUILDER
                .comment("Mana cost per point of durability")
                .defineInRange("mana_cost_per_damage", 200, 0, Integer.MAX_VALUE);
        TIMELESS_IVY_EXP_COST = COMMON_BUILDER
                .comment("Experience cost for attaching Timeless Ivy")
                .defineInRange("timeless_ivy_exp_cost", 10, 0, Integer.MAX_VALUE);
        TRY_REPAIR_TO_FULL = COMMON_BUILDER
                .comment("Attempt full durability repair each time")
                .define("try_repair_to_full", true);
        ONLY_REPAIR_EQUIPMENTS = COMMON_BUILDER
                .comment("Only repair equipped items")
                .define("only_repair_equipments", false);
        REPAIR_TICK = COMMON_BUILDER
                .comment("Ticks between repair attempts")
                .defineInRange("repair_tick", 1, 1, Integer.MAX_VALUE);
        COMMON_BUILDER.pop(); // timeless_ivy

        COMMON_BUILDER.comment("Sprawl Rod Settings").push("sprawl_rod");
        sprawlRodSpeedConfig = COMMON_BUILDER
                .comment("Speed multiplier for Sprawl Rod projectiles")
                .defineInRange("sprawlSpeed", 1.5, 0.1, 5.0);
        sprawlRodMaxAreaConfig = COMMON_BUILDER
                .comment("Area of effect for Sprawl Rod projectile")
                .defineInRange("sprawlMaxArea", 64, 1, 256);
        COMMON_BUILDER.pop(); // sprawl_rod

        COMMON_BUILDER.comment("Nebula Rod Settings").push("nebula_rod");
        lockWorldNameNebulaRodConfig = COMMON_BUILDER
                .comment("List of world names where Nebula Rod is disabled")
                .defineList("lockWorldNames", Arrays.asList(), obj -> obj instanceof String);
        nebulaWandCooldownTickConfig = COMMON_BUILDER
                .comment("Cooldown in ticks between mana consumption")
                .defineInRange("cooldownTicks", 20, 1, 200);
        nebulaRodManaCostConfig = COMMON_BUILDER
                .comment("Mana cost per tick")
                .defineInRange("manaCost", 100, 1, 10000);
        limitXZCoordsConfig = COMMON_BUILDER
                .comment("Maximum X/Z coordinate limit for teleportation")
                .defineInRange("coordinateLimit", 30000, 1000, 30000000);
        COMMON_BUILDER.pop(); // nebula_rod

        COMMON_BUILDER.comment("Advanced Botany Spreader Settings").push("ab_spreader");
        spreaderMaxManaConfig = COMMON_BUILDER
                .comment("Maximum amount of mana held in an Advanced Botany mana spreader")
                .defineInRange("spreaderMaxMana", 128000, 1000, 10000000);
        spreaderBurstManaConfig = COMMON_BUILDER
                .comment("Amount of mana in a mana burst from Advanced Botany spreader")
                .defineInRange("spreaderBurstMana", 32000, 100, 1000000);
        COMMON_BUILDER.pop(); // ab_spreader

        COMMON_BUILDER.comment("Horn of Plenty Settings").push("horn_plenty");
        lockEntityListToHornConfig = COMMON_BUILDER
                .comment(
                        "Block entities from being affected by Horn of Plenty's double drop effect",
                        "Enter the simple class name of the entity (e.g., 'Zombie', 'Creeper', 'EnderDragon')",
                        "Example: ['EnderDragon', 'WitherBoss']"
                )
                .defineList(
                        "lockEntityList",
                        Arrays.asList(),
                        obj -> obj instanceof String
                );
        COMMON_BUILDER.pop(); // horn_plenty

        COMMON_BUILDER.comment("Flower Settings").push("flowers");
        maxDictariusCountConfig = COMMON_BUILDER
                .comment("Maximum number of Dictarius flowers allowed near each other")
                .defineInRange("maxDictariusCount", 64, 1, 256);
        emcFlowerManaPerEMCConfig = COMMON_BUILDER
                .comment("Mana generated per EMC consumed by EMCFlower")
                .defineInRange("manaPerEMC", 10, 1, 1000);
        emcFlowerMaxManaConfig = COMMON_BUILDER
                .comment("Maximum mana capacity for EMCFlower")
                .defineInRange("maxMana", 1000, 100, 100000);

        COMMON_BUILDER.comment("AstralKillop Flower Settings").push("astral_killop");
        astralKillopManaCostConfig = COMMON_BUILDER
                .comment("Mana cost per day")
                .defineInRange("manaCost", 100, 1, 1000);
        astralKillopNuggetDayConfig = COMMON_BUILDER
                .comment("Day number for Astral Nugget drop")
                .defineInRange("nuggetDay", 14, 1, 100);
        astralKillopEffectDayConfig = COMMON_BUILDER
                .comment("Day number for special effect")
                .defineInRange("effectDay", 25, 1, 100);
        astralKillopRangeConfig = COMMON_BUILDER
                .comment("Effect range (blocks)")
                .defineInRange("range", 5, 1, 32);
        astralKillopEffectDropCountConfig = COMMON_BUILDER
                .comment("Number of items dropped on effect day")
                .defineInRange("effectDropCount", 4, 1, 64);
        astralKillopEffectDurationConfig = COMMON_BUILDER
                .comment("Duration of resistance effect (ticks)")
                .defineInRange("effectDuration", 12000, 200, 72000);
        astralKillopEffectLevelConfig = COMMON_BUILDER
                .comment("Level of resistance effect (0-29)")
                .defineInRange("effectLevel", 29, 0, 29);
        astralKillopMaxManaConfig = COMMON_BUILDER
                .comment("Maximum mana capacity")
                .defineInRange("maxMana", 10000, 1000, 100000);
        COMMON_BUILDER.pop(); // astral_killop
        COMMON_BUILDER.pop(); // flowers

        COMMON_BUILDER.comment("Power Inventory Settings").push("power_inventory");
        COMMON_BUILDER.comment("Experience Costs").push("experience");
        EXP_COST_PEARL = COMMON_BUILDER
                .comment("Experience levels to unlock ender pearl slot")
                .defineInRange("expCostPearl", 30, 0, 100);
        EXP_COST_ECHEST = COMMON_BUILDER
                .comment("Experience levels to unlock ender chest slot")
                .defineInRange("expCostEChest", 32, 0, 100);
        EXP_COST_STORAGE_START = COMMON_BUILDER
                .comment("Experience levels for first storage section")
                .defineInRange("expCostStorageStart", 2, 0, 100);
        EXP_COST_STORAGE_INC = COMMON_BUILDER
                .comment("Experience increment per additional storage section")
                .defineInRange("expCostStorageInc", 3, 0, 100);
        COMMON_BUILDER.pop(); // experience

        COMMON_BUILDER.comment("Display Settings").push("display");
        IS_LARGE_SCREEN = COMMON_BUILDER
                .comment("Use large screen layout (15 sections vs 6)")
                .define("isLargeScreen", true);
        INVO_WIDTH = COMMON_BUILDER
                .comment("Inventory GUI width")
                .defineInRange("invoWidth", 508, 342, 1000);
        INVO_HEIGHT = COMMON_BUILDER
                .comment("Inventory GUI height")
                .defineInRange("invoHeight", 346, 230, 1000);
        MAX_SECTIONS = COMMON_BUILDER
                .comment("Maximum storage sections")
                .defineInRange("maxSections", 15, 1, 15);
        SHOW_GUI_BUTTON = COMMON_BUILDER
                .comment("Show button in vanilla inventory")
                .define("showGuiButton", true);
        COMMON_BUILDER.pop(); // display

        COMMON_BUILDER.comment("Magic Table Settings").push("magic_table");
        magicTableUseLongConfig = COMMON_BUILDER
                .comment(
                        "Enable long (64-bit) count mode for Magic Table output",
                        "Default: false (uses int max = 2,147,483,647)",
                        "Set to true to use long max = 9,223,372,036,854,775,807"
                )
                .define("useLongCount", false);
        COMMON_BUILDER.pop(); // magic_table

        COMMON_BUILDER.comment("Gameplay Settings").push("gameplay");
        PERSIST_ON_DEATH = COMMON_BUILDER
                .comment("Keep inventory and unlocks on death")
                .define("persistOnDeath", true);
        REQUIRE_RING = COMMON_BUILDER
                .comment("Require power ring to use features")
                .define("requireRing", true);
        FILTER_RANGE = COMMON_BUILDER
                .comment("Range for filter/dump buttons")
                .defineInRange("filterRange", 32, 8, 128);
        COMMON_BUILDER.pop(); // gameplay

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
        freezePlayers = FREEZE_PLAYERS.get();
        emcFlowerManaPerEMC = emcFlowerManaPerEMCConfig.get();
        emcFlowerMaxMana = emcFlowerMaxManaConfig.get();
        lockWorldNameNebulaRod = new ArrayList<>(lockWorldNameNebulaRodConfig.get());
        nebulaWandCooldownTick = nebulaWandCooldownTickConfig.get();
        nebulaRodManaCost = nebulaRodManaCostConfig.get();
        limitXZCoords = limitXZCoordsConfig.get();
        maxDictariusCount = maxDictariusCountConfig.get();
        sprawlRodMaxArea = sprawlRodMaxAreaConfig.get();
        sprawlRodSpeed = sprawlRodSpeedConfig.get();
        syncPowerInventoryConfig();
        astralKillopManaCost = astralKillopManaCostConfig.get();
        astralKillopNuggetDay = astralKillopNuggetDayConfig.get();
        astralKillopEffectDay = astralKillopEffectDayConfig.get();
        astralKillopRange = astralKillopRangeConfig.get();
        astralKillopEffectDropCount = astralKillopEffectDropCountConfig.get();
        astralKillopEffectDuration = astralKillopEffectDurationConfig.get();
        astralKillopEffectLevel = astralKillopEffectLevelConfig.get();
        astralKillopMaxMana = astralKillopMaxManaConfig.get();
        lockEntityListToHorn = new ArrayList<>(lockEntityListToHornConfig.get());
        spreaderMaxMana = spreaderMaxManaConfig.get();
        spreaderBurstMana = spreaderBurstManaConfig.get();
        peacefulTableInAllDifficulties = peacefulTableInAllDifficultiesConfig.get();

        BlockPeacefulTable.clearCache();

        Exe.LOGGER.info("Peaceful Table config loaded: AllDifficulties={}, Blacklist={}",
                peacefulTableInAllDifficulties,
                PeacefulTableConfig.getEntityBlacklist());
        Exe.LOGGER.info("Horn of Plenty entity blacklist loaded: {}", lockEntityListToHorn);
        Exe.LOGGER.info("Advanced Botany Spreader config loaded: MaxMana={}, BurstMana={}",
                spreaderMaxMana, spreaderBurstMana);
    }

    public static void syncClientConfig() {
        enableDragonArmorOverlay = enableDragonArmorOverlayConfig.get();
        starlitGuiScale = starlitGuiScaleConfig.get().floatValue();
        starlitJeiScale = starlitJeiScaleConfig.get().floatValue();
        infinityPotatoDrawHalo = infinityPotatoDrawHaloConfig.get();
        itemNameplateEnabled = itemNameplateEnabledConfig.get();
        bladeFlashEnabled = ENABLED.get();
        bladeFlashHeldSwordTrails = HELD_SWORD_TRAILS.get();
        bladeFlashDemoOnSwordSwing = DEMO_ON_SWORD_SWING.get();
        bladeFlashTrailSeconds = TRAIL_SECONDS.get();
        bladeFlashDistortionPixels = DISTORTION_PIXELS.get();
        bladeFlashBrightness = BRIGHTNESS.get();
        bladeFlashMaxTrails = MAX_TRAILS.get();
        bladeFlashMaxSamplesPerTrail = MAX_SAMPLES.get();
        Exe.LOGGER.info("Starlit Sanctum GUI scales loaded: GUI={}, JEI={}",
                starlitGuiScale, starlitJeiScale);
        Exe.LOGGER.info("Infinity Potato config loaded: drawHalo={}", infinityPotatoDrawHalo);
    }

    private static void syncPowerInventoryConfig() {
        if (IS_LARGE_SCREEN.get()) {
            if (INVO_WIDTH.get() < 508) INVO_WIDTH.set(508);
            if (INVO_HEIGHT.get() < 346) INVO_HEIGHT.set(346);
            if (MAX_SECTIONS.get() < 15) MAX_SECTIONS.set(15);
        } else {
            if (INVO_WIDTH.get() > 342) INVO_WIDTH.set(342);
            if (INVO_HEIGHT.get() > 230) INVO_HEIGHT.set(230);
            if (MAX_SECTIONS.get() > 6) MAX_SECTIONS.set(6);
        }
        Exe.LOGGER.info("Power Inventory config synced: {}x{}, {} sections",
                INVO_WIDTH.get(), INVO_HEIGHT.get(), MAX_SECTIONS.get());
    }

    // ========== 内部类（不变）==========
    public static class PowerInventoryConfig {
        public static int getWidth() { return INVO_WIDTH.get(); }
        public static int getHeight() { return INVO_HEIGHT.get(); }
        public static int getMaxSections() { return MAX_SECTIONS.get(); }
        public static boolean isLargeScreen() { return IS_LARGE_SCREEN.get(); }
        public static boolean persistOnDeath() { return PERSIST_ON_DEATH.get(); }
        public static boolean requireRing() { return REQUIRE_RING.get(); }
        public static boolean showGuiButton() { return SHOW_GUI_BUTTON.get(); }
        public static int getFilterRange() { return FILTER_RANGE.get(); }
    }

    public static class MagicTableConfig {
        public static boolean useLongCount() { return magicTableUseLongConfig.get(); }
    }

    public static class ItemNameplateConfig {
        public static boolean isEnabled() { return itemNameplateEnabled; }
    }

    public static class NebulaRodConfig {
        public static List getLockWorldNames() { return lockWorldNameNebulaRod; }
        public static double getSprawlRodSpeed() { return sprawlRodSpeed; }
        public static int getSprawlRodMaxArea() { return sprawlRodMaxArea; }
        public static int getCooldownTicks() { return nebulaWandCooldownTick; }
        public static int getManaCost() { return nebulaRodManaCost; }
        public static int getCoordinateLimit() { return limitXZCoords; }
        public static boolean isWorldLocked(String worldName) { return lockWorldNameNebulaRod.contains(worldName); }
    }

    public static class BladeFlash {
        public static boolean isEnabled() { return bladeFlashEnabled; }
        public static boolean isHeldSwordTrails() { return bladeFlashHeldSwordTrails; }
        public static boolean isDemoOnSwordSwing() { return bladeFlashDemoOnSwordSwing; }
        public static double getTrailSeconds() { return bladeFlashTrailSeconds; }
        public static double getDistortionPixels() { return bladeFlashDistortionPixels; }
        public static double getBrightness() { return bladeFlashBrightness; }
        public static int getMaxTrails() { return bladeFlashMaxTrails; }
        public static int getMaxSamplesPerTrail() { return bladeFlashMaxSamplesPerTrail; }
    }

    public static class FlowerConfig {
        public static int getEMCFlowerManaPerEMC() { return emcFlowerManaPerEMC; }
        public static int getEMCFlowerMaxMana() { return emcFlowerMaxMana; }
        public static int getAstralKillopManaCost() { return astralKillopManaCost; }
        public static int getAstralKillopNuggetDay() { return astralKillopNuggetDay; }
        public static int getAstralKillopEffectDay() { return astralKillopEffectDay; }
        public static int getAstralKillopRange() { return astralKillopRange; }
        public static int getAstralKillopEffectDropCount() { return astralKillopEffectDropCount; }
        public static int getAstralKillopEffectDuration() { return astralKillopEffectDuration; }
        public static int getAstralKillopEffectLevel() { return astralKillopEffectLevel; }
        public static int getAstralKillopMaxMana() { return astralKillopMaxMana; }
    }

    public static class ClientConfig {
        public static boolean isDragonArmorOverlayEnabled() { return enableDragonArmorOverlay; }
    }

    public static class HornPlentyConfig {
        public static List getLockEntityList() { return lockEntityListToHorn; }
        public static boolean isEntityLocked(String entityClassName) {
            return lockEntityListToHorn.contains(entityClassName);
        }
    }

    public static class StarlitGuiConfig {
        public static float getGuiScale() { return starlitGuiScale; }
        public static float getJeiScale() { return starlitJeiScale; }
    }

    public static class ABSpreaderConfig {
        public static int getSpreaderMaxMana() { return spreaderMaxMana; }
        public static int getSpreaderBurstMana() { return spreaderBurstMana; }
    }

    public static class PeacefulTableConfig {
        public static boolean isEnabledInAllDifficulties() {
            return peacefulTableInAllDifficultiesConfig.get();
        }

        public static List<String> getEntityBlacklist() {
            if (peacefulTableEntityBlacklistConfig == null) return Collections.emptyList();
            return peacefulTableEntityBlacklistConfig.get()
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }

        public static boolean isEntityBlacklisted(ResourceLocation entityId) {
            return getEntityBlacklist().contains(entityId.toString());
        }
    }

    public static HashMap<String, Boolean> entityDamageWhitelistMap() {
        return entityDamageWhitelistMap;
    }

    public static void setEntityDamageWhitelistMap(HashMap<String, Boolean> whitelistMap) {
        entityDamageWhitelistMap = whitelistMap;
    }

    public static class InfinityPotatoConfig {
        public static boolean isDrawHalo() { return infinityPotatoDrawHalo; }
    }
}
