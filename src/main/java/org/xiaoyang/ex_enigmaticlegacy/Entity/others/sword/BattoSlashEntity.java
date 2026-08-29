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
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class BattoSlashEntity extends Entity {
    private int clientAge = 0;
    private int prevClientAge = 0;
    public static final int LIFE_TICKS = 40;
    public static final int PREVIEW_LIFE_TICKS = 20 * 60;
    public static final double HEIGHT_RANGE = 5.0D;
    public static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(BattoSlashEntity.class, EntityDataSerializers.INT); // 同步生命周期年龄。
    public static final EntityDataAccessor<Integer> VISUAL_SEED = SynchedEntityData.defineId(BattoSlashEntity.class, EntityDataSerializers.INT); // 同步视觉随机种子。
    public static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(BattoSlashEntity.class, EntityDataSerializers.FLOAT); // 同步释放朝向 X。
    public static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(BattoSlashEntity.class, EntityDataSerializers.FLOAT); // 同步释放朝向 Z。
    public static final EntityDataAccessor<Boolean> PREVIEW_STATIC = SynchedEntityData.defineId(BattoSlashEntity.class, EntityDataSerializers.BOOLEAN); // 同步是否为 testitem 静态预览。
    @Nullable
    public UUID casterUUID;
    public boolean clientAppearanceParticlesEmitted = false;

    public BattoSlashEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static BattoSlashEntity create(LivingEntity owner) {
        BattoSlashEntity entity = new BattoSlashEntity(ModEntities.BATTO_SLASH.get(), owner.level());
        Vec3 forward = getHorizontalForward(owner.getViewVector(1.0F));
        entity.casterUUID = owner.getUUID();
        entity.setPos(owner.position());
        entity.entityData.set(VISUAL_SEED, owner.level().getRandom().nextInt());
        entity.entityData.set(DIR_X, (float) forward.x);
        entity.entityData.set(DIR_Z, (float) forward.z);
        return entity;
    }

    @Override
    public void defineSynchedData() {
        this.entityData.define(AGE, 0);
        this.entityData.define(VISUAL_SEED, 0);
        this.entityData.define(DIR_X, 1.0F);
        this.entityData.define(DIR_Z, 0.0F);
        this.entityData.define(PREVIEW_STATIC, false);
    }

    @Override
    public void tick() {
        super.tick();
        this.entityData.set(AGE, this.getAge() + 1);

        if (this.level.isClientSide()) {
            this.prevClientAge = this.clientAge;
            this.clientAge++;
            return;
        }

        if (!isPreviewStatic() && this.getAge() == 1) {
            doDamage();
        }
        if (this.getAge() >= getLifeTicks()) {
            this.discard();
        }
    }

    public void doDamage() {
        Vec3 center = this.position();
        double radius = getDamageRadius();
        AABB box = new AABB(
                center.x - radius, center.y - HEIGHT_RANGE, center.z - radius,
                center.x + radius, center.y + HEIGHT_RANGE, center.z + radius
        );
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, box, this::canDamageTarget);
        for (LivingEntity target : targets) {
            hurtTarget(target, getDamageAmount());
        }
    }

    // 判断目标是否可被拔刀斩伤害。
    public boolean canDamageTarget(LivingEntity target) {
        if (target == null || !target.isAlive()) return false;
        if (this.casterUUID != null && target.getUUID().equals(this.casterUUID)) return false;
        if (Math.abs(target.getY() - this.getY()) > HEIGHT_RANGE) return false;
        double radius = getDamageRadius();
        if (horizontalDistanceSqr(target.position(), this.position()) > radius * radius) return false;
        return !EntityUtil.isInDamageWhitelist(target);
    }

    // 用释放者归属或魔法伤害结算拔刀斩。
    public void hurtTarget(LivingEntity target, float damage) {
        Player caster = this.casterUUID == null ? null : this.level().getPlayerByUUID(this.casterUUID);
        if (caster != null) {
            target.hurt(caster.damageSources().playerAttack(caster), damage);
            return;
        }
        target.hurt(this.damageSources().magic(), damage);
    }

    // 计算两个位置的水平距离平方。
    public double horizontalDistanceSqr(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return dx * dx + dz * dz;
    }

    public int getAge() {
        return this.entityData.get(AGE);
    }

    public int getVisualSeed() {
        return this.entityData.get(VISUAL_SEED);
    }

    // 普通拔刀斩按完整生命周期推进，预览拔刀斩在显现完成后保持可见。
    public float getProgress(float partialTick) {
        if (this.level.isClientSide()) {
            float localAge = Mth.lerp(partialTick, this.prevClientAge, this.clientAge);
            if (isPreviewStatic()) {
                return Mth.clamp(localAge / 20.0F, 0.0F, 0.50F);
            }
            return Mth.clamp(localAge / (float) Math.max(1, LIFE_TICKS), 0.0F, 1.0F);
        }

        if (isPreviewStatic()) {
            return Mth.clamp((this.getAge() + partialTick) / 20.0F, 0.0F, 0.50F);
        }
        return Mth.clamp((this.getAge() + partialTick) / (float) Math.max(1, LIFE_TICKS), 0.0F, 1.0F);
    }

    public int getLifeTicks() {
        return isPreviewStatic() ? PREVIEW_LIFE_TICKS : LIFE_TICKS;
    }

    public Vec3 getForward() {
        return safeNormalize(new Vec3(this.entityData.get(DIR_X), 0.0D, this.entityData.get(DIR_Z)), new Vec3(1.0D, 0.0D, 0.0D));
    }

    // 由同步视觉种子派生固定倾斜角，保证多客户端看到一致的随机倾斜。
    public float getTiltAngle() {
        RandomSource random = RandomSource.create((long) getVisualSeed() * 31L + 0x5DEECE66DL);
        return (random.nextFloat() * 30.0F - 15.0F) * ((float) Math.PI / 180.0F);
    }

    // 取得绕释放方向倾斜后的横向基向量。
    public Vec3 getTiltedSide() {
        Vec3 forward = getForward();
        Vec3 side = getBaseSide(forward);
        Vec3 up = getBaseUp(forward, side);
        double angle = getTiltAngle();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return safeNormalize(side.scale(cos).add(up.scale(sin)), side);
    }

    // 取得绕释放方向倾斜后的竖向基向量。
    public Vec3 getTiltedUp() {
        Vec3 forward = getForward();
        Vec3 side = getBaseSide(forward);
        Vec3 up = getBaseUp(forward, side);
        double angle = getTiltAngle();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return safeNormalize(up.scale(cos).subtract(side.scale(sin)), up);
    }

    // 根据释放方向计算未倾斜的横向基向量。
    public Vec3 getBaseSide(Vec3 forward) {
        return safeNormalize(new Vec3(0.0D, 1.0D, 0.0D).cross(forward), new Vec3(1.0D, 0.0D, 0.0D));
    }

    // 根据释放方向和横向基向量计算未倾斜的竖向基向量。
    public Vec3 getBaseUp(Vec3 forward, Vec3 side) {
        return safeNormalize(side.cross(forward), new Vec3(0.0D, 1.0D, 0.0D));
    }

    public boolean isPreviewStatic() {
        return this.entityData.get(PREVIEW_STATIC);
    }

    public double getDamageRadius() {
        return ConfigFile.flySwordBattoSlashRadius();
    }

    public float getDamageAmount() {
        return ConfigFile.flySwordBattoSlashDamage();
    }

    public static Vec3 getHorizontalForward(Vec3 direction) {
        return safeNormalize(new Vec3(direction.x, 0.0D, direction.z), new Vec3(1.0D, 0.0D, 0.0D));
    }

    public static Vec3 safeNormalize(Vec3 value, Vec3 fallback) {
        if (value == null || value.lengthSqr() < 1.0E-8D) {
            return fallback;
        }
        return value.normalize();
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
        if (tag.contains("DirX")) {
            this.entityData.set(DIR_X, tag.getFloat("DirX"));
        }
        if (tag.contains("DirZ")) {
            this.entityData.set(DIR_Z, tag.getFloat("DirZ"));
        }
        if (tag.contains("VisualSeed")) {
            this.entityData.set(VISUAL_SEED, tag.getInt("VisualSeed"));
        }
        if (tag.contains("PreviewStatic")) {
            this.entityData.set(PREVIEW_STATIC, tag.getBoolean("PreviewStatic"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        if (this.casterUUID != null) {
            tag.putUUID("Caster", this.casterUUID);
        }
        tag.putFloat("DirX", this.entityData.get(DIR_X));
        tag.putFloat("DirZ", this.entityData.get(DIR_Z));
        tag.putInt("VisualSeed", this.entityData.get(VISUAL_SEED));
        tag.putBoolean("PreviewStatic", this.entityData.get(PREVIEW_STATIC));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
