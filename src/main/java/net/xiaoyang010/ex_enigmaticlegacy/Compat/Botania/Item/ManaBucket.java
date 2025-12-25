package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

public class ManaBucket extends Item {
    private final boolean filled;

    public ManaBucket(Properties properties, boolean filled) {
        super(properties);
        this.filled = filled;
    }

    public boolean isFilled() {
        return filled;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState blockState = level.getBlockState(pos);

        if (player == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!filled && (blockState.is(Blocks.WATER) ||
                blockState.getFluidState().getType() == Fluids.WATER)) {

            if (blockState.getFluidState().isSource()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);

                ItemStack currentStack = context.getItemInHand();
                if (!player.getAbilities().instabuild) {
                    currentStack.shrink(1);
                    ItemStack filledBucket = new ItemStack(ModItems.FILLED_MANA_BUCKET.get());

                    if (currentStack.isEmpty()) {
                        player.setItemInHand(context.getHand(), filledBucket);
                    } else {
                        if (!player.getInventory().add(filledBucket)) {
                            player.drop(filledBucket, false);
                        }
                    }
                }

                return InteractionResult.SUCCESS;
            }
        }

        return super.useOn(context);
    }
}
