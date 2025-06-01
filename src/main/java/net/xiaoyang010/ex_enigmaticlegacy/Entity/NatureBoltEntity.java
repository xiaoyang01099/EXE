package net.xiaoyang010.ex_enigmaticlegacy.Entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModParticleTypes;

public class NatureBoltEntity extends ThrowableProjectile implements ItemSupplier {

    private float damage = 90.0F; // 基础伤害值
    private int lifetime = 60; // 最长存活时间（ticks）

    public NatureBoltEntity(EntityType<? extends NatureBoltEntity> type, Level level) {
        super(type, level);
    }

    public NatureBoltEntity(EntityType<? extends NatureBoltEntity> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.LILY_OF_THE_VALLEY);
    }

    @Override
    protected void defineSynchedData() {
        // 定义需要同步的数据
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public void tick() {
        super.tick();

        // 粒子效果
        if (level.isClientSide) {
            for (int i = 0; i < 3; i++) {
                level.addParticle(
                        ParticleTypes.COMPOSTER,
                        getX() + (random.nextDouble() - 0.5) * 0.5,
                        getY() + (random.nextDouble() - 0.5) * 0.5,
                        getZ() + (random.nextDouble() - 0.5) * 0.5,
                        0, 0, 0
                );

                // 添加一些叶子粒子效果
                if (random.nextInt(3) == 0) {
                    level.addParticle(
                            ParticleTypes.FALLING_SPORE_BLOSSOM,
                            getX() + (random.nextDouble() - 0.5) * 0.5,
                            getY() + (random.nextDouble() - 0.5) * 0.5,
                            getZ() + (random.nextDouble() - 0.5) * 0.5,
                            (random.nextDouble() - 0.5) * 0.2,
                            random.nextDouble() * 0.1,
                            (random.nextDouble() - 0.5) * 0.2
                    );
                }
            }
        }

        // 光效
        if (!level.isClientSide && tickCount % 4 == 0) {
            ((ServerLevel) level).sendParticles(
                    ParticleTypes.END_ROD,
                    getX(), getY(), getZ(),
                    1, 0, 0, 0, 0.02
            );
        }

        // 存活时间限制
        if (tickCount > lifetime) {
            if (!level.isClientSide) {
                // 自然消散时的粒子效果
                ((ServerLevel) level).sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        getX(), getY(), getZ(),
                        10, 0.5, 0.5, 0.5, 0.1
                );

                // 播放消散音效
                level.playSound(null, getX(), getY(), getZ(),
                        SoundEvents.FLOWERING_AZALEA_BREAK, SoundSource.NEUTRAL,
                        1.0F, 1.0F + (random.nextFloat() - 0.5F) * 0.2F);
            }
            discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!level.isClientSide) {
            // 命中效果的粒子
            ((ServerLevel) level).sendParticles(
                    ModParticleTypes.ASGARDANDELION.get(),
                    getX(), getY(), getZ(),
                    14, 0.4, 0.4, 0.4, 0.1
            );

            ((ServerLevel) level).sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    getX(), getY(), getZ(),
                    15, 0.5, 0.5, 0.5, 0.1
            );

            // 播放命中音效
            level.playSound(null, getX(), getY(), getZ(),
                    SoundEvents.THORNS_HIT, SoundSource.NEUTRAL,
                    1.0F, 1.0F + (random.nextFloat() - 0.5F) * 0.2F);

            if (!(result instanceof EntityHitResult)) {
                // 如果击中方块，在地面产生小范围的生长效果
                growNearbyPlants();
            }

            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level.isClientSide) {
            Entity target = result.getEntity();
            Entity owner = getOwner();

            // 确保不会伤害发射者
            if (target != owner && target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;

                for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5d))) {
                    if (living == owner) continue; //不攻击自己
                    // 造成伤害
                    living.hurt(DamageSource.MAGIC, damage);
                    applyEffectsToTarget(living);
                }
            }
        }
    }

    // 对目标实体应用效果
    private void applyEffectsToTarget(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 1200, 5));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1200, 5));

        target.addEffect(new MobEffectInstance(ModEffects.DROWNING.get(), 1200, 5));

        // 额外粒子效果
        if (level instanceof ServerLevel) {
            ((ServerLevel) level).sendParticles(
                    ParticleTypes.CRIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    15, 0.5, 0.5, 0.5, 0.1
            );
        }
    }

    // 使附近植物生长的效果
    private void growNearbyPlants() {
        if (level instanceof ServerLevel) {
            ((ServerLevel) level).sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    getX(), getY(), getZ(),
                    20, 1.5, 0.5, 1.5, 0.1
            );
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}