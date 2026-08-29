package org.xiaoyang.ex_enigmaticlegacy.Entity.others.bow;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.xiaoyang.ex_enigmaticlegacy.Util.EntityUtil;
import org.xiaoyang.ex_enigmaticlegacy.Util.ParticleTemplate;
import org.xiaoyang.ex_enigmaticlegacy.Config.MagicBowConfig;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;

import java.util.List;

// MagicArrowEntity 是魔法弓射出的箭，负责低下坠、蓄力类型同步、方块落地扩散和命中控制器触发。
public class MagicArrowEntity extends Arrow {
    public static final int TRAIL_INTERVAL_TICKS = 1; // 魔法箭客户端拖尾刷新间隔。
    private static final EntityDataAccessor<Integer> CHARGE_TYPE = SynchedEntityData.defineId(MagicArrowEntity.class, EntityDataSerializers.INT); // 魔法箭蓄力类型同步字段。

    // 配置缓存字段。
    private float velocityMultiplier;
    private double gravityCompensation;
    private int strongSnareDurationTicks;
    private int superSnareDurationTicks;
    private int groundedDiscardTicks;
    private int groundedTicks;
    private boolean autoTrackingShot; // 本箭是否属于自动追踪发射，用于让飞行更直更快。
    private boolean trailColorsInitialized; // 客户端拖尾颜色是否已经为本支箭初始化。
    private int trailStartColor; // 本支箭拖尾固定开始色。
    private int trailEndColor; // 本支箭拖尾固定结束色。

    public MagicArrowEntity(EntityType<? extends MagicArrowEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
        loadConfigValues();
    }

    // 读取魔法箭运行需要的配置值。
    public void loadConfigValues() {
        this.velocityMultiplier = MagicBowConfig.arrowVelocityMultiplier();
        this.gravityCompensation = MagicBowConfig.gravityCompensation();
        this.strongSnareDurationTicks = MagicBowConfig.strongSnareDurationTicks();
        this.superSnareDurationTicks = MagicBowConfig.superSnareDurationTicks();
        this.groundedDiscardTicks = MagicBowConfig.groundedDiscardTicks();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CHARGE_TYPE, MagicBowParticleEffectEntity.CHARGE_NORMAL);
    }

    public void setChargeType(int chargeType) {
        this.entityData.set(CHARGE_TYPE, chargeType);
    }

    // 标记当前箭是否来自自动追踪发射。
    public void setAutoTrackingShot(boolean autoTrackingShot) {
        this.autoTrackingShot = autoTrackingShot;
    }

    // 判断当前箭是否来自自动追踪发射。
    public boolean isAutoTrackingShot() {
        return autoTrackingShot;
    }

    public int getChargeType() {
        return this.entityData.get(CHARGE_TYPE);
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity * velocityMultiplier, inaccuracy);
    }

    @Override
    public void tick() {
        Vec3 previousPos = new Vec3(this.xOld, this.yOld, this.zOld);
        super.tick();
        if (!this.level().isClientSide()) {
            tickGroundedDiscard();
        }
        if (!this.inGround && !this.isNoGravity()) {
            Vec3 motion = this.getDeltaMovement();
            double compensation = autoTrackingShot ? gravityCompensation * 1.5D : gravityCompensation;
            this.setDeltaMovement(motion.x, motion.y + compensation, motion.z);
        }
        if (this.level().isClientSide() && !this.inGround && this.tickCount > 1 && this.tickCount % TRAIL_INTERVAL_TICKS == 0) {
            spawnTrailEffects(previousPos, this.position());
        }
    }

    // 魔法箭落地后使用独立计时清理，避免继承原版箭在地上保留太久。
    public void tickGroundedDiscard() {
        if (!this.inGround) {
            groundedTicks = 0;
            return;
        }
        groundedTicks++;
        if (groundedTicks >= groundedDiscardTicks) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        if (!canDamage(target)) {
            this.discard();
            return;
        }

        int chargeType = getChargeType();
        Vec3 hitDirection = this.getDeltaMovement();
        if (chargeType != MagicBowParticleEffectEntity.CHARGE_NORMAL) {
            this.setKnockback(0);
        }
        super.onHitEntity(result);
        if (!this.level().isClientSide()) {
            if (target instanceof LivingEntity living && chargeType != MagicBowParticleEffectEntity.CHARGE_NORMAL) {
                applySnare(living, chargeType);
            }
            if (chargeType != MagicBowParticleEffectEntity.CHARGE_NORMAL) {
                target.setDeltaMovement(Vec3.ZERO);
                target.hurtMarked = true;
            }
            int effectType = chargeType == MagicBowParticleEffectEntity.CHARGE_NORMAL
                    ? MagicBowParticleEffectEntity.EFFECT_IMPACT
                    : MagicBowParticleEffectEntity.EFFECT_METEOR;
            Vec3 pos = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            spawnEffect(effectType, pos, target, hitDirection);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        Vec3 hitDirection = this.getDeltaMovement();
        super.onHitBlock(result);
        if (this.level().isClientSide()) {
            spawnArrowLandingDiffusion(result.getLocation(), hitDirection);
        } else {
            applyArrowLandingSplashDamage(result.getLocation());
        }
    }

    // 给强蓄力和星辰裁决命中的目标施加束缚。
    public void applySnare(LivingEntity target, int chargeType) {
        int duration = chargeType == MagicBowParticleEffectEntity.CHARGE_SUPER ? superSnareDurationTicks : strongSnareDurationTicks;
        if (chargeType == MagicBowParticleEffectEntity.CHARGE_SUPER) {
            duration = Math.max(duration, MagicBowParticleEffectEntity.STAR_JUDGEMENT_TOTAL_DURATION_TICKS);
            duration = Math.max(duration, Math.max(MagicBowConfig.superFinalStrikeDelayTicks(),
                    MagicBowParticleEffectEntity.STAR_JUDGEMENT_MIN_FINAL_STRIKE_DELAY_TICKS) + 25);
        }
        if (duration <= 0) return;
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 6, false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, 128, false, false, false));
    }

    // 服务端创建短生命周期粒子效果实体。
    public void spawnEffect(int effectType, Vec3 pos, Entity target) {
        spawnEffect(effectType, pos, target, null);
    }

    // 服务端创建带命中方向的粒子效果实体。
    public void spawnEffect(int effectType, Vec3 pos, Entity target, Vec3 hitDirection) {
        MagicBowParticleEffectEntity effect = new MagicBowParticleEffectEntity(ModEntities.MAGIC_BOW_PARTICLE_EFFECT_ENTITY.get(), this.level());
        effect.setEffectData(effectType, getChargeType(), pos, this.getOwner(), target, hitDirection);
        this.level().addFreshEntity(effect);
    }

    // 客户端在箭落地方块时直接播放小范围扩散，不再额外等待效果实体同步。
    public void spawnArrowLandingDiffusion(Vec3 position, Vec3 hitDirection) {
        RandomSource random = RandomSource.create(this.getId() * 73471L ^ this.tickCount * 19349663L);
        int[] colors = MagicBowParticleEffects.randomGradientColors(random);
        ParticleTemplate.emitGroundDiffusion(position, 2.25F, MagicBowParticleEffectEntity.ARROW_LANDING_DIFFUSION_BURST,
                1.15F, 0.075F, colors[0], colors[1], random);
    }

    // 服务端在箭落地方块时直接结算一次扩散伤害，避免创建额外效果实体。
    public void applyArrowLandingSplashDamage(Vec3 center) {
        double radius = MagicBowConfig.arrowLandingSplashRadius();
        float damage = MagicBowConfig.arrowLandingSplashDamage();
        if (radius <= 0.0D || damage <= 0.0F) return;

        AABB area = new AABB(center, center).inflate(radius);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area, this::canDamageLandingSplash);
        for (LivingEntity target : targets) {
            target.hurt(this.damageSources().magic(), damage);
        }
    }

    // 客户端沿箭上一帧到当前帧补点生成拖尾，避免高速飞行时拖尾断层。
    public void spawnTrailEffects(Vec3 previousPos, Vec3 currentPos) {
        ensureTrailColors();
        double distance = previousPos.distanceTo(currentPos);
        int points = Math.max(2, Math.min(6, (int) Math.ceil(distance * 1.4D)));
        Vec3 trailDirection = previousPos.subtract(currentPos);
        if (trailDirection.lengthSqr() < 1.0E-6D) {
            trailDirection = new Vec3(0.0D, 1.0D, 0.0D);
        }
        for (int i = 0; i <= points; i++) {
            double progress = (double) i / (double) points;
            RandomSource random = createTrailRandom(i);
            MagicBowParticleEffects.spawnTrail(previousPos.lerp(currentPos, progress), trailDirection, getChargeType(), random, new int[] {trailStartColor, trailEndColor});
        }
    }

    // 每支箭只随机一次拖尾渐变色，后续所有拖尾补点复用这一组颜色。
    public void ensureTrailColors() {
        if (trailColorsInitialized) return;
        long seed = this.getUUID().getMostSignificantBits() ^ this.getUUID().getLeastSignificantBits() ^ 0x51A7C0DEL;
        int[] colors = MagicBowParticleEffects.randomGradientColors(RandomSource.create(seed));
        trailStartColor = colors[0];
        trailEndColor = colors[1];
        trailColorsInitialized = true;
    }

    // 为每个拖尾补点生成稳定随机源，让形状和扰动随箭、tick 和补点序号变化。
    public RandomSource createTrailRandom(int pointIndex) {
        long seed = this.getUUID().getLeastSignificantBits()
                ^ ((long) this.getId() << 32)
                ^ ((long) this.tickCount * 997L)
                ^ ((long) pointIndex * 7919L);
        return RandomSource.create(seed);
    }

    // 判断当前目标是否允许被魔法弓伤害。
    public boolean canDamage(Entity target) {
        if (!(target instanceof LivingEntity living) || !living.isAlive()) return false;
        Entity owner = this.getOwner();
        if (owner != null && target.getUUID().equals(owner.getUUID())) return false;
        return !EntityUtil.isInDamageWhitelist(target);
    }

    // 箭落地方块扩散只伤害怪物类目标。
    public boolean canDamageLandingSplash(Entity target) {
        return target instanceof Enemy && canDamage(target);
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ChargeType", getChargeType());
        tag.putInt("GroundedTicks", groundedTicks);
        tag.putBoolean("AutoTrackingShot", autoTrackingShot);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setChargeType(tag.getInt("ChargeType"));
        groundedTicks = tag.getInt("GroundedTicks");
        autoTrackingShot = tag.getBoolean("AutoTrackingShot");
        loadConfigValues();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
