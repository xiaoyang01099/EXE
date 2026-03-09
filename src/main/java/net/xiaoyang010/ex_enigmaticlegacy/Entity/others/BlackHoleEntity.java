package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import morph.avaritia.entity.EndestPearlEntity;
import morph.avaritia.entity.GapingVoidEntity;
import morph.avaritia.init.AvaritiaModContent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.Cuboid6;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

public class BlackHoleEntity extends GapingVoidEntity {
    private static final EntityDataAccessor<Float> CUSTOM_SCALE =
            SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> TARGET_Y =
            SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.FLOAT);

    private boolean reachedTarget = false;

    public BlackHoleEntity(EntityType<? extends GapingVoidEntity> type, Level level) {
        super((EntityType<GapingVoidEntity>) type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CUSTOM_SCALE, 1.0F);
        this.entityData.define(OWNER_ID, -1);
        this.entityData.define(TARGET_Y, 0.0F);
    }

    @Override
    public void tick() {
        this.baseTick();

        if (!reachedTarget) {
            double targetY = this.getTargetY();
            double currentY = this.getY();

            if (Math.abs(currentY - targetY) < 0.1) {
                this.setPos(this.getX(), targetY, this.getZ());
                this.setDeltaMovement(0, 0, 0);
                reachedTarget = true;
            } else {
                double speed = 0.2;
                double newY = currentY + (targetY - currentY) * speed;
                this.setPos(this.getX(), newY, this.getZ());
            }
        }

        int age = this.getAge();

        if (age >= MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (age == 0 && !this.level.isClientSide) {
            this.level.playLocalSound(
                    this.getX(), this.getY(), this.getZ(),
                    AvaritiaModContent.GAPING_VOID_SOUND.get(),
                    SoundSource.HOSTILE, 8.0F, 1.0F, true
            );
        }

        this.setAge(age + 1);

        if (!this.level.isClientSide) {
            Vector3 pos = Vector3.fromEntity(this);
            double voidScale = getVoidScale(age) * this.getCustomScale();

            spawnParticles(pos, voidScale);
            attractEntities(pos, voidScale);
            damageEntities(pos, voidScale);
        }

        if (age >= MAX_LIFETIME - 20 && !this.level.isClientSide) {
            for (int i = 0; i < 10; i++) {
                double vx = (this.random.nextDouble() - 0.5) * 0.3;
                double vy = (this.random.nextDouble() - 0.5) * 0.3;
                double vz = (this.random.nextDouble() - 0.5) * 0.3;

                this.level.addParticle(
                        ParticleTypes.REVERSE_PORTAL,
                        this.getX(), this.getY(), this.getZ(),
                        vx, vy, vz
                );
            }
        }
    }

    private void spawnParticles(Vector3 pos, double voidScale) {
        double particleSpeed = 4.5;
        double size = voidScale * 0.5 - 0.2;

        for (int i = 0; i < 50; i++) {
            Vector3 particlePos = new Vector3(0.0, 0.0, size);
            particlePos.rotate(this.random.nextFloat() * 180.0F, new Vector3(0.0, 1.0, 0.0));
            particlePos.rotate(this.random.nextFloat() * 360.0F, new Vector3(1.0, 0.0, 0.0));
            Vector3 velocity = particlePos.copy().normalize().multiply(particleSpeed);
            particlePos.add(pos);
            this.level.addParticle(
                    ParticleTypes.PORTAL,
                    particlePos.x, particlePos.y, particlePos.z,
                    velocity.x, velocity.y, velocity.z
            );
        }
    }

    private void attractEntities(Vector3 pos, double voidScale) {
        double radius = voidScale * 0.5;
        double attractRange = 24.0;
        Cuboid6 cuboid = new Cuboid6().add(pos).expand(attractRange);

        for (Entity ent : this.level.getEntitiesOfClass(Entity.class, cuboid.aabb())) {
            if (!canSuckEntity(ent)) continue;

            Vector3 diff = pos.copy().subtract(Vector3.fromEntity(ent));
            double distance = diff.mag();

            if (distance <= attractRange && distance > 0.1) {
                double normalizedDist = distance / attractRange;
                double distanceFactor = 1.0 - normalizedDist;

                double strengthCurve = Math.pow(distanceFactor, 2.5);

                if (distance <= 2.0) {
                    strengthCurve = 1.0;
                    ent.setDeltaMovement(diff.divide(distance).multiply(5.0).vec3());
                    ent.fallDistance = 0;
                    ent.setNoGravity(true);
                } else {
                    double basePower = 0.2 * radius;
                    double finalPower = basePower * (1.0 + strengthCurve * 5.0); // 最高吸力
                    ent.setDeltaMovement(diff.divide(distance).multiply(finalPower).vec3());
                    ent.setNoGravity(false);
                }

                ent.hurtMarked = true;
            }
        }
    }


    private void damageEntities(Vector3 pos, double voidScale) {
        double attackRange = voidScale * 0.5 * 0.95;
        Cuboid6 cuboid = new Cuboid6().add(pos).expand(attackRange);

        for (Entity toAttack : this.level.getEntitiesOfClass(LivingEntity.class, cuboid.aabb())) {
            if (!canAttackEntity(toAttack)) continue;

            Vector3 diff = pos.copy().subtract(Vector3.fromEntity(toAttack));
            if (diff.mag() <= attackRange) {
                LivingEntity living = (LivingEntity) toAttack;
                living.invulnerableTime = 0;
                living.hurt(DamageSource.OUT_OF_WORLD, 100.0F);
            }
        }
    }

    private boolean canSuckEntity(Entity entity) {
        if (entity == this) return false;
        if (entity instanceof BlackHoleEntity) return false;
        if (entity.getId() == this.getOwnerId()) return false;
        if (entity instanceof EndestPearlEntity) return false;

        if (entity instanceof Player player) {
            return !player.getAbilities().instabuild || !player.getAbilities().flying;
        }

        return true;
    }

    private boolean canAttackEntity(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        if (entity.getId() == this.getOwnerId()) return false;

        if (entity instanceof Player player) {
            return !player.getAbilities().instabuild;
        }

        return true;
    }

    public float getCustomScale() {
        return this.entityData.get(CUSTOM_SCALE);
    }

    public void setCustomScale(float scale) {
        this.entityData.set(CUSTOM_SCALE, scale);
    }

    public int getOwnerId() {
        return this.entityData.get(OWNER_ID);
    }

    public void setOwnerId(int id) {
        this.entityData.set(OWNER_ID, id);
    }

    public float getTargetY() {
        return this.entityData.get(TARGET_Y);
    }

    public void setTargetY(float y) {
        this.entityData.set(TARGET_Y, y);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setCustomScale(tag.getFloat("CustomScale"));
        this.setOwnerId(tag.getInt("OwnerId"));
        this.setTargetY(tag.getFloat("TargetY"));
        this.reachedTarget = tag.getBoolean("ReachedTarget");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("CustomScale", this.getCustomScale());
        tag.putInt("OwnerId", this.getOwnerId());
        tag.putFloat("TargetY", this.getTargetY());
        tag.putBoolean("ReachedTarget", this.reachedTarget);
    }
}
