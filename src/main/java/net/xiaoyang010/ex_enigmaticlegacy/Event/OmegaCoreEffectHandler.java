package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.xiaoyang010.ex_enigmaticlegacy.Item.OmegaCore;

public class OmegaCoreEffectHandler {

    // 取消死亡事件
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (hasOmegaCore(player)) {
                // 取消死亡事件
                event.setCanceled(true);
                player.setHealth(20.0F);  // 设置玩家为满血
            }
        }
    }

    // 取消受到的伤害事件
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (hasOmegaCore(player)) {
                // 取消伤害
                event.setCanceled(true);
            }
        }
    }

    // 检查玩家是否有 OmegaCore
    private boolean hasOmegaCore(Player player) {
        // 检查玩家的背包是否有 OmegaCore
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof OmegaCore) {
                return true;
            }
        }
        return false;
    }
}
