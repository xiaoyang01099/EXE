package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.ManaitaArrow;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class ManaitaBow extends BowItem {

    public ManaitaBow() {
        super(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR).rarity(ModRarities.MIRACLE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        InteractionResultHolder<ItemStack> ret = ForgeEventFactory.onArrowNock(itemstack, pLevel, pPlayer, pHand, true);
        if (ret != null) {
            return ret;
        } else {
            pPlayer.startUsingItem(pHand);
            return InteractionResultHolder.consume(itemstack);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            if (!level.isClientSide) {
                ManaitaArrow arrowEntity = new ManaitaArrow(level, player);

                arrowEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 0.01F);
                arrowEntity.setCritArrow(true);
                arrowEntity.setPierceLevel((byte) 5);

                arrowEntity.pickup = Pickup.CREATIVE_ONLY;
                level.addFreshEntity(arrowEntity);

                level.playSound((Player) null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 3.0f);
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow) {
        return super.customArrow(arrow);
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
