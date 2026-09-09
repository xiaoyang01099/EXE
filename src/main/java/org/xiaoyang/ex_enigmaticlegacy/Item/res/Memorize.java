package org.xiaoyang.ex_enigmaticlegacy.Item.res;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModDamageSources;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.TrueBoltSpawnPacket;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class Memorize extends Item {
    public Memorize() {
        super(new Properties().rarity(Rarity.EPIC));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        if (player.tickCount % 5 == 0 && !level.isClientSide) {
            Vec3 look = player.getLookAngle();
            Vec3 eye = player.getEyePosition(1.0f);
            Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
            Vec3 startPoint = eye.add(look.scale(0.6)).add(right.scale(0.35));
            HitResult hit = player.pick(40.0, 1.0f, false);
            Vec3 targetPoint = hit.getType() == HitResult.Type.MISS
                    ? eye.add(look.scale(40.0))
                    : hit.getLocation();
            Random random = new Random();
            long seed = random.nextLong();
            TrueBoltSpawnPacket packet = new TrueBoltSpawnPacket(
                    startPoint,
                    targetPoint,
                    seed,
                    10,
                    0.13f,
                    0.60f,
                    0.72f,
                    1.0f
            );
            NetworkHandler.sendToTrackingEntityAndSelf(packet, player);
            AABB damageArea = new AABB(startPoint, targetPoint).inflate(2.0);
            List<Entity> entities = level.getEntities(player, damageArea, e -> e instanceof LivingEntity);
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity livingEntity2) {
                    if (!livingEntity2.hasLineOfSight(player)) continue;
                    float damage = 1000.0F;
                    forceSetHealth(livingEntity2, Math.max(livingEntity2.getHealth() - damage, 0f),
                            livingEntity2.damageSources().mobAttack(player), player);
                    if (!livingEntity2.isDeadOrDying())
                        livingEntity2.hurt(level.damageSources().lightningBolt(), damage);
                    if (livingEntity2.isOnFire()) {
                        livingEntity2.setRemainingFireTicks(livingEntity2.getRemainingFireTicks() + 20);
                    } else {
                        livingEntity2.setRemainingFireTicks(40);
                    }
                }
            }
            if (player.tickCount % 15 == 0) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.8F, 0.8F);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (livingEntity instanceof Player player && !level.isClientSide) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    public static void forceSetHealth(LivingEntity living, float f, DamageSource damageSource, @Nullable LivingEntity attacker) {
        if (living == null)
            return;
        if (attacker != null && !living.level.isClientSide) {
            living.setLastHurtByMob(attacker);
        }
        SynchedEntityData newData = living.entityData;
        newData.set(LivingEntity.DATA_HEALTH_ID, f);
        living.entityData.set(LivingEntity.DATA_HEALTH_ID, f);
        if (f <= 0f && !living.level.isClientSide && living.getServer() != null && damageSource != null) {
            if (attacker != null) {
                attacker.killedEntity((ServerLevel) attacker.level, living);
                attacker.awardKillScore(living, 1, damageSource);
            }
            if (living instanceof Player player) {
                if (!living.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                    player.getInventory().dropAll();
                }
            } else if (living.isDeadOrDying()) forceDropLoot(living, damageSource, attacker);
        }
    }

    public static void forceDropLoot(LivingEntity living, @Nullable DamageSource source, LivingEntity attacker) {
        if (living == null || !(living.level instanceof ServerLevel serverLevel))
            return;
        if (living instanceof Player) return;
        Entity sourceEntity = source != null ? source.getDirectEntity() : attacker;
        if (attacker != null && sourceEntity != attacker)
            sourceEntity = attacker;
        if (source == null) {
            if (attacker != null) {
                source = living.damageSources().source(ModDamageSources.ABSOLUTE, attacker, living);
            } else {
                source = living.damageSources().generic();
            }
        }
        int lootingLevel = ForgeHooks.getLootingLevel(living, sourceEntity, source);
        living.captureDrops(new ArrayList<>());
        boolean killedByPlayer = sourceEntity instanceof Player;
        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            ResourceLocation lootTableId = living.getLootTable();
            LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(lootTableId);
            if (lootTable != LootTable.EMPTY) {
                LootParams.Builder builder = new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.THIS_ENTITY, living)
                        .withParameter(LootContextParams.ORIGIN, living.position())
                        .withParameter(LootContextParams.DAMAGE_SOURCE, source);
                if (sourceEntity instanceof LivingEntity killer) {
                    builder.withParameter(LootContextParams.KILLER_ENTITY, killer);
                    builder.withParameter(LootContextParams.DIRECT_KILLER_ENTITY, killer);
                    if (killer instanceof Player player) {
                        builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player);
                        builder.withLuck(player.getLuck() + lootingLevel);
                    }
                }
                LootParams params = builder.create(LootContextParamSets.ENTITY);
                List<ItemStack> drops = lootTable.getRandomItems(params);
                for (ItemStack stack : drops) {
                    if (!stack.isEmpty()) {
                        living.spawnAtLocation(stack, 0.0F);
                    }
                }
            }
            living.dropCustomDeathLoot(source, lootingLevel, killedByPlayer);
        }
        living.dropEquipment();
        if (living.shouldDropExperience()) {
            ExperienceOrb.award(serverLevel, living.position(), living.getExperienceReward());
        }
        Collection<ItemEntity> capturedDrops = living.captureDrops(null);
        if (!ForgeHooks.onLivingDrops(living, source, capturedDrops, lootingLevel, killedByPlayer)) {
            forEachServerLevelAddFreshEntity(capturedDrops, serverLevel);
        }
        living.dropAllDeathLoot(source);
    }

    public static void forEachServerLevelAddFreshEntity(
            Collection<? extends Entity> collection,
            net.minecraft.server.level.ServerLevel serverLevel
    ) {
        if (collection == null || serverLevel == null) return;
        for (Entity entity : collection) {
            serverLevel.addFreshEntity(entity);
        }
    }
}
