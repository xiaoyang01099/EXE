package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.InfinityTotem;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class AnvilRepairHandler {
    private static final int REPAIR_COST = 1500; // 固定修复经验值

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        // 检查左槽是否是无限不死图腾
        if (left.getItem() instanceof InfinityTotem) {
            InfinityTotem totem = (InfinityTotem) left.getItem();

            // 检查是否是有效的修复材料
            if (totem.isValidRepairItem(left, right)) {
                // 创建输出物品
                ItemStack output = left.copy();
                output.setDamageValue(0); // 完全修复

                // 设置固定的经验消耗和材料消耗
                event.setCost(REPAIR_COST / 50); // 30级
                event.setMaterialCost(1);
                event.setOutput(output);
            }
        }
    }
}