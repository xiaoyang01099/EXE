package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Registry;
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
import net.minecraft.world.entity.projectile.Arrow;
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
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.CatMewEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ManaitaArrow extends AbstractArrow {
    private static final int EXPOSED_POTION_DECAY_TIME = 600;
    private static final int NO_EFFECT_COLOR = -1;
    private static final EntityDataAccessor<Integer> ID_EFFECT_COLOR;
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
        float f = (float)this.getDeltaMovement().length();
        int i = Mth.ceil(Mth.clamp((double)f * this.getBaseDamage(), 0.0D, 2.147483647E9D));
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
        // DamageSource damagesource = DamageSource.arrow(this, owner);
        DamageSource damagesource = DamageSource.playerAttack((Player)owner);
        if (owner instanceof LivingEntity) {
            ((LivingEntity)owner).setLastHurtMob(entity);
        }

        int k = entity.getRemainingFireTicks();
        if (this.isOnFire()) {
            entity.setSecondsOnFire(5);
        }

        boolean hurt;
        if (entity instanceof EnderDragon enderDragon){
            hurt = enderDragon.hurt(enderDragon.head, damagesource, Integer.MAX_VALUE);
        }else if (entity instanceof WitherBoss witherBoss){
            witherBoss.setInvulnerableTicks(0);
            hurt = witherBoss.hurt(damagesource, Float.MAX_VALUE);
        }else if (entity instanceof EnderDragonPart part){
            EnderDragon enderDragon = part.parentMob;
            if (enderDragon != null) {
                hurt = enderDragon.hurt(enderDragon.head, damagesource, Integer.MAX_VALUE);
            }
            hurt = part.parentMob.hurt(part.parentMob.head, damagesource, Integer.MAX_VALUE);
        }else {
            hurt = entity.hurt(damagesource, (float) i);
        }

        if (hurt) {

            if (entity instanceof LivingEntity livingentity) {
                if (!this.level.isClientSide && this.getPierceLevel() <= 0) {
                    livingentity.setArrowCount(livingentity.getArrowCount() + 1);
                }

                if (this.getKnockback() > 0) {
                    Vec3 vec3 = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double)this.knockback * 0.6D);
                    if (vec3.lengthSqr() > 0.0D) {
                        livingentity.push(vec3.x, 0.1D, vec3.z);
                    }
                }

                if (!this.level.isClientSide && owner instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingentity, owner);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity)owner, livingentity);
                }

                this.doPostHurtEffects(livingentity);
                if (livingentity != owner && livingentity instanceof Player && owner instanceof ServerPlayer && !this.isSilent()) {
                    ((ServerPlayer)owner).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(livingentity);
                }

                if (!this.level.isClientSide && owner instanceof ServerPlayer serverplayer) {
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
            if (!this.level.isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
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
            method.setAccessible(true);  // 绕过 protected 访问权限
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
                Iterator var3 = $$1.iterator();

                while(var3.hasNext()) {
                    MobEffectInstance $$2 = (MobEffectInstance)var3.next();
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

        // 处理粒子和范围伤害
        if (!this.inGround) {
            // 获取箭矢当前位置
            Vec3 position = this.position();

            // 获取范围内的实体
            double radius = 30.0; // 与粒子效果范围一致
            AABB damageArea = new AABB(
                    position.x - radius, position.y - radius, position.z - radius,
                    position.x + radius, position.y + radius, position.z + radius
            );
            this.level.getEntitiesOfClass(LivingEntity.class, damageArea).forEach(e -> {
                if (!(e instanceof Player) && e.isAlive()) {
                    e.hurt(DamageSource.OUT_OF_WORLD, 10240f);
                }
            });
            List<LivingEntity> entities = this.level.getEntitiesOfClass(
                    LivingEntity.class,
                    damageArea,
                    entity -> {
                        // 过滤判定条件：
                        return entity != this.getOwner() &&
                                !entity.isInvulnerable() &&
                                entity.isAlive() &&
                                entity.invulnerableTime <= 0;
                    }
            );

            Entity owner = this.getOwner();
            DamageSource damageSource = owner instanceof Player ?
                    DamageSource.playerAttack((Player)owner) :
                    DamageSource.arrow(this, this);

            // 对范围内的实体造成伤害
            for (LivingEntity entity : entities) {
                // 计算距离衰减
                double distance = entity.position().distanceTo(position);
                double damageFalloff = 1.0 - (distance / radius); // 距离越远伤害越低

                if (damageFalloff > 0) {
                    float damage = 1000 * (float)damageFalloff; // 基础伤害 * 距离衰减
                    boolean hurt = false;

                    /// 造成伤害
                    if (entity instanceof CatMewEntity miaoMiao) {
                        miaoMiao.invulnerableTime = 0;  // 直接设置无敌时间为0
                        // 使用玩家攻击伤害源
                        hurt = miaoMiao.hurt(owner instanceof Player ?
                                        DamageSource.playerAttack((Player)owner) :
                                        DamageSource.MAGIC,
                                Float.max(10000,50000));
                    }
                    else if (entity instanceof EnderDragon enderDragon) {
                        hurt = enderDragon.hurt(DamageSource.MAGIC, damage);
                    } else if (entity instanceof WitherBoss witherBoss) {
                        witherBoss.setInvulnerableTicks(0);
                        hurt = witherBoss.hurt(damageSource, damage);
                    } else {
                        hurt = entity.hurt(damageSource, damage);
                    }

                    if (hurt) {
                        // 击中效果
                        // 应用击退效果
                        Vec3 knockback = entity.position().subtract(this.position()).normalize();
                        entity.push(knockback.x * 2.5, 2.3, knockback.z * 2.5);
                        this.doPostHurtEffects(entity);

                        // 应用附魔效果
                        if (owner instanceof LivingEntity livingOwner) {
                            EnchantmentHelper.doPostHurtEffects(entity, livingOwner);
                            EnchantmentHelper.doPostDamageEffects(livingOwner, entity);
                        }

                        // 添加击中粒子效果
                        if (this.level.isClientSide) {
                            for (int i = 0; i < 5; i++) {
                                this.level.addParticle(
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

        if (this.level.isClientSide) {
            if (this.inGround) {
                if (this.inGroundTime % 5 == 0) {
                    this.makeParticle(1);
                }
            } else {
                this.makeParticle(2);
                this.createBeamParticles();
            }
        } else if (this.inGround && this.inGroundTime != 0 && !this.effects.isEmpty() && this.inGroundTime >= 600) {
            this.level.broadcastEntityEvent(this, (byte)0);
            this.potion = Potions.EMPTY;
            this.effects.clear();
            this.entityData.set(ID_EFFECT_COLOR, -1);
        }
    }

    // 添加一个方法来处理范围伤害的命中效果
    private void applyAreaDamageEffects(LivingEntity target, float damage) {
        if (!this.level.isClientSide && this.getOwner() instanceof LivingEntity owner) {
            // 应用击退效果
            Vec3 knockback = target.position().subtract(this.position()).normalize();
            target.push(knockback.x * 2.5, 2.3, knockback.z * 2.5);

            // 应用药水效果
            this.doPostHurtEffects(target);

            // 应用附魔效果
            EnchantmentHelper.doPostHurtEffects(target, owner);
            EnchantmentHelper.doPostDamageEffects((LivingEntity)owner, target);
        }
    }

    private void createBeamParticles() {
        Vec3 motion = this.getDeltaMovement();
        Vec3 position = this.position();
        Vec3 direction = motion.normalize();

        // 计算垂直于飞行方向的向量
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize();
        up = right.cross(direction).normalize();

        // 主光束参数
        double mainBeamRadius = 20.0; // 增大主光束半径到20
        int particleCount = 36; // 增加粒子数量
        float time = (float) (this.tickCount * 0.4);

        // 生成主螺旋光束 - 现在有6层
        for (int layer = 0; layer < 6; layer++) {
            double layerOffset = layer * 0.5; // 增大层间距
            double angle = time + (layer * Math.PI * 2.0 / 6.0);
            double baseRadius = mainBeamRadius * (0.2 + layer * 0.1); // 每层半径不同

            for (int i = 0; i < particleCount; i++) {
                double particleAngle = angle + (i * Math.PI * 2.0 / particleCount);
                double radius = baseRadius * (1 + Math.sin(time * 2 + particleAngle) * 0.1); // 添加呼吸效果

                Vec3 offset = right.scale(Math.cos(particleAngle) * radius)
                        .add(up.scale(Math.sin(particleAngle) * radius));

                // 每层使用更丰富的粒子组合
                switch (layer % 6) {
                    case 0:
                        // 末地光束
                        level.addParticle(ParticleTypes.END_ROD,
                                position.x + offset.x,
                                position.y + offset.y,
                                position.z + offset.z,
                                Math.cos(time) * 0.01,
                                Math.sin(time) * 0.01,
                                0);
                        break;
                    case 1:
                        // 灵魂火焰
                        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                                position.x + offset.x,
                                position.y + offset.y,
                                position.z + offset.z,
                                (random.nextDouble() - 0.5) * 0.1,
                                0.05,
                                (random.nextDouble() - 0.5) * 0.1);
                        break;
                    case 2:
                        // 传送门效果
                        level.addParticle(ParticleTypes.PORTAL,
                                position.x + offset.x,
                                position.y + offset.y,
                                position.z + offset.z,
                                (random.nextDouble() - 0.5) * 0.2,
                                -0.2,
                                (random.nextDouble() - 0.5) * 0.2);
                        break;
                    case 3:
                        // 反向传送门
                        level.addParticle(ParticleTypes.REVERSE_PORTAL,
                                position.x + offset.x,
                                position.y + offset.y,
                                position.z + offset.z,
                                (random.nextDouble() - 0.5) * 0.1,
                                0.2,
                                (random.nextDouble() - 0.5) * 0.1);
                        break;
                    case 4:
                        // 龙息粒子
                        level.addParticle(ParticleTypes.DRAGON_BREATH,
                                position.x + offset.x,
                                position.y + offset.y,
                                position.z + offset.z,
                                (random.nextDouble() - 0.5) * 0.1,
                                (random.nextDouble() - 0.5) * 0.1,
                                (random.nextDouble() - 0.5) * 0.1);
                        break;
                    case 5:
                        // 发光粒子
                        level.addParticle(ParticleTypes.GLOW,
                                position.x + offset.x,
                                position.y + offset.y,
                                position.z + offset.z,
                                0, 0, 0);
                        break;
                }
            }
        }

        // 添加能量场旋涡效果
        double spiralTime = time * 2;
        int spiralCount = 8;
        for (int i = 0; i < spiralCount; i++) {
            double spiralAngle = spiralTime + (i * Math.PI * 2.0 / spiralCount);
            double spiralRadius = mainBeamRadius * 0.8;
            Vec3 spiralOffset = right.scale(Math.cos(spiralAngle) * spiralRadius)
                    .add(up.scale(Math.sin(spiralAngle) * spiralRadius));

            // 在每个点生成小型能量旋涡
            for (int j = 0; j < 5; j++) {
                double subAngle = random.nextDouble() * Math.PI * 2;
                double subRadius = random.nextDouble() * 2.0;
                Vec3 subOffset = right.scale(Math.cos(subAngle) * subRadius)
                        .add(up.scale(Math.sin(subAngle) * subRadius));

                level.addParticle(ParticleTypes.ENCHANTED_HIT,
                        position.x + spiralOffset.x + subOffset.x,
                        position.y + spiralOffset.y + subOffset.y,
                        position.z + spiralOffset.z + subOffset.z,
                        (random.nextDouble() - 0.5) * 0.2,
                        (random.nextDouble() - 0.5) * 0.2,
                        (random.nextDouble() - 0.5) * 0.2);
            }
        }

        // 添加尾迹效果
        int tailLength = 10;
        Vec3 backOffset = direction.scale(-1.0);
        for (int i = 0; i < tailLength; i++) {
            double distanceBack = i * 0.5;
            double spread = 0.3 * (1 + i * 0.1);

            // 龙息尾迹
            for (int j = 0; j < 3; j++) {
                level.addParticle(ParticleTypes.DRAGON_BREATH,
                        position.x + backOffset.x * distanceBack + (random.nextDouble() - 0.5) * spread,
                        position.y + backOffset.y * distanceBack + (random.nextDouble() - 0.5) * spread,
                        position.z + backOffset.z * distanceBack + (random.nextDouble() - 0.5) * spread,
                        0, 0, 0);
            }

            // 闪光尾迹
            if (i % 2 == 0) {
                level.addParticle(ParticleTypes.END_ROD,
                        position.x + backOffset.x * distanceBack,
                        position.y + backOffset.y * distanceBack,
                        position.z + backOffset.z * distanceBack,
                        (random.nextDouble() - 0.5) * 0.1,
                        (random.nextDouble() - 0.5) * 0.1,
                        (random.nextDouble() - 0.5) * 0.1);
            }
        }

        // 添加闪电效果
        if (random.nextFloat() < 0.8) { // 增加到30%几率
            for (int i = 0; i < 3; i++) { // 每次生成多个闪电
                double lightningRadius = mainBeamRadius * 0.5;
                double lightningAngle = random.nextDouble() * Math.PI * 2;
                Vec3 lightningOffset = right.scale(Math.cos(lightningAngle) * lightningRadius)
                        .add(up.scale(Math.sin(lightningAngle) * lightningRadius));

                // 闪电主体
                level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                        position.x + lightningOffset.x,
                        position.y + lightningOffset.y,
                        position.z + lightningOffset.z,
                        (random.nextDouble() - 0.5) * 0.5,
                        (random.nextDouble() - 0.5) * 0.5,
                        (random.nextDouble() - 0.5) * 0.5);

                // 闪电分支
                for (int j = 0; j < 3; j++) {
                    level.addParticle(ParticleTypes.GLOW,
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
            double $$2 = (double)($$1 >> 16 & 255) / 255.0;
            double $$3 = (double)($$1 >> 8 & 255) / 255.0;
            double $$4 = (double)($$1 >> 0 & 255) / 255.0;

            for(int $$5 = 0; $$5 < pParticleAmount; ++$$5) {
                this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), $$2, $$3, $$4);
            }
        }
    }

    public int getColor() {
        return (Integer)this.entityData.get(ID_EFFECT_COLOR);
    }

    private void setFixedColor(int pFixedColor) {
        this.fixedColor = true;
        this.entityData.set(ID_EFFECT_COLOR, pFixedColor);
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (this.potion != Potions.EMPTY) {
            pCompound.putString("Potion", Registry.POTION.getKey(this.potion).toString());
        }

        if (this.fixedColor) {
            pCompound.putInt("Color", this.getColor());
        }

        if (!this.effects.isEmpty()) {
            ListTag $$1 = new ListTag();
            Iterator var3 = this.effects.iterator();

            while(var3.hasNext()) {
                MobEffectInstance $$2 = (MobEffectInstance)var3.next();
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

        Iterator var2 = PotionUtils.getCustomEffects(pCompound).iterator();

        while(var2.hasNext()) {
            MobEffectInstance $$1 = (MobEffectInstance)var2.next();
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
        Iterator var3 = this.potion.getEffects().iterator();

        MobEffectInstance $$3;
        while(var3.hasNext()) {
            $$3 = (MobEffectInstance)var3.next();
            pLiving.addEffect(new MobEffectInstance($$3.getEffect(), Math.max($$3.getDuration() / 8, 1), $$3.getAmplifier(), $$3.isAmbient(), $$3.isVisible()), $$1);
        }

        if (!this.effects.isEmpty()) {
            var3 = this.effects.iterator();

            while(var3.hasNext()) {
                $$3 = (MobEffectInstance)var3.next();
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
                double $$2 = (double)($$1 >> 16 & 255) / 255.0;
                double $$3 = (double)($$1 >> 8 & 255) / 255.0;
                double $$4 = (double)($$1 >> 0 & 255) / 255.0;

                for(int $$5 = 0; $$5 < 20; ++$$5) {
                    this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), $$2, $$3, $$4);
                }
            }
        } else {
            super.handleEntityEvent(pId);
        }

    }

    static {
        ID_EFFECT_COLOR = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.INT);
    }
}
