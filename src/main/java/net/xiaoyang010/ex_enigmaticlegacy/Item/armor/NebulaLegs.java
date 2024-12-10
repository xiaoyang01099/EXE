package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class NebulaLegs extends NebulaArmor {
    public NebulaLegs(Properties props) {
        super(EquipmentSlot.LEGS, props);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {  // 使用新的模式匹配语法
            ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
            if (!legs.isEmpty() && legs.getItem() instanceof NebulaLegs) {
                // 更新为新的运动属性访问方式
                player.setDeltaMovement(player.getDeltaMovement().add(
                        0, getJump(legs), 0
                ));
                player.fallDistance = -getFallBuffer(legs);  // 更新为新的摔落伤害计算
            }
        }
    }

    private float getJump(ItemStack stack) {
        return 0.3F * (1.0F - (float)getDamage(stack) / 1000.0F);
    }

    private float getFallBuffer(ItemStack stack) {
        return 12.0F * (1.0F - (float)getDamage(stack) / 1000.0F);
    }

    @Override
    public float getEfficiency(ItemStack stack, Player player) {
        return 0;
    }
}