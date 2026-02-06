package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.ScepterOfSovereign;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy")
public class SoulCaptureHandler {

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level.isClientSide) {
            return;
        }

        if (!(event.getEntity() instanceof TamableAnimal tamable)) {
            return;
        }

        if (!tamable.isTame() || tamable.getOwner() == null) {
            return;
        }

        if (!(tamable.getOwner() instanceof Player owner)) {
            return;
        }

        ItemStack totem = findRevivalTotem(owner);
        if (!totem.isEmpty()) {
            ScepterOfSovereign.storeSoul(totem, tamable, owner);
        }
    }

    private static ItemStack findRevivalTotem(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof ScepterOfSovereign) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }
}
