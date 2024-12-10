package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModArmors;

@Mod.EventBusSubscriber
public class ArmorEffectEvent {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;

            // 检查玩家是否穿戴了整套盔甲
            if (isWearingFullArmor(player)) {
                // 给予玩家飞行能力，如果其他物品没有控制飞行能力
                if (!player.getAbilities().mayfly && !player.isCreative() && !player.getAbilities().flying) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities(); // 确保同步能力
                }
                // 给予玩家无限饱食度
                if (player.getFoodData().getFoodLevel() < 20) {
                    player.getFoodData().setFoodLevel(20);
                }
                if (player.getFoodData().getSaturationLevel() < 5.0F) {
                    player.getFoodData().setSaturation(5.0F);
                }
            } else {
                // 如果没穿整套盔甲且玩家不在创造模式，移除飞行能力
                if (!player.isCreative() && !player.hasEffect(MobEffects.NIGHT_VISION) && player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false; // 停止飞行
                    player.onUpdateAbilities(); // 确保同步能力
                }
            }
        }
    }

    // 检查玩家是否穿戴了完整的 Manaita 盔甲
    private boolean isWearingFullArmor(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);

        return head.getItem() == ModArmors.MANAITA_HELMET.get()
                && chest.getItem() == ModArmors.MANAITA_CHESTPLATE.get()
                && legs.getItem() == ModArmors.MANAITA_LEGGINGS.get()
                && feet.getItem() == ModArmors.MANAITA_BOOTS.get();
    }
}
