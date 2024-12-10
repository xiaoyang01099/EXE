package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModArmors;

public class ArmorProtectionEvent {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingHurt(LivingHurtEvent event) {
        // 确保伤害对象是玩家
        if (event.getEntity() instanceof Player player) {
            // 检查玩家是否穿戴了自定义盔甲
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);

            // 如果穿戴了 ManaitaChestplate，取消所有伤害
            if (chestplate.getItem() == ModArmors.MANAITA_CHESTPLATE.get()) {
                event.setCanceled(true); // 取消伤害事件
            }
        }
    }
}
