package net.xiaoyang010.ex_enigmaticlegacy.api.test.res;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * 魔力污染管理器
 */
public class ManaCorruptionManager {

    public static final int MAX_CORRUPTION = 100;
    public static final int CORRUPTION_SPREAD_RANGE = 16;

    /**
     * 污染等级枚举
     */
    public enum CorruptionLevel {
        NONE(0, 0),           // 无污染
        LOW(1, 25),           // 轻度污染
        MEDIUM(26, 50),       // 中度污染
        HIGH(51, 75),         // 重度污染
        EXTREME(76, 100);     // 极度污染

        public final int min;
        public final int max;

        CorruptionLevel(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public static CorruptionLevel fromValue(int corruption) {
            for (CorruptionLevel level : values()) {
                if (corruption >= level.min && corruption <= level.max) {
                    return level;
                }
            }
            return NONE;
        }
    }

    /**
     * 应用污染效果到附近的生物
     */
    public static void applyCorruptionEffects(Level level, BlockPos pos, int corruptionLevel) {
        if (level.isClientSide) return;

        CorruptionLevel level_enum = CorruptionLevel.fromValue(corruptionLevel);
        if (level_enum == CorruptionLevel.NONE) return;

        AABB bounds = new AABB(pos).inflate(CORRUPTION_SPREAD_RANGE);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, bounds);

        for (LivingEntity entity : entities) {
            applyCorruptionToEntity(entity, level_enum);
        }
    }

    /**
     * 对单个实体应用污染效果
     */
    private static void applyCorruptionToEntity(LivingEntity entity, CorruptionLevel level) {
        switch (level) {
            case LOW:
                // 轻度污染：虚弱 I
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, false));
                break;

            case MEDIUM:
                // 中度污染：虚弱 II + 缓慢 I
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0, false, false));
                break;

            case HIGH:
                // 重度污染：虚弱 III + 缓慢 II + 挖掘疲劳 I
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 0, false, false));
                break;

            case EXTREME:
                // 极度污染：虚弱 IV + 缓慢 III + 挖掘疲劳 II + 凋零 I
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 3, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 1, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0, false, false));
                break;
        }
    }

    /**
     * 计算污染扩散
     * @return 扩散到周围的污染量
     */
    public static int calculateCorruptionSpread(int currentCorruption) {
        if (currentCorruption < 25) return 0;
        if (currentCorruption < 50) return 1;
        if (currentCorruption < 75) return 2;
        return 3;
    }

    /**
     * 污染会影响魔力池的容量
     */
    public static int getCorruptionCapacityPenalty(int corruption) {
        return (int) (corruption * 1000); // 每点污染减少1000魔力容量
    }

    /**
     * 污染会影响魔力传输效率
     */
    public static float getCorruptionEfficiencyPenalty(int corruption) {
        return 1.0f - (corruption / 200.0f); // 最多减少50%效率
    }
}
