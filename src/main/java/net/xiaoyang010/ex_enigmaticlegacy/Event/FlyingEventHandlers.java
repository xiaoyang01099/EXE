package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Effect.FlyingEffect;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModArmors;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import net.xiaoyang010.ex_enigmaticlegacy.Item.StarflowerStone;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class FlyingEventHandlers {
    public static List<String> playersWithStone = new ArrayList<>();
    public static final List<String> playersWithNebulaChest = new ArrayList<>();
    public static final List<String> playersWithManaitaArmor = new ArrayList<>();
    public static final List<String> playersWithFlyEffect = new ArrayList<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        boolean hasStone = isStone(player);
        boolean hasNeutralChest = hasNeutralChest(player);
        MobEffectInstance effect = player.getEffect(ModEffects.FLYING.get());
        boolean flying = effect != null && effect.getAmplifier() >= 0;
        boolean hasManaitaArmor = isWearingFullArmor(player);

        String key = player.getGameProfile().getName()+":"+player.level.isClientSide;
        if (playersWithStone.contains(key)){
            if (hasStone){
                if (!player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
                addEffects(player);
            }else {
                if (player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                }
                removeEffects(player);
                playersWithStone.remove(key);
            }
        }else if (hasStone){
            playersWithStone.add(key);
        }

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
            if (hasManaitaArmor) {
                if (!player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
                if (player.getFoodData().getFoodLevel() < 20) {
                    player.getFoodData().setFoodLevel(20);
                }
                if (player.getFoodData().getSaturationLevel() < 5.0F) {
                    player.getFoodData().setSaturation(5.0F);
                }
            }else {
                if (player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()){
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                }
                playersWithManaitaArmor.remove(key);
            }
        } else if (hasManaitaArmor){
            playersWithManaitaArmor.add(key);
        }

        if (playersWithFlyEffect.contains(key)) {
            if (flying) {
                if (!player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
            } else {
                if (player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();

                    player.displayClientMessage(new TranslatableComponent("info.ex_enigmaticlegacy.flying.stop"), true);
                    if (!player.isOnGround()) {
                        player.getPersistentData().putBoolean(FlyingEffect.NBT_FLYING, true);
                    }
                }
                playersWithFlyEffect.remove(key);
            }
        } else if (flying) {
            playersWithFlyEffect.add(key);
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

        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof StarflowerStone) {
               return true;
            }
        }

        return false;
    }

    private static void addEffects(Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 255, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, Integer.MAX_VALUE, 255, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Integer.MAX_VALUE, 255, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, Integer.MAX_VALUE, 255, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 4, false, false));
    }

    private static void removeEffects(Player player) {
        player.removeEffect(MobEffects.NIGHT_VISION);
        player.removeEffect(MobEffects.SATURATION);
        player.removeEffect(MobEffects.REGENERATION);
        player.removeEffect(MobEffects.ABSORPTION);
        player.removeEffect(MobEffects.MOVEMENT_SPEED);
    }

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
