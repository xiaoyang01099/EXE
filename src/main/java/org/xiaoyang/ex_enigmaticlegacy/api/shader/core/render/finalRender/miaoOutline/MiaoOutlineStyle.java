package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline;

import org.joml.Vector4f;

public class MiaoOutlineStyle {
    public static final MiaoOutlineStyle AUTO_TRACKING_RED = create(Kind.AUTO_TRACKING_RED); // 自动追踪红色 Miao 描边样式。
    public static final MiaoOutlineStyle SPARKLING_FRUIT_FIRE = create(Kind.SPARKLING_FRUIT_FIRE); // 闪闪果实火焰 Miao 描边样式。

    public enum Kind {
        AUTO_TRACKING_RED, // 普通自动追踪描边，不使用噪声扰动。
        SPARKLING_FRUIT_FIRE // 闪闪果实 buff 火焰描边，使用噪声扰动和火焰渐变色。
    }

    public final Kind kind; // Miao 描边类型，后续新增类型时每个类型返回独立参数。
    public final float outlinePixels; // 描边宽度，单位为屏幕像素。
    public final int radialSamples; // 径向采样数量，shader 内会限制到安全范围。
    public final float edgeSoftness; // 边缘强度放大系数。
    public final float depthThreshold; // 归一化深度差阈值，用于判断深度边缘。
    public final float depthRange; // view-space 深度归一化范围。
    public final float maskThreshold; // 目标 mask 判定阈值。
    public final float noiseSpeedX; // 噪声横向流速。
    public final float noiseSpeedY; // 噪声纵向流速。
    public final float noiseIntensity; // 噪声基础强度，当前保留给调试和兼容说明。
    public final float noiseTiling; // 噪声平铺倍数。
    public final float distanceFadeStart; // 描边距离衰减开始的归一化深度。
    public final float distanceFadeEnd; // 描边距离衰减结束的归一化深度。
    public final float distanceMinScale; // 描边距离衰减后的最小宽度倍率。
    public final boolean distanceFadeEnabled; // 是否启用按目标深度缩小描边宽度。
    public final float fireColorSpeed; // 火焰渐变颜色随时间流动速度。
    public final float fireGradientEdgeScale; // edge 对火焰渐变坐标的影响。
    public final float fireGradientNoiseScale; // 噪声对火焰渐变坐标的影响。
    public final float fireColorIntensity; // 火焰颜色整体输出强度。
    public final float fireEdgeNoiseStrength; // 火焰边缘明暗跳动强度。
    public final float fireEdgeCutoff; // 火焰边缘断裂基础阈值。
    public final float fireEdgeBreakup; // 火焰噪声造成边缘断裂的强度。
    public final float fireEdgeSoftness; // 火焰断裂边缘柔和宽度。
    public final float fireWarpIntensity; // 火焰边缘采样 UV 位移强度，对应 UE5 NoiseIntensity。
    public final float fireWarpMix; // 火焰 clean edge 与 warped edge 的混合比例。
    public final float fireWarpDirectionScale; // 火焰噪声双向位移倍率。
    public final Vector4f visibleColor; // 写入 CA0 的可见描边颜色。
    public final Vector4f bloomColor; // 写入 CA1 的 bloom 描边颜色。

    public MiaoOutlineStyle(Kind kind, float outlinePixels, int radialSamples, float edgeSoftness,
                            float depthThreshold, float depthRange, float maskThreshold,
                            float noiseSpeedX, float noiseSpeedY, float noiseIntensity, float noiseTiling,
                            float distanceFadeStart, float distanceFadeEnd, float distanceMinScale, boolean distanceFadeEnabled,
                            float fireColorSpeed, float fireGradientEdgeScale, float fireGradientNoiseScale, float fireColorIntensity,
                            float fireEdgeNoiseStrength, float fireEdgeCutoff, float fireEdgeBreakup, float fireEdgeSoftness,
                            float fireWarpIntensity, float fireWarpMix, float fireWarpDirectionScale,
                            Vector4f visibleColor, Vector4f bloomColor) {
        this.kind = kind;
        this.outlinePixels = outlinePixels;
        this.radialSamples = radialSamples;
        this.edgeSoftness = edgeSoftness;
        this.depthThreshold = depthThreshold;
        this.depthRange = depthRange;
        this.maskThreshold = maskThreshold;
        this.noiseSpeedX = noiseSpeedX;
        this.noiseSpeedY = noiseSpeedY;
        this.noiseIntensity = noiseIntensity;
        this.noiseTiling = noiseTiling;
        this.distanceFadeStart = distanceFadeStart;
        this.distanceFadeEnd = distanceFadeEnd;
        this.distanceMinScale = distanceMinScale;
        this.distanceFadeEnabled = distanceFadeEnabled;
        this.fireColorSpeed = fireColorSpeed;
        this.fireGradientEdgeScale = fireGradientEdgeScale;
        this.fireGradientNoiseScale = fireGradientNoiseScale;
        this.fireColorIntensity = fireColorIntensity;
        this.fireEdgeNoiseStrength = fireEdgeNoiseStrength;
        this.fireEdgeCutoff = fireEdgeCutoff;
        this.fireEdgeBreakup = fireEdgeBreakup;
        this.fireEdgeSoftness = fireEdgeSoftness;
        this.fireWarpIntensity = fireWarpIntensity;
        this.fireWarpMix = fireWarpMix;
        this.fireWarpDirectionScale = fireWarpDirectionScale;
        this.visibleColor = visibleColor;
        this.bloomColor = bloomColor;
    }

    public static MiaoOutlineStyle create(Kind kind) {
        Kind safeKind = kind == null ? Kind.AUTO_TRACKING_RED : kind;
        if (safeKind == Kind.SPARKLING_FRUIT_FIRE) {
            return createSparklingFire();
        }
        return createAutoTrackingRed();
    }

    public static MiaoOutlineStyle createAutoTrackingRed() {
        return new MiaoOutlineStyle(
                Kind.AUTO_TRACKING_RED,
                2.0f,
                4,
                1.0f,
                0.008f,
                128.0f,
                1.1f,
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                0.18f,
                0.75f,
                0.45f,
                true,
                0.0f,
                0.0f,
                0.0f,
                1.0f,
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                1.0f,
                new Vector4f(1.0f, 0.08f, 0.08f, 0.90f),
                new Vector4f(1.0f, 0.15f, 0.15f, 0.65f)
        );
    }

    public static MiaoOutlineStyle createSparklingFire() {
        return new MiaoOutlineStyle(
                Kind.SPARKLING_FRUIT_FIRE,
                4.0f,
                6,
                1.15f,
                0.0f,
                128.0f,
                1.1f,
                0.1f,
                -1.5f,
                1.012f,
                8.0f,
                0.16f,
                0.72f,
                0.55f,
                true,
                0.18f,
                0.0f,
                0.35f,
                1.15f,
                1.35f,
                0.12f,
                0.45f,
                0.18f,
                0.012f,
                0.65f,
                0.5f,
                new Vector4f(1.0f, 0.18f, 0.08f, 0.90f),
                new Vector4f(1.0f, 0.42f, 0.12f, 0.85f)
        );
    }
}
