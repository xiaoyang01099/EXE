package net.xiaoyang010.ex_enigmaticlegacy.Effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class CreeperFriendly extends MobEffect {

    public CreeperFriendly() {
        super(MobEffectCategory.BENEFICIAL, 0x00FF00); // 设置效果为绿色并标记为有益效果
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false; // 不需要在每个 tick 中做任何事情
    }
}
