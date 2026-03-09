package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class CreeperNugget extends Item {
    public static final CreeperNugget INSTANCE = new CreeperNugget();

    private CreeperNugget() {
        super(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_MINERAL));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            if (entity instanceof Player player) {
                if (!player.isCreative()) stack.shrink(1);

                FoodData stats = player.getFoodData();
                stats.eat(level.random.nextInt(4) + 2, 0.0f);

                if (!player.canEat(false)) {
                    level.explode(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            2.0f,
                            Explosion.BlockInteraction.DESTROY
                    );
                }
            }
        }
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.canEat(false)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.success(stack);
        } else {
            return InteractionResultHolder.fail(stack);
        }
    }
}