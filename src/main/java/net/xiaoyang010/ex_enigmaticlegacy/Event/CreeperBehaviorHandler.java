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

@Mod.EventBusSubscriber
public class CreeperBehaviorHandler {

    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
        if (event.getEntityLiving() instanceof Creeper) {
            LivingEntity target = event.getNewTarget();
            if (target instanceof Player player &&
                    player.hasEffect(ModEffects.CREEPER_FRIENDLY.get())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerAttacked(LivingAttackEvent event) {
        if (event.getEntityLiving() instanceof Player player) {
            if (player.hasEffect(ModEffects.CREEPER_FRIENDLY.get())) {
                Entity attacker = event.getSource().getEntity();
                if (attacker instanceof LivingEntity && !(attacker instanceof Creeper)) {
                    List<Creeper> nearbyCreepers = player.level.getEntitiesOfClass(
                            Creeper.class,
                            player.getBoundingBox().inflate(20),
                            creeper -> !creeper.isIgnited() &&
                                    creeper.hasLineOfSight(attacker)
                    );

                    for (Creeper creeper : nearbyCreepers) {
                        creeper.setLastHurtByMob((LivingEntity) attacker);
                        creeper.getNavigation().moveTo(attacker.getX(), attacker.getY(), attacker.getZ(), 3D);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCreeperExplosion(ExplosionEvent.Detonate event) {
        if (event.getExplosion().getSourceMob() instanceof Creeper) {
            List<Entity> affectedEntities = event.getAffectedEntities();
            List<Entity> entitiesToRemove = new ArrayList<>();
            boolean hasFriendlyPlayer = false;

            for (Entity entity : affectedEntities) {
                if (entity instanceof Player player &&
                        player.hasEffect(ModEffects.CREEPER_FRIENDLY.get())) {
                    entitiesToRemove.add(player);
                    hasFriendlyPlayer = true;
                }
            }

            if (hasFriendlyPlayer) {
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
                affectedEntities.removeAll(entitiesToRemove);
            }
        }
    }
}