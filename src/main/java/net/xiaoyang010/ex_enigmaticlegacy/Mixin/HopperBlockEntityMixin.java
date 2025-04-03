package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
    @Inject(method = "canPlaceItemInContainer", at = @At("HEAD"), cancellable = true)
    private static void onCanPlaceItemInContainer(Container container, ItemStack stack, int slot, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(ModTags.Items.SPECTRITE_ITEMS)) {
            if (container != null) {
                if (container instanceof Inventory || container instanceof CraftingContainer) {
                    return;
                }

                if (container instanceof BlockEntity blockEntity) {
                    if (!blockEntity.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER)) {
                        cir.setReturnValue(false);
                    }
                } else if (container instanceof CompoundContainer compoundContainer) {
                    if (compoundContainer.container1 instanceof BlockEntity be1
                            && compoundContainer.container2 instanceof BlockEntity be2) {
                        if (!be1.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER)
                                && !be2.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER)) {
                            cir.setReturnValue(false);
                        }
                    }
                }
            }
        }
    }
}