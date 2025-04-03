package net.xiaoyang010.ex_enigmaticlegacy.Effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;

import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.UUID;

public class Drowning extends MobEffect {
    private static final HashMap<UUID, Integer> lastHealTick = new HashMap<>();
    private static final int HEAL_COOLDOWN_TICKS = 20;

    public Drowning() {
        super(MobEffectCategory.HARMFUL, 0x1E90FF);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                "7107DE5E-7CE8-4030-940E-514C1F160890",
                -0.35D, // 降低35%移动速度
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                "7107DE5E-7CE8-4030-940E-514C1F160891",
                -0.15D, // 降低15%攻击速度
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    //削弱版
//    @Override
//    public void applyEffectTick(LivingEntity entity, int amplifier) {
//        // 当实体不在水中时执行溺水效果
//        if (!entity.isInWater()) {
//            int currentAir = entity.getAirSupply();
//
//            // 计算空气损失量（基础值3，随等级提高）
//            int airLoss = 3 * (amplifier + 1);
//
//            if (currentAir > -20) {
//                // 减少空气值
//                entity.setAirSupply(currentAir - airLoss);
//
//                // 当空气即将耗尽时造成伤害
//                if (currentAir <= airLoss && currentAir > 0) {
//                    entity.setAirSupply(0);
//                    // 伤害计算：基础伤害3.0，每级增加0.75
//                    float damage = 3.0F + (amplifier * 0.75F);
//                    entity.hurt(ModDamageSources.ABSOLUTE, damage);
//                }
//            }
//        }
//
//        if (entity.getHealth() < entity.getMaxHealth()) {
//            UUID entityId = entity.getUUID();
//            int currentTick = (int) entity.level.getGameTime();
//            Integer lastHeal = lastHealTick.get(entityId);
//
//            if (lastHeal != null && (currentTick - lastHeal) < HEAL_COOLDOWN_TICKS) {
//                float currentHealth = entity.getHealth();
//                if (entity.getHealth() > currentHealth) {
//                    entity.setHealth(currentHealth);
//                }
//            } else {
//                lastHealTick.put(entityId, currentTick);
//            }
//        }
//
//        super.applyEffectTick(entity, amplifier);
//    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.setAirSupply(Math.max(-20, entity.getAirSupply() - (3 * (amplifier + 1))));

        if (entity.getAirSupply() <= 0) {
            float damage = 3.0F + (amplifier * 0.75F);
            entity.hurt(ModDamageSources.ABSOLUTE, damage);
        }

        // 回血限制逻辑
        if (entity.getHealth() < entity.getMaxHealth()) {
            UUID entityId = entity.getUUID();
            int currentTick = (int) entity.level.getGameTime();
            Integer lastHeal = lastHealTick.get(entityId);

            if (lastHeal != null && (currentTick - lastHeal) < HEAL_COOLDOWN_TICKS) {
                float currentHealth = entity.getHealth();
                if (entity.getHealth() > currentHealth) {
                    entity.setHealth(currentHealth);
                }
            } else {
                lastHealTick.put(entityId, currentTick);
            }
        }

        super.applyEffectTick(entity, amplifier);
    }


    @Override
    public boolean isInstantenous() {
        return false;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return Collections.emptyList();
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        cleanupHealingData(entity.getUUID());
    }

    public static void cleanupHealingData(UUID entityId) {
        lastHealTick.remove(entityId);
    }
}