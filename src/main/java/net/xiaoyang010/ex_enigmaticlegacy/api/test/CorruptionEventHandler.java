package net.xiaoyang010.ex_enigmaticlegacy.api.test;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

/**
 * 污染事件监听器
 * 处理污染相关的游戏逻辑
 */
@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class CorruptionEventHandler {

    /**
     * 当污染达到临界值时触发爆炸
     */
    @SubscribeEvent
    public static void onCorruptionCritical(CorruptionEvent.Critical event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            // 播放警告音效
            serverLevel.playSound(null, event.getPos(),
                    SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1.0F, 0.5F);

            // 如果污染达到100，触发小型爆炸
            if (event.getCorruptionLevel() >= 100) {
                serverLevel.explode(null,
                        event.getPos().getX() + 0.5,
                        event.getPos().getY() + 0.5,
                        event.getPos().getZ() + 0.5,
                        2.0F,
                        Explosion.BlockInteraction.NONE);
            }
        }
    }

    /**
     * 污染扩散时的特效
     */
    @SubscribeEvent
    public static void onCorruptionSpread(CorruptionEvent.Spread event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            // 播放扩散音效
            serverLevel.playSound(null, event.getTargetPos(),
                    SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.3F, 2.0F);
        }
    }
}
