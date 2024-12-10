package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;

import java.util.List;
import java.util.ArrayList;
//这个效果监听有问题，我离苦力怕很近是他会炸我，而且我碰他他也会自爆，而且我需要他聪明一点，我被攻击后不是原地爆炸，而是去在攻击我的那个生物的地方爆炸
@Mod.EventBusSubscriber
public class CreeperBehaviorHandler {

    // 当玩家受到攻击时，苦力怕会帮助拥有CREEPER_FRIENDLY效果的玩家
    @SubscribeEvent
    public static void onPlayerAttacked(LivingAttackEvent event) {
        // 确保事件的目标是玩家
        if (event.getEntityLiving() instanceof Player player) {
            // 确保玩家拥有CREEPER_FRIENDLY效果
            if (player.hasEffect(ModEffects.CREEPER_FRIENDLY.get())) {
                Entity attacker = event.getSource().getEntity();
                if (attacker instanceof Mob mobAttacker) {
                    // 找到玩家附近的苦力怕
                    List<Creeper> nearbyCreepers = player.level.getEntitiesOfClass(Creeper.class, player.getBoundingBox().inflate(20));

                    // 让附近的苦力怕攻击并引爆在攻击玩家的敌对生物
                    for (Creeper creeper : nearbyCreepers) {
                        // 设置苦力怕的目标为攻击玩家的生物
                        creeper.setTarget(mobAttacker);

                        // 苦力怕靠近攻击者，引爆自己
                        creeper.ignite();
                    }
                }
            }
        }
    }

    // 确保苦力怕的爆炸不会伤害到拥有CREEPER_FRIENDLY效果的玩家
    @SubscribeEvent
    public static void onCreeperExplosion(ExplosionEvent.Detonate event) {
        List<Entity> affectedEntities = event.getAffectedEntities();
        List<Entity> entitiesToRemove = new ArrayList<>(); // 用于存储要移除的实体

        // 检查是否有玩家在爆炸范围内
        for (Entity entity : affectedEntities) {
            if (entity instanceof Player player) {
                // 如果玩家拥有CREEPER_FRIENDLY效果，准备移除玩家
                if (player.hasEffect(ModEffects.CREEPER_FRIENDLY.get())) {
                    entitiesToRemove.add(player);  // 将玩家加入要移除的列表
                }
            }
        }

        // 在迭代结束后，从爆炸影响列表中移除玩家
        affectedEntities.removeAll(entitiesToRemove);
    }
}
