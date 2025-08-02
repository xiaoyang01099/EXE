package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Network.ClientProxy;

import java.util.List;

public class EntityDarkMatterOrb extends ThrowableProjectile {
    private static final float NORMAL_DAMAGE = 32.5f;
    private static final float OUTER_LANDS_DAMAGE = 100.0f;

    public EntityDarkMatterOrb(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public EntityDarkMatterOrb(Level level, LivingEntity thrower) {
        super(ModEntities.DARK_MATTER_ORB.get(), thrower, level);
    }

    @Override
    protected float getGravity() {
        return 0.0f;
    }

    @Override
    public void tick() {
        super.tick();

        double absMotionX = Math.abs(this.getDeltaMovement().x);
        double absMotionY = Math.abs(this.getDeltaMovement().y);
        double absMotionZ = Math.abs(this.getDeltaMovement().z);

        if (this.tickCount >= 200) {
            this.discard();
        } else if (this.tickCount >= 100 && absMotionX < 0.01D && absMotionY < 0.01D && absMotionZ < 0.01D) {
            HitResult missResult = new HitResult(this.position()) {
                @Override
                public Type getType() {
                    return Type.MISS;
                }
            };
            this.onHit(missResult);
        }

        if (this.level.isClientSide) {
            ClientProxy.wispFX4((ClientLevel) this.level, this.getX(), this.getY() + 0.22 * this.getBbHeight(), this.getZ(),
                    this, 5, false, 0.02f);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            if (this.level.isClientSide) {
                for (int a = 0; a < 30; ++a) {
                    final float fx = (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.3f;
                    final float fy = (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.3f;
                    final float fz = (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.3f;

                    ClientProxy.wispFX3((ClientLevel) this.level,
                            this.getX() + fx, this.getY() + fy, this.getZ() + fz,
                            this.getX() + fx * 8.0f, this.getY() + fy * 8.0f, this.getZ() + fz * 8.0f,
                            0.3f, 5, true, 0.02f);
                }
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = new BlockPos(hitResult.getLocation());
            BlockState blockState = this.level.getBlockState(blockPos);
            
            if (blockState.getBlock() instanceof BushBlock ||
                    blockState.getBlock() instanceof LeavesBlock ||
                    blockState.getBlock() instanceof LiquidBlock) {
                return;
            } else {
                this.playSound(SoundEvents.LAVA_EXTINGUISH, 0.5f,
                        2.6f + (this.random.nextFloat() - this.random.nextFloat()) * 0.8f);
                this.tickCount = 200;
                this.level.broadcastEntityEvent(this, (byte)16);
                return;
            }
        }

        if (!this.level.isClientSide && this.getOwner() != null) {
            AABB aabb = this.getBoundingBox().inflate(1.0, 1.0, 1.0);
            List<Entity> entities = this.level.getEntities(this, aabb);

            for (Entity entity : entities) {
                if (entity instanceof LivingEntity && entity != this.getOwner()) {

                    LivingEntity livingEntity = (LivingEntity) entity;
                    boolean isInOuterLands = isInOuterLands(this.level);
                    float damage = isInOuterLands ? OUTER_LANDS_DAMAGE : NORMAL_DAMAGE;
                    livingEntity.hurt(new ModDamageSources.DamageSourceDarkMatter(this.getOwner()), damage);
                    
                    try {
                        if (isInOuterLands) {
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 320, 2));
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 2));
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 250, 3));
                        } else {
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
                        }
                    } catch (Exception ex) {
                    }
                }
            }
            
            this.playSound(SoundEvents.LAVA_EXTINGUISH, 0.5f,
                    2.6f + (this.random.nextFloat() - this.random.nextFloat()) * 0.8f);
            this.tickCount = 199;
            this.level.broadcastEntityEvent(this, (byte)16);
        }

        this.discard();
    }
    
    private boolean isInOuterLands(Level level) {
        String dimensionLocation = level.dimension().location().toString();
        return dimensionLocation.contains("ender") || dimensionLocation.contains("nether");
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {
    }
    
    public float getShadowRadius() {
        return 0.1f;
    }
}