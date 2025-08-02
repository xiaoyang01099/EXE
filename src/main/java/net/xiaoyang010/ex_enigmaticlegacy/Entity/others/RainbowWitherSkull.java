package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import com.mojang.math.Vector3f;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class RainbowWitherSkull extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS =
            SynchedEntityData.defineId(RainbowWitherSkull.class, EntityDataSerializers.BOOLEAN);

    public RainbowWitherSkull(EntityType<? extends RainbowWitherSkull> type, Level level) {
        super(type, level);
    }

//    public RainbowWitherSkull(Level level, LivingEntity shooter, double x, double y, double z) {
//        super(ModEntities.RAINBOW_WITHER_SKULL.get(), shooter, x, y, z, level);
//    }

    @Override
    public void tick() {
        super.tick();

        if (this.level.isClientSide) {
            // 获取当前位置和移动方向
            Vec3 motion = this.getDeltaMovement();
            double x = this.getX();
            double y = this.getY();
            double z = this.getZ();

            // 彩虹轨迹效果
            float time = (float) ((this.level.getGameTime() % 60) / 60.0 * Math.PI * 2);

            // 主尾迹
            for (int i = 0; i < 2; i++) {
                double radius = 0.3;
                double angle = time + (i * Math.PI);
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;

                // 彩虹色粒子
                float hue = (time + i * 0.5f) % 1.0f;
                Vector3f color = hsvToRgb(hue, 1.0f, 1.0f);

                this.level.addParticle(
                        new DustParticleOptions(color, 1.0F),
                        x + offsetX, y, z + offsetZ,
                        0, 0, 0
                );
            }

            // 螺旋粒子效果
            double spiralRadius = 0.5;
            int spiralPoints = 8;
            for (int i = 0; i < spiralPoints; i++) {
                double spiralAngle = time * 3 + (i * Math.PI * 2.0 / spiralPoints);
                double spiralX = Math.cos(spiralAngle) * spiralRadius;
                double spiralZ = Math.sin(spiralAngle) * spiralRadius;

                // 紫色能量粒子
                this.level.addParticle(
                        ParticleTypes.WITCH,
                        x + spiralX, y, z + spiralZ,
                        motion.x * 0.15, motion.y * 0.15, motion.z * 0.15
                );
            }

            // 中心能量核心
            for (int i = 0; i < 3; i++) {
                this.level.addParticle(
                        ParticleTypes.END_ROD,
                        x, y, z,
                        (this.random.nextDouble() - 0.5) * 0.2,
                        (this.random.nextDouble() - 0.5) * 0.2,
                        (this.random.nextDouble() - 0.5) * 0.2
                );
            }

            // 能量波纹效果
            if (this.tickCount % 5 == 0) {
                double ringRadius = 0.8;
                int ringPoints = 12;
                for (int i = 0; i < ringPoints; i++) {
                    double ringAngle = (i * Math.PI * 2.0 / ringPoints);
                    double ringX = Math.cos(ringAngle) * ringRadius;
                    double ringZ = Math.sin(ringAngle) * ringRadius;

                    this.level.addParticle(
                            ParticleTypes.REVERSE_PORTAL,
                            x + ringX, y, z + ringZ,
                            0, 0.05, 0
                    );
                }
            }
        }
    }

    // 爆炸效果增强
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level.isClientSide) {
            Explosion.BlockInteraction interaction = Explosion.BlockInteraction.NONE;
            this.level.explode(this, this.getX(), this.getY(), this.getZ(), 3.0F, false, interaction);

            // 在服务端发送额外的粒子效果包
            ((ServerLevel)this.level).sendParticles(
                    ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(), this.getY(), this.getZ(),
                    1, 0.0D, 0.0D, 0.0D, 0.0D
            );

            // 发送多个彩虹能量波
            for (int i = 0; i < 360; i += 15) {
                double angle = Math.toRadians(i);
                double radius = 2.0;
                double px = this.getX() + Math.cos(angle) * radius;
                double pz = this.getZ() + Math.sin(angle) * radius;

                ((ServerLevel)this.level).sendParticles(
                        ParticleTypes.END_ROD,
                        px, this.getY(), pz,
                        1, 0.0D, 0.2D, 0.0D, 0.1D
                );
            }

            this.discard();
        }
    }

    // HSV转RGB的辅助方法
    private Vector3f hsvToRgb(float hue, float saturation, float value) {
        int h = (int)(hue * 6);
        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        float r, g, b;
        switch (h) {
            case 0: r = value; g = t; b = p; break;
            case 1: r = q; g = value; b = p; break;
            case 2: r = p; g = value; b = t; break;
            case 3: r = p; g = q; b = value; break;
            case 4: r = t; g = p; b = value; break;
            default: r = value; g = p; b = q; break;
        }

        return new Vector3f(r, g, b);
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DANGEROUS, false);
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.WITCH;
    }

    @Override
    protected float getInertia() {
        return this.isDangerous() ? 0.73F : 0.95F;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level.isClientSide) {
            if (result.getEntity() instanceof LivingEntity target) {
                Entity owner = this.getOwner();

                // 基础伤害（受护甲影响）
                boolean flag;
                if (owner instanceof LivingEntity) {
                    flag = target.hurt(DamageSource.indirectMobAttack(this, (LivingEntity)owner), 30.0F);
                } else {
                    flag = target.hurt(DamageSource.MAGIC, 30.0F);
                }

                if (flag) {
                    // 真实伤害（无视护甲）
                    target.hurt(DamageSource.MAGIC, 10.0F);

                    // 添加凋零效果（等级4，持续10秒）
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 3));

                    // 禁用生命恢复（持续30秒）
                    target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 0, false, false));

                    // 如果攻击者是LivingEntity，添加生命汲取效果
                    if (owner instanceof LivingEntity) {
                        this.doEnchantDamageEffects((LivingEntity)owner, target);
                    }
                }
            }
        }
    }

    public boolean isDangerous() {
        return this.entityData.get(DATA_DANGEROUS);
    }

    public void setDangerous(boolean dangerous) {
        this.entityData.set(DATA_DANGEROUS, dangerous);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }
}