package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.DimensionSlashConfig;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.DimensionSlashDomainEntity;

public class DimensionSlashScreenEffect {
    public float blueIntensity; // 蓝色重影强度。
    public float grayIntensity; // 灰化强度。
    public float cutIntensity; // 屏幕 Voronoi 碎片错位强度。
    public float cutProgress; // 屏幕 Voronoi 碎片推进进度。
    public float flashIntensity; // 兼容旧 uniform 的爆闪强度，当前配置为 0 禁用白闪。
    public float zoomBlurStrength; // 径向拉伸模糊强度。
    public float contrastBoost; // 灰白高对比强度。
    public float chromaticStrength; // RGB 色散强度。
    public float vignetteStrength; // 边缘暗角压迫强度。
    public float wallStrength; // 领域壁边缘弧面强度。
    public float seed; // 当前效果随机种子。

    public void add(DimensionSlashDomainEntity entity, Camera camera, float partialTick) {
        if (camera == null || entity == null) return;
        Vec3 cameraPos = camera.getPosition();
        double radius = DimensionSlashConfig.RADIUS;
        double distanceSqr = cameraPos.distanceToSqr(entity.position());
        if (distanceSqr > radius * radius) return;
        float distanceFade = 1.0F - (float) Math.sqrt(distanceSqr) / (float) radius;
        float age = entity.getAge() + partialTick;
        float blue = getBlueIntensity(age) * distanceFade;
        float cut = getCutIntensity(age) * distanceFade;
        float gray = getGrayIntensity(age) * distanceFade;
        float flash = getFlashIntensity(age) * distanceFade;
        float zoom = getZoomBlurStrength(age) * distanceFade;
        float contrast = getContrastBoost(age) * distanceFade;
        float chromatic = getChromaticStrength(age) * distanceFade;
        float vignette = getVignetteStrength(age) * distanceFade;
        float wall = getWallStrength(age) * distanceFade;
        blueIntensity = Math.max(blueIntensity, blue);
        cutIntensity = Math.max(cutIntensity, cut);
        grayIntensity = Math.max(grayIntensity, gray);
        flashIntensity = Math.max(flashIntensity, flash);
        zoomBlurStrength = Math.max(zoomBlurStrength, zoom);
        contrastBoost = Math.max(contrastBoost, contrast);
        chromaticStrength = Math.max(chromaticStrength, chromatic);
        vignetteStrength = Math.max(vignetteStrength, vignette);
        wallStrength = Math.max(wallStrength, wall);
        cutProgress = Math.max(cutProgress, getCutProgress(age));
        seed = entity.getVisualSeed();
    }

    public void clearFrame() {
        blueIntensity = 0.0F;
        grayIntensity = 0.0F;
        cutIntensity = 0.0F;
        cutProgress = 0.0F;
        flashIntensity = 0.0F;
        zoomBlurStrength = 0.0F;
        contrastBoost = 0.0F;
        chromaticStrength = 0.0F;
        vignetteStrength = 0.0F;
        wallStrength = 0.0F;
    }

    // 计算蓝色重影强度。
    public float getBlueIntensity(float age) {
        float in = Mth.clamp(age / 10.0F, 0.0F, 1.0F);
        float out = 1.0F - Mth.clamp((age - DimensionSlashConfig.BLUE_GHOST_END_TICK) / 18.0F, 0.0F, 1.0F);
        return in * out * 0.78F;
    }

    // 计算灰化强度。
    public float getGrayIntensity(float age) {
        float burst = Mth.clamp((age - DimensionSlashConfig.BURST_START_TICK) / 10.0F, 0.0F, 1.0F);
        float out = 1.0F - Mth.clamp((age - DimensionSlashConfig.GLASS_END_TICK) / 10.0F, 0.0F, 1.0F);
        return burst * out;
    }

    // 计算屏幕 Voronoi 碎片错位强度。
    public float getCutIntensity(float age) {
        float start = Mth.clamp((age - DimensionSlashConfig.CUT_START_TICK) / 5.0F, 0.0F, 1.0F);
        float end = 1.0F - Mth.clamp((age - DimensionSlashConfig.CUT_END_TICK) / 12.0F, 0.0F, 1.0F);
        return start * end;
    }

    // 计算屏幕 Voronoi 碎片推进进度。
    public float getCutProgress(float age) {
        return (age - DimensionSlashConfig.CUT_START_TICK) / Math.max(1.0F, DimensionSlashConfig.CUT_END_TICK - DimensionSlashConfig.CUT_START_TICK);
    }

    // 计算爆发白闪强度，当前通过 FLASH_STRENGTH=0 关闭。
    public float getFlashIntensity(float age) {
        float in = Mth.clamp((age - DimensionSlashConfig.BURST_START_TICK) / 3.0F, 0.0F, 1.0F);
        float out = 1.0F - Mth.clamp((age - DimensionSlashConfig.BURST_START_TICK - 9.0F) / 13.0F, 0.0F, 1.0F);
        return in * out * DimensionSlashConfig.FLASH_STRENGTH;
    }

    // 计算径向拉伸模糊强度。
    public float getZoomBlurStrength(float age) {
        float in = Mth.clamp((age - DimensionSlashConfig.BURST_START_TICK) / 8.0F, 0.0F, 1.0F);
        float out = 1.0F - Mth.clamp((age - DimensionSlashConfig.BURST_END_TICK) / 10.0F, 0.0F, 1.0F);
        return in * out * DimensionSlashConfig.ZOOM_BLUR_STRENGTH;
    }

    // 计算灰白高对比强度。
    public float getContrastBoost(float age) {
        float in = Mth.clamp((age - DimensionSlashConfig.BURST_START_TICK) / 8.0F, 0.0F, 1.0F);
        float out = 1.0F - Mth.clamp((age - DimensionSlashConfig.GLASS_END_TICK) / 10.0F, 0.0F, 1.0F);
        return in * out * DimensionSlashConfig.CONTRAST_BOOST;
    }

    // 计算蓝紫领域色散强度。
    public float getChromaticStrength(float age) {
        return getBlueIntensity(age) * DimensionSlashConfig.CHROMATIC_STRENGTH;
    }

    // 计算边缘暗角强度。
    public float getVignetteStrength(float age) {
        return getBlueIntensity(age) * DimensionSlashConfig.VIGNETTE_STRENGTH;
    }

    // 计算领域壁弧面强度。
    public float getWallStrength(float age) {
        float in = Mth.clamp(age / 18.0F, 0.0F, 1.0F);
        float out = 1.0F - Mth.clamp((age - DimensionSlashConfig.BLUE_GHOST_END_TICK) / 18.0F, 0.0F, 1.0F);
        return in * out * DimensionSlashConfig.WALL_STRENGTH;
    }

}
