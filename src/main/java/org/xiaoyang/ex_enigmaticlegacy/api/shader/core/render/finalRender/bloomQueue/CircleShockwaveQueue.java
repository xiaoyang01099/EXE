package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Util.MathUtil;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type.CircleShockwaveRenderType;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.CircleShockwaveShader;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.texture.EXETextureAtlas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CircleShockwaveQueue {
    public static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D); // 世界上方向，用于构造固定法阵平面。
    public static final Vec3 WORLD_RIGHT = new Vec3(1.0D, 0.0D, 0.0D); // 世界右方向，用于法线接近垂直时兜底。
    public static final Vec3 WORLD_FORWARD = new Vec3(0.0D, 0.0D, 1.0D); // 世界前方向，用于固定平面第二轴兜底。
    public static final float MIN_TIME = 0.01F; // 最小生命周期时间，避免除零。
    public static final float MIN_RADIUS = 0.05F; // 最小半径，避免 billboard 退化。
    public static final float DEFAULT_BLOOM_STRENGTH = 0.55F; // 法阵冲击波写入 bloom 源的默认强度。
    public static final float DEFAULT_RESERVED_RADIAL_SCALE = 0.0F; // 旧径向倍率槽位，现已由 RadialParams.xy 拆分替代。
    public static final float DEFAULT_ANGLE_SCALE = 5.0F; // 控制圆周方向纹理精度。
    public static final float DEFAULT_RADIUS_SCALE = 2.0F; // 控制半径方向圆环数量。
    public static final float DEFAULT_RADIUS_NORMALIZE = 2.0F; // VectorToRadialValue 半径归一化倍率。
    public static final float DEFAULT_ANGLE_OFFSET = 0.0F; // 径向采样角度偏移，用于旋转纹理方向。
    public static final float DEFAULT_UV_OFFSET_X = 2.0F; // 法阵冲击波材质横向偏移。
    public static final float DEFAULT_TIME_SPEED = -0.5F; // 法阵冲击波材质时间滚动速度。
    public static final float DEFAULT_UV_OFFSET_Y = 0.0F; // 法阵冲击波材质预留纵向偏移。
    public static final float DEFAULT_EDGE_FADE_START = 0.42F; // billboard 外缘开始柔化的位置。
    public static final float DEFAULT_EDGE_FADE_END = 0.50F; // billboard 外缘完全淡出的位置。
    public static final float DEFAULT_OPACITY_SCALE = 1.0F; // 法阵冲击波全局透明度倍率。
    public static final float DEFAULT_VISIBLE_START = 0.32F; // 径向图案开始出现的位置。
    public static final float DEFAULT_VISIBLE_END = 0.50F; // 径向图案柔和结束的位置。
    public static final float DEFAULT_VISIBLE_START_SOFTNESS = 0.06F; // 径向内侧出现柔化宽度。
    public static final float DEFAULT_VISIBLE_END_SOFTNESS = 0.18F; // 径向外侧结束柔化宽度。
    public static final float DEFAULT_INTENSITY = 2.4F; // 法阵冲击波自发光强度。
    public static final float DEFAULT_NOISE_PANNER_SPEED_X = 0.025F; // 法阵噪声图横向滚动速度，降低后减少单方向漂移感。
    public static final float DEFAULT_NOISE_STRENGTH = 0.50F; // 法阵噪声 R 通道扰动强度，降低后减少纹理被拉糊。
    public static final long TIME_SPEED_RANDOM_SALT = 0x4A17C12C0FFEE22L; // 动画速度随机扰动盐值，避免和普通冲击波同步。
    public static final float TINT_R = 0.45F; // 法阵冲击波紫蓝基调 R。
    public static final float TINT_G = 0.35F; // 法阵冲击波紫蓝基调 G。
    public static final float TINT_B = 2.40F; // 法阵冲击波紫蓝基调 B。
    public static final int BILLBOARD_GRID_SIZE = 8; // 固定法阵平面每边十六等分，最终写入 16x16 个四边形。
    public final List<CircleShockwaveData> pendingCircleShockwaves = new ArrayList<>(); // 本帧新增法阵冲击波。
    public final List<CircleShockwaveData> activeCircleShockwaves = new ArrayList<>(); // 跨帧播放法阵冲击波。
    public TextureAtlasSprite circleShockwaveSprite; // trail_3 法阵冲击波纹理 sprite。
    public TextureAtlasSprite circleNoiseSprite; // noise_002_256x 法阵 UV 扰动噪声 sprite。

    public void add(Vec3 center, Vec3 normal, float startRadius, float endRadius,
                    float growTime, float holdTime, float fadeTime, float width, long seed, float alpha) {
        if (center == null) return;
        pendingCircleShockwaves.add(new CircleShockwaveData(center, normal == null ? WORLD_UP : normal, seed,
                startRadius, endRadius, growTime, holdTime, fadeTime, width, alpha));
    }

    public void render(MultiBufferSource.BufferSource fboBuffer, Camera camera, float partialTick, Matrix4f viewMatrix) {
        if (!CircleShockwaveShader.isLoaded()) return;
        if (!activatePending()) return;
        if (EXETextureAtlas.EXE_TOOL_ATLAS == null) return;

        TextureAtlasSprite sprite = getCircleShockwaveSprite();
        if (sprite == null) return;
        TextureAtlasSprite noiseSprite = getCircleNoiseSprite();
        if (noiseSprite == null) return;

        float time = MathUtil.getClientTime(partialTick);

        CircleShockwaveShader.setEffectParams(time, DEFAULT_BLOOM_STRENGTH, DEFAULT_RESERVED_RADIAL_SCALE, DEFAULT_INTENSITY);
        CircleShockwaveShader.setTintParams(TINT_R, TINT_G, TINT_B, 0.0F);
        CircleShockwaveShader.setRadialParams(DEFAULT_ANGLE_SCALE, DEFAULT_RADIUS_SCALE, DEFAULT_RADIUS_NORMALIZE, DEFAULT_ANGLE_OFFSET);
        CircleShockwaveShader.setUvAnimParams(DEFAULT_UV_OFFSET_X, DEFAULT_TIME_SPEED, DEFAULT_UV_OFFSET_Y, 0.0F);
        CircleShockwaveShader.setShapeParams(DEFAULT_EDGE_FADE_START, DEFAULT_EDGE_FADE_END, DEFAULT_OPACITY_SCALE, 0.0F);
        CircleShockwaveShader.setRevealParams(DEFAULT_VISIBLE_START, DEFAULT_VISIBLE_END, DEFAULT_VISIBLE_START_SOFTNESS, DEFAULT_VISIBLE_END_SOFTNESS);
        CircleShockwaveShader.setShockwaveSpriteUV(sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
        CircleShockwaveShader.setNoiseParams(DEFAULT_NOISE_PANNER_SPEED_X, DEFAULT_NOISE_STRENGTH, 0.0F, 0.0F);
        CircleShockwaveShader.setCircleNoiseSpriteUV(noiseSprite.getU0(), noiseSprite.getV0(), noiseSprite.getU1(), noiseSprite.getV1());
        CircleShockwaveShader.setView(viewMatrix);
        CircleShockwaveShader.setSamplers(EXETextureAtlas.EXE_TOOL_ATLAS.getId());

        VertexConsumer consumer = fboBuffer.getBuffer(CircleShockwaveRenderType.getRenderType());
        Iterator<CircleShockwaveData> iterator = activeCircleShockwaves.iterator();
        while (iterator.hasNext()) {
            CircleShockwaveData circleShockwave = iterator.next();
            float age = time - circleShockwave.spawnTime;
            if (age > circleShockwave.totalTime()) {
                iterator.remove();
                continue;
            }
            writeCircleShockwavePlane(consumer, circleShockwave, age);
        }
        fboBuffer.endBatch(CircleShockwaveRenderType.getRenderType());
    }

    public boolean activatePending() {
        if (!pendingCircleShockwaves.isEmpty()) {
            activeCircleShockwaves.addAll(pendingCircleShockwaves);
            pendingCircleShockwaves.clear();
        }
        return !activeCircleShockwaves.isEmpty();
    }

    public boolean hasActive() {
        return !pendingCircleShockwaves.isEmpty() || !activeCircleShockwaves.isEmpty();
    }

    public void clear() {
        pendingCircleShockwaves.clear();
        activeCircleShockwaves.clear();
    }

    public void writeCircleShockwavePlane(VertexConsumer consumer, CircleShockwaveData circleShockwave, float age) {
        float reveal = circleShockwave.reveal(age);
        float alpha = circleShockwave.alpha(age);
        if (reveal <= 0.01F || alpha <= 0.003F) return;

        float radius = Mth.lerp(reveal, circleShockwave.startRadius, circleShockwave.endRadius);
        Vec3 normal = safeNormalize(circleShockwave.normal, WORLD_UP);
        Vec3 tangentSeed = Math.abs(normal.dot(WORLD_UP)) > 0.95D ? WORLD_RIGHT : WORLD_UP;
        Vec3 right = safeNormalize(tangentSeed.cross(normal), WORLD_RIGHT);
        Vec3 up = safeNormalize(normal.cross(right), WORLD_FORWARD);
        Vec3 r = right.scale(radius);
        Vec3 u = up.scale(radius);

        float finalAlpha = alpha * circleShockwave.alphaScale;

        for (int y = 0; y < BILLBOARD_GRID_SIZE; y++) {
            float v0 = (float) y / BILLBOARD_GRID_SIZE;
            float v1 = (float) (y + 1) / BILLBOARD_GRID_SIZE;
            for (int x = 0; x < BILLBOARD_GRID_SIZE; x++) {
                float u0 = (float) x / BILLBOARD_GRID_SIZE;
                float u1 = (float) (x + 1) / BILLBOARD_GRID_SIZE;
                Vec3 p0 = planePoint(circleShockwave.center, r, u, u0, v0);
                Vec3 p1 = planePoint(circleShockwave.center, r, u, u1, v0);
                Vec3 p2 = planePoint(circleShockwave.center, r, u, u1, v1);
                Vec3 p3 = planePoint(circleShockwave.center, r, u, u0, v1);
                vertex(consumer, p0, u0, v0, finalAlpha, circleShockwave.timeSpeedRandom);
                vertex(consumer, p1, u1, v0, finalAlpha, circleShockwave.timeSpeedRandom);
                vertex(consumer, p2, u1, v1, finalAlpha, circleShockwave.timeSpeedRandom);
                vertex(consumer, p3, u0, v1, finalAlpha, circleShockwave.timeSpeedRandom);
            }
        }
    }

    public static Vec3 planePoint(Vec3 center, Vec3 rightRadius, Vec3 upRadius, float u, float v) {
        double sx = u * 2.0D - 1.0D;
        double sy = v * 2.0D - 1.0D;
        return center.add(rightRadius.scale(sx)).add(upRadius.scale(sy));
    }

    public static void vertex(VertexConsumer consumer, Vec3 pos, float u, float v, float alpha, float timeSpeedRandom) {
        int packedTimeSpeedRandom = Mth.clamp(Math.round(Mth.clamp(timeSpeedRandom, 0.0F, 1.0F) * 255.0F), 0, 255);
        consumer.vertex(pos.x, pos.y, pos.z)
                .uv(u, v)
                .color(1.0F, 1.0F, 1.0F, Mth.clamp(alpha, 0.0F, 1.0F))
                .uv2(packedTimeSpeedRandom, 0)
                .endVertex();
    }

    public static Vec3 safeNormalize(Vec3 vector, Vec3 fallback) {
        if (vector == null || vector.lengthSqr() < 1.0E-8D) return fallback;
        return vector.normalize();
    }

    public TextureAtlasSprite getCircleShockwaveSprite() {
        if (circleShockwaveSprite == null) circleShockwaveSprite = EXETextureAtlas.getTextureLocation(EXETextureAtlas.CIRCLE_SHOCKWAVE_TEXTURE);
        return circleShockwaveSprite;
    }

    public TextureAtlasSprite getCircleNoiseSprite() {
        if (circleNoiseSprite == null) circleNoiseSprite = EXETextureAtlas.getTextureLocation(EXETextureAtlas.noise_002_128x);
        return circleNoiseSprite;
    }

    public static class CircleShockwaveData {
        public final Vec3 center; // 法阵冲击波中心点。
        public final Vec3 normal; // 预留法线参数，后续用于让法阵冲击波贴合法阵平面。
        public final long seed; // 随机种子，预留给后续材质随机参数。
        public final float spawnTime; // 生成时刻。
        public final float startRadius; // 起始半径。
        public final float endRadius; // 结束半径。
        public final float growTime; // 扩散时间。
        public final float holdTime; // 扩散完成后的保持时间。
        public final float fadeTime; // 淡出时间。
        public final float width; // 预留宽度参数，后续可做法阵边缘柔化。
        public final float alphaScale; // 透明度倍率。
        public final float timeSpeedRandom; // 单个法阵冲击波动画速度随机值，shader 侧映射到速度倍率。

        public CircleShockwaveData(Vec3 center, Vec3 normal, long seed, float startRadius, float endRadius,
                                   float growTime, float holdTime, float fadeTime, float width, float alphaScale) {
            RandomSource random = RandomSource.create(seed ^ TIME_SPEED_RANDOM_SALT);
            this.center = center;
            this.normal = normal;
            this.seed = seed;
            this.spawnTime = MathUtil.getClientTime(0.0F);
            this.startRadius = Math.max(MIN_RADIUS, startRadius);
            this.endRadius = Math.max(this.startRadius, endRadius);
            this.growTime = Math.max(MIN_TIME, growTime);
            this.holdTime = Math.max(0.0F, holdTime);
            this.fadeTime = Math.max(MIN_TIME, fadeTime);
            this.width = Math.max(0.0F, width);
            this.alphaScale = Mth.clamp(alphaScale, 0.0F, 1.0F);
            this.timeSpeedRandom = random.nextFloat();
        }

        public float totalTime() {
            return growTime + holdTime + fadeTime;
        }

        public float reveal(float age) {
            return Mth.clamp(age / growTime, 0.0F, 1.0F);
        }

        public float alpha(float age) {
            if (age <= growTime + holdTime) return 1.0F;
            return 1.0F - Mth.clamp((age - growTime - holdTime) / fadeTime, 0.0F, 1.0F);
        }
    }
}
