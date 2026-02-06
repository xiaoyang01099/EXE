package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.xiaoyang010.ex_enigmaticlegacy.Util.DBHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Unique
    private static final Direction[] EW$HORIZONTAL = new Direction[]{
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    @Unique
    private Block ex_enigmaticlegacy$getSelf() {
        return (Block)(Object)this;
    }

    @Inject(
            method = "createBlockStateDefinition",
            at = @At("TAIL")
    )
    private void DoubleCrafting$createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> blockBlockStateBuilder,
            CallbackInfo ci
    ) {
        Block self = ex_enigmaticlegacy$getSelf();
        if (self.getClass() == CraftingTableBlock.class) {
            blockBlockStateBuilder.add(DBHelper.DOUBLE);
        }
    }

    @Inject(
            method = "getStateForPlacement",
            at = @At("RETURN"),
            cancellable = true
    )
    private void DoubleCrafting$getStateForPlacement(
            BlockPlaceContext blockPlaceContext,
            CallbackInfoReturnable<BlockState> cir
    ) {
        Block self = ex_enigmaticlegacy$getSelf();

        if (self.getClass() != CraftingTableBlock.class) {
            return;
        }

        BlockState original = cir.getReturnValue();
        if (original == null) return;

        CraftingTableBlock instance = (CraftingTableBlock)self;
        int craftingTableType = 0;
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        Direction getDirectionFace = blockPlaceContext.getClickedFace();
        Direction directionToBaseBlock = null;

        for (Direction blockDirection : EW$HORIZONTAL) {
            blockDirection = blockDirection.getOpposite();
            BlockState candidateBlock = blockPlaceContext.getLevel()
                    .getBlockState(blockPos.relative(blockDirection));
            if (candidateBlock.is(instance) && candidateBlock.getValue(DBHelper.DOUBLE) == 0) {
                directionToBaseBlock = blockDirection;
                break;
            }
        }

        boolean flag = blockPlaceContext.isSecondaryUseActive();

        if (getDirectionFace.getAxis().isHorizontal() && flag) {
            BlockState blockState = blockPlaceContext.getLevel()
                    .getBlockState(blockPos.relative(getDirectionFace.getOpposite()));
            if (blockState.is(instance) && blockState.getValue(DBHelper.DOUBLE) == 0) {
                craftingTableType = DBHelper.directionToType(getDirectionFace.getOpposite());
            }
        }

        if (craftingTableType == 0 && !flag && directionToBaseBlock != null) {
            craftingTableType = DBHelper.directionToType(directionToBaseBlock);
        }

        cir.setReturnValue(original.setValue(DBHelper.DOUBLE, craftingTableType));
    }
}
