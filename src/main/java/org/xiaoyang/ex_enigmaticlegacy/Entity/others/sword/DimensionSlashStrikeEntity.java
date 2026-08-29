package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;
import org.xiaoyang.ex_enigmaticlegacy.Util.EntityUtil;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

// DimensionSlashStrikeEntity 负责连续斩击的小伤害和客户端 bloom 光刃渲染。
public class DimensionSlashStrikeEntity extends Entity {
    public static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(DimensionSlashStrikeEntity.class, EntityDataSerializers.INT); // 同步斩击年龄。
    public static final EntityDataAccessor<Integer> VISUAL_SEED = SynchedEntityData.defineId(DimensionSlashStrikeEntity.class, EntityDataSerializers.INT); // 同步斩击随机种子。
    @Nullable
    public UUID casterUUID; // 释放者 UUID。

    public DimensionSlashStrikeEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    // 根据领域实体创建连续斩击实体。
    public static DimensionSlashStrikeEntity create(DimensionSlashDomainEntity domain) {
        DimensionSlashStrikeEntity entity = new DimensionSlashStrikeEntity(ModEntities.DIMENSION_SLASH_STRIKE.get(), domain.level());
        entity.casterUUID = domain.casterUUID;
        entity.setPos(domain.position());
        entity.entityData.set(VISUAL_SEED, domain.getVisualSeed() ^ 0x5A77C1);
        return entity;
    }

    @Override
    public void defineSynchedData() {
        this.entityData.define(AGE, 0);
        this.entityData.define(VISUAL_SEED, 0);
    }

    @Override
    public void tick() {
        super.tick();
        this.entityData.set(AGE, this.getAge() + 1);
        if (!this.level().isClientSide()) {
            this.trySmallHit();
            if (this.getAge() > DimensionSlashConfig.STRIKE_LIFE_TICKS) {
                this.discard();
            }
        }
    }

    // 按间隔造成多次小伤害，斩击音效只在领域开始时播放一次。
    public void trySmallHit() {
        if (this.getAge() <= 0) return;
        if (this.getAge() % DimensionSlashConfig.SMALL_HIT_INTERVAL_TICKS != 0) return;
        for (LivingEntity target : this.getStrikeTargets()) {
            if (!this.canDamageTarget(target)) continue;
            this.hurtTarget(target, ConfigFile.flySwordDimensionSlashSmallDamage());
        }
    }

    // 获取连续斩击范围内候选目标。
    public List<LivingEntity> getStrikeTargets() {
        double radius = DimensionSlashConfig.RADIUS;
        AABB box = this.getBoundingBox().inflate(radius);
        return this.level().getEntitiesOfClass(LivingEntity.class, box, entity -> entity.distanceToSqr(this) <= radius * radius);
    }

    // 判断目标是否可被小伤害影响。
    public boolean canDamageTarget(LivingEntity target) {
        if (target == null || !target.isAlive()) return false;
        if (this.casterUUID != null && target.getUUID().equals(this.casterUUID)) return false;
        return !EntityUtil.isInDamageWhitelist(target);
    }

    // 用释放者归属造成小伤害。
    public void hurtTarget(LivingEntity target, float damage) {
        Player caster = this.casterUUID == null ? null : this.level().getPlayerByUUID(this.casterUUID);
        if (caster != null) {
            target.hurt(caster.damageSources().playerAttack(caster), damage);
        } else {
            target.hurt(this.damageSources().magic(), damage);
        }
    }

    public int getAge() {
        return this.entityData.get(AGE);
    }

    public int getVisualSeed() {
        return this.entityData.get(VISUAL_SEED);
    }

    public float getProgress(float partialTick) {
        return Math.min(1.0F, (this.getAge() + partialTick) / Math.max(1.0F, (float) DimensionSlashConfig.STRIKE_LIFE_TICKS));
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Caster")) {
            this.casterUUID = tag.getUUID("Caster");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        if (this.casterUUID != null) {
            tag.putUUID("Caster", this.casterUUID);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
