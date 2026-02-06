package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class UtilInventory {
    private static final String NBT_SORT = "powerinventory_sort";
    private static final int SORT_ALPH = 0;
    private static final int SORT_ALPHI = 1;

    public static void swapHotbars(ServerPlayer p) {
        InventoryOverpowered extendedInventory = PlayerInventoryData.getInventory(p);

        for (int bar = 0; bar < Const.HOTBAR_SIZE; bar++) {
            int second = bar + Const.HOTBAR_SIZE;

            ItemStack barStack = p.getInventory().getItem(bar);
            ItemStack secondStack = extendedInventory.getItem(second);

            p.getInventory().setItem(bar, secondStack);
            extendedInventory.setItem(second, barStack);
        }

        PlayerInventoryData.saveInventory(p, extendedInventory);
    }

    public static void swapInventoryGroup(ServerPlayer p, int invoGroup) {
        InventoryOverpowered extendedInventory = PlayerInventoryData.getInventory(p);

        for (int i = Const.HOTBAR_SIZE; i < Const.HOTBAR_SIZE + Const.V_INVO_SIZE; i++) {
            int second = i + (invoGroup - 1) * Const.V_INVO_SIZE + Const.HOTBAR_SIZE;

            ItemStack barStack = p.getInventory().getItem(i);
            ItemStack secondStack = extendedInventory.getItem(second);

            p.getInventory().setItem(i, secondStack);
            extendedInventory.setItem(second, barStack);
        }

        PlayerInventoryData.saveInventory(p, extendedInventory);
    }

    private static int getNextSort(ServerPlayer p) {
        int prev = p.getPersistentData().getInt(NBT_SORT);
        int n = prev + 1;
        if (n >= 2) n = 0;
        p.getPersistentData().putInt(NBT_SORT, n);
        return n;
    }

    public static void doSort(ServerPlayer p) {
        InventoryOverpowered invo = PlayerInventoryData.getInventory(p);
        int sortType = getNextSort(p);

        Map<String, SortGroup> unames = new HashMap<>();
        int iSize = invo.getContainerSize();

        for (int i = 2 * Const.HOTBAR_SIZE; i < iSize; i++) {
            ItemStack item = invo.getItem(i);
            if (item.isEmpty()) continue;

            String key;
            if (sortType == SORT_ALPH)
                key = item.getDescriptionId() + item.getDamageValue();
            else
                key = item.getItem().getClass().getName() +
                        item.getDescriptionId() +
                        item.getDamageValue();

            SortGroup temp = unames.get(key);
            if (temp == null) {
                temp = new SortGroup(key);
            }

            if (!temp.stacks.isEmpty()) {
                ItemStack top = temp.stacks.remove(temp.stacks.size() - 1);
                int room = top.getMaxStackSize() - top.getCount();

                if (room > 0) {
                    int moveover = Math.min(item.getCount(), room);
                    top.grow(moveover);
                    item.shrink(moveover);

                    if (item.getCount() == 0) {
                        item = ItemStack.EMPTY;
                        invo.setItem(i, item);
                    }
                }
                temp.stacks.add(top);
            }

            if (!item.isEmpty())
                temp.add(item);

            unames.put(key, temp);
        }

        ArrayList<SortGroup> sorted = new ArrayList<>(unames.values());
        sorted.sort(Comparator.comparing(o -> o.key));

        int k = 2 * Const.HOTBAR_SIZE;
        for (SortGroup sg : sorted) {
            for (ItemStack stack : sg.stacks) {
                if (k >= iSize) break;
                invo.setItem(k, ItemStack.EMPTY);
                invo.setItem(k, stack);
                k++;
            }
        }

        for (int j = k; j < iSize; j++) {
            invo.setItem(j, ItemStack.EMPTY);
        }

        PlayerInventoryData.saveInventory(p, invo);
    }

    public static ArrayList<Container> findTileEntityInventories(ServerPlayer player, int RADIUS) {
        ArrayList<Container> found = new ArrayList<>();

        int xMin = (int) player.getX() - RADIUS;
        int xMax = (int) player.getX() + RADIUS;
        int yMin = (int) player.getY() - RADIUS;
        int yMax = (int) player.getY() + RADIUS;
        int zMin = (int) player.getZ() - RADIUS;
        int zMax = (int) player.getZ() + RADIUS;

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockEntity blockEntity = player.level.getBlockEntity(pos);

                    if (blockEntity instanceof Container container) {
                        found.add(container);
                    }
                }
            }
        }

        return found;
    }

    public static void dumpFromPlayerToIInventory(Level world, Container inventory, ServerPlayer p) {
        InventoryOverpowered extendedInventory = PlayerInventoryData.getInventory(p);

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack chestItem = inventory.getItem(slot);

            if (!chestItem.isEmpty()) continue;

            for (int islotInv = 2 * Const.HOTBAR_SIZE;
                 islotInv < extendedInventory.getContainerSize();
                 islotInv++) {

                ItemStack invItem = extendedInventory.getItem(islotInv);
                if (invItem.isEmpty()) continue;

                inventory.setItem(slot, invItem);
                extendedInventory.setItem(islotInv, ItemStack.EMPTY);
                break;
            }
        }

        PlayerInventoryData.saveInventory(p, extendedInventory);
    }

    public static void sortFromPlayerToInventory(Level world, Container chest, ServerPlayer p) {
        InventoryOverpowered extendedInventory = PlayerInventoryData.getInventory(p);

        int START_CHEST = 0;
        int END_CHEST = chest.getContainerSize();

        for (int islotChest = START_CHEST; islotChest < END_CHEST; islotChest++) {
            ItemStack chestItem = chest.getItem(islotChest);

            if (chestItem.isEmpty()) continue;

            for (int islotInv = 2 * Const.HOTBAR_SIZE;
                 islotInv < extendedInventory.getContainerSize();
                 islotInv++) {

                ItemStack invItem = extendedInventory.getItem(islotInv);
                if (invItem.isEmpty()) continue;

                if (invItem.getItem().equals(chestItem.getItem()) &&
                        invItem.getDamageValue() == chestItem.getDamageValue()) {

                    int chestMax = chestItem.getItem().getMaxStackSize();
                    int room = chestMax - chestItem.getCount();

                    if (room <= 0) continue;

                    int toDeposit = Math.min(invItem.getCount(), room);

                    chestItem.grow(toDeposit);
                    chest.setItem(islotChest, chestItem);

                    invItem.shrink(toDeposit);

                    if (invItem.getCount() <= 0) {
                        extendedInventory.setItem(islotInv, ItemStack.EMPTY);
                    } else {
                        extendedInventory.setItem(islotInv, invItem);
                    }
                }
            }
        }

        PlayerInventoryData.saveInventory(p, extendedInventory);
    }

    public static class SortGroup {
        public final ArrayList<ItemStack> stacks;
        public final String key;

        public SortGroup(String key) {
            this.stacks = new ArrayList<>();
            this.key = key;
        }

        public void add(ItemStack stack) {
            this.stacks.add(stack);
        }
    }
}