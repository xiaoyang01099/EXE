//package org.xiaoyang.ex_enigmaticlegacy.Item.weapon.WIP;
//
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.world.damagesource.DamageSource;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.Mob;
//import net.minecraft.world.entity.ai.attributes.Attributes;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.SwordItem;
//import net.minecraft.world.level.Level;
//import net.minecraftforge.event.entity.living.LivingAttackEvent;
//import net.minecraftforge.event.entity.living.LivingDeathEvent;
//import net.minecraftforge.event.entity.living.LivingHurtEvent;
//import net.minecraftforge.eventbus.api.EventPriority;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import org.xiaoyang.ex_enigmaticlegacy.Exe;
//
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.UUID;
//
//@Mod.EventBusSubscriber(modid = Exe.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
//public class BladeFallenStar extends SwordItem {
//    private static final Map<UUID, YuhuaData> YUHUA_ENTITIES = new HashMap<>();
//    private static final int   MAX_HITS        = 3;
//    private static final float YUHUA_THRESHOLD = 0.25f;
//    private static final int   YUHUA_DURATION  = 40; // 2秒
//
//    private static class YuhuaData {
//        int hitCount;
//        boolean pendingDeath;
//        int ticksAlive;
//
//        YuhuaData(int hitCount, boolean pendingDeath) {
//            this.hitCount     = hitCount;
//            this.pendingDeath = pendingDeath;
//            this.ticksAlive   = 0;
//        }
//    }
//
//    public BladeFallenStar() {
//        super(EXEAPI.MIRACLE_ITEM_TIER, 120, -2.4F,
//                new Properties()
//                        .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
//                        .rarity(ModRarities.MIRACLE));
//    }
//
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void onLivingAttack(LivingAttackEvent event) {
//        LivingEntity target = event.getEntityLiving();
//        if (target.level.isClientSide()) return;
//
//        UUID uuid = target.getUUID();
//        if (!YUHUA_ENTITIES.containsKey(uuid)) return;
//
//        YuhuaData data = YUHUA_ENTITIES.get(uuid);
//
//        Entity source = event.getSource().getEntity();
//        if (source instanceof LivingEntity attacker) {
//            ItemStack weapon = attacker.getMainHandItem();
//            if (weapon.getItem() instanceof BladeFallenStar) {
//                return;
//            }
//        }
//        event.setCanceled(true);
//    }
//
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void onLivingHurt(LivingHurtEvent event) {
//        LivingEntity target = event.getEntityLiving();
//        if (target.level.isClientSide()) return;
//
//        UUID uuid = target.getUUID();
//        if (!YUHUA_ENTITIES.containsKey(uuid)) return;
//
//        event.setCanceled(true);
//    }
//
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void onLivingDeath(LivingDeathEvent event) {
//        LivingEntity target = event.getEntityLiving();
//        if (target.level.isClientSide()) return;
//
//        UUID uuid = target.getUUID();
//        if (!YUHUA_ENTITIES.containsKey(uuid)) return;
//
//        event.setCanceled(true);
//        target.setHealth(1.0f);
//    }
//
//    @Override
//    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
//        if (attacker.level.isClientSide()) {
//            return super.hurtEnemy(stack, target, attacker);
//        }
//
//        UUID uuid = target.getUUID();
//
//        if (YUHUA_ENTITIES.containsKey(uuid)) {
//            YuhuaData data = YUHUA_ENTITIES.get(uuid);
//            data.hitCount++;
//
//            if (data.hitCount >= MAX_HITS) {
//                executeYuhuaKill(uuid, target);
//            } else {
//                NetworkHandler.sendToTrackingEntity(
//                        new PacketYuhuaMark(uuid, data.hitCount, MAX_HITS), target
//                );
//                freezeEntity(target);
//                data.ticksAlive = 0;
//            }
//
//            return true;
//        }
//
//        float maxHp      = (float) target.getAttributeValue(Attributes.MAX_HEALTH);
//        float curHp      = target.getHealth();
//        float atkDmg     = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
//        float hpAfterHit = curHp - atkDmg;
//
//        boolean willDie     = hpAfterHit <= 0;
//        boolean inThreshold = (curHp / maxHp) <= YUHUA_THRESHOLD;
//
//        if (inThreshold && willDie) {
//            target.setHealth(1.0f);
//            target.setDeltaMovement(0, 0, 0);
//            target.hurtTime = 0;
//            target.invulnerableTime = 0;
//            freezeEntity(target);
//
//            YUHUA_ENTITIES.put(uuid, new YuhuaData(0, true));
//
//            NetworkHandler.sendToTrackingEntity(
//                    new PacketYuhuaMark(uuid, 0, MAX_HITS), target
//            );
//
//            return true;
//        }
//
//        return super.hurtEnemy(stack, target, attacker);
//    }
//
//
//    private static void executeYuhuaKill(UUID uuid, LivingEntity target) {
//        NetworkHandler.sendToTrackingEntity(
//                new PacketYuhuaBreak(uuid), target
//        );
//
//        YUHUA_ENTITIES.remove(uuid);
//
//        if (target instanceof Mob mob) {
//            mob.setNoAi(false);
//        }
//        target.setTicksFrozen(0);
//
//        target.invulnerableTime = 0;
//        target.hurtTime = 0;
//        target.setHealth(0);
//        target.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
//
//        if (target.isAlive()) {
//            target.setHealth(0);
//            target.kill();
//        }
//        if (!target.isRemoved()) {
//            target.discard();
//        }
//    }
//
//    public static void freezeEntity(LivingEntity entity) {
//        entity.setTicksFrozen(400);
//        entity.setDeltaMovement(0, 0, 0);
//
//        if (entity instanceof Mob mob) {
//            mob.setTarget(null);
//            mob.getNavigation().stop();
//            mob.setNoAi(true);
//        }
//
//        entity.hurtMarked = false;
//    }
//
//    public static void tickYuhuaEntities(Level level) {
//        if (level.isClientSide()) return;
//        if (!(level instanceof ServerLevel serverLevel)) return;
//
//        Iterator<Map.Entry<UUID, YuhuaData>> it = YUHUA_ENTITIES.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry<UUID, YuhuaData> entry = it.next();
//            UUID uuid = entry.getKey();
//            YuhuaData data = entry.getValue();
//
//            Entity e = serverLevel.getEntity(uuid);
//
//            if (e == null || e.isRemoved()) {
//                it.remove();
//                continue;
//            }
//
//            if (e instanceof LivingEntity living) {
//                data.ticksAlive++;
//
//                if (data.ticksAlive >= YUHUA_DURATION) {
//                    it.remove();
//
//                    NetworkHandler.sendToTrackingEntity(
//                            new PacketYuhuaBreak(uuid), living
//                    );
//
//                    if (living instanceof Mob mob) {
//                        mob.setNoAi(false);
//                    }
//                    living.setTicksFrozen(0);
//                    living.invulnerableTime = 0;
//                    living.hurtTime = 0;
//
//                    living.setHealth(0);
//                    living.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
//
//                    if (living.isAlive()) {
//                        living.setHealth(0);
//                        living.kill();
//                    }
//                    if (!living.isRemoved()) {
//                        living.discard();
//                    }
//
//                    continue;
//                }
//
//                living.deathTime = 0;
//
//                if (living.getHealth() <= 0) {
//                    living.setHealth(1.0f);
//                }
//
//                living.setTicksFrozen(400);
//                living.setDeltaMovement(0, 0, 0);
//
//                if (living instanceof Mob mob) {
//                    mob.setTarget(null);
//                    mob.getNavigation().stop();
//                    mob.setNoAi(true);
//                }
//
//                living.hurtTime = 0;
//                living.invulnerableTime = 0;
//                living.hurtMarked = false;
//
//                living.yBodyRot = living.yBodyRotO;
//                living.yHeadRot = living.yHeadRotO;
//                living.setYRot(living.yRotO);
//                living.setXRot(living.xRotO);
//                living.animationSpeed = 0;
//                living.animationSpeedOld = 0;
//                living.animationPosition = 0;
//            }
//        }
//    }
//
//    public static boolean isYuhua(UUID uuid) {
//        return YUHUA_ENTITIES.containsKey(uuid);
//    }
//}