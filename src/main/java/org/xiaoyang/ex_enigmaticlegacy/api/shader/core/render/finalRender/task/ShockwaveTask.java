package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task;

import net.minecraft.world.phys.Vec3;

public class ShockwaveTask implements PostRenderTask {
    public final PostRenderQueueType queueType;
    public final Vec3 center;
    public final Vec3 normal;
    public final float startRadius;
    public final float endRadius;
    public final float growTime;
    public final float holdTime;
    public final float fadeTime;
    public final float width;
    public final long seed;
    public final float alpha;

    public ShockwaveTask(PostRenderQueueType queueType, Vec3 center, Vec3 normal, float startRadius, float endRadius,
                         float growTime, float holdTime, float fadeTime, float width, long seed, float alpha) {
        this.queueType = queueType;
        this.center = center;
        this.normal = normal;
        this.startRadius = startRadius;
        this.endRadius = endRadius;
        this.growTime = growTime;
        this.holdTime = holdTime;
        this.fadeTime = fadeTime;
        this.width = width;
        this.seed = seed;
        this.alpha = alpha;
    }

    public PostRenderQueueType queueType() {
        return queueType;
    }

    public static ShockwaveTask shockwave(Vec3 center, Vec3 normal, float startRadius, float endRadius,
                                          float growTime, float holdTime, float fadeTime, float width, long seed, float alpha) {
        return new ShockwaveTask(PostRenderQueueType.SHOCKWAVE, center, normal, startRadius, endRadius,
                growTime, holdTime, fadeTime, width, seed, alpha);
    }

    public static ShockwaveTask circleShockwave(Vec3 center, Vec3 normal, float startRadius, float endRadius,
                                                float growTime, float holdTime, float fadeTime, float width, long seed, float alpha) {
        return new ShockwaveTask(PostRenderQueueType.CIRCLE_SHOCKWAVE, center, normal, startRadius, endRadius,
                growTime, holdTime, fadeTime, width, seed, alpha);
    }
}
