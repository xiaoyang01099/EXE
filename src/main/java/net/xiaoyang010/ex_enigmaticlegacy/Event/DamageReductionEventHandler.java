package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;

public class DamageReductionEventHandler {

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntityLiving();
        MobEffectInstance effect = entity.getEffect(ModEffects.DAMAGE_REDUCTION.get());

        if (effect != null) {
            event.setAmount(event.getAmount() * 0.01F);
        }
    }
}
