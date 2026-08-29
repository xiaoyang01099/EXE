package org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.xiaoyang.ex_enigmaticlegacy.Util.EntityUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * 电磁炮光束实体
 * 纯数据容器 + 单次伤害触发器，不移动、不参与物理。
 * 客户端通过 SynchedEntityData 读取 origin/endpoint 用于渲染。
 */
public class RailgunBeamEntity extends Entity {

    // ========== SynchedEntityData：网络同步字段 ==========
    private static final EntityDataAccessor<Float> ORIGIN_X = SynchedEntityData.defineId(RailgunBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ORIGIN_Y = SynchedEntityData.defineId(RailgunBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ORIGIN_Z = SynchedEntityData.defineId(RailgunBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ENDPOINT_X = SynchedEntityData.defineId(RailgunBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ENDPOINT_Y = SynchedEntityData.defineId(RailgunBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ENDPOINT_Z = SynchedEntityData.defineId(RailgunBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(RailgunBeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(RailgunBeamEntity.class, EntityDataSerializers.FLOAT);

    // ========== 常量 ==========
    /** 光束总生命周期（tick），约 1 秒 */
    public static final int LIFETIME = 20;

    // ========== 本地字段（服务端） ==========
    @Nullable
    private UUID ownerUUID;
    private boolean damageDealt = false;

    // ========== 构造 ==========
    public RailgunBeamEntity(EntityType<?> type, Level level) {
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

    // ========== 设置光束数据（服务端调用） ==========
    public void setBeamData(Vec3 origin, Vec3 endpoint, @Nullable UUID owner, float damage) {
        this.entityData.set(ORIGIN_X, (float) origin.x);
        this.entityData.set(ORIGIN_Y, (float) origin.y);
        this.entityData.set(ORIGIN_Z, (float) origin.z);
        this.entityData.set(ENDPOINT_X, (float) endpoint.x);
        this.entityData.set(ENDPOINT_Y, (float) endpoint.y);
        this.entityData.set(ENDPOINT_Z, (float) endpoint.z);
        this.entityData.set(DAMAGE, damage);
        this.ownerUUID = owner;
        // 实体生成位置放到光束起点朝向前方 0.5 格，避免落在远端终点。
        this.setPos(getSpawnPosition(origin, endpoint));
    }

    // ========== 客户端读取接口 ==========
    public Vec3 getOrigin(float partialTick) {
        return new Vec3(
                this.entityData.get(ORIGIN_X),
                this.entityData.get(ORIGIN_Y),
                this.entityData.get(ORIGIN_Z)
        );
    }

    private static Vec3 getSpawnPosition(Vec3 origin, Vec3 endpoint) {
        Vec3 direction = endpoint.subtract(origin);
        if (direction.lengthSqr() < 1.0E-6) {
            return origin;
        }
        return origin.add(direction.normalize().scale(0.5));
    }

    public Vec3 getEndpoint(float partialTick) {
        return new Vec3(
                this.entityData.get(ENDPOINT_X),
                this.entityData.get(ENDPOINT_Y),
                this.entityData.get(ENDPOINT_Z)
        );
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

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    // ========== 核心 Tick 逻辑 ==========
    @Override
    public void tick() {
        // 每 tick 递增年龄
        this.entityData.set(AGE, this.entityData.get(AGE) + 1);
        int age = this.entityData.get(AGE);

        // 仅服务端执行伤害逻辑
        if (!this.level().isClientSide()) {
            // 第 1 tick：对路径上所有实体造成一次穿透伤害
            if (age == 1 && !damageDealt) {
                performHitDetection();
                damageDealt = true;
            }
            // 生命周期结束，移除实体
            if (age > LIFETIME) {
                this.discard();
            }
        }
    }

    // ========== 穿透伤害检测 ==========
    private void performHitDetection() {
        Vec3 origin = getOrigin(0);
        Vec3 endpoint = getEndpoint(0);
        float damage = getDamage();
        Player owner = ownerUUID == null ? null : this.level().getPlayerByUUID(ownerUUID);
        Vec3 hitOrigin = getHitOrigin(origin, endpoint, owner);

        // 构建路径上的 AABB 搜索区域
        AABB pathBox = new AABB(hitOrigin, endpoint).inflate(2);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(
                LivingEntity.class, pathBox, e -> {
                    // 排除发射者自己
                    if (ownerUUID != null && e.getUUID().equals(ownerUUID)) return false;
                    return e.isAlive();
                }
        );

        // 对每个命中实体造成伤害
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

    // 伤害检测使用玩家眼位瞄准线，避免手部光束起点在近距离漏掉正前方实体。
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

    // ========== 线段-AABB 碰撞检测 ==========
    private boolean rayIntersectsEntity(Vec3 origin, Vec3 endpoint, LivingEntity entity) {
        AABB box = entity.getBoundingBox().inflate(1.2);
        // 使用 AABB.clip 检测线段是否穿过包围盒
        return box.clip(origin, endpoint).isPresent();
    }

    // ========== NBT 序列化（noSave 实体通常不需要，但保留以防万一） ==========
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    // ========== 网络包（Forge 需要） ==========
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
