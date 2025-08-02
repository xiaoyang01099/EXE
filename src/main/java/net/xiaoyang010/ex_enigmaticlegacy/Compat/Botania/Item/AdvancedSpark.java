package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.EntityAdvancedSpark;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nonnull;

public class AdvancedSpark extends Item {

    public AdvancedSpark(Properties builder) {
        super(builder);
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        return attachSpark(ctx.getLevel(), ctx.getClickedPos(), ctx.getItemInHand()) ? InteractionResult.sidedSuccess(ctx.getLevel().isClientSide) : InteractionResult.PASS;
    }

    public static boolean attachSpark(Level world, BlockPos pos, ItemStack stack) {
        var attach = IXplatAbstractions.INSTANCE.findSparkAttachable(world, pos, world.getBlockState(pos), world.getBlockEntity(pos), Direction.UP);
        if (attach != null) {
            if (attach.canAttachSpark(stack) && attach.getAttachedSpark() == null) {
                if (!world.isClientSide) {
                    stack.shrink(1);
                    EntityAdvancedSpark spark = new EntityAdvancedSpark(world);
                    spark.setPos(pos.getX() + 0.5, pos.getY() + 1.25, pos.getZ() + 0.5);
                    world.addFreshEntity(spark);
                    attach.attachSpark(spark);
                }
                return true;
            }
        }
        return false;
    }
}
