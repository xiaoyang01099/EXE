package org.xiaoyang.ex_enigmaticlegacy.Config;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.HashMap;
import java.util.List;

public class ConfigFile {
    public static final ForgeConfigSpec CONFIG_SPEC;
    private static final ForgeConfigSpec.BooleanValue CAN_BREAK_BLOCK;
    private static final ForgeConfigSpec.BooleanValue DAMAGE_PLAYERS;
    private static final ForgeConfigSpec.IntValue FLY_SWORD_ATTACK_DAMAGE;
    private static final ForgeConfigSpec.IntValue FLY_SWORD_SEARCH_RANGE;
    private static final ForgeConfigSpec.BooleanValue FLY_SWORD_ATTACK_ALL_ENTITIES;
    private static final ForgeConfigSpec.DoubleValue FLY_SWORD_AURA_DAMAGE;
    private static final ForgeConfigSpec.DoubleValue FLY_SWORD_AURA_SPEED;
    private static final ForgeConfigSpec.IntValue FLY_SWORD_AURA_LIFE_TICKS;
    private static final ForgeConfigSpec.DoubleValue FLY_SWORD_AURA_HIT_RADIUS;
    private static final ForgeConfigSpec.IntValue FLY_SWORD_DIMENSION_SLASH_COOLDOWN;
    private static final ForgeConfigSpec.DoubleValue FLY_SWORD_DIMENSION_SLASH_SMALL_DAMAGE;
    private static final ForgeConfigSpec.DoubleValue FLY_SWORD_DIMENSION_SLASH_FINAL_DAMAGE;
    private static final ForgeConfigSpec.DoubleValue FLY_SWORD_BATTO_SLASH_DAMAGE;
    private static final ForgeConfigSpec.DoubleValue FLY_SWORD_BATTO_SLASH_RADIUS;
    private static final ForgeConfigSpec.IntValue FLY_SWORD_BATTO_SLASH_CHARGE_TICKS;
    private static final ForgeConfigSpec.DoubleValue BEAM_DAMAGE;
    private static final ForgeConfigSpec.DoubleValue MAX_RANGE;
    private static final ForgeConfigSpec.DoubleValue COLORFUL_BEAM_DAMAGE;
    private static final ForgeConfigSpec.DoubleValue COLORFUL_MAX_RANGE;
    private static final ForgeConfigSpec.IntValue COLORFUL_FULL_CHARGE_TIME;
    private static final ForgeConfigSpec.BooleanValue COLORFUL_QUICK_CHARGE_ENABLED;
    private static final ForgeConfigSpec.DoubleValue COLORFUL_QUICK_CHARGE_REDUCTION;
    private static final ForgeConfigSpec.IntValue SPARKLING_BUFF_DURATION_TICKS;
    private static final ForgeConfigSpec.IntValue SPARKLING_SPEED_AMPLIFIER;
    private static final ForgeConfigSpec.IntValue SPARKLING_JUMP_AMPLIFIER;
    private static final ForgeConfigSpec.DoubleValue SPARKLING_TELEPORT_DISTANCE;
    private static final ForgeConfigSpec.IntValue SPARKLING_TELEPORT_COOLDOWN_TICKS;
    private static final ForgeConfigSpec.DoubleValue SPARKLING_FLIGHT_BOOST_MAX_SPEED;
    private static final ForgeConfigSpec.IntValue SPARKLING_FLIGHT_BOOST_ACCELERATION_TICKS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITY_DAMAGE_WHITELIST;
    private static volatile HashMap<String, Boolean> entityDamageWhitelistMap = new HashMap<>();

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("EXE settings").comment("EXE 配置").push("setting");

        CAN_BREAK_BLOCK = builder
                .comment("can break terrain blocks, It will not damage FTB Chunks.")
                .comment("是否破坏地形方块，不会破坏FTB Chunks。")
                .define("canBreakBlock", true);
        DAMAGE_PLAYERS = builder
                .comment("can damage player entities.")
                .comment("是否对玩家实体造成伤害。")
                .define("damagePlayers", false);
        ENTITY_DAMAGE_WHITELIST = builder
                .comment("Entity damage whitelist settings.")
                .comment("实体伤害白名单配置。")
                .defineList("whitelist", List.of("touhou_little_maid:maid", "minecraft:cat"), value -> value instanceof String);

        builder.comment("Fly sword settings.").push("flySword");
        FLY_SWORD_ATTACK_DAMAGE = builder.defineInRange("attackDamage", 2, 0, Integer.MAX_VALUE);
        FLY_SWORD_SEARCH_RANGE = builder.defineInRange("searchRange", 16, 6, 160);
        FLY_SWORD_ATTACK_ALL_ENTITIES = builder.define("attackAllEntities", false);
        FLY_SWORD_AURA_DAMAGE = builder.defineInRange("swordAuraDamage", 2.0D, 0.0D, Double.MAX_VALUE);
        FLY_SWORD_AURA_SPEED = builder.defineInRange("swordAuraSpeed", 2.0D, 0.0D, 16.0D);
        FLY_SWORD_AURA_LIFE_TICKS = builder.defineInRange("swordAuraLifeTicks", 18, 1, 200);
        FLY_SWORD_AURA_HIT_RADIUS = builder.defineInRange("swordAuraHitRadius", 2.0D, 0.0D, 16.0D);
        FLY_SWORD_DIMENSION_SLASH_COOLDOWN = builder.defineInRange("dimensionSlashCooldown", 200, 1, Integer.MAX_VALUE);
        FLY_SWORD_DIMENSION_SLASH_SMALL_DAMAGE = builder.defineInRange("dimensionSlashSmallDamage", 2.0D, 0.0D, Double.MAX_VALUE);
        FLY_SWORD_DIMENSION_SLASH_FINAL_DAMAGE = builder.defineInRange("dimensionSlashFinalDamage", 200.0D, 0.0D, Double.MAX_VALUE);
        FLY_SWORD_BATTO_SLASH_DAMAGE = builder.defineInRange("battoSlashDamage", 30.0D, 0.0D, Double.MAX_VALUE);
        FLY_SWORD_BATTO_SLASH_RADIUS = builder.defineInRange("battoSlashRadius", 25.0D, 0.0D, 2560.0D);
        FLY_SWORD_BATTO_SLASH_CHARGE_TICKS = builder.defineInRange("battoSlashChargeTicks", 20, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.comment("Beam item settings.").push("beamItem");
        BEAM_DAMAGE = builder.defineInRange("beamDamage", 20.0D, 0.0D, Double.MAX_VALUE);
        MAX_RANGE = builder.defineInRange("maxRange", 50.0D, 0.0D, 10000.0D);
        builder.pop();

        builder.comment("Colorful beam settings.").push("colorful");
        COLORFUL_BEAM_DAMAGE = builder.defineInRange("beamDamage", 40.0D, 0.0D, Double.MAX_VALUE);
        COLORFUL_MAX_RANGE = builder.defineInRange("maxRange", 60.0D, 0.0D, 10000.0D);

        builder.push("charge");
        COLORFUL_FULL_CHARGE_TIME = builder.defineInRange("fullChargeTime", 50, 1, Integer.MAX_VALUE);
        COLORFUL_QUICK_CHARGE_ENABLED = builder.define("quickChargeEnabled", true);
        COLORFUL_QUICK_CHARGE_REDUCTION = builder.defineInRange("quickChargeReduction", 0.20D, 0.0D, 1.0D);
        builder.pop();
        builder.pop();

        builder.comment("Sparkling fruit settings.").push("sparkling");
        SPARKLING_BUFF_DURATION_TICKS = builder.defineInRange("buffDurationTicks", 600, 1, Integer.MAX_VALUE);
        SPARKLING_SPEED_AMPLIFIER = builder.defineInRange("speedAmplifier", 4, 4, 255);
        SPARKLING_JUMP_AMPLIFIER = builder.defineInRange("jumpAmplifier", 4, 4, 255);
        SPARKLING_TELEPORT_DISTANCE = builder.defineInRange("teleportDistance", 12.0D, 0.0D, 2560.0D);
        SPARKLING_TELEPORT_COOLDOWN_TICKS = builder.defineInRange("teleportCooldownTicks", 6, 1, Integer.MAX_VALUE);
        SPARKLING_FLIGHT_BOOST_MAX_SPEED = builder.defineInRange("flightBoostMaxSpeed", 10.0D, 0.1D, Double.MAX_VALUE);
        SPARKLING_FLIGHT_BOOST_ACCELERATION_TICKS = builder.defineInRange("flightBoostAccelerationTicks", 40, 1, Integer.MAX_VALUE);
        builder.pop();

        TridentPlusConfig.register(builder);
        MagicBowConfig.register(builder);
        ExExcaliburConfig.register(builder);

        builder.pop();
        CONFIG_SPEC = builder.build();
    }

    public static boolean canBreakBlock() { return CAN_BREAK_BLOCK.get(); }
    public static boolean damagePlayers() { return DAMAGE_PLAYERS.get(); }
    public static int flySwordAttackDamage() { return FLY_SWORD_ATTACK_DAMAGE.get(); }
    public static int flySwordSearchRange() { return FLY_SWORD_SEARCH_RANGE.get(); }
    public static boolean flySwordAttackAllEntities() { return FLY_SWORD_ATTACK_ALL_ENTITIES.get(); }
    public static float flySwordAuraDamage() { return FLY_SWORD_AURA_DAMAGE.get().floatValue(); }
    public static double flySwordAuraSpeed() { return FLY_SWORD_AURA_SPEED.get(); }
    public static int flySwordAuraLifeTicks() { return FLY_SWORD_AURA_LIFE_TICKS.get(); }
    public static double flySwordAuraHitRadius() { return FLY_SWORD_AURA_HIT_RADIUS.get(); }
    public static int flySwordDimensionSlashCooldown() { return FLY_SWORD_DIMENSION_SLASH_COOLDOWN.get(); }
    public static float flySwordDimensionSlashSmallDamage() { return FLY_SWORD_DIMENSION_SLASH_SMALL_DAMAGE.get().floatValue(); }
    public static float flySwordDimensionSlashFinalDamage() { return FLY_SWORD_DIMENSION_SLASH_FINAL_DAMAGE.get().floatValue(); }
    public static float flySwordBattoSlashDamage() { return FLY_SWORD_BATTO_SLASH_DAMAGE.get().floatValue(); }
    public static double flySwordBattoSlashRadius() { return FLY_SWORD_BATTO_SLASH_RADIUS.get(); }
    public static int flySwordBattoSlashChargeTicks() { return FLY_SWORD_BATTO_SLASH_CHARGE_TICKS.get(); }
    public static float BeamDamage() { return BEAM_DAMAGE.get().floatValue(); }
    public static double MaxRange() { return MAX_RANGE.get(); }
    public static float colorfulBeamDamage() { return COLORFUL_BEAM_DAMAGE.get().floatValue(); }
    public static double colorfulMaxRange() { return COLORFUL_MAX_RANGE.get(); }
    public static int colorfulFullChargeTime() { return COLORFUL_FULL_CHARGE_TIME.get(); }
    public static boolean colorfulQuickChargeEnabled() { return COLORFUL_QUICK_CHARGE_ENABLED.get(); }
    public static double colorfulQuickChargeReduction() { return COLORFUL_QUICK_CHARGE_REDUCTION.get(); }
    public static int sparklingBuffDurationTicks() { return SPARKLING_BUFF_DURATION_TICKS.get(); }
    public static int sparklingSpeedAmplifier() { return SPARKLING_SPEED_AMPLIFIER.get(); }
    public static int sparklingJumpAmplifier() { return SPARKLING_JUMP_AMPLIFIER.get(); }
    public static double sparklingTeleportDistance() { return SPARKLING_TELEPORT_DISTANCE.get(); }
    public static int sparklingTeleportCooldownTicks() { return SPARKLING_TELEPORT_COOLDOWN_TICKS.get(); }
    public static double sparklingFlightBoostMaxSpeed() { return SPARKLING_FLIGHT_BOOST_MAX_SPEED.get(); }
    public static int sparklingFlightBoostAccelerationTicks() { return SPARKLING_FLIGHT_BOOST_ACCELERATION_TICKS.get(); }
    public static List<? extends String> entityDamageWhitelist() { return ENTITY_DAMAGE_WHITELIST.get(); }
    public static HashMap<String, Boolean> entityDamageWhitelistMap() { return entityDamageWhitelistMap; }
    public static void setEntityDamageWhitelistMap(HashMap<String, Boolean> whitelistMap) { entityDamageWhitelistMap = whitelistMap; }
}