package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;
import net.xiaoyang010.ex_enigmaticlegacy.Network.ClientProxy;

import java.util.List;
import java.util.Random;

public class EntityChaoticOrb extends ThrowableProjectile {
    private static final EntityDataAccessor<Boolean> DATA_SEEKER = SynchedEntityData.defineId(EntityChaoticOrb.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID = SynchedEntityData.defineId(EntityChaoticOrb.class, EntityDataSerializers.INT);
    private int count = 0;
    private static final float CHAOS_TOME_DAMAGE_CAP = 100.0F;

    public EntityChaoticOrb(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public EntityChaoticOrb(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, boolean seeker) {
        super(entityType, shooter, level);
        this.setSeeker(seeker);
        this.setOwnerId(shooter.getId());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_SEEKER, false);
        this.entityData.define(DATA_OWNER_ID, -1);
    }

    public boolean isSeeker() {
        return this.entityData.get(DATA_SEEKER);
    }

    public void setSeeker(boolean seeker) {
        this.entityData.set(DATA_SEEKER, seeker);
    }

    public int getOwnerId() {
        return this.entityData.get(DATA_OWNER_ID);
    }

    public void setOwnerId(int ownerId) {
        this.entityData.set(DATA_OWNER_ID, ownerId);
    }

    @Override
    protected float getGravity() {
        return 0.001F;
    }

    @Override
    public void tick() {
        super.tick();
        ++this.count;

        BlockPos pos = this.blockPosition();
        BlockState state = this.level.getBlockState(pos);
        if (state.is(Blocks.NETHER_PORTAL) || state.is(Blocks.END_PORTAL)) {
            this.onHit(new BlockHitResult(this.position(), this.getDirection(), pos, false));
        }

        if (this.level.isClientSide) {
            for (int a = 0; a < 6; ++a) {
                double offsetX = (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F;
                double offsetY = (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F;
                double offsetZ = (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F;

                ClientProxy.wispFX4(
                        (ClientLevel) this.level,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        this,
                        a,
                        true,
                        0.0F
                );
            }

            ClientProxy.wispFX2(
                    (ClientLevel) this.level,
                    this.getX() + (double)((this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F),
                    this.getY() + (double)((this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F),
                    this.getZ() + (double)((this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F),
                    0.1F,
                    this.level.random.nextInt(6),
                    true,
                    true,
                    0.0F
            );
        }

        Random rr = new Random((long)(this.getId() + this.count));
        if (this.tickCount > 20) {
            if (!this.isSeeker()) {
                this.setDeltaMovement(
                        this.getDeltaMovement().x + (rr.nextFloat() - rr.nextFloat()) * 0.01F,
                        this.getDeltaMovement().y + (rr.nextFloat() - rr.nextFloat()) * 0.01F,
                        this.getDeltaMovement().z + (rr.nextFloat() - rr.nextFloat()) * 0.01F
                );
            } else {
                List<Entity> entities = RelicsEventHandler.getEntitiesInRange(
                        this.level, this.getX(), this.getY(), this.getZ(),
                        this, LivingEntity.class, 16.0D
                );

                double minDistance = Double.MAX_VALUE;
                Entity target = null;

                for (Entity entity : entities) {
                    if ((entity.getId() != this.getOwnerId() || !(Math.random() < 0.8)) && entity.isAlive()) {
                        double distance = this.distanceToSqr(entity);
                        if (distance < minDistance) {
                            minDistance = distance;
                            target = entity;
                        }
                    }
                }

                if (target != null) {
                    double dx = target.getX() - this.getX();
                    double dy = target.getBoundingBox().minY + target.getBbHeight() * 0.9 - this.getY();
                    double dz = target.getZ() - this.getZ();

                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (distance > 0) {
                        dx /= distance;
                        dy /= distance;
                        dz /= distance;

                        Vec3 motion = this.getDeltaMovement();
                        this.setDeltaMovement(
                                motion.x + dx * 0.2D,
                                motion.y + dy * 0.2D,
                                motion.z + dz * 0.2D
                        );

                        Vec3 newMotion = this.getDeltaMovement();
                        this.setDeltaMovement(
                                Mth.clamp(newMotion.x, -0.2D, 0.2D),
                                Mth.clamp(newMotion.y, -0.2D, 0.2D),
                                Mth.clamp(newMotion.z, -0.2D, 0.2D)
                        );
                    }
                }
            }
        }

        if (this.tickCount > 5000) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (this.level.isClientSide) {
            for (int a = 0; a < 6; ++a) {
                for (int b = 0; b < 6; ++b) {
                    float fx = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
                    float fy = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
                    float fz = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;

                    ClientProxy.wispFX3(
                            (ClientLevel) this.level,
                            this.getX() + fx,
                            this.getY() + fy,
                            this.getZ() + fz,
                            this.getX() + (fx * 10.0F),
                            this.getY() + (fy * 10.0F),
                            this.getZ() + (fz * 10.0F),
                            0.4F, b, true, 0.05F
                    );
                }
            }
        }

        if (!this.level.isClientSide) {
            float specialChance = 1.0F;
            float explosionPower = 2.0F;

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
                BlockState state = this.level.getBlockState(pos);
                if (state.is(Blocks.NETHER_PORTAL) || state.is(Blocks.END_PORTAL) || state.is(Blocks.END_GATEWAY)) {
                    explosionPower = 4.0F;
                    specialChance = 10.0F;
                }
            }

            if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) hitResult;
                Entity thrower = this.getOwner();
                if (thrower != null) {
                    float damage = (float)(1.0F + Math.random() * CHAOS_TOME_DAMAGE_CAP);
                    entityHit.getEntity().hurt(new ModDamageSources.DamageSourceMagic(thrower), damage);
                }
            }

            this.level.explode(null, this.getX(), this.getY(), this.getZ(),
                    (float)(1.0F + Math.random() * 6.0F), Explosion.BlockInteraction.BREAK);

            if (!this.isSeeker() && this.random.nextInt(100) <= specialChance) {
                if (this.random.nextBoolean()) {
                    this.taintExplosion();
                } else {
                    this.createMagicalEffect();
                }
            }

            this.discard();
        }
    }

    private void taintExplosion() {
        int x = (int) this.getX();
        int y = (int) this.getY();
        int z = (int) this.getZ();

        for (int a = 0; a < 10; ++a) {
            int xx = x + (int)((this.random.nextFloat() - this.random.nextFloat()) * 6.0F);
            int yy = y + (int)((this.random.nextFloat() - this.random.nextFloat()) * 3.0F);
            int zz = z + (int)((this.random.nextFloat() - this.random.nextFloat()) * 6.0F);

            BlockPos pos = new BlockPos(xx, yy, zz);
            if (this.level.isEmptyBlock(pos) && this.random.nextBoolean()) {
                this.level.setBlock(pos, Blocks.MYCELIUM.defaultBlockState(), 3);
            }
        }
    }

    private void createMagicalEffect() {
        if (this.level instanceof ServerLevel serverLevel) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.AMBIENT, 1.0F, 1.0F);

            for (int i = 0; i < 5; i++) {
                int xx = (int) (this.getX() + (this.random.nextFloat() - 0.5F) * 4);
                int yy = (int) (this.getY() + (this.random.nextFloat() - 0.5F) * 2);
                int zz = (int) (this.getZ() + (this.random.nextFloat() - 0.5F) * 4);

                BlockPos pos = new BlockPos(xx, yy, zz);
                if (this.level.isEmptyBlock(pos)) {
                    this.level.setBlock(pos, Blocks.DIAMOND_BLOCK.defaultBlockState(), 3);
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Seeker", this.isSeeker());
        compound.putInt("OwnerId", this.getOwnerId());
        compound.putInt("Count", this.count);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setSeeker(compound.getBoolean("Seeker"));
        this.setOwnerId(compound.getInt("OwnerId"));
        this.count = compound.getInt("Count");
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
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