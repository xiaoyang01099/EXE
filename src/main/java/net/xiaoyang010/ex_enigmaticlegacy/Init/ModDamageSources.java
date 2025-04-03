package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;


public class ModDamageSources {
    // 绝对伤害 - 无法被任何方式减免
    public static final DamageSource ABSOLUTE = new DamageSource("absolute")
            .bypassArmor()      // 无视护甲
            .bypassMagic()      // 无视魔法保护
            .bypassInvul()      // 无视无敌时间
            .setMagic();        // 设为魔法伤害，这样不会被火焰保护等减免

    // 带有攻击者的版本
    public static DamageSource causeAbsoluteDamage(Entity attacker) {
        return new EntityDamageSource("absolute", attacker)
                .bypassArmor()
                .bypassMagic()
                .bypassInvul()
                .setMagic();
    }
}