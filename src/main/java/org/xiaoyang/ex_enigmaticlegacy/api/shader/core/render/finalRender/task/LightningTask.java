package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue.CoinLightningQueue;

import java.util.Random;

public class LightningTask implements PostRenderTask {
    public enum Kind {
        DATA,
        CHARGING
    }

    public final Kind kind; // 闪电任务类型。
    public final CoinLightningQueue.LightningData lightning; // 单条闪电数据。
    public final Player player; // 蓄力闪电所属玩家。
    public final float chargeProgress; // 玩家蓄力进度。
    public final float chargingPartialTick; // 蓄力闪电采样插值。
    public final boolean colorful; // 是否使用彩色蓄力闪电。

    public LightningTask(CoinLightningQueue.LightningData lightning) {
        this.kind = Kind.DATA;
        this.lightning = lightning;
        this.player = null;
        this.chargeProgress = 0.0F;
        this.chargingPartialTick = 0.0F;
        this.colorful = false;
    }

    public LightningTask(Player player, float chargeProgress, float partialTick, boolean colorful) {
        this.kind = Kind.CHARGING;
        this.lightning = null;
        this.player = player;
        this.chargeProgress = chargeProgress;
        this.chargingPartialTick = partialTick;
        this.colorful = colorful;
    }

    public PostRenderQueueType queueType() {
        return PostRenderQueueType.LIGHTNING;
    }

    public static LightningTask startToEnd(Vec3 start, Vec3 end, float lifetime, float width, long seed,
                                           float coreR, float coreG, float coreB,
                                           float bloomR, float bloomG, float bloomB) {
        float safeLifetime = Math.max(CoinLightningQueue.MIN_TIME * 3.0F, lifetime);
        return path(start, end, safeLifetime * 0.65F, safeLifetime * 0.25F, safeLifetime * 0.10F, width, seed,
                coreR, coreG, coreB, bloomR, bloomG, bloomB);
    }

    public static LightningTask path(Vec3 start, Vec3 end, float growTime, float holdTime, float fadeTime,
                                     float width, long seed,
                                     float coreR, float coreG, float coreB,
                                     float bloomR, float bloomG, float bloomB) {
        return path(start, end, growTime, holdTime, fadeTime, width, seed,
                coreR, coreG, coreB, bloomR, bloomG, bloomB, CoinLightningQueue.DEFAULT_PATH_JITTER_SCALE);
    }

    public static LightningTask path(Vec3 start, Vec3 end, float growTime, float holdTime, float fadeTime,
                                     float width, long seed,
                                     float coreR, float coreG, float coreB,
                                     float bloomR, float bloomG, float bloomB, float jitterScale) {
        return path(start, end, growTime, holdTime, fadeTime, width, seed,
                coreR, coreG, coreB, bloomR, bloomG, bloomB, jitterScale, 0);
    }

    public static LightningTask path(Vec3 start, Vec3 end, float growTime, float holdTime, float fadeTime,
                                     float width, long seed,
                                     float coreR, float coreG, float coreB,
                                     float bloomR, float bloomG, float bloomB, float jitterScale, int terminalBounceCount) {
        float noiseIndex = CoinLightningQueue.noiseIndexFromSeed(seed);
        return path(start, end, growTime, holdTime, fadeTime, width, seed,
                coreR, coreG, coreB, bloomR, bloomG, bloomB, jitterScale, terminalBounceCount,
                noiseIndex, CoinLightningQueue.DEFAULT_NOISE_STRENGTH);
    }

    public static LightningTask path(Vec3 start, Vec3 end, float growTime, float holdTime, float fadeTime,
                                     float width, long seed,
                                     float coreR, float coreG, float coreB,
                                     float bloomR, float bloomG, float bloomB, float jitterScale, int terminalBounceCount,
                                     float noiseIndex, float noiseStrength) {
        return path(start, end, growTime, holdTime, fadeTime, width, seed,
                coreR, coreG, coreB, bloomR, bloomG, bloomB, jitterScale, terminalBounceCount,
                noiseIndex, noiseStrength, 0.0F);
    }

    public static LightningTask path(Vec3 start, Vec3 end, float growTime, float holdTime, float fadeTime,
                                     float width, long seed,
                                     float coreR, float coreG, float coreB,
                                     float bloomR, float bloomG, float bloomB, float jitterScale, int terminalBounceCount,
                                     float noiseIndex, float noiseStrength, float startDelay) {
        CoinLightningQueue.LightningStyle style = new CoinLightningQueue.LightningStyle(coreR, coreG, coreB, bloomR, bloomG, bloomB);
        return new LightningTask(CoinLightningQueue.LightningData.path(start, end, seed, growTime, holdTime, fadeTime, width,
                style, jitterScale, terminalBounceCount, noiseIndex, noiseStrength, startDelay));
    }

    public static LightningTask burst(Vec3 start, Vec3 end, float growTime, float holdTime, float fadeTime,
                                      float width, long seed,
                                      float coreR, float coreG, float coreB,
                                      float bloomR, float bloomG, float bloomB) {
        Random random = new Random(seed);
        CoinLightningQueue.LightningStyle style = new CoinLightningQueue.LightningStyle(coreR, coreG, coreB, bloomR, bloomG, bloomB);
        return new LightningTask(CoinLightningQueue.LightningData.burst(start, end, seed, growTime, holdTime, fadeTime, width,
                3 + random.nextInt(3), style));
    }

    public static LightningTask ring(Vec3 center, Vec3 normal, float startRadius, float endRadius,
                                     float growTime, float holdTime, float fadeTime, float width, long seed,
                                     float coreR, float coreG, float coreB,
                                     float bloomR, float bloomG, float bloomB) {
        CoinLightningQueue.LightningStyle style = new CoinLightningQueue.LightningStyle(coreR, coreG, coreB, bloomR, bloomG, bloomB);
        return new LightningTask(CoinLightningQueue.LightningData.ring(center, normal, seed, startRadius, endRadius,
                growTime, holdTime, fadeTime, width, style));
    }

    public static LightningTask charging(Player player, float chargeProgress, float partialTick, boolean colorful) {
        return new LightningTask(player, chargeProgress, partialTick, colorful);
    }
}
