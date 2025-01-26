package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.OmegaCore;

@Mod.EventBusSubscriber
public class TimeStopEffectHandler {

    // 处理时停状态
    //@SubscribeEvent
    public static void onLivingUpdate(LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        Level level = entity.level; // 获取当前实体的世界

        // 确认时停状态是否激活
        if (OmegaCore.isTimeStopped()) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                // 获取当前时停的玩家，传入level
                Player timeStopPlayer = OmegaCore.getTimeStopPlayer(level);

                // 如果时停的玩家存在且不等于当前玩家，取消事件（暂停玩家）
                if (timeStopPlayer != null && !player.getUUID().equals(timeStopPlayer.getUUID())) {
                    event.setCanceled(true); // 暂停其他玩家
                }
            } else {
                //entity.canUpdate = false;
                // 暂停所有非玩家实体的更新
                event.setCanceled(true);
            }
        }
    }

    // 处理每tick更新，更新时停计时器
   // @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            OmegaCore.updateTimeStop(); // 更新时停计时器
        }
    }

    // 防止时停状态下的生物反击
    //@SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity target = event.getEntityLiving();
        Level level = target.level; // 获取当前实体的世界

        if (OmegaCore.isTimeStopped()) {
            // 如果时停激活，禁止生物反击或承受伤害
            Player timeStopPlayer = OmegaCore.getTimeStopPlayer(level); // 传入level

            if (timeStopPlayer != null && target instanceof Mob) {
                Mob mob = (Mob) target;

                // 暂时移除仇恨，以防止时停期间的反击
                mob.setTarget(null);
                event.setCanceled(true);
            }
        }
    }
}
