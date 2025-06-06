package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.RideablePearlEntity;

public class RideablePearl extends Item {
    public RideablePearl(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_PEARL_THROW, SoundSource.NEUTRAL,
                0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide) {
            RideablePearlEntity pearl = new RideablePearlEntity(player, level);
            pearl.setItem(itemstack);
            pearl.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(pearl);

            if (!player.getAbilities().instabuild) {
                itemstack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
            }

            player.startRiding(pearl);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}