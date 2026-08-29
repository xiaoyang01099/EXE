package org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful;

// 蓄力状态信息类。
public class ChargeInfo {
    // 蓄力开始参数。
    private final int startTick;
    // 当前蓄力进度（0.0 ~ 1.0）
    private final int fullChargeTime;

    // 蓄力状态参数。
    private float progress;
    // 是否正在蓄力
    private boolean charging;
    // 是否已完成蓄力（达到最大值）
    private boolean fullyCharged;

    public ChargeInfo(int startTick, int fullChargeTime) {
        this.startTick = startTick;
        this.fullChargeTime = Math.max(1, fullChargeTime);
        this.progress = 0.0f;
        this.charging = true;
        this.fullyCharged = false;
    }

    // 更新蓄力进度。
    public void update(int currentTick) {
        if (!charging) {
            return;
        }

        int elapsed = currentTick - startTick;
        progress = Math.min(1.0f, (float) elapsed / fullChargeTime);
        if (progress >= 1.0f) {
            fullyCharged = true;
        }
    }

    // 停止蓄力并返回最终进度。
    public float stop() {
        charging = false;
        return progress;
    }

    public float getProgress() {
        return progress;
    }

    public boolean isFullyCharged() {
        return fullyCharged;
    }
}
