package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Util.DBHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {

    @Unique
    private BlockBehaviour ex_enigmaticlegacy$getSelf() {
        return (BlockBehaviour)(Object)this;
    }

    @Inject(
            method = "updateShape",
            at = @At("RETURN"),
            cancellable = true
    )
    private void DoubleCrafting$updateShape(
            BlockState mainBlock,
            Direction neighborBlockDirection,
            BlockState neighborBlock,
            LevelAccessor levelAccessor,
            BlockPos mainPos,
            BlockPos neighborPos,
            CallbackInfoReturnable<BlockState> cir
    ) {
        BlockBehaviour targetClass = ex_enigmaticlegacy$getSelf();

        if (targetClass.getClass() != CraftingTableBlock.class || !neighborBlockDirection.getAxis().isHorizontal()) {
            return;
        }

        BlockState original = cir.getReturnValue();
        CraftingTableBlock instance = (CraftingTableBlock)targetClass;
        int type = original.getValue(DBHelper.DOUBLE);
        Direction direction = DBHelper.typeToDirection(type);

        if (type == 0 && neighborBlock.is(instance) &&
                neighborBlock.getValue(DBHelper.DOUBLE) == DBHelper.directionToType(neighborBlockDirection.getOpposite())) {
            cir.setReturnValue(original.setValue(DBHelper.DOUBLE, DBHelper.directionToType(neighborBlockDirection)));
        } else {
            if (direction != null) {
                BlockState candidateBlock = levelAccessor.getBlockState(mainPos.relative(direction));
                if (type != 0 && candidateBlock.is(instance) && candidateBlock.getValue(DBHelper.DOUBLE) != 0) {
                    return;
                }
            }
            cir.setReturnValue(original.setValue(DBHelper.DOUBLE, 0));
        }
    }
}
