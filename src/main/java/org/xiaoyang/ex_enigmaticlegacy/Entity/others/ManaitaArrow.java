package org.xiaoyang.ex_enigmaticlegacy.Entity.others;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Entity.biological.CatMewEntity;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ManaitaArrow extends AbstractArrow {
    private static final int EXPOSED_POTION_DECAY_TIME = 600;
    private static final int NO_EFFECT_COLOR = -1;
    private static final EntityDataAccessor<Integer> ID_EFFECT_COLOR =
            SynchedEntityData.defineId(ManaitaArrow.class, EntityDataSerializers.INT);
    private static final byte EVENT_POTION_PUFF = 0;
    private Potion potion;
    private final Set<MobEffectInstance> effects;
    private boolean fixedColor;


    public ManaitaArrow(EntityType<? extends ManaitaArrow> entityType, Level level) {
        super(entityType, level);
        this.setBaseDamage(Float.POSITIVE_INFINITY);
        this.potion = Potions.EMPTY;
        this.effects = Sets.newHashSet();
    }

    public ManaitaArrow(Level level, LivingEntity owner) {
        super(ModEntities.MANAITA_ARROW.get(), owner, level);
        this.setOwner(owner);
        this.setBaseDamage(Float.POSITIVE_INFINITY);
        this.potion = Potions.EMPTY;
        this.effects = Sets.newHashSet();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        float f = (float) this.getDeltaMovement().length();
        int i = Mth.ceil(Mth.clamp((double) f * this.getBaseDamage(), 0.0D, 2.147483647E9D));
        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }

            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }

            if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.discard();
                return;
            }

            this.piercingIgnoreEntityIds.add(entity.getId());
        }

        if (this.isCritArrow()) {
            long j = (long) this.random.nextInt(i / 2 + 2);
            i = (int) Math.min(j + (long) i, 2147483647L);
        }

        Entity owner = this.getOwner();
        DamageSource damagesource = this.level().damageSources().playerAttack((Player) owner);
        if (owner instanceof LivingEntity) {
            ((LivingEntity) owner).setLastHurtMob(entity);
        }

        int k = entity.getRemainingFireTicks();
        if (this.isOnFire()) {
            entity.setSecondsOnFire(5);
        }

        boolean hurt;
        if (entity instanceof EnderDragon enderDragon) {
            hurt = enderDragon.hurt(enderDragon.head, damagesource, Integer.MAX_VALUE);
        } else if (entity instanceof WitherBoss witherBoss) {
            witherBoss.setInvulnerableTicks(0);
            hurt = witherBoss.hurt(damagesource, Float.MAX_VALUE);
        } else if (entity instanceof EnderDragonPart part) {
            EnderDragon enderDragon = part.parentMob;
            if (enderDragon != null) {
                hurt = enderDragon.hurt(enderDragon.head, damagesource, Integer.MAX_VALUE);
            }
            hurt = part.parentMob.hurt(part.parentMob.head, damagesource, Integer.MAX_VALUE);
        } else {
            hurt = entity.hurt(damagesource, (float) i);
        }

        if (hurt) {

            if (entity instanceof LivingEntity livingentity) {
                if (!this.level().isClientSide && this.getPierceLevel() <= 0) {
                    livingentity.setArrowCount(livingentity.getArrowCount() + 1);
                }

                if (this.getKnockback() > 0) {
                    Vec3 vec3 = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double) this.getKnockback() * 0.6D);
                    if (vec3.lengthSqr() > 0.0D) {
                        livingentity.push(vec3.x, 0.1D, vec3.z);
                    }
                }

                if (!this.level().isClientSide && owner instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingentity, owner);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) owner, livingentity);
                }

                this.doPostHurtEffects(livingentity);
                if (livingentity != owner && livingentity instanceof Player && owner instanceof ServerPlayer && !this.isSilent()) {
                    ((ServerPlayer) owner).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(livingentity);
                }

                if (!this.level().isClientSide && owner instanceof ServerPlayer serverplayer) {
                    if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, this.piercedAndKilledEntities);
                    } else if (!entity.isAlive() && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, List.of(entity));
                    }
                }
            }

            this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0) {
                this.discard();
            }
        } else {
            entity.setRemainingFireTicks(k);
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
            this.setYRot(this.getYRot() + 180.0F);
            this.yRotO += 180.0F;
            if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
                if (this.pickup == Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.discard();
            }
        }
    }

    private void invokeDropAllDeathLoot(LivingEntity entity, DamageSource source) {
        try {
            Method method = LivingEntity.class.getDeclaredMethod("dropAllDeathLoot", DamageSource.class);
            method.setAccessible(true);
            method.invoke(entity, source);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEffectsFromItem(ItemStack pStack) {
        if (pStack.is(Items.TIPPED_ARROW)) {
            this.potion = PotionUtils.getPotion(pStack);
            Collection<MobEffectInstance> $$1 = PotionUtils.getCustomEffects(pStack);
            if (!$$1.isEmpty()) {
                for (MobEffectInstance $$2 : $$1) {
                    this.effects.add(new MobEffectInstance($$2));
                }
            }

            int $$3 = getCustomColor(pStack);
            if ($$3 == -1) {
                this.updateColor();
            } else {
                this.setFixedColor($$3);
            }
        } else if (pStack.is(Items.ARROW)) {
            this.potion = Potions.EMPTY;
            this.effects.clear();
            this.entityData.set(ID_EFFECT_COLOR, -1);
        }

    }

    public static int getCustomColor(ItemStack pStack) {
        CompoundTag $$1 = pStack.getTag();
        return $$1 != null && $$1.contains("CustomPotionColor", 99) ? $$1.getInt("CustomPotionColor") : -1;
    }

    private void updateColor() {
        this.fixedColor = false;
        if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
            this.entityData.set(ID_EFFECT_COLOR, -1);
        } else {
            this.entityData.set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
        }

    }

    public void addEffect(MobEffectInstance pEffectInstance) {
        this.effects.add(pEffectInstance);
        this.getEntityData().set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_EFFECT_COLOR, -1);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.inGround) {
            Vec3 position = this.position();

            double radius = 30.0;
            AABB damageArea = new AABB(
                    position.x - radius, position.y - radius, position.z - radius,
                    position.x + radius, position.y + radius, position.z + radius
            );
            this.level().getEntitiesOfClass(LivingEntity.class, damageArea).forEach(e -> {
                if (!(e instanceof Player) && e.isAlive()) {
                    e.hurt(this.level().damageSources().genericKill(), 10240f);
                }
            });
            List<LivingEntity> entities = this.level().getEntitiesOfClass(
                    LivingEntity.class,
                    damageArea,
                    entity -> {
                        return entity != this.getOwner() &&
                                !entity.isInvulnerable() &&
                                entity.isAlive() &&
                                entity.invulnerableTime <= 0;
                    }
            );

            Entity owner = this.getOwner();
            // FIX: DamageSource 静态方法 → level().damageSources() 实例方法
            DamageSource damageSource = owner instanceof Player ?
                    this.level().damageSources().playerAttack((Player) owner) :
                    this.level().damageSources().arrow(this, this);

            for (LivingEntity entity : entities) {
                double distance = entity.position().distanceTo(position);
                double damageFalloff = 1.0 - (distance / radius);

                if (damageFalloff > 0) {
                    float damage = 1000 * (float) damageFalloff;
                    boolean hurt = false;

                    if (entity instanceof CatMewEntity miaoMiao) {
                        miaoMiao.invulnerableTime = 0;
                        hurt = miaoMiao.hurt(owner instanceof Player ?
                                        this.level().damageSources().playerAttack((Player) owner) :
                                        this.level().damageSources().magic(),
                                Float.max(10000, 50000));
                    } else if (entity instanceof EnderDragon enderDragon) {
                        hurt = enderDragon.hurt(this.level().damageSources().magic(), damage);
                    } else if (entity instanceof WitherBoss witherBoss) {
                        witherBoss.setInvulnerableTicks(0);
                        hurt = witherBoss.hurt(damageSource, damage);
                    } else {
                        hurt = entity.hurt(damageSource, damage);
                    }

                    if (hurt) {
                        Vec3 knockback = entity.position().subtract(this.position()).normalize();
                        entity.push(knockback.x * 2.5, 2.3, knockback.z * 2.5);
                        this.doPostHurtEffects(entity);

                        if (owner instanceof LivingEntity livingOwner) {
                            EnchantmentHelper.doPostHurtEffects(entity, livingOwner);
                            EnchantmentHelper.doPostDamageEffects(livingOwner, entity);
                        }

                        if (this.level().isClientSide) {
                            for (int ii = 0; ii < 5; ii++) {
                                this.level().addParticle(
                                        ParticleTypes.CRIT,
                                        entity.getX() + (random.nextDouble() - 0.5),
                                        entity.getY() + entity.getBbHeight() * 0.5 + (random.nextDouble() - 0.5),
                                        entity.getZ() + (random.nextDouble() - 0.5),
                                        0, 0, 0
                                );
                            }
                        }
                    }
                }
            }
        }

        if (this.level().isClientSide) {
            if (this.inGround) {
                if (this.inGroundTime % 5 == 0) {
                    this.makeParticle(1);
                }
            } else {
                this.makeParticle(2);
                this.createBeamParticles();
            }
        } else if (this.inGround && this.inGroundTime != 0 && !this.effects.isEmpty() && this.inGroundTime >= 600) {
            this.level().broadcastEntityEvent(this, (byte) 0);
            this.potion = Potions.EMPTY;
            this.effects.clear();
            this.entityData.set(ID_EFFECT_COLOR, -1);
        }
    }

    private void applyAreaDamageEffects(LivingEntity target, float damage) {
        if (!this.level().isClientSide && this.getOwner() instanceof LivingEntity owner) {
            Vec3 knockback = target.position().subtract(this.position()).normalize();
            target.push(knockback.x * 2.5, 2.3, knockback.z * 2.5);

            this.doPostHurtEffects(target);

            EnchantmentHelper.doPostHurtEffects(target, owner);
            EnchantmentHelper.doPostDamageEffects(owner, target);
        }
    }

    private void createBeamParticles() {
        Vec3 motion = this.getDeltaMovement();
        Vec3 position = this.position();
        Vec3 direction = motion.normalize();

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize();
        up = right.cross(direction).normalize();

        double mainBeamRadius = 20.0;
        int particleCount = 36;
        float time = (float) (this.tickCount * 0.4);

        for (int layer = 0; layer < 6; layer++) {
            double angle = time + (layer * Math.PI * 2.0 / 6.0);
            double baseRadius = mainBeamRadius * (0.2 + layer * 0.1);

            for (int i = 0; i < particleCount; i++) {
                double particleAngle = angle + (i * Math.PI * 2.0 / particleCount);
                double radius = baseRadius * (1 + Math.sin(time * 2 + particleAngle) * 0.1);

                Vec3 offset = right.scale(Math.cos(particleAngle) * radius)
                        .add(up.scale(Math.sin(particleAngle) * radius));

                switch (layer % 6) {
                    case 0:
                        this.level().addParticle(ParticleTypes.END_ROD,
                                position.x + offset.x,
                                position.y + offset.y,
                                position.z + offset.z,
                                Math.cos(time) * 0.01,
                                Math.sin(time) * 0.01,
                                0);
                        break;
                    case 1:
                        this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                                position.x + offset.x,
                                position.y + offset.y,
                                position.z + offset.z,
                                (random.nextDouble() - 0.5) * 0.1,
                                0.05,
                                (random.nextDouble() - 0.5) * 0.1);
                        break;
                    case 2:
                        this.level().addParticle(ParticleTypes.PORTAL,
                                position.x + offset.x,
                                position.y + offset.y,
                                position.z + offset.z,
                                (random.nextDouble() - 0.5) * 0.2,
                                -0.2,
                                (random.nextDouble() - 0.5) * 0.2);
                        break;
                    case 3:
                        this.level().addParticle(ParticleTypes.REVERSE_PORTAL,
                                position.x + offset.x,
                                position.y + offset.y,
                                position.z + offset.z,
                                (random.nextDouble() - 0.5) * 0.1,
                                0.2,
                                (random.nextDouble() - 0.5) * 0.1);
                        break;
                    case 4:
                        this.level().addParticle(ParticleTypes.DRAGON_BREATH,
                                position.x + offset.x,
                                position.y + offset.y,
                                position.z + offset.z,
                                (random.nextDouble() - 0.5) * 0.1,
                                (random.nextDouble() - 0.5) * 0.1,
                                (random.nextDouble() - 0.5) * 0.1);
                        break;
                    case 5:
                        this.level().addParticle(ParticleTypes.GLOW,
                                position.x + offset.x,
                                position.y + offset.y,
                                position.z + offset.z,
                                0, 0, 0);
                        break;
                }
            }
        }

        double spiralTime = time * 2;
        int spiralCount = 8;
        for (int i = 0; i < spiralCount; i++) {
            double spiralAngle = spiralTime + (i * Math.PI * 2.0 / spiralCount);
            double spiralRadius = mainBeamRadius * 0.8;
            Vec3 spiralOffset = right.scale(Math.cos(spiralAngle) * spiralRadius)
                    .add(up.scale(Math.sin(spiralAngle) * spiralRadius));

            for (int j = 0; j < 5; j++) {
                double subAngle = random.nextDouble() * Math.PI * 2;
                double subRadius = random.nextDouble() * 2.0;
                Vec3 subOffset = right.scale(Math.cos(subAngle) * subRadius)
                        .add(up.scale(Math.sin(subAngle) * subRadius));

                this.level().addParticle(ParticleTypes.ENCHANTED_HIT,
                        position.x + spiralOffset.x + subOffset.x,
                        position.y + spiralOffset.y + subOffset.y,
                        position.z + spiralOffset.z + subOffset.z,
                        (random.nextDouble() - 0.5) * 0.2,
                        (random.nextDouble() - 0.5) * 0.2,
                        (random.nextDouble() - 0.5) * 0.2);
            }
        }

        int tailLength = 10;
        Vec3 backOffset = direction.scale(-1.0);
        for (int i = 0; i < tailLength; i++) {
            double distanceBack = i * 0.5;
            double spread = 0.3 * (1 + i * 0.1);

            for (int j = 0; j < 3; j++) {
                this.level().addParticle(ParticleTypes.DRAGON_BREATH,
                        position.x + backOffset.x * distanceBack + (random.nextDouble() - 0.5) * spread,
                        position.y + backOffset.y * distanceBack + (random.nextDouble() - 0.5) * spread,
                        position.z + backOffset.z * distanceBack + (random.nextDouble() - 0.5) * spread,
                        0, 0, 0);
            }

            if (i % 2 == 0) {
                this.level().addParticle(ParticleTypes.END_ROD,
                        position.x + backOffset.x * distanceBack,
                        position.y + backOffset.y * distanceBack,
                        position.z + backOffset.z * distanceBack,
                        (random.nextDouble() - 0.5) * 0.1,
                        (random.nextDouble() - 0.5) * 0.1,
                        (random.nextDouble() - 0.5) * 0.1);
            }
        }

        if (random.nextFloat() < 0.8) {
            for (int i = 0; i < 3; i++) {
                double lightningRadius = mainBeamRadius * 0.5;
                double lightningAngle = random.nextDouble() * Math.PI * 2;
                Vec3 lightningOffset = right.scale(Math.cos(lightningAngle) * lightningRadius)
                        .add(up.scale(Math.sin(lightningAngle) * lightningRadius));

                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        position.x + lightningOffset.x,
                        position.y + lightningOffset.y,
                        position.z + lightningOffset.z,
                        (random.nextDouble() - 0.5) * 0.5,
                        (random.nextDouble() - 0.5) * 0.5,
                        (random.nextDouble() - 0.5) * 0.5);

                for (int j = 0; j < 3; j++) {
                    this.level().addParticle(ParticleTypes.GLOW,
                            position.x + lightningOffset.x + (random.nextDouble() - 0.5),
                            position.y + lightningOffset.y + (random.nextDouble() - 0.5),
                            position.z + lightningOffset.z + (random.nextDouble() - 0.5),
                            (random.nextDouble() - 0.5) * 0.1,
                            (random.nextDouble() - 0.5) * 0.1,
                            (random.nextDouble() - 0.5) * 0.1);
                }
            }
        }
    }

    private void makeParticle(int pParticleAmount) {
        int $$1 = this.getColor();
        if ($$1 != -1 && pParticleAmount > 0) {
            double $$2 = (double) ($$1 >> 16 & 255) / 255.0;
            double $$3 = (double) ($$1 >> 8 & 255) / 255.0;
            double $$4 = (double) ($$1 & 255) / 255.0;

            for (int $$5 = 0; $$5 < pParticleAmount; ++$$5) {
                this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), $$2, $$3, $$4);
            }
        }
    }

    public int getColor() {
        return this.entityData.get(ID_EFFECT_COLOR);
    }

    private void setFixedColor(int pFixedColor) {
        this.fixedColor = true;
        this.entityData.set(ID_EFFECT_COLOR, pFixedColor);
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (this.potion != Potions.EMPTY) {
            // FIX: Registry.POTION → BuiltInRegistries.POTION
            pCompound.putString("Potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
        }

        if (this.fixedColor) {
            pCompound.putInt("Color", this.getColor());
        }

        if (!this.effects.isEmpty()) {
            ListTag $$1 = new ListTag();
            for (MobEffectInstance $$2 : this.effects) {
                $$1.add($$2.save(new CompoundTag()));
            }
            pCompound.put("CustomPotionEffects", $$1);
        }

    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("Potion", 8)) {
            this.potion = PotionUtils.getPotion(pCompound);
        }

        for (MobEffectInstance $$1 : PotionUtils.getCustomEffects(pCompound)) {
            this.addEffect($$1);
        }

        if (pCompound.contains("Color", 99)) {
            this.setFixedColor(pCompound.getInt("Color"));
        } else {
            this.updateColor();
        }

    }

    protected void doPostHurtEffects(LivingEntity pLiving) {
        super.doPostHurtEffects(pLiving);
        Entity $$1 = this.getEffectSource();

        for (MobEffectInstance $$3 : this.potion.getEffects()) {
            pLiving.addEffect(new MobEffectInstance($$3.getEffect(), Math.max($$3.getDuration() / 8, 1), $$3.getAmplifier(), $$3.isAmbient(), $$3.isVisible()), $$1);
        }

        if (!this.effects.isEmpty()) {
            for (MobEffectInstance $$3 : this.effects) {
                pLiving.addEffect($$3, $$1);
            }
        }

    }

    @Override
    protected ItemStack getPickupItem() {
        if (this.effects.isEmpty() && this.potion == Potions.EMPTY) {
            return new ItemStack(Items.ARROW);
        } else {
            ItemStack $$0 = new ItemStack(Items.TIPPED_ARROW);
            PotionUtils.setPotion($$0, this.potion);
            PotionUtils.setCustomEffects($$0, this.effects);
            if (this.fixedColor) {
                $$0.getOrCreateTag().putInt("CustomPotionColor", this.getColor());
            }

            return $$0;
        }
    }

    public void handleEntityEvent(byte pId) {
        if (pId == 0) {
            int $$1 = this.getColor();
            if ($$1 != -1) {
                double $$2 = (double) ($$1 >> 16 & 255) / 255.0;
                double $$3 = (double) ($$1 >> 8 & 255) / 255.0;
                double $$4 = (double) ($$1 & 255) / 255.0;

                for (int $$5 = 0; $$5 < 20; ++$$5) {
                    this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), $$2, $$3, $$4);
                }
            }
        } else {
            super.handleEntityEvent(pId);
        }

    }

    // FIX: 移除底部的 static{} 初始化块，已合并到字段声明处
}