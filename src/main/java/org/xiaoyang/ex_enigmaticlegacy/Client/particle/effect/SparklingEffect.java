package org.xiaoyang.ex_enigmaticlegacy.Client.particle.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class SparklingEffect extends MobEffect {
    public static final int COLOR = 0xFFFF44;

    public SparklingEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }
}