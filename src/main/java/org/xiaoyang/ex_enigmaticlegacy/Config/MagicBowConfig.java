package org.xiaoyang.ex_enigmaticlegacy.Config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.bow.MagicBowParticleEffectEntity;

public final class MagicBowConfig {
    private static final int DEFAULT_FULL_CHARGE_TIME = 20; // 默认基础满蓄力 tick 数。
    private static final boolean DEFAULT_QUICK_CHARGE_ENABLED = true; // 默认是否启用快速装填缩短蓄力。
    private static final double DEFAULT_QUICK_CHARGE_REDUCTION_PER_LEVEL = 0.30D; // 默认快速装填每级缩短比例。
    private static final double DEFAULT_SUPER_CHARGE_TIME_MULTIPLIER = 1.5D; // 默认星辰裁决蓄力时间倍率。
    private static final double DEFAULT_ARROW_VELOCITY_MULTIPLIER = 1.3D; // 默认魔法箭初速度倍率。
    private static final double DEFAULT_GRAVITY_COMPENSATION = 0.025D; // 默认魔法箭每 tick 重力补偿。
    private static final int DEFAULT_GROUNDED_DISCARD_TICKS = 40; // 默认魔法箭落地清理 tick 数。
    private static final double DEFAULT_NORMAL_ARROW_DAMAGE = 2.0D; // 默认普通魔法箭基础伤害。
    private static final double DEFAULT_STRONG_ARROW_DAMAGE = 2.75D; // 默认强蓄力魔法箭基础伤害。
    private static final double DEFAULT_SUPER_ARROW_DAMAGE = 3.5D; // 默认星辰裁决魔法箭基础伤害。
    private static final double DEFAULT_STRONG_CHARGE_CHANCE = 0.30D; // 默认强蓄力触发概率。
    private static final int DEFAULT_STRONG_SNARE_DURATION_TICKS = 60; // 默认强蓄力束缚 tick 数。
    private static final double DEFAULT_SUPER_CHARGE_CHANCE = 0.05D; // 默认星辰裁决触发概率。
    private static final int DEFAULT_SUPER_SNARE_DURATION_TICKS = 180; // 默认星辰裁决束缚 tick 数。
    private static final int DEFAULT_SUPER_FINAL_STRIKE_DELAY_TICKS = 130; // 默认星辰裁决最终伤害延迟。
    private static final double DEFAULT_SUPER_FINAL_STRIKE_DAMAGE = 200.0D; // 默认星辰裁决总伤害。
    private static final double DEFAULT_SUPER_FINAL_STRIKE_RADIUS = 10.0D; // 默认星辰裁决最终伤害半径。
    private static final double DEFAULT_SUPER_BOMBARDMENT_TICK_DAMAGE = 5.0D; // 默认星辰裁决轰炸期每 tick 伤害。
    private static final int DEFAULT_METEOR_STRIKE_INTERVAL_TICKS = 10; // 默认流星周期伤害间隔。
    private static final double DEFAULT_METEOR_STRIKE_DAMAGE = 4.0D; // 默认流星周期伤害数值。
    private static final double DEFAULT_ARROW_LANDING_SPLASH_DAMAGE = 3.0D; // 默认魔法箭落地扩散伤害。
    private static final double DEFAULT_ARROW_LANDING_SPLASH_RADIUS = 3.0D; // 默认魔法箭落地扩散伤害半径。
    private static final double DEFAULT_AUTO_TRACKING_MAX_LOCK_ANGLE = 20.0D; // 默认自动追踪最大锁定角度。
    private static final double DEFAULT_AUTO_TRACKING_MAX_LOCK_RANGE = 64.0D; // 默认自动追踪最大锁定距离。
    private static final boolean DEFAULT_AUTO_TRACKING_REQUIRE_LINE_OF_SIGHT = true; // 默认自动追踪是否要求视线可见。

    private static ForgeConfigSpec.IntValue fullChargeTime; // 基础满蓄力配置。
    private static ForgeConfigSpec.BooleanValue quickChargeEnabled; // 快速装填启用配置。
    private static ForgeConfigSpec.DoubleValue quickChargeReductionPerLevel; // 快速装填每级缩短比例配置。
    private static ForgeConfigSpec.DoubleValue superChargeTimeMultiplier; // 星辰裁决蓄力倍率配置。
    private static ForgeConfigSpec.DoubleValue arrowVelocityMultiplier; // 魔法箭初速度倍率配置。
    private static ForgeConfigSpec.DoubleValue gravityCompensation; // 魔法箭重力补偿配置。
    private static ForgeConfigSpec.IntValue groundedDiscardTicks; // 魔法箭落地清理配置。
    private static ForgeConfigSpec.DoubleValue normalArrowDamage; // 普通魔法箭基础伤害配置。
    private static ForgeConfigSpec.DoubleValue strongArrowDamage; // 强蓄力魔法箭基础伤害配置。
    private static ForgeConfigSpec.DoubleValue superArrowDamage; // 星辰裁决魔法箭基础伤害配置。
    private static ForgeConfigSpec.DoubleValue strongChargeChance; // 强蓄力概率配置。
    private static ForgeConfigSpec.IntValue strongSnareDurationTicks; // 强蓄力束缚时长配置。
    private static ForgeConfigSpec.DoubleValue superChargeChance; // 星辰裁决概率配置。
    private static ForgeConfigSpec.IntValue superSnareDurationTicks; // 星辰裁决束缚时长配置。
    private static ForgeConfigSpec.IntValue superFinalStrikeDelayTicks; // 星辰裁决最终伤害延迟配置。
    private static ForgeConfigSpec.DoubleValue superFinalStrikeDamage; // 星辰裁决最终伤害配置。
    private static ForgeConfigSpec.DoubleValue superFinalStrikeRadius; // 星辰裁决最终伤害半径配置。
    private static ForgeConfigSpec.DoubleValue superBombardmentTickDamage; // 星辰裁决轰炸期每 tick 伤害配置。
    private static ForgeConfigSpec.IntValue meteorStrikeIntervalTicks; // 流星周期伤害间隔配置。
    private static ForgeConfigSpec.DoubleValue meteorStrikeDamage; // 流星周期伤害数值配置。
    private static ForgeConfigSpec.DoubleValue arrowLandingSplashDamage; // 魔法箭落地扩散伤害配置。
    private static ForgeConfigSpec.DoubleValue arrowLandingSplashRadius; // 魔法箭落地扩散半径配置。
    private static ForgeConfigSpec.DoubleValue autoTrackingMaxLockAngle; // 自动追踪最大锁定角度配置。
    private static ForgeConfigSpec.DoubleValue autoTrackingMaxLockRange; // 自动追踪最大锁定距离配置。
    private static ForgeConfigSpec.BooleanValue autoTrackingRequireLineOfSight; // 自动追踪视线可见配置。

    private MagicBowConfig() {}

    // 注册魔法弓配置段。
    public static void register(ForgeConfigSpec.Builder builder) {
        builder.comment("Magic bow settings.")
                .comment("魔法弓配置。")
                .push("magicBow");

        builder.comment("Magic bow charge settings.")
                .comment("魔法弓蓄力配置。")
                .push("charge");
        fullChargeTime = builder
                .comment("Base ticks required for a full magic bow charge.")
                .comment("魔法弓基础满蓄力所需 tick 数。")
                .defineInRange("fullChargeTime", DEFAULT_FULL_CHARGE_TIME, 1, Integer.MAX_VALUE);
        quickChargeEnabled = builder
                .comment("Whether Quick Charge enchantment reduces magic bow charge time.")
                .comment("是否启用快速装填附魔缩短魔法弓蓄力时间。")
                .define("quickChargeEnabled", DEFAULT_QUICK_CHARGE_ENABLED);
        quickChargeReductionPerLevel = builder
                .comment("Charge time reduction ratio per Quick Charge level.")
                .comment("快速装填每级减少的蓄力时间比例。")
                .defineInRange("quickChargeReductionPerLevel", DEFAULT_QUICK_CHARGE_REDUCTION_PER_LEVEL, 0.0D, 0.95D);
        superChargeTimeMultiplier = builder
                .comment("Full charge time multiplier for Star Judgement.")
                .comment("星辰裁决满蓄力时间倍率。")
                .defineInRange("superChargeTimeMultiplier", DEFAULT_SUPER_CHARGE_TIME_MULTIPLIER, 1.0D, 20.0D);
        builder.pop();

        builder.comment("Magic arrow physics settings.")
                .comment("魔法箭物理配置。")
                .push("arrow");
        arrowVelocityMultiplier = builder
                .comment("Magic arrow velocity multiplier.")
                .comment("魔法箭初速度倍率。")
                .defineInRange("velocityMultiplier", DEFAULT_ARROW_VELOCITY_MULTIPLIER, 0.1D, 10.0D);
        gravityCompensation = builder
                .comment("Y velocity added after vanilla arrow tick to reduce drop.")
                .comment("原版箭 tick 后回补的 Y 速度，用于减少下坠。")
                .defineInRange("gravityCompensation", DEFAULT_GRAVITY_COMPENSATION, 0.0D, 1.0D);
        groundedDiscardTicks = builder
                .comment("Ticks before a magic arrow is removed after landing in a block.")
                .comment("魔法箭落到方块后等待多少 tick 自动移除。")
                .defineInRange("groundedDiscardTicks", DEFAULT_GROUNDED_DISCARD_TICKS, 1, Integer.MAX_VALUE);
        normalArrowDamage = builder
                .comment("Base damage dealt by a normal magic arrow before enchantment bonuses.")
                .comment("普通魔法箭在附魔加成前的基础伤害。")
                .defineInRange("normalDamage", DEFAULT_NORMAL_ARROW_DAMAGE, 0.0D, Double.MAX_VALUE);
        strongArrowDamage = builder
                .comment("Base damage dealt by a strong charge magic arrow before enchantment bonuses.")
                .comment("强蓄力魔法箭在附魔加成前的基础伤害。")
                .defineInRange("strongDamage", DEFAULT_STRONG_ARROW_DAMAGE, 0.0D, Double.MAX_VALUE);
        superArrowDamage = builder
                .comment("Base damage dealt by a Star Judgement magic arrow before enchantment bonuses.")
                .comment("星辰裁决魔法箭在附魔加成前的基础伤害。")
                .defineInRange("superDamage", DEFAULT_SUPER_ARROW_DAMAGE, 0.0D, Double.MAX_VALUE);
        builder.pop();

        builder.comment("Magic bow strong charge settings.")
                .comment("魔法弓强蓄力配置。")
                .push("strongCharge");
        strongChargeChance = builder
                .comment("Chance of strong charge for each charge round.")
                .comment("每轮蓄力触发强蓄力的概率。")
                .defineInRange("chance", DEFAULT_STRONG_CHARGE_CHANCE, 0.0D, 1.0D);
        strongSnareDurationTicks = builder
                .comment("Strong charge snare duration in ticks.")
                .comment("强蓄力束缚持续 tick 数。")
                .defineInRange("snareDurationTicks", DEFAULT_STRONG_SNARE_DURATION_TICKS, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.comment("Magic bow Star Judgement settings. The superCharge path is kept for compatibility.")
                .comment("魔法弓星辰裁决配置，superCharge 路径保留用于兼容旧配置。")
                .push("superCharge");
        superChargeChance = builder
                .comment("Chance of Star Judgement for each charge round%.")
                .comment("每轮蓄力触发星辰裁决的概率%。")
                .defineInRange("chance", DEFAULT_SUPER_CHARGE_CHANCE, 0.0D, 1.0D);
        superSnareDurationTicks = builder
                .comment("Star Judgement snare duration in ticks.")
                .comment("星辰裁决束缚持续 tick 数。")
                .defineInRange("snareDurationTicks", DEFAULT_SUPER_SNARE_DURATION_TICKS, 0, Integer.MAX_VALUE);
        superFinalStrikeDelayTicks = builder
                .comment("Delay before Star Judgement area damage in ticks.")
                .comment("星辰裁决范围伤害延迟 tick 数。")
                .defineInRange("finalStrikeDelayTicks", DEFAULT_SUPER_FINAL_STRIKE_DELAY_TICKS, 0, Integer.MAX_VALUE);
        superFinalStrikeDamage = builder
                .comment("Damage dealt by Star Judgement to each target in radius.")
                .comment("星辰裁决对范围内每个目标造成的伤害。")
                .defineInRange("finalStrikeDamage", DEFAULT_SUPER_FINAL_STRIKE_DAMAGE, 0.0D, Double.MAX_VALUE);
        superFinalStrikeRadius = builder
                .comment("Radius of Star Judgement area damage.")
                .comment("星辰裁决范围伤害半径。")
                .defineInRange("finalStrikeRadius", DEFAULT_SUPER_FINAL_STRIKE_RADIUS, 0.0D, 128.0D);
        superBombardmentTickDamage = builder
                .comment("Damage dealt every tick to cached target creatures during Star Judgement bombardment.")
                .comment("星辰裁决轰炸期每 tick 对缓存目标生物造成的伤害。")
                .defineInRange("bombardmentTickDamage", DEFAULT_SUPER_BOMBARDMENT_TICK_DAMAGE, 0.0D, Double.MAX_VALUE);
        builder.pop();

        builder.comment("Magic bow effect entity damage settings.")
                .comment("魔法弓效果实体伤害配置。")
                .push("effectEntity");
        meteorStrikeIntervalTicks = builder
                .comment("Ticks between meteor strike damage applications.")
                .comment("流星效果每隔多少 tick 对目标造成一次打击。")
                .defineInRange("meteorStrikeIntervalTicks", DEFAULT_METEOR_STRIKE_INTERVAL_TICKS, 1, Integer.MAX_VALUE);
        meteorStrikeDamage = builder
                .comment("Damage dealt by each meteor strike.")
                .comment("每次流星打击造成的伤害。")
                .defineInRange("meteorStrikeDamage", DEFAULT_METEOR_STRIKE_DAMAGE, 0.0D, Double.MAX_VALUE);
        arrowLandingSplashDamage = builder
                .comment("Damage dealt to monsters near a magic arrow landing point.")
                .comment("魔法箭落地时对附近怪物造成的一次扩散伤害。")
                .defineInRange("arrowLandingSplashDamage", DEFAULT_ARROW_LANDING_SPLASH_DAMAGE, 0.0D, Double.MAX_VALUE);
        arrowLandingSplashRadius = builder
                .comment("Radius of magic arrow landing splash damage.")
                .comment("魔法箭落地扩散伤害半径。")
                .defineInRange("arrowLandingSplashRadius", DEFAULT_ARROW_LANDING_SPLASH_RADIUS, 0.0D, 32.0D);
        builder.pop();

        builder.comment("Magic bow auto tracking settings.")
                .comment("魔法弓自动追踪配置。")
                .push("autoTracking");
        autoTrackingMaxLockAngle = builder
                .comment("Maximum angle in degrees for auto tracking target lock.")
                .comment("自动追踪锁定目标的最大角度。")
                .defineInRange("maxLockAngle", DEFAULT_AUTO_TRACKING_MAX_LOCK_ANGLE, 1.0D, 90.0D);
        autoTrackingMaxLockRange = builder
                .comment("Maximum target lock range for auto tracking.")
                .comment("自动追踪锁定目标的最大距离。")
                .defineInRange("maxLockRange", DEFAULT_AUTO_TRACKING_MAX_LOCK_RANGE, 4.0D, 256.0D);
        autoTrackingRequireLineOfSight = builder
                .comment("Whether auto tracking requires line of sight to the target.")
                .comment("自动追踪是否要求目标在视线内。")
                .define("requireLineOfSight", DEFAULT_AUTO_TRACKING_REQUIRE_LINE_OF_SIGHT);
        builder.pop();

        builder.pop();
    }

    public static int fullChargeTime() { return safeGet(fullChargeTime, DEFAULT_FULL_CHARGE_TIME); }
    public static boolean quickChargeEnabled() { return safeGet(quickChargeEnabled, DEFAULT_QUICK_CHARGE_ENABLED); }
    public static double quickChargeReductionPerLevel() { return safeGet(quickChargeReductionPerLevel, DEFAULT_QUICK_CHARGE_REDUCTION_PER_LEVEL); }
    public static double superChargeTimeMultiplier() { return safeGet(superChargeTimeMultiplier, DEFAULT_SUPER_CHARGE_TIME_MULTIPLIER); }
    public static float arrowVelocityMultiplier() { return (float) safeGet(arrowVelocityMultiplier, DEFAULT_ARROW_VELOCITY_MULTIPLIER); }
    public static double gravityCompensation() { return safeGet(gravityCompensation, DEFAULT_GRAVITY_COMPENSATION); }
    public static int groundedDiscardTicks() { return safeGet(groundedDiscardTicks, DEFAULT_GROUNDED_DISCARD_TICKS); }
    public static double normalArrowDamage() { return safeGet(normalArrowDamage, DEFAULT_NORMAL_ARROW_DAMAGE); }
    public static double strongArrowDamage() { return safeGet(strongArrowDamage, DEFAULT_STRONG_ARROW_DAMAGE); }
    public static double superArrowDamage() { return safeGet(superArrowDamage, DEFAULT_SUPER_ARROW_DAMAGE); }
    // 按蓄力类型读取魔法箭本体基础伤害。
    public static double arrowDamage(int chargeType) {
        if (chargeType == MagicBowParticleEffectEntity.CHARGE_SUPER) return superArrowDamage();
        if (chargeType == MagicBowParticleEffectEntity.CHARGE_STRONG) return strongArrowDamage();
        return normalArrowDamage();
    }
    public static double strongChargeChance() { return safeGet(strongChargeChance, DEFAULT_STRONG_CHARGE_CHANCE); }
    public static int strongSnareDurationTicks() { return Math.max(safeGet(strongSnareDurationTicks, DEFAULT_STRONG_SNARE_DURATION_TICKS), DEFAULT_STRONG_SNARE_DURATION_TICKS); }
    public static double superChargeChance() { return safeGet(superChargeChance, DEFAULT_SUPER_CHARGE_CHANCE); }
    public static int superSnareDurationTicks() { return Math.max(safeGet(superSnareDurationTicks, DEFAULT_SUPER_SNARE_DURATION_TICKS), DEFAULT_SUPER_SNARE_DURATION_TICKS); }
    public static int superFinalStrikeDelayTicks() { return safeGet(superFinalStrikeDelayTicks, DEFAULT_SUPER_FINAL_STRIKE_DELAY_TICKS); }
    public static float superFinalStrikeDamage() { return (float) safeGet(superFinalStrikeDamage, DEFAULT_SUPER_FINAL_STRIKE_DAMAGE); }
    public static double superFinalStrikeRadius() { return Math.max(safeGet(superFinalStrikeRadius, DEFAULT_SUPER_FINAL_STRIKE_RADIUS), DEFAULT_SUPER_FINAL_STRIKE_RADIUS); }
    public static float superBombardmentTickDamage() { return (float) safeGet(superBombardmentTickDamage, DEFAULT_SUPER_BOMBARDMENT_TICK_DAMAGE); }
    public static int meteorStrikeIntervalTicks() { return safeGet(meteorStrikeIntervalTicks, DEFAULT_METEOR_STRIKE_INTERVAL_TICKS); }
    public static float meteorStrikeDamage() { return (float) safeGet(meteorStrikeDamage, DEFAULT_METEOR_STRIKE_DAMAGE); }
    public static float arrowLandingSplashDamage() { return (float) safeGet(arrowLandingSplashDamage, DEFAULT_ARROW_LANDING_SPLASH_DAMAGE); }
    public static double arrowLandingSplashRadius() { return safeGet(arrowLandingSplashRadius, DEFAULT_ARROW_LANDING_SPLASH_RADIUS); }
    public static double autoTrackingMaxLockAngle() { return safeGet(autoTrackingMaxLockAngle, DEFAULT_AUTO_TRACKING_MAX_LOCK_ANGLE); }
    public static double autoTrackingMaxLockRange() { return safeGet(autoTrackingMaxLockRange, DEFAULT_AUTO_TRACKING_MAX_LOCK_RANGE); }
    public static boolean autoTrackingRequireLineOfSight() { return safeGet(autoTrackingRequireLineOfSight, DEFAULT_AUTO_TRACKING_REQUIRE_LINE_OF_SIGHT); }

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

    // 配置尚未加载时返回默认 double，避免注册期读取配置崩溃。
    public static double safeGet(ForgeConfigSpec.DoubleValue value, double fallback) {
        if (value == null) return fallback;
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }
}
