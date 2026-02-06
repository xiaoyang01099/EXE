package net.xiaoyang010.ex_enigmaticlegacy.Container;

import morph.avaritia.api.ExtremeCraftingRecipe;
import morph.avaritia.container.slot.OutputSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Container.slot.AutoCraftSlot;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.AutoCrafterPacket;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.AutoCrafterRecipePacket;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityExtremeAutoCrafter;

import javax.annotation.Nonnull;

public final class ContainerExtremeAutoCrafter extends AbstractContainerMenu {
    private final TileEntityExtremeAutoCrafter autoCrafter;
    private final Level level;
    private final Player player;
    private final int playerInventoryEnds = 200, playerInventoryStarts = 164, inventoryFull = 81, result = 163;

    public ContainerExtremeAutoCrafter(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, (Container) inventory.player.level.getBlockEntity(buf.readBlockPos()));
    }

    public ContainerExtremeAutoCrafter(int containerId, Inventory inventory, Container tileAutoCraft) {
        super(ModMenus.EXTREME_AUTO_CRAFTER_MENU, containerId);
        this.autoCrafter = (TileEntityExtremeAutoCrafter) tileAutoCraft;
        this.player = inventory.player;
        this.level = player.level;
        for (int y = 0; y < 9; y++)
            for (int x = 0; x < 9; x++)
                this.addSlot((new Slot(autoCrafter, y * 9 + x, 8 + (18 * x), 18 + (18 * y))));
        for (int y = 0; y < 9; y++)
            for (int x = 0; x < 9; x++)
                this.addSlot((new AutoCraftSlot(autoCrafter, 81 + (y * 9 + x), 175 + (18 * x), 18 + (18 * y))));
        this.addSlot((new AutoCraftSlot(autoCrafter, 162, 247, 194)));
        this.addSlot((new OutputSlot(autoCrafter, 163, 247, 222)));
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 9; x++)
                this.addSlot((new Slot(inventory, 9 + y * 9 + x, 43 + (18 * x), 194 + (18 * y))));
        for (int i = 0; i < 9; i++)
            this.addSlot((new Slot(inventory, i, 43 + (18 * i), 252)));
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull final Player player, final int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        final Slot actualSlot = this.slots.get(slotIndex);
        if (actualSlot.hasItem()) {
            ItemStack itemstack1 = actualSlot.getItem();
            itemstack = itemstack1.copy();
            if (slotIndex >= playerInventoryStarts) {
                if (!moveItemStackTo(itemstack1, 0, inventoryFull, false))
                    return ItemStack.EMPTY;
            } else if (slotIndex <= inventoryFull || slotIndex == result) {
                if (!moveItemStackTo(itemstack1, playerInventoryStarts, playerInventoryEnds, true))
                    return ItemStack.EMPTY;
            }
            if (itemstack1.getCount() == 0)
                actualSlot.set(ItemStack.EMPTY);
            actualSlot.setChanged();
        }
        return itemstack;
    }

    @Override
    public void clicked(final int slotId, final int mouseButton, @Nonnull final ClickType clickType, @Nonnull final Player player) {
        if (slotId >= inventoryFull && slotId < 162) {
            final Slot actualSlot = slots.get(slotId);
            if (clickType == ClickType.QUICK_MOVE) {
                actualSlot.set(ItemStack.EMPTY);
            } else if (clickType == ClickType.PICKUP) {
                final ItemStack playerStack = getCarried();
                final boolean slotHasStack = actualSlot.hasItem();
                if (!playerStack.isEmpty() && !slotHasStack) {
                    final ItemStack newSlotStack = playerStack.copy();
                    newSlotStack.setCount(1);
                    actualSlot.set(newSlotStack);
                } else if (playerStack.isEmpty() && slotHasStack || !playerStack.isEmpty() && ItemStack.isSameItemSameTags(playerStack, actualSlot.getItem()))
                    actualSlot.set(ItemStack.EMPTY);
                update(false);
            }
        } else if (slotId == 162){
            if (clickType == ClickType.PICKUP) {
                slots.get(slotId).set(ItemStack.EMPTY);
                for (int i = inventoryFull; i <= 162; i++){
                    slots.get(i).set(ItemStack.EMPTY);
                }
                update(true);
            }
        }else super.clicked(slotId, mouseButton, clickType, player);
    }

    public void setItem(int slot, ItemStack stack) {
        NetworkHandler.CHANNEL.sendToServer(new AutoCrafterPacket(slot, this.autoCrafter.getBlockPos(), stack));
    }

    public void setRecipe(ExtremeCraftingRecipe recipe){
        NetworkHandler.CHANNEL.sendToServer(new AutoCrafterRecipePacket(recipe == null ? ResourceLocation.tryParse("null") : recipe.getId(), this.autoCrafter.getBlockPos()));
    }

    @Override
    public boolean stillValid(Player player) {
        return this.autoCrafter.stillValid(player);
    }

    private void update(boolean flag){
        if (flag) {
            this.setRecipe(null);
            return;
        }
    }
}