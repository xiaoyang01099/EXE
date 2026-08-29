package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.xiaoyang.ex_enigmaticlegacy.Item.FlySwordPlusItem;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;

import javax.annotation.Nullable;
import java.util.UUID;

// ExcaliburChargeEntity 是真·飞剑咖喱棒蓄力同步实体，负责跟随玩家中心并驱动客户端螺旋和粒子渲染。
public class ExcaliburChargeEntity extends Entity {
    public static final int RELEASE_VISUAL_TICKS = 10; // 松键释放后的强化螺旋持续 tick。
    public static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(ExcaliburChargeEntity.class, EntityDataSerializers.INT); // 蓄力玩家实体 ID。
    public static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(ExcaliburChargeEntity.class, EntityDataSerializers.INT); // 蓄力实体年龄。
    public static final EntityDataAccessor<Integer> RELEASE_AGE = SynchedEntityData.defineId(ExcaliburChargeEntity.class, EntityDataSerializers.INT); // 释放阶段年龄。
    public static final EntityDataAccessor<Integer> FULL_CHARGE_TICKS = SynchedEntityData.defineId(ExcaliburChargeEntity.class, EntityDataSerializers.INT); // 满蓄力 tick。
    public static final EntityDataAccessor<Integer> MAX_CHARGE_TICKS = SynchedEntityData.defineId(ExcaliburChargeEntity.class, EntityDataSerializers.INT); // 最大蓄力 tick。
    public static final EntityDataAccessor<Integer> VISUAL_SEED = SynchedEntityData.defineId(ExcaliburChargeEntity.class, EntityDataSerializers.INT); // 视觉随机种子。
    public static final EntityDataAccessor<Boolean> RELEASED = SynchedEntityData.defineId(ExcaliburChargeEntity.class, EntityDataSerializers.BOOLEAN); // 是否进入释放视觉阶段。
    public static final EntityDataAccessor<Byte> HAND_ID = SynchedEntityData.defineId(ExcaliburChargeEntity.class, EntityDataSerializers.BYTE); // 使用手同步字段。
    @Nullable
    public UUID ownerUUID; // 蓄力玩家 UUID。
    public int clientLastParticleTick = -1; // 客户端持续粒子提交防重 tick。
    public boolean clientTenTickBurstPlayed = false; // 客户端第 10 tick 双材质爆发防重标记。
    public boolean clientEnhancedSoundPlayed = false; // 客户端增强阶段音效防重标记。

    public ExcaliburChargeEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    // 按玩家当前状态创建服务端咖喱棒蓄力同步实体。
    public static ExcaliburChargeEntity create(Player owner, InteractionHand hand) {
        ExcaliburChargeEntity entity = new ExcaliburChargeEntity(ModEntities.EXCALIBUR_CHARGE.get(), owner.level());
        entity.ownerUUID = owner.getUUID();
        entity.entityData.set(OWNER_ID, owner.getId());
        entity.entityData.set(HAND_ID, (byte) (hand == InteractionHand.OFF_HAND ? 1 : 0));
        entity.entityData.set(FULL_CHARGE_TICKS, FlySwordPlusItem.getExcaliburFullChargeTicks());
        entity.entityData.set(MAX_CHARGE_TICKS, FlySwordPlusItem.EXCALIBUR_MAX_CHARGE_TICKS);
        entity.entityData.set(VISUAL_SEED, owner.getRandom().nextInt());
        entity.setPos(entity.getPlayerCenterAnchor(owner, 1.0F));
        return entity;
    }

    @Override
    public void defineSynchedData() {
        this.entityData.define(OWNER_ID, -1);
        this.entityData.define(AGE, 0);
        this.entityData.define(RELEASE_AGE, 0);
        this.entityData.define(FULL_CHARGE_TICKS, FlySwordPlusItem.getExcaliburFullChargeTicks());
        this.entityData.define(MAX_CHARGE_TICKS, FlySwordPlusItem.EXCALIBUR_MAX_CHARGE_TICKS);
        this.entityData.define(VISUAL_SEED, 0);
        this.entityData.define(RELEASED, false);
        this.entityData.define(HAND_ID, (byte) 0);
    }

    @Override
    public void tick() {
        super.tick();
        this.entityData.set(AGE, this.getChargeAge() + 1);
        if (this.isReleased()) {
            this.entityData.set(RELEASE_AGE, this.getReleaseAge() + 1);
        }

        Player owner = this.getOwnerPlayer();
        if (owner != null) {
            this.setPos(this.getPlayerCenterAnchor(owner, 1.0F));
            this.setYRot(owner.getYRot());
            this.setXRot(owner.getXRot());
        }

        if (this.level().isClientSide()) return;
        if (owner == null || !owner.isAlive() || this.getChargeAge() > this.getMaxChargeTicks() + RELEASE_VISUAL_TICKS) {
            this.discard();
            return;
        }
        if (this.isReleased() && this.getReleaseAge() >= RELEASE_VISUAL_TICKS) {
            this.discard();
        }
    }

    // 让实体进入松键后的短暂释放视觉阶段。
    public void startReleaseVisual() {
        this.entityData.set(RELEASED, true);
        this.entityData.set(RELEASE_AGE, 0);
    }

    @Nullable
    public Player getOwnerPlayer() {
        Entity entity = this.level().getEntity(this.entityData.get(OWNER_ID));
        if (entity instanceof Player player) return player;
        if (this.ownerUUID != null) return this.level().getPlayerByUUID(this.ownerUUID);
        return null;
    }

    // 根据玩家身体中心生成螺旋锚点，服务端和客户端都使用同一套近似规则。
    public Vec3 getPlayerCenterAnchor(Player owner, float partialTick) {
        if (owner == null) return this.position();
        return owner.getPosition(partialTick).add(0.0D, owner.getBbHeight() * 0.5D, 0.0D);
    }

    public InteractionHand getHand() {
        return this.entityData.get(HAND_ID) == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    public int getChargeAge() {
        return this.entityData.get(AGE);
    }

    public int getReleaseAge() {
        return this.entityData.get(RELEASE_AGE);
    }

    public int getFullChargeTicks() {
        return Math.max(1, this.entityData.get(FULL_CHARGE_TICKS));
    }

    public int getMaxChargeTicks() {
        return Math.max(this.getFullChargeTicks(), this.entityData.get(MAX_CHARGE_TICKS));
    }

    public int getVisualSeed() {
        return this.entityData.get(VISUAL_SEED);
    }

    public boolean isReleased() {
        return this.entityData.get(RELEASED);
    }

    public static Vec3 safeNormalize(Vec3 value, Vec3 fallback) {
        if (value == null || value.lengthSqr() < 1.0E-8D) return fallback;
        return value.normalize();
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    // 咖喱棒蓄力实体只表示当前服务端会话，不写入世界存档。
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) this.ownerUUID = tag.getUUID("Owner");
        if (tag.contains("OwnerId")) this.entityData.set(OWNER_ID, tag.getInt("OwnerId"));
        if (tag.contains("HandId")) this.entityData.set(HAND_ID, tag.getByte("HandId"));
        if (tag.contains("VisualSeed")) this.entityData.set(VISUAL_SEED, tag.getInt("VisualSeed"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUUID != null) tag.putUUID("Owner", this.ownerUUID);
        tag.putInt("OwnerId", this.entityData.get(OWNER_ID));
        tag.putByte("HandId", this.entityData.get(HAND_ID));
        tag.putInt("VisualSeed", this.entityData.get(VISUAL_SEED));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
