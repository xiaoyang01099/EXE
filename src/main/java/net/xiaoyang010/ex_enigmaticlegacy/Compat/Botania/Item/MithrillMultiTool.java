package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;


public class MithrillMultiTool extends DiggerItem {

    public MithrillMultiTool(Properties tab) {
        super(
                0.0F,
                -2.8F,
                EXEAPI.mithrilToolMaterial,
                BlockTags.MINEABLE_WITH_PICKAXE,
                new Properties()
                        .tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)
                        .stacksTo(1)
        );
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState blockState) {
        return blockState.getBlock() != Blocks.BEDROCK;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (!isEnabled(stack)) {
            return 0.99F;
        }

        float baseSpeed = super.getDestroySpeed(stack, state);
        return baseSpeed > 1.0F ? baseSpeed + 135.0F : 135.0F;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        setEnabled(stack, !isEnabled(stack));

        if (!level.isClientSide) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 0.5F, 0.4F);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
//        return isEnabled(stack);
        return false;
    }

    public static boolean isEnabled(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean("enabled");
    }

    private void setEnabled(ItemStack stack, boolean enabled) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("enabled", enabled);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return EXEAPI.mithrilToolMaterial.getRepairIngredient().test(repair);
    }

    @Override
    public int getEnchantmentValue() {
        return EXEAPI.mithrilToolMaterial.getEnchantmentValue();
    }
}