package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModArmors;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import vazkii.botania.api.mana.IManaItem;

public class NebulaArmorHelper {
    private static final String TAG_COSMIC_FACE = "enableCosmicFace";

    public static boolean enableCosmicFace(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(TAG_COSMIC_FACE);
    }

    public static void setCosmicFace(ItemStack stack, boolean enable) {
        stack.getOrCreateTag().putBoolean(TAG_COSMIC_FACE, enable);
    }

    public static boolean shouldPlayerHaveStepup(Player player) {
        ItemStack armor = player.getItemBySlot(EquipmentSlot.FEET);
        return !armor.isEmpty() && armor.getItem() == ModArmors.NEBULA_BOOTS.get();
    }

    /**
     * 是否全套
     */
    public static boolean hasNebulaArmor(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
        return head.getItem() == ModArmors.NEBULA_HELMET.get() && chest.getItem() == ModArmors.NEBULA_CHESTPLATE.get()
                && legs.getItem() == ModArmors.NEBULA_LEGGINGS.get() && feet.getItem() == ModArmors.NEBULA_BOOTS.get();
    }

    //回血
    public static void foodToHeal(Player player){
        int food = player.getFoodData().getFoodLevel();
        if (food > 0 && food < 18 && player.canEat(false) && player.tickCount % 80 == 0) {
            player.heal(1.0F);
        }
    }

    public static boolean dispatchManaExact(ItemStack stack, Player player, int manaToSend, boolean add) {
        if (stack.isEmpty()) {
            return false;
        }

        // 检查玩家物品栏
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stackInSlot = inventory.getItem(i);
            if (checkAndTransferMana(stack, stackInSlot, manaToSend, add)) {
                return true;
            }
        }

        // 检查 Curios 槽位
        return CuriosApi.getCuriosHelper().findCurios(player, itemStack ->
                        itemStack.getItem() instanceof IManaItem)
                .stream()
                .map(SlotResult::stack)
                .anyMatch(curioStack -> checkAndTransferMana(stack, curioStack, manaToSend, add));
    }

    private static boolean checkAndTransferMana(ItemStack source, ItemStack target, int manaToSend, boolean add) {
        if (!target.isEmpty() && !target.is(source.getItem()) &&
                target.getItem() instanceof IManaItem manaItem) {

            if (manaItem.getMana() + manaToSend <= manaItem.getMaxMana()
                    && manaItem.canReceiveManaFromItem(target)) {

                if (add) {
                    manaItem.addMana(manaToSend);
                }
                return true;
            }
        }
        return false;
    }
}
