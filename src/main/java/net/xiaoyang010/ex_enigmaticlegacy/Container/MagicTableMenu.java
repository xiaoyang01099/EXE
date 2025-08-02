package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;

public class MagicTableMenu extends AbstractContainerMenu {
    private static final int TexturesX = 0;

    private static final int TexturesY = 0;

    private final SimpleContainer container = new SimpleContainer(2);

    private final SimpleContainer outputContainer = new SimpleContainer(2);

    public MagicTableMenu(int id, Inventory inventory) {
        super(null,id);
        this.addSlot(new Slot(container,0,78+TexturesX,83+TexturesY));
        this.addSlot(new Slot(inventory,0,175+TexturesX,209+TexturesY));
        this.addSlot(new Slot(inventory,1,157+TexturesX,209+TexturesY));
        this.addSlot(new Slot(inventory,2,139+TexturesX,209+TexturesY));
        this.addSlot(new Slot(inventory,3,121+TexturesX,209+TexturesY));
        this.addSlot(new Slot(inventory,4,103+TexturesX,209+TexturesY));
        this.addSlot(new Slot(inventory,5,85+TexturesX,209+TexturesY));
        this.addSlot(new Slot(inventory,6,67+TexturesX,209+TexturesY));
        this.addSlot(new Slot(inventory,7,49+TexturesX,209+TexturesY));
        this.addSlot(new Slot(inventory,8,193+TexturesX,209+TexturesY));
        this.addSlot(new Slot(inventory,9,175+TexturesX,187+TexturesY));
        this.addSlot(new Slot(inventory,10,193+TexturesX,187+TexturesY));
        this.addSlot(new Slot(inventory,11,193+TexturesX,169+TexturesY));
        this.addSlot(new Slot(inventory,12,193+TexturesX,151+TexturesY));
        this.addSlot(new Slot(inventory,13,175+TexturesX,151+TexturesY));
        this.addSlot(new Slot(inventory,14,175+TexturesX,169+TexturesY));
        this.addSlot(new Slot(inventory,15,157+TexturesX,169+TexturesY));
        this.addSlot(new Slot(inventory,16,157+TexturesX,187+TexturesY));
        this.addSlot(new Slot(inventory,17,139+TexturesX,187+TexturesY));
        this.addSlot(new Slot(inventory,18,121+TexturesX,187+TexturesY));
        this.addSlot(new Slot(inventory,19,103+TexturesX,187+TexturesY));
        this.addSlot(new Slot(inventory,20,85+TexturesX,187+TexturesY));
        this.addSlot(new Slot(inventory,21,67+TexturesX,187+TexturesY));
        this.addSlot(new Slot(inventory,22,49+TexturesX,187+TexturesY));
        this.addSlot(new Slot(inventory,23,49+TexturesX,169+TexturesY));
        this.addSlot(new Slot(inventory,24,49+TexturesX,151+TexturesY));
        this.addSlot(new Slot(inventory,25,67+TexturesX,151+TexturesY));
        this.addSlot(new Slot(inventory,26,67+TexturesX,169+TexturesY));
        this.addSlot(new Slot(inventory,27,85+TexturesX,169+TexturesY));
        this.addSlot(new Slot(inventory,28,85+TexturesX,151+TexturesY));
        this.addSlot(new Slot(inventory,29,103+TexturesX,169+TexturesY));
        this.addSlot(new Slot(inventory,30,103+TexturesX,151+TexturesY));
        this.addSlot(new Slot(inventory,31,121+TexturesX,151+TexturesY));
        this.addSlot(new Slot(inventory,32,121+TexturesX,169+TexturesY));
        this.addSlot(new Slot(inventory,33,139+TexturesX,169+TexturesY));
        this.addSlot(new Slot(inventory,34,139+TexturesX,151+TexturesY));
        this.addSlot(new Slot(inventory,35,157+TexturesX,151+TexturesY));
        this.addSlot(new OutputSlot(outputContainer,0,160+TexturesX,83+TexturesY));
    }

    public MagicTableMenu(int i, Inventory inventory, FriendlyByteBuf friendlyByteBuf) {
        super(ModMenus.MAGIC_TABLE_MENU,i);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public static final class OutputSlot extends Slot {
        OutputSlot(Container container, int index, int x, int y) {
            super(container,index,x,y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}

