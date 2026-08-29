package org.xiaoyang.ex_enigmaticlegacy.Config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ExExcaliburConfig {
    public static final double DEFAULT_MAX_RANGE = 150.0D; // 默认最大射程。
    public static final double DEFAULT_BRANCH_DISTANCE = 20.0D; // 默认最大单侧分叉距离。
    public static final double DEFAULT_DAMAGE = 1024.0D; // 默认每 tick 锥形范围命中伤害。
    public static final double DEFAULT_DAMAGE_HEIGHT_UP = 30.0D; // 默认锥形伤害中心线上方高度。
    public static final double DEFAULT_DAMAGE_HEIGHT_DOWN = 5.0D; // 默认锥形伤害中心线下方高度。
    public static final double DEFAULT_DAMAGE_SIDE_PADDING = 3.0D; // 默认锥形伤害左右额外扩展距离。
    public static final int DEFAULT_FULL_CHARGE_TICKS = 20; // 默认咖喱棒满蓄力 tick。
    public static final int DEFAULT_ENHANCED_CHARGE_TICKS = 20; // 默认咖喱棒增强视觉开始 tick。
    public static final int DEFAULT_COOLDOWN_TICKS = 600; // 默认咖喱棒释放冷却 tick。
    public static final double DEFAULT_END_SHOCKWAVE_WIDTH = 74.0D; // 默认终点冲击波基础直径。
    public static final double DEFAULT_END_SHOCKWAVE_HEIGHT = 250.0D; // 默认终点冲击波基础高度。
    public static ForgeConfigSpec.DoubleValue maxRange; // 最大射程配置。
    public static ForgeConfigSpec.DoubleValue branchDistance; // 最大单侧分叉距离配置。
    public static ForgeConfigSpec.DoubleValue damage; // 每 tick 锥形范围命中伤害配置。
    public static ForgeConfigSpec.DoubleValue damageHeightUp; // 锥形伤害中心线上方高度配置。
    public static ForgeConfigSpec.DoubleValue damageHeightDown; // 锥形伤害中心线下方高度配置。
    public static ForgeConfigSpec.DoubleValue damageSidePadding; // 锥形伤害左右额外扩展配置。
    public static ForgeConfigSpec.IntValue fullChargeTicks; // 咖喱棒满蓄力 tick 配置。
    public static ForgeConfigSpec.IntValue enhancedStartTick; // 咖喱棒增强视觉开始 tick 配置。
    public static ForgeConfigSpec.IntValue cooldownTicks; // 咖喱棒释放服务端冷却 tick 配置。
    public static ForgeConfigSpec.DoubleValue endShockwaveBaseWidth; // 终点冲击波基础直径配置。
    public static ForgeConfigSpec.DoubleValue endShockwaveBaseHeight; // 终点冲击波基础高度配置。

    public ExExcaliburConfig() {
    }

    public static void register(ForgeConfigSpec.Builder builder) {
        builder.comment("EX Excalibur settings.")
                .comment("EX 咖喱棒配置。")
                .push("exExcalibur");
        maxRange = builder
                .comment("Maximum travel range of the EX sword wave in blocks.")
                .comment("EX 剑气中心线的最大前进距离，单位为格。")
                .defineInRange("maxRange", DEFAULT_MAX_RANGE, 1.0D, Double.MAX_VALUE);
        branchDistance = builder
                .comment("Maximum one-side branch distance at the end of the V shape.")
                .comment("EX 剑气到达最大射程时，V 字单侧最大分叉距离。")
                .defineInRange("branchDistance", DEFAULT_BRANCH_DISTANCE, 0.0D, 1000.0D);
        damage = builder
                .comment("Damage dealt by the EX sword wave cone on every tick.")
                .comment("EX 剑气锥形伤害范围每 tick 命中造成的伤害。")
                .defineInRange("damage", DEFAULT_DAMAGE, 0.0D, Double.MAX_VALUE);
        damageHeightUp = builder
                .comment("Vertical damage height above the EX sword wave center line.")
                .comment("EX 剑气锥形伤害范围相对中心线向上的高度。")
                .defineInRange("damageHeightUp", DEFAULT_DAMAGE_HEIGHT_UP, 0.0D, 1000.0D);
        damageHeightDown = builder
                .comment("Vertical damage height below the EX sword wave center line.")
                .comment("EX 剑气锥形伤害范围相对中心线向下的高度。")
                .defineInRange("damageHeightDown", DEFAULT_DAMAGE_HEIGHT_DOWN, 0.0D, 1000.0D);
        damageSidePadding = builder
                .comment("Extra horizontal damage padding on each side of the EX sword wave cone.")
                .comment("EX 剑气锥形伤害范围左右两侧额外扩展距离。")
                .defineInRange("damageSidePadding", DEFAULT_DAMAGE_SIDE_PADDING, 0.0D, 1000.0D);
        fullChargeTicks = builder
                .comment("Ticks required to fully charge Excalibur.")
                .comment("咖喱棒满蓄力所需 tick。")
                .defineInRange("fullChargeTicks", DEFAULT_FULL_CHARGE_TICKS, 1, 1200);
        enhancedStartTick = builder
                .comment("Ticks before Excalibur charge visual enters enhanced stage.")
                .comment("咖喱棒蓄力视觉进入增强阶段所需 tick。")
                .defineInRange("enhancedStartTick", DEFAULT_ENHANCED_CHARGE_TICKS, 0, 1200);
        cooldownTicks = builder
                .comment("Server-side cooldown after successfully casting Excalibur, in ticks.")
                .comment("咖喱棒成功发射后的服务端冷却 tick，只影响服务端是否允许发射剑气。")
                .defineInRange("cooldownTicks", DEFAULT_COOLDOWN_TICKS, 0, 20 * 60 * 60);
        endShockwaveBaseWidth = builder
                .comment("Base diameter of the innermost Excalibur end shockwave cylinder.")
                .comment("咖喱棒终点冲击波最内层圆柱基础直径。")
                .defineInRange("endShockwaveBaseWidth", DEFAULT_END_SHOCKWAVE_WIDTH, 1.0D, 1000.0D);
        endShockwaveBaseHeight = builder
                .comment("Base height of the innermost Excalibur end shockwave cylinder.")
                .comment("咖喱棒终点冲击波最内层圆柱基础高度。")
                .defineInRange("endShockwaveBaseHeight", DEFAULT_END_SHOCKWAVE_HEIGHT, 1.0D, 1000.0D);
        builder.pop();
    }

    public static double maxRange() {
        return safeGet(maxRange, DEFAULT_MAX_RANGE);
    }

    public static double branchDistance() {
        return safeGet(branchDistance, DEFAULT_BRANCH_DISTANCE);
    }

    public static float damage() {
        return (float) safeGet(damage, DEFAULT_DAMAGE);
    }

    public static double damageHeightUp() {
        return safeGet(damageHeightUp, DEFAULT_DAMAGE_HEIGHT_UP);
    }

    public static double damageHeightDown() {
        return safeGet(damageHeightDown, DEFAULT_DAMAGE_HEIGHT_DOWN);
    }

    public static double damageSidePadding() {
        return safeGet(damageSidePadding, DEFAULT_DAMAGE_SIDE_PADDING);
    }

    public static int fullChargeTicks() {
        return safeGet(fullChargeTicks, DEFAULT_FULL_CHARGE_TICKS);
    }

    public static int enhancedStartTick() {
        return safeGet(enhancedStartTick, DEFAULT_ENHANCED_CHARGE_TICKS);
    }

    public static int cooldownTicks() {
        return safeGet(cooldownTicks, DEFAULT_COOLDOWN_TICKS);
    }

    public static float endShockwaveBaseWidth() {
        return (float) safeGet(endShockwaveBaseWidth, DEFAULT_END_SHOCKWAVE_WIDTH);
    }

    public static float endShockwaveBaseHeight() {
        return (float) safeGet(endShockwaveBaseHeight, DEFAULT_END_SHOCKWAVE_HEIGHT);
    }

    // 配置尚未加载时返回默认值，避免早期实体创建读取配置失败。
    public static double safeGet(ForgeConfigSpec.DoubleValue value, double fallback) {
        if (value == null) return fallback;
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    // 配置尚未加载时返回默认值，避免早期实体创建读取配置失败。
    public static int safeGet(ForgeConfigSpec.IntValue value, int fallback) {
        if (value == null) return fallback;
        try {
            return value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }
}
