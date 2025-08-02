package net.xiaoyang010.ex_enigmaticlegacy.Effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FreezeWorst extends MobEffect {
    private static final Map<UUID, Boolean> originalFlightStates = new HashMap<>();

    private static final UUID FREEZE_SPEED_UUID = UUID.fromString("7107DE5E-7CE8-4030-940E-514C1F160890");
    private static final UUID FREEZE_KNOCKBACK_UUID = UUID.fromString("7207DE5E-7CE8-4030-940E-514C1F160891");
    private static final UUID FREEZE_FLYING_UUID = UUID.fromString("7307DE5E-7CE8-4030-940E-514C1F160892");
    private static final UUID FREEZE_JUMP_UUID = UUID.fromString("7407DE5E-7CE8-4030-940E-514C1F160893");

    public FreezeWorst() {
        super(MobEffectCategory.HARMFUL, 0x4A90E2);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        double speedReduction = calculateSpeedReduction(amplifier);
        double knockbackResistance = calculateKnockbackResistance(amplifier);
        double flyingSpeedReduction = calculateFlyingSpeedReduction(amplifier);
        double jumpReduction = calculateJumpReduction(amplifier);

        AttributeInstance movementAttribute = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (movementAttribute != null) {
            movementAttribute.removeModifier(FREEZE_SPEED_UUID);
            AttributeModifier speedModifier = new AttributeModifier(
                    FREEZE_SPEED_UUID,
                    "Freeze movement effect",
                    speedReduction,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            movementAttribute.addTransientModifier(speedModifier);
        }

        AttributeInstance jumpAttribute = attributeMap.getInstance(Attributes.JUMP_STRENGTH);
        if (jumpAttribute != null) {
            jumpAttribute.removeModifier(FREEZE_JUMP_UUID);
            AttributeModifier jumpModifier = new AttributeModifier(
                    FREEZE_JUMP_UUID,
                    "Freeze jump effect",
                    jumpReduction,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            jumpAttribute.addTransientModifier(jumpModifier);
        }

        AttributeInstance flyingAttribute = attributeMap.getInstance(Attributes.FLYING_SPEED);
        if (flyingAttribute != null) {
            flyingAttribute.removeModifier(FREEZE_FLYING_UUID);
            AttributeModifier flyingModifier = new AttributeModifier(
                    FREEZE_FLYING_UUID,
                    "Freeze flying effect",
                    flyingSpeedReduction,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            flyingAttribute.addTransientModifier(flyingModifier);
        }

        AttributeInstance knockbackAttribute = attributeMap.getInstance(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackAttribute != null) {
            knockbackAttribute.removeModifier(FREEZE_KNOCKBACK_UUID);
            AttributeModifier knockbackModifier = new AttributeModifier(
                    FREEZE_KNOCKBACK_UUID,
                    "Freeze knockback resistance",
                    knockbackResistance,
                    AttributeModifier.Operation.ADDITION
            );
            knockbackAttribute.addTransientModifier(knockbackModifier);
        }

        super.addAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        AttributeInstance movementAttribute = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (movementAttribute != null) {
            movementAttribute.removeModifier(FREEZE_SPEED_UUID);
        }

        AttributeInstance jumpAttribute = attributeMap.getInstance(Attributes.JUMP_STRENGTH);
        if (jumpAttribute != null) {
            jumpAttribute.removeModifier(FREEZE_JUMP_UUID);
        }

        AttributeInstance flyingAttribute = attributeMap.getInstance(Attributes.FLYING_SPEED);
        if (flyingAttribute != null) {
            flyingAttribute.removeModifier(FREEZE_FLYING_UUID);
        }

        AttributeInstance knockbackAttribute = attributeMap.getInstance(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackAttribute != null) {
            knockbackAttribute.removeModifier(FREEZE_KNOCKBACK_UUID);
        }

        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (amplifier >= 2) {
                Boolean hadFlight = originalFlightStates.remove(player.getUUID());
                if (hadFlight != null && hadFlight || player.isCreative()) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
            }
        }

        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level.isClientSide) {
            handleFlightDisabling(entity, amplifier);
            handleJumpControl(entity, amplifier);
            handleMovementControl(entity, amplifier);
            handleCreativeMode(entity, amplifier);
            handleVisualEffects(entity, amplifier);
        }

        super.applyEffectTick(entity, amplifier);
    }

    private void handleFlightDisabling(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && amplifier >= 1) {
            if (player.getAbilities().flying) {
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    private void handleJumpControl(LivingEntity entity, int amplifier) {
        if (amplifier >= 2) {
            if (entity.getDeltaMovement().y > 0.0D) {
                if (amplifier >= 4) {
                    double downwardForce = amplifier >= 5 ? -0.1D : -0.05D;
                    entity.setDeltaMovement(
                            entity.getDeltaMovement().x,
                            downwardForce,
                            entity.getDeltaMovement().z
                    );
                } else {
                    entity.setDeltaMovement(
                            entity.getDeltaMovement().x,
                            0.0D,
                            entity.getDeltaMovement().z
                    );
                }
            }

            if (entity.tickCount % 5 == 0 && amplifier >= 5) {
                Vec3 currentMovement = entity.getDeltaMovement();
                entity.setDeltaMovement(currentMovement.x, currentMovement.y - 0.02D, currentMovement.z);
            }
        } else if (amplifier >= 1) {
            if (entity.getDeltaMovement().y > 0.0D) {
                double jumpReductionFactor = 0.1D;
                entity.setDeltaMovement(
                        entity.getDeltaMovement().x,
                        entity.getDeltaMovement().y * jumpReductionFactor,
                        entity.getDeltaMovement().z
                );
            }
        }
    }

    private void handleMovementControl(LivingEntity entity, int amplifier) {
        if (amplifier >= 4) {
            double reverseStrength = 0.02D * (amplifier - 3);
            entity.setDeltaMovement(
                    -entity.getLookAngle().x * reverseStrength,
                    entity.getDeltaMovement().y - 0.01D,
                    -entity.getLookAngle().z * reverseStrength
            );
        } else if (amplifier >= 2) {
            entity.setDeltaMovement(
                    0,
                    Math.min(entity.getDeltaMovement().y, 0),
                    0
            );
        } else if (amplifier >= 1) {
            entity.setDeltaMovement(
                    entity.getDeltaMovement().x * 0.1D,
                    entity.getDeltaMovement().y * 0.5D,
                    entity.getDeltaMovement().z * 0.1D
            );
        }
    }

    private void handleCreativeMode(LivingEntity entity, int amplifier) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (amplifier >= 2 && player.getAbilities().mayfly) {
                originalFlightStates.put(player.getUUID(), true);

                player.getAbilities().flying = false;
                player.getAbilities().mayfly = false;
                player.onUpdateAbilities();
            }
        }
    }

    private void handleVisualEffects(LivingEntity entity, int amplifier) {
        float particleChance = 0.05F + (amplifier * 0.03F);
        if (entity.level.getRandom().nextFloat() < particleChance) {
            // 这里可以添加粒子效果
            // entity.level.addParticle(ParticleTypes.SNOWFLAKE,
            //     entity.getX(), entity.getY() + 0.5D, entity.getZ(),
            //     0.0D, 0.0D, 0.0D);

            if (amplifier >= 4 && entity.level.getRandom().nextFloat() < 0.02F) {
            }
        }
    }

    private double calculateSpeedReduction(int amplifier) {
        switch (amplifier) {
            case 0: return -0.3D;
            case 1: return -0.6D;
            case 2: return -0.8D;
            case 3: return -1.0D;
            case 4: return -1.2D;
            case 5: return -1.5D;
            default: return -2.0D;
        }
    }

    private double calculateFlyingSpeedReduction(int amplifier) {
        switch (amplifier) {
            case 0: return -0.5D;
            case 1: return -0.8D;
            case 2: return -1.0D;
            case 3: return -1.3D;
            case 4: return -1.6D;
            default: return -2.0D;
        }
    }

    private double calculateJumpReduction(int amplifier) {
        switch (amplifier) {
            case 0: return -0.7D;
            case 1: return -0.9D;
            case 2: return -1.0D;
            case 3: return -1.0D;
            case 4: return -1.0D;
            default: return -1.0D;
        }
    }

    private double calculateKnockbackResistance(int amplifier) {
        return Math.min(1.0D, 0.3D + (amplifier * 0.15D));
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean isInstantenous() {
        return false;
    }
}