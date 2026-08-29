package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.material.ParticleMaterialKey;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModSounds;

import java.util.function.Consumer;

// ExcaliburSwordWaveEffects 集中保存 EX 咖喱棒剑气的推进、路线和 GPU 粒子参数。
public final class ExcaliburSwordWaveEffects {
    public static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D); // 世界上方向。
    public static final Vec3 WORLD_FORWARD = new Vec3(0.0D, 0.0D, 1.0D); // 水平发射方向回退值。
    public static final Vec3 WORLD_SIDE = new Vec3(-1.0D, 0.0D, 0.0D); // 剑气侧面法线回退值。

    // 基础推进参数。
    public static final double FORWARD_SPEED = 6.0D; // 服务端剑气伤害前沿每 tick 前进距离。
    public static final double HIT_RADIUS = 2.0D; // 旧路线扫掠命中半径保留参数，当前锥形伤害不再读取。
    public static final double SPAWN_FORWARD_OFFSET = 0.0D; // 释放起点相对玩家眼睛的前移距离。
    public static final int EMIT_INTERVAL_TICKS = 1; // 客户端粒子发射间隔。

    // EX 剑气路线参数。
    public static final double LANE_SPACING = 2.0D; // 服务端动态多路之间的目标间距。
    public static final int MAX_LANE_COUNT = 51; // 极端分叉配置下的最大奇数路数。
    public static final int EX_WAVE_VISUAL_ADVANCE_TICKS = 0; // EX 剑气视觉相对伤害前沿提前生成的 tick 数。
    public static final int EX_WAVE_VISUAL_TRAVEL_TICKS = 20; // EX 剑气视觉从起点到终点的 tick 数，速度由最终距离动态反推。
    public static final float EX_WAVE_VISUAL_DISTANCE_POWER = 1.0F; // EX 剑气视觉距离进度曲线，1 为线性。
    public static final int EX_WAVE_DAMAGE_TRAVEL_TICKS = 14; // 服务端锥形伤害从起点推进到最大射程的 tick 数。
    public static final float EX_WAVE_DAMAGE_DISTANCE_POWER = 1.0F; // 服务端锥形伤害距离进度曲线，1 为线性。
    public static final int EX_WAVE_DAMAGE_PATH_KEEP_TICKS = 12; // 旧锥形路径持续保留伤害 tick 数，<=0 表示全路径保留。
    public static final double EX_WAVE_VISUAL_LANE_SPACING = LANE_SPACING; // EX 剑气视觉路线间隔。
    public static final int EX_WAVE_VISUAL_MAX_LANE_COUNT = MAX_LANE_COUNT; // EX 剑气视觉最大路线数量。
    public static final double EX_WAVE_VISUAL_SIDE_SIGN = -1.0D; // EX 剑气视觉左右方向修正符号。

    // EX 剑气主粒子参数。
    public static final double PARTICLE_BASE_Y_OFFSET = -1.45D; // 粒子底边相对眼睛高度路线的视觉偏移。
    public static final float PARTICLE_LIFE_SECONDS = 2.8F; // 单个 EX 剑气粒子生命周期基准值。
    public static final int PARTICLE_TAIL_TICKS = 16; // 到达最大射程后等待末批粒子淡出的 tick 数。
    public static final float START_SIZE_X = 5.75F; // 出生阶段宽度基准值。
    public static final float START_SIZE_Y = 6.0F; // 出生阶段高度基准值。
    public static final float MID_SIZE_X = 10.0F; // 快速成长阶段宽度基准值。
    public static final float MID_SIZE_Y = 15.0F; // 快速成长阶段高度基准值。
    public static final float END_SIZE_X = 8.0F; // 结束阶段宽度基准值。
    public static final float END_SIZE_Y = 14.0F; // 结束阶段高度基准值。
    public static final float MID_SIZE_TIME = 0.25F; // 生命周期中间尺寸时间点基准值。
    public static final float EX_WAVE_PATH_START_SCALE = 0.55F; // 起点粒子整体尺寸倍率。
    public static final float EX_WAVE_PATH_END_SCALE = 1.55F; // 终点粒子整体尺寸倍率。
    public static final float EX_WAVE_PATH_SCALE_POWER = 1.15F; // 路径尺寸增长曲线。
    public static final double EX_WAVE_START_FILL_DISTANCE = 12.0D; // 起点补粒子生效的前段距离。
    public static final int EX_WAVE_START_FILL_COUNT = 2; // 起点每条视觉路线额外补充的粒子数量。
    public static final double EX_WAVE_START_FILL_BACK_STEP = 1.2D; // 起点补粒子动态分布时的最小回退距离兜底。
    public static final int EX_WAVE_START_EXTRA_FILL_TICKS = 2; // 起点前几个视觉 tick 使用额外补粒子。
    public static final int EX_WAVE_START_EXTRA_FILL_COUNT = 2; // 起点前段每条视觉路线额外增加的补粒子数量。
    public static final float EX_WAVE_START_FILL_SCALE_MIN = 0.70F; // 起点补粒子的最小额外尺寸倍率。
    public static final float EX_WAVE_START_FILL_SCALE_MAX = 1.05F; // 起点补粒子的最大额外尺寸倍率。
    public static final double EX_WAVE_PATH_FILL_SPACING = 4.0D; // 沿前进方向补点目标间距，减少长射程中段间隙。
    public static final int EX_WAVE_PATH_FILL_MAX_COUNT = 2; // 每 tick 每条视觉路线最多补点数量，避免粒子任务暴涨。
    public static final float EX_WAVE_PATH_FILL_SCALE_MIN = 0.90F; // 路径补点粒子的最小额外尺寸倍率。
    public static final float EX_WAVE_PATH_FILL_SCALE_MAX = 1.05F; // 路径补点粒子的最大额外尺寸倍率。
    public static final int EX_WAVE_PATH_FILL_START_AGE = 2; // 从第几批视觉剑气开始启用中段补点。
    public static final float MAX_RANDOM_YAW_DEGREES = 12.0F; // 侧面法线最大随机水平偏转角。
    public static final float MAX_RANDOM_ROTATION_DEGREES = 6.0F; // 剑气平面内最大随机旋转角。
    public static final int PARTICLE_COLOR = 0xFF9E1A; // 对齐咖喱棒螺旋核心金色，Shader 再生成高亮层。
    public static final float START_ALPHA = 0.5F; // 出生透明度基准值。
    public static final float MID_ALPHA = 0.8F; // 中段透明度基准值。
    public static final float END_ALPHA = 0.3F; // 结束透明度。
    public static final double MAIN_FORWARD_JITTER = 0.35D; // 主剑气沿发射方向的最大位置扰动。
    public static final double MAIN_SIDE_JITTER = 0.45D; // 主剑气沿路线侧轴的最大位置扰动。
    public static final double MAIN_Y_JITTER_MIN = -0.18D; // 主剑气底边最小垂直扰动。
    public static final double MAIN_Y_JITTER_MAX = 0.22D; // 主剑气底边最大垂直扰动。
    public static final float MAIN_LIFE_SCALE_MIN = 0.88F; // 主剑气生命周期最小倍率。
    public static final float MAIN_LIFE_SCALE_MAX = 1.12F; // 主剑气生命周期最大倍率。
    public static final float MAIN_COLOR_SCALE_MIN = 0.90F; // 主剑气颜色最小亮度倍率。
    public static final float MAIN_COLOR_SCALE_MAX = 1.08F; // 主剑气颜色最大亮度倍率。

    // EX 剑气配套 LIGHT_EFFECT 参数。
    public static final double EX_WAVE_LIGHT_Y_OFFSET = 2.0D; // 配套 LIGHT_EFFECT 相对主剑气底边的上移距离。
    public static final float LIGHT_START_ALPHA_SCALE_MIN = 0.30F; // 配套光效出生 Alpha 最小倍率。
    public static final float LIGHT_START_ALPHA_SCALE_MAX = 0.45F; // 配套光效出生 Alpha 最大倍率。
    public static final float LIGHT_MID_ALPHA_SCALE_MIN = 0.25F; // 配套光效中段 Alpha 最小倍率。
    public static final float LIGHT_MID_ALPHA_SCALE_MAX = 0.40F; // 配套光效中段 Alpha 最大倍率。
    public static final float LIGHT_MASK_RADIUS = 0.18F; // 配套光效圆形遮罩半径。
    public static final float LIGHT_MASK_SOFTNESS = 0.16F; // 配套光效圆形遮罩柔边。

    // EX 剑气后向 SDF 细节参数。
    public static final float DETAIL_NONE_THRESHOLD = 0.15F; // 不生成后向 SDF 的概率阈值。
    public static final float DETAIL_ONE_THRESHOLD = 0.75F; // 只生成一个后向 SDF 的累计概率阈值。
    public static final float DETAIL_RANDOM_ROTATION_DEGREES = 18.0F; // 后向 SDF 碎片平面内随机旋转角。
    public static final int[] DETAIL_SHAPES = { // 后向细节允许使用的 SDF 碎片形状。
            ParticleEmitTask.SHAPE_TRIANGLE,
            ParticleEmitTask.SHAPE_SQUARE,
            ParticleEmitTask.SHAPE_STAR
    };

    // 能量光柱主参数。
    public static final int LIGHT_COLUMN_SLASH_TICKS = 15; // EX 剑气生成前的定向能量光柱劈下 tick 数。
    public static final int EX_WAVE_START_TICKS = 4; // 能量光柱落地后开始生成 EX 剑气逐点位粒子的 tick。
    public static final int LIGHT_COLUMN_HOLD_TICKS = 10; // 能量光柱到达玩家视野方向后的保留 tick 数。
    public static final int LIGHT_COLUMN_FADE_TICKS = 10; // 能量光柱保留结束后的淡出 tick 数。
    public static final int LIGHT_COLUMN_GRACE_TICKS = 10; // 能量光柱额外生命周期缓冲，避免 EX 剑气出现前产生空档。
    public static final int LIGHT_COLUMN_OPENING_TICKS = LIGHT_COLUMN_SLASH_TICKS + LIGHT_COLUMN_HOLD_TICKS; // 能量光柱开场显示 tick 数，EX 剑气在劈落结束后已开始推进。
    public static final int LIGHT_COLUMN_FACE_COUNT = 4; // 同轴光柱长粒子数量，第二片只负责补足侧面视角。
    public static final float LIGHT_COLUMN_LIFE = (LIGHT_COLUMN_SLASH_TICKS + LIGHT_COLUMN_HOLD_TICKS + LIGHT_COLUMN_FADE_TICKS + LIGHT_COLUMN_GRACE_TICKS) / 20.0F; // 同轴光柱长粒子生命周期，覆盖劈下、保留、淡出和缓冲阶段。
    public static final float LIGHT_COLUMN_LENGTH = 60.20F; // 单片长粒子覆盖整根能量光柱的长度。
    public static final float LIGHT_COLUMN_MAIN_WIDTH = 0.8F; // 主光柱面宽度。
    public static final float LIGHT_COLUMN_SUPPLEMENT_WIDTH = 0.7F; // 补充光柱面宽度，只防止侧面观察变薄。
    public static final float LIGHT_COLUMN_MAIN_ALPHA = 0.5F; // 主光柱出生透明度。
    public static final float LIGHT_COLUMN_SUPPLEMENT_ALPHA = 0.5F; // 补充光柱出生透明度。
    public static final float LIGHT_COLUMN_MASK_RADIUS = 0.68F; // 长光柱 LIGHT_EFFECT 遮罩半径，减少长片边缘断裂。
    public static final float LIGHT_COLUMN_MASK_SOFTNESS = 0.18F; // 长光柱 LIGHT_EFFECT 遮罩柔边。
    public static final float LIGHT_COLUMN_FACE_STEP_RADIANS = (float) (Math.PI * 0.5D); // 同轴补充面绕光柱长轴旋转 90 度。
    public static final float LIGHT_COLUMN_TARGET_MIN_Y = -0.85F; // 能量光柱最终目标方向的最低俯角限制。
    public static final float LIGHT_COLUMN_TARGET_MAX_Y = 0.35F; // 能量光柱最终目标方向的最高仰角限制。
    public static final float LIGHT_COLUMN_SMALL_ARC_DEGREES = 45.0F; // 小角度劈砍保护阈值。
    public static final float LIGHT_COLUMN_SMALL_ARC_MAX_Y = 0.98F; // 小角度劈砍时允许保留的最高 Y 分量。

    // 能量光柱空气切痕参数。
    public static final float LIGHT_COLUMN_AIR_CUT_EDGE_OFFSET = 0.15F; // 贴合式 V 形空气切痕相对主光柱边缘的横向偏移。
    public static final float LIGHT_COLUMN_AIR_CUT_WIDTH = 0.72F; // 贴合式 V 形空气切痕宽度，窄于主光柱。
    public static final float LIGHT_COLUMN_AIR_CUT_LENGTH_SCALE = 0.96F; // 贴合式 V 形空气切痕长度相对主光柱的倍率。
    public static final float LIGHT_COLUMN_AIR_CUT_ALPHA = 0.32F; // 贴合式 V 形空气切痕出生透明度。
    public static final float LIGHT_COLUMN_AIR_CUT_ROLL_RADIANS = (float) Math.toRadians(28.0D); // 贴合式 V 形空气切痕绕长轴倾斜角。

    // 能量光柱扇面 SDF 参数。
    public static final int LIGHT_COLUMN_FAN_SDF_PER_TICK = 30; // 光柱劈砍扇面每 tick SDF 数量。
    public static final float LIGHT_COLUMN_FAN_SDF_RADIUS_MIN = 1.2F; // 扇面 SDF 距离光柱根部的最小半径。
    public static final float LIGHT_COLUMN_FAN_SDF_RADIUS_MAX = 18.0F; // 扇面 SDF 距离光柱根部的最大半径。
    public static final float LIGHT_COLUMN_FAN_SDF_SIDE_WIDTH = 0.0F; // 扇面 SDF 左右扩散宽度。
    public static final float LIGHT_COLUMN_FAN_SDF_SIZE_MIN = 0.12F; // 扇面 SDF 最小尺寸。
    public static final float LIGHT_COLUMN_FAN_SDF_SIZE_MAX = 0.45F; // 扇面 SDF 最大尺寸。
    public static final float LIGHT_COLUMN_FAN_SDF_ALPHA = 0.55F; // 扇面 SDF 初始透明度。
    public static final float LIGHT_COLUMN_FAN_SDF_LIFE_MIN = 10.25F; // 扇面 SDF 最短生命周期。
    public static final float LIGHT_COLUMN_FAN_SDF_LIFE_MAX = 10.75F; // 扇面 SDF 最长生命周期。
    public static final float LIGHT_COLUMN_FAN_SDF_SPEED_MIN = 1.5F; // 扇面 SDF 起始速度最小值。
    public static final float LIGHT_COLUMN_FAN_SDF_SPEED_MAX = 6.0F; // 扇面 SDF 起始速度最大值。
    public static final float LIGHT_COLUMN_FAN_SDF_END_SPEED_MIN = 0.3F; // 扇面 SDF 结束速度最小值。
    public static final float LIGHT_COLUMN_FAN_SDF_END_SPEED_MAX = 1.8F; // 扇面 SDF 结束速度最大值。
    public static final float LIGHT_COLUMN_FAN_SDF_SPEED_CURVE_MIN = 0.65F; // 扇面 SDF 速度曲线最小值。
    public static final float LIGHT_COLUMN_FAN_SDF_SPEED_CURVE_MAX = 1.20F; // 扇面 SDF 速度曲线最大值。
    public static final float LIGHT_COLUMN_FAN_SDF_ROTATION_DEGREES = 25.0F; // 扇面 SDF 平面内随机旋转角。

    // EX 剑气终点屏幕暗化参数。
    public static final float EX_WAVE_END_DARKEN_STRENGTH = 0.90F; // 终点爆闪时场景暗化强度。
    public static final int EX_WAVE_END_DARKEN_LIFE_TICKS = 22; // 终点暗化总持续 tick。
    public static final int EX_WAVE_END_DARKEN_FADE_IN_TICKS = 2; // 终点暗化淡入 tick。
    public static final int EX_WAVE_END_DARKEN_FADE_OUT_TICKS = 12; // 终点暗化淡出 tick。

    // EX 剑气终点星星粒子参数。
    public static final double EX_WAVE_END_STAR_Y_OFFSET = 0.0D; // 终点星星相对剑气中心线的垂直偏移。
    public static final float EX_WAVE_END_STAR_LIFE = 1.85F; // 终点星星生命周期。
    public static final float EX_WAVE_END_STAR_START_SIZE = 258.0F; // 终点星星出生尺寸。
    public static final float EX_WAVE_END_STAR_MID_SIZE = 338.0F; // 终点星星膨胀峰值尺寸。
    public static final float EX_WAVE_END_STAR_END_SIZE = 228; // 终点星星消散尺寸。
    public static final float EX_WAVE_END_STAR_MID_TIME = 0.22F; // 终点星星膨胀峰值时间点。
    public static final float EX_WAVE_END_STAR_ROTATION_SPEED = 1.6F; // 终点星星逆时针旋转速度。
    public static final int EX_WAVE_END_STAR_COLOR = 0xFFD15A; // 终点星星主金色。
    public static final int EX_WAVE_END_STAR_MID_COLOR = 0xFFF4A8; // 终点星星中段白金色。
    public static final float EX_WAVE_END_STAR_START_ALPHA = 0.75F; // 终点星星出生透明度。
    public static final float EX_WAVE_END_STAR_MID_ALPHA = 1.0F; // 终点星星峰值透明度。
    public static final float EX_WAVE_END_STAR_END_ALPHA = 0.5F; // 终点星星结束透明度。
    public static final float EX_WAVE_END_STAR_SOUND_VOLUME = 1.35F; // 终点音效音量。
    public static final float EX_WAVE_END_STAR_SOUND_PITCH = 0.95F; // 终点音效音高。

    public ExcaliburSwordWaveEffects() {
    }

    // 按客户端实体本地 tick 提交开场光柱、光柱扇面 SDF、侧面主剑气和配套光效。
    public static void emitWaveBatch(ExcaliburSwordWaveEntity entity, Consumer<ParticleEmitTask> particleConsumer) {
        if (entity == null || particleConsumer == null) return;
        int age = entity.tickCount;
        int waveAge = entity.getWaveAge();
        if (entity.clientLastParticleTick == age) return;
        entity.clientLastParticleTick = age;

        if (age <= LIGHT_COLUMN_OPENING_TICKS) {
            emitOpeningDirectedLightColumn(entity, particleConsumer);
        }
        if (age <= LIGHT_COLUMN_SLASH_TICKS) {
            emitLightColumnFanSdfParticles(entity, particleConsumer);
        }

        emitEndEffectIfReady(entity, particleConsumer);
        ExcaliburEndShockwaveEffects.emitAfterStarIfReady(entity, particleConsumer);
        if (resolveVisualWaveAge(waveAge) <= 0 || age % Math.max(1, EMIT_INTERVAL_TICKS) != 0) return;
        emitPointSwordWaveParticles(entity, waveAge, particleConsumer);
    }

    // 视觉剑气到达终点后提交一次屏幕暗化、终点音效和超大星星粒子。
    public static void emitEndEffectIfReady(ExcaliburSwordWaveEntity entity, Consumer<ParticleEmitTask> particleConsumer) {
        if (entity == null || particleConsumer == null || entity.clientEndEffectPlayed) return;
        if (!entity.level().isClientSide()) return;
        int visualWaveAge = resolveVisualWaveAge(entity.getWaveAge());
        if (visualWaveAge < getVisualTravelTicks() + 1) return;

        entity.clientEndEffectPlayed = true;
        Vec3 endPos = resolveEndStarPosition(entity);
        if (Exe.POST != null) {
            Exe.POST.addScreenDarkening(EX_WAVE_END_DARKEN_STRENGTH, EX_WAVE_END_DARKEN_LIFE_TICKS,
                    EX_WAVE_END_DARKEN_FADE_IN_TICKS, EX_WAVE_END_DARKEN_FADE_OUT_TICKS);
        }
        entity.level().playLocalSound(endPos.x, endPos.y, endPos.z, ModSounds.AAAAA.get(),
                SoundSource.PLAYERS, EX_WAVE_END_STAR_SOUND_VOLUME, EX_WAVE_END_STAR_SOUND_PITCH, false);
        particleConsumer.accept(createEndStarParticle(endPos));
    }

    // 计算 EX 剑气终点星星位置，默认使用中心线终点而不是贴地剑气底边。
    public static Vec3 resolveEndStarPosition(ExcaliburSwordWaveEntity entity) {
        if (entity == null) return Vec3.ZERO;
        return entity.position()
                .add(entity.getForward().scale(entity.getMaxRange()))
                .add(0.0D, EX_WAVE_END_STAR_Y_OFFSET, 0.0D);
    }

    // 创建终点超大号金黄色 STAR_TEXTURE 星星粒子。
    public static ParticleEmitTask createEndStarParticle(Vec3 position) {
        Vec3 safePosition = position == null ? Vec3.ZERO : position;
        return new ParticleEmitTask()
                .position(safePosition)
                .direction(0.0F, 1.0F, 0.0F)
                .speed(0.0F, 0.0F)
                .spread(0.0F)
                .life(EX_WAVE_END_STAR_LIFE)
                .gravity(0.0F)
                .sizeOverLife(
                        EX_WAVE_END_STAR_START_SIZE, EX_WAVE_END_STAR_START_SIZE,
                        EX_WAVE_END_STAR_MID_SIZE, EX_WAVE_END_STAR_MID_SIZE,
                        EX_WAVE_END_STAR_END_SIZE, EX_WAVE_END_STAR_END_SIZE,
                        EX_WAVE_END_STAR_MID_TIME)
                .fixedRotation(0.0F)
                .rotationSpeed(EX_WAVE_END_STAR_ROTATION_SPEED)
                .fixedSizeScale()
                .color(EX_WAVE_END_STAR_COLOR, EX_WAVE_END_STAR_START_ALPHA)
                .midColor(EX_WAVE_END_STAR_MID_COLOR, EX_WAVE_END_STAR_MID_ALPHA)
                .midColorTime(EX_WAVE_END_STAR_MID_TIME)
                .endColor(EX_WAVE_END_STAR_COLOR, EX_WAVE_END_STAR_END_ALPHA)
                .material(ParticleMaterialKey.STAR_TEXTURE)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(0)
                .duration(0.0F)
                .burst(1);
    }

    // 光柱落地后按视觉总时间和最终距离动态计算前沿位置，再逐点位生成 EX 剑气。
    public static void emitPointSwordWaveParticles(ExcaliburSwordWaveEntity entity, int waveAge,
                                                   Consumer<ParticleEmitTask> particleConsumer) {
        if (entity == null || particleConsumer == null) return;
        int visualWaveAge = resolveVisualWaveAge(waveAge);
        if (visualWaveAge <= 0 || visualWaveAge > getVisualTravelTicks() + 1) return;
        double visualDistance = resolveVisualDistance(entity, visualWaveAge);
        double previousDistance = resolveVisualDistance(entity, visualWaveAge - 1);
        int pathFillCount = resolvePathFillCount(visualWaveAge, previousDistance, visualDistance);
        int laneCount = resolveVisualLaneCount(entity, visualDistance);
        Vec3 forward = ExcaliburSwordWaveEntity.safeNormalize(entity.getForward(), WORLD_FORWARD);
        Vec3 side = ExcaliburSwordWaveEntity.safeNormalize(entity.getSide(), WORLD_SIDE);
        Vec3 visualSide = side.scale(EX_WAVE_VISUAL_SIDE_SIGN);
        Vec3 basePlaneNormal = resolveBasePlaneNormal(forward, visualSide);
        float pathScale = resolvePathScale(entity, visualDistance);

        // 视觉侧向只影响客户端粒子，服务端伤害路线仍使用实体原始 side。
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++) {
            Vec3 lanePosition = resolveVisualLanePosition(entity, visualDistance, laneIndex, laneCount, visualSide)
                    .add(0.0D, PARTICLE_BASE_Y_OFFSET, 0.0D);
            SwordWaveParticleVisual visual = createMainVisual(
                    entity.getVisualSeed(), visualWaveAge, laneIndex, lanePosition, forward, visualSide, basePlaneNormal, pathScale);
            particleConsumer.accept(createLaneParticle(visual));
            particleConsumer.accept(createLaneLightParticle(visual, entity.getVisualSeed(), visualWaveAge, laneIndex));
            emitBackwardSdfParticles(entity.getVisualSeed(), visualWaveAge, laneIndex, visual.position, forward, visualSide, particleConsumer);
            emitStartFillSwordWaveParticles(entity, visualWaveAge, laneIndex, laneCount, visualDistance,
                    forward, visualSide, basePlaneNormal, pathScale, particleConsumer);
            emitPathFillSwordWaveParticles(entity, visualWaveAge, laneIndex, laneCount, previousDistance, visualDistance,
                    pathFillCount, forward, visualSide, basePlaneNormal, particleConsumer);
        }
    }

    // 起点段额外补充同路径小剑气粒子，减少玩家身边因粒子较小产生的视觉间隔。
    public static void emitStartFillSwordWaveParticles(ExcaliburSwordWaveEntity entity, int visualWaveAge, int laneIndex,
                                                       int laneCount, double visualDistance, Vec3 forward, Vec3 visualSide,
                                                       Vec3 basePlaneNormal, float pathScale,
                                                       Consumer<ParticleEmitTask> particleConsumer) {
        if (entity == null || particleConsumer == null) return;
        if (visualDistance > resolveStartFillDistanceLimit(entity)) return;

        int fillCount = resolveStartFillCount(visualWaveAge);
        if (fillCount <= 0) return;
        double previousDistance = resolveVisualDistance(entity, visualWaveAge - 1);

        // 补粒子沿上一批视觉前沿到当前前沿之间均匀铺开，不改变服务端锥形伤害范围。
        for (int fillIndex = 1; fillIndex <= fillCount; fillIndex++) {
            double fillDistance = resolveStartFillDistance(visualDistance, previousDistance, fillIndex, fillCount);
            Vec3 fillPosition = resolveVisualLanePosition(entity, fillDistance, laneIndex, laneCount, visualSide)
                    .add(0.0D, PARTICLE_BASE_Y_OFFSET, 0.0D);
            int fillSalt = laneIndex + fillIndex * 1000;
            int fillAge = visualWaveAge * 31 + fillIndex;
            float fillScale = pathScale * stableRange(entity.getVisualSeed(), visualWaveAge, fillSalt, 24,
                    EX_WAVE_START_FILL_SCALE_MIN, EX_WAVE_START_FILL_SCALE_MAX);
            SwordWaveParticleVisual fillVisual = createMainVisual(
                    entity.getVisualSeed(), fillAge, fillSalt, fillPosition, forward, visualSide, basePlaneNormal, fillScale);
            particleConsumer.accept(createLaneParticle(fillVisual));
            particleConsumer.accept(createLaneLightParticle(fillVisual, entity.getVisualSeed(), fillAge, fillSalt));
        }
    }

    // 动态计算起点补粒子生效距离，避免大射程时第二批剑气跳出固定补粒子范围。
    public static double resolveStartFillDistanceLimit(ExcaliburSwordWaveEntity entity) {
        if (entity == null) return EX_WAVE_START_FILL_DISTANCE;
        double firstStepDistance = Math.max(0.0D, resolveVisualDistance(entity, 2) - resolveVisualDistance(entity, 1));
        return Math.max(EX_WAVE_START_FILL_DISTANCE, firstStepDistance * 1.25D);
    }

    // 根据视觉年龄返回起点段补粒子数量，前三批额外增强以填补玩家身边空隙。
    public static int resolveStartFillCount(int visualWaveAge) {
        int fillCount = Math.max(0, EX_WAVE_START_FILL_COUNT);
        if (visualWaveAge > 0 && visualWaveAge <= EX_WAVE_START_EXTRA_FILL_TICKS) {
            fillCount += Math.max(0, EX_WAVE_START_EXTRA_FILL_COUNT);
        }
        return fillCount;
    }

    // 按当前和上一批视觉前沿距离计算补粒子位置，间距过小时使用最小回退距离兜底。
    public static double resolveStartFillDistance(double visualDistance, double previousDistance, int fillIndex, int fillCount) {
        double current = Math.max(0.0D, visualDistance);
        double previous = Math.max(0.0D, Math.min(previousDistance, current));
        double tickStep = Math.max(0.0D, current - previous);
        double fillStep = tickStep > 1.0E-6D
                ? tickStep / Math.max(1, fillCount + 1)
                : Math.max(0.0D, EX_WAVE_START_FILL_BACK_STEP);
        fillStep = Math.max(fillStep, Math.max(0.0D, EX_WAVE_START_FILL_BACK_STEP));
        return Math.max(0.0D, current - Math.max(1, fillIndex) * fillStep);
    }

    // 沿上一批视觉前沿到当前前沿补充同路径粒子，减少长射程中段的前后间隙。
    public static void emitPathFillSwordWaveParticles(ExcaliburSwordWaveEntity entity, int visualWaveAge, int laneIndex,
                                                       int laneCount, double previousDistance, double visualDistance,
                                                       int pathFillCount, Vec3 forward, Vec3 visualSide,
                                                       Vec3 basePlaneNormal, Consumer<ParticleEmitTask> particleConsumer) {
        if (entity == null || particleConsumer == null || pathFillCount <= 0) return;

        // 路径补点只补主剑气和配套光效，不额外生成后向 SDF，避免长射程时任务数量过高。
        for (int fillIndex = 1; fillIndex <= pathFillCount; fillIndex++) {
            double fillDistance = resolvePathFillDistance(previousDistance, visualDistance, fillIndex, pathFillCount);
            Vec3 fillPosition = resolveVisualLanePosition(entity, fillDistance, laneIndex, laneCount, visualSide)
                    .add(0.0D, PARTICLE_BASE_Y_OFFSET, 0.0D);
            int fillSalt = laneIndex + fillIndex * 2000;
            int fillAge = visualWaveAge * 47 + fillIndex;
            float fillScale = resolvePathScale(entity, fillDistance) * stableRange(entity.getVisualSeed(), visualWaveAge, fillSalt, 25,
                    EX_WAVE_PATH_FILL_SCALE_MIN, EX_WAVE_PATH_FILL_SCALE_MAX);
            SwordWaveParticleVisual fillVisual = createMainVisual(
                    entity.getVisualSeed(), fillAge, fillSalt, fillPosition, forward, visualSide, basePlaneNormal, fillScale);
            particleConsumer.accept(createLaneParticle(fillVisual));
            particleConsumer.accept(createLaneLightParticle(fillVisual, entity.getVisualSeed(), fillAge, fillSalt));
        }
    }

    // 根据前后视觉距离自动计算中段补点数量，距离越大补点越多但受最大数量限制。
    public static int resolvePathFillCount(int visualWaveAge, double previousDistance, double visualDistance) {
        if (visualWaveAge < EX_WAVE_PATH_FILL_START_AGE || EX_WAVE_PATH_FILL_MAX_COUNT <= 0) return 0;
        double stepDistance = Math.max(0.0D, visualDistance - previousDistance);
        int fillCount = Math.max(0, Mth.ceil(stepDistance / Math.max(0.001D, EX_WAVE_PATH_FILL_SPACING)) - 1);
        return Math.min(fillCount, Math.max(0, EX_WAVE_PATH_FILL_MAX_COUNT));
    }

    // 在上一批和当前批视觉距离之间均匀插入路径补点。
    public static double resolvePathFillDistance(double previousDistance, double visualDistance, int fillIndex, int fillCount) {
        double previous = Math.max(0.0D, Math.min(previousDistance, visualDistance));
        double current = Math.max(previous, visualDistance);
        double fillT = Mth.clamp(fillIndex / (double) Math.max(1, fillCount + 1), 0.0D, 1.0D);
        return Mth.lerp(fillT, previous, current);
    }

    // 在 EX 剑气前沿出现前提交同轴多面长粒子，并用 MOTION_ARC_DIRECTION 驱动整根光柱劈下。
    public static void emitOpeningDirectedLightColumn(ExcaliburSwordWaveEntity entity, Consumer<ParticleEmitTask> particleConsumer) {
        if (entity == null || particleConsumer == null) return;
        int slashAge = Math.max(0, entity.tickCount);
        if (slashAge != 1) return;
        Vec3 forward = resolveHorizontalForward(entity.getForward());
        Vec3 side = ExcaliburSwordWaveEntity.safeNormalize(entity.getSide(), WORLD_SIDE);
        Vec3 targetDir = resolveLightColumnTargetDirection(entity.getForward(), forward);
        float arcSeconds = Math.max(0.05F, LIGHT_COLUMN_SLASH_TICKS / 20.0F);
        float holdSeconds = Math.max(0.0F, LIGHT_COLUMN_HOLD_TICKS / 20.0F);
        float fadeSeconds = Math.max(0.05F, LIGHT_COLUMN_FADE_TICKS / 20.0F);

        // 两片长粒子首尾同轴，只改变绕光柱长轴的面角，避免侧面观察时主光片变成细线。
        for (int faceIndex = 0; faceIndex < LIGHT_COLUMN_FACE_COUNT; faceIndex++) {
            particleConsumer.accept(createArcDirectionLightColumnParticle(
                    entity.position(), targetDir, arcSeconds, holdSeconds, fadeSeconds, faceIndex));
        }
        emitOpeningAirCutLightColumns(entity.position(), targetDir, side, arcSeconds, holdSeconds, fadeSeconds, particleConsumer);
    }

    // 在主光柱边缘追加贴合式 V 形空气切痕，不改变目标方向，只通过边缘偏移和面旋转表现劈开空气。
    public static void emitOpeningAirCutLightColumns(Vec3 origin, Vec3 targetDir, Vec3 side,
                                                     float arcSeconds, float holdSeconds, float fadeSeconds,
                                                     Consumer<ParticleEmitTask> particleConsumer) {
        if (particleConsumer == null) return;
        Vec3 safeSide = ExcaliburSwordWaveEntity.safeNormalize(side, WORLD_SIDE);
        Vec3 safeTargetDir = resolveLightColumnTargetDirection(targetDir, WORLD_FORWARD);
        for (int sideIndex = 0; sideIndex < 2; sideIndex++) {
            double sideSign = sideIndex == 0 ? -1.0D : 1.0D;
            Vec3 airCutOrigin = (origin == null ? Vec3.ZERO : origin).add(safeSide.scale(sideSign * LIGHT_COLUMN_AIR_CUT_EDGE_OFFSET));
            float faceRoll = (float) sideSign * LIGHT_COLUMN_AIR_CUT_ROLL_RADIANS;
            particleConsumer.accept(createArcDirectionLightColumnParticle(
                    airCutOrigin, safeTargetDir, arcSeconds, holdSeconds, fadeSeconds,
                    LIGHT_COLUMN_AIR_CUT_WIDTH, LIGHT_COLUMN_LENGTH * LIGHT_COLUMN_AIR_CUT_LENGTH_SCALE,
                    LIGHT_COLUMN_AIR_CUT_ALPHA, faceRoll));
        }
    }

    // 计算 EX 剑气视觉年龄，允许客户端粒子相对伤害前沿提前生成。
    public static int resolveVisualWaveAge(int waveAge) {
        return Math.max(0, waveAge + EX_WAVE_VISUAL_ADVANCE_TICKS);
    }

    // 返回 EX 剑气视觉从玩家起点到最终距离的总 tick 数。
    public static int getVisualTravelTicks() {
        return Math.max(1, EX_WAVE_VISUAL_TRAVEL_TICKS);
    }

    // 按视觉总时间和最终距离动态计算 EX 剑气当前视觉距离，不再固定每 tick 前进距离。
    public static double resolveVisualDistance(ExcaliburSwordWaveEntity entity, int visualWaveAge) {
        if (entity == null) return 0.0D;
        double visualTime = Math.max(0.0D, visualWaveAge - 1.0D);
        double visualT = Mth.clamp(visualTime / getVisualTravelTicks(), 0.0D, 1.0D);
        double distanceT = Math.pow(visualT, Math.max(0.05F, EX_WAVE_VISUAL_DISTANCE_POWER));
        return entity.getMaxRange() * distanceT;
    }

    // 根据视觉距离计算客户端显示用 laneCount，必要时可独立于服务端伤害间隔调节。
    public static int resolveVisualLaneCount(ExcaliburSwordWaveEntity entity, double visualDistance) {
        if (entity == null) return 3;
        double currentWidth = entity.getBranchOffset(visualDistance) * 2.0D;
        int laneCount = Math.max(3, Mth.ceil(currentWidth / Math.max(0.001D, EX_WAVE_VISUAL_LANE_SPACING)) + 1);
        if ((laneCount & 1) == 0) laneCount++;
        return Math.min(laneCount, Math.max(3, EX_WAVE_VISUAL_MAX_LANE_COUNT));
    }

    // 使用视觉侧轴计算客户端粒子位置，修正左右显示但不影响服务端伤害路线。
    public static Vec3 resolveVisualLanePosition(ExcaliburSwordWaveEntity entity, double distance,
                                                  int laneIndex, int laneCount, Vec3 visualSide) {
        if (entity == null) return Vec3.ZERO;
        Vec3 center = entity.position().add(entity.getForward().scale(Math.max(0.0D, distance)));
        if (laneCount <= 1) return center;
        double laneT = Mth.clamp(laneIndex / (double) (laneCount - 1), 0.0D, 1.0D);
        double branchOffset = entity.getBranchOffset(distance);
        double laneOffset = Mth.lerp(laneT, -branchOffset, branchOffset);
        Vec3 safeVisualSide = ExcaliburSwordWaveEntity.safeNormalize(visualSide, WORLD_SIDE);
        return center.add(safeVisualSide.scale(laneOffset));
    }

    // 根据路径进度计算整体尺寸倍率，让 EX 剑气从玩家起点小、终点大。
    public static float resolvePathScale(ExcaliburSwordWaveEntity entity, double visualDistance) {
        if (entity == null) return EX_WAVE_PATH_START_SCALE;
        double pathT = Mth.clamp(visualDistance / Math.max(1.0D, entity.getMaxRange()), 0.0D, 1.0D);
        float scaleT = (float) Math.pow(pathT, Math.max(0.05F, EX_WAVE_PATH_SCALE_POWER));
        return Mth.lerp(scaleT, EX_WAVE_PATH_START_SCALE, EX_WAVE_PATH_END_SCALE);
    }

    // 在能量光柱劈落过程中为整片弧面补充短寿命 SDF 火花。
    public static void emitLightColumnFanSdfParticles(ExcaliburSwordWaveEntity entity, Consumer<ParticleEmitTask> particleConsumer) {
        if (entity == null || particleConsumer == null) return;
        int age = Math.max(0, entity.tickCount);
        if (age <= 0 || age > LIGHT_COLUMN_SLASH_TICKS) return;
        Vec3 forward = resolveHorizontalForward(entity.getForward());
        Vec3 side = ExcaliburSwordWaveEntity.safeNormalize(entity.getSide(), WORLD_SIDE);
        Vec3 targetDir = resolveLightColumnTargetDirection(entity.getForward(), forward);
        float slashT = smoothstep(age / (float) Math.max(1, LIGHT_COLUMN_SLASH_TICKS));
        Vec3 beamDir = slerpVec3(WORLD_UP, targetDir, slashT);

        // 每个 tick 沿当前光柱方向和侧向宽度采样一批 SDF，形成被劈开的扇面碎光。
        for (int sampleIndex = 0; sampleIndex < LIGHT_COLUMN_FAN_SDF_PER_TICK; sampleIndex++) {
            particleConsumer.accept(createLightColumnFanSdfParticle(
                    entity.getVisualSeed(), age, sampleIndex, entity.position(), beamDir, side));
        }
    }

    // 创建单个能量光柱扇面 SDF 粒子。
    public static ParticleEmitTask createLightColumnFanSdfParticle(int visualSeed, int age, int sampleIndex,
                                                                    Vec3 origin, Vec3 beamDir, Vec3 side) {
        Vec3 safeOrigin = origin == null ? Vec3.ZERO : origin;
        Vec3 safeBeamDir = ExcaliburSwordWaveEntity.safeNormalize(beamDir, WORLD_UP);
        Vec3 safeSide = ExcaliburSwordWaveEntity.safeNormalize(side, WORLD_SIDE);
        double radius = stableRange(visualSeed, age, sampleIndex, 300, LIGHT_COLUMN_FAN_SDF_RADIUS_MIN, LIGHT_COLUMN_FAN_SDF_RADIUS_MAX);
        double sideOffset = stableRange(visualSeed, age, sampleIndex, 301, -LIGHT_COLUMN_FAN_SDF_SIDE_WIDTH, LIGHT_COLUMN_FAN_SDF_SIDE_WIDTH)
                * (radius / Math.max(0.001F, LIGHT_COLUMN_FAN_SDF_RADIUS_MAX));
        double backOffset = stableRange(visualSeed, age, sampleIndex, 302, -0.35D, 0.35D);
        Vec3 position = safeOrigin.add(safeBeamDir.scale(radius)).add(safeSide.scale(sideOffset)).add(WORLD_FORWARD.scale(backOffset));

        double sideMotion = stableRange(visualSeed, age, sampleIndex, 303, -0.85D, 0.85D);
        double upMotion = stableRange(visualSeed, age, sampleIndex, 304, -0.10D, 0.55D);
        Vec3 direction = ExcaliburSwordWaveEntity.safeNormalize(
                safeSide.scale(sideMotion).add(safeBeamDir.scale(0.25D)).add(WORLD_UP.scale(upMotion)), safeSide);
        float size = stableRange(visualSeed, age, sampleIndex, 305, LIGHT_COLUMN_FAN_SDF_SIZE_MIN, LIGHT_COLUMN_FAN_SDF_SIZE_MAX);
        float midSize = size * stableRange(visualSeed, age, sampleIndex, 306, 1.15F, 1.85F);
        float endSize = size * stableRange(visualSeed, age, sampleIndex, 307, 0.20F, 0.45F);
        float life = stableRange(visualSeed, age, sampleIndex, 308, LIGHT_COLUMN_FAN_SDF_LIFE_MIN, LIGHT_COLUMN_FAN_SDF_LIFE_MAX);
        float startSpeed = stableRange(visualSeed, age, sampleIndex, 309, LIGHT_COLUMN_FAN_SDF_SPEED_MIN, LIGHT_COLUMN_FAN_SDF_SPEED_MAX);
        float endSpeed = stableRange(visualSeed, age, sampleIndex, 310, LIGHT_COLUMN_FAN_SDF_END_SPEED_MIN, LIGHT_COLUMN_FAN_SDF_END_SPEED_MAX);
        float speedCurve = stableRange(visualSeed, age, sampleIndex, 311, LIGHT_COLUMN_FAN_SDF_SPEED_CURVE_MIN, LIGHT_COLUMN_FAN_SDF_SPEED_CURVE_MAX);
        float rotation = (float) Math.toRadians(stableRange(
                visualSeed, age, sampleIndex, 312, -LIGHT_COLUMN_FAN_SDF_ROTATION_DEGREES, LIGHT_COLUMN_FAN_SDF_ROTATION_DEGREES));
        int shape = DETAIL_SHAPES[Math.min(DETAIL_SHAPES.length - 1,
                (int) (stableUnit(visualSeed, age, sampleIndex, 313) * DETAIL_SHAPES.length))];

        return new ParticleEmitTask()
                .position(position)
                .direction((float) direction.x, (float) direction.y, (float) direction.z)
                .speed(startSpeed, endSpeed)
                .speedCurve(speedCurve)
                .spread(0.12F)
                .life(life)
                .gravity(0.0F)
                .sizeOverLife(size, size, midSize, midSize, endSize, endSize, 0.35F)
                .fixedSizeScale()
                .fixedRotation(rotation)
                .color(0xFFC247, LIGHT_COLUMN_FAN_SDF_ALPHA)
                .midColor(PARTICLE_COLOR, LIGHT_COLUMN_FAN_SDF_ALPHA * 0.70F)
                .endColor(0x6A0900, 0.0F)
                .material(ParticleMaterialKey.DEFAULT_SDF)
                .shape(shape)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(0)
                .duration(0.0F)
                .burst(1);
    }

    // 创建一片由 MOTION_ARC_DIRECTION 驱动的同轴定向 LIGHT_EFFECT 长光柱。
    public static ParticleEmitTask createArcDirectionLightColumnParticle(Vec3 origin, Vec3 targetDir,
                                                                         float arcSeconds, float holdSeconds,
                                                                         float fadeSeconds, int faceIndex) {
        boolean mainFace = faceIndex == 0;
        float width = mainFace ? LIGHT_COLUMN_MAIN_WIDTH : LIGHT_COLUMN_SUPPLEMENT_WIDTH;
        float alpha = mainFace ? LIGHT_COLUMN_MAIN_ALPHA : LIGHT_COLUMN_SUPPLEMENT_ALPHA;
        float faceRoll = faceIndex * LIGHT_COLUMN_FACE_STEP_RADIANS;
        return createArcDirectionLightColumnParticle(
                origin, targetDir, arcSeconds, holdSeconds, fadeSeconds,
                width, LIGHT_COLUMN_LENGTH, alpha, faceRoll);
    }

    // 使用显式宽度、长度、透明度和面旋转创建弧面光柱，供主光柱和贴合式空气切痕共用。
    public static ParticleEmitTask createArcDirectionLightColumnParticle(Vec3 origin, Vec3 targetDir,
                                                                         float arcSeconds, float holdSeconds,
                                                                         float fadeSeconds,
                                                                         float width, float length,
                                                                         float alpha, float faceRoll) {
        float safeWidth = Math.max(0.001F, width);
        float safeLength = Math.max(0.001F, length);
        float safeAlpha = Math.max(0.0F, Math.min(1.0F, alpha));
        Vec3 safeOrigin = origin == null ? Vec3.ZERO : origin;
        Vec3 safeTargetDir = resolveLightColumnTargetDirection(targetDir, WORLD_FORWARD);

        return new ParticleEmitTask()
                .position(safeOrigin)
                .direction((float) safeTargetDir.x, (float) safeTargetDir.y, (float) safeTargetDir.z)
                .speed(0.0F, 0.0F)
                .spread(0.0F)
                .life(LIGHT_COLUMN_LIFE)
                .gravity(0.0F)
                .sizeOverLife(safeWidth, safeLength, safeWidth, safeLength, safeWidth * 0.82F, safeLength, 0.68F)
                .fixedSizeScale()
                .fixedRotation(faceRoll)
                .lightEffectMask(LIGHT_COLUMN_MASK_RADIUS, LIGHT_COLUMN_MASK_SOFTNESS)
                .color(0xFFFFFF, safeAlpha)
                .midColor(0xFFC247, Math.min(1.0F, safeAlpha + 0.22F))
                .midColorTime(0.35F)
                .endColor(0x6A0900, 0.0F)
                .material(ParticleMaterialKey.DIRECTED_LIGHT_EFFECT)
                .arcDirection(safeLength, arcSeconds, holdSeconds, fadeSeconds)
                .rate(0)
                .duration(0.0F)
                .burst(1);
    }
    // 根据当前路线和稳定随机值创建一份主剑气视觉参数快照。
    public static SwordWaveParticleVisual createMainVisual(int visualSeed, int age, int laneIndex,
                                                            Vec3 lanePosition, Vec3 forward, Vec3 side,
                                                            Vec3 basePlaneNormal, float pathScale) {
        Vec3 safePosition = lanePosition == null ? Vec3.ZERO : lanePosition;
        Vec3 safeForward = ExcaliburSwordWaveEntity.safeNormalize(forward, WORLD_FORWARD);
        Vec3 safeSide = ExcaliburSwordWaveEntity.safeNormalize(side, WORLD_SIDE);

        // 位置扰动只作用于客户端视觉，不改变实体服务端伤害路线。
        double forwardOffset = stableRange(visualSeed, age, laneIndex, 1, -MAIN_FORWARD_JITTER, MAIN_FORWARD_JITTER);
        double sideOffset = stableRange(visualSeed, age, laneIndex, 2, -MAIN_SIDE_JITTER, MAIN_SIDE_JITTER);
        double yOffset = stableRange(visualSeed, age, laneIndex, 3, MAIN_Y_JITTER_MIN, MAIN_Y_JITTER_MAX);
        Vec3 position = safePosition.add(safeForward.scale(forwardOffset)).add(safeSide.scale(sideOffset)).add(0.0D, yOffset, 0.0D);

        float yawDegrees = resolveStableRandomYaw(visualSeed, age, laneIndex);
        Vec3 planeNormal = rotateAroundWorldUp(basePlaneNormal, Math.toRadians(yawDegrees));
        float rotation = (float) Math.toRadians(stableRange(
                visualSeed, age, laneIndex, 4, -MAX_RANDOM_ROTATION_DEGREES, MAX_RANDOM_ROTATION_DEGREES));
        float life = PARTICLE_LIFE_SECONDS * stableRange(
                visualSeed, age, laneIndex, 5, MAIN_LIFE_SCALE_MIN, MAIN_LIFE_SCALE_MAX);

        // 三段尺寸先做稳定随机，再乘路径倍率，让玩家起点小、终点大。
        float safePathScale = Math.max(0.001F, pathScale);
        float startSizeX = START_SIZE_X * safePathScale * stableRange(visualSeed, age, laneIndex, 6, 0.82F, 1.12F);
        float startSizeY = START_SIZE_Y * safePathScale * stableRange(visualSeed, age, laneIndex, 7, 0.88F, 1.10F);
        float midSizeX = MID_SIZE_X * safePathScale * stableRange(visualSeed, age, laneIndex, 8, 0.82F, 1.16F);
        float midSizeY = MID_SIZE_Y * safePathScale * stableRange(visualSeed, age, laneIndex, 9, 0.88F, 1.08F);
        float endSizeX = END_SIZE_X * safePathScale * stableRange(visualSeed, age, laneIndex, 10, 0.86F, 1.14F);
        float endSizeY = END_SIZE_Y * safePathScale * stableRange(visualSeed, age, laneIndex, 11, 0.86F, 1.10F);
        float midSizeTime = stableRange(visualSeed, age, laneIndex, 12, 0.72F, 0.90F);

        // 三段颜色分别做轻微亮度和 Alpha 扰动，避免多路剑气同时以完全相同亮度消散。
        int startColor = scaleColor(PARTICLE_COLOR, stableRange(
                visualSeed, age, laneIndex, 13, MAIN_COLOR_SCALE_MIN, MAIN_COLOR_SCALE_MAX));
        int midColor = scaleColor(PARTICLE_COLOR, stableRange(
                visualSeed, age, laneIndex, 14, MAIN_COLOR_SCALE_MIN, MAIN_COLOR_SCALE_MAX));
        int endColor = scaleColor(PARTICLE_COLOR, stableRange(
                visualSeed, age, laneIndex, 15, MAIN_COLOR_SCALE_MIN, MAIN_COLOR_SCALE_MAX));
        float startAlpha = stableRange(visualSeed, age, laneIndex, 16, 0.82F, START_ALPHA);
        float midAlpha = stableRange(visualSeed, age, laneIndex, 17, 0.88F, MID_ALPHA);

        return new SwordWaveParticleVisual(
                position, planeNormal, rotation, life,
                startSizeX, startSizeY, midSizeX, midSizeY, endSizeX, endSizeY, midSizeTime,
                startColor, midColor, endColor, startAlpha, midAlpha, END_ALPHA);
    }

    // 根据视觉参数快照创建单路静止 EX 剑气粒子任务。
    public static ParticleEmitTask createLaneParticle(SwordWaveParticleVisual visual) {
        SwordWaveParticleVisual safeVisual = visual == null ? SwordWaveParticleVisual.fallback() : visual;
        return new ParticleEmitTask()
                .position(safeVisual.position)
                .direction((float) safeVisual.planeNormal.x, (float) safeVisual.planeNormal.y, (float) safeVisual.planeNormal.z)
                .speed(0.0F, 0.0F)
                .spread(0.0F)
                .life(safeVisual.life)
                .gravity(0.0F)
                .sizeOverLife(
                        safeVisual.startSizeX, safeVisual.startSizeY,
                        safeVisual.midSizeX, safeVisual.midSizeY,
                        safeVisual.endSizeX, safeVisual.endSizeY,
                        safeVisual.midSizeTime)
                .fixedSizeScale()
                .fixedRotation(safeVisual.rotation)
                .color(safeVisual.startColor, safeVisual.startAlpha)
                .midColor(safeVisual.midColor, safeVisual.midAlpha)
                .endColor(safeVisual.endColor, safeVisual.endAlpha)
                .material(ParticleMaterialKey.EX_SWORD_WAVE)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(0)
                .duration(0.0F)
                .burst(1);
    }

    // 使用相同位置、尺寸、生命周期和旋转数值创建现有 billboard LIGHT_EFFECT。
    public static ParticleEmitTask createLaneLightParticle(SwordWaveParticleVisual visual,
                                                            int visualSeed, int age, int laneIndex) {
        SwordWaveParticleVisual safeVisual = visual == null ? SwordWaveParticleVisual.fallback() : visual;
        float startAlphaScale = stableRange(
                visualSeed, age, laneIndex, 18, LIGHT_START_ALPHA_SCALE_MIN, LIGHT_START_ALPHA_SCALE_MAX);
        float midAlphaScale = stableRange(
                visualSeed, age, laneIndex, 19, LIGHT_MID_ALPHA_SCALE_MIN, LIGHT_MID_ALPHA_SCALE_MAX);

        // 现有 LIGHT_EFFECT 仍朝向相机并使用中心 Pivot，因此相对主剑气底边独立上移。
        return new ParticleEmitTask()
                .position(safeVisual.position.add(0.0D, EX_WAVE_LIGHT_Y_OFFSET, 0.0D))
                .direction((float) safeVisual.planeNormal.x, (float) safeVisual.planeNormal.y, (float) safeVisual.planeNormal.z)
                .speed(0.0F, 0.0F)
                .spread(0.0F)
                .life(safeVisual.life)
                .gravity(0.0F)
                .sizeOverLife(
                        safeVisual.startSizeX, safeVisual.startSizeY,
                        safeVisual.midSizeX, safeVisual.midSizeY,
                        safeVisual.endSizeX, safeVisual.endSizeY,
                        safeVisual.midSizeTime)
                .fixedSizeScale()
                .fixedRotation(safeVisual.rotation)
                .lightEffectMask(LIGHT_MASK_RADIUS, LIGHT_MASK_SOFTNESS)
                .color(safeVisual.startColor, safeVisual.startAlpha * startAlphaScale)
                .midColor(safeVisual.midColor, safeVisual.midAlpha * midAlphaScale)
                .endColor(safeVisual.endColor, 0.0F)
                .material(ParticleMaterialKey.LIGHT_EFFECT)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(0)
                .duration(0.0F)
                .burst(1);
    }

    // 按稳定概率为当前主剑气提交零到两个向斜后方移动的 SDF 碎片。
    public static void emitBackwardSdfParticles(int visualSeed, int age, int laneIndex,
                                                Vec3 origin, Vec3 forward, Vec3 side,
                                                Consumer<ParticleEmitTask> particleConsumer) {
        if (particleConsumer == null) return;
        int detailCount = resolveDetailCount(visualSeed, age, laneIndex);
        for (int detailIndex = 0; detailIndex < detailCount; detailIndex++) {
            particleConsumer.accept(createBackwardSdfParticle(
                    visualSeed, age, laneIndex, detailIndex, origin, forward, side));
        }
    }

    // 创建单个向左后方或右后方移动的相机 billboard SDF 粒子。
    public static ParticleEmitTask createBackwardSdfParticle(int visualSeed, int age, int laneIndex, int detailIndex,
                                                              Vec3 origin, Vec3 forward, Vec3 side) {
        int saltBase = 100 + detailIndex * 32;
        Vec3 safeOrigin = origin == null ? Vec3.ZERO : origin;
        Vec3 horizontalForward = resolveHorizontalForward(forward);
        Vec3 safeSide = ExcaliburSwordWaveEntity.safeNormalize(side, WORLD_SIDE);
        double sideSign = stableUnit(visualSeed, age, laneIndex, saltBase) < 0.5F ? -1.0D : 1.0D;

        // 方向以水平后方为主，加入左右分量和少量上扬，避免向上瞄准时细节反向钻地。
        double backwardWeight = stableRange(visualSeed, age, laneIndex, saltBase + 1, 0.78D, 1.00D);
        double sideWeight = stableRange(visualSeed, age, laneIndex, saltBase + 2, 0.28D, 0.62D) * sideSign;
        double upWeight = stableRange(visualSeed, age, laneIndex, saltBase + 3, 0.04D, 0.18D);
        Vec3 detailDirection = ExcaliburSwordWaveEntity.safeNormalize(
                horizontalForward.scale(-backwardWeight).add(safeSide.scale(sideWeight)).add(WORLD_UP.scale(upWeight)),
                horizontalForward.scale(-1.0D));

        // 出生点围绕主剑气前沿轻微打散，SDF 尺寸和寿命始终小于主体。
        double forwardOffset = stableRange(visualSeed, age, laneIndex, saltBase + 4, -0.20D, 0.30D);
        double sideOffset = stableRange(visualSeed, age, laneIndex, saltBase + 5, -0.35D, 0.35D);
        double yOffset = stableRange(visualSeed, age, laneIndex, saltBase + 6, 0.10D, 1.20D);
        Vec3 position = safeOrigin.add(horizontalForward.scale(forwardOffset)).add(safeSide.scale(sideOffset)).add(0.0D, yOffset, 0.0D);

        float startSpeed = stableRange(visualSeed, age, laneIndex, saltBase + 7, 5.0F, 9.0F);
        float endSpeed = stableRange(visualSeed, age, laneIndex, saltBase + 8, 1.2F, 3.2F);
        float speedCurve = stableRange(visualSeed, age, laneIndex, saltBase + 9, 0.65F, 1.10F);
        float life = stableRange(visualSeed, age, laneIndex, saltBase + 10, 0.35F, 0.85F);
        float startSizeX = stableRange(visualSeed, age, laneIndex, saltBase + 11, 0.12F, 0.35F);
        float startSizeY = stableRange(visualSeed, age, laneIndex, saltBase + 12, 0.18F, 0.55F);
        float midSizeX = stableRange(visualSeed, age, laneIndex, saltBase + 13, 0.22F, 0.60F);
        float midSizeY = stableRange(visualSeed, age, laneIndex, saltBase + 14, 0.30F, 0.85F);
        float endSizeX = stableRange(visualSeed, age, laneIndex, saltBase + 15, 0.05F, 0.20F);
        float endSizeY = stableRange(visualSeed, age, laneIndex, saltBase + 16, 0.08F, 0.30F);
        float midSizeTime = stableRange(visualSeed, age, laneIndex, saltBase + 17, 0.25F, 0.50F);
        float rotation = (float) Math.toRadians(stableRange(
                visualSeed, age, laneIndex, saltBase + 18, -DETAIL_RANDOM_ROTATION_DEGREES, DETAIL_RANDOM_ROTATION_DEGREES));
        float spread = stableRange(visualSeed, age, laneIndex, saltBase + 19, 0.05F, 0.22F);
        float startAlpha = stableRange(visualSeed, age, laneIndex, saltBase + 20, 0.45F, 0.85F);
        int startColor = scaleColor(PARTICLE_COLOR, stableRange(visualSeed, age, laneIndex, saltBase + 21, 0.85F, 1.08F));
        int endColor = scaleColor(0xE86A12, stableRange(visualSeed, age, laneIndex, saltBase + 22, 0.82F, 1.02F));
        int shape = DETAIL_SHAPES[Math.min(DETAIL_SHAPES.length - 1,
                (int) (stableUnit(visualSeed, age, laneIndex, saltBase + 23) * DETAIL_SHAPES.length))];

        return new ParticleEmitTask()
                .position(position)
                .direction((float) detailDirection.x, (float) detailDirection.y, (float) detailDirection.z)
                .speed(startSpeed, endSpeed)
                .speedCurve(speedCurve)
                .spread(spread)
                .life(life)
                .gravity(0.0F)
                .sizeOverLife(
                        startSizeX, startSizeY,
                        midSizeX, midSizeY,
                        endSizeX, endSizeY,
                        midSizeTime)
                .fixedSizeScale()
                .fixedRotation(rotation)
                .color(startColor, startAlpha)
                .midColor(PARTICLE_COLOR, startAlpha * 0.65F)
                .endColor(endColor, 0.0F)
                .material(ParticleMaterialKey.DEFAULT_SDF)
                .shape(shape)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(0)
                .duration(0.0F)
                .burst(1);
    }

    // 根据稳定概率返回零、一个或两个后向 SDF 粒子。
    public static int resolveDetailCount(int visualSeed, int age, int laneIndex) {
        float value = stableUnit(visualSeed, age, laneIndex, 90);
        if (value < DETAIL_NONE_THRESHOLD) return 0;
        if (value < DETAIL_ONE_THRESHOLD) return 1;
        return 2;
    }

    // 根据实体种子、当前 tick 和路线下标生成侧面基准附近的稳定水平偏转角。
    public static float resolveStableRandomYaw(int visualSeed, int age, int laneIndex) {
        return stableRange(visualSeed, age, laneIndex, 0, -MAX_RANDOM_YAW_DEGREES, MAX_RANDOM_YAW_DEGREES);
    }

    // 优先使用同步侧轴作为侧面法线，异常时根据水平发射方向恢复。
    public static Vec3 resolveBasePlaneNormal(Vec3 forward, Vec3 side) {
        Vec3 horizontalSide = side == null ? Vec3.ZERO : new Vec3(side.x, 0.0D, side.z);
        if (horizontalSide.lengthSqr() >= 1.0E-8D) return horizontalSide.normalize();
        Vec3 horizontalForward = resolveHorizontalForward(forward);
        Vec3 fallback = horizontalForward.cross(WORLD_UP);
        return ExcaliburSwordWaveEntity.safeNormalize(fallback, WORLD_SIDE);
    }

    // 返回发射方向的水平投影，供后向 SDF 避免继承过大的垂直俯仰。
    public static Vec3 resolveHorizontalForward(Vec3 forward) {
        Vec3 horizontalForward = forward == null ? Vec3.ZERO : new Vec3(forward.x, 0.0D, forward.z);
        return ExcaliburSwordWaveEntity.safeNormalize(horizontalForward, WORLD_FORWARD);
    }

    // 使用玩家视野方向作为光柱最终方向，并限制极端俯仰以避免光柱过度穿地或贴近竖直向上。
    public static Vec3 resolveLightColumnTargetDirection(Vec3 rawForward, Vec3 fallbackForward) {
        Vec3 fallback = resolveHorizontalForward(fallbackForward);
        Vec3 safeForward = ExcaliburSwordWaveEntity.safeNormalize(rawForward, fallback);
        double upDot = Mth.clamp(WORLD_UP.dot(safeForward), -1.0D, 1.0D);
        double arcDegrees = Math.toDegrees(Math.acos(upDot));
        if (arcDegrees < LIGHT_COLUMN_SMALL_ARC_DEGREES) {
            double smallArcY = Math.min(LIGHT_COLUMN_SMALL_ARC_MAX_Y, safeForward.y);
            Vec3 smallArc = new Vec3(safeForward.x, smallArcY, safeForward.z);
            return ExcaliburSwordWaveEntity.safeNormalize(smallArc, fallback);
        }
        double clampedY = Math.max(LIGHT_COLUMN_TARGET_MIN_Y, Math.min(LIGHT_COLUMN_TARGET_MAX_Y, safeForward.y));
        Vec3 clamped = new Vec3(safeForward.x, clampedY, safeForward.z);
        return ExcaliburSwordWaveEntity.safeNormalize(clamped, fallback);
    }

    // 绕世界 Y 轴旋转平面法线，实现侧面基准附近的小幅随机朝向。
    public static Vec3 rotateAroundWorldUp(Vec3 direction, double radians) {
        Vec3 safeDirection = ExcaliburSwordWaveEntity.safeNormalize(direction, WORLD_SIDE);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        Vec3 rotated = new Vec3(
                safeDirection.x * cos + safeDirection.z * sin,
                0.0D,
                -safeDirection.x * sin + safeDirection.z * cos);
        return ExcaliburSwordWaveEntity.safeNormalize(rotated, WORLD_SIDE);
    }

    // 把一组正交轨道基底转换成 gpushader.comp 的 X/Y/Z 欧拉角顺序。
    public static Vec3 orbitPlaneAngles(Vec3 basisX, Vec3 basisY, Vec3 basisZ) {
        Vec3 safeX = ExcaliburSwordWaveEntity.safeNormalize(basisX, WORLD_UP);
        Vec3 safeZ = ExcaliburSwordWaveEntity.safeNormalize(basisZ, WORLD_FORWARD);
        Vec3 safeY = ExcaliburSwordWaveEntity.safeNormalize(basisY, safeZ.cross(safeX));
        double yaw = Math.asin(Math.max(-1.0D, Math.min(1.0D, -safeX.z)));
        double cosYaw = Math.cos(yaw);
        double pitch;
        double roll;
        if (Math.abs(cosYaw) > 1.0E-6D) {
            pitch = Math.atan2(safeY.z, safeZ.z);
            roll = Math.atan2(safeX.y, safeX.x);
        } else {
            pitch = 0.0D;
            roll = Math.atan2(-safeY.x, safeY.y);
        }
        return new Vec3(pitch, yaw, roll);
    }

    // 对 0～1 进度做平滑插值，让光柱下劈起止阶段不突兀。
    public static float smoothstep(float t) {
        float safeT = Math.max(0.0F, Math.min(1.0F, t));
        return safeT * safeT * (3.0F - 2.0F * safeT);
    }

    // 在两个方向之间做球面插值，用于把光柱长轴从世界 Y 轴旋向玩家下劈方向。
    public static Vec3 slerpVec3(Vec3 from, Vec3 to, float t) {
        Vec3 safeFrom = ExcaliburSwordWaveEntity.safeNormalize(from, WORLD_UP);
        Vec3 safeTo = ExcaliburSwordWaveEntity.safeNormalize(to, WORLD_FORWARD);
        double dot = Math.max(-1.0D, Math.min(1.0D, safeFrom.dot(safeTo)));
        if (dot > 0.9995D || dot < -0.9995D) {
            return ExcaliburSwordWaveEntity.safeNormalize(safeFrom.lerp(safeTo, t), safeTo);
        }
        double theta = Math.acos(dot);
        double sinTheta = Math.sin(theta);
        double fromWeight = Math.sin((1.0D - t) * theta) / sinTheta;
        double toWeight = Math.sin(t * theta) / sinTheta;
        return ExcaliburSwordWaveEntity.safeNormalize(
                safeFrom.scale(fromWeight).add(safeTo.scale(toWeight)), safeTo);
    }

    // 把稳定随机值映射到指定 float 范围。
    public static float stableRange(int visualSeed, int age, int laneIndex, int salt, float min, float max) {
        return min + (max - min) * stableUnit(visualSeed, age, laneIndex, salt);
    }

    // 把稳定随机值映射到指定 double 范围。
    public static double stableRange(int visualSeed, int age, int laneIndex, int salt, double min, double max) {
        return min + (max - min) * stableUnit(visualSeed, age, laneIndex, salt);
    }

    // 混合实体视觉种子、tick、路线和参数盐，生成不会逐帧变化的 0～1 随机值。
    public static float stableUnit(int visualSeed, int age, int laneIndex, int salt) {
        long mixed = visualSeed;
        mixed ^= (long) age * 0x9E3779B97F4A7C15L;
        mixed ^= (long) laneIndex * 0xC2B2AE3D27D4EB4FL;
        mixed ^= (long) salt * 0x165667B19E3779F9L;
        mixed ^= mixed >>> 30;
        mixed *= 0xBF58476D1CE4E5B9L;
        mixed ^= mixed >>> 27;
        mixed *= 0x94D049BB133111EBL;
        mixed ^= mixed >>> 31;
        return (float) ((mixed >>> 11) * 0x1.0p-53);
    }

    // 按亮度倍率缩放 RGB，并把结果夹紧到 0～255。
    public static int scaleColor(int rgb, float scale) {
        int red = clampColor(Math.round(((rgb >> 16) & 0xFF) * scale));
        int green = clampColor(Math.round(((rgb >> 8) & 0xFF) * scale));
        int blue = clampColor(Math.round((rgb & 0xFF) * scale));
        return (red << 16) | (green << 8) | blue;
    }

    // 把颜色通道夹紧到合法的 8 bit 范围。
    public static int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    // SwordWaveParticleVisual 保存单个主剑气及其配套光效共用的稳定视觉参数。
    public static final class SwordWaveParticleVisual {
        public final Vec3 position; // 粒子发射位置。
        public final Vec3 planeNormal; // EX 剑气世界侧面法线。
        public final float rotation; // 粒子平面内固定旋转弧度。
        public final float life; // 粒子生命周期秒数。
        public final float startSizeX; // 出生阶段宽度。
        public final float startSizeY; // 出生阶段高度。
        public final float midSizeX; // 中间阶段宽度。
        public final float midSizeY; // 中间阶段高度。
        public final float endSizeX; // 结束阶段宽度。
        public final float endSizeY; // 结束阶段高度。
        public final float midSizeTime; // 中间尺寸生命周期时间点。
        public final int startColor; // 出生阶段 RGB。
        public final int midColor; // 中间阶段 RGB。
        public final int endColor; // 结束阶段 RGB。
        public final float startAlpha; // 出生阶段 Alpha。
        public final float midAlpha; // 中间阶段 Alpha。
        public final float endAlpha; // 结束阶段 Alpha。

        public SwordWaveParticleVisual(Vec3 position, Vec3 planeNormal, float rotation, float life,
                                       float startSizeX, float startSizeY, float midSizeX, float midSizeY,
                                       float endSizeX, float endSizeY, float midSizeTime,
                                       int startColor, int midColor, int endColor,
                                       float startAlpha, float midAlpha, float endAlpha) {
            this.position = position == null ? Vec3.ZERO : position;
            this.planeNormal = ExcaliburSwordWaveEntity.safeNormalize(planeNormal, WORLD_SIDE);
            this.rotation = rotation;
            this.life = Math.max(0.01F, life);
            this.startSizeX = Math.max(0.001F, startSizeX);
            this.startSizeY = Math.max(0.001F, startSizeY);
            this.midSizeX = Math.max(0.001F, midSizeX);
            this.midSizeY = Math.max(0.001F, midSizeY);
            this.endSizeX = Math.max(0.001F, endSizeX);
            this.endSizeY = Math.max(0.001F, endSizeY);
            this.midSizeTime = Math.max(0.001F, Math.min(0.999F, midSizeTime));
            this.startColor = startColor;
            this.midColor = midColor;
            this.endColor = endColor;
            this.startAlpha = Math.max(0.0F, Math.min(1.0F, startAlpha));
            this.midAlpha = Math.max(0.0F, Math.min(1.0F, midAlpha));
            this.endAlpha = Math.max(0.0F, Math.min(1.0F, endAlpha));
        }

        // 为防御性空参数调用提供一份使用当前正式基准值的视觉快照。
        public static SwordWaveParticleVisual fallback() {
            return new SwordWaveParticleVisual(
                    Vec3.ZERO, WORLD_SIDE, 0.0F, PARTICLE_LIFE_SECONDS,
                    START_SIZE_X, START_SIZE_Y, MID_SIZE_X, MID_SIZE_Y,
                    END_SIZE_X, END_SIZE_Y, MID_SIZE_TIME,
                    PARTICLE_COLOR, PARTICLE_COLOR, PARTICLE_COLOR,
                    START_ALPHA, MID_ALPHA, END_ALPHA);
        }
    }
}
