package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Item.InfinityTotem;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class InfinityTotemEvent {
    private static boolean totemJustTriggered = false;
    private static int invulnerableTimer = 0;
    private static final int INVULNERABLE_DURATION = 30; // 1.5秒 = 30 ticks

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof Player player)) {
            return;
        }

        if (player.level.isClientSide) {
            return;
        }

        InfinityTotem totemItem = (InfinityTotem) ModItems.INFINITY_TOTEM.get();
        if (totemItem.hasTotemInInventory(player)) {
            ItemStack totem = totemItem.getTotemFromInventory(player);
            if (!totem.isEmpty()) {
                event.setCanceled(true);
                totemJustTriggered = true;
                invulnerableTimer = INVULNERABLE_DURATION;

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING), 1);
                    CriteriaTriggers.USED_TOTEM.trigger(serverPlayer, totem);
                }
                totemItem.triggerTotemEffect(player, totem, event.getSource());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && totemJustTriggered) {
            if (invulnerableTimer > 0) {
                invulnerableTimer--;
            } else {
                totemJustTriggered = false;
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof Player)) {
            return;
        }

        // 只有在图腾刚触发且无敌时间内才取消伤害
        if (totemJustTriggered && invulnerableTimer > 0) {
            event.setCanceled(true);
        }
    }
}