package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;


import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class Dragonwings extends ElytraItem {
    private boolean isSpeedBoosted = false;

    public Dragonwings(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canElytraFly(ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
        return true;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, net.minecraft.world.entity.LivingEntity entity, int flightTicks) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.getItem() == Items.PHANTOM_MEMBRANE;
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.CHEST;
    }

    @Override
    public void onArmorTick(ItemStack stack, Level world, Player player) {
        super.onArmorTick(stack, world, player);

        if (player.isFallFlying()) {

            if (player.swingingArm != null) {

                if (!isSpeedBoosted) {
                    player.setDeltaMovement(player.getDeltaMovement().multiply(1.1, 1.0, 1.1));
                    isSpeedBoosted = true;

                } else {
                    player.setDeltaMovement(player.getDeltaMovement().multiply(0.9, 1.0, 0.9));
                    isSpeedBoosted = false;
                }
            }
        } else {
            // 如果玩家没有在飞行，重置增幅状态
            isSpeedBoosted = false;
        }
    }
}
