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
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModArmor;

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
// 飞行能力
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

//                // 移除所有与 StarflowerStone 相关的效果
                removeEffects(player);
                playersWithStone.remove(key);
            }
        }else if (hasStone){
            playersWithStone.add(key);
        }

//        if (!player.level.isClientSide) {
//            boolean hasStarflowerStone = false;
//
//
//
//            // 如果有 StarflowerStone，给予飞行和其他效果
//            if (hasStarflowerStone) {
//                // 飞行能力
//                if (!player.getAbilities().mayfly) {
//                    player.getAbilities().mayfly = true;
//                    player.onUpdateAbilities();
//                }
//
//                // 添加各种效果
//                addEffects(player);
//
//            } else if (isWearingFullArmor(player)) {
//                // 如果没有 StarflowerStone，但穿戴了整套盔甲，则给予飞行能力
//                if (!player.getAbilities().mayfly) {
//                    player.getAbilities().mayfly = true;
//                    player.onUpdateAbilities();
//                }
//
//                // 不添加其他效果，仅处理飞行和饱食度
//                if (player.getFoodData().getFoodLevel() < 20) {
//                    player.getFoodData().setFoodLevel(20);
//                }
//                if (player.getFoodData().getSaturationLevel() < 5.0F) {
//                    player.getFoodData().setSaturation(5.0F);
//                }
//
//            } else {
//                // 如果没有 StarflowerStone 且没有穿戴整套盔甲，则移除飞行能力和所有效果
//                if (player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
//                    player.getAbilities().mayfly = false;
//                    player.getAbilities().flying = false;
//                    player.onUpdateAbilities();
//                }
//
//                // 移除所有与 StarflowerStone 相关的效果
//                removeEffects(player);  //这个是啥东西？ 这里没tick移除了再生效果  你这个逻辑是：没用石和全套盔甲就持续移除效果
//                // 将给予的buff时间设为5秒左右 这样没用时时间结束就自动移除 夜视需单独设为20秒 还有飞行可能也有问题 会和其他飞行道具冲突
//            }
//        }
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

        return head.getItem() == ModArmor.MANAITA_HELMET.get()
                && chest.getItem() == ModArmor.MANAITA_CHESTPLATE.get()
                && legs.getItem() == ModArmor.MANAITA_LEGGINGS.get()
                && feet.getItem() == ModArmor.MANAITA_BOOTS.get();
    }
}
