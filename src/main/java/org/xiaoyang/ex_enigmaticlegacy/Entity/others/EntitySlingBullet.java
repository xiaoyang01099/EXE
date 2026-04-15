package org.xiaoyang.ex_enigmaticlegacy.Entity.others;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;

public class EntitySlingBullet extends ThrowableProjectile {
    private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(EntitySlingBullet.class, EntityDataSerializers.ITEM_STACK);
    private static final int THROWING_INTERVAL = (1 * 20);
    private static final float BLOCKHARDNESS_MIN = 0.0F;
    private static final float BLOCKHARDNESS_MAX = 10.0F;
    private static final String TAG = Exe.MODID + ".";
    private static final String TAG_ITEM = TAG + "item";
    private static final String TAG_CHARGE_AMOUNT = TAG + "charge_amount";
    private static final String TAG_KNOCKBACK_STRENGTH = TAG + "knockback_strength";
    private static final String TAG_AGE = TAG + "age";

    private int chargeAmount;
    private int knockbackStrength;
    private int age;

    public EntitySlingBullet(EntityType<? extends EntitySlingBullet> type, Level world) {
        super(type, world);
    }

    public EntitySlingBullet(Level world, LivingEntity thrower, ItemStack stack, int chargeAmount) {
        super(ModEntities.SLING_BULLET.get(), thrower, world);
        this.setEntityItem(stack.copy());
        this.setChargeAmount(chargeAmount);
        this.setKnockback(0);
        this.setAge(0);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ITEM, ItemStack.EMPTY);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (!this.getEntityItem().isEmpty()) {
            compound.put(TAG_ITEM, this.getEntityItem().save(new CompoundTag()));
        }
        compound.putByte(TAG_CHARGE_AMOUNT, (byte) this.getChargeAmount());
        compound.putByte(TAG_KNOCKBACK_STRENGTH, (byte) this.getKnockbackStrength());
        compound.putInt(TAG_AGE, this.getAge());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(TAG_ITEM)) {
            this.setEntityItem(ItemStack.of(compound.getCompound(TAG_ITEM)));
        }
        this.setChargeAmount(compound.getByte(TAG_CHARGE_AMOUNT));
        this.setKnockback(compound.getByte(TAG_KNOCKBACK_STRENGTH));
        this.setAge(compound.getInt(TAG_AGE));
    }

    @Override
    public String toString() {
        return this.getBulletBlockState().getBlock().getName().getString();
    }

    @Override
    protected float getGravity() {
        return 0.05F - ((float) this.getChargeAmount() / 200);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) return;

        Entity target = result.getEntity();
        BlockPos resultBlockPos = target.blockPosition();

        if (this.isThrowingInterval(target)) return;

        if (this.isOnFire()) {
            target.setSecondsOnFire(this.getChargeAmount() * 20);
        }

        if (this.getKnockbackStrength() > 0) {
            Vec3 motion = this.getDeltaMovement();
            double velocity = Math.sqrt(motion.x * motion.x + motion.z * motion.z);

            if (velocity > 0.0D) {
                target.push(
                        motion.x * this.getKnockbackStrength() * 0.6D / velocity,
                        0.1D,
                        motion.z * this.getKnockbackStrength() * 0.6D / velocity
                );
            }
        }

        float attackAmount = this.getAttackAmount();
        if (attackAmount > 0.0F) {
            target.hurt(
                    this.level().damageSources().thrown(this, this.getOwner()),
                    attackAmount
            );
        }

        this.level().levelEvent(2001, resultBlockPos,
                Block.getId(this.getBulletBlockState()));
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.level().isClientSide) return;

        BlockPos resultBlockPos = result.getBlockPos();
        BlockState resultBlockState = this.level().getBlockState(resultBlockPos);

        if (WitherBoss.canDestroy(resultBlockState)) {
            float resultBlockHardness = resultBlockState.getDestroySpeed(this.level(), resultBlockPos);

            if (resultBlockHardness == 0.0F) {
                this.level().destroyBlock(resultBlockPos, true);
                this.level().levelEvent(2001, resultBlockPos,
                        Block.getId(this.getBulletBlockState()));
                this.discard();
                return;
            }

            float bulletBlockHardness = this.getBulletBlockState()
                    .getDestroySpeed(this.level(), this.blockPosition());

            if (bulletBlockHardness >= 1.0F && resultBlockHardness < bulletBlockHardness) {
                if (this.level().destroyBlock(resultBlockPos, true)) {
                    int charge = this.getChargeAmount();
                    --charge;
                    if (charge > 0) {
                        this.setChargeAmount(charge);
                        return;
                    }
                }
            }
        }

        this.level().levelEvent(2001, resultBlockPos,
                Block.getId(this.getBulletBlockState()));
        this.discard();
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            int age = this.getAge();
            ++age;

            if (age > this.getMaxAge()) {
                this.discard();
                return;
            } else {
                this.setAge(age);
            }
        }

        if (this.isInWater()) {
            this.clearFire();

            if ((this.getAge() % (20 / 2) == 0) && !this.level().isClientSide) {
                int charge = this.getChargeAmount();
                --charge;
                if (charge > 0) {
                    this.setChargeAmount(charge);
                }
            }
        }

        super.tick();
    }

    public ItemStack getEntityItem() {
        return this.entityData.get(ITEM);
    }

    public void setEntityItem(ItemStack stack) {
        this.entityData.set(ITEM, stack.copy());
    }

    public int getChargeAmount() {
        return this.chargeAmount;
    }

    public void setChargeAmount(int chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public int getKnockbackStrength() {
        return this.knockbackStrength;
    }

    public void setKnockback(int knockbackStrength) {
        this.knockbackStrength = knockbackStrength;
    }

    public int getAge() {
        return this.age;
    }

    public int getMaxAge() {
        return (60 * 20);
    }

    public void setAge(int age) {
        this.age = age;
    }

    private boolean isThrowingInterval(Entity target) {
        if (this.getAge() < THROWING_INTERVAL) {
            return target.equals(this.getOwner());
        }
        return false;
    }

    public BlockState getBulletBlockState() {
        ItemStack stack = this.getEntityItem();

        if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState();
        }

        return Blocks.AIR.defaultBlockState();
    }

    private float getAttackAmount() {
        BlockState state = this.getBulletBlockState();

        if (state.is(Blocks.AIR)) return 0.0F;

        float blockHardness = state.getDestroySpeed(this.level(), this.blockPosition());

        if (blockHardness < 0) blockHardness = BLOCKHARDNESS_MAX;

        blockHardness = Math.min(blockHardness, BLOCKHARDNESS_MAX);
        blockHardness = Math.max(blockHardness, BLOCKHARDNESS_MIN);

        return (blockHardness * this.getChargeAmount());
    }
}