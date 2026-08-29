package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Util.MathUtil;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task.ExcaliburSpiralTask;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type.GoldenSpiralRenderType;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.GoldenSpiralShader;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.texture.EXETextureAtlas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcaliburSpiralQueue {
    public static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D); // 世界上方向。
    public static final Vec3 WORLD_RIGHT = new Vec3(1.0D, 0.0D, 0.0D); // 退化时使用的右方向。
    public static final Vec3 WORLD_FORWARD = new Vec3(0.0D, 0.0D, 1.0D); // 世界前方向。
    public static final int SEGMENTS = 96; // 咖喱棒螺旋分段数量，参考 testitem 金色螺旋。
    public static final int SPAWN_INTERVAL_TICKS = 2; // 每隔多少 tick 生成一批短螺旋。
    public static final int RIBBONS_PER_SPAWN = 5; // 每批生成的短螺旋数量。
    public static final int MAX_ACTIVE_RIBBONS = 20; // 同屏最多保留的短螺旋数量。
    public static final float RIBBON_LIFE_MIN_SECONDS = 0.35F; // 单条螺旋最短生命周期秒数。
    public static final float RIBBON_LIFE_MAX_SECONDS = 0.75F; // 单条螺旋最长生命周期秒数。
    public static final float HEIGHT_MIN = 2.55F; // 单条螺旋最小高度。
    public static final float HEIGHT_MAX = 8.65F; // 单条螺旋最大高度。
    public static final float RADIUS_MIN = 0.42F; // 单条螺旋最小半径。
    public static final float RADIUS_MAX = 0.95F; // 单条螺旋最大半径。
    public static final float TURNS_MIN = 1.75F; // 单条螺旋最少圈数。
    public static final float TURNS_MAX = 3.35F; // 单条螺旋最多圈数。
    public static final float WIDTH_MIN = 0.08F; // 单条 ribbon 最小半宽。
    public static final float WIDTH_MAX = 0.18F; // 单条 ribbon 最大半宽。
    public static final float SPEED_MIN = 7.65F; // 单条螺旋最小秒单位旋转速度。
    public static final float SPEED_MAX = 15.20F; // 单条螺旋最大秒单位旋转速度。
    public static final float PITCH_STRETCH_MIN = 0.00F; // 单条螺旋最小圈距拉长强度。
    public static final float PITCH_STRETCH_MAX = 0.55F; // 单条螺旋最大圈距拉长强度。
    public static final float ALPHA_MIN = 0.78F; // 单条螺旋最小透明度倍率。
    public static final float ALPHA_MAX = 1.0F; // 单条螺旋最大透明度倍率。
    public static final float REVERSE_CHANCE = 0.20F; // 反向旋转短螺旋概率。
    public static final float BLOOM_STRENGTH = 2.25F; // Bloom 强度。
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
    public final List<ExcaliburSpiralTask> tasks = new ArrayList<>(); // 当前帧咖喱棒螺旋任务。
    public final List<ExcaliburSpiralRibbon> activeRibbons = new ArrayList<>(); // 当前存活的短生命周期螺旋。
    public TextureAtlasSprite noise1Sprite;
    public TextureAtlasSprite noise2Sprite;
    public TextureAtlasSprite noise3Sprite;

    public void add(ExcaliburSpiralTask task) {
        if (task == null || task.anchor == null) return;
        tasks.add(task);
    }

    public void render(MultiBufferSource.BufferSource fboBuffer, Camera camera, float partialTick, Matrix4f viewMatrix) {
        if (tasks.isEmpty() && activeRibbons.isEmpty()) return;
        if (!GoldenSpiralShader.isLoaded()) {
            clear();
            return;
        }
        if (EXETextureAtlas.EXE_TOOL_ATLAS == null || !loadSprites()) {
            clear();
            return;
        }

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
        for (ExcaliburSpiralTask task : tasks) {
            spawnRibbons(task, time);
        }
        renderRibbons(consumer, cameraPos, time);
        fboBuffer.endBatch(GoldenSpiralRenderType.getRenderType());
        tasks.clear();
    }

    public boolean hasActive() {
        return !tasks.isEmpty() || !activeRibbons.isEmpty();
    }

    public void clear() {
        tasks.clear();
        activeRibbons.clear();
    }

    public void spawnRibbons(ExcaliburSpiralTask task, float time) {
        if (task == null || task.anchor == null || task.released) return;
        int currentStep = Math.max(0, (int) Math.floor(task.ageTicks / SPAWN_INTERVAL_TICKS));
        if (hasSpawnedStep(task.seed, currentStep)) return;

        for (int i = 0; i < RIBBONS_PER_SPAWN; i++) {
            long ribbonSeed = task.seed ^ (long) currentStep * 0x9E3779B97F4A7C15L ^ (long) i * 0xBF58476D1CE4E5B9L;
            activeRibbons.add(ExcaliburSpiralRibbon.create(task.seed, currentStep, ribbonSeed, task.anchor, time));
        }
        trimRibbons();
    }

    public boolean hasSpawnedStep(long ownerSeed, int spawnStep) {
        for (ExcaliburSpiralRibbon ribbon : activeRibbons) {
            if (ribbon.ownerSeed == ownerSeed && ribbon.spawnStep == spawnStep) return true;
        }
        return false;
    }

    public void trimRibbons() {
        while (activeRibbons.size() > MAX_ACTIVE_RIBBONS) {
            activeRibbons.remove(0);
        }
    }

    public void renderRibbons(VertexConsumer consumer, Vec3 cameraPos, float time) {
        Iterator<ExcaliburSpiralRibbon> iterator = activeRibbons.iterator();
        while (iterator.hasNext()) {
            ExcaliburSpiralRibbon ribbon = iterator.next();
            float lifeT = Mth.clamp((time - ribbon.spawnTimeSeconds) / ribbon.lifeSeconds, 0.0F, 1.0F);
            if (lifeT >= 1.0F) {
                iterator.remove();
                continue;
            }
            writeRibbon(consumer, ribbon, cameraPos, time, lifeT);
        }
    }

    public void writeRibbon(VertexConsumer consumer, ExcaliburSpiralRibbon ribbon, Vec3 cameraPos, float time, float lifeT) {
        float alpha = shortLifeFade(lifeT) * ribbon.alphaScale;
        if (alpha <= 0.003F) return;

        Vec3[] centers = new Vec3[SEGMENTS + 1];
        Vec3[] leftPoints = new Vec3[SEGMENTS + 1];
        Vec3[] rightPoints = new Vec3[SEGMENTS + 1];
        Vec3 previousSide = WORLD_RIGHT;
        float ribbonAgeSeconds = Math.max(0.0F, time - ribbon.spawnTimeSeconds);

        for (int i = 0; i <= SEGMENTS; i++) {
            float t = (float) i / SEGMENTS;
            centers[i] = spiralCenter(ribbon, t, ribbonAgeSeconds);
        }

        for (int i = 0; i <= SEGMENTS; i++) {
            Vec3 center = centers[i];
            Vec3 tangent = sampleTangent(centers, i);
            Vec3 viewDir = safeNormalize(cameraPos.subtract(center), WORLD_FORWARD);
            Vec3 side = safeNormalize(tangent.cross(viewDir), previousSide);
            if (side.dot(previousSide) < 0.0D) side = side.reverse();
            previousSide = side;
            leftPoints[i] = center.subtract(side.scale(ribbon.width));
            rightPoints[i] = center.add(side.scale(ribbon.width));
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

    public Vec3 spiralCenter(ExcaliburSpiralRibbon ribbon, float t, float ageSeconds) {
        float radiusCurve = Mth.lerp(smoothstep(0.0F, 0.35F, t), 0.45F, 1.0F);
        radiusCurve *= 1.0F - smoothstep(0.82F, 1.0F, t) * 0.35F;
        float stretchedT = pitchT(t, ribbon.pitchStretch);
        float angleT = pitchAngleT(t, ribbon.pitchStretch);
        float radius = ribbon.radius * radiusCurve;
        float angle = ribbon.phase + ribbon.angleOffset + angleT * ribbon.turns * TWO_PI + ageSeconds * ribbon.speed;
        return ribbon.anchor.add(Math.cos(angle) * radius, stretchedT * ribbon.height, Math.sin(angle) * radius);
    }

    public float shortLifeFade(float lifeT) {
        float fadeIn = smoothstep(0.0F, 0.18F, lifeT);
        float fadeOut = 1.0F - smoothstep(0.58F, 1.0F, lifeT);
        return fadeIn * fadeOut;
    }

    public float pitchT(float t, float stretch) {
        float curve = t * t * (3.0F - 2.0F * t);
        return Mth.lerp(Mth.clamp(stretch, 0.0F, 1.0F), t, curve);
    }

    public float pitchAngleT(float t, float stretch) {
        return t / (1.0F + Mth.clamp(stretch, 0.0F, 1.0F) * t);
    }

    public Vec3 sampleTangent(Vec3[] centers, int index) {
        if (centers == null || centers.length < 2) return WORLD_UP;
        if (index <= 0) return safeNormalize(centers[1].subtract(centers[0]), WORLD_UP);
        if (index >= centers.length - 1) return safeNormalize(centers[index].subtract(centers[index - 1]), WORLD_UP);
        return safeNormalize(centers[index + 1].subtract(centers[index - 1]), WORLD_UP);
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

    public static float randomRange(long seed, int salt, float min, float max) {
        return Mth.lerp(random01(seed, salt), min, max);
    }

    public static float random01(long seed, int salt) {
        long mixed = seed + (long) salt * 0x9E3779B97F4A7C15L;
        mixed ^= mixed >>> 33;
        mixed *= 0xff51afd7ed558ccdL;
        mixed ^= mixed >>> 33;
        return (float) ((mixed & 0xFFFFFFL) / (double) 0x1000000L);
    }

    public boolean loadSprites() {
        if (noise1Sprite == null) noise1Sprite = EXETextureAtlas.getTextureLocation(EXETextureAtlas.T_FX_TILE_0012_TEXTURE);
        if (noise2Sprite == null) noise2Sprite = EXETextureAtlas.getTextureLocation(EXETextureAtlas.fx_noise015);
        if (noise3Sprite == null) noise3Sprite = EXETextureAtlas.getTextureLocation(EXETextureAtlas.T_FX_TILE_0137_MOON_TEXTURE);
        return noise1Sprite != null && noise2Sprite != null && noise3Sprite != null;
    }

    public static class ExcaliburSpiralRibbon {
        public final long ownerSeed; // 所属咖喱棒蓄力视觉种子。
        public final int spawnStep; // 所属生成步。
        public final long seed; // 单条螺旋随机种子。
        public final Vec3 anchor; // 单条螺旋生成时的玩家中心锚点。
        public final float spawnTimeSeconds; // 单条螺旋生成时客户端秒时间。
        public final float lifeSeconds; // 单条螺旋生命周期秒数。
        public final float height; // 单条螺旋高度。
        public final float radius; // 单条螺旋半径。
        public final float turns; // 单条螺旋圈数。
        public final float width; // 单条 ribbon 半宽。
        public final float speed; // 单条螺旋秒单位旋转速度。
        public final float phase; // 单条螺旋初始相位。
        public final float pitchStretch; // 单条螺旋逐渐拉长强度。
        public final float angleOffset; // 单条螺旋整体横向错位角。
        public final float alphaScale; // 单条螺旋透明度倍率。

        public ExcaliburSpiralRibbon(long ownerSeed, int spawnStep, long seed, Vec3 anchor, float spawnTimeSeconds,
                                     float lifeSeconds, float height, float radius, float turns, float width,
                                     float speed, float phase, float pitchStretch, float angleOffset, float alphaScale) {
            this.ownerSeed = ownerSeed;
            this.spawnStep = spawnStep;
            this.seed = seed;
            this.anchor = anchor;
            this.spawnTimeSeconds = spawnTimeSeconds;
            this.lifeSeconds = lifeSeconds;
            this.height = height;
            this.radius = radius;
            this.turns = turns;
            this.width = width;
            this.speed = speed;
            this.phase = phase;
            this.pitchStretch = pitchStretch;
            this.angleOffset = angleOffset;
            this.alphaScale = alphaScale;
        }

        public static ExcaliburSpiralRibbon create(long ownerSeed, int spawnStep, long seed, Vec3 anchor, float spawnTimeSeconds) {
            float direction = random01(seed, 9) < REVERSE_CHANCE ? -1.0F : 1.0F;
            return new ExcaliburSpiralRibbon(ownerSeed, spawnStep, seed, anchor, spawnTimeSeconds,
                    randomRange(seed, 0, RIBBON_LIFE_MIN_SECONDS, RIBBON_LIFE_MAX_SECONDS),
                    randomRange(seed, 1, HEIGHT_MIN, HEIGHT_MAX),
                    randomRange(seed, 2, RADIUS_MIN, RADIUS_MAX),
                    randomRange(seed, 3, TURNS_MIN, TURNS_MAX),
                    randomRange(seed, 4, WIDTH_MIN, WIDTH_MAX),
                    randomRange(seed, 5, SPEED_MIN, SPEED_MAX) * direction,
                    randomRange(seed, 6, 0.0F, TWO_PI),
                    randomRange(seed, 7, PITCH_STRETCH_MIN, PITCH_STRETCH_MAX),
                    randomRange(seed, 10, 0.0F, TWO_PI),
                    randomRange(seed, 8, ALPHA_MIN, ALPHA_MAX));
        }
    }
}
