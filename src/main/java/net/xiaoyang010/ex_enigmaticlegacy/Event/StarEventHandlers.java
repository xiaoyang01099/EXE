package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.StarflowerStone;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModArmors;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class StarEventHandlers {
    public static List<String> playersWithStone = new ArrayList<>();


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        boolean hasStone = isStone(player);

        //防止其它模组飞行装备无法使用 参考1.12无尽
        String key = player.getGameProfile().getName()+":"+player.level.isClientSide;
        //head
        if (playersWithStone.contains(key)){
            if (hasStone){
                if (!player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
                // 添加各种效果
                addEffects(player);
            }else {
                // 如果没有 StarflowerStone 且没有穿戴整套盔甲，则移除飞行能力和所有效果
                if (player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                }
                // 移除所有与 StarflowerStone 相关的效果
                removeEffects(player);
                playersWithStone.remove(key);
            }
        }else if (hasStone){
            playersWithStone.add(key);
        }

    }

    private static boolean isStone(Player player){
        ItemStack hand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!hand.isEmpty() && hand.getItem() instanceof StarflowerStone){
            return true;
        }
        // 优先检查玩家的物品栏中是否有 StarflowerStone  物品栏不包含副手和盔甲栏
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof StarflowerStone) {
               return true;
            }
        }

        return false;
    }

    // 添加效果的方法
    private static void addEffects(Player player) {
        // 夜视效果
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 255, false, false));

        // 饱和效果
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, Integer.MAX_VALUE, 255, false, false));

        // 生命恢复效果
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Integer.MAX_VALUE, 255, false, false));

        // 伤害吸收效果
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, Integer.MAX_VALUE, 255, false, false));

        // 速度效果
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 4, false, false));
    }

    // 移除效果的方法
    private static void removeEffects(Player player) {
        // 移除夜视效果
        player.removeEffect(MobEffects.NIGHT_VISION);

        // 移除饱和效果
        player.removeEffect(MobEffects.SATURATION);

        // 移除生命恢复效果
        player.removeEffect(MobEffects.REGENERATION);

        // 移除伤害吸收效果
        player.removeEffect(MobEffects.ABSORPTION);

        // 移除速度效果
        player.removeEffect(MobEffects.MOVEMENT_SPEED);
    }

    // 检查是否穿戴完整的盔甲
    private static boolean isWearingFullArmor(Player player) {
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
