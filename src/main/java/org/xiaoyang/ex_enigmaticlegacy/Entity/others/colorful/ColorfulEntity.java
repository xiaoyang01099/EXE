package org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.xiaoyang.ex_enigmaticlegacy.Util.BlockUtil;
import org.xiaoyang.ex_enigmaticlegacy.Util.EntityUtil;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ColorfulEntity extends Entity {
    private static final EntityDataAccessor<Float> ORIGIN_X = SynchedEntityData.defineId(ColorfulEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ORIGIN_Y = SynchedEntityData.defineId(ColorfulEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ORIGIN_Z = SynchedEntityData.defineId(ColorfulEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ENDPOINT_X = SynchedEntityData.defineId(ColorfulEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ENDPOINT_Y = SynchedEntityData.defineId(ColorfulEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ENDPOINT_Z = SynchedEntityData.defineId(ColorfulEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(ColorfulEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(ColorfulEntity.class, EntityDataSerializers.FLOAT);
    public static final int LIFETIME = 20;
    private static final double BLOCK_BREAK_RADIUS = 2.0;

    @Nullable
    private UUID ownerUUID;
    Entity master = null;
    private boolean effectApplied = false;
    private boolean useOwnerEyeHitOrigin = true;
    private boolean breakBlocksEnabled = true;

    public ColorfulEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ORIGIN_X, 0f);
        this.entityData.define(ORIGIN_Y, 0f);
        this.entityData.define(ORIGIN_Z, 0f);
        this.entityData.define(ENDPOINT_X, 0f);
        this.entityData.define(ENDPOINT_Y, 0f);
        this.entityData.define(ENDPOINT_Z, 0f);
        this.entityData.define(AGE, 0);
        this.entityData.define(DAMAGE, 0f);
    }

    public void setBeamData(Vec3 origin, Vec3 endpoint, @Nullable Entity master, float damage ) {

        this.entityData.set(ORIGIN_X, (float) origin.x);
        this.entityData.set(ORIGIN_Y, (float) origin.y);
        this.entityData.set(ORIGIN_Z, (float) origin.z);
        this.entityData.set(ENDPOINT_X, (float) endpoint.x);
        this.entityData.set(ENDPOINT_Y, (float) endpoint.y);
        this.entityData.set(ENDPOINT_Z, (float) endpoint.z);
        this.entityData.set(DAMAGE, damage);
        this.ownerUUID = master.getUUID();
        this.master = master;
        Vec3 spawnPosition = getSpawnPosition(origin, endpoint);
        this.setPos(spawnPosition);
    }

    public void setUseOwnerEyeHitOrigin(boolean useOwnerEyeHitOrigin) {
        this.useOwnerEyeHitOrigin = useOwnerEyeHitOrigin;
    }

    public void setBreakBlocksEnabled(boolean enabled) {
        this.breakBlocksEnabled = enabled;
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.ownerUUID = uuid;
    }

    public Vec3 getOrigin(float partialTick) {
        return new Vec3(
                this.entityData.get(ORIGIN_X),
                this.entityData.get(ORIGIN_Y),
                this.entityData.get(ORIGIN_Z)
        );
    }

    public Vec3 getEndpoint(float partialTick) {
        return new Vec3(
                this.entityData.get(ENDPOINT_X),
                this.entityData.get(ENDPOINT_Y),
                this.entityData.get(ENDPOINT_Z)
        );
    }

    private static Vec3 getSpawnPosition(Vec3 origin, Vec3 endpoint) {
        Vec3 direction = endpoint.subtract(origin);
        if (direction.lengthSqr() < 1.0E-6) {
            return origin;
        }
        return origin.add(direction.normalize().scale(0.5));
    }

    public int getAge() {
        return this.entityData.get(AGE);
    }

    public int getLifetime() {
        return LIFETIME;
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    @Override
    public void tick() {
        this.entityData.set(AGE, this.entityData.get(AGE) + 1);
        int age = this.entityData.get(AGE);

        if (!this.level.isClientSide()) {
            if (age == 1 && !effectApplied) {
                applyBeamEffects();
                effectApplied = true;
            }
            if (age > LIFETIME) {
                this.discard();
            }
        }
    }

    private void applyBeamEffects() {
        damageEntities();
        if (breakBlocksEnabled && ConfigFile.canBreakBlock() && this.level instanceof ServerLevel serverLevel) {
            breakBlocks(serverLevel);
        }
    }

    private void damageEntities() {
        Vec3 origin = getOrigin(0);
        Vec3 endpoint = getEndpoint(0);
        float damage = getDamage();
        Player owner = ownerUUID == null ? null : this.level().getPlayerByUUID(ownerUUID);
        Vec3 hitOrigin = useOwnerEyeHitOrigin ? getHitOrigin(origin, endpoint, owner) : origin;



        AABB pathBox = new AABB(hitOrigin, endpoint).inflate(2.5);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(
                LivingEntity.class, pathBox, e -> {
                    if (ownerUUID != null && e.getUUID().equals(ownerUUID)) return false;
                    return e.isAlive();
                }
        );

        for (LivingEntity target : targets) {
            if (rayIntersectsEntity(hitOrigin, endpoint, target)) {
                if (EntityUtil.isInDamageWhitelist(target.getType())) continue;

                if (owner != null) {
                    target.hurt(owner.level().damageSources().playerAttack(owner), damage);
                } else {
                    target.hurt(this.level().damageSources().magic(), damage);
                }
            }
        }
    }

    private Vec3 getHitOrigin(Vec3 origin, Vec3 endpoint, @Nullable Player owner) {
        Vec3 hitOrigin = owner == null ? origin : owner.getEyePosition();
        Vec3 direction = endpoint.subtract(hitOrigin);
        if (direction.lengthSqr() < 1.0E-6) {
            direction = endpoint.subtract(origin);
        }
        if (direction.lengthSqr() < 1.0E-6) {
            return hitOrigin;
        }
        return hitOrigin.subtract(direction.normalize().scale(0.5));
    }

    private void breakBlocks(ServerLevel serverLevel) {
        Vec3 origin = getOrigin(0);
        Vec3 endpoint = getEndpoint(0);
        Vec3 segment = endpoint.subtract(origin);
        origin = origin.add(segment.normalize());

        double lengthSqr = segment.lengthSqr();
        if (lengthSqr < 0.0025) return;

        AABB breakBox = new AABB(origin, endpoint).inflate(BLOCK_BREAK_RADIUS);
        BlockPos min = BlockPos.containing(breakBox.minX, breakBox.minY, breakBox.minZ);
        BlockPos max = BlockPos.containing(breakBox.maxX, breakBox.maxY, breakBox.maxZ);
        double radiusSqr = BLOCK_BREAK_RADIUS * BLOCK_BREAK_RADIUS;

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockPos immutablePos = pos.immutable();
            Vec3 center = Vec3.atCenterOf(immutablePos);
            if (distanceToSegmentSqr(center, origin, segment, lengthSqr) > radiusSqr) continue;
            if(master != null && !BlockUtil.isPlaceBlock(serverLevel, master, immutablePos)) continue;
            tryBreakBlock(serverLevel, immutablePos);
        }
    }

    private static double distanceToSegmentSqr(Vec3 point, Vec3 origin, Vec3 segment, double lengthSqr) {
        double t = point.subtract(origin).dot(segment) / lengthSqr;
        t = Math.max(0.0, Math.min(1.0, t));
        Vec3 closest = origin.add(segment.scale(t));
        return point.distanceToSqr(closest);
    }

    private boolean tryBreakBlock(ServerLevel serverLevel, BlockPos pos) {
        BlockState state = serverLevel.getBlockState(pos);
        if (state.isAir()) return false;
        if (state.getDestroySpeed(serverLevel, pos) < 0.0f) return false;
        return serverLevel.destroyBlock(pos, true, this);
    }

    private boolean rayIntersectsEntity(Vec3 origin, Vec3 endpoint, LivingEntity entity) {
        AABB box = entity.getBoundingBox().inflate(2.0);
        return box.clip(origin, endpoint).isPresent();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
