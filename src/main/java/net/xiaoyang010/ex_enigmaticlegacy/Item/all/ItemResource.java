package net.xiaoyang010.ex_enigmaticlegacy.Item.all;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class ItemResource extends Item {
    private final boolean isDrinkable;

    public ItemResource() {
        this(false);
    }

    public ItemResource(boolean isDrinkable) {
        super(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).stacksTo(64));
        this.isDrinkable = isDrinkable;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!isDrinkable) {
            return super.use(world, player, hand);
        }
        return ItemUtils.startUsingInstantly(world, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (!isDrinkable) {
            return super.finishUsingItem(stack, world, entity);
        }

        if (entity instanceof Player player) {
            if (!world.isClientSide) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.CONFUSION,
                        120,
                        3
                ));
            }

            if (!player.getAbilities().instabuild) {
                ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.getInventory().add(bottle)) {
                    player.drop(bottle, false);
                }
                stack.shrink(1);
            }
        }

        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return isDrinkable ? 24 : super.getUseDuration(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return isDrinkable ? UseAnim.DRINK : super.getUseAnimation(stack);
    }
}
