package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModWeapons;
import vazkii.botania.api.BotaniaAPI;

import java.util.List;

public class EntitySword extends ThrowableProjectile implements ItemSupplier {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(EntitySword.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> ATTACKER = SynchedEntityData.defineId(EntitySword.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> PIERCE_COUNT = SynchedEntityData.defineId(EntitySword.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_PIERCE = SynchedEntityData.defineId(EntitySword.class, EntityDataSerializers.INT);

    public EntitySword(EntityType<? extends EntitySword> entityType, Level level) {
        super(entityType, level);
    }

    public EntitySword(EntityType<? extends EntitySword> entityType, Level level, LivingEntity owner) {
        super(entityType, owner, level);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModWeapons.CLAYMORE.get());
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DAMAGE, 0.0F);
        this.getEntityData().define(ATTACKER, "");
        this.getEntityData().define(PIERCE_COUNT, 0);
        this.getEntityData().define(MAX_PIERCE, 10);
    }

    @Override
    public void tick() {
        super.tick();
        this.update();

        if (this.tickCount < 20) {
            this.setDeltaMovement(this.getDeltaMovement().scale(1.115D));
        } else if (this.tickCount > 160) {
            this.discard();
        }

        if (this.level.isClientSide) {
            for(int i = 0; i < 12; ++i) {
                float r = this.level.random.nextBoolean() ? 0.88235295F : 0.39607844F;
                float g = this.level.random.nextBoolean() ? 0.2627451F : 0.81960785F;
                float b = this.level.random.nextBoolean() ? 0.9411765F : 0.88235295F;

                BotaniaAPI.instance().sparkleFX(
                        this.level,
                        this.getX() + (Math.random() - 0.5D) * 0.25D,
                        this.getY() + (Math.random() - 0.5D) * 0.25D,
                        this.getZ() + (Math.random() - 0.5D) * 0.25D,
                        r + (float)(Math.random() / 4.0D - 0.125D),
                        g + (float)(Math.random() / 4.0D - 0.125D),
                        b + (float)(Math.random() / 4.0D - 0.125D),
                        1.6F,
                        2
                );
            }
        }
    }

    public void update() {
        String attacker = this.getAttacker();
        AABB boundingBox = this.getBoundingBox().inflate(1.0D, 1.0D, 1.0D);

        List<LivingEntity> entities = this.level.getEntitiesOfClass(LivingEntity.class, boundingBox);

        for(LivingEntity living : entities) {
            if ((!(living instanceof Player) ||
                    !((Player)living).getName().getString().equals(attacker)) &&
                    !this.level.isClientSide &&
                    living.invulnerableTime == 0) {

                float damage = this.getDamage();
                Player player = null;
                if (this.level.getServer() != null) {
                    player = this.level.getServer().getPlayerList().getPlayerByName(attacker);
                }

                DamageSource damageSource;
                if (player != null) {
                    damageSource = DamageSource.playerAttack(player);
                } else {
                    damageSource = DamageSource.MAGIC;
                }

                living.hurt(damageSource, damage);

                int currentPierce = this.getPierceCount();
                this.setPierceCount(currentPierce + 1);

                if (currentPierce >= this.getMaxPierce()) {
                    this.discard();
                    return;
                }
            }
        }
    }

    public int getPierceCount() {
        return this.getEntityData().get(PIERCE_COUNT);
    }

    public void setPierceCount(int count) {
        this.getEntityData().set(PIERCE_COUNT, count);
    }

    public int getMaxPierce() {
        return this.getEntityData().get(MAX_PIERCE);
    }

    public void setMaxPierce(int maxPierce) {
        this.getEntityData().set(MAX_PIERCE, maxPierce);
    }

    public String getAttacker() {
        return this.getEntityData().get(ATTACKER);
    }

    public void setAttacker(String attacker) {
        this.getEntityData().set(ATTACKER, attacker);
    }

    public float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    public void setDamage(float damage) {
        this.getEntityData().set(DAMAGE, damage);
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        BlockState blockState = this.level.getBlockState(blockHitResult.getBlockPos());
        Block block = blockState.getBlock();

        if (block instanceof BushBlock || block instanceof LeavesBlock) {
            return;
        }

        this.discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("ticks", this.tickCount);
        compound.putFloat("disDamage", this.getDamage());
        compound.putString("attacker", this.getAttacker());
        compound.putInt("pierceCount", this.getPierceCount());
        compound.putInt("maxPierce", this.getMaxPierce());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.tickCount = compound.getInt("ticks");
        this.setDamage(compound.getFloat("disDamage"));
        this.setAttacker(compound.getString("attacker"));
        this.setPierceCount(compound.getInt("pierceCount"));
        this.setMaxPierce(compound.getInt("maxPierce"));
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            onHitBlock((BlockHitResult) hitResult);
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}