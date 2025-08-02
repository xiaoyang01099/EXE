package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;

import java.util.ArrayList;

public class EntityThunderpealOrb extends ThrowableProjectile {
    public int area = 4;
    public float damage;

    private static final float DEFAULT_DAMAGE_THUNDERPEAL_DIRECT = 24.0F;
    private static final float DEFAULT_DAMAGE_THUNDERPEAL_BOLT = 16.0F;

    public EntityThunderpealOrb(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
        this.damage = DEFAULT_DAMAGE_THUNDERPEAL_BOLT;
    }

    public EntityThunderpealOrb(Level level) {
        this(ModEntities.THUNDERPEAL_ORB.get(), level);
    }

    public EntityThunderpealOrb(Level level, LivingEntity shooter) {
        super(ModEntities.THUNDERPEAL_ORB.get(), shooter, level);
        this.damage = DEFAULT_DAMAGE_THUNDERPEAL_BOLT;
    }

    @Override
    protected void defineSynchedData() {
    }

    public void shootLightning(Level world, Entity entity, double xx, double yy, double zz, boolean main) {
        Vector3 initPos = Vector3.fromEntity(entity);
        Vector3 finalPos = new Vector3(xx, yy, zz);
        Vector3 diffVec = finalPos.copy().sub(initPos);
        Vector3 motionVec = initPos.add(diffVec.copy().multiply(1.0F / getDistance(finalPos.x, finalPos.y, finalPos.z) * 0.5F));

        float curve = 0.5F;
        double distance = Math.sqrt(entity.distanceToSqr(finalPos.x, finalPos.y, finalPos.z));
        curve = (float)(curve * 1.0F / distance * 24.0F);

        float width;
        if (main) {
            width = 0.075F;
        } else {
            width = 0.04F;
        }

        RelicsEventHandler.imposeLightning(
                entity.level,
                motionVec.x, motionVec.y, motionVec.z,
                finalPos.x, finalPos.y, finalPos.z,
                20, curve, (int)(distance * 1.6F),
                0, width
        );
    }

    private double getDistance(double x, double y, double z) {
        double dx = this.getX() - x;
        double dy = this.getY() - y;
        double dz = this.getZ() - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    protected float getGravity() {
        return 0.05F;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        Entity hitEntity = null;
        if (hitResult instanceof EntityHitResult entityHitResult) {
            hitEntity = entityHitResult.getEntity();
            hitEntity.hurt(new ModDamageSources.DamageSourceTLightning(this.getOwner()), DEFAULT_DAMAGE_THUNDERPEAL_DIRECT);
            hitEntity.invulnerableTime = 0;
        }

        if (!this.level.isClientSide) {
            ArrayList<Entity> list = RelicsEventHandler.getEntitiesInRange(
                    this.level,
                    this.getX(), this.getY(), this.getZ(),
                    this,
                    LivingEntity.class,
                    (double)this.area
            );

            if (this.getOwner() != null && list.contains(this.getOwner())) {
                list.remove(this.getOwner());
            }

            if (list.contains(hitEntity)) {
                list.remove(hitEntity);
            }

            for (Entity e : list) {
                Vector3 targetVec = Vector3.fromEntityCenter((LivingEntity)e);
                this.shootLightning(this.level, this, targetVec.x, targetVec.y, targetVec.z, true);
                e.invulnerableTime = 0;
                e.hurt(new ModDamageSources.DamageSourceTLightning(this.getOwner()), this.damage);

                ArrayList<Entity> exlist = RelicsEventHandler.getEntitiesInRange(
                        e.level,
                        e.getX(), e.getY(), e.getZ(),
                        e,
                        LivingEntity.class,
                        4.0
                );

                if (this.getOwner() != null && exlist.contains(this.getOwner())) {
                    exlist.remove(this.getOwner());
                }

                if (exlist.contains(e)) {
                    exlist.remove(e);
                }

                while (exlist.size() > 3) {
                    exlist.remove((int)(Math.random() * (double)exlist.size()));
                }

                for (Entity ex : exlist) {
                    Vector3 targetVecX = Vector3.fromEntityCenter((LivingEntity)ex);
                    this.shootLightning(this.level, e, targetVecX.x, targetVecX.y, targetVecX.z, false);
                    ex.invulnerableTime = 0;
                    ex.hurt(new ModDamageSources.DamageSourceTLightning(this.getOwner()), this.damage / 2.0F);
                }
            }

            RelicsEventHandler.imposeBurst(this.level, this.getX(), this.getY(), this.getZ(), 2.0F);
            RelicsEventHandler.imposeBurst(this.level, this.getX(), this.getY(), this.getZ(), 2.0F);

            this.level.playSound(null,
                    this.getX(), this.getY(), this.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.HOSTILE,
                    2.0F,
                    1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
            );
        }

        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > 500) {
            this.discard();
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        } else {
            this.markHurt();
            Entity attacker = damageSource.getEntity();
            if (attacker != null) {
                Vec3 lookVec = attacker.getLookAngle();
                if (lookVec != null) {
                    this.setDeltaMovement(lookVec.x * 0.9, lookVec.y * 0.9, lookVec.z * 0.9);

                    this.level.playSound(null,
                            this.getX(), this.getY(), this.getZ(),
                            SoundEvents.LIGHTNING_BOLT_IMPACT,
                            SoundSource.HOSTILE,
                            1.0F,
                            1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
                    );
                }
                return true;
            } else {
                return false;
            }
        }
    }
}