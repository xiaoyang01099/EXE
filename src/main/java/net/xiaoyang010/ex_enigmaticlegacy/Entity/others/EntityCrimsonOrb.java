package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;

import java.util.List;

public class EntityCrimsonOrb extends ThrowableProjectile implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Boolean> IS_RED = SynchedEntityData.defineId(EntityCrimsonOrb.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(EntityCrimsonOrb.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CASTER_ID = SynchedEntityData.defineId(EntityCrimsonOrb.class, EntityDataSerializers.INT);

    private LivingEntity target;
    private LivingEntity caster;

    private static final float CRIMSON_SPELL_DAMAGE_MIN = 42.0f;
    private static final float CRIMSON_SPELL_DAMAGE_MAX = 100.0f;

    public EntityCrimsonOrb(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public EntityCrimsonOrb(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity caster, LivingEntity target, boolean isRed) {
        super(entityType, caster, level);
        this.caster = caster;
        this.target = target;
        this.setRed(isRed);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(IS_RED, false);
        this.entityData.define(TARGET_ID, -1);
        this.entityData.define(CASTER_ID, -1);
    }

    public boolean isRed() {
        return this.entityData.get(IS_RED);
    }

    public void setRed(boolean red) {
        this.entityData.set(IS_RED, red);
    }

    private void setTargetId(int id) {
        this.entityData.set(TARGET_ID, id);
    }

    private void setCasterId(int id) {
        this.entityData.set(CASTER_ID, id);
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        int targetId = -1;
        int casterId = -1;

        if (this.target != null) {
            targetId = this.target.getId();
        }

        if (this.caster != null) {
            casterId = this.caster.getId();
        }

        buffer.writeInt(targetId);
        buffer.writeInt(casterId);
        buffer.writeBoolean(this.isRed());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        int targetId = buffer.readInt();
        int casterId = buffer.readInt();
        boolean isRed = buffer.readBoolean();

        this.setTargetId(targetId);
        this.setCasterId(casterId);
        this.setRed(isRed);

        if (targetId >= 0) {
            Entity entity = this.level.getEntity(targetId);
            if (entity instanceof LivingEntity) {
                this.target = (LivingEntity) entity;
            }
        }

        if (casterId >= 0) {
            Entity entity = this.level.getEntity(casterId);
            if (entity instanceof LivingEntity) {
                this.caster = (LivingEntity) entity;
            }
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);

        if (!this.level.isClientSide && this.getOwner() != null) {
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) hitResult;

                float damage = CRIMSON_SPELL_DAMAGE_MIN +
                        (float)(Math.random() * (CRIMSON_SPELL_DAMAGE_MAX - CRIMSON_SPELL_DAMAGE_MIN));

                entityHit.getEntity().hurt(
                        new ModDamageSources.DamageSourceMagic(this.getOwner()),
                        damage
                );

                this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 1.0F,
                        1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);

                RelicsEventHandler.imposeBurst(this.level, this.getX(), this.getY(), this.getZ(), 1.0F);
                this.discard();

            } else if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) hitResult;
                BlockPos pos = blockHit.getBlockPos();
                BlockState blockState = this.level.getBlockState(pos);

                if (!(blockState.getBlock() instanceof BushBlock) &&
                        !(blockState.getBlock() instanceof LeavesBlock) &&
                        !(blockState.getBlock() instanceof LiquidBlock)) {

                    this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 1.0F,
                            1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);

                    RelicsEventHandler.imposeBurst(this.level, this.getX(), this.getY(), this.getZ(), 1.0F);
                    this.discard();
                }
            }
        }
    }

    public void getNewTarget() {
        int searchRange = 32;
        AABB searchArea = new AABB(
                this.getX() - searchRange, this.getY() - searchRange, this.getZ() - searchRange,
                this.getX() + searchRange, this.getY() + searchRange, this.getZ() + searchRange
        );

        List<LivingEntity> entities = this.level.getEntitiesOfClass(LivingEntity.class, searchArea);

        entities.removeIf(entity ->
                entity.equals(this.caster) ||
                        !entity.isAlive() ||
                        entity.getUUID().equals(this.getUUID())
        );

        if (!entities.isEmpty()) {
            this.target = entities.get((int)(Math.random() * entities.size()));
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > 1000) {
            this.discard();
            return;
        }

        if (!this.isRed()) {
            this.discard();
            return;
        }

        if (this.target != null) {
            if (!this.target.isAlive()) {
                this.getNewTarget();
            } else {
                double distance = this.distanceTo(this.target);
                double dx = this.target.getX() - this.getX();
                double dy = this.target.getBoundingBox().minY + this.target.getBbHeight() * 0.6 - this.getY();
                double dz = this.target.getZ() - this.getZ();

                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (length > 0) {
                    dx /= length;
                    dy /= length;
                    dz /= length;
                }

                Vec3 currentDelta = this.getDeltaMovement();
                double acceleration = 0.3;

                Vec3 newDelta = currentDelta.add(dx * acceleration, dy * acceleration, dz * acceleration);

                newDelta = new Vec3(
                        Mth.clamp(newDelta.x, -0.25, 0.25),
                        Mth.clamp(newDelta.y, -0.25, 0.25),
                        Mth.clamp(newDelta.z, -0.25, 0.25)
                );

                if (this.tickCount < 5 && newDelta.y < 0) {
                    newDelta = new Vec3(newDelta.x, Math.abs(newDelta.y), newDelta.z);
                }

                this.setDeltaMovement(newDelta);
            }
        } else {
            this.getNewTarget();
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        } else {
            this.markHurt();

            Entity sourceEntity = damageSource.getEntity();
            if (sourceEntity != null) {
                Vec3 sourcePos = sourceEntity.getLookAngle();
                if (sourcePos != null) {
                    Vec3 currentDelta = this.getDeltaMovement();
                    Vec3 newDelta = currentDelta.add(
                            sourcePos.x * 0.9,
                            sourcePos.y * 0.9,
                            sourcePos.z * 0.9
                    );
                    this.setDeltaMovement(newDelta);

                    this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 1.0F,
                            1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsRed", this.isRed());
        if (this.target != null) {
            compound.putInt("TargetId", this.target.getId());
        }
        if (this.caster != null) {
            compound.putInt("CasterId", this.caster.getId());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setRed(compound.getBoolean("IsRed"));

        if (compound.contains("TargetId")) {
            int targetId = compound.getInt("TargetId");
            Entity entity = this.level.getEntity(targetId);
            if (entity instanceof LivingEntity) {
                this.target = (LivingEntity) entity;
            }
        }

        if (compound.contains("CasterId")) {
            int casterId = compound.getInt("CasterId");
            Entity entity = this.level.getEntity(casterId);
            if (entity instanceof LivingEntity) {
                this.caster = (LivingEntity) entity;
            }
        }
    }
}