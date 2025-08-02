package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.LunarBurstMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.LunarFlaresParticleMessage;
import vazkii.botania.api.BotaniaAPI;

import java.util.List;

public class EntityLunarFlare extends ThrowableProjectile {

    private static final EntityDataAccessor<Integer> LOCK_X = SynchedEntityData.defineId(EntityLunarFlare.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LOCK_Y = SynchedEntityData.defineId(EntityLunarFlare.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LOCK_Z = SynchedEntityData.defineId(EntityLunarFlare.class, EntityDataSerializers.INT);

    private int lockX;
    private int lockY;
    private int lockZ;

    private LivingEntity attacker;

    public EntityLunarFlare(EntityType<? extends EntityLunarFlare> entityType, Level level) {
        super(entityType, level);
        this.setSize(0.0F, 0.0F);
    }

    public EntityLunarFlare(EntityType<? extends EntityLunarFlare> entityType, Level level, LivingEntity shooter, int x, int y, int z) {
        super(entityType, shooter, level);
        this.attacker = shooter;
        this.lockX = x;
        this.lockY = y;
        this.lockZ = z;
        this.setSize(0.0F, 0.0F);

        this.entityData.set(LOCK_X, x);
        this.entityData.set(LOCK_Y, y);
        this.entityData.set(LOCK_Z, z);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(LOCK_X, 0);
        this.entityData.define(LOCK_Y, 0);
        this.entityData.define(LOCK_Z, 0);
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > 1000) {
            this.discard();
            return;
        }

        this.lockX = this.entityData.get(LOCK_X);
        this.lockY = this.entityData.get(LOCK_Y);
        this.lockZ = this.entityData.get(LOCK_Z);

        generateTrailParticles();
    }


    private void generateTrailParticles() {
        if (!this.level.isClientSide) return;

        double lastTickPosX = this.xo;
        double lastTickPosY = this.yo - this.getBbHeight() + (this.getBbHeight() / 2.0F);
        double lastTickPosZ = this.zo;

        Vector3 thisVec = Vector3.fromEntityCenter(this);
        Vector3 oldPos = new Vector3(lastTickPosX, lastTickPosY, lastTickPosZ);
        Vector3 diff = thisVec.copy().sub(oldPos);
        Vector3 step = diff.copy().normalize().multiply(0.05);

        int steps = (int)(diff.mag() / step.mag());
        Vector3 particlePos = oldPos.copy();
        float rc = 0.0F;

        for (int i = 0; i < steps; i++) {
            BotaniaAPI.instance().sparkleFX(
                    this.level,
                    particlePos.x + (Math.random() - 0.5) * 0.2,
                    particlePos.y + (Math.random() - 0.5) * 0.2,
                    particlePos.z + (Math.random() - 0.5) * 0.2,
                    rc,
                    (float)(0.8 + Math.random() * 0.2),
                    (float)(0.4 + Math.random() * 0.6),
                    2.0F,
                    1
            );

            if (this.level.random.nextInt(steps) <= 1) {
                BotaniaAPI.instance().sparkleFX(
                        this.level,
                        particlePos.x + (Math.random() - 0.5) * 1.0,
                        particlePos.y + (Math.random() - 0.5) * 1.0,
                        particlePos.z + (Math.random() - 0.5) * 1.0,
                        rc,
                        (float)(0.8 + Math.random() * 0.2),
                        (float)(0.4 + Math.random() * 0.6),
                        2.4F,
                        4
                );
            }

            particlePos.add(step);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.getOwner() == null && !this.level.isClientSide) {
            this.discard();
            return;
        }

        if (result.getEntity() != this.getOwner() && !this.level.isClientSide) {
            result.getEntity().hurt(new ModDamageSources.DamageSourceMagic(this.getOwner()), 100.0F);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        BlockPos hitPos = result.getBlockPos();

        if (hitPos.getX() == this.lockX && hitPos.getY() == this.lockY && hitPos.getZ() == this.lockZ) {
            handleTargetHit(hitPos);
        }
    }

    private void handleTargetHit(BlockPos targetPos) {
        if (this.level.isClientSide) return;

        AABB damageArea = new AABB(targetPos).inflate(2.5);
        List<LivingEntity> affectedEntities = this.level.getEntitiesOfClass(LivingEntity.class, damageArea);

        if (this.getOwner() instanceof LivingEntity) {
            affectedEntities.remove(this.getOwner());
        }

        for (LivingEntity entity : affectedEntities) {
            if (!entity.isRemoved()) {

                entity.hurt(new ModDamageSources.DamageSourceMagic(this.getOwner()), 75.0F);

                Vector3 targetPos3 = Vector3.fromEntityCenter(entity);
                Vector3 thisPos3 = Vector3.fromEntityCenter(this);
                Vector3 diff = targetPos3.copy().sub(thisPos3);
                diff.normalize();
                diff.multiply(1.0 / entity.distanceTo(this));

                if (diff.mag() > 1.0) {
                    diff.normalize();
                }

                if (entity instanceof EnderDragon || entity instanceof WitherBoss) {
                    diff.multiply(0.5);
                }

                Vec3 knockback = diff.toVec3();
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            }
        }

        if (this.level instanceof ServerLevel serverLevel) {
            LunarFlaresParticleMessage particleMessage = new LunarFlaresParticleMessage(
                    targetPos.getX() + 0.5,
                    targetPos.getY() + 1.25,
                    targetPos.getZ() + 0.5,
                    48
            );

            NetworkHandler.CHANNEL.send(PacketDistributor.NEAR.with(() ->
                    new PacketDistributor.TargetPoint(
                            targetPos.getX(), targetPos.getY(), targetPos.getZ(), 128.0, serverLevel.dimension()
                    )), particleMessage);

            LunarBurstMessage burstMessage = new LunarBurstMessage(
                    targetPos.getX() + 0.5,
                    targetPos.getY() + 1.5,
                    targetPos.getZ() + 0.5,
                    2.0F
            );

            NetworkHandler.CHANNEL.send(PacketDistributor.NEAR.with(() ->
                    new PacketDistributor.TargetPoint(
                            targetPos.getX(), targetPos.getY(), targetPos.getZ(), 128.0, serverLevel.dimension()
                    )), burstMessage);
        }

        BlockState blockState = this.level.getBlockState(targetPos);
        this.level.levelEvent(2001, targetPos,
                Block.getId(blockState));

        this.level.playSound(null, targetPos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                16.0F, 0.8F + this.level.random.nextFloat() * 0.2F);

        this.discard();
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void setTargetPosition(int x, int y, int z) {
        this.lockX = x;
        this.lockY = y;
        this.lockZ = z;
        this.entityData.set(LOCK_X, x);
        this.entityData.set(LOCK_Y, y);
        this.entityData.set(LOCK_Z, z);
    }

    public BlockPos getTargetPosition() {
        return new BlockPos(
                this.entityData.get(LOCK_X),
                this.entityData.get(LOCK_Y),
                this.entityData.get(LOCK_Z)
        );
    }

    private void setSize(float width, float height) {
        this.dimensions = this.dimensions.scale(width, height);
        this.refreshDimensions();
    }
}