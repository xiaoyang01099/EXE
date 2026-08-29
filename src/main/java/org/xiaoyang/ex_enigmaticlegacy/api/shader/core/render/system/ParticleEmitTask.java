package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.material.ParticleMaterialKey;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.material.ParticleMaterialRegistry;

import java.util.Random;

public class ParticleEmitTask {
    public static final int SHAPE_CIRCLE = 11;
    public static final int SHAPE_HEART = 12;
    public static final int SHAPE_TRIANGLE = 2;
    public static final int SHAPE_SQUARE = 3;
    public static final int SHAPE_STAR = 4;
    public static final int MOTION_BALLISTIC = 0;
    public static final int MOTION_CIRCULAR = 1;
    public static final int MOTION_RADIAL_DIFFUSION = 2;
    public static final int MOTION_DIRECTION_PLANE_RANDOM = 4; // 方向平面随机模式，沿主方向移动并在垂直平面平滑随机摆动。
    public static final int MOTION_TURBULENT_RISE = 5; // 噪声流场上升模式，使用圆盘出生、curl 噪声和径向扩散。
    public static final int MOTION_ARC_DIRECTION = 6; // 专用弧面方向模式，从世界 Y 轴同步旋向目标方向并保留显示。
    public static final int ORBIT_SPAWN_FIXED = 0;
    public static final int ORBIT_SPAWN_RANDOM = 1;
    public static final int ORBIT_SPAWN_RANGE = 2;
    public static final int ORBIT_SPAWN_DISTRIBUTED = 3;

    private static final int[] RANDOM_SHAPES = {
            SHAPE_CIRCLE, SHAPE_SQUARE, SHAPE_TRIANGLE, SHAPE_HEART, SHAPE_STAR
    };
    private static final int[] RAINBOW_COLORS = {
            0xFF3B30, 0xFF9500, 0xFFCC00, 0x34C759,
            0x32ADE6, 0x007AFF, 0xAF52DE
    };

    float posX;
    float posY;
    float posZ;
    float dirX = 0f;
    float dirY = 1f;
    float dirZ = 0f;
    float speed = 1f;
    float startSpeed = 1f; // 生命周期起始速度。
    float endSpeed = 1f; // 生命周期结束速度。
    float speedCurvePower = 1f; // 速度插值曲线指数。
    float directionSign = 1f; // 方向符号，负数表示沿 direction 反向移动。
    float spread = 1f;
    float life = 10f;
    float gravity = 0f;
    float sizeX = 0.1f;
    float sizeY = 0.1f;
    float midSizeX = 0.1f; // 生命周期中间阶段宽度。
    float midSizeY = 0.1f; // 生命周期中间阶段高度。
    float endSizeX = 0.1f; // 生命周期结束阶段宽度。
    float endSizeY = 0.1f; // 生命周期结束阶段高度。
    float midSizeTime = 0.5f; // 中间尺寸在生命周期中的位置。
    boolean fixedSizeScale; // 是否关闭 GPU 出生阶段的随机尺寸倍率。
    float lightEffectMaskRadius = 0.20f; // LIGHT_EFFECT 最终圆形遮罩半径。
    float lightEffectMaskSoftness = 0.18f; // LIGHT_EFFECT 最终圆形遮罩柔边宽度。
    float rotation;
    boolean randomRotation = true; // 是否在 GPU 出生阶段为每个粒子叠加随机旋转角。
    float rotationSpeed; // 生命周期内屏幕空间旋转速度，正数为逆时针。
    float startR = 1f;
    float startG = 1f;
    float startB = 1f;
    float startA = 1f;
    float midR = 1f; // 中间颜色 R。
    float midG = 1f; // 中间颜色 G。
    float midB = 1f; // 中间颜色 B。
    float midA = 0.9f; // 中间颜色 Alpha。
    float endR = 1f;
    float endG = 1f;
    float endB = 1f;
    float endA = 0.8f;
    float midColorTime = 0.5f; // 中间颜色在生命周期中的位置。
    boolean midColorExplicit; // 是否显式设置过中间颜色。
    int shapeType = SHAPE_CIRCLE;
    int motionType = MOTION_BALLISTIC;
    int materialId = ParticleMaterialRegistry.DEFAULT_SDF_ID;
    float orbitRadius = 2f;
    float angularSpeed = 2f;
    float verticalSpeed;
    float orbitPhase;
    float orbitPhaseRange = (float) (Math.PI * 2.0);
    int orbitSpawnMode = ORBIT_SPAWN_RANDOM;
    float orbitPlanePitch;
    float orbitPlaneYaw;
    float orbitPlaneRoll;
    float orbitPlanePitchRange;
    float orbitPlaneYawRange;
    float orbitPlaneRollRange;
    float radialSpawnRadiusJitter = 0.08f;
    float radialVerticalSpeed = 0.04f;
    float radialVerticalSpeedJitter = 0.05f;
    float turbulentSpawnRadius = 0.20f; // 噪声上升模式出生圆盘半径。
    float turbulentNoiseScale = 1.00f; // 噪声上升模式空间噪声频率。
    float turbulentCurlStrength = 0.30f; // 噪声上升模式局部卷曲强度。
    float turbulentRadialExpansion = 0.10f; // 噪声上升模式随生命周期向外扩散强度。
    float turbulentNoiseSpeed = 0.85f; // 噪声上升模式时间推进速度。
    float turbulentSpawnHeightMin = -0.04f; // 噪声上升模式沿主方向的最小出生高度偏移。
    float turbulentSpawnHeightMax = 0.04f; // 噪声上升模式沿主方向的最大出生高度偏移。
    float risingShockwavePower = 1.35f; // 上升冲击波 1-Fresnel 曲线指数。
    float risingShockwaveDissolvePower = 1.80f; // 上升冲击波纹理 RGBA 溶解 power。
    float risingShockwaveUvTileX = 2.0f; // 上升冲击波圆周方向 UV 平铺倍率。
    float risingShockwaveUvTileY = 3.5f; // 上升冲击波高度方向 UV 平铺倍率。
    float risingShockwaveUvFlowSpeed = 0.65f; // 上升冲击波纹理向上流动基础速度。
    int rate = 10;  //每秒发射数量。
    float duration = 10f;
    int burstCount;
    float elapsed;
    float emitAccumulator;
    boolean burstEmitted;
    boolean removed;

    public ParticleEmitTask position(float x, float y, float z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        return this;
    }
    public ParticleEmitTask position(BlockPos pos) {
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
        return this;
    }

    public ParticleEmitTask position(Vec3 pos) {
        this.posX = (float) pos.x();
        this.posY = (float) pos.y();
        this.posZ = (float) pos.z();
        return this;
    }

    public ParticleEmitTask direction(float x, float y, float z) {
        float len = (float) Math.sqrt(x * x + y * y + z * z);
        if (len > 0.0001f) {
            this.dirX = x / len;
            this.dirY = y / len;
            this.dirZ = z / len;
        } else {
            this.dirX = 0f;
            this.dirY = 1f;
            this.dirZ = 0f;
        }
        return this;
    }

    public ParticleEmitTask speed(float speed) {
        this.speed = speed;
        this.startSpeed = speed;
        this.endSpeed = speed;
        return this;
    }

    public ParticleEmitTask speed(float startSpeed, float endSpeed) {
        this.speed = startSpeed;
        this.startSpeed = startSpeed;
        this.endSpeed = endSpeed;
        return this;
    }

    // 设置速度插值曲线，1 为线性，大于 1 前期更慢，小于 1 前期更快。
    public ParticleEmitTask speedCurve(float speedCurvePower) {
        this.speedCurvePower = Math.max(0.05f, speedCurvePower);
        return this;
    }

    // 控制粒子沿 direction 正向或反向运动，速度值本身保持非负。
    public ParticleEmitTask reverseDirection(boolean reverse) {
        this.directionSign = reverse ? -1.0f : 1.0f;
        return this;
    }

    public ParticleEmitTask spread(float spread) {
        this.spread = spread;
        return this;
    }

    public ParticleEmitTask life(float life) {
        this.life = life;
        return this;
    }

    public ParticleEmitTask gravity(float gravity) {
        this.gravity = gravity;
        return this;
    }

    public ParticleEmitTask size(float sizeX, float sizeY, float rotation) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.midSizeX = sizeX;
        this.midSizeY = sizeY;
        this.endSizeX = sizeX;
        this.endSizeY = sizeY;
        this.rotation = rotation;
        return this;
    }

    // 单独设置粒子出生阶段尺寸。
    public ParticleEmitTask startSize(float sizeX, float sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        return this;
    }

    // 单独设置粒子生命周期中间阶段尺寸。
    public ParticleEmitTask midSize(float sizeX, float sizeY) {
        this.midSizeX = sizeX;
        this.midSizeY = sizeY;
        return this;
    }

    // 单独设置粒子生命周期结束阶段尺寸。
    public ParticleEmitTask endSize(float sizeX, float sizeY) {
        this.endSizeX = sizeX;
        this.endSizeY = sizeY;
        return this;
    }

    // 设置中间尺寸出现的生命周期比例。
    public ParticleEmitTask midSizeTime(float midSizeTime) {
        this.midSizeTime = Math.max(0.001f, Math.min(0.999f, midSizeTime));
        return this;
    }

    // 一次设置出生、中间、结束三段尺寸及中间时间点。
    public ParticleEmitTask sizeOverLife(float startSizeX, float startSizeY,
                                         float midSizeX, float midSizeY,
                                         float endSizeX, float endSizeY,
                                         float midSizeTime) {
        return startSize(startSizeX, startSizeY)
                .midSize(midSizeX, midSizeY)
                .endSize(endSizeX, endSizeY)
                .midSizeTime(midSizeTime);
    }

    // 固定所有粒子的旋转角，不再在 GPU 出生阶段叠加随机角度。
    public ParticleEmitTask fixedRotation(float rotation) {
        this.rotation = rotation;
        this.randomRotation = false;
        return this;
    }

    // 设置粒子生命周期内围绕自身中心的屏幕空间旋转速度，单位 rad/s。
    public ParticleEmitTask rotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
        return this;
    }

    // 关闭 GPU 出生阶段的 0.55～1.15 随机倍率，让三段尺寸严格使用调用值。
    public ParticleEmitTask fixedSizeScale() {
        this.fixedSizeScale = true;
        return this;
    }

    // 同时设置 LIGHT_EFFECT 最终圆形遮罩半径和柔边宽度。
    public ParticleEmitTask lightEffectMask(float radius, float softness) {
        return lightEffectMaskRadius(radius).lightEffectMaskSoftness(softness);
    }

    // 设置 LIGHT_EFFECT 最终圆形遮罩半径，最大值覆盖到四边形角点。
    public ParticleEmitTask lightEffectMaskRadius(float radius) {
        this.lightEffectMaskRadius = Math.max(0.001f, Math.min(0.707f, radius));
        return this;
    }

    // 设置 LIGHT_EFFECT 最终圆形遮罩柔边宽度。
    public ParticleEmitTask lightEffectMaskSoftness(float softness) {
        this.lightEffectMaskSoftness = Math.max(0.001f, Math.min(0.707f, softness));
        return this;
    }

    // 一次设置上升冲击波材质粒子的菲尼尔、溶解、UV 平铺和流动速度参数。
    public ParticleEmitTask risingShockwave(float effectPower, float dissolvePower, float uvTileX, float uvTileY, float uvFlowSpeed) {
        return risingShockwavePower(effectPower)
                .risingShockwaveDissolve(dissolvePower)
                .risingShockwaveUv(uvTileX, uvTileY, uvFlowSpeed);
    }

    // 设置上升冲击波 1-Fresnel 的曲线指数，值越大正对相机的区域越集中。
    public ParticleEmitTask risingShockwavePower(float effectPower) {
        this.risingShockwavePower = Math.max(0.001f, effectPower);
        return this;
    }

    // 设置上升冲击波纹理 RGBA 的 power 溶解参数。
    public ParticleEmitTask risingShockwaveDissolve(float dissolvePower) {
        this.risingShockwaveDissolvePower = Math.max(0.001f, dissolvePower);
        return this;
    }

    // 设置上升冲击波主纹理 X/Y 平铺倍率和向上流动基础速度。
    public ParticleEmitTask risingShockwaveUv(float tileX, float tileY, float flowSpeed) {
        this.risingShockwaveUvTileX = Math.max(0.001f, tileX);
        this.risingShockwaveUvTileY = Math.max(0.001f, tileY);
        this.risingShockwaveUvFlowSpeed = flowSpeed;
        return this;
    }

    public ParticleEmitTask color(float r, float g, float b, float a) {
        this.startR = r;
        this.startG = g;
        this.startB = b;
        this.startA = a;
        refreshDefaultMidColor();
        return this;
    }

    public ParticleEmitTask color(int rgb, float a) {
        return color(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, a);
    }



    public ParticleEmitTask endColor(float r, float g, float b, float a) {
        this.endR = r;
        this.endG = g;
        this.endB = b;
        this.endA = a;
        refreshDefaultMidColor();
        return this;
    }

    public ParticleEmitTask endColor(int rgb, float a) {
        return endColor(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, a);
    }

    // 设置中间颜色，让粒子从 start -> mid -> end 三段渐变。
    public ParticleEmitTask midColor(float r, float g, float b, float a) {
        this.midR = r;
        this.midG = g;
        this.midB = b;
        this.midA = a;
        this.midColorExplicit = true;
        return this;
    }

    public ParticleEmitTask midColor(int rgb, float a) {
        return midColor(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, a);
    }

    // 设置中间颜色出现的位置，0.5 表示生命周期中点。
    public ParticleEmitTask midColorTime(float midColorTime) {
        this.midColorTime = Math.max(0.001f, Math.min(0.999f, midColorTime));
        return this;
    }

    // 未显式设置中间颜色时，自动保持 start/end 的中间值以兼容旧调用。
    public void refreshDefaultMidColor() {
        if (midColorExplicit) return;
        this.midR = (startR + endR) * 0.5f;
        this.midG = (startG + endG) * 0.5f;
        this.midB = (startB + endB) * 0.5f;
        this.midA = (startA + endA) * 0.5f;
    }
    public ParticleEmitTask shape(int shapeType) {
        this.shapeType = shapeType;
        return this;
    }

    // 设置粒子材质，材质会决定渲染阶段使用的纹理、噪声和 Shader 批次。
    public ParticleEmitTask material(ParticleMaterialKey key) {
        this.materialId = ParticleMaterialRegistry.idOf(key);
        return this;
    }

    // 返回材质 ID，供 GPU 发射任务写入 EmitJob SSBO。
    public int materialId() {
        return materialId;
    }

    // 随机选择一种已有粒子形状。
    public ParticleEmitTask randomShape(RandomSource random) {
        return shape(RANDOM_SHAPES[random.nextInt(RANDOM_SHAPES.length)]);
    }
    public ParticleEmitTask randomShape(Random random) {
        return shape(RANDOM_SHAPES[random.nextInt(RANDOM_SHAPES.length)]);
    }

    // 按七彩颜色循环设置出生和结束渐变颜色。
    public ParticleEmitTask rainbowColor(int index, float startAlpha, float endAlpha) {
        int start = RAINBOW_COLORS[Math.floorMod(index, RAINBOW_COLORS.length)];
        int end = RAINBOW_COLORS[Math.floorMod(index + 1, RAINBOW_COLORS.length)];
        return color(start, startAlpha).endColor(end, endAlpha);
    }

    public ParticleEmitTask motion(int motionType) {
        this.motionType = motionType;
        return this;
    }

    public ParticleEmitTask orbit(float radius, float angularSpeed, float verticalSpeed) {
        this.orbitRadius = radius;
        this.angularSpeed = angularSpeed;
        this.verticalSpeed = verticalSpeed;
        return this;
    }

    // 沿 direction 主方向移动，并在垂直于 direction 的两个方向做连续随机摆动。
    public ParticleEmitTask directionPlaneRandom(float amplitude, float frequency, float speed) {
        this.motionType = MOTION_DIRECTION_PLANE_RANDOM;
        this.orbitRadius = Math.max(0.0f, amplitude);
        this.angularSpeed = Math.max(0.0f, frequency);
        this.verticalSpeed = Math.max(0.0f, speed);
        return this;
    }

    // 从世界 Y 轴旋向 direction 目标方向，arc/hold/fade 控制劈落、保留和淡出阶段秒数。
    public ParticleEmitTask arcDirection(float beamLength, float arcDuration, float holdDuration, float fadeDuration) {
        this.motionType = MOTION_ARC_DIRECTION;
        this.orbitRadius = Math.max(0.001f, beamLength);
        this.angularSpeed = Math.max(0.001f, holdDuration);
        this.verticalSpeed = Math.max(0.001f, fadeDuration);
        this.startSpeed = Math.max(0.001f, arcDuration);
        this.endSpeed = Math.max(0.0f, holdDuration);
        this.speedCurvePower = Math.max(0.001f, fadeDuration);
        return this;
    }

    // 使用圆盘出生、curl 噪声速度场和生命周期径向扩散，生成随机上升的体积粒子。
    public ParticleEmitTask turbulentRise(float spawnRadius, float noiseScale, float curlStrength, float radialExpansion) {
        return turbulentRise(spawnRadius, noiseScale, curlStrength, radialExpansion, turbulentNoiseSpeed);
    }

    // 使用可调噪声速度的随机上升模式，适合模拟 UE5 Niagara Curl Noise Force 风格的粒子。
    public ParticleEmitTask turbulentRise(float spawnRadius, float noiseScale, float curlStrength, float radialExpansion, float noiseSpeed) {
        this.motionType = MOTION_TURBULENT_RISE;
        this.turbulentSpawnRadius = Math.max(0.0f, spawnRadius);
        this.turbulentNoiseScale = Math.max(0.001f, noiseScale);
        this.turbulentCurlStrength = Math.max(0.0f, curlStrength);
        this.turbulentRadialExpansion = Math.max(0.0f, radialExpansion);
        this.turbulentNoiseSpeed = Math.max(0.001f, noiseSpeed);
        return this;
    }

    // 设置噪声上升粒子沿主方向的随机出生高度范围，偏移量相对任务 position。
    public ParticleEmitTask turbulentSpawnHeight(float minOffset, float maxOffset) {
        this.turbulentSpawnHeightMin = Math.min(minOffset, maxOffset);
        this.turbulentSpawnHeightMax = Math.max(minOffset, maxOffset);
        return this;
    }

    public ParticleEmitTask orbitPhase(float phaseRadians) {
        this.orbitPhase = phaseRadians;
        this.orbitSpawnMode = ORBIT_SPAWN_FIXED;
        return this;
    }

    public ParticleEmitTask orbitPhaseRandom(float phaseRangeRadians) {
        this.orbitPhaseRange = phaseRangeRadians;
        this.orbitSpawnMode = ORBIT_SPAWN_RANDOM;
        return this;
    }

    public ParticleEmitTask orbitPhaseRange(float startRadians, float endRadians) {
        this.orbitPhase = startRadians;
        this.orbitPhaseRange = endRadians;
        this.orbitSpawnMode = ORBIT_SPAWN_RANGE;
        return this;
    }

    public ParticleEmitTask orbitSpawnMode(int orbitSpawnMode) {
        this.orbitSpawnMode = orbitSpawnMode;
        return this;
    }

    public ParticleEmitTask orbitPlane(float pitchRadians, float yawRadians, float rollRadians) {
        this.orbitPlanePitch = pitchRadians;
        this.orbitPlaneYaw = yawRadians;
        this.orbitPlaneRoll = rollRadians;
        return this;
    }

    public ParticleEmitTask orbitPlaneRandom(float pitchRangeRadians, float yawRangeRadians, float rollRangeRadians) {
        this.orbitPlanePitchRange = pitchRangeRadians;
        this.orbitPlaneYawRange = yawRangeRadians;
        this.orbitPlaneRollRange = rollRangeRadians;
        return this;
    }

    // 配置径向扩散的出生半径扰动和向上速度扰动，保持一个任务批量生成完整圆形扩散。
    public ParticleEmitTask radialDiffusion(float spawnRadiusJitter, float verticalSpeed, float verticalSpeedJitter) {
        this.radialSpawnRadiusJitter = spawnRadiusJitter;
        this.radialVerticalSpeed = verticalSpeed;
        this.radialVerticalSpeedJitter = verticalSpeedJitter;
        return this;
    }

    // 每秒发射数量。
    public ParticleEmitTask rate(int rate) {
        this.rate = rate;
        return this;
    }

    public ParticleEmitTask duration(float duration) {
        this.duration = duration;
        return this;
    }

    public ParticleEmitTask burst(int burstCount) {
        this.burstCount = burstCount;
        return this;
    }

    int consumeEmitCount(float dt) {
        if (removed) {
            return 0;
        }

        elapsed += dt;
        int count = 0;

        if (burstCount > 0 && !burstEmitted) {
            count += burstCount;
            burstEmitted = true;
        }

        if (rate > 0 && (duration < 0f || elapsed <= duration)) {
            emitAccumulator += rate * dt;
            int continuousCount = (int) emitAccumulator;
            if (continuousCount > 0) {
                count += continuousCount;
                emitAccumulator -= continuousCount;
            }
        }

        if (duration >= 0f && elapsed > duration && (burstCount <= 0 || burstEmitted)) {
            removed = true;
        }

        return count;
    }

    boolean isDead() {
        return removed;
    }
}
