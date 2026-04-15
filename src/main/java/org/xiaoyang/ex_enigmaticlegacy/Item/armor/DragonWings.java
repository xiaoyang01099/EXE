package org.xiaoyang.ex_enigmaticlegacy.Item.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DragonWings extends ElytraItem {

    public DragonWings(Properties properties) {
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
        if (!player.isFallFlying()) return;

        if (world.isClientSide) {
            checkClientInput(player);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void checkClientInput(Player player) {
        Minecraft mc = Minecraft.getInstance();
        boolean rightPressed = mc.options.keyUse.isDown();
        boolean leftPressed = mc.options.keyAttack.isDown();

        if (leftPressed) {
            move(player, true);
        }
        if (rightPressed) {
            move(player, false);
        }
    }

    private void move(Player player, boolean move) {
        Vec3 vec31 = player.getLookAngle();
        double d0 = 1.5;
        double d1 = 0.25;
        double d2 = 0.5;
        double flag = move ? 1.0d : 0;
        Vec3 vec32 = player.getDeltaMovement();
        player.setDeltaMovement(vec32.add(
                vec31.x * d1 + (vec31.x * d0 - vec32.x) * d2,
                vec31.y * d1 + (vec31.y * d0 - vec32.y) * d2,
                vec31.z * d1 + (vec31.z * d0 - vec32.z) * d2).scale(flag));
    }
}