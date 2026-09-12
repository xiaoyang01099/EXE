package org.xiaoyang.ex_enigmaticlegacy.api.test.curse;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

@Mod.EventBusSubscriber(modid = Exe.MODID)
public class CorruptionEventHandler {

    @SubscribeEvent
    public static void onCorruptionCritical(CorruptionEvent.Critical event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, event.getPos(),
                    SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1.0F, 0.5F);
            if (event.getCorruptionLevel() >= 100) {
                serverLevel.explode(null,
                        event.getPos().getX() + 0.5,
                        event.getPos().getY() + 0.5,
                        event.getPos().getZ() + 0.5,
                        2.0F,
                        net.minecraft.world.level.Level.ExplosionInteraction.NONE);
            }
        }
    }

    @SubscribeEvent
    public static void onCorruptionSpread(CorruptionEvent.Spread event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, event.getTargetPos(),
                    SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.3F, 2.0F);
        }
    }
}
