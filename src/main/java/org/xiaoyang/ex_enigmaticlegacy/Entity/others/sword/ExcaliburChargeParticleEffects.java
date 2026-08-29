package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Config.ExExcaliburConfig;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.material.ParticleMaterialKey;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModSounds;

// ExcaliburChargeParticleEffects 集中提交咖喱棒蓄力阶段的 SDF 与光效 GPU 粒子。
public class ExcaliburChargeParticleEffects {
    public static final int CONTINUOUS_REFRESH_TICKS = 2; // 短时持续发射器的刷新 tick 间隔。
    public static final float CONTINUOUS_EMITTER_DURATION = 0.32F; // 短时持续发射器的存活秒数。
    public static final double SDF_EMIT_Y_OFFSET = 0.18D; // SDF 持续粒子相对身体中心的发射高度偏移。
    public static final double LIGHT_EMIT_Y_OFFSET = 0.28D; // 光效持续粒子相对身体中心的发射高度偏移。

    public static final float BASE_CHARGE_RISE_START_SPEED = 1.00F; // 基础阶段 SDF/LIGHT_EFFECT 共用的固定起始速度。
    public static final float BASE_CHARGE_RISE_END_SPEED = 0.70F; // 基础阶段 SDF/LIGHT_EFFECT 共用的固定结束速度。
    public static final float BASE_CHARGE_RISE_SPEED_CURVE = 1.15F; // 基础阶段 SDF/LIGHT_EFFECT 共用的速度曲线指数。
    public static final float ENHANCED_CHARGE_RISE_START_SPEED = 2.20F; // 增强阶段 SDF/LIGHT_EFFECT 共用的固定起始速度。
    public static final float ENHANCED_CHARGE_RISE_END_SPEED = 1.65F; // 增强阶段 SDF/LIGHT_EFFECT 共用的固定结束速度。
    public static final float ENHANCED_CHARGE_RISE_SPEED_CURVE = 1.15F; // 增强阶段 SDF/LIGHT_EFFECT 共用的速度曲线指数。

    public static final int BASE_LARGE_LIGHT_RATE = 4; // 基础阶段玩家身边大型光效每秒发射数量。
    public static final int ENHANCED_LARGE_LIGHT_RATE = 7; // 增强阶段玩家身边大型光效每秒发射数量。
    public static final float LARGE_LIGHT_SPREAD = 1.3F; // 大型光效出生范围，弹道模式下约覆盖玩家中心正负一格。
    public static final float LARGE_LIGHT_LIFE = 3.20F; // 玩家身边大型光效生命周期秒数。
    public static final float LARGE_LIGHT_SIZE_X = 0.70F; // 玩家身边大型光效宽度。
    public static final float LARGE_LIGHT_SIZE_Y = 2.40F; // 玩家身边大型光效高度。
    public static final float LARGE_LIGHT_SPEED = 0.00F; // 玩家身边大型光效保持静止的速度。

    public static final int BASE_AMBIENT_SDF_RATE = 222; // 基础阶段周围氛围 SDF 每秒发射数量。
    public static final int ENHANCED_AMBIENT_SDF_RATE = 388; // 增强阶段周围氛围 SDF 每秒发射数量。
    public static final float AMBIENT_SDF_RADIUS = 10.00F; // 周围氛围 SDF 的水平出生圆盘半径。
    public static final float AMBIENT_SDF_HEIGHT_MIN = -0.75F; // 周围氛围 SDF 相对玩家中心的最低出生高度。
    public static final float AMBIENT_SDF_HEIGHT_MAX = 6.00F; // 周围氛围 SDF 相对玩家中心的最高出生高度。
    public static final float AMBIENT_SDF_SPEED_START = 0.12F; // 周围氛围 SDF 的缓慢起始上升速度。
    public static final float AMBIENT_SDF_SPEED_END = 0.04F; // 周围氛围 SDF 的缓慢结束上升速度。
    public static final float AMBIENT_SDF_LIFE_MIN = 6.00F; // 周围氛围 SDF 最短生命周期秒数。
    public static final float AMBIENT_SDF_LIFE_MAX = 9.00F; // 周围氛围 SDF 最长生命周期秒数。
    public static final float AMBIENT_SDF_SIZE_MIN = 0.06F; // 周围氛围 SDF 最小尺寸。
    public static final float AMBIENT_SDF_SIZE_MAX = 0.11F; // 周围氛围 SDF 最大尺寸。
    public static final float AMBIENT_SDF_NOISE_SCALE = 0.22F; // 周围氛围 SDF 的低频噪声尺度。
    public static final float AMBIENT_SDF_CURL_STRENGTH = 0.035F; // 周围氛围 SDF 的轻微横向卷曲强度。
    public static final float AMBIENT_SDF_RADIAL_EXPANSION = 0.08F; // 周围氛围 SDF 生命周期后段径向扩散强度。
    public static final float AMBIENT_SDF_NOISE_SPEED = 0.16F; // 周围氛围 SDF 的缓慢噪声推进速度。

    public static final int MAGIC_CIRCLE_ENERGY_INTERVAL_TICKS = 10; // 脚下基础能量法阵的重复生成间隔。
    public static final float MAGIC_CIRCLE_ENERGY_LIFE = 5.25F; // 脚下基础能量法阵生命周期秒数。
    public static final double MAGIC_CIRCLE_ENERGY_Y_OFFSET = 0.01D; // 脚下基础能量法阵防止地面深度冲突的高度偏移。
    public static final float BASE_MAGIC_CIRCLE_ENERGY_SIZE = 7.50F; // 基础阶段脚下能量法阵的世界空间基准直径。
    public static final float ENHANCED_MAGIC_CIRCLE_ENERGY_SIZE = 20.50F; // 增强阶段脚下能量法阵的世界空间基准直径。
    public static final float MAGIC_CIRCLE_ENERGY_SPEED = 0.00F; // 脚下基础能量法阵保持静止的运动速度。
    public static final int SHOCKWAVE_MAGIC_CIRCLE_INTERVAL_TICKS = 10; // 增强阶段冲击波法阵的独立重复生成间隔。
    public static final float SHOCKWAVE_MAGIC_CIRCLE_LIFE = 3.25F; // 增强阶段冲击波法阵的独立生命周期秒数。
    public static final double SHOCKWAVE_MAGIC_CIRCLE_Y_OFFSET = 0.02D; // 冲击波法阵相对脚下锚点的独立高度偏移。
    public static final float SHOCKWAVE_MAGIC_CIRCLE_SIZE = 50.00F; // 增强阶段冲击波法阵的世界空间基准直径。
    public static final float SHOCKWAVE_MAGIC_CIRCLE_SPEED = 0.00F; // 增强阶段冲击波法阵保持静止的运动速度。
    public static final int SHOCKWAVE_MAGIC_CIRCLE_START_COLOR = 0xFFFDF0; // 冲击波法阵起始白金色。
    public static final float SHOCKWAVE_MAGIC_CIRCLE_START_ALPHA = 0.18F; // 冲击波法阵起始透明度。
    public static final int SHOCKWAVE_MAGIC_CIRCLE_MID_COLOR = 0xFFB21A; // 冲击波法阵峰值浅金色。
    public static final float SHOCKWAVE_MAGIC_CIRCLE_MID_ALPHA = 0.42F; // 冲击波法阵峰值透明度。
    public static final float SHOCKWAVE_MAGIC_CIRCLE_MID_TIME = 0.38F; // 冲击波法阵达到峰值颜色的生命周期比例。
    public static final int SHOCKWAVE_MAGIC_CIRCLE_END_COLOR = 0x8A6A2B; // 冲击波法阵结束深金色。
    public static final float SHOCKWAVE_MAGIC_CIRCLE_END_ALPHA = 0.00F; // 冲击波法阵结束透明度。

    public static final int BASE_SDF_INNER_RATE = 20; // 基础阶段 SDF 内层每秒发射数量。
    public static final float BASE_SDF_INNER_SPREAD = 0.20F; // 基础阶段 SDF 内层扩散强度。
    public static final float BASE_SDF_INNER_LIFE_MIN = 2.60F; // 基础阶段 SDF 内层最短生命周期秒数。
    public static final float BASE_SDF_INNER_LIFE_MAX = 3.20F; // 基础阶段 SDF 内层最长生命周期秒数。
    public static final int BASE_SDF_OUTER_RATE = 40; // 基础阶段 SDF 外层每秒发射数量。
    public static final float BASE_SDF_OUTER_SPREAD = 0.55F; // 基础阶段 SDF 外层扩散强度。
    public static final float BASE_SDF_OUTER_LIFE_MIN = 2.80F; // 基础阶段 SDF 外层最短生命周期秒数。
    public static final float BASE_SDF_OUTER_LIFE_MAX = 3.40F; // 基础阶段 SDF 外层最长生命周期秒数。
    public static final float BASE_SDF_SIZE_MIN = 0.06F; // 基础阶段 SDF 最小尺寸。
    public static final float BASE_SDF_SIZE_MAX = 0.13F; // 基础阶段 SDF 最大尺寸。

    public static final int BASE_LIGHT_INNER_RATE = 25; // 基础阶段光效内层每秒发射数量。
    public static final float BASE_LIGHT_INNER_SPREAD = 0.35F; // 基础阶段光效内层扩散强度。
    public static final float BASE_LIGHT_INNER_LIFE_MIN = 8.50F; // 基础阶段光效内层最短生命周期秒数。
    public static final float BASE_LIGHT_INNER_LIFE_MAX = 10.00F; // 基础阶段光效内层最长生命周期秒数。
    public static final int BASE_LIGHT_OUTER_RATE = 58; // 基础阶段光效外层每秒发射数量。
    public static final float BASE_LIGHT_OUTER_SPREAD = 0.80F; // 基础阶段光效外层参考测试任务的扩散强度。
    public static final float BASE_LIGHT_OUTER_LIFE_MIN = 9.00F; // 基础阶段光效外层最短生命周期秒数。
    public static final float BASE_LIGHT_OUTER_LIFE_MAX = 10.55F; // 基础阶段光效外层参考测试任务的最长生命周期秒数。
    public static final float BASE_LIGHT_SIZE_X_MIN = 0.18F; // 基础阶段光效最小宽度。
    public static final float BASE_LIGHT_SIZE_X_MAX = 0.28F; // 基础阶段光效最大宽度。
    public static final float BASE_LIGHT_SIZE_Y_MIN = 0.36F; // 基础阶段光效最小高度。
    public static final float BASE_LIGHT_SIZE_Y_MAX = 0.50F; // 基础阶段光效最大高度。

    public static final int ENHANCED_SDF_INNER_RATE = 110; // 增强阶段 SDF 内层每秒发射数量。
    public static final float ENHANCED_SDF_INNER_SPREAD = 0.14F; // 增强阶段 SDF 内层扩散强度。
    public static final float ENHANCED_SDF_INNER_LIFE_MIN = 8.00F; // 增强阶段 SDF 内层最短生命周期秒数。
    public static final float ENHANCED_SDF_INNER_LIFE_MAX = 13.70F; // 增强阶段 SDF 内层最长生命周期秒数。
    public static final int ENHANCED_SDF_OUTER_RATE = 153; // 增强阶段 SDF 外层每秒发射数量。
    public static final float ENHANCED_SDF_OUTER_SPREAD = 0.78F; // 增强阶段 SDF 外层扩散强度。
    public static final float ENHANCED_SDF_OUTER_LIFE_MIN = 8.20F; // 增强阶段 SDF 外层最短生命周期秒数。
    public static final float ENHANCED_SDF_OUTER_LIFE_MAX = 13.90F; // 增强阶段 SDF 外层最长生命周期秒数。
    public static final float ENHANCED_SDF_SIZE_MIN = 0.10F; // 增强阶段 SDF 最小尺寸。
    public static final float ENHANCED_SDF_SIZE_MAX = 0.22F; // 增强阶段 SDF 最大尺寸。

    public static final int ENHANCED_LIGHT_INNER_RATE = 215; // 增强阶段光效内层每秒发射数量。
    public static final float ENHANCED_LIGHT_INNER_SPREAD = 0.32F; // 增强阶段光效内层扩散强度。
    public static final float ENHANCED_LIGHT_INNER_LIFE_MIN = 10.20F; // 增强阶段光效内层最短生命周期秒数。
    public static final float ENHANCED_LIGHT_INNER_LIFE_MAX = 15.90F; // 增强阶段光效内层最长生命周期秒数。
    public static final int ENHANCED_LIGHT_OUTER_RATE = 388; // 增强阶段光效外层每秒发射数量。
    public static final float ENHANCED_LIGHT_OUTER_SPREAD = 0.9F; // 增强阶段光效外层扩散强度。
    public static final float ENHANCED_LIGHT_OUTER_LIFE_MIN = 8.40F; // 增强阶段光效外层最短生命周期秒数。
    public static final float ENHANCED_LIGHT_OUTER_LIFE_MAX = 13.10F; // 增强阶段光效外层最长生命周期秒数。
    public static final float ENHANCED_LIGHT_SIZE_X_MIN = 0.22F; // 增强阶段光效最小宽度。
    public static final float ENHANCED_LIGHT_SIZE_X_MAX = 0.38F; // 增强阶段光效最大宽度。
    public static final float ENHANCED_LIGHT_SIZE_Y_MIN = 0.38F; // 增强阶段光效最小高度。
    public static final float ENHANCED_LIGHT_SIZE_Y_MAX = 0.68F; // 增强阶段光效最大高度。

    public static final int TEN_TICK_SDF_BURST_COUNT = 140; // 第 10 tick 的 SDF 爆发数量。
    public static final int TEN_TICK_LIGHT_BURST_COUNT = 256; // 第 10 tick 的光效爆发数量。
    public static final float TEN_TICK_SDF_BURST_SPEED = 7.50F; // 第 10 tick 的 SDF 径向爆发起始速度。
    public static final float TEN_TICK_SDF_BURST_END_SPEED = 4.20F; // 第 10 tick 的 SDF 径向爆发结束速度。
    public static final float TEN_TICK_SDF_BURST_LIFE_MIN = 1.20F; // 第 10 tick 的 SDF 爆发最短生命周期秒数。
    public static final float TEN_TICK_SDF_BURST_LIFE_MAX = 5.80F; // 第 10 tick 的 SDF 爆发最长生命周期秒数。
    public static final float TEN_TICK_LIGHT_BURST_SPEED = 5.80F; // 第 10 tick 的光效径向爆发起始速度。
    public static final float TEN_TICK_LIGHT_BURST_END_SPEED = 3.20F; // 第 10 tick 的光效径向爆发结束速度。
    public static final float TEN_TICK_LIGHT_BURST_LIFE_MIN = 3.30F; // 第 10 tick 的光效爆发最短生命周期秒数。
    public static final float TEN_TICK_LIGHT_BURST_LIFE_MAX = 8.00F; // 第 10 tick 的光效爆发最长生命周期秒数。

    // 每客户端 tick 按当前蓄力阶段提交两种材质，并在玩家脚下补一次双层爆发。
    public static void emitChargeParticles(ExcaliburChargeEntity entity, Vec3 anchor, Vec3 footAnchor) {
        if (Exe.POST == null || entity == null || anchor == null || entity.isReleased()) return;
        int age = entity.getChargeAge();
        if (entity.clientLastParticleTick == age) return;
        entity.clientLastParticleTick = age;

        // 使用实体固定种子和当前 tick 生成可复现随机参数，避免粒子密度随渲染帧率变化。
        long tickSeed = entity.getVisualSeed() * 31L + age * 0x9E3779B9L;
        RandomSource random = RandomSource.create(tickSeed);
        boolean enhanced = age >= getEnhancedStartTick();
        playEnhancedChargeSoundIfNeeded(entity, anchor, enhanced);

        // 从首个有效蓄力 tick 开始按配置间隔重复生成法阵，基础和增强阶段分别选择基准直径。
        if (age % MAGIC_CIRCLE_ENERGY_INTERVAL_TICKS == 1) {
            Vec3 magicCircleAnchor = footAnchor == null ? anchor.add(0.0D, -0.90D, 0.0D) : footAnchor;
            emitMagicCircleEnergy(magicCircleAnchor, enhanced);
        }

        // 冲击波法阵只在增强阶段按独立间隔生成，后续调参不会影响基础能量法阵。
        if (enhanced && age % SHOCKWAVE_MAGIC_CIRCLE_INTERVAL_TICKS == 1) {
            Vec3 shockwaveCircleAnchor = footAnchor == null ? anchor.add(0.0D, -0.90D, 0.0D) : footAnchor;
            emitShockwaveMagicCircle(shockwaveCircleAnchor);
        }

        // 跨过第 10 tick 后只在脚下执行一次大爆发，客户端首次稍晚看到实体时也不会永久漏掉。
        if (enhanced && !entity.clientTenTickBurstPlayed) {
            Vec3 burstAnchor = footAnchor == null ? anchor.add(0.0D, -0.90D, 0.0D) : footAnchor;
            emitTenTickBurst(burstAnchor, random);
            entity.clientTenTickBurstPlayed = true;
        }

        // 每两 tick 刷新四个略有重叠的短时发射器，松开蓄力后能快速停止且可跟随玩家位置。
        if (age % CONTINUOUS_REFRESH_TICKS == 0) {
            emitSdfContinuous(anchor, random, enhanced);
            emitLightContinuous(anchor, random, enhanced);
        }
    }

    public static int getEnhancedStartTick() {
        return ExExcaliburConfig.enhancedStartTick();
    }

    // 增强阶段首次出现时播放一次 ex.ogg，远端玩家会随同步实体各自在客户端播放。
    public static void playEnhancedChargeSoundIfNeeded(ExcaliburChargeEntity entity, Vec3 anchor, boolean enhanced) {
        if (entity == null || anchor == null || !enhanced || entity.clientEnhancedSoundPlayed) return;
        entity.clientEnhancedSoundPlayed = true;
        entity.level().playLocalSound(anchor.x, anchor.y, anchor.z,
                ModSounds.AAAAA.get(), SoundSource.PLAYERS, 1.2F, 1.0F, false);
    }

    // 提交 SDF 内外两层弹道发射器，外层使用更大的 spread 形成倒锥轮廓。
    public static void emitSdfContinuous(Vec3 anchor, RandomSource random, boolean enhanced) {
        int innerRate = enhanced ? ENHANCED_SDF_INNER_RATE : BASE_SDF_INNER_RATE;
        float innerSpread = enhanced ? ENHANCED_SDF_INNER_SPREAD : BASE_SDF_INNER_SPREAD;
        float innerLifeMin = enhanced ? ENHANCED_SDF_INNER_LIFE_MIN : BASE_SDF_INNER_LIFE_MIN;
        float innerLifeMax = enhanced ? ENHANCED_SDF_INNER_LIFE_MAX : BASE_SDF_INNER_LIFE_MAX;
        int outerRate = enhanced ? ENHANCED_SDF_OUTER_RATE : BASE_SDF_OUTER_RATE;
        float outerSpread = enhanced ? ENHANCED_SDF_OUTER_SPREAD : BASE_SDF_OUTER_SPREAD;
        float outerLifeMin = enhanced ? ENHANCED_SDF_OUTER_LIFE_MIN : BASE_SDF_OUTER_LIFE_MIN;
        float outerLifeMax = enhanced ? ENHANCED_SDF_OUTER_LIFE_MAX : BASE_SDF_OUTER_LIFE_MAX;

        // 内层收束中心密度，外层随上升距离扩大水平位移，远处小粒子补充环境氛围。
        emitSdfGroup(anchor, random, enhanced, innerRate, innerSpread, innerLifeMin, innerLifeMax);
        emitSdfGroup(anchor, random, enhanced, outerRate, outerSpread, outerLifeMin, outerLifeMax);
        emitAmbientSdfAroundPlayer(anchor, random, enhanced);
    }

    // 提交 LIGHT_EFFECT 内外两层弹道发射器，填充中心并强化倒锥边缘 Bloom。
    public static void emitLightContinuous(Vec3 anchor, RandomSource random, boolean enhanced) {
        int innerRate = enhanced ? ENHANCED_LIGHT_INNER_RATE : BASE_LIGHT_INNER_RATE;
        float innerSpread = enhanced ? ENHANCED_LIGHT_INNER_SPREAD : BASE_LIGHT_INNER_SPREAD;
        float innerLifeMin = enhanced ? ENHANCED_LIGHT_INNER_LIFE_MIN : BASE_LIGHT_INNER_LIFE_MIN;
        float innerLifeMax = enhanced ? ENHANCED_LIGHT_INNER_LIFE_MAX : BASE_LIGHT_INNER_LIFE_MAX;
        int outerRate = enhanced ? ENHANCED_LIGHT_OUTER_RATE : BASE_LIGHT_OUTER_RATE;
        float outerSpread = enhanced ? ENHANCED_LIGHT_OUTER_SPREAD : BASE_LIGHT_OUTER_SPREAD;
        float outerLifeMin = enhanced ? ENHANCED_LIGHT_OUTER_LIFE_MIN : BASE_LIGHT_OUTER_LIFE_MIN;
        float outerLifeMax = enhanced ? ENHANCED_LIGHT_OUTER_LIFE_MAX : BASE_LIGHT_OUTER_LIFE_MAX;

        // 光效拆成收束内层和扩散外层，并补充一组围绕玩家的静止大型光效。
        emitLightGroup(anchor, random, enhanced, innerRate, innerSpread, innerLifeMin, innerLifeMax);
        emitLightGroup(anchor, random, enhanced, outerRate, outerSpread, outerLifeMin, outerLifeMax);
        emitLargeLightAroundPlayer(anchor, enhanced);
    }

    // 创建一组持续发射的 SDF 弹道粒子，基础/增强阶段与 LIGHT_EFFECT 共用固定速度。
    public static void emitSdfGroup(Vec3 anchor, RandomSource random, boolean enhanced, int rate, float spread,
                                    float lifeMin, float lifeMax) {
        float life = randomRange(random, lifeMin, lifeMax);
        float startSpeed = enhanced ? ENHANCED_CHARGE_RISE_START_SPEED : BASE_CHARGE_RISE_START_SPEED;
        float endSpeed = enhanced ? ENHANCED_CHARGE_RISE_END_SPEED : BASE_CHARGE_RISE_END_SPEED;
        float speedCurve = enhanced ? ENHANCED_CHARGE_RISE_SPEED_CURVE : BASE_CHARGE_RISE_SPEED_CURVE;
        float size = randomRange(random, enhanced ? ENHANCED_SDF_SIZE_MIN : BASE_SDF_SIZE_MIN,
                enhanced ? ENHANCED_SDF_SIZE_MAX : BASE_SDF_SIZE_MAX);

        // 两种持续粒子使用同一阶段速度，只有 spread、生命周期和尺寸分组不同。
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(anchor.add(0.0D, SDF_EMIT_Y_OFFSET, 0.0D))
                .direction(0.0F, 1.0F, 0.0F)
                .speed(startSpeed, endSpeed)
                .speedCurve(speedCurve)
                .spread(spread)
                .life(life)
                .gravity(0.0F)
                .size(size, size, random.nextFloat() * 6.283185F)
                .color(0xFFF4B0, enhanced ? 1.0F : 0.86F)
                .midColor(0xFFC247, enhanced ? 0.96F : 0.78F)
                .midColorTime(0.42F)
                .endColor(0x8A2E00, 0.0F)
                .randomShape(random)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(rate)
                .duration(CONTINUOUS_EMITTER_DURATION));
    }

    // 在玩家周围约十格圆盘内持续生成缓慢上升的小型 SDF 氛围粒子。
    public static void emitAmbientSdfAroundPlayer(Vec3 anchor, RandomSource random, boolean enhanced) {
        int rate = enhanced ? ENHANCED_AMBIENT_SDF_RATE : BASE_AMBIENT_SDF_RATE;
        float life = randomRange(random, AMBIENT_SDF_LIFE_MIN, AMBIENT_SDF_LIFE_MAX);
        float size = randomRange(random, AMBIENT_SDF_SIZE_MIN, AMBIENT_SDF_SIZE_MAX);

        // turbulentRise 使用水平圆盘控制出生范围，低速和低 curl 让粒子只做轻微漂浮上升。
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(anchor)
                .direction(0.0F, 1.0F, 0.0F)
                .speed(AMBIENT_SDF_SPEED_START, AMBIENT_SDF_SPEED_END)
                .speedCurve(1.10F)
                .turbulentRise(AMBIENT_SDF_RADIUS, AMBIENT_SDF_NOISE_SCALE, AMBIENT_SDF_CURL_STRENGTH,
                        AMBIENT_SDF_RADIAL_EXPANSION, AMBIENT_SDF_NOISE_SPEED)
                .turbulentSpawnHeight(AMBIENT_SDF_HEIGHT_MIN, AMBIENT_SDF_HEIGHT_MAX)
                .life(life)
                .gravity(0.0F)
                .size(size, size, random.nextFloat() * 6.283185F)
                .color(0xFFF4B0, enhanced ? 0.24F : 0.16F)
                .midColor(0xFFD05A, enhanced ? 0.66F : 0.48F)
                .midColorTime(0.48F)
                .endColor(0x8A2E00, 0.0F)
                .randomShape(random)
                .rate(rate)
                .duration(CONTINUOUS_EMITTER_DURATION));
    }

    // 创建一组持续发射的 LIGHT_EFFECT 弹道粒子，与 SDF 共用当前蓄力阶段固定速度。
    public static void emitLightGroup(Vec3 anchor, RandomSource random, boolean enhanced, int rate, float spread,
                                      float lifeMin, float lifeMax) {
        float life = randomRange(random, lifeMin, lifeMax);
        float startSpeed = enhanced ? ENHANCED_CHARGE_RISE_START_SPEED : BASE_CHARGE_RISE_START_SPEED;
        float endSpeed = enhanced ? ENHANCED_CHARGE_RISE_END_SPEED : BASE_CHARGE_RISE_END_SPEED;
        float speedCurve = enhanced ? ENHANCED_CHARGE_RISE_SPEED_CURVE : BASE_CHARGE_RISE_SPEED_CURVE;
        float sizeX = randomRange(random, enhanced ? ENHANCED_LIGHT_SIZE_X_MIN : BASE_LIGHT_SIZE_X_MIN,
                enhanced ? ENHANCED_LIGHT_SIZE_X_MAX : BASE_LIGHT_SIZE_X_MAX);
        float sizeY = randomRange(random, enhanced ? ENHANCED_LIGHT_SIZE_Y_MIN : BASE_LIGHT_SIZE_Y_MIN,
                enhanced ? ENHANCED_LIGHT_SIZE_Y_MAX : BASE_LIGHT_SIZE_Y_MAX);

        // LIGHT_EFFECT 与 SDF 使用同一阶段速度，材质差异只来自尺寸、颜色和发射率。
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(anchor.add(0.0D, LIGHT_EMIT_Y_OFFSET, 0.0D))
                .direction(0.0F, 1.0F, 0.0F)
                .speed(startSpeed, endSpeed)
                .speedCurve(speedCurve)
                .spread(spread)
                .life(life)
                .gravity(0.0F)
                .size(sizeX, sizeY, random.nextFloat() * 6.283185F)
                .color(enhanced ? 0xFFF8C7 : 0xFFF4A8, enhanced ? 0.18F : 0.10F)
                .midColor(0xFFB000, enhanced ? 0.94F : 0.82F)
                .midColorTime(enhanced ? 0.38F : 0.50F)
                .endColor(0x5A0800, 0.0F)
                .material(ParticleMaterialKey.LIGHT_EFFECT)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(rate)
                .duration(CONTINUOUS_EMITTER_DURATION));
    }

    // 在玩家身体周围持续生成少量静止的大型 LIGHT_EFFECT，强化近身蓄力亮度。
    public static void emitLargeLightAroundPlayer(Vec3 anchor, boolean enhanced) {
        int rate = enhanced ? ENHANCED_LARGE_LIGHT_RATE : BASE_LARGE_LIGHT_RATE;

        // 速度保持为零，仅使用 spread 随机分布出生位置，避免大型光效离开玩家周围。
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(anchor)
                .direction(0.0F, 1.0F, 0.0F)
                .speed(LARGE_LIGHT_SPEED, LARGE_LIGHT_SPEED)
                .speedCurve(1.0F)
                .spread(LARGE_LIGHT_SPREAD)
                .life(LARGE_LIGHT_LIFE)
                .gravity(0.0F)
                .size(LARGE_LIGHT_SIZE_X, LARGE_LIGHT_SIZE_Y, 0.0F)
                .fixedRotation(0.0F)
                .color(enhanced ? 0xFFF8C7 : 0xFFF4A8, enhanced ? 0.18F : 0.10F)
                .midColor(0xFFB000, enhanced ? 0.34F : 0.32F)
                .midColorTime(enhanced ? 0.38F : 0.50F)
                .endColor(0x5A0800, 0.0F)
                .material(ParticleMaterialKey.LIGHT_EFFECT)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(rate)
                .duration(CONTINUOUS_EMITTER_DURATION));
    }

    // 在玩家脚下生成单个静止的水平基础能量法阵，纹理扩散完全交给材质 Shader 驱动。
    public static void emitMagicCircleEnergy(Vec3 footAnchor, boolean enhanced) {
        float circleSize = enhanced ? ENHANCED_MAGIC_CIRCLE_ENERGY_SIZE : BASE_MAGIC_CIRCLE_ENERGY_SIZE;
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(footAnchor.add(0.0D, MAGIC_CIRCLE_ENERGY_Y_OFFSET, 0.0D))
                .direction(0.0F, 1.0F, 0.0F)
                .speed(MAGIC_CIRCLE_ENERGY_SPEED, MAGIC_CIRCLE_ENERGY_SPEED)
                .spread(0.0F)
                .life(MAGIC_CIRCLE_ENERGY_LIFE)
                .gravity(0.0F)
                .size(circleSize, circleSize, 0.0F)
                .color(enhanced ? 0xFFF7C4 : 0xFFE9A0, 0.18F)
                .midColor(0xFFB21A, enhanced ? 0.42F : 0.32F)
                .midColorTime(0.38F)
                .endColor(0x7A1900, 0.0F)
                .material(ParticleMaterialKey.MAGIC_CIRCLE_ENERGY)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(0)
                .duration(0.0F)
                .burst(1));
    }

    // 提交使用独立尺寸、生命周期和白金色参数的增强阶段冲击波法阵。
    public static void emitShockwaveMagicCircle(Vec3 footAnchor) {
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(footAnchor.add(0.0D, SHOCKWAVE_MAGIC_CIRCLE_Y_OFFSET, 0.0D))
                .direction(0.0F, 1.0F, 0.0F)
                .speed(SHOCKWAVE_MAGIC_CIRCLE_SPEED, SHOCKWAVE_MAGIC_CIRCLE_SPEED)
                .spread(0.0F)
                .life(SHOCKWAVE_MAGIC_CIRCLE_LIFE)
                .gravity(0.0F)
                .size(SHOCKWAVE_MAGIC_CIRCLE_SIZE, SHOCKWAVE_MAGIC_CIRCLE_SIZE, 0.0F)
                .color(SHOCKWAVE_MAGIC_CIRCLE_START_COLOR, SHOCKWAVE_MAGIC_CIRCLE_START_ALPHA)
                .midColor(SHOCKWAVE_MAGIC_CIRCLE_MID_COLOR, SHOCKWAVE_MAGIC_CIRCLE_MID_ALPHA)
                .midColorTime(SHOCKWAVE_MAGIC_CIRCLE_MID_TIME)
                .endColor(SHOCKWAVE_MAGIC_CIRCLE_END_COLOR, SHOCKWAVE_MAGIC_CIRCLE_END_ALPHA)
                .material(ParticleMaterialKey.SHOCKWAVE_MAGIC_CIRCLE)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(0)
                .duration(0.0F)
                .burst(1));
    }

    // 在增强阶段入口从玩家脚下同时提交 SDF 碎片和 LIGHT_EFFECT 光团的大范围径向爆发。
    public static void emitTenTickBurst(Vec3 footAnchor, RandomSource random) {
        emitSdfBurst(footAnchor, random);
        emitLightBurst(footAnchor.add(0.0D, 0.04D, 0.0D), random);
    }

    // 提交第 10 tick 外层 SDF 径向碎片。
    public static void emitSdfBurst(Vec3 anchor, RandomSource random) {
        float life = randomRange(random, TEN_TICK_SDF_BURST_LIFE_MIN, TEN_TICK_SDF_BURST_LIFE_MAX);
        float size = randomRange(random, 0.12F, 0.28F);
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(anchor)
                .direction(0.0F, 1.0F, 0.0F)
                .speed(TEN_TICK_SDF_BURST_SPEED, TEN_TICK_SDF_BURST_END_SPEED)
                .speedCurve(1.25F)
                .spread(0.72F)
                .life(life)
                .gravity(0.18F)
                .size(size, size, random.nextFloat() * 6.283185F)
                .color(0xFFFDF0, 1.0F)
                .midColor(0xFFD05A, 0.96F)
                .midColorTime(0.28F)
                .endColor(0x9A3100, 0.0F)
                .randomShape(random)
                .motion(ParticleEmitTask.MOTION_RADIAL_DIFFUSION)
                .radialDiffusion(0.42F, 0.08F, 0.10F)
                .rate(0)
                .duration(0.0F)
                .burst(TEN_TICK_SDF_BURST_COUNT));
    }

    // 提交第 10 tick 内层 LIGHT_EFFECT 径向光团。
    public static void emitLightBurst(Vec3 anchor, RandomSource random) {
        float life = randomRange(random, TEN_TICK_LIGHT_BURST_LIFE_MIN, TEN_TICK_LIGHT_BURST_LIFE_MAX);
        float sizeX = randomRange(random, 0.28F, 0.52F);
        float sizeY = randomRange(random, 0.50F, 0.92F);
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(anchor)
                .direction(0.0F, 1.0F, 0.0F)
                .speed(TEN_TICK_LIGHT_BURST_SPEED, TEN_TICK_LIGHT_BURST_END_SPEED)
                .speedCurve(1.25F)
                .spread(0.88F)
                .life(life)
                .gravity(0.12F)
                .size(sizeX, sizeY, random.nextFloat() * 6.283185F)
                .color(0xFFFFFF, 0.88F)
                .midColor(0xFFB817, 1.0F)
                .midColorTime(0.30F)
                .endColor(0x6B0900, 0.0F)
                .material(ParticleMaterialKey.LIGHT_EFFECT)
                .motion(ParticleEmitTask.MOTION_RADIAL_DIFFUSION)
                .radialDiffusion(0.34F, 0.12F, 0.12F)
                .rate(0)
                .duration(0.0F)
                .burst(TEN_TICK_LIGHT_BURST_COUNT));
    }

    // 在指定范围内生成单个任务使用的稳定随机参数。
    public static float randomRange(RandomSource random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

}
