package org.xiaoyang.ex_enigmaticlegacy.Entity.others;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;
import org.xiaoyang.ex_enigmaticlegacy.Util.ProjectileAntiImmunityDamage;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;

public class EntityInfinityArrowLevel extends AbstractArrow {
    private static final EntityDataAccessor<Integer> ID_EFFECT_COLOR;
    private static final EntityDataAccessor<Boolean> IS_HOMING;
    private Potion potion;
    private final Set<MobEffectInstance> effects;
    private boolean fixedColor;
    private LivingEntity homingTarget;
    private Vec3 seekOrigin;
    private int homingTime;
    private static final EntityDataAccessor<Integer> SPECTRAL_TIME;
    private static final EntityDataAccessor<Integer> JUMP_COUNT;
    private static List<String> projectileAntiImmuneEntities;

    public EntityInfinityArrowLevel(EntityType<? extends EntityInfinityArrowLevel> entityType, Level world) {
        super(entityType, world);
        this.potion = Potions.EMPTY;
        this.effects = Sets.newHashSet();
        this.homingTarget = null;
        this.seekOrigin = null;
        this.homingTime = 0;
    }

    public EntityInfinityArrowLevel(Level world, double xPos, double yPos, double zPos) {
        super(ModEntities.INFINITY_ARROW_LEVEL_ENTITY.get(), xPos, yPos, zPos, world);
        this.potion = Potions.EMPTY;
        this.effects = Sets.newHashSet();
        this.homingTarget = null;
        this.seekOrigin = null;
        this.homingTime = 0;
    }

    public EntityInfinityArrowLevel(Level world, LivingEntity shooter) {
        super(ModEntities.INFINITY_ARROW_LEVEL_ENTITY.get(), shooter, world);
        this.potion = Potions.EMPTY;
        this.effects = Sets.newHashSet();
        this.homingTarget = null;
        this.seekOrigin = null;
        this.homingTime = 0;
    }

    public void setSpectral(int spectralTime) {
        this.entityData.set(SPECTRAL_TIME, spectralTime);
    }

    public int getSpectralTime() {
        return this.entityData.get(SPECTRAL_TIME);
    }

    public void setJumpCount(int jumpCount) {
        this.entityData.set(JUMP_COUNT, jumpCount);
    }

    public int getJumpCount() {
        return this.entityData.get(JUMP_COUNT);
    }

    public void tick() {
        this.updateHoming();
        this.superTick();
        if (this.level.isClientSide) {
            if (this.inGround) {
                if (this.inGroundTime % 5 == 0) {
                    this.makeParticle(1);
                }
            } else {
                this.makeParticle(2);
                if (this.entityData.get(IS_HOMING)) {
                    this.makeHomingTrail();
                }
            }
        } else if (this.inGround && this.inGroundTime != 0 && !this.effects.isEmpty() && this.inGroundTime >= 600) {
            this.level.broadcastEntityEvent(this, (byte)0);
            this.potion = Potions.EMPTY;
            this.effects.clear();
            this.entityData.set(ID_EFFECT_COLOR, -1);
        }
    }

    private void superTick() {
        if (!this.leftOwner) {
            this.leftOwner = this.checkLeftOwner();
        }

        if (!this.level.isClientSide) {
            this.setSharedFlag(6, this.isCurrentlyGlowing());
        }

        this.baseTick();
        boolean flag = this.isNoPhysics();
        Vec3 vector3d = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double f = vector3d.horizontalDistance();
            this.setYRot((float)(Mth.atan2(vector3d.x, vector3d.z) * (double)(180F / (float)Math.PI)));
            this.setXRot((float)(Mth.atan2(vector3d.y, f) * (double)(180F / (float)Math.PI)));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        BlockPos blockpos = this.blockPosition();
        BlockState blockstate = this.level.getBlockState(blockpos);
        if (!blockstate.isAir() && !flag) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level, blockpos);
            if (!voxelshape.isEmpty()) {
                Vec3 vector3d1 = this.position();

                for(AABB axisalignedbb : voxelshape.toAabbs()) {
                    if (axisalignedbb.move(blockpos).contains(vector3d1)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.shakeTime > 0) {
            --this.shakeTime;
        }

        if (this.isInWaterOrRain()) {
            this.clearFire();
        }

        if (this.inGround && !flag) {
            if (this.lastState != blockstate && this.shouldFall()) {
                this.startFalling();
            } else if (!this.level.isClientSide) {
                this.tickDespawn();
            }

            ++this.inGroundTime;
        } else {
            this.inGroundTime = 0;
            Vec3 vector3d2 = this.position();
            Vec3 vector3d3 = vector3d2.add(vector3d);
            HitResult raytraceresult = this.level.clip(new ClipContext(vector3d2, vector3d3, Block.COLLIDER, Fluid.NONE, this));
            if (raytraceresult.getType() != Type.MISS) {
                vector3d3 = raytraceresult.getLocation();
            }

            while(!this.isRemoved()) {
                EntityHitResult entityraytraceresult = this.findHitEntity(vector3d2, vector3d3);
                if (entityraytraceresult != null) {
                    raytraceresult = entityraytraceresult;
                }

                if (raytraceresult != null && raytraceresult.getType() == Type.ENTITY) {
                    Entity entity = ((EntityHitResult)raytraceresult).getEntity();
                    Entity entity1 = this.getOwner();
                    if (entity instanceof Player && entity1 instanceof Player && !((Player)entity1).canHarmPlayer((Player)entity)) {
                        raytraceresult = null;
                        entityraytraceresult = null;
                    }
                }

                if (raytraceresult != null && raytraceresult.getType() != Type.MISS && !flag && !ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                    this.onHit(raytraceresult);
                    this.hasImpulse = true;
                }

                if (entityraytraceresult == null || this.getPierceLevel() <= 0) {
                    break;
                }

                raytraceresult = null;
            }

            vector3d = this.getDeltaMovement();
            double d3 = vector3d.x;
            double d4 = vector3d.y;
            double d0 = vector3d.z;
            if (this.isCritArrow()) {
                for(int i = 0; i < 4; ++i) {
                    this.level.addParticle(ParticleTypes.CRIT, this.getX() + d3 * (double)i / (double)4.0F, this.getY() + d4 * (double)i / (double)4.0F, this.getZ() + d0 * (double)i / (double)4.0F, -d3, -d4 + 0.2, -d0);
                }
            }

            double d5 = this.getX() + d3;
            double d1 = this.getY() + d4;
            double d2 = this.getZ() + d0;
            double f1 = vector3d.horizontalDistance();
            if (flag) {
                this.setYRot((float)(Mth.atan2(-d3, -d0) * (double)(180F / (float)Math.PI)));
            } else {
                this.setYRot((float)(Mth.atan2(d3, d0) * (double)(180F / (float)Math.PI)));
            }

            this.setXRot((float)(Mth.atan2(d4, f1) * (double)(180F / (float)Math.PI)));
            this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
            this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
            float f2 = 0.99F;
            float f3 = 0.05F;
            if (this.isInWater()) {
                for(int j = 0; j < 4; ++j) {
                    float f4 = 0.25F;
                    this.level.addParticle(ParticleTypes.BUBBLE, d5 - d3 * (double)0.25F, d1 - d4 * (double)0.25F, d2 - d0 * (double)0.25F, d3, d4, d0);
                }

                f2 = this.getWaterInertia();
            }

            this.setDeltaMovement(vector3d.scale(f2));
            if (!this.isNoGravity() && !flag) {
                Vec3 vector3d4 = this.getDeltaMovement();
                this.setDeltaMovement(vector3d4.x, vector3d4.y - (double)0.05F, vector3d4.z);
            }

            this.setPos(d5, d1, d2);
            this.checkInsideBlocks();
        }

    }

    protected void onHitEntity(EntityHitResult p_213868_1_) {
        Entity entity = p_213868_1_.getEntity();
        float f = (float)this.getDeltaMovement().length();
        int i = Mth.ceil(Mth.clamp((double)f * this.getBaseDamage(), 0.0F, Integer.MAX_VALUE));
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
            long j = (long)this.random.nextInt(i / 2 + 2);
            i = (int)Math.min(j + (long)i, 2147483647L);
        }

        Entity owner = this.getOwner();
        DamageSource damagesource = this.getDamageSource(entity);
        boolean isEnderman = entity.getType() == EntityType.ENDERMAN;
        int k = entity.getRemainingFireTicks();
        if (this.isOnFire() && !isEnderman) {
            entity.setSecondsOnFire(5);
        }

        if (entity instanceof Player player) {
            if (player.isUsingItem() && player.getUseItem().getItem() instanceof ShieldItem) {
                player.getCooldowns().addCooldown(player.getUseItem().getItem(), 100);
                this.level.broadcastEntityEvent(player, (byte)30);
                player.stopUsingItem();
            }
        }

        if (entity.hurt(damagesource, (float)i)) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity)entity;
                if (!this.level.isClientSide && this.getPierceLevel() <= 0) {
                    livingentity.setArrowCount(livingentity.getArrowCount() + 1);
                }

                if (this.knockback > 0) {
                    Vec3 vector3d = this.getDeltaMovement().multiply(1.0F, 0.0F, 1.0F).normalize().scale((double)this.knockback * 0.6);
                    if (vector3d.lengthSqr() > (double)0.0F) {
                        livingentity.push(vector3d.x, 0.1, vector3d.z);
                    }
                }

                if (!this.level.isClientSide && owner instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingentity, owner);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity)owner, livingentity);
                }

                this.doPostHurtEffects(livingentity);
                if (owner != null && livingentity != owner && livingentity instanceof Player && owner instanceof ServerPlayer && !this.isSilent()) {
                    ((ServerPlayer)owner).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(livingentity);
                }

                if (!this.level.isClientSide && owner instanceof ServerPlayer) {
                    ServerPlayer serverplayerentity = (ServerPlayer)owner;
                    if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayerentity, this.piercedAndKilledEntities);
                    } else if (!entity.isAlive() && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayerentity, Arrays.asList(entity));
                    }
                }
            }

            this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.0F));
                this.setPos(entity.position());
                this.seekNextTarget();
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 4.0F, 1.0F);
            }
        } else {
            entity.setRemainingFireTicks(k);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.0F));
            this.setYRot(this.getYRot() + 180.0F);
            this.setPos(entity.position());
            this.yRotO += 180.0F;
            if (!this.level.isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7) {
                if (this.pickup == Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.seekNextTarget();
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 4.0F, 1.0F);
            }
        }

    }

    protected void onHitBlock(BlockHitResult hitResult) {
        this.lastState = this.level.getBlockState(hitResult.getBlockPos());
        BlockState blockstate = this.level.getBlockState(hitResult.getBlockPos());
        blockstate.onProjectileHit(this.level, blockstate, hitResult, this);
        Vec3 vec3 = hitResult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3);
        Vec3 vec31 = vec3.normalize().scale((double)0.05F);
        this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
        this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
        this.seekNextTarget();
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 4.0F, 1.0F);
    }

    private DamageSource getDamageSource(Entity target) {
        Entity owner = this.getOwner();
        DamageSource damagesource;
        if (owner == null) {
            damagesource = this.level.damageSources().arrow(this, this);
        } else {
            damagesource = this.level.damageSources().arrow(this, owner);
            if (owner instanceof LivingEntity livingOwner) {
                livingOwner.setLastHurtMob(target);
            }
        }

        if (projectileAntiImmuneEntities.contains(
                BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString())) {
            damagesource = new ProjectileAntiImmunityDamage(
                    this.level,
                    "infinity_arrow",
                    this,
                    damagesource.getEntity()
            );
        }

        return damagesource;
    }

    public void setEffectsFromItem(ItemStack p_184555_1_) {
        if (p_184555_1_.getItem() == Items.TIPPED_ARROW) {
            this.potion = PotionUtils.getPotion(p_184555_1_);
            Collection<MobEffectInstance> collection = PotionUtils.getCustomEffects(p_184555_1_);
            if (!collection.isEmpty()) {
                for(MobEffectInstance effectinstance : collection) {
                    this.effects.add(new MobEffectInstance(effectinstance));
                }
            }

            int i = getCustomColor(p_184555_1_);
            if (i == -1) {
                this.updateColor();
            } else {
                this.setFixedColor(i);
            }
        } else if (p_184555_1_.getItem() == Items.ARROW) {
            this.potion = Potions.EMPTY;
            this.effects.clear();
            this.entityData.set(ID_EFFECT_COLOR, -1);
        }

    }

    public static int getCustomColor(ItemStack p_191508_0_) {
        CompoundTag compoundnbt = p_191508_0_.getTag();
        return compoundnbt != null && compoundnbt.contains("CustomPotionColor", 99) ? compoundnbt.getInt("CustomPotionColor") : -1;
    }

    private void updateColor() {
        this.fixedColor = false;
        if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
            this.entityData.set(ID_EFFECT_COLOR, -1);
        } else {
            this.entityData.set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
        }

    }

    public void addEffect(MobEffectInstance p_184558_1_) {
        this.effects.add(p_184558_1_);
        this.getEntityData().set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_EFFECT_COLOR, -1);
        this.entityData.define(SPECTRAL_TIME, 0);
        this.entityData.define(JUMP_COUNT, 0);
        this.entityData.define(IS_HOMING, false);
    }

    private void makeParticle(int p_184556_1_) {
        int i = this.getColor();
        if (i != -1 && p_184556_1_ > 0) {
            double d0 = (double)(i >> 16 & 255) / (double)255.0F;
            double d1 = (double)(i >> 8 & 255) / (double)255.0F;
            double d2 = (double)(i >> 0 & 255) / (double)255.0F;

            for(int j = 0; j < p_184556_1_; ++j) {
                this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5F), this.getRandomY(), this.getRandomZ((double)0.5F), d0, d1, d2);
            }
        }

    }

    public int getColor() {
        return this.entityData.get(ID_EFFECT_COLOR);
    }

    private void setFixedColor(int p_191507_1_) {
        this.fixedColor = true;
        this.entityData.set(ID_EFFECT_COLOR, p_191507_1_);
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.potion != Potions.EMPTY && this.potion != null) {
            compound.putString("Potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
        }

        if (this.fixedColor) {
            compound.putInt("Color", this.getColor());
        }

        if (!this.effects.isEmpty()) {
            ListTag listnbt = new ListTag();

            for(MobEffectInstance effectinstance : this.effects) {
                listnbt.add(effectinstance.save(new CompoundTag()));
            }

            compound.put("CustomPotionEffects", listnbt);
        }

        if (this.getSpectralTime() > 0) {
            compound.putInt("spectral_time", this.entityData.get(SPECTRAL_TIME));
        }

        if (this.getJumpCount() > 0) {
            compound.putInt("jump_count", this.entityData.get(JUMP_COUNT));
        }

    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Potion", 8)) {
            this.potion = PotionUtils.getPotion(compound);
        }

        for(MobEffectInstance effectinstance : PotionUtils.getCustomEffects(compound)) {
            this.addEffect(effectinstance);
        }

        if (compound.contains("Color", 99)) {
            this.setFixedColor(compound.getInt("Color"));
        } else {
            this.updateColor();
        }

        if (compound.contains("spectral_time")) {
            this.setSpectral(compound.getInt("spectral_time"));
        }

        if (compound.contains("jump_count")) {
            this.setJumpCount(compound.getInt("jump_count"));
        }

    }

    protected void doPostHurtEffects(LivingEntity p_184548_1_) {
        super.doPostHurtEffects(p_184548_1_);

        for(MobEffectInstance effectinstance : this.potion.getEffects()) {
            p_184548_1_.addEffect(new MobEffectInstance(effectinstance.getEffect(), Math.max(effectinstance.getDuration() / 8, 1), effectinstance.getAmplifier(), effectinstance.isAmbient(), effectinstance.isVisible()));
        }

        if (!this.effects.isEmpty()) {
            for(MobEffectInstance effectinstance1 : this.effects) {
                p_184548_1_.addEffect(effectinstance1);
            }
        }

        int spectralTime = this.entityData.get(SPECTRAL_TIME);
        if (spectralTime > 0) {
            MobEffectInstance effectinstance = new MobEffectInstance(MobEffects.GLOWING, spectralTime, 0);
            p_184548_1_.addEffect(effectinstance);
        }

    }

    protected ItemStack getPickupItem() {
        if (this.effects.isEmpty() && this.potion == Potions.EMPTY) {
            return new ItemStack(Items.ARROW);
        } else {
            ItemStack itemstack = new ItemStack(Items.TIPPED_ARROW);
            PotionUtils.setPotion(itemstack, this.potion);
            PotionUtils.setCustomEffects(itemstack, this.effects);
            if (this.fixedColor) {
                itemstack.getOrCreateTag().putInt("CustomPotionColor", this.getColor());
            }

            return itemstack;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(byte p_70103_1_) {
        if (p_70103_1_ == 0) {
            int i = this.getColor();
            if (i != -1) {
                double d0 = (double)(i >> 16 & 255) / (double)255.0F;
                double d1 = (double)(i >> 8 & 255) / (double)255.0F;
                double d2 = (double)(i >> 0 & 255) / (double)255.0F;

                for(int j = 0; j < 20; ++j) {
                    this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX((double)0.5F), this.getRandomY(), this.getRandomZ((double)0.5F), d0, d1, d2);
                }
            }
        } else {
            super.handleEntityEvent(p_70103_1_);
        }

    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypeTags.IS_FIRE) || super.isInvulnerableTo(source);
    }

    @OnlyIn(Dist.CLIENT)
    private void makeHomingTrail() {
        Vec3 motion = this.getDeltaMovement();
        double speed = motion.length();
        double time = this.tickCount * 0.35;

        double radius = 0.4;
        for (int i = 0; i < 4; i++) {
            double angle = time + i * (Math.PI / 2.0);
            double spiralX = Math.cos(angle) * radius;
            double spiralZ = Math.sin(angle) * radius;

            WispParticleData outerWisp = WispParticleData.wisp(0.25F, 1.0F, 0.95F, 0.6F, 1.0F);
            this.level.addParticle(outerWisp,
                    this.getX() + spiralX, this.getY(), this.getZ() + spiralZ,
                    -motion.x * 0.03, 0.01, -motion.z * 0.03);
        }

        double innerRadius = 0.2;
        for (int i = 0; i < 3; i++) {
            double angle = -time * 1.5 + i * (Math.PI * 2.0 / 3.0);
            double spiralX = Math.cos(angle) * innerRadius;
            double spiralZ = Math.sin(angle) * innerRadius;

            WispParticleData innerWisp = WispParticleData.wisp(0.15F, 1.0F, 1.0F, 1.0F, 0.9F);
            this.level.addParticle(innerWisp,
                    this.getX() + spiralX, this.getY(), this.getZ() + spiralZ,
                    0, 0.015, 0);
        }

        for (int i = 0; i < 5; i++) {
            double t = i / 5.0;
            double offsetX = (this.random.nextDouble() - 0.5) * 0.15;
            double offsetY = (this.random.nextDouble() - 0.5) * 0.15;
            double offsetZ = (this.random.nextDouble() - 0.5) * 0.15;

            float r = (float)(1.0 - t * 0.1);
            float g = (float)(0.85 + t * 0.15);
            float b = (float)(0.3 + t * 0.7);

            WispParticleData trailData = WispParticleData.wisp(
                    (float)(0.2 - t * 0.08), r, g, b, (float)(0.9 - t * 0.3));
            this.level.addParticle(trailData,
                    this.getX() - motion.x * (0.3 + t * 0.5) + offsetX,
                    this.getY() - motion.y * (0.3 + t * 0.5) + offsetY,
                    this.getZ() - motion.z * (0.3 + t * 0.5) + offsetZ,
                    offsetX * 0.02, offsetY * 0.02, offsetZ * 0.02);
        }

        for (int i = 0; i < 3; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
            double offsetY = (this.random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;

            SparkleParticleData holySparkle = SparkleParticleData.sparkle(
                    0.6F + this.random.nextFloat() * 0.4F,
                    1.0F, 0.95F, 0.7F,
                    4 + this.random.nextInt(4));
            this.level.addParticle(holySparkle,
                    this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                    -motion.x * 0.1, -motion.y * 0.1, -motion.z * 0.1);
        }

        if (speed > 0.5) {
            int extraCount = (int)(speed * 2);
            for (int i = 0; i < Math.min(extraCount, 6); i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.1;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.1;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.1;

                WispParticleData speedWisp = WispParticleData.wisp(
                        0.1F, 1.0F, 1.0F, 0.85F, 0.7F);
                this.level.addParticle(speedWisp,
                        this.getX() - motion.x * i * 0.15 + offsetX,
                        this.getY() - motion.y * i * 0.15 + offsetY,
                        this.getZ() - motion.z * i * 0.15 + offsetZ,
                        0, 0, 0);
            }
        }

        if (this.entityData.get(IS_HOMING)) {
            double pulseRadius = 0.3 + Math.sin(time * 2.0) * 0.15;
            for (int i = 0; i < 6; i++) {
                double angle = time * 0.5 + i * (Math.PI / 3.0);
                double px = Math.cos(angle) * pulseRadius;
                double pz = Math.sin(angle) * pulseRadius;
                double py = Math.sin(time * 3.0 + i) * 0.1;

                WispParticleData pulseWisp = WispParticleData.wisp(
                        0.18F, 1.0F, 0.9F, 0.5F, 0.85F);
                this.level.addParticle(pulseWisp,
                        this.getX() + px, this.getY() + py, this.getZ() + pz,
                        0, 0.02, 0);
            }

            for (int i = 0; i < 2; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.2;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.2;

                WispParticleData holyBeam = WispParticleData.wisp(
                        0.12F, 1.0F, 1.0F, 0.9F, 0.6F);
                this.level.addParticle(holyBeam,
                        this.getX() + offsetX, this.getY(), this.getZ() + offsetZ,
                        0, 0.08 + this.random.nextDouble() * 0.05, 0);
            }

            for (int i = 0; i < 4; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.4;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.4;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.4;

                SparkleParticleData targetSparkle = SparkleParticleData.sparkle(
                        0.9F + this.random.nextFloat() * 0.5F,
                        1.0F, 0.85F, 0.3F,
                        5 + this.random.nextInt(5));
                this.level.addParticle(targetSparkle,
                        this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                        motion.x * 0.2, motion.y * 0.2, motion.z * 0.2);
            }

            if (this.tickCount % 10 < 5) {
                for (int axis = 0; axis < 2; axis++) {
                    for (int j = -2; j <= 2; j++) {
                        double cx = axis == 0 ? j * 0.15 : 0;
                        double cz = axis == 1 ? j * 0.15 : 0;

                        SparkleParticleData crossSparkle = SparkleParticleData.sparkle(
                                0.5F, 1.0F, 1.0F, 0.8F, 3);
                        this.level.addParticle(crossSparkle,
                                this.getX() + cx, this.getY(), this.getZ() + cz,
                                0, 0.03, 0);
                    }
                }
            }
        }
    }

    public void seekNextTarget() {
        if (this.getJumpCount() <= 16 && this.isCritArrow()) {
            if (this.seekOrigin == null) {
                this.seekOrigin = this.position();
            }

            if (!this.level.isClientSide) {
                TargetingConditions conditions = TargetingConditions.forCombat().selector((living) -> living.hasLineOfSight(this));
                Entity owner = (Entity)(this.getOwner() == null ? this : this.getOwner());
                this.homingTarget = this.level.getNearestEntity(LivingEntity.class, conditions, owner instanceof LivingEntity ? (LivingEntity)owner : null, this.seekOrigin.x, this.seekOrigin.y, this.seekOrigin.z, this.getBoundingBox().inflate((double)64.0F));
                if (this.homingTarget != null) {
                    Vec3 targetPos = this.homingTarget.getEyePosition();
                    double x = targetPos.x - this.getX();
                    double y = targetPos.y - this.getY();
                    double z = targetPos.z - this.getZ();
                    this.shoot(x, y, z, 3.0F, 0.0F);
                    this.setJumpCount(this.getJumpCount() + 1);
                    this.homingTime = 0;
                } else {
                    this.destroyArrow();
                }

            }
        } else {
            this.destroyArrow();
        }
    }

    private void updateHoming() {
        if (this.homingTarget != null) {
            if (!this.level.isClientSide) {
                this.entityData.set(IS_HOMING, true);
            }

            if (this.homingTime++ > 60) {
                this.destroyArrow();
            } else if (!this.homingTarget.isDeadOrDying() && !this.homingTarget.isRemoved()) {
                Vec3 targetPos = this.homingTarget.getEyePosition();
                if (!(targetPos.distanceToSqr(this.position()) < (double)4.0F)) {
                    double x = targetPos.x - this.getX();
                    double y = targetPos.y - this.getY();
                    double z = targetPos.z - this.getZ();
                    this.shoot(x, y, z, 3.0F, 0.0F);
                    this.hasImpulse = true;
                }
            } else {
                this.homingTarget = null;
                if (!this.level.isClientSide) {
                    this.entityData.set(IS_HOMING, false);
                }
                this.seekNextTarget();
            }
        } else {
            if (!this.level.isClientSide) {
                this.entityData.set(IS_HOMING, false);
            }
        }
    }

    private void destroyArrow() {
        if (!this.level.isClientSide) {
            Level var2 = this.level;
            if (var2 instanceof ServerLevel level) {
                level.sendParticles(
                        ParticleTypes.SMOKE,
                        this.getX(), this.getY(), this.getZ(),
                        10,
                        0.0, 0.0, 0.0,
                        4.0
                );

                level.explode(
                        this.getOwner() == null ? this : this.getOwner(),
                        this.getX(), this.getY(), this.getZ(),
                        4.0F,
                        Level.ExplosionInteraction.NONE
                );
            }

            this.discard();
        }
    }

    static {
        ID_EFFECT_COLOR = SynchedEntityData.defineId(EntityInfinityArrowLevel.class, EntityDataSerializers.INT);
        SPECTRAL_TIME = SynchedEntityData.defineId(EntityInfinityArrowLevel.class, EntityDataSerializers.INT);
        JUMP_COUNT = SynchedEntityData.defineId(EntityInfinityArrowLevel.class, EntityDataSerializers.INT);
        IS_HOMING = SynchedEntityData.defineId(EntityInfinityArrowLevel.class, EntityDataSerializers.BOOLEAN);
        projectileAntiImmuneEntities = Lists.newArrayList(
                "minecraft:enderman",
                "minecraft:wither",
                "minecraft:ender_dragon",
                "draconicevolution:guardian_wither"
        );
    }
}
