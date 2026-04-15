package org.xiaoyang.ex_enigmaticlegacy.Entity.others;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;
import vazkii.botania.client.fx.WispParticleData;

public class EntitySlimeCannonBall extends Projectile {
    private static final EntityDataAccessor<Integer> SLIME_SIZE =
            SynchedEntityData.defineId(EntitySlimeCannonBall.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE =
            SynchedEntityData.defineId(EntitySlimeCannonBall.class, EntityDataSerializers.FLOAT);

    private int cachedSize = -1;
    private static final float[] PARTICLE_COLORS = {0.2F, 0.9F, 0.4F};
    private static final double AOE_BASE_RADIUS = 1.5;
    private static final double AOE_RADIUS_PER_SIZE = 0.5;

    public EntitySlimeCannonBall(EntityType<? extends EntitySlimeCannonBall> type, Level level) {
        super(type, level);
    }

    public EntitySlimeCannonBall(Level level, LivingEntity shooter, float damage, int slimeSize) {
        super(ModEntities.SLIME_CANNON_BALL.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
        this.entityData.set(DAMAGE, damage);
        this.entityData.set(SLIME_SIZE, Math.max(1, slimeSize));
        this.cachedSize = Math.max(1, slimeSize);
        refreshDimensions();
    }

    public EntitySlimeCannonBall(Level level, float damage, int slimeSize) {
        super(ModEntities.SLIME_CANNON_BALL.get(), level);
        this.entityData.set(DAMAGE, damage);
        this.entityData.set(SLIME_SIZE, Math.max(1, slimeSize));
        this.cachedSize = Math.max(1, slimeSize);
        refreshDimensions();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SLIME_SIZE, 1);
        this.entityData.define(DAMAGE, 4.0F);
    }

    public int getSlimeSize() {
        return this.entityData.get(SLIME_SIZE);
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    private float getContactDamagePerTick() {
        return 10.0F + (getSlimeSize() - 1) * 5.0F;
    }

    /**
     * 1.20.1: DamageSource 必须通过 level().damageSources() 来创建，
     * 或者使用自定义 DamageType。这里用 mob_attack 作为基础类型。
     */
    private DamageSource createSlimeDamage() {
        Entity owner = getOwner();
        if (owner instanceof LivingEntity living) {
            return level().damageSources().mobAttack(living);
        }
        return level().damageSources().generic();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        int size = cachedSize > 0 ? cachedSize : 1;
        float bbSize = 0.51F * size;
        return EntityDimensions.scalable(bbSize, bbSize);
    }

    @Override
    public void tick() {
        this.xOld = this.getX();
        this.yOld = this.getY();
        this.zOld = this.getZ();

        super.tick();
        int currentSize = getSlimeSize();
        if (currentSize != cachedSize) {
            cachedSize = currentSize;
            refreshDimensions();
        }

        Level level = level();

        if (level.isClientSide) {
            spawnTrailParticles();
            Vec3 motion = this.getDeltaMovement();
            this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
            this.setDeltaMovement(motion.add(0, -0.05, 0));
            return;
        }

        Vec3 pos = this.position();
        Vec3 motion = this.getDeltaMovement();
        Vec3 nextPos = pos.add(motion);

        // 穿透接触伤害
        level.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().expandTowards(motion).inflate(0.3),
                this::canHitEntity
        ).forEach(living -> {
            int oldInvTime = living.invulnerableTime;
            living.invulnerableTime = 0;
            living.hurt(createSlimeDamage(), getContactDamagePerTick());
            living.invulnerableTime = oldInvTime;
        });

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level, this, pos, nextPos,
                this.getBoundingBox().expandTowards(motion).inflate(0.5),
                this::canHitEntity
        );
        if (entityHit != null) {
            onHitEntity(entityHit);
            this.discard();
            return;
        }

        BlockHitResult blockHit = level.clip(
                new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)
        );
        if (blockHit.getType() != HitResult.Type.MISS) {
            onHitBlock(blockHit);
            return;
        }

        this.setPos(nextPos.x, nextPos.y, nextPos.z);
        this.setDeltaMovement(motion.add(0, -0.05, 0));

        if (this.tickCount > 200) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide) return;
        Entity target = result.getEntity();

        if (target instanceof LivingEntity living && target != getOwner()) {
            int oldInvTime = living.invulnerableTime;
            living.invulnerableTime = 0;
            living.hurt(createSlimeDamage(), getDamage());
            living.invulnerableTime = oldInvTime;

            doAoeDamage();
        }

        if (getSlimeSize() > 1) {
            spawnChildSlimes();
        }

        if (level() instanceof ServerLevel serverLevel) {
            spawnHitParticles(serverLevel);
            spawnAoeParticles(serverLevel);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (level().isClientSide) return;

        doAoeDamage();

        if (getSlimeSize() > 1) {
            spawnChildSlimes();
        }

        if (level() instanceof ServerLevel serverLevel) {
            spawnHitParticles(serverLevel);
            spawnAoeParticles(serverLevel);
        }

        this.discard();
    }

    private void doAoeDamage() {
        int size = getSlimeSize();
        double radius = AOE_BASE_RADIUS + (size - 1) * AOE_RADIUS_PER_SIZE;
        float damage = getDamage();

        level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(radius),
                e -> e != getOwner() && e.isAlive()
        ).forEach(living -> {
            int oldInvTime = living.invulnerableTime;
            living.invulnerableTime = 0;
            living.hurt(createSlimeDamage(), damage);
            living.invulnerableTime = oldInvTime;
        });
    }

    private void spawnAoeParticles(ServerLevel serverLevel) {
        int size = getSlimeSize();
        double radius = AOE_BASE_RADIUS + (size - 1) * AOE_RADIUS_PER_SIZE;
        WispParticleData aoe = WispParticleData.wisp(0.4F, 0.2F, 0.9F, 0.4F, 0.8F);
        int particleCount = 12 + size * 4;
        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            double px = getX() + radius * Math.cos(angle);
            double pz = getZ() + radius * Math.sin(angle);
            serverLevel.sendParticles(aoe, px, getY() + 0.1, pz, 1, 0.05, 0.05, 0.05, 0.0);
        }
        WispParticleData center = WispParticleData.wisp(
                0.6F + 0.15F * size, 0.5F, 1.0F, 0.6F, 1.0F);
        serverLevel.sendParticles(center,
                getX(), getY() + 0.5, getZ(),
                6 + size * 2, radius * 0.4, 0.2, radius * 0.4, 0.05);
    }

    private void spawnChildSlimes() {
        int childSize = getSlimeSize() - 1;
        int childCount = 2 + level().random.nextInt(3);
        float childDamage = getDamage() * 0.6F;

        Entity ownerEntity = this.getOwner();
        LivingEntity ownerLiving = (ownerEntity instanceof LivingEntity l) ? l : null;

        for (int i = 0; i < childCount; i++) {
            EntitySlimeCannonBall child;
            if (ownerLiving != null) {
                child = new EntitySlimeCannonBall(level(), ownerLiving, childDamage, childSize);
            } else {
                child = new EntitySlimeCannonBall(level(), childDamage, childSize);
            }

            child.setPos(this.getX(), this.getY(), this.getZ());

            double vx = (level().random.nextDouble() - 0.5) * 0.8;
            double vy = level().random.nextDouble() * 0.5 + 0.3;
            double vz = (level().random.nextDouble() - 0.5) * 0.8;
            child.setDeltaMovement(vx, vy, vz);

            level().addFreshEntity(child);
        }
    }

    private void spawnTrailParticles() {
        Level level = level();
        int size = getSlimeSize();
        WispParticleData trail = WispParticleData.wisp(
                0.25F + 0.1F * size,
                PARTICLE_COLORS[0], PARTICLE_COLORS[1], PARTICLE_COLORS[2], 1.0F
        );
        for (int i = 0; i < 2 + size; i++) {
            double ox = (level.random.nextDouble() - 0.5) * 0.15;
            double oy = (level.random.nextDouble() - 0.5) * 0.15;
            double oz = (level.random.nextDouble() - 0.5) * 0.15;
            level.addParticle(trail, getX() + ox, getY() + oy, getZ() + oz, 0, 0, 0);
        }

        WispParticleData glow = WispParticleData.wisp(0.15F, 0.4F, 1.0F, 0.6F, 0.7F);
        Vec3 motion = this.getDeltaMovement();
        level.addParticle(glow, getX(), getY(), getZ(),
                -motion.x * 0.3, -motion.y * 0.3, -motion.z * 0.3);
    }

    private void spawnHitParticles(ServerLevel serverLevel) {
        int size = getSlimeSize();

        WispParticleData burst = WispParticleData.wisp(0.5F + 0.2F * size, 0.2F, 0.9F, 0.4F, 1.0F);
        serverLevel.sendParticles(burst, getX(), getY(), getZ(), 8 + 4 * size, 0.3, 0.3, 0.3, 0.12);

        WispParticleData flash = WispParticleData.wisp(0.8F, 1.0F, 1.0F, 1.0F, 1.0F);
        serverLevel.sendParticles(flash, getX(), getY(), getZ(), 3, 0.1, 0.1, 0.1, 0.2);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(d)) d = 4.0;
        d *= 64.0;
        return distance < d * d;
    }

    @Override
    public boolean canHitEntity(Entity entity) {
        if (entity == getOwner()) return false;
        if (!entity.isAlive()) return false;
        if (entity instanceof EntitySlimeCannonBall) return false;
        return entity instanceof LivingEntity;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", getDamage());
        tag.putInt("SlimeSize", getSlimeSize());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        int size = Math.max(1, tag.getInt("SlimeSize"));
        this.entityData.set(DAMAGE, tag.getFloat("Damage"));
        this.entityData.set(SLIME_SIZE, size);
        this.cachedSize = size;
        refreshDimensions();
    }
}