package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModArmors;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import net.xiaoyang010.ex_enigmaticlegacy.Item.StarflowerStone;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy")
public class FlyingEventHandlers {
    public static List<String> playersWithStone = new ArrayList<>();
    public static final List<String> playersWithNebulaChest = new ArrayList<>();
    public static final List<String> playersWithManaitaArmor = new ArrayList<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        boolean hasStone = isStone(player);
        boolean hasNeutralChest = hasNeutralChest(player);
        MobEffectInstance effect = player.getEffect(ModEffects.FLYING.get());
        boolean flying = effect != null && effect.getAmplifier() >= 0;
        boolean hasManaitaArmor = isWearingFullArmor(player);

        //防止其它模组飞行装备无法使用 参考1.12无尽
        String key = player.getGameProfile().getName()+":"+player.level.isClientSide;
        //星花石
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

        //星云盔甲
        if (playersWithNebulaChest.contains(key)) {
            if (hasNeutralChest) {
                if (!player.getAbilities().mayfly){
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
            } else {
                if (player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                }
                playersWithNebulaChest.remove(key);
            }
        } else if (hasNeutralChest) {
            playersWithNebulaChest.add(key);
        }

        if (playersWithManaitaArmor.contains(key)) {
            // 检查玩家是否穿戴了整套砧板盔甲
            if (hasManaitaArmor) {
                // 给予玩家飞行能力，如果其他物品没有控制飞行能力
                if (!player.getAbilities().mayfly) {
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
            }else {
                if (player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()){
                    // 如果没穿整套盔甲且玩家不在创造模式，移除飞行能力
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false; // 停止飞行
                    player.onUpdateAbilities(); // 确保同步能力
                }
                playersWithManaitaArmor.remove(key);
            }
        } else if (hasManaitaArmor){
            playersWithManaitaArmor.add(key);
        }
    }

    private static boolean hasNeutralChest(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return !chest.isEmpty() && chest.getItem() == ModArmors.NEBULA_CHESTPLATE.get();
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
