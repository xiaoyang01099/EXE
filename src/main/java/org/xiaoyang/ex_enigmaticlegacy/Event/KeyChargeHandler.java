package org.xiaoyang.ex_enigmaticlegacy.Event;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public class KeyChargeHandler {
    public static final Map<String, ChargeState> CHARGES = new HashMap<>();

    public static boolean start(Minecraft minecraft, KeyMapping keyMapping, String skillKey, int requiredTicks,
                                boolean slowWhileCharging, Runnable onStart) {
        if (minecraft == null || minecraft.level == null || keyMapping == null || skillKey == null) return false;
        if (CHARGES.containsKey(skillKey)) return false;
        ChargeState state = new ChargeState(keyMapping, minecraft.level.getGameTime(), requiredTicks, requiredTicks, slowWhileCharging, false);
        CHARGES.put(skillKey, state);
        if (onStart != null) onStart.run();
        return true;
    }

    public static boolean startManualRelease(Minecraft minecraft, KeyMapping keyMapping, String skillKey, int requiredTicks,
                                             int maxChargeTicks, boolean blockMovement, Runnable onStart) {
        if (minecraft == null || minecraft.level == null || keyMapping == null || skillKey == null) return false;
        if (CHARGES.containsKey(skillKey)) return false;
        ChargeState state = new ChargeState(keyMapping, minecraft.level.getGameTime(), requiredTicks, maxChargeTicks, false, blockMovement);
        CHARGES.put(skillKey, state);
        if (onStart != null) onStart.run();
        return true;
    }

    public static void tick(Minecraft minecraft, String skillKey, Runnable onFullCharge, Runnable onCancel) {
        ChargeState state = CHARGES.get(skillKey);
        if (state == null || minecraft == null || minecraft.level == null) return;

        if (state.completed) {
            if (!state.keyMapping.isDown()) CHARGES.remove(skillKey);
            return;
        }
        if (!state.keyMapping.isDown()) {
            CHARGES.remove(skillKey);
            if (onCancel != null) onCancel.run();
            return;
        }

        state.chargeTicks = Math.max(0L, minecraft.level.getGameTime() - state.startGameTime);
        if (state.chargeTicks < state.requiredChargeTicks) return;
        state.completed = true;
        if (onFullCharge != null) onFullCharge.run();
    }

    public static void tickHoldToRelease(Minecraft minecraft, String skillKey, Runnable onRelease, Runnable onCancel, Runnable onExpire) {
        ChargeState state = CHARGES.get(skillKey);
        if (state == null || minecraft == null || minecraft.level == null) return;

        state.chargeTicks = Math.max(0L, minecraft.level.getGameTime() - state.startGameTime);
        if (state.chargeTicks > state.maxChargeTicks) {
            CHARGES.remove(skillKey);
            if (onExpire != null) onExpire.run();
            return;
        }

        if (state.keyMapping.isDown()) return;
        CHARGES.remove(skillKey);
        if (state.chargeTicks >= state.requiredChargeTicks) {
            if (onRelease != null) onRelease.run();
            return;
        }
        if (onCancel != null) onCancel.run();
    }

    public static void cancel(String skillKey, Runnable onCancel) {
        ChargeState state = CHARGES.remove(skillKey);
        if (state == null || state.completed) return;
        if (onCancel != null) onCancel.run();
    }

    public static void clear() {
        CHARGES.clear();
    }

    public static float getProgress(String skillKey) {
        ChargeState state = CHARGES.get(skillKey);
        if (state == null) return 0.0F;
        return Math.min(1.0F, (float) state.chargeTicks / (float) state.requiredChargeTicks);
    }

    public static void updateRequiredTicks(String skillKey, int requiredTicks) {
        ChargeState state = CHARGES.get(skillKey);
        if (state == null || state.completed) return;
        state.requiredChargeTicks = Math.max(1, requiredTicks);
    }

    public static boolean shouldSlowMovement() {
        for (ChargeState state : CHARGES.values()) {
            if (!state.completed && state.slowWhileCharging) return true;
        }
        return false;
    }

    public static boolean shouldBlockMovement() {
        for (ChargeState state : CHARGES.values()) {
            if (!state.completed && state.blockMovement) return true;
        }
        return false;
    }

    public static class ChargeState {
        public final KeyMapping keyMapping;
        public final long startGameTime;
        public int requiredChargeTicks;
        public final int maxChargeTicks;
        public final boolean slowWhileCharging;
        public final boolean blockMovement;
        public long chargeTicks;
        public boolean completed;

        public ChargeState(KeyMapping keyMapping, long startGameTime, int requiredChargeTicks, int maxChargeTicks,
                           boolean slowWhileCharging, boolean blockMovement) {
            this.keyMapping = keyMapping;
            this.startGameTime = startGameTime;
            this.requiredChargeTicks = Math.max(1, requiredChargeTicks);
            this.maxChargeTicks = Math.max(this.requiredChargeTicks, maxChargeTicks);
            this.slowWhileCharging = slowWhileCharging;
            this.blockMovement = blockMovement;
        }
    }
}
