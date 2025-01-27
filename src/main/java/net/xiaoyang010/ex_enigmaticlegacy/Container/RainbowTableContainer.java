package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.RainbowTableTile;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RainbowTableContainer extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
    private final Map<Integer, Slot> customSlots = new HashMap<>();
    private final RainbowTableTile blockEntity;
    private final ContainerLevelAccess access;

    public RainbowTableContainer(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory playerInventory, BlockEntity entity, BlockPos pos) {
        super(pMenuType, pContainerId);
        this.blockEntity = (RainbowTableTile) entity;
        this.access = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());

        this.addSlot(new Slot(blockEntity, 0, 26, 36));  // 左上紫色槽位
        this.addSlot(new Slot(blockEntity, 1, 26, 54));  // 左下粉色槽位
        this.addSlot(new Slot(blockEntity, 2, 78, 36));  // 右上红色槽位
        this.addSlot(new Slot(blockEntity, 3, 78, 54));  // 右下蓝色槽位

        this.addSlot(new Slot(blockEntity, 4, 137, 44) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, 9 + col + row * 9,
                        8 + col * 18,
                        84 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col,
                    8 + col * 18,
                    142));
        }

        this.addDataSlots(this.blockEntity.getDate());
    }

    @OnlyIn(Dist.CLIENT)
    public int getTime() {
        return (int) Math.floor(this.blockEntity.getDate().get(0) / 40d * 35d);
    }

    @OnlyIn(Dist.CLIENT)
    public int getMana(){
        return (int) Math.floor((double) this.blockEntity.getDate().get(1) / RainbowTableTile.MAX_MANA * 85d);
    }

    @Override
    public Map<Integer, Slot> get() {
        return customSlots;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blockEntity.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 5) {
                if (!this.moveItemStackTo(itemstack1, 5, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.moveItemStackTo(itemstack1, 0, 4, false)) { // 只能放入前4个输入槽
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
}