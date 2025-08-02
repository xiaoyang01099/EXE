package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import vazkii.botania.api.BotaniaAPI;

import javax.annotation.Nullable;
import java.util.List;

public class EntitySoulEnergy extends ThrowableProjectile implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(EntitySoulEnergy.class, EntityDataSerializers.INT);

    @Nullable
    private LivingEntity target;

    public EntitySoulEnergy(EntityType<? extends EntitySoulEnergy> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public EntitySoulEnergy(EntityType<? extends EntitySoulEnergy> entityType, Level level, LivingEntity owner, LivingEntity target) {
        super(entityType, owner, level);
        this.target = target;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        int id = -1;
        if (this.target != null) {
            id = this.target.getId();
        }
        buffer.writeInt(id);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        int id = buffer.readInt();
        try {
            if (id >= 0) {
                Entity entity = this.level.getEntity(id);
                if (entity instanceof LivingEntity) {
                    this.target = (LivingEntity) entity;
                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() == this.target) {
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > 1000) {
            this.discard();
            return;
        }

        if (this.level.isClientSide) {
            createParticleTrail();
        }

        List<LivingEntity> targetList = this.level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        this.getX() - 0.5, this.getY() - 0.5, this.getZ() - 0.5,
                        this.getX() + 0.5, this.getY() + 0.5, this.getZ() + 0.5
                )
        );

        if (targetList.contains(this.target)) {
            if (this.level.isClientSide) {
                createHitParticles();
            } else {
                this.level.playSound(null, this.target.getX(), this.target.getY(), this.target.getZ(),
                        SoundEvents.GENERIC_DRINK, this.target.getSoundSource(), 0.6F, 0.8F + this.random.nextFloat() * 0.2F);

                this.target.heal(1.0F);
                if (this.target instanceof Player player) {
                    player.getFoodData().eat(1, 1.0F);
                }
            }
            this.discard();
            return;
        }

        if (this.target != null && this.target.isAlive()) {
            double distance = this.distanceToSqr(this.target);
            double dx = this.target.getX() - this.getX();
            double dy = this.target.getBoundingBox().minY + this.target.getBbHeight() * 0.6 - this.getY();
            double dz = this.target.getZ() - this.getZ();

            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length > 0) {
                dx /= length;
                dy /= length;
                dz /= length;
            }

            double acceleration = 0.3;
            Vec3 motion = this.getDeltaMovement();
            motion = motion.add(dx * acceleration, dy * acceleration, dz * acceleration);

            double maxSpeed = 0.35;
            motion = new Vec3(
                    Mth.clamp(motion.x, -maxSpeed, maxSpeed),
                    Mth.clamp(motion.y, -maxSpeed, maxSpeed),
                    Mth.clamp(motion.z, -maxSpeed, maxSpeed)
            );

            this.setDeltaMovement(motion);
        } else {
            this.discard();
        }
    }

    private void createParticleTrail() {
        Vec3 lastPos = new Vec3(this.xOld, this.yOld - this.getBbHeight() + this.getBbHeight() / 2.0F, this.zOld);
        Vector3 thisVec = Vector3.fromEntityCenter(this);
        Vector3 oldPos = new Vector3(lastPos);
        Vector3 diff = thisVec.copy().sub(oldPos);
        Vector3 step = diff.copy().normalize().multiply(0.05);

        if (step.mag() > 0) {
            int steps = (int)(diff.mag() / step.mag());
            Vector3 particlePos = oldPos.copy();
            float rc = 1.0F;
            float gc = 1.0F;
            float bc = 1.0F;

            for (int i = 0; i < steps; i++) {
                int motionValue = Math.max(1, 2);
                BotaniaAPI.instance().sparkleFX(this.level, particlePos.x, particlePos.y, particlePos.z, rc, gc, bc, 0.8F, motionValue);
                if (this.random.nextInt(Math.max(1, steps)) <= 1) {
                    BotaniaAPI.instance().sparkleFX(this.level,
                            particlePos.x + (this.random.nextDouble() - 0.5) * 0.4,
                            particlePos.y + (this.random.nextDouble() - 0.5) * 0.4,
                            particlePos.z + (this.random.nextDouble() - 0.5) * 0.4,
                            rc, gc, bc, 0.8F, 2);
                }
                particlePos.add(step);
            }
        }
    }

    private void createHitParticles() {
        for (int i = 0; i <= 6; i++) {
            float r = 1.0F;
            float g = 1.0F;
            float b = 1.0F;
            float s = 0.1F + this.random.nextFloat() * 0.1F;
            float m = 0.15F;
            float xm = (this.random.nextFloat() - 0.5F) * m;
            float ym = (this.random.nextFloat() - 0.5F) * m;
            float zm = (this.random.nextFloat() - 0.5F) * m;

            int motionValue = Math.max(5, 10);
            BotaniaAPI.instance().sparkleFX(this.level,
                    this.getX() + this.getBbWidth() / 2.0F,
                    this.getY() + this.getBbHeight() / 2.0F,
                    this.getZ() + this.getBbWidth() / 2.0F,
                    r, g, b, s, motionValue);
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.target != null) {
            compound.putInt("TargetID", this.target.getId());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("TargetID")) {
            int targetId = compound.getInt("TargetID");
            Entity entity = this.level.getEntity(targetId);
            if (entity instanceof LivingEntity) {
                this.target = (LivingEntity) entity;
            }
        }
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public float getPickRadius() {
        return 0.1F;
    }
}