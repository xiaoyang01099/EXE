package org.xiaoyang.ex_enigmaticlegacy.Entity.others;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModItems;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.ContinuumItem;
import org.xiaoyang.ex_enigmaticlegacy.api.exboapi.RewardType;

public class EntityContinuumBomb extends ThrowableProjectile implements ItemSupplier {
    private boolean hasHit;
    private double hitX;
    private double hitY;
    private double hitZ;
    private int partyTimeNeeded = 100;
    private int partyTime;
    private int presentCount = 9;

    public EntityContinuumBomb(EntityType<? extends EntityContinuumBomb> entityType, Level level) {
        super(entityType, level);
    }

    public EntityContinuumBomb(Level level, LivingEntity shooter) {
        super(ModEntities.CONTINUUM_BOMB.get(), shooter, level);
    }

    public EntityContinuumBomb(Level level, double x, double y, double z) {
        super(ModEntities.CONTINUUM_BOMB.get(), x, y, z, level);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.CONTINUUM_BOMB.get());
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        if (this.hasHit) {
            this.setPos(this.hitX, this.hitY, this.hitZ);
            if (this.partyTime < this.partyTimeNeeded) {
                if (this.partyTime % 10 == 0) {
                    double motX = (random.nextBoolean() ? -1 : 1) * random.nextDouble();
                    double motY = 1.0F;
                    double motZ = (random.nextBoolean() ? -1 : 1) * random.nextDouble();

                    if (!level.isClientSide) {
                        ItemStack reward = ContinuumItem.getRandomStack(random);
                        EntityRewardItemStack entity = new EntityRewardItemStack(
                                level, getX(), getY(), getZ(), reward, RewardType.STANDARD
                        );

                        if (getOwner() != null) {
                            entity.setPlayerName(getOwner().getName().getString());
                        }

                        entity.setDeltaMovement(motX, motY, motZ);
                        level.addFreshEntity(entity);
                    }

                    for (int sparkCount = 1; sparkCount <= 10; ++sparkCount) {
                        motX = (random.nextBoolean() ? -1 : 1) * random.nextDouble();
                        motY = random.nextDouble();
                        motZ = (random.nextBoolean() ? -1 : 1) * random.nextDouble();

                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(
                                    ParticleTypes.AMBIENT_ENTITY_EFFECT,
                                    getX(), getY(), getZ(),
                                    1, motX, motY, motZ,
                                    0.0D
                            );
                            serverLevel.sendParticles(
                                    ParticleTypes.CLOUD,
                                    getX(), getY(), getZ(),
                                    1, motX / 6.0F, motY / 6.0F, motZ / 6.0F,
                                    0.0D
                            );
                        }
                    }
                }
                ++this.partyTime;
            } else {
                for (int sparkCount = 1; sparkCount <= 10; ++sparkCount) {
                    double motX = (random.nextBoolean() ? -1 : 1) * random.nextDouble();
                    double motY = random.nextDouble();
                    double motZ = (random.nextBoolean() ? -1 : 1) * random.nextDouble();

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.AMBIENT_ENTITY_EFFECT,
                                getX(), getY(), getZ(),
                                1, motX, motY, motZ,
                                0.0D
                        );
                        serverLevel.sendParticles(
                                ParticleTypes.CLOUD,
                                getX(), getY(), getZ(),
                                1, motX / 6.0F, motY / 6.0F, motZ / 6.0F,
                                0.0D
                        );
                    }
                }
                discard();
            }
        } else {
            super.tick();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.hasHit) {
            this.hasHit = true;
            this.hitX = this.getX();
            this.hitY = this.getY();
            this.hitZ = this.getZ();

            if (result instanceof EntityHitResult entityHit) {
                byte damage = 0;
                if (entityHit.getEntity() instanceof Blaze) {
                    damage = 3;
                }

                entityHit.getEntity().hurt(
                        this.damageSources().thrown(this, this.getOwner()),
                        damage
                );
            }
        }
    }
}