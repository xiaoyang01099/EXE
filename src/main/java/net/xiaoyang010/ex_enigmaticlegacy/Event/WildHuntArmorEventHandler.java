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
    private static final Map<UUID, Set<UUID>> playerAttackedUndeadMap = new HashMap<>();

    public static Map<UUID, Set<UUID>> getPlayerAttackedUndeadMap() {
        return playerAttackedUndeadMap;
    }

    private static boolean isUndead(Entity entity) {
        if (entity instanceof LivingEntity) {
            return ((LivingEntity) entity).getMobType() == MobType.UNDEAD;
        }
        return false;
    }

    private static boolean isMonster(Entity entity) {
        return entity instanceof Monster;
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            long currentTime = System.currentTimeMillis();

            playerAttackedUndeadMap.forEach((playerId, attackedMobs) -> {
                attackedMobs.removeIf(mobId -> {
                    return false;
                });
            });

            playerAttackedUndeadMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isClientSide()) {
            return;
        }

        if (isUndead(event.getEntity()) && isMonster(event.getEntity())) {
            Mob undead = (Mob) event.getEntity();
            AABB searchArea = undead.getBoundingBox().inflate(32.0D);

            for (Player player : event.getWorld().getEntitiesOfClass(Player.class, searchArea)) {
                if (WildHuntArmor.isWearingFullSet(player)) {
                    undead.setTarget(null);
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof Mob)) {
            return;
        }

        Mob mob = (Mob) event.getEntityLiving();

        if (!isUndead(mob)) {
            return;
        }

        Player nearestPlayer = getNearestPlayerWithFullSet(mob);

        if (nearestPlayer != null && !hasPlayerAttackedUndead(nearestPlayer.getUUID(), mob.getUUID())) {
            if (mob.getTarget() == nearestPlayer) {
                mob.setTarget(null);
            }

            findAndAttackEnemies(mob, nearestPlayer);
        }
    }

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

    private static void findAndAttackEnemies(Mob undead, Player player) {
        AABB searchArea = undead.getBoundingBox().inflate(16.0D);

        for (Monster monster : undead.level.getEntitiesOfClass(Monster.class, searchArea)) {
            if (!isUndead(monster) && monster.getTarget() == player) {
                undead.setTarget(monster);

                if (undead instanceof AbstractSkeleton) {
                    undead.setAggressive(true);
                }
                break;
            }
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (isUndead(event.getEntityLiving()) && isMonster(event.getEntityLiving())) {
            LivingEntity undead = event.getEntityLiving();

            if (event.getSource().getEntity() instanceof Player) {
                Player player = (Player) event.getSource().getEntity();

                if (WildHuntArmor.isWearingFullSet(player)) {
                    recordPlayerAttack(player.getUUID(), undead.getUUID());

                    makeUndeadHostile(undead, player);
                }
            }
        }
    }

    private static void recordPlayerAttack(UUID playerId, UUID undeadId) {
        playerAttackedUndeadMap.computeIfAbsent(playerId, k -> new HashSet<>()).add(undeadId);
    }

    private static boolean hasPlayerAttackedUndead(UUID playerId, UUID undeadId) {
        Set<UUID> attackedSet = playerAttackedUndeadMap.get(playerId);
        return attackedSet != null && attackedSet.contains(undeadId);
    }

    private static void makeUndeadHostile(LivingEntity undead, Player player) {
        if (undead instanceof Mob) {
            Mob mobUndead = (Mob) undead;
            mobUndead.setTarget(player);

            if (mobUndead instanceof AbstractSkeleton) {
                mobUndead.setAggressive(true);
            }
        }

        AABB searchArea = undead.getBoundingBox().inflate(16.0D);
        for (Mob nearbyUndead : undead.level.getEntitiesOfClass(Mob.class, searchArea)) {
            if (isUndead(nearbyUndead) && nearbyUndead != undead) {
                recordPlayerAttack(player.getUUID(), nearbyUndead.getUUID());
                nearbyUndead.setTarget(player);

                if (nearbyUndead instanceof AbstractSkeleton) {
                    nearbyUndead.setAggressive(true);
                }
            }
        }
    }
}