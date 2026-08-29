package org.xiaoyang.ex_enigmaticlegacy.Config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class TridentPlusConfig {
    public static final int TRIDENT_PLUS_DURABILITY = 2048; // 天雷战戟固定耐久。
    private static final double DEFAULT_ATTACK_DAMAGE = 12.0D; // 默认近战伤害。
    private static final double DEFAULT_THROWN_DAMAGE = 12.0D; // 默认投掷直接命中伤害。
    private static final double DEFAULT_SPLASH_DAMAGE = 10.0D; // 默认落点范围伤害。
    private static final double DEFAULT_SPLASH_RADIUS = 10.0D; // 默认落点范围半径。
    private static final int DEFAULT_NORMAL_LIGHTNING_COUNT = 4; // 默认普通落雷数量。
    private static final int DEFAULT_ENHANCED_LIGHTNING_COUNT = 18; // 默认引雷强化落雷密度。
    private static final int DEFAULT_ENHANCED_DURATION_TICKS = 60; // 默认引雷强化持续 tick。
    private static final int DEFAULT_ENHANCED_DAMAGE_INTERVAL = 5; // 默认强化伤害间隔。
    private static final double DEFAULT_HEAVENLY_THUNDER_DAMAGE = 20.0D; // 默认天雷法阵每次持续伤害。
    private static final int DEFAULT_HEAVENLY_THUNDER_DAMAGE_INTERVAL_TICKS = 5; // 默认天雷法阵持续伤害间隔 tick。
    private static final int DEFAULT_HEAVENLY_THUNDER_CHARGE_TICKS = 20; // 默认天雷技能蓄力 tick。
    private static final int DEFAULT_HEAVENLY_THUNDER_COOLDOWN_TICKS = 400; // 默认天雷技能冷却 tick。
    private static final boolean DEFAULT_HEAVENLY_THUNDER_SLOW_WHILE_CHARGING = true; // 默认天雷蓄力时减速。

    private static ForgeConfigSpec.DoubleValue attackDamage; // 近战伤害配置。
    private static ForgeConfigSpec.DoubleValue thrownDamage; // 投掷直接命中伤害配置。
    private static ForgeConfigSpec.DoubleValue splashDamage; // 落点范围伤害配置。
    private static ForgeConfigSpec.DoubleValue splashRadius; // 落点范围半径配置。
    private static ForgeConfigSpec.IntValue normalLightningCount; // 普通落雷数量配置。
    private static ForgeConfigSpec.IntValue enhancedLightningCount; // 引雷强化落雷密度配置。
    private static ForgeConfigSpec.IntValue enhancedDurationTicks; // 引雷强化持续时间配置。
    private static ForgeConfigSpec.IntValue enhancedDamageInterval; // 引雷强化伤害间隔配置。
    private static ForgeConfigSpec.DoubleValue heavenlyThunderDamage; // 天雷法阵持续伤害配置。
    private static ForgeConfigSpec.IntValue heavenlyThunderDamageIntervalTicks; // 天雷法阵持续伤害间隔配置。
    private static ForgeConfigSpec.IntValue heavenlyThunderChargeTicks; // 天雷技能蓄力时间配置。
    private static ForgeConfigSpec.IntValue heavenlyThunderCooldownTicks; // 天雷技能冷却时间配置。
    private static ForgeConfigSpec.BooleanValue heavenlyThunderSlowWhileCharging; // 天雷技能蓄力减速开关。

    private TridentPlusConfig() {}

    public static void register(ForgeConfigSpec.Builder builder) {
        builder.comment("Trident plus settings.")
                .comment("天雷战戟配置。")
                .push("tridentPlus");
        attackDamage = builder
                .comment("Melee attack damage of Trident Plus.")
                .comment("天雷战戟手持近战攻击伤害。")
                .defineInRange("attackDamage", DEFAULT_ATTACK_DAMAGE, 0.0D, Double.MAX_VALUE);
        thrownDamage = builder
                .comment("Direct hit damage of thrown Trident Plus.")
                .comment("天雷战戟投掷直接命中伤害。")
                .defineInRange("thrownDamage", DEFAULT_THROWN_DAMAGE, 0.0D, Double.MAX_VALUE);
        splashDamage = builder
                .comment("Area damage dealt by landing lightning.")
                .comment("天雷战戟落点雷电范围伤害。")
                .defineInRange("splashDamage", DEFAULT_SPLASH_DAMAGE, 0.0D, Double.MAX_VALUE);
        splashRadius = builder
                .comment("Area damage radius of landing lightning.")
                .comment("天雷战戟落点雷电范围半径。")
                .defineInRange("splashRadius", DEFAULT_SPLASH_RADIUS, 0.0D, 64.0D);
        normalLightningCount = builder
                .comment("Visual lightning count for normal landing.")
                .comment("普通落点视觉落雷数量。")
                .defineInRange("normalLightningCount", DEFAULT_NORMAL_LIGHTNING_COUNT, 1, 64);
        enhancedLightningCount = builder
                .comment("Visual lightning density for channeling landing.")
                .comment("引雷强化视觉落雷密度。")
                .defineInRange("enhancedLightningCount", DEFAULT_ENHANCED_LIGHTNING_COUNT, 1, 128);
        enhancedDurationTicks = builder
                .comment("Duration of channeling lightning storm in ticks.")
                .comment("引雷强化雷暴持续 tick 数。")
                .defineInRange("enhancedDurationTicks", DEFAULT_ENHANCED_DURATION_TICKS, 1, 400);
        enhancedDamageInterval = builder
                .comment("Ticks between channeling area damage applications.")
                .comment("引雷强化范围伤害间隔 tick 数。")
                .defineInRange("enhancedDamageInterval", DEFAULT_ENHANCED_DAMAGE_INTERVAL, 1, 200);
        heavenlyThunderDamage = builder
                .comment("Area damage dealt by each Heavenly Thunder magic circle damage tick.")
                .comment("天雷 V 键法阵每次持续伤害数值。")
                .defineInRange("heavenlyThunderDamage", DEFAULT_HEAVENLY_THUNDER_DAMAGE, 0.0D, Double.MAX_VALUE);
        heavenlyThunderDamageIntervalTicks = builder
                .comment("Ticks between Heavenly Thunder magic circle damage applications.")
                .comment("天雷 V 键法阵持续伤害间隔 tick 数。")
                .defineInRange("heavenlyThunderDamageIntervalTicks", DEFAULT_HEAVENLY_THUNDER_DAMAGE_INTERVAL_TICKS, 1, 200);
        heavenlyThunderChargeTicks = builder
                .comment("Ticks required to charge the Heavenly Thunder skill before automatic release.")
                .comment("天雷技能自动释放前需要蓄力的 tick 数。")
                .defineInRange("heavenlyThunderChargeTicks", DEFAULT_HEAVENLY_THUNDER_CHARGE_TICKS, 1, 200);
        heavenlyThunderCooldownTicks = builder
                .comment("Cooldown of the Heavenly Thunder skill in ticks.")
                .comment("天雷技能冷却 tick 数。")
                .defineInRange("heavenlyThunderCooldownTicks", DEFAULT_HEAVENLY_THUNDER_COOLDOWN_TICKS, 1, Integer.MAX_VALUE);
        heavenlyThunderSlowWhileCharging = builder
                .comment("Whether holding the Heavenly Thunder charge key slows player movement.")
                .comment("长按天雷技能蓄力键时是否降低玩家移动速度。")
                .define("heavenlyThunderSlowWhileCharging", DEFAULT_HEAVENLY_THUNDER_SLOW_WHILE_CHARGING);
        builder.pop();
    }

    public static float attackDamage() {
        return (float) safeGet(attackDamage, DEFAULT_ATTACK_DAMAGE);
    }

    public static float thrownDamage() {
        return (float) safeGet(thrownDamage, DEFAULT_THROWN_DAMAGE);
    }

    public static float splashDamage() {
        return (float) safeGet(splashDamage, DEFAULT_SPLASH_DAMAGE);
    }

    public static double splashRadius() {
        return safeGet(splashRadius, DEFAULT_SPLASH_RADIUS);
    }

    public static int normalLightningCount() {
        return safeGet(normalLightningCount, DEFAULT_NORMAL_LIGHTNING_COUNT);
    }

    public static int enhancedLightningCount() {
        return safeGet(enhancedLightningCount, DEFAULT_ENHANCED_LIGHTNING_COUNT);
    }

    public static int enhancedDurationTicks() {
        return safeGet(enhancedDurationTicks, DEFAULT_ENHANCED_DURATION_TICKS);
    }

    public static int enhancedDamageInterval() {
        return safeGet(enhancedDamageInterval, DEFAULT_ENHANCED_DAMAGE_INTERVAL);
    }

    public static float heavenlyThunderDamage() {
        return (float) safeGet(heavenlyThunderDamage, DEFAULT_HEAVENLY_THUNDER_DAMAGE);
    }

    public static int heavenlyThunderDamageIntervalTicks() {
        return safeGet(heavenlyThunderDamageIntervalTicks, DEFAULT_HEAVENLY_THUNDER_DAMAGE_INTERVAL_TICKS);
    }

    public static int heavenlyThunderChargeTicks() {
        return safeGet(heavenlyThunderChargeTicks, DEFAULT_HEAVENLY_THUNDER_CHARGE_TICKS);
    }

    public static int heavenlyThunderCooldownTicks() {
        return safeGet(heavenlyThunderCooldownTicks, DEFAULT_HEAVENLY_THUNDER_COOLDOWN_TICKS);
    }

    public static boolean heavenlyThunderSlowWhileCharging() {
        return safeGet(heavenlyThunderSlowWhileCharging, DEFAULT_HEAVENLY_THUNDER_SLOW_WHILE_CHARGING);
    }

    // 配置尚未加载时返回默认 double，避免注册期读取配置崩溃。
    public static double safeGet(ForgeConfigSpec.DoubleValue value, double fallback) {
        if (value == null) return fallback;
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    // 配置尚未加载时返回默认 int，避免注册期读取配置崩溃。
    public static int safeGet(ForgeConfigSpec.IntValue value, int fallback) {
        if (value == null) return fallback;
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    // 配置尚未加载时返回默认 boolean，避免注册期读取配置崩溃。
    public static boolean safeGet(ForgeConfigSpec.BooleanValue value, boolean fallback) {
        if (value == null) return fallback;
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }
}
