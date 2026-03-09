package net.xiaoyang010.ex_enigmaticlegacy.api.test;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class VampireWingsEventHandler {

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level.isClientSide) return;

        ItemStack wings = getWings(player);
        if (wings.isEmpty()) return;

        player.heal(VampireWings.VAMPIRIC_HEAL);
        VampireWings.setBlood(wings, VampireWings.getBlood(wings) + 5);
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getNewTarget() instanceof Player player)) return;

        if (!(event.getEntity() instanceof Mob mob)) return;
        if (mob.getMobType() != MobType.UNDEAD) return;

        ItemStack wings = getWings(player);
        if (!wings.isEmpty()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level.isClientSide) return;

        ItemStack wings = getWings(player);
        if (wings.isEmpty()) return;

        int revives = VampireWings.getRevives(wings);
        if (revives > 0) {
            event.setCanceled(true);
            VampireWings.setRevives(wings, revives - 1);
            player.setHealth(player.getMaxHealth());
            player.removeAllEffects();
            player.displayClientMessage(
                    new net.minecraft.network.chat.TextComponent(
                            "§6黑暗之力将你从死亡边缘拉回！剩余复活次数: §c" + (revives - 1)), false);
        }
    }

    @SubscribeEvent
    public static void onWitherKill(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof WitherBoss)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level.isClientSide) return;

        ItemStack wings = getWings(player);
        if (wings.isEmpty()) return;

        VampireWings.addWitherKill(wings);
        int kills = VampireWings.getWitherKills(wings);
        player.displayClientMessage(
                new net.minecraft.network.chat.TextComponent(
                        kills == 0
                                ? "§6凋零的力量注入翅膀，复活次数已恢复一次！"
                                : "§8凋零击杀进度: " + kills + " / 10"), false);
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof Player player)) return;
        if (player.level.isClientSide) return;
        ItemStack wings = getWings(player);
        if (wings.isEmpty()) return;
        if (VampireWings.getMode(wings) == VampireWings.AbilityMode.SPIDER_CLIMB) {
            if (player.horizontalCollision && !player.isOnGround()) {
                double yMotion = player.getDeltaMovement().y;
                if (yMotion < 0) {
                    player.setDeltaMovement(
                            player.getDeltaMovement().x,
                            0.0,
                            player.getDeltaMovement().z
                    );
                }
                player.fallDistance = 0;
                player.resetFallDistance();
            }
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getPlayer();
        if (player.level.isClientSide) return;
        ItemStack wings = getWings(player);
        if (wings.isEmpty()) return;
        if (player.isCrouching()) {
            VampireWings.switchMode(player, wings);
            event.setCanceled(true);
        } else {
            VampireWings.activateAbility(player, wings);
            event.setCanceled(true);
        }
    }

    private static ItemStack getWings(Player player) {
        return CuriosApi.getCuriosHelper()
                .findEquippedCurio(s -> s.getItem() instanceof VampireWings, player)
                .map(triple -> triple.getRight())
                .orElse(ItemStack.EMPTY);
    }
}