package net.xiaoyang010.ex_enigmaticlegacy.Event;

import java.util.Arrays;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.IvyRegen;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import vazkii.botania.api.mana.ManaItemHandler;

@Mod.EventBusSubscriber
public class PlayerTickEventHandler {
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase == Phase.END && !player.level.isClientSide
                && player.level.getGameTime() % ConfigHandler.REPAIR_TICK.get() == 0) {
            if (!ConfigHandler.ONLY_REPAIR_EQUIPMENTS.get()) {
                player.getInventory().items.forEach((ItemStack itemstack) -> {
                    if (IvyRegen.hasIvy(itemstack) && itemstack.getDamageValue() > 0)
                        processItemRepair(player, itemstack);
                });
            }
            List<EquipmentSlot> relevantSlots = Arrays.asList(
                    EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
                    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

            for (EquipmentSlot slot : relevantSlots) {
                ItemStack itemstack = player.getItemBySlot(slot);
                if (IvyRegen.hasIvy(itemstack))
                    processItemRepair(player, itemstack);
            }
        }
    }

    protected static void processItemRepair(Player player, ItemStack itemstack) {
        if (itemstack.getDamageValue() == 0 || !itemstack.isRepairable()) {
            return;
        }

        int manaCostPerDamage = ConfigHandler.MANA_COST_PER_DAMAGE.get();
        int maxRepair = ConfigHandler.TRY_REPAIR_TO_FULL.get()
                ? calculateMaxRepair(player, itemstack, manaCostPerDamage)
                : 1;

        if (maxRepair > 0) {
            int totalCost = maxRepair * manaCostPerDamage;
            boolean success = ManaItemHandler.instance().requestManaExactForTool(
                    itemstack, player, totalCost, true);

            if (success) {
                int newDamage = Math.max(0, itemstack.getDamageValue() - maxRepair);
                itemstack.setDamageValue(newDamage);
            }
        }
    }

    private static int calculateMaxRepair(Player player, ItemStack tool, int manaPerDamage) {
        int availableMana = ManaItemHandler.instance().requestManaForTool(
                tool, player, Integer.MAX_VALUE, false);
        return Math.min(tool.getDamageValue(), availableMana / manaPerDamage);
    }
}