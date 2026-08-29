package org.xiaoyang.ex_enigmaticlegacy.Entity.others.trident;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue.CoinLightningQueue;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

// TridentLightningSplashEffects 集中提交天雷战戟和天雷法阵共用的落地溅射雷电视觉。
public class TridentLightningSplashEffects {
    public static final float RANDOM_SPLASH_INVERTED_V_CHANCE = 0.30F; // 统一随机溅射选择倒 V 的概率。
    public static final int RANDOM_SPLASH_MIN_COUNT = 3; // 统一随机溅射最少数量。
    public static final int RANDOM_SPLASH_RANDOM_COUNT = 3; // 统一随机溅射额外随机数量。
    public static final float RANDOM_SPLASH_WIDTH_SCALE = 0.86F; // 统一随机溅射宽度倍率。
    public static final float RANDOM_SPLASH_JITTER_SCALE = 0.50F; // 统一随机溅射路径抖动倍率。
    public static final float INVERTED_V_GROW_TIME_SCALE = 1.45F; // 倒 V 溅射显现时间倍率，放慢出现速度。
    public static final float INVERTED_V_HOLD_TIME_SCALE = 1.10F; // 倒 V 溅射保持时间倍率。
    public static final float INVERTED_V_FADE_TIME_SCALE = 1.15F; // 倒 V 溅射淡出时间倍率。
    public static final int INVERTED_V_CLUSTER_MIN_COUNT = 3; // 倒 V 裂纹簇最少方向数量。
    public static final int INVERTED_V_CLUSTER_RANDOM_COUNT = 3; // 倒 V 裂纹簇额外随机方向数量。
    public static final int INVERTED_V_PER_CLUSTER_MIN_COUNT = 1; // 每个裂纹簇最少电弧数量。
    public static final int INVERTED_V_PER_CLUSTER_RANDOM_COUNT = 2; // 每个裂纹簇额外随机电弧数量。
    public static final double INVERTED_V_CLUSTER_ANGLE_SPREAD = 0.38D; // 同一裂纹簇内角度散布，越大越散。
    public static final float INVERTED_V_FULL_SHAPE_CHANCE = 0.45F; // 每条裂纹使用完整倒 V 三段结构的概率。
    public static final float INVERTED_V_SHORT_BRANCH_CHANCE = 0.35F; // 每条裂纹追加短分支的概率。
    public static final double INVERTED_V_SHORT_BRANCH_LENGTH_SCALE = 0.45D; // 短分支长度倍率。
    public static final int UPWARD_SPLASH_BOLT_MIN_COUNT = 5; // 普通向上溅射短雷最少数量。
    public static final int UPWARD_SPLASH_BOLT_RANDOM_COUNT = 4; // 普通向上溅射短雷额外随机数量。
    public static final int OUTWARD_INVERTED_V_MIN_COUNT = 8; // 向外倒 V 溅射电弧最少数量。
    public static final int OUTWARD_INVERTED_V_RANDOM_COUNT = 5; // 向外倒 V 溅射电弧额外随机数量。
    public static final double UPWARD_RADIUS_MIN = 1.4D; // 向上溅射短雷最小水平半径。
    public static final double UPWARD_RADIUS_RANDOM = 3.4D; // 向上溅射短雷额外随机水平半径。
    public static final double UPWARD_HEIGHT_MIN = 1.0D; // 向上溅射短雷最小高度。
    public static final double UPWARD_HEIGHT_RANDOM = 2.5D; // 向上溅射短雷额外随机高度。
    public static final double OUTWARD_RADIUS_MIN = 2.0D; // 普通外扩溅射短雷最小水平半径。
    public static final double OUTWARD_RADIUS_RANDOM = 3.5D; // 普通外扩溅射短雷额外随机水平半径。
    public static final double OUTWARD_HEIGHT_MIN = 0.25D; // 普通外扩溅射短雷最小高度。
    public static final double OUTWARD_HEIGHT_RANDOM = 1.10D; // 普通外扩溅射短雷额外随机高度。
    public static final double INVERTED_V_START_Y_OFFSET = 0.08D; // 倒 V 从落点上方少量抬起，避免和地面深度冲突。
    public static final double INVERTED_V_MID_RADIUS_MIN = 1.5D; // 倒 V 顶部过渡段距离落点的最小水平半径，数值越大转角越钝。
    public static final double INVERTED_V_MID_RADIUS_RANDOM = 1.6D; // 倒 V 顶部过渡段距离落点的额外随机水平半径。
    public static final double INVERTED_V_END_RADIUS_MIN = 3.0D; // 倒 V 终点距离落点的最小水平半径。
    public static final double INVERTED_V_END_RADIUS_RANDOM = 3.2D; // 倒 V 终点距离落点的额外随机水平半径。
    public static final double INVERTED_V_SIDE_SPREAD = 0.75D; // 倒 V 终点左右散布，避免所有电弧径向完全一致。
    public static final double INVERTED_V_APEX_HEIGHT_MIN = 0.45D; // 倒 V 顶部过渡段最小高度，压低后避免溅射雷过高。
    public static final double INVERTED_V_APEX_HEIGHT_RANDOM = 1.35D; // 倒 V 顶部过渡段额外随机高度。
    public static final double INVERTED_V_CORNER_LENGTH_MIN = 0.45D; // 倒 V 顶部过渡段最短长度，用短横段替代尖锐单点。
    public static final double INVERTED_V_CORNER_LENGTH_RANDOM = 0.55D; // 倒 V 顶部过渡段额外随机长度。
    public static final float SPLASH_GROW_TIME_MIN = 0.035F; // 溅射雷最短显现时间。
    public static final float SPLASH_GROW_TIME_RANDOM = 0.085F; // 溅射雷额外随机显现时间。
    public static final float SPLASH_HOLD_TIME_MIN = 0.015F; // 溅射雷最短保持时间。
    public static final float SPLASH_HOLD_TIME_RANDOM = 0.055F; // 溅射雷额外随机保持时间。
    public static final float SPLASH_FADE_TIME_MIN = 0.07F; // 溅射雷最短淡出时间。
    public static final float SPLASH_FADE_TIME_RANDOM = 0.13F; // 溅射雷额外随机淡出时间。
    public static final float UPWARD_WIDTH_MIN = 0.35F; // 向上短雷最小宽度。
    public static final float UPWARD_WIDTH_RANDOM = 0.50F; // 向上短雷额外随机宽度。
    public static final float INVERTED_V_WIDTH_MIN = 0.35F; // 倒 V 短雷最小宽度。
    public static final float INVERTED_V_WIDTH_RANDOM = 0.40F; // 倒 V 短雷额外随机宽度。
    public static final float NOISE_STRENGTH_MIN = 0.08F; // 溅射雷噪声扰动强度下限。
    public static final float NOISE_STRENGTH_RANDOM = 0.10F; // 溅射雷噪声扰动强度额外随机范围。

    public TridentLightningSplashEffects() {
    }

    // 提交统一随机落地溅射，战戟和法阵都通过这里按 30% 概率选择倒 V。
    public static void submitRandomSplash(Vec3 hitPos, boolean enhanced, long seed) {
        submitRandomSplash(hitPos, null, enhanced, seed, RANDOM_SPLASH_MIN_COUNT, RANDOM_SPLASH_RANDOM_COUNT,
                RANDOM_SPLASH_WIDTH_SCALE, RANDOM_SPLASH_JITTER_SCALE);
    }

    // 提交可固定颜色的统一随机落地溅射，用于法阵同一批落雷保持主雷和溅射同色。
    public static void submitRandomSplash(Vec3 hitPos, TridentLightningColorStyle fixedStyle, boolean enhanced, long seed,
                                          int minCount, int randomCount, float widthScale, float jitterScale) {
        submitRandomSplash(hitPos, fixedStyle, enhanced, seed, minCount, randomCount, widthScale, jitterScale,
                1.0D, 1.0D, 1.0F, 1.0F, 1.0F);
    }

    // 提交带独立范围和生命周期倍率的统一随机落地溅射，中心大型主雷可单独放大和延长显示。
    public static void submitRandomSplash(Vec3 hitPos, TridentLightningColorStyle fixedStyle, boolean enhanced, long seed,
                                          int minCount, int randomCount, float widthScale, float jitterScale,
                                          double rangeScale, double heightScale, float growTimeScale, float holdTimeScale, float fadeTimeScale) {
        submitRandomSplash(hitPos, fixedStyle, enhanced, seed, minCount, randomCount, minCount, randomCount, widthScale, jitterScale,
                rangeScale, heightScale, growTimeScale, holdTimeScale, fadeTimeScale);
    }

    // 提交带倒 V 专用数量的统一随机溅射，中心大型主雷可减少倒 V 数量避免触手感。
    public static void submitRandomSplash(Vec3 hitPos, TridentLightningColorStyle fixedStyle, boolean enhanced, long seed,
                                          int minCount, int randomCount, int invertedVMinCount, int invertedVRandomCount,
                                          float widthScale, float jitterScale, double rangeScale, double heightScale,
                                          float growTimeScale, float holdTimeScale, float fadeTimeScale) {
        RandomSource random = RandomSource.create(seed);
        if (random.nextFloat() < RANDOM_SPLASH_INVERTED_V_CHANCE) {
            submitOutwardInvertedVSplash(hitPos, fixedStyle, enhanced, seed ^ 0x71D1A77EL, invertedVMinCount, invertedVRandomCount, widthScale, jitterScale,
                    rangeScale, heightScale, growTimeScale, holdTimeScale, fadeTimeScale);
            return;
        }
        submitOutwardSplash(hitPos, fixedStyle, enhanced, seed ^ 0x51A5B017L, minCount, randomCount, widthScale, jitterScale,
                rangeScale, heightScale, growTimeScale, holdTimeScale, fadeTimeScale);
    }

    // 提交默认数量的向上随机短雷，主要用于战戟普通落雷落地溅射。
    public static void submitUpwardSplash(Vec3 hitPos, boolean enhanced, long seed) {
        submitUpwardSplash(hitPos, enhanced, seed, UPWARD_SPLASH_BOLT_MIN_COUNT, UPWARD_SPLASH_BOLT_RANDOM_COUNT, 1.0F, 0.55F);
    }

    // 提交可调数量的向上随机短雷，法阵可用较小数量复用同一套随机颜色和随机时序逻辑。
    public static void submitUpwardSplash(Vec3 hitPos, boolean enhanced, long seed, int minCount, int randomCount, float widthScale, float jitterScale) {
        submitUpwardSplash(hitPos, null, enhanced, seed, minCount, randomCount, widthScale, jitterScale);
    }

    // 提交可传入固定颜色的向上随机短雷，法阵同一批落雷可以保持主雷和溅射同色。
    public static void submitUpwardSplash(Vec3 hitPos, TridentLightningColorStyle fixedStyle, boolean enhanced, long seed, int minCount, int randomCount, float widthScale, float jitterScale) {
        if (Exe.POST == null) return;
        RandomSource random = RandomSource.create(seed);
        int count = minCount + random.nextInt(Math.max(1, randomCount));
        Vec3 start = hitPos.add(0.0D, INVERTED_V_START_Y_OFFSET, 0.0D);
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = UPWARD_RADIUS_MIN + random.nextDouble() * UPWARD_RADIUS_RANDOM;
            double height = UPWARD_HEIGHT_MIN + random.nextDouble() * UPWARD_HEIGHT_RANDOM;
            Vec3 dir = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
            Vec3 end = hitPos.add(dir.scale(radius)).add(0.0D, height, 0.0D);
            long boltSeed = seed + i * 7919L;
            TridentLightningColorStyle style = fixedStyle == null ? TridentLightningColorStyle.pick(random, enhanced) : fixedStyle;
            submitPath(start, end, boltSeed, style, random, UPWARD_WIDTH_MIN, UPWARD_WIDTH_RANDOM, widthScale, jitterScale);
        }
    }

    // 提交普通外扩溅射短雷，方向贴近地面向外炸开，适合法阵大范围落雷。
    public static void submitOutwardSplash(Vec3 hitPos, boolean enhanced, long seed, int minCount, int randomCount, float widthScale, float jitterScale) {
        submitOutwardSplash(hitPos, null, enhanced, seed, minCount, randomCount, widthScale, jitterScale);
    }

    // 提交可传入固定颜色的普通外扩溅射短雷，保证同一落雷点视觉颜色统一。
    public static void submitOutwardSplash(Vec3 hitPos, TridentLightningColorStyle fixedStyle, boolean enhanced, long seed, int minCount, int randomCount, float widthScale, float jitterScale) {
        submitOutwardSplash(hitPos, fixedStyle, enhanced, seed, minCount, randomCount, widthScale, jitterScale,
                1.0D, 1.0D, 1.0F, 1.0F, 1.0F);
    }

    // 提交带独立范围和生命周期倍率的普通外扩溅射短雷，中心大型主雷用它拉大半径和停留时间。
    public static void submitOutwardSplash(Vec3 hitPos, TridentLightningColorStyle fixedStyle, boolean enhanced, long seed,
                                           int minCount, int randomCount, float widthScale, float jitterScale,
                                           double rangeScale, double heightScale, float growTimeScale, float holdTimeScale, float fadeTimeScale) {
        if (Exe.POST == null) return;
        RandomSource random = RandomSource.create(seed);
        int count = minCount + random.nextInt(Math.max(1, randomCount));
        Vec3 start = hitPos.add(0.0D, INVERTED_V_START_Y_OFFSET, 0.0D);
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = (OUTWARD_RADIUS_MIN + random.nextDouble() * OUTWARD_RADIUS_RANDOM) * rangeScale;
            double height = (OUTWARD_HEIGHT_MIN + random.nextDouble() * OUTWARD_HEIGHT_RANDOM) * heightScale;
            Vec3 dir = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
            Vec3 end = hitPos.add(dir.scale(radius)).add(0.0D, height, 0.0D);
            long boltSeed = seed + i * 6841L;
            TridentLightningColorStyle style = fixedStyle == null ? TridentLightningColorStyle.pick(random, enhanced) : fixedStyle;
            submitPath(start, end, boltSeed, style, random, UPWARD_WIDTH_MIN, UPWARD_WIDTH_RANDOM, widthScale, jitterScale,
                    growTimeScale, holdTimeScale, fadeTimeScale);
        }
    }

    // 提交默认数量的向外倒 V 溅射，主要用于战戟强化落雷落地扩散。
    public static void submitOutwardInvertedVSplash(Vec3 hitPos, boolean enhanced, long seed) {
        submitOutwardInvertedVSplash(hitPos, enhanced, seed, OUTWARD_INVERTED_V_MIN_COUNT, OUTWARD_INVERTED_V_RANDOM_COUNT, 1.0F, 0.45F);
    }

    // 提交从落点向外扩散的倒 V 溅射，每个 V 随机颜色和时序。
    public static void submitOutwardInvertedVSplash(Vec3 hitPos, boolean enhanced, long seed, int minCount, int randomCount, float widthScale, float jitterScale) {
        submitOutwardInvertedVSplash(hitPos, null, enhanced, seed, minCount, randomCount, widthScale, jitterScale);
    }

    // 提交可传入固定颜色的倒 V 溅射，每个 V 保持随机时序但颜色跟随同一批落雷。
    public static void submitOutwardInvertedVSplash(Vec3 hitPos, TridentLightningColorStyle fixedStyle, boolean enhanced, long seed, int minCount, int randomCount, float widthScale, float jitterScale) {
        submitOutwardInvertedVSplash(hitPos, fixedStyle, enhanced, seed, minCount, randomCount, widthScale, jitterScale,
                1.0D, 1.0D, 1.0F, 1.0F, 1.0F);
    }

    // 提交带独立范围和生命周期倍率的倒 V 溅射，中心大型主雷可放大 V 形铺开范围和显示时间。
    public static void submitOutwardInvertedVSplash(Vec3 hitPos, TridentLightningColorStyle fixedStyle, boolean enhanced, long seed,
                                                    int minCount, int randomCount, float widthScale, float jitterScale,
                                                    double rangeScale, double heightScale, float growTimeScale, float holdTimeScale, float fadeTimeScale) {
        if (Exe.POST == null) return;
        RandomSource random = RandomSource.create(seed);
        int count = minCount + random.nextInt(Math.max(1, randomCount));
        Vec3 start = hitPos.add(0.0D, INVERTED_V_START_Y_OFFSET, 0.0D);
        int clusterCount = Math.min(count, INVERTED_V_CLUSTER_MIN_COUNT + random.nextInt(Math.max(1, INVERTED_V_CLUSTER_RANDOM_COUNT)));
        int submitted = 0;
        for (int cluster = 0; cluster < clusterCount && submitted < count; cluster++) {
            double baseAngle = random.nextDouble() * Math.PI * 2.0D;
            int arcCount = INVERTED_V_PER_CLUSTER_MIN_COUNT + random.nextInt(Math.max(1, INVERTED_V_PER_CLUSTER_RANDOM_COUNT));
            for (int arc = 0; arc < arcCount && submitted < count; arc++) {
                double angle = baseAngle + (random.nextDouble() * 2.0D - 1.0D) * INVERTED_V_CLUSTER_ANGLE_SPREAD;
                submitClusteredInvertedVArc(hitPos, start, fixedStyle, enhanced, seed, random, submitted, angle,
                        widthScale, jitterScale, rangeScale, heightScale, growTimeScale, holdTimeScale, fadeTimeScale);
                submitted++;
            }
        }
    }

    // 提交一条簇状倒 V 裂纹，按概率拆成完整 V 或破碎短弧，减少均匀触手感。
    public static void submitClusteredInvertedVArc(Vec3 hitPos, Vec3 start, TridentLightningColorStyle fixedStyle, boolean enhanced,
                                                   long seed, RandomSource random, int index, double angle,
                                                   float widthScale, float jitterScale, double rangeScale, double heightScale,
                                                   float growTimeScale, float holdTimeScale, float fadeTimeScale) {
        Vec3 outwardDir = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
        Vec3 sideDir = new Vec3(-outwardDir.z, 0.0D, outwardDir.x);
        double endRadius = (INVERTED_V_END_RADIUS_MIN + random.nextDouble() * INVERTED_V_END_RADIUS_RANDOM) * rangeScale;
        double midRadius = Math.min(endRadius * 0.72D, (INVERTED_V_MID_RADIUS_MIN + random.nextDouble() * INVERTED_V_MID_RADIUS_RANDOM) * rangeScale);
        double apexHeight = (INVERTED_V_APEX_HEIGHT_MIN + random.nextDouble() * INVERTED_V_APEX_HEIGHT_RANDOM) * heightScale;
        double sideOffset = (random.nextDouble() * 2.0D - 1.0D) * INVERTED_V_SIDE_SPREAD * rangeScale;
        double cornerLength = (INVERTED_V_CORNER_LENGTH_MIN + random.nextDouble() * INVERTED_V_CORNER_LENGTH_RANDOM) * rangeScale;
        Vec3 apexIn = hitPos.add(outwardDir.scale(midRadius)).add(sideDir.scale(-cornerLength * 0.5D)).add(0.0D, apexHeight, 0.0D);
        Vec3 apexOut = hitPos.add(outwardDir.scale(midRadius + cornerLength * 0.35D)).add(sideDir.scale(cornerLength * 0.5D)).add(0.0D, apexHeight * 0.92D, 0.0D);
        Vec3 outerGround = hitPos.add(outwardDir.scale(endRadius)).add(sideDir.scale(sideOffset)).add(0.0D, INVERTED_V_START_Y_OFFSET, 0.0D);
        long upSeed = seed + index * 1543L;
        long cornerSeed = seed + index * 1543L + 4919L;
        long downSeed = seed + index * 1543L + 9973L;
        TridentLightningColorStyle style = fixedStyle == null ? TridentLightningColorStyle.pick(random, enhanced) : fixedStyle;
        float growScale = INVERTED_V_GROW_TIME_SCALE * growTimeScale;
        float holdScale = INVERTED_V_HOLD_TIME_SCALE * holdTimeScale;
        float fadeScale = INVERTED_V_FADE_TIME_SCALE * fadeTimeScale;
        if (random.nextFloat() < INVERTED_V_FULL_SHAPE_CHANCE) {
            submitPath(start, apexIn, upSeed, style, random, INVERTED_V_WIDTH_MIN, INVERTED_V_WIDTH_RANDOM, widthScale, jitterScale, growScale, holdScale, fadeScale);
            submitPath(apexIn, apexOut, cornerSeed, style, random, INVERTED_V_WIDTH_MIN, INVERTED_V_WIDTH_RANDOM, widthScale * 0.88F, jitterScale * 0.75F, growScale, holdScale, fadeScale);
            submitPath(apexOut, outerGround, downSeed, style, random, INVERTED_V_WIDTH_MIN, INVERTED_V_WIDTH_RANDOM, widthScale, jitterScale, growScale, holdScale, fadeScale);
        } else if (random.nextBoolean()) {
            submitPath(start, apexOut, upSeed, style, random, INVERTED_V_WIDTH_MIN, INVERTED_V_WIDTH_RANDOM, widthScale, jitterScale, growScale, holdScale, fadeScale);
        } else {
            submitPath(apexIn, outerGround, downSeed, style, random, INVERTED_V_WIDTH_MIN, INVERTED_V_WIDTH_RANDOM, widthScale, jitterScale, growScale, holdScale, fadeScale);
        }
        if (random.nextFloat() < INVERTED_V_SHORT_BRANCH_CHANCE) {
            Vec3 branchStart = random.nextBoolean() ? apexIn : apexOut;
            Vec3 branchEnd = branchStart.add(outwardDir.scale(endRadius * INVERTED_V_SHORT_BRANCH_LENGTH_SCALE * 0.25D))
                    .add(sideDir.scale((random.nextDouble() * 2.0D - 1.0D) * cornerLength))
                    .add(0.0D, -apexHeight * 0.22D, 0.0D);
            submitPath(branchStart, branchEnd, seed + index * 1543L + 13171L, style, random,
                    INVERTED_V_WIDTH_MIN, INVERTED_V_WIDTH_RANDOM, widthScale * 0.62F, jitterScale * 0.85F, growScale, holdScale, fadeScale);
        }
    }

    // 统一提交一段溅射 PATH 闪电，集中随机生命周期、宽度和噪声图。
    public static void submitPath(Vec3 start, Vec3 end, long seed, TridentLightningColorStyle style, RandomSource random,
                                  float widthMin, float widthRandom, float widthScale, float jitterScale) {
        submitPath(start, end, seed, style, random, widthMin, widthRandom, widthScale, jitterScale, 1.0F, 1.0F, 1.0F);
    }

    // 统一提交带生命周期倍率的溅射 PATH 闪电，倒 V 可单独放慢出现速度。
    public static void submitPath(Vec3 start, Vec3 end, long seed, TridentLightningColorStyle style, RandomSource random,
                                  float widthMin, float widthRandom, float widthScale, float jitterScale,
                                  float growTimeScale, float holdTimeScale, float fadeTimeScale) {
        float growTime = (SPLASH_GROW_TIME_MIN + random.nextFloat() * SPLASH_GROW_TIME_RANDOM) * growTimeScale;
        float holdTime = (SPLASH_HOLD_TIME_MIN + random.nextFloat() * SPLASH_HOLD_TIME_RANDOM) * holdTimeScale;
        float fadeTime = (SPLASH_FADE_TIME_MIN + random.nextFloat() * SPLASH_FADE_TIME_RANDOM) * fadeTimeScale;
        float width = (widthMin + random.nextFloat() * widthRandom) * widthScale;
        float noiseIndex = random.nextBoolean() ? CoinLightningQueue.NOISE_INDEX_ALT : CoinLightningQueue.NOISE_INDEX_PRIMARY;
        float noiseStrength = NOISE_STRENGTH_MIN + random.nextFloat() * NOISE_STRENGTH_RANDOM;
        Exe.POST.effects().addLightningPath(start, end, growTime, holdTime, fadeTime, width, seed,
                style.pathCoreR, style.pathCoreG, style.pathCoreB, style.pathBloomR, style.pathBloomG, style.pathBloomB,
                jitterScale, 0, noiseIndex, noiseStrength);
    }
}
