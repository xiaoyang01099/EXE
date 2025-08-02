package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;
import vazkii.botania.api.BotaniaAPI;

import java.util.LinkedList;
import java.util.List;

public class EntityRageousMissile extends ThrowableProjectile {
    private static final String TAG_TIME = "time";
    private static final float DAMAGE_MIN = 24.0F;
    private static final float DAMAGE_MAX = 32.0F;

    private static final EntityDataAccessor<Boolean> DATA_EVIL =
            SynchedEntityData.defineId(EntityRageousMissile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
            SynchedEntityData.defineId(EntityRageousMissile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_THROWER_NAME =
            SynchedEntityData.defineId(EntityRageousMissile.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_RED =
            SynchedEntityData.defineId(EntityRageousMissile.class, EntityDataSerializers.BOOLEAN);


    private double lockX;
    private double lockY = -1.0;
    private double lockZ;
    private int time = 0;

    public EntityRageousMissile(EntityType<? extends ThrowableProjectile> type, Level world) {
        super(type, world);
    }

    public EntityRageousMissile(EntityType<? extends ThrowableProjectile> type, Player thrower, boolean evil, Level world) {
        super(type, world);
        this.setOwner(thrower);
        this.setEvil(evil);
        this.setThrowerName(thrower.getDisplayName().getString());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_EVIL, false);
        this.entityData.define(DATA_TARGET_ID, -1);
        this.entityData.define(DATA_THROWER_NAME, "");
        this.entityData.define(DATA_RED, false);
    }

    public boolean isRed() {
        return this.entityData.get(DATA_RED);
    }

    public void setRed(boolean red) {
        this.entityData.set(DATA_RED, red);
    }

    public void setEvil(boolean evil) {
        this.entityData.set(DATA_EVIL, evil);
    }

    public void setThrowerName(String name) {
        this.entityData.set(DATA_THROWER_NAME, name);
    }

    public boolean isEvil() {
        return this.entityData.get(DATA_EVIL);
    }

    public Player getTrueThrower() {
        String name = this.entityData.get(DATA_THROWER_NAME);
        if (name == null || name.isEmpty()) {
            return null;
        }
        if (!this.level.isClientSide && this.level.getServer() != null) {
            return this.level.getServer().getPlayerList().getPlayerByName(name);
        } else {
            for (Player player : this.level.players()) {
                if (player.getName().getString().equals(name)) {
                    return player;
                }
            }
        }
        return null;
    }

    public void setTarget(LivingEntity e) {
        this.entityData.set(DATA_TARGET_ID, e == null ? -1 : e.getId());
    }

    public LivingEntity getTargetEntity() {
        int id = this.entityData.get(DATA_TARGET_ID);
        Entity e = this.level.getEntity(id);
        return e instanceof LivingEntity ? (LivingEntity) e : null;
    }

    @Override
    public void tick() {
        double lastTickPosX = this.xOld;
        double lastTickPosY = this.yOld - this.getBbHeight() + (this.getBbHeight() / 2.0F);
        double lastTickPosZ = this.zOld;
        super.tick();

        if (!this.level.isClientSide && !this.getTarget() && this.time > 160) {
            this.discard();
        } else {
            boolean evil = this.isEvil();
            Vector3 thisVec = Vector3.fromEntityCenter(this);
            Vector3 oldPos = new Vector3(lastTickPosX, lastTickPosY, lastTickPosZ);
            Vector3 diff = thisVec.copy().sub(oldPos);
            Vector3 step = diff.copy().normalize().multiply(0.05);
            int steps = (int)(diff.mag() / step.mag());
            Vector3 particlePos = oldPos.copy();
            float rc = 0.0F;

            for (int i = 0; i < steps; i++) {
                BotaniaAPI.instance().sparkleFX(this.level, particlePos.x, particlePos.y, particlePos.z,
                        rc, (float)(0.8F + Math.random() * 0.2F), (float)(0.4F + Math.random() * 0.6F), 0.8F, 2);

                if (this.level.random.nextInt(steps) <= 1) {
                    BotaniaAPI.instance().sparkleFX(this.level,
                            particlePos.x + (Math.random() - 0.5F) * 0.4,
                            particlePos.y + (Math.random() - 0.5F) * 0.4,
                            particlePos.z + (Math.random() - 0.5F) * 0.4,
                            rc, (float)(0.8F + Math.random() * 0.2F), (float)(0.4F + Math.random() * 0.6F), 0.8F, 2);
                }
                particlePos.add(step);
            }

            LivingEntity target = this.getTargetEntity();
            if (target != null) {
                if (this.lockY == -1.0) {
                    this.lockX = target.getX();
                    this.lockY = target.getY();
                    this.lockZ = target.getZ();
                }

                Vector3 targetVec = Vector3.fromEntityCenter(target);
                Vector3 diffVec = targetVec.copy().sub(thisVec);
                Vector3 motionVec = diffVec.copy().normalize().multiply(0.5);
                this.setDeltaMovement(motionVec.x, motionVec.y, motionVec.z);

                if (this.time < 30) {
                    this.setDeltaMovement(this.getDeltaMovement().x, Math.abs(this.getDeltaMovement().y), this.getDeltaMovement().z);
                }

                if (this.level.isClientSide) {
                    List<LivingEntity> targetList = this.level.getEntitiesOfClass(LivingEntity.class,
                            new AABB(this.getX() - 0.5, this.getY() - 0.5, this.getZ() - 0.5,
                                    this.getX() + 0.5, this.getY() + 0.5, this.getZ() + 0.5));

                    if (!targetList.isEmpty()) {
                        for (int i = 0; i < 12; i++) {
                            float r = 0.0F;
                            float g = 0.8F + (float)Math.random() * 0.2F;
                            float b = 0.4F + (float)Math.random() * 0.6F;
                            float s = 0.2F + (float)Math.random() * 0.1F;
                            float m = 0.15F;
                            float xm = ((float)Math.random() - 0.5F) * m;
                            float ym = ((float)Math.random() - 0.5F) * m;
                            float zm = ((float)Math.random() - 0.5F) * m;
                            BotaniaAPI.instance().sparkleFX(this.level,
                                    this.getX() + this.getBbWidth() / 2.0F,
                                    this.getY() + this.getBbHeight() / 2.0F,
                                    this.getZ() + this.getBbWidth() / 2.0F,
                                    r, g, b, s, 2);
                        }
                    }
                }

                if (evil && diffVec.mag() < 1.0) {
                    this.discard();
                }
            } else {
                Vector3 targetVec = new Vector3(
                        this.getX() + (Math.random() - 0.5) * 16.0,
                        this.getY() + (Math.random() - 0.5) * 16.0,
                        this.getZ() + (Math.random() - 0.5) * 16.0);
                Vector3 diffVec = targetVec.copy().sub(thisVec);
                Vector3 motionVec = diffVec.copy().normalize().multiply(0.5);
                this.setDeltaMovement(motionVec.x, motionVec.y, motionVec.z);
            }

            this.time++;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(TAG_TIME, this.time);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.time = tag.getInt(TAG_TIME);
    }

    public boolean getTarget() {
        LivingEntity target = this.getTargetEntity();
        if (target != null && target.getHealth() > 0.0F && !target.isRemoved() &&
                this.level.getEntity(target.getId()) != null) {
            return true;
        } else {
            if (target != null) {
                this.setTarget(null);
            }

            double range = 32.0;
            List<Entity> entities = this.level.getEntities(this.getTrueThrower(),
                    new AABB(this.getX() - range, this.getY() - range, this.getZ() - range,
                            this.getX() + range, this.getY() + range, this.getZ() + range));

            List<Entity> validTargets = new LinkedList<>(entities);
            while (!validTargets.isEmpty()) {
                Entity e = validTargets.get(this.level.random.nextInt(validTargets.size()));
                if (e instanceof LivingEntity && !e.isRemoved()) {
                    target = (LivingEntity) e;
                    this.setTarget(target);
                    break;
                }
                validTargets.remove(e);
            }

            return target != null;
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityResult = (EntityHitResult) result;
            if (entityResult.getEntity() != null && this.getTargetEntity() == entityResult.getEntity()) {
                Player thrower = this.getTrueThrower();
                if (thrower != null) {
                    float damage = DAMAGE_MIN + (float)(Math.random() * (DAMAGE_MAX - DAMAGE_MIN));
                    entityResult.getEntity().hurt(new ModDamageSources.DamageSourceMagic(thrower), damage);
                }

                this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 2.0F,
                        0.8F + (float)(Math.random() * 0.2F));
                this.discard();
            }
        }
    }
}