package net.xiaoyang010.ex_enigmaticlegacy.Entity.biological;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;

public class SpectriteWither extends WitherBoss {

    public SpectriteWither(EntityType<? extends WitherBoss> entityType, Level level) {
        super(entityType, level);
        this.setHealth(this.getMaxHealth()); // 设置最大生命值
    }

    public static AttributeSupplier.Builder createAttributes() {
        return WitherBoss.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30000.0D)
                .add(Attributes.ATTACK_DAMAGE, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.2D)
                .add(Attributes.FLYING_SPEED, 1.5D);
    }

    // 自定义行为：攻击敌对生物，不攻击玩家
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.removeAllGoals();
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    // 检查是否与玩家结盟
    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof Player) {
            return true;
        }
        return super.isAlliedTo(entity);
    }

    // 不对玩家造成伤害，但允许玩家攻击
    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof Player) {
            return super.hurt(source, amount);
        }
        return super.hurt(source, amount);
    }

    // 不掉落物品
    @Override
    public void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        // 覆盖掉落逻辑，取消掉落
    }
}
