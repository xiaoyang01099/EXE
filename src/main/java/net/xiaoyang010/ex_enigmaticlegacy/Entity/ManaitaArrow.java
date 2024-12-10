package net.xiaoyang010.ex_enigmaticlegacy.Entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ManaitaArrow extends AbstractArrow {

    private List<ItemStack> targetDrops = new ArrayList<>(); // 用于存储被击杀目标的掉落物

    public ManaitaArrow(EntityType<? extends ManaitaArrow> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (result.getEntity() instanceof Player)return;

        if (result.getEntity() instanceof LivingEntity target) {
            if (!this.level.isClientSide) {

                // 获取周围所有相同类型的生物
                AABB area = new AABB(target.blockPosition()).inflate(18000);  // 以目标为中心
                List<LivingEntity> nearbyEntities = this.level.getEntitiesOfClass(LivingEntity.class, area, e -> e.getType() == target.getType());

                // 杀死所有相同类型的生物并处理掉落物
                for (LivingEntity entity : nearbyEntities) {
                    if (entity != target) { // 避免重复杀死最初的目标
                        if (entity instanceof EnderMan) {
                            // 禁用末影人的传送能力
                            ((EnderMan) entity).setHealth(0);
                        }
                        entity.hurt(DamageSource.GENERIC, Float.MAX_VALUE); // 使用通用伤害来源
                        invokeDropAllDeathLoot(entity, DamageSource.GENERIC); // 使用反射调用 dropAllDeathLoot
                        entity.die(DamageSource.GENERIC);
                        if (entity instanceof EnderMan) {
                            // 恢复末影人的传送能力
                            ((EnderMan) entity).setNoAi(false);
                        }
                    }
                }

                // 记录初始目标的掉落物
                if (target instanceof EnderMan) {
                    // 禁用末影人的传送能力
                    ((EnderMan) target).setNoAi(true);
                }
                invokeDropAllDeathLoot(target, DamageSource.GENERIC); // 使用反射调用 dropAllDeathLoot
                target.die(DamageSource.GENERIC); // 让目标自然死亡并掉落物品
                if (target instanceof EnderMan) {
                    // 恢复末影人的传送能力
                    ((EnderMan) target).setNoAi(false);
                }
            }
        }
    }


    private void invokeDropAllDeathLoot(LivingEntity entity, DamageSource source) {
        try {
            Method method = LivingEntity.class.getDeclaredMethod("dropAllDeathLoot", DamageSource.class);
            method.setAccessible(true);  // 绕过 protected 访问权限
            method.invoke(entity, source);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        // 返回被击杀目标的第一个掉落物，如果没有掉落物则返回一个空的 ItemStack
        return targetDrops.isEmpty() ? ItemStack.EMPTY : targetDrops.get(0);
    }
}
