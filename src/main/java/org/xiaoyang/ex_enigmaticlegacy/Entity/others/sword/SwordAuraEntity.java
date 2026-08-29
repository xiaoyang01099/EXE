package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.xiaoyang.ex_enigmaticlegacy.Util.EntityUtil;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

// SwordAuraEntity 是飞剑左键释放的剑气实体，负责移动和伤害。
public class SwordAuraEntity extends Entity {
    public static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D); // 构造随机方向用世界上方向。
    public static final Vec3 WORLD_RIGHT = new Vec3(1.0D, 0.0D, 0.0D); // 构造随机方向兜底右方向。
    public static double SPEED_SCALE = 2.0D; // 剑气实体移动速度倍率。
    public static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(SwordAuraEntity.class, EntityDataSerializers.FLOAT); // 同步飞行方向 X。
    public static final EntityDataAccessor<Float> DIR_Y = SynchedEntityData.defineId(SwordAuraEntity.class, EntityDataSerializers.FLOAT); // 同步飞行方向 Y。
    public static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(SwordAuraEntity.class, EntityDataSerializers.FLOAT); // 同步飞行方向 Z。
    public static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(SwordAuraEntity.class, EntityDataSerializers.INT); // 同步生命周期 tick。
    public static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(SwordAuraEntity.class, EntityDataSerializers.FLOAT); // 同步伤害。
    public static final EntityDataAccessor<Float> ROLL = SynchedEntityData.defineId(SwordAuraEntity.class, EntityDataSerializers.FLOAT); // 同步视觉翻滚角。
    public static final EntityDataAccessor<Integer> VISUAL_SEED = SynchedEntityData.defineId(SwordAuraEntity.class, EntityDataSerializers.INT); // 同步视觉随机种子。
    public static final EntityDataAccessor<Boolean> PREVIEW_STATIC = SynchedEntityData.defineId(SwordAuraEntity.class, EntityDataSerializers.BOOLEAN); // 同步是否为静态预览剑气。
    @Nullable
    public UUID ownerUUID; // 发射者 UUID，用于跳过自身和归属伤害。
    public Vec3 previousDamagePos = Vec3.ZERO; // 上一 tick 伤害检测位置。

    public SwordAuraEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    public void defineSynchedData() {
        this.entityData.define(DIR_X, 0.0F);
        this.entityData.define(DIR_Y, 0.0F);
        this.entityData.define(DIR_Z, 1.0F);
        this.entityData.define(AGE, 0);
        this.entityData.define(DAMAGE, 0.0F);
        this.entityData.define(ROLL, 0.0F);
        this.entityData.define(VISUAL_SEED, 0);
        this.entityData.define(PREVIEW_STATIC, false);
    }

    // 初始化剑气数据，服务端生成实体后立即调用。
    public void setAuraData(LivingEntity owner, Vec3 origin, Vec3 direction, float damage) {
        RandomSource random = this.level().getRandom();
        Vec3 finalDirection = getAimDirection(direction, owner);
        float roll = randomDegrees(random, SwordAuraVisualConfig.MAX_ROLL_DEGREES);
        this.ownerUUID = owner.getUUID();
        this.entityData.set(DIR_X, (float) finalDirection.x);
        this.entityData.set(DIR_Y, (float) finalDirection.y);
        this.entityData.set(DIR_Z, (float) finalDirection.z);
        this.entityData.set(DAMAGE, damage);
        this.entityData.set(ROLL, roll);
        this.entityData.set(VISUAL_SEED, random.nextInt());
        this.entityData.set(PREVIEW_STATIC, false);
        Vec3 spawnPos = origin.add(finalDirection.scale(0.65D));
        this.setPos(spawnPos);
        this.previousDamagePos = spawnPos;
    }

    // 初始化静态预览剑气，供 testitem 右键方块时观察剑气视觉效果。
    public void setPreviewAuraData(Player owner, Vec3 origin, Vec3 direction) {
        Vec3 finalDirection = getAimDirection(direction, owner);
        this.ownerUUID = owner.getUUID();
        this.entityData.set(DIR_X, (float) finalDirection.x);
        this.entityData.set(DIR_Y, (float) finalDirection.y);
        this.entityData.set(DIR_Z, (float) finalDirection.z);
        this.entityData.set(DAMAGE, 0.0F);
        this.entityData.set(ROLL, 0.0F);
        this.entityData.set(VISUAL_SEED, this.level().getRandom().nextInt());
        this.entityData.set(PREVIEW_STATIC, true);
        this.setPos(origin);
        this.previousDamagePos = origin;
    }

    @Override
    public void tick() {
        super.tick();
        this.entityData.set(AGE, this.getAge() + 1);
        if (this.level().isClientSide()) {
            return;
        }

        if (isPreviewStatic()) {
            if (this.getAge() > SwordAuraVisualConfig.PREVIEW_LIFE_TICKS) {
                this.discard();
            }
            return;
        }

        Vec3 oldPos = this.position();
        Vec3 newPos = oldPos.add(getAuraDirection().scale(ConfigFile.flySwordAuraSpeed() * SPEED_SCALE));
        this.setPos(newPos);
        damageAlongSegment(oldPos, newPos);
        this.previousDamagePos = newPos;

        if (this.getAge() > getEffectiveLifeTicks()) {
            this.discard();
        }
    }

    // 沿移动线段检测实体，避免高速移动时穿透目标。
    public void damageAlongSegment(Vec3 oldPos, Vec3 newPos) {
        double hitRadius = ConfigFile.flySwordAuraHitRadius();
        AABB searchBox = new AABB(oldPos, newPos).inflate(hitRadius);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox, this::canTryDamage);
        for (LivingEntity target : targets) {
            AABB targetBox = target.getBoundingBox().inflate(hitRadius);
            if (!targetBox.clip(oldPos, newPos).isPresent()) continue;
            hurtTarget(target);
        }
    }

    // 判断目标是否进入剑气伤害候选列表。
    public boolean canTryDamage(LivingEntity target) {
        if (!target.isAlive()) return false;
        if (this.ownerUUID != null && target.getUUID().equals(this.ownerUUID)) return false;
        return !EntityUtil.isInDamageWhitelist(target);
    }

    // 对目标造成带归属的伤害。
    public void hurtTarget(LivingEntity target) {
        float damage = this.entityData.get(DAMAGE);
        Player owner = this.ownerUUID == null ? null : this.level().getPlayerByUUID(this.ownerUUID);
        if (owner != null) {
            target.hurt(owner.damageSources().playerAttack(owner), damage);
        } else {
            target.hurt(this.damageSources().magic(), damage);
        }
    }

    // 按玩家准心方向确定剑气飞行方向，不再做水平或垂直随机偏转。
    public Vec3 getAimDirection(Vec3 direction, LivingEntity owner) {
        return safeNormalize(direction, owner.getViewVector(1.0F));
    }

    public float randomSignedDegrees(RandomSource random, double maxDegrees) {
        return (float) ((random.nextDouble() * 2.0D - 1.0D) * maxDegrees);
    }

    public float randomDegrees(RandomSource random, double maxDegrees) {
        return (float) (random.nextDouble() * maxDegrees);
    }

    public Vec3 getAuraDirection() {
        return safeNormalize(new Vec3(
                this.entityData.get(DIR_X),
                this.entityData.get(DIR_Y),
                this.entityData.get(DIR_Z)), new Vec3(0.0D, 0.0D, 1.0D));
    }

    public Vec3 getAuraSide(Vec3 direction) {
        Vec3 side = safeNormalize(direction.cross(WORLD_UP), WORLD_RIGHT);
        Vec3 up = safeNormalize(side.cross(direction), WORLD_UP);
        double cos = Math.cos(getRollRadians());
        double sin = Math.sin(getRollRadians());
        return safeNormalize(side.scale(cos).add(up.scale(sin)), side);
    }

    public int getAge() {
        return this.entityData.get(AGE);
    }

    public float getAgeProgress(float partialTick) {
        int lifeTicks = isPreviewStatic() ? SwordAuraVisualConfig.PREVIEW_LIFE_TICKS : getEffectiveLifeTicks();
        return Mth.clamp((this.getAge() + partialTick) / (float) Math.max(1, lifeTicks), 0.0F, 1.0F);
    }

    // 计算加速后对应的有效生命周期，保证射程基本保持不变。
    public int getEffectiveLifeTicks() {
        return Math.max(1, Mth.ceil(ConfigFile.flySwordAuraLifeTicks() * SwordAuraVisualConfig.LIFE_SCALE));
    }

    public float getRollRadians() {
        return (float) Math.toRadians(this.entityData.get(ROLL));
    }

    public int getVisualSeed() {
        return this.entityData.get(VISUAL_SEED);
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    public boolean isPreviewStatic() {
        return this.entityData.get(PREVIEW_STATIC);
    }

    public static Vec3 safeNormalize(Vec3 vector, Vec3 fallback) {
        if (vector == null || vector.lengthSqr() < 1.0E-8D) {
            return fallback;
        }
        return vector.normalize();
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
