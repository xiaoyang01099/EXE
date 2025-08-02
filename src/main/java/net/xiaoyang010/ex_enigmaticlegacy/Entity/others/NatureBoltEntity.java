package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import net.xiaoyang010.ex_enigmaticlegacy.Client.ModParticleTypes;

import java.util.List;

public class NatureBoltEntity extends ThrowableProjectile implements ItemSupplier {

    private float damage = 90.0F;
    private int lifetime = 60;
    private float damageRadius = 10F;

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
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level.isClientSide) {
            checkAndDamageNearbyEntities();
        }

        if (level.isClientSide) {
            spawnClientParticles();
        }

        if (!level.isClientSide) {
            spawnServerParticlesWithDamage();
        }

        if (tickCount > lifetime) {
            if (!level.isClientSide) {
                spawnDeathParticlesWithDamage();
                level.playSound(null, getX(), getY(), getZ(),
                        SoundEvents.FLOWERING_AZALEA_BREAK, SoundSource.NEUTRAL,
                        1.0F, 1.0F + (random.nextFloat() - 0.5F) * 0.2F);
            }
            discard();
        }
    }

    private void spawnClientParticles() {
        for (int i = 0; i < 3; i++) {
            level.addParticle(
                    ParticleTypes.COMPOSTER,
                    getX() + (random.nextDouble() - 0.5) * 0.5,
                    getY() + (random.nextDouble() - 0.5) * 0.5,
                    getZ() + (random.nextDouble() - 0.5) * 0.5,
                    0, 0, 0
            );

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

    private void spawnServerParticlesWithDamage() {
        for (int i = 0; i < 5; i++) {
            double particleX = getX() + (random.nextDouble() - 0.5) * 1.0;
            double particleY = getY() + (random.nextDouble() - 0.5) * 1.0;
            double particleZ = getZ() + (random.nextDouble() - 0.5) * 1.0;

            damageAtPosition(particleX, particleY, particleZ, 0.8F, damage * 0.3F);

            ((ServerLevel) level).sendParticles(
                    ParticleTypes.COMPOSTER,
                    particleX, particleY, particleZ,
                    1, 0.1, 0.1, 0.1, 0.0
            );
        }

        if (tickCount % 2 == 0) {
            for (int i = 0; i < 3; i++) {
                double trailX = getX() + (random.nextDouble() - 0.5) * 0.6;
                double trailY = getY() + (random.nextDouble() - 0.5) * 0.6;
                double trailZ = getZ() + (random.nextDouble() - 0.5) * 0.6;

                damageAtPosition(trailX, trailY, trailZ, 0.6F, damage * 0.2F);

                ((ServerLevel) level).sendParticles(
                        ParticleTypes.FALLING_SPORE_BLOSSOM,
                        trailX, trailY, trailZ,
                        1, 0.0, 0.0, 0.0, 0.0
                );
            }
        }

        if (tickCount % 4 == 0) {
            for (int i = 0; i < 2; i++) {
                double endRodX = getX() + (random.nextDouble() - 0.5) * 0.4;
                double endRodY = getY() + (random.nextDouble() - 0.5) * 0.4;
                double endRodZ = getZ() + (random.nextDouble() - 0.5) * 0.4;

                damageAtPosition(endRodX, endRodY, endRodZ, 0.5F, damage * 0.5F);

                ((ServerLevel) level).sendParticles(
                        ParticleTypes.END_ROD,
                        endRodX, endRodY, endRodZ,
                        1, 0, 0, 0, 0.02
                );
            }
        }
    }

    private void spawnDeathParticlesWithDamage() {
        for (int i = 0; i < 15; i++) {
            double spreadX = getX() + (random.nextDouble() - 0.5) * 3.0;
            double spreadY = getY() + (random.nextDouble() - 0.5) * 2.0;
            double spreadZ = getZ() + (random.nextDouble() - 0.5) * 3.0;

            damageAtPosition(spreadX, spreadY, spreadZ, 1.0F, damage * 0.4F);

            ((ServerLevel) level).sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    spreadX, spreadY, spreadZ,
                    1, 0.1, 0.1, 0.1, 0.1
            );
        }

        for (int i = 0; i < 8; i++) {
            double poisonX = getX() + (random.nextDouble() - 0.5) * 4.0;
            double poisonY = getY() + (random.nextDouble() - 0.5) * 1.5;
            double poisonZ = getZ() + (random.nextDouble() - 0.5) * 4.0;

            damageAtPosition(poisonX, poisonY, poisonZ, 1.2F, damage * 0.6F);

            ((ServerLevel) level).sendParticles(
                    ParticleTypes.WITCH,
                    poisonX, poisonY, poisonZ,
                    2, 0.2, 0.2, 0.2, 0.05
            );
        }
    }

    private void damageAtPosition(double x, double y, double z, float radius, float particleDamage) {
        AABB damageBox = new AABB(
                x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius
        );

        List<LivingEntity> entities = this.level.getEntitiesOfClass(LivingEntity.class, damageBox);
        Entity owner = getOwner();

        for (LivingEntity living : entities) {
            if (living == owner) continue;

            double distance = Math.sqrt(
                    Math.pow(living.getX() - x, 2) +
                            Math.pow(living.getY() - y, 2) +
                            Math.pow(living.getZ() - z, 2)
            );

            if (distance <= radius && living.invulnerableTime <= 8) {
                living.hurt(DamageSource.MAGIC, particleDamage);

                applyParticleEffectsToTarget(living);

                if (level instanceof ServerLevel) {
                    ((ServerLevel) level).sendParticles(
                            ParticleTypes.DAMAGE_INDICATOR,
                            living.getX(), living.getY() + living.getBbHeight() * 0.7, living.getZ(),
                            2, 0.2, 0.2, 0.2, 0.0
                    );

                    if (random.nextInt(5) == 0) {
                        level.playSound(null, living.getX(), living.getY(), living.getZ(),
                                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.NEUTRAL,
                                0.3F, 1.5F + random.nextFloat() * 0.5F);
                    }
                }
            }
        }
    }

    private void applyParticleEffectsToTarget(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 10));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 10));
        target.addEffect(new MobEffectInstance(ModEffects.DROWNING.get(), 200, 10));

        if (level instanceof ServerLevel && random.nextInt(3) == 0) {
            ((ServerLevel) level).sendParticles(
                    ParticleTypes.SOUL,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    3, 0.3, 0.3, 0.3, 0.02
            );
        }
    }

    private void checkAndDamageNearbyEntities() {
        AABB boundingBox = this.getBoundingBox().inflate(damageRadius);
        List<LivingEntity> entities = this.level.getEntitiesOfClass(LivingEntity.class, boundingBox);

        Entity owner = getOwner();

        for (LivingEntity living : entities) {
            if (living == owner) {
                continue;
            }

            if (living.invulnerableTime <= 15) {
                living.hurt(DamageSource.MAGIC, damage);

                applyMainEffectsToTarget(living);

                if (level instanceof ServerLevel) {
                    ((ServerLevel) level).sendParticles(
                            ParticleTypes.CRIT,
                            living.getX(), living.getY() + living.getBbHeight() * 0.5, living.getZ(),
                            8, 0.3, 0.3, 0.3, 0.1
                    );

                    level.playSound(null, living.getX(), living.getY(), living.getZ(),
                            SoundEvents.THORNS_HIT, SoundSource.NEUTRAL,
                            0.8F, 1.0F + (random.nextFloat() - 0.5F) * 0.2F);
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!level.isClientSide) {
            ((ServerLevel) level).sendParticles(
                    ModParticleTypes.ASGARDANDELION.get(),
                    getX(), getY(), getZ(),
                    14, 0.4, 0.4, 0.4, 0.1
            );

            level.playSound(null, getX(), getY(), getZ(),
                    SoundEvents.THORNS_HIT, SoundSource.NEUTRAL,
                    1.0F, 1.0F + (random.nextFloat() - 0.5F) * 0.2F);

            if (!(result instanceof EntityHitResult)) {
                growNearbyPlants();
            }

            finalExplosionDamage();
            spawnDeathParticlesWithDamage();

            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
    }

    private void finalExplosionDamage() {
        AABB explosionBox = this.getBoundingBox().inflate(3.0D);
        List<LivingEntity> entities = this.level.getEntitiesOfClass(LivingEntity.class, explosionBox);
        Entity owner = getOwner();

        for (LivingEntity living : entities) {
            if (living == owner) continue;

            living.hurt(DamageSource.MAGIC, damage * 1.5F);
            applyMainEffectsToTarget(living);

            if (level instanceof ServerLevel) {
                ((ServerLevel) level).sendParticles(
                        ParticleTypes.EXPLOSION,
                        living.getX(), living.getY() + living.getBbHeight() * 0.5, living.getZ(),
                        3, 0.2, 0.2, 0.2, 0.0
                );
            }
        }
    }

    private void applyMainEffectsToTarget(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 1200, 5));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1200, 5));
        target.addEffect(new MobEffectInstance(ModEffects.DROWNING.get(), 1200, 5));

        if (level instanceof ServerLevel) {
            ((ServerLevel) level).sendParticles(
                    ParticleTypes.WITCH,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    5, 0.3, 0.3, 0.3, 0.05
            );
        }
    }

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