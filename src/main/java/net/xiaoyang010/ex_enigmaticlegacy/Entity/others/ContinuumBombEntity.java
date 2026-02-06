package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import com.mojang.math.Vector3f;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Item.ContinuumItem;
import net.xiaoyang010.ex_enigmaticlegacy.api.RewardType;

public class ContinuumBombEntity extends ThrowableProjectile implements ItemSupplier {
    private boolean hasHit;
    private double hitX;
    private double hitY;
    private double hitZ;
    private int partyTimeNeeded = 100;
    private int partyTime;
    private int presentCount = 9;

    private static final Vector3f[] FIREWORK_COLORS = {
            new Vector3f(1.0F, 0.0F, 0.0F),      // 红色
            new Vector3f(1.0F, 0.5F, 0.0F),      // 橙色
            new Vector3f(1.0F, 1.0F, 0.0F),      // 黄色
            new Vector3f(0.0F, 1.0F, 0.0F),      // 绿色
            new Vector3f(0.0F, 1.0F, 1.0F),      // 青色
            new Vector3f(0.0F, 0.5F, 1.0F),      // 蓝色
            new Vector3f(0.5F, 0.0F, 1.0F),      // 紫色
            new Vector3f(1.0F, 0.0F, 1.0F),      // 品红色
            new Vector3f(1.0F, 0.75F, 0.8F),     // 粉色
            new Vector3f(1.0F, 1.0F, 1.0F)       // 白色
    };

    public ContinuumBombEntity(EntityType<? extends ContinuumBombEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ContinuumBombEntity(Level level, LivingEntity shooter) {
        super(ModEntities.CONTINUUM_BOMB.get(), shooter, level);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.CONTINUUM_BOMB.get());
    }

    @Override
    protected void defineSynchedData() {
    }

    private Vector3f getRandomFireworkColor() {
        return FIREWORK_COLORS[random.nextInt(FIREWORK_COLORS.length)];
    }

    private void spawnColorfulParticles(double x, double y, double z, int count) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        for (int i = 0; i < count; i++) {
            double motX = (random.nextBoolean() ? -1 : 1) * random.nextDouble();
            double motY = random.nextDouble();
            double motZ = (random.nextBoolean() ? -1 : 1) * random.nextDouble();

            Vector3f color = getRandomFireworkColor();

            DustParticleOptions dustOptions = new DustParticleOptions(color, 1.5F);
            serverLevel.sendParticles(
                    dustOptions,
                    x, y, z,
                    1, motX, motY, motZ,
                    0.1D
            );

            if (random.nextFloat() < 0.3F) {
                serverLevel.sendParticles(
                        ParticleTypes.END_ROD,
                        x, y, z,
                        1, motX * 0.5, motY * 0.5, motZ * 0.5,
                        0.05D
                );
            }

            if (random.nextFloat() < 0.2F) {
                serverLevel.sendParticles(
                        ParticleTypes.FIREWORK,
                        x, y, z,
                        1, motX * 0.3, motY * 0.3, motZ * 0.3,
                        0.02D
                );
            }
        }
    }

    private void spawnExplosionParticles(double x, double y, double z) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        for (int i = 0; i < 30; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = random.nextDouble() * 0.5 + 0.5;
            double motX = Math.cos(angle) * speed;
            double motY = random.nextDouble() * 0.5;
            double motZ = Math.sin(angle) * speed;

            Vector3f color = getRandomFireworkColor();
            DustParticleOptions dustOptions = new DustParticleOptions(color, 2.0F);

            serverLevel.sendParticles(
                    dustOptions,
                    x, y, z,
                    1, motX, motY, motZ,
                    0.15D
            );

            serverLevel.sendParticles(
                    ParticleTypes.FIREWORK,
                    x, y, z,
                    2, motX * 0.5, motY * 0.5, motZ * 0.5,
                    0.1D
            );
        }

        for (int i = 0; i < 15; i++) {
            double motX = (random.nextBoolean() ? -1 : 1) * random.nextDouble() * 0.5;
            double motY = random.nextDouble() * 0.5;
            double motZ = (random.nextBoolean() ? -1 : 1) * random.nextDouble() * 0.5;

            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    x, y, z,
                    1, motX, motY, motZ,
                    0.08D
            );
        }
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

                    spawnColorfulParticles(getX(), getY(), getZ(), 20);
                }

                if (this.partyTime % 2 == 0) {
                    spawnColorfulParticles(getX(), getY(), getZ(), 5);
                }

                ++this.partyTime;
            } else {
                spawnExplosionParticles(getX(), getY(), getZ());
                discard();
            }
        } else {
            super.tick();

            if (level instanceof ServerLevel serverLevel && random.nextFloat() < 0.5F) {
                Vector3f color = getRandomFireworkColor();
                DustParticleOptions dustOptions = new DustParticleOptions(color, 1.0F);
                serverLevel.sendParticles(
                        dustOptions,
                        getX(), getY(), getZ(),
                        1, 0, 0, 0,
                        0.0D
                );
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.hasHit) {
            this.hasHit = true;
            this.hitX = this.getX();
            this.hitY = this.getY();
            this.hitZ = this.getZ();

            spawnExplosionParticles(this.hitX, this.hitY, this.hitZ);

            if (result instanceof EntityHitResult entityHit) {
                byte damage = 0;
                if (entityHit.getEntity() instanceof Blaze) {
                    damage = 3;
                }
                entityHit.getEntity().hurt(
                        DamageSource.thrown(this, this.getOwner()),
                        damage
                );
            }
        }
    }
}