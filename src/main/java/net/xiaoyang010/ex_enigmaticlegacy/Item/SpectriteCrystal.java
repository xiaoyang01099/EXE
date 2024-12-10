package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.SpectriteCrystalEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;


public class SpectriteCrystal extends Item {

    public SpectriteCrystal() {
        super(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM).stacksTo(64).fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        HitResult hitResult = player.pick(5.0D, 0.0F, false);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;

            if (!world.isClientSide) {
                SpectriteCrystalEntity crystal = new SpectriteCrystalEntity(ModEntities.SPECTRITE_CRYSTAL.get(), world);
                crystal.moveTo(blockHitResult.getLocation().x, blockHitResult.getLocation().y + 1, blockHitResult.getLocation().z);
                world.addFreshEntity(crystal);
                world.playSound(null, blockHitResult.getBlockPos(), SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }
            }

            return InteractionResultHolder.sidedSuccess(itemstack, world.isClientSide());
        } else {
            return InteractionResultHolder.pass(itemstack);
        }
    }
}