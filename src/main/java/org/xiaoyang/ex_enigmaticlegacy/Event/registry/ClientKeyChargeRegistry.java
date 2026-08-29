package org.xiaoyang.ex_enigmaticlegacy.Event.registry;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.xiaoyang.ex_enigmaticlegacy.Util.SkillCooldownType;
import org.xiaoyang.ex_enigmaticlegacy.Event.KeyChargeHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ClientKeyChargeRegistry {
    public static final int EXPIRE_GRACE_TICKS = 40;
    public static final Map<Integer, VisualChargeState> STATES = new HashMap<>();

    public static void apply(int entityId, boolean active, InteractionHand hand, int chargeTicks, int elapsedTicks) {
        if (!active) {
            STATES.remove(entityId);
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.player.getId() == entityId) {
                KeyChargeHandler.cancel(SkillCooldownType.HEAVENLY_THUNDER.key(), null);
            }
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        long gameTime = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        STATES.put(entityId, new VisualChargeState(hand, gameTime - Math.max(0, elapsedTicks), chargeTicks));
        if (minecraft.player != null && minecraft.player.getId() == entityId) {
            KeyChargeHandler.updateRequiredTicks(SkillCooldownType.HEAVENLY_THUNDER.key(), chargeTicks);
        }
    }

    public static void startLocal(Player player, InteractionHand hand, int chargeTicks) {
        if (player == null) return;
        apply(player.getId(), true, hand, chargeTicks, 0);
    }

    public static void stop(Player player) {
        if (player == null) return;
        STATES.remove(player.getId());
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            STATES.clear();
            return;
        }
        long gameTime = minecraft.level.getGameTime();
        Iterator<Map.Entry<Integer, VisualChargeState>> iterator = STATES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, VisualChargeState> entry = iterator.next();
            VisualChargeState state = entry.getValue();
            if (minecraft.level.getEntity(entry.getKey()) == null
                    || gameTime - state.startGameTime > state.chargeTicks + EXPIRE_GRACE_TICKS) {
                iterator.remove();
            }
        }
    }

    public static boolean isCharging(Player player) {
        return player != null && STATES.containsKey(player.getId());
    }

    public static InteractionHand getHand(Player player) {
        VisualChargeState state = player == null ? null : STATES.get(player.getId());
        return state == null ? InteractionHand.MAIN_HAND : state.hand;
    }

    public static float getProgress(Player player, float partialTick) {
        if (player == null) return 0.0F;
        VisualChargeState state = STATES.get(player.getId());
        Minecraft minecraft = Minecraft.getInstance();
        if (state == null || minecraft.level == null) return 0.0F;
        float elapsed = minecraft.level.getGameTime() - state.startGameTime + partialTick;
        return Mth.clamp(elapsed / state.chargeTicks, 0.0F, 1.0F);
    }

    public static void clear() {
        STATES.clear();
    }

    public static class VisualChargeState {
        public final InteractionHand hand;
        public final long startGameTime;
        public final int chargeTicks;

        public VisualChargeState(InteractionHand hand, long startGameTime, int chargeTicks) {
            this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
            this.startGameTime = startGameTime;
            this.chargeTicks = Math.max(1, chargeTicks);
        }
    }
}
