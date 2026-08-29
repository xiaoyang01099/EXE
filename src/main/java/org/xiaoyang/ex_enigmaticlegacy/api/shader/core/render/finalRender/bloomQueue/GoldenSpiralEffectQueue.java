package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Util.MathUtil;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type.GoldenSpiralRenderType;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.GoldenSpiralShader;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.texture.EXETextureAtlas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GoldenSpiralEffectQueue {
    public static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D); // 世界上方向。
    public static final Vec3 WORLD_RIGHT = new Vec3(1.0D, 0.0D, 0.0D); // 退化时使用的右方向。
    public static final Vec3 WORLD_FORWARD = new Vec3(0.0D, 0.0D, 1.0D); // 退化时使用的前方向。
    public static final int SEGMENTS = 96; // 螺旋分段数量。
    public static final float HEIGHT = 3.2F; // 螺旋总高度。
    public static final float RADIUS = 0.75F; // 螺旋中段半径。
    public static final float TURNS = 2.6F; // 螺旋圈数。
    public static final float WIDTH = 0.18F; // ribbon 半宽。
    public static final float LIFE_TIME = 2.5F; // 光效生命周期。
    public static final float SWIRL_SPEED = 1.2F; // 整体旋转速度。
    public static final float BLOOM_STRENGTH = 1.25F; // Bloom 强度。
    public static final float INTENSITY = 2.4F; // 可见层自发光强度。
    public static final float OPACITY = 0.72F; // 可见层透明度倍率。
    public static final float CORE_R = 1.00F; // 核心金色 R。
    public static final float CORE_G = 0.62F; // 核心金色 G。
    public static final float CORE_B = 0.10F; // 核心金色 B。
    public static final float EDGE_R = 1.00F; // 高亮金色 R。
    public static final float EDGE_G = 0.92F; // 高亮金色 G。
    public static final float EDGE_B = 0.35F; // 高亮金色 B。
    public static final float COLOR_POWER = 1.4F; // 噪声到颜色混合曲线。
    public static final float NOISE1_TILE_X = 1.0F; // 第一张噪声 U 平铺。
    public static final float NOISE1_TILE_Y = 3.0F; // 第一张噪声 V 平铺。
    public static final float NOISE2_TILE_X = 2.2F; // fx_noise015 U 平铺。
    public static final float NOISE2_TILE_Y = 4.5F; // fx_noise015 V 平铺。
    public static final float NOISE3_TILE_X = 1.2F; // 月面噪声 U 平铺。
    public static final float NOISE3_TILE_Y = 1.8F; // 月面噪声 V 平铺。
    public static final float NOISE3_STRENGTH = 0.18F; // 月面噪声 UV 扰动强度。
    public static final float NOISE1_SPEED_X = 0.08F; // 第一张噪声 U 流速。
    public static final float NOISE1_SPEED_Y = -0.65F; // 第一张噪声 V 向上流速。
    public static final float NOISE2_SPEED_X = -0.12F; // 第二张噪声 U 流速。
    public static final float NOISE2_SPEED_Y = -0.42F; // 第二张噪声 V 向上流速。
    public static final float NOISE3_SPEED_X = 0.00F; // 第三张噪声 U 流速。
    public static final float NOISE3_SPEED_Y = -0.35F; // 第三张噪声 V 向上流速。
    public static final float MASK_SCALE_X = 1.0F; // mask X 比例。
    public static final float MASK_SCALE_Y = 1.0F; // mask Y 比例。
    public static final float MASK_RADIUS = 0.92F; // ribbon 宽度圆形 mask 半径。
    public static final float MASK_SOFTNESS = 0.10F; // mask 柔化宽度。
    public static final float BOTTOM_FADE = 0.10F; // 底部淡入比例。
    public static final float TOP_FADE = 0.22F; // 顶部淡出比例。
    public static final float NOISE_CUTOFF_LOW = 0.16F; // 噪声低阈值。
    public static final float NOISE_CUTOFF_HIGH = 0.72F; // 噪声高阈值。
    public static final float TWO_PI = (float) (Math.PI * 2.0D); // Java 侧螺旋角度一整圈弧度。

    public final List<GoldenSpiralEffectData> pendingEffects = new ArrayList<>(); // 本帧新增光效。
    public final List<GoldenSpiralEffectData> activeEffects = new ArrayList<>(); // 跨帧播放光效。
    public TextureAtlasSprite noise1Sprite;
    public TextureAtlasSprite noise2Sprite;
    public TextureAtlasSprite noise3Sprite;

    public void add(Vec3 center, long seed) {
        if (center == null) return;
        pendingEffects.add(new GoldenSpiralEffectData(center, seed));
    }

    public void render(MultiBufferSource.BufferSource fboBuffer, Camera camera, float partialTick, Matrix4f viewMatrix) {
        if (!GoldenSpiralShader.isLoaded()) return;
        if (!activatePending()) return;
        if (EXETextureAtlas.EXE_TOOL_ATLAS == null) return;
        if (!loadSprites()) return;

        float time = MathUtil.getClientTime(partialTick);
        GoldenSpiralShader.setView(viewMatrix);
        GoldenSpiralShader.setSamplers(EXETextureAtlas.EXE_TOOL_ATLAS.getId());
        GoldenSpiralShader.setEffectParams(time, BLOOM_STRENGTH, INTENSITY, OPACITY);
        GoldenSpiralShader.setColorParams(CORE_R, CORE_G, CORE_B, COLOR_POWER);
        GoldenSpiralShader.setEdgeColor(EDGE_R, EDGE_G, EDGE_B, 0.0F);
        GoldenSpiralShader.setNoiseSpriteUVs(noise1Sprite.getU0(), noise1Sprite.getV0(), noise1Sprite.getU1(), noise1Sprite.getV1(),
                noise2Sprite.getU0(), noise2Sprite.getV0(), noise2Sprite.getU1(), noise2Sprite.getV1(),
                noise3Sprite.getU0(), noise3Sprite.getV0(), noise3Sprite.getU1(), noise3Sprite.getV1());
        GoldenSpiralShader.setNoiseParams(NOISE1_TILE_X, NOISE1_TILE_Y, NOISE2_TILE_X, NOISE2_TILE_Y,
                NOISE3_TILE_X, NOISE3_TILE_Y, NOISE3_STRENGTH);
        GoldenSpiralShader.setMaskParams(MASK_SCALE_X, MASK_SCALE_Y, MASK_RADIUS, MASK_SOFTNESS,
                BOTTOM_FADE, TOP_FADE, NOISE_CUTOFF_LOW, NOISE_CUTOFF_HIGH);
        GoldenSpiralShader.setNoiseFlows(NOISE1_SPEED_X, NOISE1_SPEED_Y, 0.0F, 0.0F,
                NOISE2_SPEED_X, NOISE2_SPEED_Y, 0.0F, 0.0F,
                NOISE3_SPEED_X, NOISE3_SPEED_Y, 0.0F, 0.0F);

        VertexConsumer consumer = fboBuffer.getBuffer(GoldenSpiralRenderType.getRenderType());
        Vec3 cameraPos = camera.getPosition();
        Iterator<GoldenSpiralEffectData> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            GoldenSpiralEffectData effect = iterator.next();
            float age = time - effect.spawnTime;
            if (age > effect.lifeTime) {
                iterator.remove();
                continue;
            }
            writeSpiralRibbon(consumer, effect, cameraPos, age);
        }
        fboBuffer.endBatch(GoldenSpiralRenderType.getRenderType());
    }

    public boolean activatePending() {
        if (!pendingEffects.isEmpty()) {
            activeEffects.addAll(pendingEffects);
            pendingEffects.clear();
        }
        return !activeEffects.isEmpty();
    }

    public boolean hasActive() {
        return !pendingEffects.isEmpty() || !activeEffects.isEmpty();
    }

    public void clear() {
        pendingEffects.clear();
        activeEffects.clear();
    }

    public void writeSpiralRibbon(VertexConsumer consumer, GoldenSpiralEffectData effect, Vec3 cameraPos, float age) {
        float ageT = Mth.clamp(age / effect.lifeTime, 0.0F, 1.0F);
        float alpha = lifeFade(ageT);
        if (alpha <= 0.003F) return;

        Vec3[] centers = new Vec3[SEGMENTS + 1];
        Vec3[] leftPoints = new Vec3[SEGMENTS + 1];
        Vec3[] rightPoints = new Vec3[SEGMENTS + 1];
        Vec3 previousSide = WORLD_RIGHT;

        for (int i = 0; i <= SEGMENTS; i++) {
            float t = (float) i / SEGMENTS;
            centers[i] = spiralCenter(effect, t, age);
        }

        for (int i = 0; i <= SEGMENTS; i++) {
            Vec3 center = centers[i];
            Vec3 tangent = sampleTangent(centers, i);
            Vec3 viewDir = safeNormalize(cameraPos.subtract(center), WORLD_FORWARD);
            Vec3 side = safeNormalize(tangent.cross(viewDir), previousSide);

            if (side.dot(previousSide) < 0.0D) {
                side = side.reverse();
            }

            previousSide = side;
            Vec3 scaledSide = side.scale(effect.width);
            leftPoints[i] = center.subtract(scaledSide);
            rightPoints[i] = center.add(scaledSide);
        }

        for (int i = 0; i < SEGMENTS; i++) {
            float t0 = (float) i / SEGMENTS;
            float t1 = (float) (i + 1) / SEGMENTS;
            vertex(consumer, leftPoints[i], 0.0F, t0, alpha);
            vertex(consumer, rightPoints[i], 1.0F, t0, alpha);
            vertex(consumer, rightPoints[i + 1], 1.0F, t1, alpha);
            vertex(consumer, leftPoints[i + 1], 0.0F, t1, alpha);
        }
    }

    public Vec3 sampleTangent(Vec3[] centers, int index) {
        if (centers == null || centers.length < 2) return WORLD_UP;
        if (index <= 0) return safeNormalize(centers[1].subtract(centers[0]), WORLD_UP);
        if (index >= centers.length - 1) return safeNormalize(centers[index].subtract(centers[index - 1]), WORLD_UP);
        return safeNormalize(centers[index + 1].subtract(centers[index - 1]), WORLD_UP);
    }

    public Vec3 spiralCenter(GoldenSpiralEffectData effect, float t, float age) {
        float radiusCurve = Mth.lerp(smoothstep(0.0F, 0.35F, t), 0.45F, 1.0F);
        radiusCurve *= 1.0F - smoothstep(0.82F, 1.0F, t) * 0.35F;
        float radius = effect.radius * radiusCurve;
        float angle = effect.baseRotation + t * effect.turns * TWO_PI + age * SWIRL_SPEED;
        return effect.center.add(Math.cos(angle) * radius, t * effect.height, Math.sin(angle) * radius);
    }

    public float lifeFade(float ageT) {
        float fadeIn = smoothstep(0.0F, 0.08F, ageT);
        float fadeOut = 1.0F - smoothstep(0.78F, 1.0F, ageT);
        return fadeIn * fadeOut;
    }

    public static void vertex(VertexConsumer consumer, Vec3 pos, float u, float v, float alpha) {
        consumer.vertex(pos.x, pos.y, pos.z)
                .uv(u, v)
                .color(1.0F, 1.0F, 1.0F, Mth.clamp(alpha, 0.0F, 1.0F))
                .endVertex();
    }

    public static Vec3 safeNormalize(Vec3 vector, Vec3 fallback) {
        if (vector == null || vector.lengthSqr() < 1.0E-8D) return fallback;
        return vector.normalize();
    }

    public static float smoothstep(float edge0, float edge1, float value) {
        float t = Mth.clamp((value - edge0) / Math.max(edge1 - edge0, 0.0001F), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    public boolean loadSprites() {
        if (noise1Sprite == null) noise1Sprite = EXETextureAtlas.getTextureLocation(EXETextureAtlas.T_FX_TILE_0012_TEXTURE);
        if (noise2Sprite == null) noise2Sprite = EXETextureAtlas.getTextureLocation(EXETextureAtlas.fx_noise015);
        if (noise3Sprite == null) noise3Sprite = EXETextureAtlas.getTextureLocation(EXETextureAtlas.T_FX_TILE_0137_MOON_TEXTURE);
        return noise1Sprite != null && noise2Sprite != null && noise3Sprite != null;
    }

    public static class GoldenSpiralEffectData {
        public final Vec3 center; // 光效中心底部。
        public final long seed; // 随机种子。
        public final float spawnTime; // 生成时刻。
        public final float height; // 螺旋高度。
        public final float radius; // 螺旋半径。
        public final float turns; // 螺旋圈数。
        public final float width; // ribbon 半宽。
        public final float lifeTime; // 生命周期。
        public final float baseRotation; // 初始旋转角。

        public GoldenSpiralEffectData(Vec3 center, long seed) {
            this.center = center;
            this.seed = seed;
            this.spawnTime = MathUtil.getClientTime(0.0F);
            this.height = HEIGHT;
            this.radius = RADIUS;
            this.turns = TURNS;
            this.width = WIDTH;
            this.lifeTime = LIFE_TIME;
            this.baseRotation = unit(seed) * TWO_PI;
        }

        public static float unit(long seed) {
            long mixed = seed ^ (seed >>> 33);
            mixed *= 0xff51afd7ed558ccdL;
            mixed ^= mixed >>> 33;
            return (float) ((mixed & 0xFFFFFFL) / (double) 0x1000000L);
        }
    }
}
