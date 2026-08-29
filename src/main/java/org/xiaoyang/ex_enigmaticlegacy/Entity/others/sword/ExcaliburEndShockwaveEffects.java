package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Util.CameraShakeUtil;
import org.xiaoyang.ex_enigmaticlegacy.Config.ExExcaliburConfig;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.material.ParticleMaterialKey;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModSounds;

import java.util.function.Consumer;

// ExcaliburEndShockwaveEffects 负责咖喱棒终点提前二段爆发、五层圆台冲击波、附加粒子、抖动和持续伤害参数。
public class ExcaliburEndShockwaveEffects {
    // 触发时序参数。
    public static int SHOCKWAVE_TRIGGER_ADVANCE_TICKS = 20; // 冲击波相对星星完全消失提前触发的 tick 数。
    public static double SHOCKWAVE_ORIGIN_Y_OFFSET = -3.0D; // 终点冲击波整体起点向下偏移。
    public static int SHOCKWAVE_EXTRA_TAIL_TICKS = 8; // 冲击波结束后的实体保活缓冲 tick。
    public static int SHOCKWAVE_DAMAGE_TICKS = 120; // 终点冲击波持续伤害 tick 数。
    public static int SHOCKWAVE_DAMAGE_INTERVAL_TICKS = 4; // 终点圆柱伤害结算间隔，避免每 tick 伤害过密。

    // 主体圆台冲击波参数。
    public static float HEIGHT_EXPAND_RATIO = 0.65F; // 外层范围增大值对高度的换算比例。
    public static float SHOCKWAVE_LIFE = 16.0F; // 五层冲击波视觉生命周期秒数。
    public static float SHOCKWAVE_SOUND_VOLUME = 1.65F; // 终点冲击波二段爆发音量。
    public static float SHOCKWAVE_SOUND_PITCH = 0.92F; // 终点冲击波二段爆发音高。
    public static float LAYER_2_RANGE_EXPAND = 15.0F; // 第二层相对基础宽度的范围增大值。
    public static float LAYER_3_RANGE_EXPAND = 23.0F; // 第三层相对基础宽度的范围增大值。
    public static float LAYER_4_RANGE_EXPAND = 23.0F; // 第四层相对基础宽度的范围增大值。
    public static float LAYER_5_RANGE_EXPAND = 40.0F; // 第五层相对基础宽度的范围增大值。
    public static ShockwaveLayer[] LAYERS = { // 终点五层上升冲击波参数，从内层到外层排列。
            new ShockwaveLayer(3.0F, 0.3F, 0.8F, 10.0F, 0.0F, 0.0F,
                    0xFFF1A8, 1.0F, 0.42F),
            new ShockwaveLayer(2.0F, 0.5F, 0.4F, 10.0F, LAYER_2_RANGE_EXPAND, 18.0F,
                    0xFFD15A, 0.92F, 0.36F),
            new ShockwaveLayer(3.0F, 0.5F, 1.0F, 10.0F, LAYER_3_RANGE_EXPAND, 36.0F,
                    0xFFB12A, 0.52F, 0.32F),
            new ShockwaveLayer(5.0F, 0.3F, 5.0F, 5.0F, LAYER_4_RANGE_EXPAND, 54.0F,
                    0xFF8A10, 0.98F, 0.28F),
            new ShockwaveLayer(2.0F, 0.5F, 1.0F, 5.0F, LAYER_5_RANGE_EXPAND, 72.0F,
                    0xFFD15A, 0.56F, 0.24F)
    };

    // 底部能量法阵参数。
    public static double MAGIC_CIRCLE_Y_OFFSET = 0.03D; // 底部能量法阵相对冲击波起点的上移，减少地面深度冲突。
    public static float MAGIC_CIRCLE_SIZE_MULTIPLIER = 1.55F; // 底部能量法阵相对最外层圆台底部直径的放大倍率。
    public static int MAGIC_CIRCLE_START_COLOR = 0xFFFDF0; // 底部能量法阵出生白金色。
    public static float MAGIC_CIRCLE_START_ALPHA = 0.58F; // 底部能量法阵出生透明度。
    public static int MAGIC_CIRCLE_MID_COLOR = 0xFFB21A; // 底部能量法阵峰值金色。
    public static float MAGIC_CIRCLE_MID_ALPHA = 1.0F; // 底部能量法阵峰值透明度。
    public static float MAGIC_CIRCLE_MID_TIME = 0.38F; // 底部能量法阵达到峰值的生命周期比例。
    public static int MAGIC_CIRCLE_END_COLOR = 0x8A6A2B; // 底部能量法阵结束深金色。
    public static float MAGIC_CIRCLE_END_ALPHA = 0.7F; // 底部能量法阵结束透明度。
    public static double SHOCKWAVE_MAGIC_CIRCLE_Y_OFFSET = 0.05D; // 大号冲击波法阵相对冲击波起点的上移。
    public static float SHOCKWAVE_MAGIC_CIRCLE_SIZE_MULTIPLIER = 2.35F; // 大号冲击波法阵相对最外层圆台底部直径的放大倍率。
    public static int SHOCKWAVE_MAGIC_CIRCLE_START_COLOR = 0xFFF4D0; // 大号冲击波法阵出生暖白色。
    public static float SHOCKWAVE_MAGIC_CIRCLE_START_ALPHA = 0.48F; // 大号冲击波法阵出生透明度。
    public static int SHOCKWAVE_MAGIC_CIRCLE_MID_COLOR = 0xFF5A00; // 大号冲击波法阵峰值橙红色。
    public static float SHOCKWAVE_MAGIC_CIRCLE_MID_ALPHA = 0.78F; // 大号冲击波法阵峰值透明度。
    public static float SHOCKWAVE_MAGIC_CIRCLE_MID_TIME = 0.30F; // 大号冲击波法阵达到峰值的生命周期比例。
    public static int SHOCKWAVE_MAGIC_CIRCLE_END_COLOR = 0x5A0600; // 大号冲击波法阵结束暗红色。
    public static float SHOCKWAVE_MAGIC_CIRCLE_END_ALPHA = 0.0F; // 大号冲击波法阵结束透明度。

    // 底部雾化裙边参数，用于遮挡空中圆台底部平直切面。
    public static int BOTTOM_SKIRT_SDF_COUNT = 396; // 底部裙边 SDF 粒子数量。
    public static int BOTTOM_SKIRT_LIGHT_COUNT = 348; // 底部裙边 LIGHT_EFFECT 粒子数量。
    public static float BOTTOM_SKIRT_RADIUS_SCALE = 0.50F; // 底部裙边半径相对最外层底部直径的倍率。
    public static double BOTTOM_SKIRT_Y_OFFSET = 0.35D; // 底部裙边相对冲击波起点的高度。
    public static float BOTTOM_SKIRT_LIFE_SCALE = 0.92F; // 底部裙边相对冲击波寿命的倍率。

    // 中心扩散圈参数，出生高度固定在圆柱体高度的一半。
    public static int EXPAND_RING_SDF_BURST = 360; // 中心扩散 SDF 粒子数量。
    public static int EXPAND_RING_LIGHT_BURST = 372; // 中心扩散 LIGHT_EFFECT 粒子数量。
    public static float EXPAND_RING_START_SPEED = 38.0F; // 中心扩散初始高速。
    public static float EXPAND_RING_END_SPEED = 0.15F; // 中心扩散后段近似停留速度。
    public static float EXPAND_RING_SPEED_CURVE = 2.40F; // 中心扩散前快后慢速度曲线。
    public static float EXPAND_RING_SPAWN_RADIUS_JITTER = 1.50F; // 中心扩散出生半径扰动。
    public static float EXPAND_RING_VERTICAL_SPEED = 0.05F; // 中心扩散少量向上速度。
    public static float EXPAND_RING_VERTICAL_JITTER = 0.18F; // 中心扩散垂直速度随机。
    public static float EXPAND_RING_HEIGHT_RATIO = 0.30F; // 中心扩散圈出生高度占圆柱高度比例。

    // 屏幕抖动参数。
    public static double SHOCKWAVE_SHAKE_RADIUS_PADDING = 30.0D; // 动态抖动范围在最大射程和圆柱直径外额外增加的半径。
    public static int SHOCKWAVE_SHAKE_TOTAL_TICKS = 100; // 终点冲击波持续屏幕抖动总 tick，独立于冲击波视觉生命周期。
    public static float SHOCKWAVE_SHAKE_STRENGTH = 1.1F; // 终点冲击波持续抖动强度。
    public static int SHOCKWAVE_SHAKE_FADE_TICKS = 10; // 终点冲击波抖动末尾淡出 tick。

    public ExcaliburEndShockwaveEffects() {
    }

    // 返回星星粒子未完全消失前触发终点冲击波的延迟 tick。
    public static int getTriggerDelayTicks() {
        int starLifeTicks = Mth.ceil(ExcaliburSwordWaveEffects.EX_WAVE_END_STAR_LIFE * 20.0F);
        return Math.max(0, starLifeTicks - Math.max(0, SHOCKWAVE_TRIGGER_ADVANCE_TICKS));
    }

    // 返回终点冲击波在实体 tickCount 时间轴上的开始 tick。
    public static int getShockwaveStartTick() {
        return ExcaliburSwordWaveEffects.EX_WAVE_START_TICKS
                + ExcaliburSwordWaveEffects.getVisualTravelTicks()
                + getTriggerDelayTicks()
                - ExcaliburSwordWaveEffects.EX_WAVE_VISUAL_ADVANCE_TICKS;
    }

    // 返回终点冲击波视觉生命周期 tick 数。
    public static int getShockwaveVisualTicks() {
        return Math.max(1, Mth.ceil(SHOCKWAVE_LIFE * 20.0F));
    }

    // 返回终点冲击波持续伤害和视觉结束后的实体最早可回收 tick。
    public static int getEntityDiscardTick() {
        int shockwaveTicks = Math.max(SHOCKWAVE_DAMAGE_TICKS, getShockwaveVisualTicks());
        return getShockwaveStartTick() + shockwaveTicks + SHOCKWAVE_EXTRA_TAIL_TICKS;
    }

    // 判断服务端当前 tick 是否处于终点冲击波持续伤害阶段。
    public static boolean isDamageActive(ExcaliburSwordWaveEntity entity) {
        if (entity == null) return false;
        int age = entity.tickCount - getShockwaveStartTick();
        return age >= 0 && age < SHOCKWAVE_DAMAGE_TICKS;
    }

    // 判断服务端当前 tick 是否需要结算一次终点冲击波伤害。
    public static boolean shouldDamageThisTick(ExcaliburSwordWaveEntity entity) {
        if (!isDamageActive(entity)) return false;
        int age = entity.tickCount - getShockwaveStartTick();
        return age % Math.max(1, SHOCKWAVE_DAMAGE_INTERVAL_TICKS) == 0;
    }

    // 返回下移后的终点冲击波统一中心。
    public static Vec3 resolveShockwaveCenter(ExcaliburSwordWaveEntity entity) {
        if (entity == null) return Vec3.ZERO;
        return ExcaliburSwordWaveEffects.resolveEndStarPosition(entity).add(0.0D, SHOCKWAVE_ORIGIN_Y_OFFSET, 0.0D);
    }

    // 返回中心扩散圈出生位置，高度位于圆柱体高度的一半。
    public static Vec3 resolveExpandRingCenter(Vec3 center) {
        Vec3 safeCenter = center == null ? Vec3.ZERO : center;
        return safeCenter.add(0.0D, getMaxLayerHeight() * EXPAND_RING_HEIGHT_RATIO, 0.0D);
    }

    // 在提前后的冲击波时刻提交五层冲击波、附加粒子、底部法阵、持续抖动并播放二段爆发音效。
    public static void emitAfterStarIfReady(ExcaliburSwordWaveEntity entity, Consumer<ParticleEmitTask> particleConsumer) {
        if (entity == null || particleConsumer == null || entity.clientEndShockwavePlayed) return;
        if (!entity.level().isClientSide()) return;
        if (!entity.clientEndEffectPlayed) return;
        int visualWaveAge = ExcaliburSwordWaveEffects.resolveVisualWaveAge(entity.getWaveAge());
        if (visualWaveAge < ExcaliburSwordWaveEffects.getVisualTravelTicks() + 1 + getTriggerDelayTicks()) return;

        entity.clientEndShockwavePlayed = true;
        Vec3 center = resolveShockwaveCenter(entity);
        entity.level().playLocalSound(center.x, center.y, center.z, ModSounds.AAAAA.get(),
                SoundSource.PLAYERS, SHOCKWAVE_SOUND_VOLUME, SHOCKWAVE_SOUND_PITCH, false);
        submitShockwaveCameraShake(center);
        emitAfterStar(center, particleConsumer);
    }

    // 一次性提交终点冲击波主体、法阵、底部裙边和半高扩散圈。
    public static void emitAfterStar(Vec3 center, Consumer<ParticleEmitTask> particleConsumer) {
        if (particleConsumer == null) return;
        Vec3 safeCenter = center == null ? Vec3.ZERO : center;
        for (ShockwaveLayer layer : LAYERS) {
            particleConsumer.accept(createLayerParticle(safeCenter, layer));
        }
        particleConsumer.accept(createMagicCircleParticle(safeCenter));
        particleConsumer.accept(createShockwaveMagicCircleParticle(safeCenter));
        emitBottomSkirtParticles(safeCenter, particleConsumer);
        emitExpandRingParticles(safeCenter, particleConsumer);
    }

    // 提交范围型持续相机抖动，让冲击波存在期间保持强烈冲击感。
    public static void submitShockwaveCameraShake(Vec3 center) {
        int shakeTicks = Math.max(1, SHOCKWAVE_SHAKE_TOTAL_TICKS);
        int fadeTicks = Math.max(1, Math.min(SHOCKWAVE_SHAKE_FADE_TICKS, shakeTicks));
        int sustainTicks = Math.max(1, shakeTicks - fadeTicks);
        CameraShakeUtil.addSustainedShake(center, getShakeRadius(), sustainTicks, fadeTicks, SHOCKWAVE_SHAKE_STRENGTH);
    }

    // 根据层参数创建单个 RISING_SHOCKWAVE 圆台粒子。
    public static ParticleEmitTask createLayerParticle(Vec3 center, ShockwaveLayer layer) {
        Vec3 safeCenter = center == null ? Vec3.ZERO : center;
        ShockwaveLayer safeLayer = layer == null ? LAYERS[0] : layer;
        float width = safeLayer.width();
        float height = safeLayer.height();
        int midColor = ExcaliburSwordWaveEffects.scaleColor(safeLayer.color, 1.16F);
        int endColor = ExcaliburSwordWaveEffects.scaleColor(safeLayer.color, 0.72F);
        return new ParticleEmitTask()
                .position(safeCenter)
                .direction(0.0F, 1.0F, 0.0F)
                .speed(0.0F, 0.0F)
                .spread(0.0F)
                .life(SHOCKWAVE_LIFE)
                .gravity(0.0F)
                .size(width, height, (float) Math.toRadians(safeLayer.rotationDegrees))
                .fixedRotation((float) Math.toRadians(safeLayer.rotationDegrees))
                .fixedSizeScale()
                .color(safeLayer.color, safeLayer.alpha)
                .midColor(midColor, Math.min(1.0F, safeLayer.alpha * 1.12F))
                .midColorTime(0.32F)
                .endColor(endColor, safeLayer.alpha * 0.58F)
                .risingShockwave(safeLayer.effectPower, safeLayer.dissolvePower,
                        safeLayer.uvTileX, safeLayer.uvTileY, safeLayer.uvFlowSpeed)
                .material(ParticleMaterialKey.RISING_SHOCKWAVE)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(0)
                .duration(0.0F)
                .burst(1);
    }

    // 创建圆台下方更大范围的水平能量法阵粒子。
    public static ParticleEmitTask createMagicCircleParticle(Vec3 center) {
        Vec3 safeCenter = center == null ? Vec3.ZERO : center;
        float circleSize = getMagicCircleSize();
        return new ParticleEmitTask()
                .position(safeCenter.add(0.0D, MAGIC_CIRCLE_Y_OFFSET, 0.0D))
                .direction(0.0F, 1.0F, 0.0F)
                .speed(0.0F, 0.0F)
                .spread(0.0F)
                .life(SHOCKWAVE_LIFE)
                .gravity(0.0F)
                .size(circleSize, circleSize, 0.0F)
                .fixedRotation(0.0F)
                .fixedSizeScale()
                .color(MAGIC_CIRCLE_START_COLOR, MAGIC_CIRCLE_START_ALPHA)
                .midColor(MAGIC_CIRCLE_MID_COLOR, MAGIC_CIRCLE_MID_ALPHA)
                .midColorTime(MAGIC_CIRCLE_MID_TIME)
                .endColor(MAGIC_CIRCLE_END_COLOR, MAGIC_CIRCLE_END_ALPHA)
                .material(ParticleMaterialKey.MAGIC_CIRCLE_ENERGY)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(0)
                .duration(0.0F)
                .burst(1);
    }

    // 创建底部更大范围的 SHOCKWAVE_MAGIC_CIRCLE 法阵粒子，强化二段爆发范围。
    public static ParticleEmitTask createShockwaveMagicCircleParticle(Vec3 center) {
        Vec3 safeCenter = center == null ? Vec3.ZERO : center;
        float circleSize = getShockwaveMagicCircleSize();
        return new ParticleEmitTask()
                .position(safeCenter.add(0.0D, SHOCKWAVE_MAGIC_CIRCLE_Y_OFFSET, 0.0D))
                .direction(0.0F, 1.0F, 0.0F)
                .speed(0.0F, 0.0F)
                .spread(0.0F)
                .life(SHOCKWAVE_LIFE)
                .gravity(0.0F)
                .size(circleSize, circleSize, 0.0F)
                .fixedRotation(0.0F)
                .fixedSizeScale()
                .color(SHOCKWAVE_MAGIC_CIRCLE_START_COLOR, SHOCKWAVE_MAGIC_CIRCLE_START_ALPHA)
                .midColor(SHOCKWAVE_MAGIC_CIRCLE_MID_COLOR, SHOCKWAVE_MAGIC_CIRCLE_MID_ALPHA)
                .midColorTime(SHOCKWAVE_MAGIC_CIRCLE_MID_TIME)
                .endColor(SHOCKWAVE_MAGIC_CIRCLE_END_COLOR, SHOCKWAVE_MAGIC_CIRCLE_END_ALPHA)
                .material(ParticleMaterialKey.SHOCKWAVE_MAGIC_CIRCLE)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(0)
                .duration(0.0F)
                .burst(1);
    }

    // 提交底部雾化裙边 SDF 和 LIGHT_EFFECT 粒子，遮挡空中圆台底部硬切面。
    public static void emitBottomSkirtParticles(Vec3 center, Consumer<ParticleEmitTask> particleConsumer) {
        Vec3 safeCenter = center == null ? Vec3.ZERO : center;
        particleConsumer.accept(createBottomSkirtSdfParticle(safeCenter));
        particleConsumer.accept(createBottomSkirtLightParticle(safeCenter));
    }

    // 创建底部 SDF 雾化裙边粒子环。
    public static ParticleEmitTask createBottomSkirtSdfParticle(Vec3 center) {
        Vec3 skirtCenter = (center == null ? Vec3.ZERO : center).add(0.0D, BOTTOM_SKIRT_Y_OFFSET, 0.0D);
        return new ParticleEmitTask()
                .position(skirtCenter)
                .speed(1.85F, 0.12F)
                .speedCurve(2.10F)
                .spread(0.55F)
                .life(SHOCKWAVE_LIFE * BOTTOM_SKIRT_LIFE_SCALE)
                .gravity(0.0F)
                .sizeOverLife(1.35F, 1.35F, 3.20F, 2.40F, 1.80F, 1.20F, 0.24F)
                .fixedSizeScale()
                .color(0xFFF1A8, 0.72F)
                .midColor(0xFFB21A, 0.46F)
                .midColorTime(0.30F)
                .endColor(0x8A4A10, 0.0F)
                .shape(ParticleEmitTask.SHAPE_STAR)
                .motion(ParticleEmitTask.MOTION_CIRCULAR)
                .orbit(getBottomSkirtRadius(), 0.35F, -0.10F)
                .orbitSpawnMode(ParticleEmitTask.ORBIT_SPAWN_DISTRIBUTED)
                .material(ParticleMaterialKey.DEFAULT_SDF)
                .rate(0)
                .duration(0.0F)
                .burst(BOTTOM_SKIRT_SDF_COUNT);
    }

    // 创建底部 LIGHT_EFFECT 雾化裙边粒子环。
    public static ParticleEmitTask createBottomSkirtLightParticle(Vec3 center) {
        Vec3 skirtCenter = (center == null ? Vec3.ZERO : center).add(0.0D, BOTTOM_SKIRT_Y_OFFSET + 0.30D, 0.0D);
        return new ParticleEmitTask()
                .position(skirtCenter)
                .speed(1.20F, 0.05F)
                .speedCurve(2.20F)
                .spread(0.18F)
                .life(SHOCKWAVE_LIFE * BOTTOM_SKIRT_LIFE_SCALE)
                .gravity(0.0F)
                .sizeOverLife(4.20F, 2.20F, 8.00F, 4.20F, 5.50F, 2.60F, 0.32F)
                .fixedSizeScale()
                .color(0xFFFDF0, 0.34F)
                .midColor(0xFFB21A, 0.28F)
                .midColorTime(0.34F)
                .endColor(0xFF8A10, 0.0F)
                .lightEffectMask(0.34F, 0.22F)
                .motion(ParticleEmitTask.MOTION_CIRCULAR)
                .orbit(getBottomSkirtRadius() * 0.96F, -0.28F, -0.06F)
                .orbitSpawnMode(ParticleEmitTask.ORBIT_SPAWN_DISTRIBUTED)
                .material(ParticleMaterialKey.LIGHT_EFFECT)
                .rate(0)
                .duration(0.0F)
                .burst(BOTTOM_SKIRT_LIGHT_COUNT);
    }

    // 提交位于圆柱体高度一半的中心扩散圈。
    public static void emitExpandRingParticles(Vec3 center, Consumer<ParticleEmitTask> particleConsumer) {
        Vec3 ringCenter = resolveExpandRingCenter(center);
        particleConsumer.accept(createExpandRingSdfParticle(ringCenter));
        particleConsumer.accept(createExpandRingLightParticle(ringCenter));
    }

    // 创建半高中心 SDF 径向扩散圈。
    public static ParticleEmitTask createExpandRingSdfParticle(Vec3 ringCenter) {
        return new ParticleEmitTask()
                .position(ringCenter == null ? Vec3.ZERO : ringCenter)
                .speed(EXPAND_RING_START_SPEED, EXPAND_RING_END_SPEED)
                .speedCurve(EXPAND_RING_SPEED_CURVE)
                .spread(0.42F)
                .life(SHOCKWAVE_LIFE)
                .gravity(0.0F)
                .sizeOverLife(0.75F, 0.75F, 2.80F, 2.10F, 1.20F, 0.85F, 0.18F)
                .fixedSizeScale()
                .color(0xFFFDF0, 0.90F)
                .midColor(0xFFB21A, 0.58F)
                .midColorTime(0.22F)
                .endColor(0xFF6A00, 0.12F)
                .shape(ParticleEmitTask.SHAPE_TRIANGLE)
                .motion(ParticleEmitTask.MOTION_RADIAL_DIFFUSION)
                .radialDiffusion(EXPAND_RING_SPAWN_RADIUS_JITTER, EXPAND_RING_VERTICAL_SPEED, EXPAND_RING_VERTICAL_JITTER)
                .material(ParticleMaterialKey.DEFAULT_SDF)
                .rate(0)
                .duration(0.0F)
                .burst(EXPAND_RING_SDF_BURST);
    }

    // 创建半高中心 LIGHT_EFFECT 径向扩散圈。
    public static ParticleEmitTask createExpandRingLightParticle(Vec3 ringCenter) {
        return new ParticleEmitTask()
                .position(ringCenter == null ? Vec3.ZERO : ringCenter)
                .speed(EXPAND_RING_START_SPEED * 0.82F, EXPAND_RING_END_SPEED)
                .speedCurve(EXPAND_RING_SPEED_CURVE)
                .spread(0.20F)
                .life(SHOCKWAVE_LIFE)
                .gravity(0.0F)
                .sizeOverLife(2.20F, 2.20F, 7.80F, 4.60F, 4.80F, 2.60F, 0.20F)
                .fixedSizeScale()
                .color(0xFFFDF0, 0.62F)
                .midColor(0xFFD15A, 0.42F)
                .midColorTime(0.25F)
                .endColor(0xFF8A10, 0.08F)
                .lightEffectMask(0.42F, 0.24F)
                .motion(ParticleEmitTask.MOTION_RADIAL_DIFFUSION)
                .radialDiffusion(EXPAND_RING_SPAWN_RADIUS_JITTER * 1.35F, EXPAND_RING_VERTICAL_SPEED, EXPAND_RING_VERTICAL_JITTER)
                .material(ParticleMaterialKey.LIGHT_EFFECT)
                .rate(0)
                .duration(0.0F)
                .burst(EXPAND_RING_LIGHT_BURST);
    }

    // 返回最外层冲击波底部直径。
    public static float getMaxLayerWidth() {
        float maxWidth = getBaseWidth();
        for (ShockwaveLayer layer : LAYERS) {
            if (layer == null) continue;
            maxWidth = Math.max(maxWidth, layer.width());
        }
        return maxWidth;
    }

    // 返回最外层冲击波高度。
    public static float getMaxLayerHeight() {
        float maxHeight = getBaseHeight();
        for (ShockwaveLayer layer : LAYERS) {
            if (layer == null) continue;
            maxHeight = Math.max(maxHeight, layer.height());
        }
        return maxHeight;
    }

    // 返回底部能量法阵直径。
    public static float getMagicCircleSize() {
        return getMaxLayerWidth() * MAGIC_CIRCLE_SIZE_MULTIPLIER;
    }

    // 返回更大冲击波法阵直径。
    public static float getShockwaveMagicCircleSize() {
        return getMaxLayerWidth() * SHOCKWAVE_MAGIC_CIRCLE_SIZE_MULTIPLIER;
    }

    // 返回底部雾化裙边半径。
    public static float getBottomSkirtRadius() {
        return getMaxLayerWidth() * BOTTOM_SKIRT_RADIUS_SCALE;
    }

    // 返回动态相机抖动半径，覆盖最大剑气距离、圆柱直径和额外缓冲。
    public static float getShakeRadius() {
        return (float) (ExExcaliburConfig.maxRange() + getMaxLayerWidth() + SHOCKWAVE_SHAKE_RADIUS_PADDING);
    }

    // 返回终点冲击波服务端圆柱伤害半径。
    public static double getDamageRadius() {
        return getMaxLayerWidth() * 0.5D + ExExcaliburConfig.damageSidePadding();
    }

    public static float getBaseWidth() {
        return ExExcaliburConfig.endShockwaveBaseWidth();
    }

    public static float getBaseHeight() {
        return ExExcaliburConfig.endShockwaveBaseHeight();
    }

    // ShockwaveLayer 保存单层终点冲击波圆台的视觉参数。
    public static class ShockwaveLayer {
        public float uvTileX; // 主纹理圆周方向平铺倍率。
        public float uvTileY; // 主纹理高度方向平铺倍率。
        public float dissolvePower; // 纹理 RGBA power 溶解参数。
        public float effectPower; // 1-Fresnel 曲线指数。
        public float rangeExpand; // 相对基础宽度的范围增大值。
        public float rotationDegrees; // 圆台整体绕 Y 轴旋转角度。
        public int color; // 单层主体颜色。
        public float alpha; // 单层主体透明度。
        public float uvFlowSpeed; // 主纹理向上流动基础速度。

        public ShockwaveLayer(float uvTileX, float uvTileY, float dissolvePower, float effectPower,
                              float rangeExpand, float rotationDegrees, int color, float alpha, float uvFlowSpeed) {
            this.uvTileX = Math.max(0.001F, uvTileX);
            this.uvTileY = Math.max(0.001F, uvTileY);
            this.dissolvePower = Math.max(0.001F, dissolvePower);
            this.effectPower = Math.max(0.001F, effectPower);
            this.rangeExpand = Math.max(0.0F, rangeExpand);
            this.rotationDegrees = rotationDegrees;
            this.color = color;
            this.alpha = Math.max(0.0F, Math.min(1.0F, alpha));
            this.uvFlowSpeed = uvFlowSpeed;
        }

        // 返回当前层底部直径。
        public float width() {
            return getBaseWidth() + rangeExpand;
        }

        // 返回当前层圆台高度。
        public float height() {
            return getBaseHeight() + rangeExpand * HEIGHT_EXPAND_RATIO;
        }
    }
}
