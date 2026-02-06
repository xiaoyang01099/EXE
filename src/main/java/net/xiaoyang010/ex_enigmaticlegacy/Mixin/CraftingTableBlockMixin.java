package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Container.DoubleCraftingMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Util.DBHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingTableBlock.class)
public abstract class CraftingTableBlockMixin {
    @Unique
    private CraftingTableBlock ex_enigmaticlegacy$getSelf() {
        return (CraftingTableBlock)(Object)this;
    }

    @Inject(
            method = "getMenuProvider",
            at = @At("RETURN"),
            cancellable = true
    )
    private void DoubleCrafting$getMenuProvider(
            BlockState blockState,
            Level level,
            BlockPos blockPos,
            CallbackInfoReturnable<MenuProvider> cir
    ) {
        CraftingTableBlock self = ex_enigmaticlegacy$getSelf();

        if (self.getClass() != CraftingTableBlock.class) {
            return;
        }

        int value = blockState.getValue(DBHelper.DOUBLE);

        if (value >= 1 && value <= 4) {
            MenuProvider newProvider = new SimpleMenuProvider(
                    (integer, inventory, player) -> new DoubleCraftingMenu(
                            integer,
                            inventory,
                            ContainerLevelAccess.create(level, blockPos)
                    ),
                    DBHelper.CONTAINER_TITLE
            );
            cir.setReturnValue(newProvider);
        }
    }
}
