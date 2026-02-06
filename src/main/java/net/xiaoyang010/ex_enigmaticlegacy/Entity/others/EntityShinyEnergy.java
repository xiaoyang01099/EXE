package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import vazkii.botania.api.BotaniaAPI;

import javax.annotation.Nullable;
import java.util.List;

public class EntityShinyEnergy extends ThrowableProjectile implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(EntityShinyEnergy.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> LOCK_X = SynchedEntityData.defineId(EntityShinyEnergy.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LOCK_Y = SynchedEntityData.defineId(EntityShinyEnergy.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LOCK_Z = SynchedEntityData.defineId(EntityShinyEnergy.class, EntityDataSerializers.FLOAT);

    @Nullable
    private LivingEntity target;

    public EntityShinyEnergy(EntityType<EntityShinyEnergy> type, Level level) {
        super(type, level);
        this.setBoundingBox(this.getBoundingBox().inflate(0.0F, 0.0F, 0.0F));
    }

    public EntityShinyEnergy(Level level, LivingEntity thrower, LivingEntity target, double x, double y, double z) {
        super(ModEntities.SHINY_ENERGY.get(), thrower, level);
        this.target = target;
        this.entityData.set(TARGET_ID, target != null ? target.getId() : -1);
        this.entityData.set(LOCK_X, (float) x);
        this.entityData.set(LOCK_Y, (float) y);
        this.entityData.set(LOCK_Z, (float) z);
        this.setBoundingBox(this.getBoundingBox().inflate(0.0F, 0.0F, 0.0F));
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TARGET_ID, -1);
        this.entityData.define(LOCK_X, 0.0F);
        this.entityData.define(LOCK_Y, 0.0F);
        this.entityData.define(LOCK_Z, 0.0F);
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        int id = this.target != null ? this.target.getId() : -1;
        buffer.writeInt(id);
        buffer.writeDouble(this.entityData.get(LOCK_X));
        buffer.writeDouble(this.entityData.get(LOCK_Y));
        buffer.writeDouble(this.entityData.get(LOCK_Z));
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        int id = buffer.readInt();
        if (id >= 0) {
            Entity entity = this.level.getEntity(id);
            if (entity instanceof LivingEntity living) {
                this.target = living;
                this.entityData.set(TARGET_ID, id);
            }
        }
        this.entityData.set(LOCK_X, buffer.readFloat());
        this.entityData.set(LOCK_Y, buffer.readFloat());
        this.entityData.set(LOCK_Z, buffer.readFloat());
    }

    @Override
    protected void onHit(HitResult hitResult) {
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > 30) {
            this.discard();
            return;
        }

        if (this.target == null || this.target.isRemoved()) {
            int targetId = this.entityData.get(TARGET_ID);
            if (targetId >= 0) {
                Entity entity = this.level.getEntity(targetId);
                if (entity instanceof LivingEntity living) {
                    this.target = living;
                } else {
                    this.discard();
                    return;
                }
            }
            if (this.target == null || this.target.isRemoved()) {
                this.discard();
                return;
            }
        }

        float size = 1.0F / (float) this.distanceTo(this.target);
        if (size > 1.5F) {
            size = 1.5F;
        }

        for (int i = 0; i < 8; ++i) {
            BotaniaAPI.instance().sparkleFX(this.level,
                    this.getX() + (Math.random() - 0.5D) * 0.1D,
                    this.getY() + (Math.random() - 0.5D) * 0.1D,
                    this.getZ() + (Math.random() - 0.5D) * 0.1D,
                    0.9F + (float) Math.random() * 0.1F,
                    0.2F + (float) Math.random() * 0.2F,
                    0.0F,
                    size,
                    2);
        }

        Vector3 thisVec = Vector3.fromEntityCenter(this);
        Vector3 targetVec = null;
        if (this.target != null) {
            targetVec = Vector3.fromEntityCenter(this.target);
        }
        Vector3 diffVec = targetVec.copy().sub(thisVec);
        Vector3 motionVec = diffVec.copy().normalize().multiply(0.15D);

        this.setDeltaMovement(motionVec.x, motionVec.y, motionVec.z);

        if (!this.level.isClientSide) {
            if (this.target == null || this.target.isRemoved()) {
                this.discard();
                return;
            }

            List<LivingEntity> targetList = this.level.getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(0.1D, 0.1D, 0.1D));
            if (targetList.contains(this.target)) {
                this.discard();
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TargetID", this.entityData.get(TARGET_ID));
        tag.putFloat("LockX", this.entityData.get(LOCK_X));
        tag.putFloat("LockY", this.entityData.get(LOCK_Y));
        tag.putFloat("LockZ", this.entityData.get(LOCK_Z));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(TARGET_ID, tag.getInt("TargetID"));
        this.entityData.set(LOCK_X, tag.getFloat("LockX"));
        this.entityData.set(LOCK_Y, tag.getFloat("LockY"));
        this.entityData.set(LOCK_Z, tag.getFloat("LockZ"));
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
