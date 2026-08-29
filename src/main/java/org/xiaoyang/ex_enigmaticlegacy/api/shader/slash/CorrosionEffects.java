package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

@Mod.EventBusSubscriber(modid = Exe.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CorrosionEffects {
    private static int ticksRemaining;

    private CorrosionEffects() {
    }

    public static void begin(int durationTicks) {
        if (ClientEffects.cannotReceiveEffects()) return;
        ticksRemaining = Math.max(ticksRemaining, Math.max(0, durationTicks));
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            clear();
        } else if (ClientLifecycle.isDead(
                minecraft.player.getHealth(),
                minecraft.player.deathTime,
                minecraft.player.isAlive()
        )) {
            abortForLocalPlayerDeath();
        } else if (PauseRenderState.isFrozen()) {
            return;
        } else if (ticksRemaining > 0) {
            ticksRemaining--;
        }
    }

    static void abortForLocalPlayerDeath() {
        Minecraft minecraft = Minecraft.getInstance();
        if (ticksRemaining > 0 && minecraft.player != null) {
            minecraft.player.removeEffect(MobEffects.CONFUSION);
        }
        clear();
    }

    static void clear() {
        ticksRemaining = 0;
    }
}
