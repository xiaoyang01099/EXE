package net.xiaoyang010.ex_enigmaticlegacy.api;

import net.minecraft.world.damagesource.DamageSource;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.creature.BodyPart;

public interface IComplexMob {
    BodyPart[] getParts();
    boolean hurt(BodyPart part, DamageSource source, float amount);
}