package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;

public class ContainerOverpowered extends AbstractContainerMenu {
    private final Player player;
    public final InventoryOverpowered customInventory;
    private int S_MAIN_START;
    private int S_MAIN_END;
    private int S_ECHEST = -1;
    private int S_PEARL = -1;
    private int S_BAR_START;
    private int S_BAR_END;
    private int S_BAROTHER_START;
    private int S_BAROTHER_END;
    private final int hotbarX = 8;
    private final int pad = 4;
    private final int hotbarY;

    public ContainerOverpowered(int windowId, Inventory playerInv) {
        super(ModMenus.OVERPOWERED_CONTAINER, windowId);
        this.player = playerInv.player;
        this.hotbarY = ConfigHandler.INVO_HEIGHT.get() - 18 - 7;

        this.customInventory = PlayerInventoryData.getInventory(player);

        // 1. 玩家原版快捷栏 (0-8)
        S_BAR_START = this.slots.size();
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i,
                    hotbarX + i * Const.SQ, hotbarY));
        }
        S_BAR_END = this.slots.size() - 1;

        // 2. 第二个快捷栏 (9-17)
        S_BAROTHER_START = this.slots.size();
        for (int i = 9; i < 18; i++) {
            int x = hotbarX + i * Const.SQ + pad;
            this.addSlot(new Slot(customInventory, i, x, hotbarY));
        }
        S_BAROTHER_END = this.slots.size() - 1;

        //3. 主存储区域
        S_MAIN_START = this.slots.size();
        int storageCount = PlayerUnlockData.getStorageCount(player);

        for (int k = 1; k <= storageCount; k++) {
            int xStart = InventoryRenderer.xPosSlotsStart(k);
            int yStart = InventoryRenderer.yPosSlotsStart(k);

            for (int i = 0; i < Const.ROWS_VANILLA; i++) {
                for (int j = 0; j < Const.COLS_VANILLA; j++) {
                    int slotNum = (k - 1) * Const.V_INVO_SIZE +
                            j + (i + 2) * Const.HOTBAR_SIZE;

                    int x = xStart + j * Const.SQ;
                    int y = yStart + i * Const.SQ;

                    this.addSlot(new Slot(customInventory, slotNum, x, y));
                }
            }
        }
        S_MAIN_END = this.slots.size() - 1;

        // 4. 末影珍珠槽位
        if (PlayerUnlockData.isEPearlUnlocked(player)) {
            S_PEARL = this.slots.size();
            this.addSlot(new SlotEnderPearl(customInventory, Const.SLOT_EPEARL));
        }

        // 5. 末影箱槽位
        if (PlayerUnlockData.isEChestUnlocked(player)) {
            S_ECHEST = this.slots.size();
            this.addSlot(new SlotEnderChest(customInventory, Const.SLOT_ECHEST));
        }

        ExEnigmaticlegacyMod.LOGGER.info(
                "Container created: BAR[{}-{}], BAROTHER[{}-{}], MAIN[{}-{}], PEARL[{}], ECHEST[{}]",
                S_BAR_START, S_BAR_END,
                S_BAROTHER_START, S_BAROTHER_END,
                S_MAIN_START, S_MAIN_END,
                S_PEARL, S_ECHEST
        );
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotNumber) {
        ItemStack stackCopy = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotNumber);

        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackOrig = slot.getItem();
        stackCopy = stackOrig.copy();

        if (slotNumber >= S_MAIN_START && slotNumber <= S_MAIN_END) {
            // 从主存储移动
            if (PlayerUnlockData.isEPearlUnlocked(player) &&
                    stackCopy.is(Items.ENDER_PEARL) &&
                    S_PEARL >= 0) {

                ItemStack pearlStack = customInventory.getItem(Const.SLOT_EPEARL);
                if (pearlStack.isEmpty() ||
                        pearlStack.getCount() < Items.ENDER_PEARL.getMaxStackSize()) {

                    if (!this.moveItemStackTo(stackOrig, S_PEARL, S_PEARL + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (PlayerUnlockData.isEChestUnlocked(player) &&
                    stackCopy.is(Blocks.ENDER_CHEST.asItem()) &&
                    S_ECHEST >= 0) {

                ItemStack chestStack = customInventory.getItem(Const.SLOT_ECHEST);
                if (chestStack.isEmpty() || chestStack.getCount() < 1) {
                    if (!this.moveItemStackTo(stackOrig, S_ECHEST, S_ECHEST + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(stackOrig, S_BAR_START, S_BAR_END + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotNumber >= S_BAR_START && slotNumber <= S_BAR_END ||
                slotNumber >= S_BAROTHER_START && slotNumber <= S_BAROTHER_END) {
            // 从快捷栏移动
            if (!this.moveItemStackTo(stackOrig, S_MAIN_START, S_MAIN_END + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotNumber == S_PEARL || slotNumber == S_ECHEST) {
            // 从特殊槽位移动
            if (!this.moveItemStackTo(stackOrig, S_MAIN_START, S_MAIN_END + 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackOrig.getCount() == 0) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stackOrig.getCount() == stackCopy.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stackOrig);

        if (!player.level.isClientSide && player instanceof ServerPlayer sp) {
            customInventory.syncAll(sp);
        }

        return stackCopy;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        super.clicked(slotId, button, clickType, player);

        if (!player.level.isClientSide && player instanceof ServerPlayer sp) {
            if (slotId >= 0 && slotId < this.slots.size()) {
                Slot slot = this.slots.get(slotId);
                NetworkHandler.sendSlotSync(sp, slot.index, slot.getItem());
            }
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (!player.level.isClientSide && player instanceof ServerPlayer sp) {
            customInventory.syncAll(sp);
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);

        if (!player.level.isClientSide) {
            PlayerInventoryData.saveInventory(player, customInventory);
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level.isClientSide) {
            PlayerInventoryData.saveInventory(player, customInventory);
            ExEnigmaticlegacyMod.LOGGER.debug("Container closed, saved inventory");
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (ConfigHandler.REQUIRE_RING.get()) {
            return ContainerOverpowered.isRingEquipped(player);
        }
        return true;
    }

    public static boolean isRingEquipped(Player player) {
        return ItemPowerRing.isRingEquipped(player);
    }
}