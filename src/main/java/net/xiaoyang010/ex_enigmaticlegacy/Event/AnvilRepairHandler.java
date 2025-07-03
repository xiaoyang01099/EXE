package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.IvyRegen;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.InfinityTotem;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class AnvilRepairHandler {
    private static final int REPAIR_COST = 1500;

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.getItem() instanceof InfinityTotem) {
            InfinityTotem totem = (InfinityTotem) left.getItem();

            if (totem.isValidRepairItem(left, right)) {
                ItemStack output = left.copy();
                output.setDamageValue(0);

                event.setCost(REPAIR_COST / 50);
                event.setMaterialCost(1);
                event.setOutput(output);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onAnvilUpdateEvent(AnvilUpdateEvent event) {
        ItemStack base = event.getLeft();
        ItemStack material = event.getRight();

        if (base.isEmpty())
            return;
        if (!base.isRepairable())
            return;
        if (material.isEmpty())
            return;
        if (!(material.getItem() instanceof IvyRegen))
            return;
        if (IvyRegen.hasIvy(base))
            return;

        ItemStack result = base.copy();
        IvyRegen.setIvy(result, true);

        event.setMaterialCost(1);
        event.setCost(ConfigHandler.TIMELESS_IVY_EXP_COST.get());
        event.setOutput(result);
    }
}