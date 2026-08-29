package org.xiaoyang.ex_enigmaticlegacy.Entity.others.bow;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.xiaoyang.ex_enigmaticlegacy.Util.EntityUtil;
import org.xiaoyang.ex_enigmaticlegacy.Util.ParticleTemplate;
import org.xiaoyang.ex_enigmaticlegacy.Config.MagicBowConfig;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModSounds;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// MagicBowParticleEffectEntity 负责同步魔法弓粒子、流星伤害和星辰裁决范围伤害。
public class MagicBowParticleEffectEntity extends Entity {
    private static final int[] IMPACT_COLORS = {0xCCDDFF, 0x32ADE6, 0x7C4DFF, 0xFF44CC, 0xFFCC00}; // 命中溅射颜色预设。
    public static final int EFFECT_TRAIL_DURATION_TICKS = 12; // 拖尾效果实体保留 tick 数。
    public static final int EFFECT_ENTITY_DURATION_TICKS = 200; // 命中效果实体保留 tick 数。
    public static final int NORMAL_EXPLOSION_BURST = 72; // 普通命中爆炸粒子数量。
    public static final int STRONG_EXPLOSION_BURST = 120; // 强蓄力命中爆炸粒子数量。
    public static final int SUPER_EXPLOSION_BURST = 180; // 星辰裁决命中爆炸粒子数量。
    public static final int STRONG_GROUND_RING_BURST = 110; // 强蓄力地面扩散粒子数量。
    public static final int SUPER_GROUND_RING_BURST = 150; // 星辰裁决地面扩散粒子数量。
    public static final float GROUND_RING_RADIUS = 5.4F; // 地面扩散环最大半径。
    public static final int GROUND_RING_DURATION_TICKS = 42; // 地面扩散环持续 tick 数。

    public static final int SUPER_METEOR_COUNT = 120; // 星辰裁决最终轰炸流星数量。
    public static final int STRONG_METEOR_SPAWN_SPREAD_TICKS = 10; // 强蓄力历史流星分批 tick 数，当前不再使用。
    public static final int STAR_JUDGEMENT_BOMBARDMENT_START_TICKS = 30; // 星辰裁决开始流星轰炸的 tick。
    public static final int STAR_JUDGEMENT_MIN_FINAL_STRIKE_DELAY_TICKS = 150; // 星辰裁决最终伤害最小延迟 tick 数。
    public static final int STAR_JUDGEMENT_FINAL_BEAM_DURATION_TICKS = 8; // 星辰裁决最终粗光束下落 tick 数。
    public static final int STAR_JUDGEMENT_TOTAL_DURATION_TICKS = 180; // 星辰裁决法阵和束缚总持续 tick 数。
    public static final int STAR_JUDGEMENT_DAMAGE_SCAN_INTERVAL_TICKS = 20; // 星辰裁决轰炸期范围怪物缓存刷新间隔。
    public static final int STAR_JUDGEMENT_BEAM_PATH_DIFFUSION_COUNT = 8; // 星辰裁决光束路径随机扩散数量。
    public static final int METEOR_FALL_DURATION_TICKS = 14; // 每颗流星走完整条路径的 tick 数。
    public static final float METEOR_TRAIL_POINTS_PER_BLOCK = 1.8F; // 流星虚拟头每格补几个箭拖尾点。
    public static final double METEOR_START_HEIGHT = 10.0D; // 流星起点相对目标中心高度。

    public static final double METEOR_END_RADIUS = 0.8D; // 流星落点水平随机半径。
    public static final float STAR_JUDGEMENT_TRIANGLE_SIDE_LENGTH = 8.5F; // 星辰裁决地面交叉三角形边长。
    public static final float STAR_JUDGEMENT_TRIANGLE_EXTRA_SECONDS = 0.5F; // 星辰裁决地面交叉三角形额外淡出秒数。
    public static final int SNARE_REFRESH_INTERVAL_TICKS = 24; // 束缚环刷新间隔 tick 数。
    public static final float SNARE_VISUAL_FADE_SECONDS = 0.25F; // 束缚环粒子额外淡出秒数。
    public static final double STAR_JUDGEMENT_VISUAL_RENDER_DISTANCE = 160.0D; // 星辰裁决法阵强制提交的最大可见距离。
    public static final float STAR_JUDGEMENT_FIXED_VISUAL_RADIUS = 16.0F; // 星辰裁决法阵和轰炸渲染固定半径。
    public static final double STAR_JUDGEMENT_VISUAL_HEIGHT = 24.0D; // 星辰裁决法阵相对视觉中心的基础高度。
    public static final double STAR_JUDGEMENT_VISUAL_CULL_RADIUS = 36.0D; // 星辰裁决法阵包围盒水平半径。
    public static final int ARROW_LANDING_DIFFUSION_BURST = 46; // 魔法箭落地小扩散粒子数量。
    public static final int STAR_JUDGEMENT_OPENING_DIFFUSION_BURST = 240; // 星辰裁决开场超大扩散粒子数量。
    public static final int STAR_JUDGEMENT_FINAL_DIFFUSION_BURST = 360; // 星辰裁决最终超大扩散粒子数量。

    public static final int EFFECT_TRAIL = 0; // 拖尾效果类型。
    public static final int EFFECT_IMPACT = 1; // 命中爆炸效果类型。
    public static final int EFFECT_METEOR = 2; // 流星和束缚效果类型。

    public static final int CHARGE_NORMAL = 0; // 普通蓄力类型。
    public static final int CHARGE_STRONG = 1; // 强蓄力类型。
    public static final int CHARGE_SUPER = 2; // 星辰裁决蓄力类型。

    private static final EntityDataAccessor<Integer> EFFECT_TYPE = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.INT); // 效果类型同步字段。
    private static final EntityDataAccessor<Integer> CHARGE_TYPE = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.INT); // 蓄力类型同步字段。
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.INT); // 目标实体 id 同步字段。
    private static final EntityDataAccessor<Integer> DURATION_TICKS = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.INT); // 效果实体生命周期同步字段。
    private static final EntityDataAccessor<Integer> SNARE_DURATION_TICKS = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.INT); // 束缚持续时间同步字段。
    private static final EntityDataAccessor<Integer> STRIKE_INTERVAL_TICKS = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.INT); // 流星周期伤害间隔同步字段。
    private static final EntityDataAccessor<Float> STRIKE_DAMAGE = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.FLOAT); // 流星周期伤害同步字段。
    private static final EntityDataAccessor<Integer> STAR_JUDGEMENT_STRIKE_DELAY_TICKS = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.INT); // 星辰裁决最终伤害延迟同步字段。
    private static final EntityDataAccessor<Float> STAR_JUDGEMENT_DAMAGE = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.FLOAT); // 星辰裁决最终伤害同步字段。
    private static final EntityDataAccessor<Float> STAR_JUDGEMENT_RADIUS = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.FLOAT); // 星辰裁决最终伤害半径同步字段。
    private static final EntityDataAccessor<Float> TARGET_WIDTH = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.FLOAT); // 目标宽度同步字段。
    private static final EntityDataAccessor<Float> TARGET_HEIGHT = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.FLOAT); // 目标高度同步字段。
    private static final EntityDataAccessor<Float> ANCHOR_X = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.FLOAT); // 束缚锚点 X 同步字段。
    private static final EntityDataAccessor<Float> ANCHOR_Y = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.FLOAT); // 束缚锚点 Y 同步字段。
    private static final EntityDataAccessor<Float> ANCHOR_Z = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.FLOAT); // 束缚锚点 Z 同步字段。
    private static final EntityDataAccessor<Float> TRAIL_DIR_X = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.FLOAT); // 命中方向 X 同步字段。
    private static final EntityDataAccessor<Float> TRAIL_DIR_Y = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.FLOAT); // 命中方向 Y 同步字段。
    private static final EntityDataAccessor<Float> TRAIL_DIR_Z = SynchedEntityData.defineId(MagicBowParticleEffectEntity.class, EntityDataSerializers.FLOAT); // 命中方向 Z 同步字段。

    // 服务端本地字段。
    @Nullable
    private UUID ownerUUID;
    private boolean starJudgementAreaDamageApplied;
    private boolean[] meteorLandedStates; // 客户端每颗虚拟流星是否已经播放落点效果。
    private final List<Integer> starJudgementBombardmentTargetIds = new ArrayList<>(); // 服务端每秒缓存的星辰裁决轰炸范围目标 id。

    public MagicBowParticleEffectEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(EFFECT_TYPE, EFFECT_TRAIL);
        this.entityData.define(CHARGE_TYPE, CHARGE_NORMAL);
        this.entityData.define(TARGET_ID, -1);
        this.entityData.define(DURATION_TICKS, EFFECT_TRAIL_DURATION_TICKS);
        this.entityData.define(SNARE_DURATION_TICKS, 0);
        this.entityData.define(STRIKE_INTERVAL_TICKS, 10);
        this.entityData.define(STRIKE_DAMAGE, 0.0F);
        this.entityData.define(STAR_JUDGEMENT_STRIKE_DELAY_TICKS, 0);
        this.entityData.define(STAR_JUDGEMENT_DAMAGE, 0.0F);
        this.entityData.define(STAR_JUDGEMENT_RADIUS, 0.0F);
        this.entityData.define(TARGET_WIDTH, 1.0F);
        this.entityData.define(TARGET_HEIGHT, 1.8F);
        this.entityData.define(ANCHOR_X, 0.0F);
        this.entityData.define(ANCHOR_Y, 0.0F);
        this.entityData.define(ANCHOR_Z, 0.0F);
        this.entityData.define(TRAIL_DIR_X, 0.0F);
        this.entityData.define(TRAIL_DIR_Y, 1.0F);
        this.entityData.define(TRAIL_DIR_Z, 0.0F);
    }

    // 初始化效果实体数据，服务端生成后立即调用。
    public void setEffectData(int effectType, int chargeType, Vec3 pos, @Nullable Entity owner, @Nullable Entity target) {
        setEffectData(effectType, chargeType, pos, owner, target, null);
    }

    // 初始化效果实体数据，拖尾和命中效果额外同步运动方向。
    public void setEffectData(int effectType, int chargeType, Vec3 pos, @Nullable Entity owner, @Nullable Entity target, @Nullable Vec3 effectDirection) {
        this.setPos(pos);
        this.entityData.set(EFFECT_TYPE, effectType);
        this.entityData.set(CHARGE_TYPE, chargeType);
        this.entityData.set(TARGET_ID, target == null ? -1 : target.getId());
        this.entityData.set(TARGET_WIDTH, target == null ? 1.0F : target.getBbWidth());
        this.entityData.set(TARGET_HEIGHT, target == null ? 1.8F : target.getBbHeight());
        this.entityData.set(ANCHOR_X, (float) pos.x);
        this.entityData.set(ANCHOR_Y, (float) (target == null ? pos.y : target.getY()));
        this.entityData.set(ANCHOR_Z, (float) pos.z);
        Vec3 direction = effectDirection == null || effectDirection.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 1.0D, 0.0D) : effectDirection.normalize();
        this.entityData.set(TRAIL_DIR_X, (float) direction.x);
        this.entityData.set(TRAIL_DIR_Y, (float) direction.y);
        this.entityData.set(TRAIL_DIR_Z, (float) direction.z);
        this.ownerUUID = owner == null ? null : owner.getUUID();
        loadConfigValues(effectType, chargeType);
    }

    // 从配置读取本次效果需要的字段值。
    public void loadConfigValues(int effectType, int chargeType) {
        if (effectType == EFFECT_TRAIL) {
            this.entityData.set(DURATION_TICKS, EFFECT_TRAIL_DURATION_TICKS);
            this.entityData.set(SNARE_DURATION_TICKS, 0);
            return;
        }

        int starJudgementDelay = chargeType == CHARGE_SUPER ? Math.max(MagicBowConfig.superFinalStrikeDelayTicks(), STAR_JUDGEMENT_MIN_FINAL_STRIKE_DELAY_TICKS) : MagicBowConfig.superFinalStrikeDelayTicks();
        int durationTicks = chargeType == CHARGE_SUPER ? Math.max(STAR_JUDGEMENT_TOTAL_DURATION_TICKS, starJudgementDelay + 25) : EFFECT_ENTITY_DURATION_TICKS;
        this.entityData.set(DURATION_TICKS, durationTicks);
        int snareDuration = chargeType == CHARGE_SUPER ? Math.max(getConfiguredSnareDurationTicks(chargeType), durationTicks) : getConfiguredSnareDurationTicks(chargeType);
        this.entityData.set(SNARE_DURATION_TICKS, snareDuration);
        this.entityData.set(STRIKE_INTERVAL_TICKS, MagicBowConfig.meteorStrikeIntervalTicks());
        this.entityData.set(STRIKE_DAMAGE, MagicBowConfig.meteorStrikeDamage());
        this.entityData.set(STAR_JUDGEMENT_STRIKE_DELAY_TICKS, starJudgementDelay);
        this.entityData.set(STAR_JUDGEMENT_DAMAGE, MagicBowConfig.superFinalStrikeDamage());
        this.entityData.set(STAR_JUDGEMENT_RADIUS, (float) MagicBowConfig.superFinalStrikeRadius());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            tickClientParticles();
        } else {
            tickServerDamage();
        }

        if (this.tickCount > this.entityData.get(DURATION_TICKS)) {
            this.discard();
        }
    }

    // 客户端按效果类型生成 GPU 粒子。
    public void tickClientParticles() {
        if (Exe.POST == null) return;
        int effectType = this.entityData.get(EFFECT_TYPE);
        int chargeType = this.entityData.get(CHARGE_TYPE);
        RandomSource random = RandomSource.create(this.getId() * 31L + this.tickCount * 997L);

        if (effectType == EFFECT_TRAIL && this.tickCount == 1) {
            spawnTrail(random, chargeType);
            return;
        }
        if (effectType == EFFECT_IMPACT && this.tickCount == 1) {
            spawnExplosion(random, chargeType);
            if (isBlockImpactEffect()) {
                spawnArrowLandingDiffusion(random);
            }
            if (chargeType != CHARGE_NORMAL) {
                spawnGroundRing(random, chargeType);
            }
            return;
        }
        if (effectType == EFFECT_METEOR) {
            if (this.tickCount == 1) {
                if (chargeType == CHARGE_SUPER) {
                    spawnStarJudgementOpeningDiffusions(random);
                    spawnStarJudgementGroundTriangles(random);
                } else if (chargeType == CHARGE_STRONG) {
                    // 强蓄力流星视觉关闭，只保留束缚和地面三角形。
                    spawnGroundTriangle(random, chargeType);
                }
                spawnSnare(random, chargeType);
            }
            if (chargeType == CHARGE_SUPER) {
                tickMeteorLines(chargeType);
            }
            if (isSnareVisualActiveTick(chargeType) && this.tickCount % SNARE_REFRESH_INTERVAL_TICKS == 1) {
                spawnSnare(random, chargeType);
            }
            if (chargeType == CHARGE_SUPER) {
                tickStarJudgementFinalStrike(random);
                return;
            }
        }
    }

    // 服务端执行星辰裁决范围伤害，强蓄力只保留束缚控制。
    public void tickServerDamage() {
        int effectType = this.entityData.get(EFFECT_TYPE);
        if (effectType == EFFECT_IMPACT) {
            if (this.tickCount == 1 && isBlockImpactEffect()) {
                applyArrowLandingSplashDamage();
            }
            return;
        }
        if (effectType != EFFECT_METEOR) return;

        Entity target = getTarget();
        lockSnaredTarget(target);
        int chargeType = this.entityData.get(CHARGE_TYPE);
        if (this.tickCount == 1 && chargeType == CHARGE_SUPER) {
            playStarJudgementSummonSound();
        }
        if (chargeType == CHARGE_STRONG) {
            // 强蓄力不再执行流星周期伤害，只保留束缚控制。
            return;
        }
        int interval = this.entityData.get(STRIKE_INTERVAL_TICKS);
        if (chargeType != CHARGE_SUPER && interval > 0 && this.tickCount % interval == 0 && target instanceof LivingEntity living && canDamage(living)) {
            living.hurt(this.damageSources().magic(), this.entityData.get(STRIKE_DAMAGE));
            lockSnaredTarget(target);
        }

        int starJudgementDelay = this.entityData.get(STAR_JUDGEMENT_STRIKE_DELAY_TICKS);
        if (chargeType == CHARGE_SUPER && isStarJudgementBombardmentDamageTick(starJudgementDelay)) {
            applyStarJudgementBombardmentDamage();
        }
        if (chargeType == CHARGE_SUPER && !starJudgementAreaDamageApplied && this.tickCount >= starJudgementDelay) {
            applyStarJudgementAreaDamage();
            starJudgementAreaDamageApplied = true;
        }
    }

    // 在星辰裁决法阵展开时播放一次服务端同步音效。
    public void playStarJudgementSummonSound() {
        Vec3 soundPos = getStarJudgementGroundCenter();
        this.level().playSound(null, soundPos.x, soundPos.y, soundPos.z,
                ModSounds.AAAAA.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    // 生成箭飞行拖尾粒子。
    public void spawnTrail(RandomSource random, int chargeType) {
        Vec3 direction = getTrailDirection(random);
        int[] colors = randomGradientColors(random);
        MagicBowParticleEffects.spawnTrail(this.position(), direction, chargeType, random, colors);
    }

    // 读取服务端同步的拖尾基础方向，具体扰动统一交给拖尾 helper 处理。
    public Vec3 getTrailDirection(RandomSource random) {
        Vec3 direction = new Vec3(
                this.entityData.get(TRAIL_DIR_X),
                this.entityData.get(TRAIL_DIR_Y),
                this.entityData.get(TRAIL_DIR_Z));
        if (direction.lengthSqr() < 1.0E-6D) {
            direction = new Vec3(0.0D, 0.0D, 1.0D);
        }
        return direction.normalize();
    }

    // 从渐变颜色表里随机取一组开始色和结束色。
    public int[] randomGradientColors(RandomSource random) {
        return MagicBowParticleEffects.randomGradientColors(random);
    }

    // 每 tick 更新虚拟流星头，并复用箭拖尾 helper 生成连续拖尾。
    public void tickMeteorLines(int chargeType) {
        int meteorCount = getMeteorCount(chargeType);
        if (meteorCount <= 0) return;
        ensureMeteorState(meteorCount);

        for (int i = 0; i < meteorCount; i++) {
            int startTick = getMeteorStartTick(i, chargeType);
            int localAge = this.tickCount - startTick;
            if (localAge <= 0) continue;
            if (localAge > METEOR_FALL_DURATION_TICKS && meteorLandedStates[i]) continue;

            Vec3 start = getMeteorStart(i, chargeType);
            Vec3 end = getMeteorEnd(i, chargeType);
            if (localAge <= METEOR_FALL_DURATION_TICKS) {
                Vec3 previousHead = getMeteorHeadPosition(start, end, localAge - 1);
                Vec3 currentHead = getMeteorHeadPosition(start, end, localAge);
                int[] colors = randomGradientColors(RandomSource.create(getMeteorSeed(i, chargeType)));
                spawnMeteorArrowTrail(i, chargeType, previousHead, currentHead, colors);
            }
            if (localAge >= METEOR_FALL_DURATION_TICKS && !meteorLandedStates[i]) {
                spawnMeteorLandingEffects(i, chargeType, end);
                meteorLandedStates[i] = true;
            }
        }
    }

    // 初始化客户端流星落点状态数组，避免每颗虚拟流星重复播放落点扩散。
    public void ensureMeteorState(int meteorCount) {
        if (meteorLandedStates != null && meteorLandedStates.length == meteorCount) return;
        meteorLandedStates = new boolean[meteorCount];
    }

    // 计算单颗流星开始下落的 tick，星辰裁决会把 200 颗流星铺在轰炸窗口内。
    public int getMeteorStartTick(int index, int chargeType) {
        if (chargeType == CHARGE_SUPER) {
            int finalBeamStart = Math.max(STAR_JUDGEMENT_BOMBARDMENT_START_TICKS, getStarJudgementStrikeDelayTicks() - STAR_JUDGEMENT_FINAL_BEAM_DURATION_TICKS);
            int window = Math.max(1, finalBeamStart - STAR_JUDGEMENT_BOMBARDMENT_START_TICKS - METEOR_FALL_DURATION_TICKS);
            RandomSource random = RandomSource.create(getMeteorSeed(index, chargeType) ^ 0x6C8E9CF570932BD5L);
            return STAR_JUDGEMENT_BOMBARDMENT_START_TICKS + random.nextInt(window);
        }
        return 1 + (int) ((long) index * STRONG_METEOR_SPAWN_SPREAD_TICKS / Math.max(1, getMeteorCount(chargeType)));
    }

    // 根据本地年龄计算虚拟流星头位置，使用平滑进度让坠落末端更有砸落感。
    public Vec3 getMeteorHeadPosition(Vec3 start, Vec3 end, int localAge) {
        float progress = Math.min(1.0F, Math.max(0.0F, localAge / (float) METEOR_FALL_DURATION_TICKS));
        float eased = progress * progress * (3.0F - 2.0F * progress);
        return start.lerp(end, eased);
    }

    // 用箭拖尾 helper 沿上一帧头位置到当前头位置补点。
    public void spawnMeteorArrowTrail(int index, int chargeType, Vec3 previousHead, Vec3 currentHead, int[] colors) {
        double distance = previousHead.distanceTo(currentHead);
        int points = Math.max(2, Math.min(chargeType == CHARGE_SUPER ? 9 : 7, (int) Math.ceil(distance * METEOR_TRAIL_POINTS_PER_BLOCK)));
        Vec3 trailDirection = previousHead.subtract(currentHead);
        if (trailDirection.lengthSqr() < 1.0E-6D) {
            trailDirection = new Vec3(0.0D, 1.0D, 0.0D);
        }
        for (int point = 0; point <= points; point++) {
            double progress = point / (double) points;
            RandomSource random = RandomSource.create(getMeteorSeed(index, chargeType) ^ ((long) this.tickCount * 9973L) ^ ((long) point * 7919L));
            MagicBowParticleEffects.spawnTrail(previousHead.lerp(currentHead, progress), trailDirection, chargeType, random, colors);
        }
    }

    // 虚拟流星到达落点时同时播放专用爆炸和统一扩散模板。
    public void spawnMeteorLandingEffects(int index, int chargeType, Vec3 landingPos) {
        RandomSource random = RandomSource.create(getMeteorSeed(index, chargeType) ^ 0x2B992DDFA23249D6L);
        int[] colors = randomGradientColors(random);
        int burst = chargeType == CHARGE_SUPER ? 52 : 58;
        float radius = chargeType == CHARGE_SUPER ? 2.9F : 2.65F;
        float size = chargeType == CHARGE_SUPER ? 0.09F : 0.105F;
        ParticleTemplate.emitGroundDiffusion(landingPos, radius, burst, 1.28F, size, colors[0], colors[1], random);
        spawnMeteorLandingExplosion(random, chargeType, landingPos, getMeteorFallDirection(index, chargeType));
    }

    // 根据蓄力类型读取静态流星数量。
    public int getMeteorCount(int chargeType) {
        if (chargeType == CHARGE_SUPER) return SUPER_METEOR_COUNT;
        // 强蓄力流星已关闭，保留控制器用于束缚和地面三角形。
        return 0;
    }

    // 为单颗流星生成稳定随机种子。
    public long getMeteorSeed(int index, int chargeType) {
        return this.getId() * 73471L ^ (long) chargeType * 19349663L ^ (long) index * 83492791L;
    }

    // 计算单颗流星起点，星辰裁决流星从天空法阵的内圆环上出发。
    public Vec3 getMeteorStart(int index, int chargeType) {
        RandomSource random = RandomSource.create(getMeteorSeed(index, chargeType));
        Vec3 end = getMeteorEnd(index, chargeType);
        Vec3 direction = getMeteorFallDirection(index, chargeType);
        if (chargeType == CHARGE_SUPER) {
            double startY = getStarJudgementSkyCenter().y - (1.4D + random.nextDouble() * 3.2D);
            Vec3 center = getStarJudgementVisualCenter();
            double innerRadius = getStarJudgementMeteorStartInnerRadius();
            double angle = Math.atan2(-direction.z, -direction.x) + (random.nextDouble() - 0.5D) * 0.32D;
            return new Vec3(
                    center.x + Math.cos(angle) * innerRadius,
                    startY,
                    center.z + Math.sin(angle) * innerRadius);
        }
        double pathLength = METEOR_START_HEIGHT + 8.0D + random.nextDouble() * 4.0D;
        return end.subtract(direction.scale(pathLength));
    }

    // 计算单颗流星落点，强蓄力集中目标附近，星辰裁决散布在裁决范围内。
    public Vec3 getMeteorEnd(int index, int chargeType) {
        RandomSource random = RandomSource.create(getMeteorSeed(index, chargeType) ^ 0x5DEECE66DL);
        double angle = Math.PI * 2.0D * random.nextDouble();
        double maxRadius = chargeType == CHARGE_SUPER ? getStarJudgementMeteorVisualRadius() * 0.48D : METEOR_END_RADIUS;
        double radius = Math.sqrt(random.nextDouble()) * maxRadius;
        Vec3 center = getTargetCenter();
        return center.add(Math.cos(angle) * radius, -getTargetHeight() * 0.35D, Math.sin(angle) * radius);
    }

    // 获取星辰裁决轰炸允许使用的固定视觉半径，不再受服务端伤害半径配置影响。
    public double getStarJudgementMeteorVisualRadius() {
        return STAR_JUDGEMENT_FIXED_VISUAL_RADIUS;
    }

    // 获取星辰裁决流星起点内圆半径，让起点聚集在法阵内部圆环而不是外圈。
    public double getStarJudgementMeteorStartInnerRadius() {
        return Math.max(2.2D, Math.min(7.0D, getStarJudgementMeteorVisualRadius() * 0.3D));
    }

    // 把流星高空起点水平夹在法阵范围内，保证轰炸角度从法阵覆盖区域内落下。
    public Vec3 clampHorizontalInsideStarJudgementCircle(Vec3 position, double radius) {
        Vec3 center = getTargetCenter();
        double dx = position.x - center.x;
        double dz = position.z - center.z;
        double distanceSqr = dx * dx + dz * dz;
        double maxDistance = Math.max(0.1D, radius);
        if (distanceSqr <= maxDistance * maxDistance) return position;

        double distance = Math.sqrt(distanceSqr);
        double scale = maxDistance / distance;
        return new Vec3(center.x + dx * scale, position.y, center.z + dz * scale);
    }

    // 生成多个主角度轮换的流星下落方向，避免星辰裁决始终从同一个角度坠落。
    public Vec3 getMeteorFallDirection(int index, int chargeType) {
        RandomSource random = RandomSource.create(getMeteorSeed(index, chargeType) ^ 0x51A7B0BF1234L);
        Vec3 base = chargeType == CHARGE_SUPER ? getStarJudgementMeteorBaseDirection(index) : new Vec3(-0.52D, -1.0D, 0.34D);
        Vec3 jitter = new Vec3(
                (random.nextDouble() - 0.5D) * (chargeType == CHARGE_SUPER ? 0.07D : 0.18D),
                (random.nextDouble() - 0.5D) * 0.05D,
                (random.nextDouble() - 0.5D) * (chargeType == CHARGE_SUPER ? 0.07D : 0.18D));
        return base.add(jitter).normalize();
    }

    // 星辰裁决按序号轮换 6 个主下落方向，让轰炸覆盖更多角度。
    public Vec3 getStarJudgementMeteorBaseDirection(int index) {
        return switch (Math.floorMod(index, 6)) {
            case 0 -> new Vec3(-0.38D, -1.0D, 0.24D);
            case 1 -> new Vec3(0.36D, -1.0D, -0.22D);
            case 2 -> new Vec3(-0.16D, -1.0D, -0.42D);
            case 3 -> new Vec3(0.18D, -1.0D, 0.4D);
            case 4 -> new Vec3(-0.44D, -1.0D, -0.1D);
            default -> new Vec3(0.42D, -1.0D, 0.12D);
        };
    }

    // 读取命中方向，普通方块命中缺少方向时使用轻微向前的默认方向。
    public Vec3 getEffectDirection(RandomSource random) {
        Vec3 direction = new Vec3(
                this.entityData.get(TRAIL_DIR_X),
                this.entityData.get(TRAIL_DIR_Y),
                this.entityData.get(TRAIL_DIR_Z));
        if (direction.lengthSqr() < 1.0E-6D || Math.abs(direction.y) > 0.96D) {
            direction = new Vec3(0.0D, 0.08D, 1.0D);
        }
        Vec3 jitter = new Vec3(
                (random.nextDouble() - 0.5D) * 0.05D,
                (random.nextDouble() - 0.5D) * 0.03D,
                (random.nextDouble() - 0.5D) * 0.05D);
        return direction.add(jitter).normalize();
    }

    // 生成命中爆炸粒子。
    public void spawnExplosion(RandomSource random, int chargeType) {
        int burst = getExplosionBurst(chargeType);
        if (burst <= 0) return;
        Vec3 direction = getEffectDirection(random);
        spawnImpactExplosionAt(random, chargeType, this.position().add(direction.scale(0.25D)), direction, burst);
    }

    // 生成重写后的命中爆炸，强调方向性和分层亮度。
    public void spawnImpactExplosionAt(RandomSource random, int chargeType, Vec3 center, Vec3 direction, int burst) {
        if (burst <= 0) return;
        if (direction.lengthSqr() < 1.0E-6D) {
            direction = new Vec3(0.0D, 1.0D, 0.0D);
        }
        direction = direction.normalize();
        int groups = chargeType == CHARGE_NORMAL ? 3 : 4;
        int groupBurst = Math.max(8, burst / groups);
        for (int i = 0; i < groups; i++) {
            int start = IMPACT_COLORS[Math.floorMod(i + chargeType, IMPACT_COLORS.length)];
            int end = IMPACT_COLORS[Math.floorMod(i + chargeType + 2, IMPACT_COLORS.length)];
            float progress = i / (float) Math.max(1, groups - 1);
            Exe.POST.addParticle(new ParticleEmitTask()
                    .position(center.add(direction.scale(0.12D + i * 0.16D)))
                    .direction((float) direction.x, (float) (direction.y + 0.1D + progress * 0.08D), (float) direction.z)
                    .speed(chargeType == CHARGE_NORMAL ? 1.35F + progress * 0.55F : 1.75F + progress * 0.75F)
                    .spread(chargeType == CHARGE_SUPER ? 1.05F : chargeType == CHARGE_STRONG ? 0.86F : 0.68F)
                    .life(chargeType == CHARGE_NORMAL ? 0.95F + progress * 0.24F : 1.2F + progress * 0.34F)
                    .gravity(0.03F)
                    .size(chargeType == CHARGE_SUPER ? 0.15F : chargeType == CHARGE_STRONG ? 0.12F : 0.095F,
                            chargeType == CHARGE_SUPER ? 0.15F : chargeType == CHARGE_STRONG ? 0.12F : 0.095F,
                            random.nextFloat() * 6.28F)
                    .color(start, 1.0F)
                    .endColor(end, 0.0F)
                    .randomShape(random)
                    .motion(ParticleEmitTask.MOTION_BALLISTIC)
                    .rate(0)
                    .duration(0.0F)
                    .burst(groupBurst));
        }
    }

    // 生成流星落地专用小爆炸，星辰裁决每颗流星落地都触发但粒子量更轻。
    public void spawnMeteorLandingExplosion(RandomSource random, int chargeType, Vec3 center, Vec3 direction) {
        if (direction.lengthSqr() < 1.0E-6D) {
            direction = new Vec3(0.0D, -1.0D, 0.0D);
        }
        Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 splashDirection = up.subtract(direction.normalize().scale(0.18D)).normalize();
        int groups = chargeType == CHARGE_SUPER ? 3 : 3;
        int burst = chargeType == CHARGE_SUPER ? 34 : 30;
        for (int i = 0; i < groups; i++) {
            float progress = i / (float) Math.max(1, groups - 1);
            Exe.POST.addParticle(new ParticleEmitTask()
                    .position(center.add(0.0D, 0.1D + i * 0.08D, 0.0D))
                    .direction((float) splashDirection.x, (float) (splashDirection.y + 0.12D), (float) splashDirection.z)
                    .speed(chargeType == CHARGE_SUPER ? 1.35F + progress * 0.7F : 1.35F + progress * 0.55F)
                    .spread(chargeType == CHARGE_SUPER ? 1.15F : 0.95F)
                    .life(chargeType == CHARGE_SUPER ? 1.05F : 1.05F)
                    .gravity(0.025F)
                    .size(chargeType == CHARGE_SUPER ? 0.13F : 0.12F,
                            chargeType == CHARGE_SUPER ? 0.13F : 0.12F,
                            random.nextFloat() * 6.28F)
                    .color(0xC7D2FF, 0.95F)
                    .endColor(chargeType == CHARGE_SUPER ? 0x7C4DFF : 0x32ADE6, 0.0F)
                    .randomShape(random)
                    .motion(ParticleEmitTask.MOTION_BALLISTIC)
                    .rate(0)
                    .duration(0.0F)
                    .burst(burst));
        }
    }

    // 根据蓄力类型读取静态命中爆炸数量。
    public int getExplosionBurst(int chargeType) {
        if (chargeType == CHARGE_SUPER) return SUPER_EXPLOSION_BURST;
        if (chargeType == CHARGE_STRONG) return STRONG_EXPLOSION_BURST;
        return NORMAL_EXPLOSION_BURST;
    }

    // 生成地面向外扩散的粒子环。
    public void spawnGroundRing(RandomSource random, int chargeType) {
        int burst = getGroundRingBurst(chargeType);
        if (burst <= 0) return;
        float radius = GROUND_RING_RADIUS;
        int total = Math.max(burst, (int) (burst * (chargeType == CHARGE_SUPER ? 1.8F : 1.55F)));
        int[] colors = randomGradientColors(random);
        ParticleTemplate.emitGroundDiffusion(this.position(), radius, total, GROUND_RING_DURATION_TICKS / 18.0F,
                chargeType == CHARGE_SUPER ? 0.14F : 0.11F, colors[0], colors[1], random);
    }

    // 魔法箭命中方块时播放小范围扩散。
    public void spawnArrowLandingDiffusion(RandomSource random) {
        int[] colors = randomGradientColors(random);
        ParticleTemplate.emitGroundDiffusion(this.position(), 2.25F, ARROW_LANDING_DIFFUSION_BURST, 1.15F,
                0.085F, colors[0], colors[1], random);
    }

    // 根据蓄力类型读取静态地面扩散粒子数量。
    public int getGroundRingBurst(int chargeType) {
        if (chargeType == CHARGE_SUPER) return SUPER_GROUND_RING_BURST;
        if (chargeType == CHARGE_STRONG) return STRONG_GROUND_RING_BURST;
        return 0;
    }

    // 强蓄力命中时在地面生成三角形包围目标。
    public void spawnGroundTriangle(RandomSource random, int chargeType) {
        if (chargeType == CHARGE_NORMAL) return;

        Vec3 center = getVisualCenter().add(0.0D, -1, 0.0D);
        int[] colors =  MagicBowParticleEffects.randomGradientColors(random);
        float lifeSeconds = Math.max(1.0F, this.entityData.get(SNARE_DURATION_TICKS) / 20.0F + 0.25F) * 2.0F;
        ParticleTemplate.emitTriangleFullConnect(center, 6.0f,
                colors[0], colors[1],
                0f, 0f, 0f,
                0.8f, lifeSeconds, random);


//        double radius = Math.max(getTargetRingRadius(chargeType) + 1.65D, 3.2D);
//        double rotation = -Math.PI / 2.0D;
//        Vec3[] vertices = new Vec3[3];
//
//        // 三个顶点按目标尺寸外扩，避免大型目标穿出三角形。
//        for (int i = 0; i < vertices.length; i++) {
//            double angle = rotation + Math.PI * 2.0D * i / vertices.length;
//            vertices[i] = center.add(Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius);
//        }
//
//        // 每条边都用持续发射的小粒子段拼成连续线。
//        for (int i = 0; i < vertices.length; i++) {
//            emitContinuousLine(random, vertices[i], vertices[(i + 1) % vertices.length], chargeType, 0.5F);
//        }
    }

    // 星辰裁决开场时沿冲天光束生成大小不一的扩散粒子。
    public void spawnStarJudgementOpeningDiffusions(RandomSource random) {
        Vec3 groundCenter = getStarJudgementGroundCenter();
        Vec3 skyCenter = getStarJudgementSkyCenter();
        ParticleTemplate.emitGroundDiffusion(groundCenter, 10.5F, STAR_JUDGEMENT_OPENING_DIFFUSION_BURST, 1.85F,
                0.16F, 0x8A55FF, 0x32ADE6, random);
        emitRandomBeamDiffusions(random, groundCenter, skyCenter, STAR_JUDGEMENT_BEAM_PATH_DIFFUSION_COUNT,
                1.8F, 7.2F, 42, 96, 1.35F, 0.095F, 0xA5D8FF, 0x7C4DFF);
    }

    // 星辰裁决开始时在地面生成一次两个交叉三角形，粒子寿命覆盖整个法阵阶段。
    public void spawnStarJudgementGroundTriangles(RandomSource random) {
        Vec3 center = getStarJudgementGroundCenter().add(0.0D, 0.08D, 0.0D);
        float lifeSeconds = Math.max(1.0F, getStarJudgementDurationTicks() / 20.0F + STAR_JUDGEMENT_TRIANGLE_EXTRA_SECONDS) * 2.0F;
        ParticleTemplate.emitTriangleFullConnect(center, STAR_JUDGEMENT_TRIANGLE_SIDE_LENGTH,
                0xA5D8FF, 0x7C4DFF,
                0.0F, 0.0F, 0.0F,
                0.65F, lifeSeconds, random);
        ParticleTemplate.emitTriangleFullConnect(center, STAR_JUDGEMENT_TRIANGLE_SIDE_LENGTH,
                0x7C4DFF, 0x32ADE6,
                0.0F, 60.0F, 0.0F,
                0.65F, lifeSeconds, random);
    }

    // 星辰裁决最终粗光束下落期间生成扩散，落地 tick 播放大爆炸。
    public void tickStarJudgementFinalStrike(RandomSource random) {
        int strikeDelay = getStarJudgementStrikeDelayTicks();
        int finalBeamStart = Math.max(1, strikeDelay - STAR_JUDGEMENT_FINAL_BEAM_DURATION_TICKS);
        if (this.tickCount == finalBeamStart) {
            emitRandomBeamDiffusions(random, getStarJudgementSkyCenter(), getStarJudgementGroundCenter(),
                    STAR_JUDGEMENT_BEAM_PATH_DIFFUSION_COUNT + 2, 2.0F, 8.4F,
                    48, 110, 1.25F, 0.11F, 0xE4ECFF, 0x7C4DFF);
        }
        if (this.tickCount >= finalBeamStart && this.tickCount < strikeDelay) {
            float progress = (this.tickCount - finalBeamStart + 1) / (float) STAR_JUDGEMENT_FINAL_BEAM_DURATION_TICKS;
            Vec3 center = getStarJudgementSkyCenter().lerp(getStarJudgementGroundCenter(), Math.min(1.0F, progress));
            ParticleTemplate.emitGroundDiffusion(center, 2.8F + progress * 3.5F, 72, 0.8F,
                    0.12F, 0xC7D2FF, 0x7C4DFF, random);
        }
        if (this.tickCount == strikeDelay) {
            spawnStarJudgementFinalExplosion(random);
        }
    }

    // 星辰裁决最终粗光束落地后播放超大扩散和爆炸粒子。
    public void spawnStarJudgementFinalExplosion(RandomSource random) {
        Vec3 center = getStarJudgementGroundCenter();
        ParticleTemplate.emitGroundDiffusion(center, 12.0F, STAR_JUDGEMENT_FINAL_DIFFUSION_BURST, 2.2F,
                0.18F, 0xE4ECFF, 0x7C4DFF, random);
        spawnFinalJudgementExplosionBurst(random, center.add(0.0D, 0.45D, 0.0D));
    }

    // 沿光束路径随机采样多个扩散圈，升天和落地都复用这个方法。
    public void emitRandomBeamDiffusions(RandomSource random, Vec3 start, Vec3 end, int count,
                                         float minRadius, float maxRadius, int minBurst, int maxBurst,
                                         float lifeSeconds, float size, int startRgb, int endRgb) {
        if (count <= 0) return;
        for (int i = 0; i < count; i++) {
            float baseProgress = (i + 1.0F) / (count + 1.0F);
            float progress = Math.min(0.96F, Math.max(0.04F, baseProgress + (random.nextFloat() - 0.5F) * 0.16F));
            Vec3 center = start.lerp(end, progress);
            float radiusProgress = Math.max(progress, random.nextFloat());
            float radius = minRadius + (maxRadius - minRadius) * radiusProgress;
            int burst = minBurst + random.nextInt(Math.max(1, maxBurst - minBurst + 1));
            float particleSize = size * (0.82F + random.nextFloat() * 0.42F);
            ParticleTemplate.emitGroundDiffusion(center, radius, burst, lifeSeconds * (0.85F + random.nextFloat() * 0.35F),
                    particleSize, startRgb, endRgb, random);
        }
    }

    // 星辰裁决最终落地专用大爆炸，和普通命中爆炸分开控制层次。
    public void spawnFinalJudgementExplosionBurst(RandomSource random, Vec3 center) {
        for (int i = 0; i < 5; i++) {
            float progress = i / 4.0F;
            Exe.POST.addParticle(new ParticleEmitTask()
                    .position(center.add(0.0D, i * 0.16D, 0.0D))
                    .direction(0.0F, 1.0F, 0.0F)
                    .speed(1.75F + progress * 1.3F)
                    .spread(1.35F + progress * 0.45F)
                    .life(1.45F + progress * 0.55F)
                    .gravity(0.025F)
                    .size(0.18F + progress * 0.08F, 0.18F + progress * 0.08F, random.nextFloat() * 6.28F)
                    .color(progress < 0.5F ? 0xE4ECFF : 0xA5D8FF, 1.0F)
                    .endColor(progress < 0.5F ? 0x7C4DFF : 0x32ADE6, 0.0F)
                    .randomShape(random)
                    .motion(ParticleEmitTask.MOTION_BALLISTIC)
                    .rate(0)
                    .duration(0.0F)
                    .burst(72 + i * 26));
        }
    }

    // 获取星辰裁决地面中心，目标存在时贴近脚下，否则回退到实体位置。
    public Vec3 getStarJudgementGroundCenter() {
        Entity target = getTarget();
        if (target != null) {
            return target.position().add(0.0D, 0.05D, 0.0D);
        }
        return this.position();
    }

    // 获取星辰裁决天空法阵中心，供光束扩散和最终粗光束使用。
    public Vec3 getStarJudgementSkyCenter() {
        return getStarJudgementVisualCenter().add(0.0D, STAR_JUDGEMENT_VISUAL_HEIGHT, 0.0D);
    }

    // 沿线段采样持续发射器，让粒子线段保持连续。
    public void emitContinuousLine(RandomSource random, Vec3 start, Vec3 end, int chargeType, float duration) {
        Vec3 line = end.subtract(start);
        int samples = Math.max(8, Math.min(28, (int) Math.ceil(line.length() * 2.6D)));
        int[] colors = randomGradientColors(random);
        for (int i = 0; i <= samples; i++) {
            Vec3 pos = start.lerp(end, (double) i / samples);
            Exe.POST.addParticle(new ParticleEmitTask()
                    .position(pos)
                    .direction(0.0F, 1.0F, 0.0F)
                    .speed(0.025F)
                    .spread(0.006F)
                    .life(chargeType == CHARGE_SUPER ? 1.45F : 1.15F)
                    .gravity(0.0F)
                    .size(chargeType == CHARGE_SUPER ? 0.13F : 0.105F,
                            chargeType == CHARGE_SUPER ? 0.13F : 0.105F,
                            random.nextFloat() * 6.28F)
                    .color(colors[0], 0.95F)
                    .endColor(colors[1], 0.35F)
                    .randomShape(random)
                    .motion(ParticleEmitTask.MOTION_BALLISTIC)
                    .rate(chargeType == CHARGE_SUPER ? 28 : 20)
                    .duration(duration)
                    .burst(chargeType == CHARGE_SUPER ? 4 : 2));
        }
    }

    // 使用圆周运动生成两个横向交叉的金色束缚环。
    public void spawnSnare(RandomSource random, int chargeType) {
        if (chargeType == CHARGE_NORMAL) return;
        float duration = getSnareEmitterDurationSeconds();
        if (duration <= 0.0F) return;
        float radius = getTargetRingRadius(chargeType) * 1.1F;
        float life = Math.min(chargeType == CHARGE_SUPER ? 9.5F : 7.0F, duration + SNARE_VISUAL_FADE_SECONDS);
        int rate = chargeType == CHARGE_SUPER ? 50 : 20;
        Vec3 center = getVisualCenter().add(0.0D, -getTargetHeight() * 0.1D, 0.0D);
        for (int ring = 0; ring < 2; ring++) {
            Exe.POST.addParticle(new ParticleEmitTask()
                    .position(center)
                    .direction(0.0F, 1.0F, 0.0F)
                    .speed(0.1F)
                    .spread(0.02F)
                    .life(life)
                    .gravity(0.0F)
                    .size(chargeType == CHARGE_SUPER ? 0.05F : 0.055F,
                            chargeType == CHARGE_SUPER ? 0.1F : 0.085F,
                            random.nextFloat() * 6.28F)
                    .color(0xFFD700, 1.0F)
                    .endColor(0xFFF3A0, 1.0F)
                    .randomShape(random)
                    .motion(ParticleEmitTask.MOTION_CIRCULAR)
                    .orbit(radius, ring == 0 ? 9.2F : -9.2F, 0.0F)
                    .orbitPlane(0.0F, 0.0F, (float) Math.toRadians(ring == 0 ? 38.0F : -38.0F))
                    .orbitSpawnMode(ParticleEmitTask.ORBIT_SPAWN_DISTRIBUTED)
                    .rate(rate)
                    .duration(duration));
        }
    }

    // 根据配置读取本次实际束缚时长，让客户端粒子与服务端控制使用同一个数值。
    public int getConfiguredSnareDurationTicks(int chargeType) {
        if (chargeType == CHARGE_SUPER) return Math.max(MagicBowConfig.superSnareDurationTicks(), STAR_JUDGEMENT_TOTAL_DURATION_TICKS);
        if (chargeType == CHARGE_STRONG) return MagicBowConfig.strongSnareDurationTicks();
        return 0;
    }

    // 按剩余束缚时间裁剪发射器时长，避免最后一轮束缚环明显超时。
    public float getSnareEmitterDurationSeconds() {
        int remainingTicks = this.entityData.get(SNARE_DURATION_TICKS) - this.tickCount + 1;
        if (remainingTicks <= 0) return 0.0F;
        float maxDuration = this.entityData.get(CHARGE_TYPE) == CHARGE_SUPER ? 3.2F : 2.4F;
        return Math.min(maxDuration, remainingTicks / 20.0F);
    }




    // 判断锁链是否仍在发射，用于让地面三角形持续到锁链结束。
    public boolean isChainActiveTick(int chargeType) {
        return chargeType != CHARGE_NORMAL && this.tickCount <= getChainWaveCount(chargeType) * 6;
    }

    // 判断束缚粒子是否需要刷新，保证显示时间覆盖实际束缚时间。
    public boolean isSnareVisualActiveTick(int chargeType) {
        if (chargeType == CHARGE_NORMAL || this.tickCount == 1) return false;
        return this.tickCount <= this.entityData.get(SNARE_DURATION_TICKS);
    }

    // 获取当前蓄力类型的锁链波次数。
    public int getChainWaveCount(int chargeType) {
        return chargeType == CHARGE_SUPER ? 8 : 10;
    }


    // 判断当前命中效果是否来自箭落地方块。
    public boolean isBlockImpactEffect() {
        return this.entityData.get(EFFECT_TYPE) == EFFECT_IMPACT && this.entityData.get(TARGET_ID) < 0;
    }

    // 魔法箭落地方块后，对小范围怪物造成一次配置伤害。
    public void applyArrowLandingSplashDamage() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        double radius = MagicBowConfig.arrowLandingSplashRadius();
        float damage = MagicBowConfig.arrowLandingSplashDamage();
        if (radius <= 0.0D || damage <= 0.0F) return;

        // 只查询落点附近的小范围实体，并限定为怪物，避免落地扩散误伤动物。
        Vec3 center = this.position();
        AABB area = new AABB(center, center).inflate(radius);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area, this::canDamageLandingSplash);
        for (LivingEntity target : targets) {
            target.hurt(this.damageSources().magic(), damage);
        }
    }

    // 判断当前 tick 是否处于星辰裁决轰炸期范围伤害阶段。
    public boolean isStarJudgementBombardmentDamageTick(int starJudgementDelay) {
        return this.tickCount >= STAR_JUDGEMENT_BOMBARDMENT_START_TICKS && this.tickCount < starJudgementDelay;
    }

    // 星辰裁决轰炸期每秒缓存一次范围内目标生物，并对缓存目标每 tick 造成伤害。
    public void applyStarJudgementBombardmentDamage() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        float damage = MagicBowConfig.superBombardmentTickDamage();
        if (damage <= 0.0F) return;

        // 每秒重新遍历范围，之后 20 tick 只处理缓存 id，降低频繁范围查询成本。
        if (this.tickCount == STAR_JUDGEMENT_BOMBARDMENT_START_TICKS || this.tickCount % STAR_JUDGEMENT_DAMAGE_SCAN_INTERVAL_TICKS == 0) {
            refreshStarJudgementBombardmentTargets(serverLevel);
        }
        double radius = this.entityData.get(STAR_JUDGEMENT_RADIUS);
        double radiusSqr = radius * radius;
        Vec3 center = getStarJudgementGroundCenter();
        for (Integer id : starJudgementBombardmentTargetIds) {
            Entity entity = serverLevel.getEntity(id);
            if (entity instanceof LivingEntity living && living.distanceToSqr(center) <= radiusSqr && canDamage(living)) {
                living.hurt(this.damageSources().magic(), damage);
            }
        }
    }

    // 刷新星辰裁决轰炸期的范围目标缓存。
    public void refreshStarJudgementBombardmentTargets(ServerLevel serverLevel) {
        starJudgementBombardmentTargetIds.clear();
        double radius = this.entityData.get(STAR_JUDGEMENT_RADIUS);
        if (radius <= 0.0D) return;

        Vec3 center = getStarJudgementGroundCenter();
        AABB area = new AABB(center, center).inflate(radius+2);//服务端轰炸范围
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area, this::canDamage);
        for (LivingEntity target : targets) {
            starJudgementBombardmentTargetIds.add(target.getId());
        }
    }

    // 对星辰裁决落点附近目标造成范围伤害。
    public void applyStarJudgementAreaDamage() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        Vec3 center = getTargetCenter();
        double radius = this.entityData.get(STAR_JUDGEMENT_RADIUS);
        AABB area = new AABB(center, center).inflate(radius);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area, this::canDamage);
        for (LivingEntity target : targets) {
            target.hurt(this.damageSources().magic(), this.entityData.get(STAR_JUDGEMENT_DAMAGE));
        }
    }

    // 获取当前目标实体中心，目标失效时回退到效果实体位置。
    public Vec3 getTargetCenter() {
        Entity target = getTarget();
        if (target == null) {
            return this.position();
        }
        return target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
    }

    // 服务端持续把束缚目标压回锚点，清掉箭矢和流星伤害带来的击退。
    public void lockSnaredTarget(@Nullable Entity target) {
        if (!(target instanceof LivingEntity living) || !living.isAlive()) return;
        if (this.entityData.get(CHARGE_TYPE) == CHARGE_NORMAL) return;
        if (this.tickCount > this.entityData.get(SNARE_DURATION_TICKS)) return;

        double anchorX = this.entityData.get(ANCHOR_X);
        double anchorY = this.entityData.get(ANCHOR_Y);
        double anchorZ = this.entityData.get(ANCHOR_Z);
        living.setDeltaMovement(0.0D, Math.min(0.0D, living.getDeltaMovement().y), 0.0D);
        living.teleportTo(anchorX, anchorY, anchorZ);
        living.hurtMarked = true;
    }

    // 判断当前效果实体是否需要星辰裁决 shader 法阵渲染。
    public boolean isStarJudgementVisual() {
        return this.entityData.get(EFFECT_TYPE) == EFFECT_METEOR && this.entityData.get(CHARGE_TYPE) == CHARGE_SUPER;
    }

    // 供 bloom 队列读取星辰裁决视觉中心。
    public Vec3 getStarJudgementVisualCenter() {
        return getVisualCenter();
    }

    // 供 bloom 队列读取星辰裁决目标高度。
    public float getStarJudgementTargetHeight() {
        return getTargetHeight();
    }

    public int getStarJudgementDurationTicks() {
        return this.entityData.get(DURATION_TICKS);
    }

    public int getStarJudgementStrikeDelayTicks() {
        return this.entityData.get(STAR_JUDGEMENT_STRIKE_DELAY_TICKS);
    }

    public float getStarJudgementRadius() {
        return this.entityData.get(STAR_JUDGEMENT_RADIUS);
    }

    // 供法阵渲染读取固定视觉半径，避免伤害半径配置改变法阵大小。
    public float getStarJudgementVisualRadius() {
        return STAR_JUDGEMENT_FIXED_VISUAL_RADIUS;
    }

    // 判断玩家是否仍在星辰裁决法阵可见距离内，避免 noCulling 后无限远提交。
    public boolean isStarJudgementVisualInRange(Vec3 cameraPosition) {
        if (!isStarJudgementVisual()) return false;
        double maxDistanceSqr = STAR_JUDGEMENT_VISUAL_RENDER_DISTANCE * STAR_JUDGEMENT_VISUAL_RENDER_DISTANCE;
        return getStarJudgementVisualCenter().distanceToSqr(cameraPosition) <= maxDistanceSqr;
    }

    // 客户端视觉中心优先跟随目标，目标失效时回到同步实体位置。
    public Vec3 getVisualCenter() {
        Entity target = getTarget();
        if (target == null) {
            return this.position();
        }
        return target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
    }

    // 根据目标尺寸计算束缚圆半径，让大型怪物不会穿出束缚环。
    public float getTargetRingRadius(int chargeType) {
        float width = this.entityData.get(TARGET_WIDTH);
        float height = this.entityData.get(TARGET_HEIGHT);
        float baseRadius = Math.max(width * 0.85F, height * 0.38F);
        float chargePadding = chargeType == CHARGE_SUPER ? 0.75F : 0.45F;
        return Math.max(1.15F, baseRadius + chargePadding);
    }

    // 获取同步的目标高度，目标实体可用时使用实时高度。
    public float getTargetHeight() {
        Entity target = getTarget();
        if (target != null) {
            return target.getBbHeight();
        }
        return this.entityData.get(TARGET_HEIGHT);
    }

    @Nullable
    public Entity getTarget() {
        int id = this.entityData.get(TARGET_ID);
        return id < 0 ? null : this.level().getEntity(id);
    }

    // 判断目标是否允许被魔法弓伤害。
    public boolean canDamage(Entity target) {
        if (!(target instanceof LivingEntity living) || !living.isAlive()) return false;
        if (ownerUUID != null && target.getUUID().equals(ownerUUID)) return false;
        return !EntityUtil.isInDamageWhitelist(target);
    }

    // 判断箭落地扩散是否能伤害目标，额外限制为怪物类型。
    public boolean canDamageLandingSplash(Entity target) {
        return target instanceof Enemy && canDamage(target);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
        this.starJudgementAreaDamageApplied = tag.getBoolean("StarJudgementAreaDamageApplied") || tag.getBoolean("FinalStrikeApplied");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
        tag.putBoolean("StarJudgementAreaDamageApplied", starJudgementAreaDamageApplied);
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        if (!isStarJudgementVisual()) {
            return super.getBoundingBoxForCulling();
        }
        Vec3 center = getStarJudgementVisualCenter();
        double radius = STAR_JUDGEMENT_VISUAL_CULL_RADIUS;
        double bottom = center.y - Math.max(getStarJudgementTargetHeight(), 2.0D);
        double top = center.y + STAR_JUDGEMENT_VISUAL_HEIGHT + 2.0D;
        return new AABB(center.x - radius, bottom, center.z - radius, center.x + radius, top, center.z + radius);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
