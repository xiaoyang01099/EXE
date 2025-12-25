package net.xiaoyang010.ex_enigmaticlegacy.Effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class Drowning extends MobEffect {
    private static final HashMap<UUID, Float> entityLastHealthMap = new HashMap<>();
    private static final int HEAL_COOLDOWN_TICKS = 20;
    private static final HashMap<UUID, Integer> healCooldowns = new HashMap<>();

    public Drowning() {
        super(MobEffectCategory.BENEFICIAL, 0x1E90FF);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                "7107DE5E-7CE8-4030-940E-514C1F160890",
                -0.35D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                "7107DE5E-7CE8-4030-940E-514C1F160891",
                -0.15D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entityLastHealthMap.put(entity.getUUID(), entity.getHealth());
        entity.setAirSupply(Math.max(-20, entity.getAirSupply() - (3 * (amplifier + 1))));

        if (entity.getAirSupply() <= 0) {
            float damage = 3.0F + (amplifier * 0.75F);
            entity.hurt(ModDamageSources.ABSOLUTE, damage);
        }

        UUID entityId = entity.getUUID();
        int currentTick = (int) entity.level.getGameTime();

        healCooldowns.compute(entityId, (id, cooldown) -> {
            if (cooldown == null || cooldown <= 0) {
                return null;
            }
            return cooldown - 1;
        });

        super.applyEffectTick(entity, amplifier);
    }

    @SubscribeEvent
    public static void onEntityHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntityLiving();

        if (entity.hasEffect(ModEffects.DROWNING.get())) {
            UUID entityId = entity.getUUID();
            Integer cooldown = healCooldowns.get(entityId);

            if (cooldown != null && cooldown > 0) {
                event.setCanceled(true);
                return;
            }

            healCooldowns.put(entityId, HEAL_COOLDOWN_TICKS);
        }
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
        cleanupEntityData(entity.getUUID());
    }

    public static void cleanupEntityData(UUID entityId) {
        entityLastHealthMap.remove(entityId);
        healCooldowns.remove(entityId);
    }
}