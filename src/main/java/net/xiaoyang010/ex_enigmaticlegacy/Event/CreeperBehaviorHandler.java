package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;

import java.util.List;
import java.util.ArrayList;

/**
 * 处理苦力怕行为的事件处理器
 * 包含以下功能：
 * 1. 防止苦力怕攻击带有友好效果的玩家
 * 2. 当玩家受到攻击时，附近的苦力怕会保护玩家
 * 3. 增强苦力怕爆炸伤害，同时保护带有效果的玩家
 */
@Mod.EventBusSubscriber
public class CreeperBehaviorHandler {

    /**
     * 处理实体目标改变事件
     * 防止苦力怕将带有友好效果的玩家设为目标
     */
    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
        if (event.getEntityLiving() instanceof Creeper) {  // 只检查苦力怕
            LivingEntity target = event.getNewTarget();
            // 检查目标是否为带有效果的玩家
            if (target instanceof Player player &&
                    player.hasEffect(ModEffects.CREEPER_FRIENDLY.get())) {
                event.setCanceled(true); // 取消设置玩家为目标
            }
        }
    }

    /**
     * 处理玩家受到攻击的事件
     * 当带有效果的玩家受到任何攻击时，附近的苦力怕会进行反击
     */
    @SubscribeEvent
    public static void onPlayerAttacked(LivingAttackEvent event) {
        if (event.getEntityLiving() instanceof Player player) {
            if (player.hasEffect(ModEffects.CREEPER_FRIENDLY.get())) {
                Entity attacker = event.getSource().getEntity();
                // 检查攻击者是否为有效实体
                if (attacker instanceof LivingEntity && !(attacker instanceof Creeper)) {
                    // 寻找附近20格范围内的苦力怕
                    List<Creeper> nearbyCreepers = player.level.getEntitiesOfClass(
                            Creeper.class,
                            player.getBoundingBox().inflate(20),
                            creeper -> !creeper.isIgnited() &&  // 确保苦力怕未被点燃
                                    creeper.hasLineOfSight(attacker)  // 确保苦力怕能看见攻击者
                    );

                    // 让每个找到的苦力怕追击攻击者
                    for (Creeper creeper : nearbyCreepers) {
                        creeper.setLastHurtByMob((LivingEntity)attacker); // 设置攻击目标
                        creeper.getNavigation().moveTo(attacker.getX(), attacker.getY(), attacker.getZ(), 1.2D); // 以1.2倍速度追击
                    }
                }
            }
        }
    }

    /**
     * 处理苦力怕爆炸事件
     * 增加爆炸伤害为50倍
     * 同时保护带有友好效果的玩家免受爆炸伤害
     */
    @SubscribeEvent
    public static void onCreeperExplosion(ExplosionEvent.Detonate event) {
        if (event.getExplosion().getSourceMob() instanceof Creeper) {
            List<Entity> affectedEntities = event.getAffectedEntities();
            List<Entity> entitiesToRemove = new ArrayList<>();

            // 找出需要保护的玩家
            for (Entity entity : affectedEntities) {
                if (entity instanceof Player player &&
                        player.hasEffect(ModEffects.CREEPER_FRIENDLY.get())) {
                    entitiesToRemove.add(player);
                }
            }

            // 增强爆炸伤害
            for (Entity entity : affectedEntities) {
                if (!(entity instanceof Player player &&
                        player.hasEffect(ModEffects.CREEPER_FRIENDLY.get()))) {
                    double dx = entity.getX() - event.getExplosion().getPosition().x;
                    double dy = entity.getY() - event.getExplosion().getPosition().y;
                    double dz = entity.getZ() - event.getExplosion().getPosition().z;
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (distance != 0) {
                        dx /= distance;
                        dy /= distance;
                        dz /= distance;
                        entity.setDeltaMovement(entity.getDeltaMovement().add(
                                dx * 50.0,
                                dy * 50.0,
                                dz * 50.0
                        ));
                        if (entity instanceof LivingEntity living) {
                            living.hurt(event.getExplosion().getDamageSource(), 50.0F);
                        }
                    }
                }
            }

            // 将受保护的玩家从爆炸影响列表中移除
            affectedEntities.removeAll(entitiesToRemove);
        }
    }
}