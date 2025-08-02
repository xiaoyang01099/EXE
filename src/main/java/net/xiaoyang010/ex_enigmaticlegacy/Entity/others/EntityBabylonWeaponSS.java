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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

import java.util.List;

public class EntityBabylonWeaponSS extends ThrowableProjectile implements IEntityAdditionalSpawnData {
    public static final float DAMAGE_DIRECT = 100.0F;
    public static final float DAMAGE_IMPACT = 75.0F;

    private static final EntityDataAccessor<Byte> CHARGING =
            SynchedEntityData.defineId(EntityBabylonWeaponSS.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> VARIETY =
            SynchedEntityData.defineId(EntityBabylonWeaponSS.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHARGE_TICKS =
            SynchedEntityData.defineId(EntityBabylonWeaponSS.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIVE_TICKS =
            SynchedEntityData.defineId(EntityBabylonWeaponSS.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DELAY =
            SynchedEntityData.defineId(EntityBabylonWeaponSS.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ROTATION =
            SynchedEntityData.defineId(EntityBabylonWeaponSS.class, EntityDataSerializers.FLOAT);

    public EntityBabylonWeaponSS(EntityType<? extends EntityBabylonWeaponSS> entityType, Level level) {
        super(entityType, level);
        this.setSize(0.0F, 0.0F);
    }

    public EntityBabylonWeaponSS(EntityType<? extends EntityBabylonWeaponSS> entityType, Level level, LivingEntity thrower) {
        super(entityType, thrower, level);
        this.setSize(0.0F, 0.0F);
    }

    private void setSize(float width, float height) {
        this.dimensions = this.dimensions.scale(width, height);
        this.refreshDimensions();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(CHARGING, (byte) 0);
        this.entityData.define(VARIETY, 0);
        this.entityData.define(CHARGE_TICKS, 0);
        this.entityData.define(LIVE_TICKS, 0);
        this.entityData.define(DELAY, 0);
        this.entityData.define(ROTATION, 0.0F);
    }

    @Override
    public void tick() {
        LivingEntity thrower = (LivingEntity) this.getOwner();

        if (this.level.isClientSide || (thrower != null && thrower instanceof Player && !thrower.isDeadOrDying())) {
            Player player = (Player) thrower;
            double x = this.getDeltaMovement().x;
            double y = this.getDeltaMovement().y;
            double z = this.getDeltaMovement().z;

            int liveTime = this.getLiveTicks();
            int delay = this.getDelay();

            if (this.tickCount <= 15) {
                this.setDeltaMovement(0, 0, 0);
                int chargeTime = this.getChargeTicks();
                this.setChargeTicks(chargeTime + 1);

                if (this.level.random.nextInt(20) == 0) {
                    this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 0.1F,
                            1.0F + this.level.random.nextFloat() * 3.0F);
                }
            } else {
                if (liveTime < delay) {
                    this.setDeltaMovement(0, 0, 0);
                } else if (liveTime == delay && player != null) {
                    Vector3 playerLook = null;
                    BlockHitResult lookAt = ToolCommons.raytraceFromEntity(player, 64.0, true);

                    if (lookAt == null || lookAt.getType() != HitResult.Type.BLOCK) {
                        Vec3 lookVec = player.getViewVector(1.0F);
                        playerLook = new Vector3(lookVec.x, lookVec.y, lookVec.z)
                                .multiply(64.0)
                                .add(Vector3.fromEntity(player));
                    } else {
                        BlockPos hitPos = lookAt.getBlockPos();
                        playerLook = new Vector3(hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5);
                    }

                    Vector3 thisVec = Vector3.fromEntityCenter(this);
                    Vector3 motionVec = playerLook.sub(thisVec).normalize().multiply(3.0);
                    x = motionVec.x;
                    y = motionVec.y;
                    z = motionVec.z;

                    this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 2.0F,
                            0.1F + this.level.random.nextFloat() * 3.0F);
                }

                this.setLiveTicks(liveTime + 1);

                if (!this.level.isClientSide) {
                    AABB collisionBox = new AABB(
                            this.getX(), this.getY(), this.getZ(),
                            this.xOld, this.yOld, this.zOld
                    ).inflate(2.0, 2.0, 2.0);

                    List<LivingEntity> livingEntities = this.level.getEntitiesOfClass(
                            LivingEntity.class, collisionBox);

                    for (LivingEntity living : livingEntities) {
                        if (living != thrower && !living.isDeadOrDying()) {
                            if (player != null) {
                                living.hurt(new ModDamageSources.DamageSourceMagic(player), DAMAGE_DIRECT);
                            }

                            Vector3 targetPos = Vector3.fromEntityCenter(living);
                            Vector3 thisPos = Vector3.fromEntityCenter(this);
                            Vector3 diff = targetPos.copy().sub(thisPos);
                            diff.normalize();
                            diff.multiply(1.0F / living.distanceTo(this));

                            if (diff.mag() > 1.0F) {
                                diff.normalize();
                            }

                            if (living instanceof EnderDragon || living instanceof WitherBoss) {
                                diff.multiply(0.5F);
                            }

                            living.setDeltaMovement(
                                    living.getDeltaMovement().x + diff.x,
                                    living.getDeltaMovement().y + diff.y,
                                    living.getDeltaMovement().z + diff.z
                            );

                            this.onHit(new EntityHitResult(living));
                            return;
                        }
                    }
                }
            }

            super.tick();
            this.setDeltaMovement(x, y, z);

            if (liveTime > delay) {
                BotaniaAPI.instance().sparkleFX(this.level,
                        this.getX(), this.getY(), this.getZ(),
                        1.0F, 1.0F, 0.0F, 0.3F, 1);
            }

            if (liveTime > 200 + delay) {
                this.discard();
            }
        } else {
            this.discard();
        }
    }

    public void invokeDamageEffects() {
        List<LivingEntity> targets = this.level.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(3.0, 3.0, 3.0)
        );

        if (targets.contains(this.getOwner())) {
            targets.remove(this.getOwner());
        }

        for (LivingEntity target : targets) {
            if (!target.isDeadOrDying() && !this.level.isClientSide) {
                target.hurt(new ModDamageSources.DamageSourceMagic(this.getOwner()), DAMAGE_IMPACT);

                Vector3 targetPos = Vector3.fromEntityCenter(target);
                Vector3 thisPos = Vector3.fromEntityCenter(this);
                Vector3 diff = targetPos.copy().sub(thisPos);
                diff.normalize();
                diff.multiply(1.0F / target.distanceTo(this));

                if (diff.mag() > 1.0F) {
                    diff.normalize();
                }

                if (target instanceof EnderDragon || target instanceof WitherBoss) {
                    diff.multiply(0.5F);
                }

                target.setDeltaMovement(
                        target.getDeltaMovement().x + diff.x,
                        target.getDeltaMovement().y + diff.y,
                        target.getDeltaMovement().z + diff.z
                );
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        LivingEntity thrower = (LivingEntity) this.getOwner();

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = this.level.getBlockState(pos);
            Block block = state.getBlock();

            if (block instanceof BushBlock || block instanceof LeavesBlock ||
                    block instanceof LiquidBlock || state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
                return;
            }
        }

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hitResult;
            if (entityHit.getEntity() == thrower) {
                return;
            }
        }

        if (!this.level.isClientSide && thrower != null) {
            RelicsEventHandler.imposeBurst(this.level, this.getX(), this.getY(), this.getZ(), 1.5F);
            this.invokeDamageEffects();

            this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 8.0F,
                    (float)(0.8F + Math.random() * 0.2));
        }

        if (!this.level.isClientSide) {
            NetworkHandler.sendApotheosisParticle(this.level,
                    this.getX(), this.getY(), this.getZ(), 40, 128.0);
        }

        this.discard();
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("charging", this.isCharging());
        compound.putInt("variety", this.getVariety());
        compound.putInt("chargeTicks", this.getChargeTicks());
        compound.putInt("liveTicks", this.getLiveTicks());
        compound.putInt("delay", this.getDelay());
        compound.putFloat("rotation", this.getRotation());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setCharging(compound.getBoolean("charging"));
        this.setVariety(compound.getInt("variety"));
        this.setChargeTicks(compound.getInt("chargeTicks"));
        this.setLiveTicks(compound.getInt("liveTicks"));
        this.setDelay(compound.getInt("delay"));
        this.setRotation(compound.getFloat("rotation"));
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    public boolean isCharging() {
        return this.entityData.get(CHARGING) == 1;
    }

    public void setCharging(boolean charging) {
        this.entityData.set(CHARGING, (byte) (charging ? 1 : 0));
    }

    public int getVariety() {
        return this.entityData.get(VARIETY);
    }

    public void setVariety(int variety) {
        this.entityData.set(VARIETY, variety);
    }

    public int getChargeTicks() {
        return this.entityData.get(CHARGE_TICKS);
    }

    public void setChargeTicks(int ticks) {
        this.entityData.set(CHARGE_TICKS, ticks);
    }

    public int getLiveTicks() {
        return this.entityData.get(LIVE_TICKS);
    }

    public void setLiveTicks(int ticks) {
        this.entityData.set(LIVE_TICKS, ticks);
    }

    public int getDelay() {
        return this.entityData.get(DELAY);
    }

    public void setDelay(int delay) {
        this.entityData.set(DELAY, delay);
    }

    public float getRotation() {
        return this.entityData.get(ROTATION);
    }

    public void setRotation(float rotation) {
        this.entityData.set(ROTATION, rotation);
    }
}