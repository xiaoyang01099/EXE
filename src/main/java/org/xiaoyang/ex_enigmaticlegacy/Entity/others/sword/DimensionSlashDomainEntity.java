package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
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
import org.xiaoyang.ex_enigmaticlegacy.Init.ModSounds;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

// DimensionSlashDomainEntity 是次元斩领域主控实体，负责冻结、阶段推进和终结伤害。
public class DimensionSlashDomainEntity extends Entity {
    public static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(DimensionSlashDomainEntity.class, EntityDataSerializers.INT); // 同步领域年龄。
    public static final EntityDataAccessor<Integer> VISUAL_SEED = SynchedEntityData.defineId(DimensionSlashDomainEntity.class, EntityDataSerializers.INT); // 同步视觉随机种子。
    public static final EntityDataAccessor<Boolean> STRIKE_STARTED = SynchedEntityData.defineId(DimensionSlashDomainEntity.class, EntityDataSerializers.BOOLEAN); // 同步是否已生成斩击。
    public static final EntityDataAccessor<Boolean> FINAL_HIT_DONE = SynchedEntityData.defineId(DimensionSlashDomainEntity.class, EntityDataSerializers.BOOLEAN); // 同步是否已终结伤害。
    @Nullable
    public UUID casterUUID; // 释放者 UUID。
    public int clientLastParticleTick = -1; // 客户端粒子提交防重 tick。
    public boolean clientShakePlayed = false; // 客户端相机抖动防重。
    public boolean clientDebrisPlayed = false; // 客户端地面碎屑防重。

    public DimensionSlashDomainEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    // 创建服务端领域实体。
    public static DimensionSlashDomainEntity create(LivingEntity owner) {
        DimensionSlashDomainEntity entity = new DimensionSlashDomainEntity(ModEntities.DIMENSION_SLASH_DOMAIN.get(), owner.level());
        entity.casterUUID = owner.getUUID();
        entity.setPos(owner.position());
        entity.entityData.set(VISUAL_SEED, owner.level().getRandom().nextInt());
        return entity;
    }

    @Override
    public void defineSynchedData() {
        this.entityData.define(AGE, 0);
        this.entityData.define(VISUAL_SEED, 0);
        this.entityData.define(STRIKE_STARTED, false);
        this.entityData.define(FINAL_HIT_DONE, false);
    }

    @Override
    public void tick() {
        super.tick();
        this.entityData.set(AGE, this.getAge() + 1);
        if (this.level().isClientSide()) {
            return;
        }

        this.refreshMovementLocks();
        this.tryStartStrikeEntity();
        this.tryFinalHit();

        if (this.getAge() > DimensionSlashConfig.DOMAIN_LIFE_TICKS) {
            this.discard();
        }
    }

    // 刷新领域内生物的移动锁定。
    public void refreshMovementLocks() {
        List<LivingEntity> targets = this.getDomainTargets();
        for (LivingEntity target : targets) {
            if (!this.canAffectTarget(target)) continue;
            EntityUtil.lockMovement(target, DimensionSlashConfig.FREEZE_REFRESH_TICKS, target.position());
        }
    }

    // 到达斩击阶段时生成连续斩击实体。
    public void tryStartStrikeEntity() {
        if (this.entityData.get(STRIKE_STARTED)) return;
        if (this.getAge() < DimensionSlashConfig.STRIKE_START_TICK) return;
        DimensionSlashStrikeEntity strike = DimensionSlashStrikeEntity.create(this);
        this.level().addFreshEntity(strike);
        this.level().playSound(null, this.blockPosition(), ModSounds.AAAAA.get(), SoundSource.PLAYERS, 1.15F, 0.95F);
        this.entityData.set(STRIKE_STARTED, true);
    }

    // 到达终结阶段时造成一次终结伤害并播放破碎声音。
    public void tryFinalHit() {
        if (this.entityData.get(FINAL_HIT_DONE)) return;
        if (this.getAge() < DimensionSlashConfig.FINAL_HIT_TICK) return;
        for (LivingEntity target : this.getDomainTargets()) {
            if (!this.canAffectTarget(target)) continue;
            this.hurtTarget(target, ConfigFile.flySwordDimensionSlashFinalDamage());
        }
        this.level().playSound(null, this.blockPosition(), ModSounds.AAAAA.get(), SoundSource.PLAYERS, 1.45F, 0.92F);
        this.entityData.set(FINAL_HIT_DONE, true);
    }

    // 取得领域范围内的 LivingEntity 候选。
    public List<LivingEntity> getDomainTargets() {
        double radius = DimensionSlashConfig.RADIUS;
        AABB box = this.getBoundingBox().inflate(radius);
        return this.level().getEntitiesOfClass(LivingEntity.class, box, entity -> entity.distanceToSqr(this) <= radius * radius);
    }

    // 判断目标是否可被次元斩影响。
    public boolean canAffectTarget(LivingEntity target) {
        if (target == null || !target.isAlive()) return false;
        if (this.casterUUID != null && target.getUUID().equals(this.casterUUID)) return false;
        return !EntityUtil.isInDamageWhitelist(target);
    }

    // 用释放者归属造成伤害。
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
        return Math.min(1.0F, (this.getAge() + partialTick) / Math.max(1.0F, (float) DimensionSlashConfig.DOMAIN_LIFE_TICKS));
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
