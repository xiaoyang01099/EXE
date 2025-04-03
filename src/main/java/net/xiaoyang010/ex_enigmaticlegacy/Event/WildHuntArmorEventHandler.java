package net.xiaoyang010.ex_enigmaticlegacy.Event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.WildHuntArmor;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class WildHuntArmorEventHandler {

    // 存储玩家攻击过的亡灵生物
    private static final Map<UUID, Set<UUID>> playerAttackedUndeadMap = new HashMap<>();
    // 攻击记录的过期时间（毫秒）
    private static final long ATTACK_RECORD_EXPIRY = 30000; // 1分钟

    // 提供一个方法让其他类可以获取这个映射
    public static Map<UUID, Set<UUID>> getPlayerAttackedUndeadMap() {
        return playerAttackedUndeadMap;
    }

    // 检查实体是否为亡灵生物
    private static boolean isUndead(Entity entity) {
        if (entity instanceof LivingEntity) {
            return ((LivingEntity) entity).getMobType() == MobType.UNDEAD;
        }
        return false;
    }

    // 检查实体是否是Monster类型
    private static boolean isMonster(Entity entity) {
        return entity instanceof Monster;
    }

    // 清理过期的攻击记录
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            long currentTime = System.currentTimeMillis();

            // 清理过期的攻击记录
            playerAttackedUndeadMap.forEach((playerId, attackedMobs) -> {
                attackedMobs.removeIf(mobId -> {
                    // 这里我们需要有一个存储时间的方式
                    // 简化实现：我们暂时不实现过期清理
                    return false;
                });
            });

            // 移除空集合
            playerAttackedUndeadMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }
    }

    // 处理实体加入世界的事件
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isClientSide()) {
            return;
        }

        if (isUndead(event.getEntity()) && isMonster(event.getEntity())) {
            // 检查附近是否有穿戴全套野猎护甲的玩家
            Mob undead = (Mob) event.getEntity();
            AABB searchArea = undead.getBoundingBox().inflate(32.0D);

            for (Player player : event.getWorld().getEntitiesOfClass(Player.class, searchArea)) {
                if (WildHuntArmor.isWearingFullSet(player)) {
                    // 初始状态为友好
                    undead.setTarget(null);
                    break;
                }
            }
        }
    }

    // 使用LivingEvent.LivingUpdateEvent来处理生物每帧更新
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof Mob)) {
            return;
        }

        Mob mob = (Mob) event.getEntityLiving();

        // 只处理亡灵生物
        if (!isUndead(mob)) {
            return;
        }

        // 获取最近的玩家
        Player nearestPlayer = getNearestPlayerWithFullSet(mob);

        // 如果找到了穿全套野猎护甲的玩家
        if (nearestPlayer != null && !hasPlayerAttackedUndead(nearestPlayer.getUUID(), mob.getUUID())) {
            // 清除目标
            if (mob.getTarget() == nearestPlayer) {
                mob.setTarget(null);
            }

            // 尝试让亡灵生物攻击附近敌对的怪物（非亡灵）
            findAndAttackEnemies(mob, nearestPlayer);
        }
    }

    // 获取最近的穿戴全套野猎护甲的玩家
    private static Player getNearestPlayerWithFullSet(Mob mob) {
        AABB searchArea = mob.getBoundingBox().inflate(32.0D);

        Player nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player player : mob.level.getEntitiesOfClass(Player.class, searchArea)) {
            if (WildHuntArmor.isWearingFullSet(player)) {
                double distance = mob.distanceToSqr(player);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestPlayer = player;
                }
            }
        }

        return nearestPlayer;
    }

    // 让亡灵生物攻击附近的敌对生物
    private static void findAndAttackEnemies(Mob undead, Player player) {
        AABB searchArea = undead.getBoundingBox().inflate(16.0D);

        for (Monster monster : undead.level.getEntitiesOfClass(Monster.class, searchArea)) {
            // 如果是敌对生物但不是亡灵，且正在攻击玩家
            if (!isUndead(monster) && monster.getTarget() == player) {
                undead.setTarget(monster);

                // 对于骷髅，确保它们变为激进状态来攻击其他怪物
                if (undead instanceof AbstractSkeleton) {
                    ((AbstractSkeleton) undead).setAggressive(true);
                }
                break;
            }
        }
    }

    // 处理玩家攻击亡灵生物的事件
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        // 如果被攻击的是亡灵生物
        if (isUndead(event.getEntityLiving()) && isMonster(event.getEntityLiving())) {
            LivingEntity undead = event.getEntityLiving();

            // 如果攻击者是玩家
            if (event.getSource().getEntity() instanceof Player) {
                Player player = (Player) event.getSource().getEntity();

                // 如果玩家穿着全套野猎护甲，记录这次攻击
                if (WildHuntArmor.isWearingFullSet(player)) {
                    recordPlayerAttack(player.getUUID(), undead.getUUID());

                    // 使该亡灵生物及附近的亡灵生物对玩家敌对
                    makeUndeadHostile(undead, player);
                }
            }
        }
    }

    // 记录玩家对亡灵生物的攻击
    private static void recordPlayerAttack(UUID playerId, UUID undeadId) {
        playerAttackedUndeadMap.computeIfAbsent(playerId, k -> new HashSet<>()).add(undeadId);
    }

    // 检查玩家是否攻击过某亡灵生物
    private static boolean hasPlayerAttackedUndead(UUID playerId, UUID undeadId) {
        Set<UUID> attackedSet = playerAttackedUndeadMap.get(playerId);
        return attackedSet != null && attackedSet.contains(undeadId);
    }

    // 使亡灵生物对玩家敌对
    private static void makeUndeadHostile(LivingEntity undead, Player player) {
        // 使该亡灵生物对玩家敌对
        if (undead instanceof Mob) {
            Mob mobUndead = (Mob) undead;
            mobUndead.setTarget(player);

            // 对于骷髅，确保它们变为激进状态
            if (mobUndead instanceof AbstractSkeleton) {
                ((AbstractSkeleton) mobUndead).setAggressive(true);
            }
        }

        // 使附近的亡灵生物也对玩家敌对
        AABB searchArea = undead.getBoundingBox().inflate(16.0D);
        for (Mob nearbyUndead : undead.level.getEntitiesOfClass(Mob.class, searchArea)) {
            if (isUndead(nearbyUndead) && nearbyUndead != undead) {
                // 记录这些亡灵生物也被玩家"攻击"过
                recordPlayerAttack(player.getUUID(), nearbyUndead.getUUID());
                nearbyUndead.setTarget(player);

                // 对于骷髅，确保它们变为激进状态
                if (nearbyUndead instanceof AbstractSkeleton) {
                    ((AbstractSkeleton) nearbyUndead).setAggressive(true);
                }
            }
        }
    }
}