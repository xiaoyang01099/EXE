package net.xiaoyang010.ex_enigmaticlegacy.Effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class DamageReduction extends MobEffect {

    public DamageReduction() {
        super(MobEffectCategory.BENEFICIAL, 0xFF0000);  // 设置为有益效果，颜色为红色
    }

    private static final ResourceLocation DAMAGE_REDUCTION = new ResourceLocation("ex_enigmaticlegacy:textures/mob_effect/damage_reduction.png");

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;  // 不需要每tick生效
    }
}
