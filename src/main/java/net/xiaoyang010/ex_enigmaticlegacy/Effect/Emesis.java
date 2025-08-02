package net.xiaoyang010.ex_enigmaticlegacy.Effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;

import java.util.Random;
import java.util.UUID;

public class Emesis extends MobEffect {

    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("7107DE5E-7CE8-4030-940E-514C1F160890");
    private final Random random = new Random();

    public Emesis() {
        super(MobEffectCategory.HARMFUL, 0x98D982);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        double slowdownAmount = Math.min(-0.98, -0.2 - (amplifier * 0.15));

        AttributeModifier speedModifier = new AttributeModifier(
                SPEED_MODIFIER_UUID,
                "Emesis movement slowdown",
                slowdownAmount,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        if (entity.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            entity.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(speedModifier);
        }
        super.addAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (entity.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            entity.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_UUID);
        }

        if (entity instanceof Player player) {
            player.getPersistentData().remove("emesis_damage_dealt");
        }

        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            if (!player.level.isClientSide) {
                if (!player.getPersistentData().getBoolean("emesis_damage_dealt")) {
                    float damage = 18.0f + (amplifier * 4.0f);
                    player.hurt(ModDamageSources.ABSOLUTE, damage);
                    player.getPersistentData().putBoolean("emesis_damage_dealt", true);
                }
            }

            if (player.level.isClientSide) {
                applyViewShaking(player, amplifier);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void applyViewShaking(Player player, int amplifier) {
        float intensityMultiplier = 1.0f + (amplifier * 0.5f);

        float yawChange = (random.nextFloat() - 0.5f) * 8.0f * intensityMultiplier;
        float pitchChange = (random.nextFloat() - 0.5f) * 4.0f * intensityMultiplier;

        player.setYRot(player.getYRot() + yawChange);
        player.setXRot(Math.max(-90.0f, Math.min(90.0f, player.getXRot() + pitchChange)));
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}