package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.ManaitaArrow;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class ManaitaBow extends BowItem {

    public ManaitaBow() {
        super(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR).rarity(ModRarities.MIRACLE));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        int charge = this.getUseDuration(stack) - timeLeft;

        if (entity instanceof Player player && !level.isClientSide) {
            if (!canFireArrow(player)) {
                return;
            }

            // 创建自定义箭矢 ManaitaArrow
            ManaitaArrow arrow = new ManaitaArrow(ModEntities.MANAITA_ARROW.get(), level);
            arrow.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ()); // 设置箭矢的位置

            // 设置箭矢为直线飞行
            double speed = charge * 0.1 + 3.0;  // 修改为 double 类型
            arrow.shootFromRotation((Entity) player, player.getXRot(), player.getYRot(), 0.0F, (float) speed, 0.0F);
            arrow.setNoGravity(true); // 取消重力影响，使其直线飞行

            // 设置箭矢为暴击箭矢，并且伤害无限大
            arrow.setCritArrow(true);
            arrow.setBaseDamage(Double.MAX_VALUE); // 设置伤害为最大

            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));

            level.addFreshEntity(arrow); // 将箭矢添加到世界中
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000; // 最大拉弓时间
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW; // 使用弓的动画
    }

    private boolean canFireArrow(Player player) {
        // 始终返回 true，这样即使没有箭矢也能发射
        return true;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }
}
