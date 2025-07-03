package net.xiaoyang010.ex_enigmaticlegacy.Container;

import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;

import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;

public class StarlitSanctumMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
    public final static HashMap<String, Object> guistate = new HashMap<>();
    public final Level world;
    public final Player entity;
    public int x, y, z;
    private IItemHandler internal;
    private final Map<Integer, Slot> customSlots = new HashMap<>();
    private boolean bound = false;

    // 槽位索引定义
    private static final int MAIN_GRID_SLOTS = 486;  // 主要区域槽位 (27x18)
    private static final int INPUT_LEFT_SLOT = 486;   // 左侧输入槽
    private static final int OUTPUT_SLOT = 487;       // 中间输出槽
    private static final int INPUT_RIGHT_SLOT = 488;  // 右侧输入槽
    private static final int TOTAL_CUSTOM_SLOTS = 489;

    public StarlitSanctumMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModMenus.STARLIT_SANCTUM_SCREEN, id);
        this.entity = inv.player;
        this.world = inv.player.level;
        this.internal = new ItemStackHandler(TOTAL_CUSTOM_SLOTS);  // 570个槽位

        BlockPos pos = null;
        if (extraData != null) {
            pos = extraData.readBlockPos();
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
        }

        if (pos != null) {
            if (extraData.readableBytes() == 1) { // 绑定到物品
                byte hand = extraData.readByte();
                ItemStack itemstack;
                if (hand == 0)
                    itemstack = this.entity.getMainHandItem();
                else
                    itemstack = this.entity.getOffhandItem();
                itemstack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capability -> {
                    this.internal = capability;
                    this.bound = true;
                });
            } else if (extraData.readableBytes() > 1) {
                extraData.readByte(); // 跳过填充
                Entity entity = world.getEntity(extraData.readVarInt());
                if (entity != null)
                    entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capability -> {
                        this.internal = capability;
                        this.bound = true;
                    });
            } else { // 可能绑定到方块
                BlockEntity ent = inv.player != null ? inv.player.level.getBlockEntity(pos) : null;
                if (ent != null) {
                    ent.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capability -> {
                        this.internal = capability;
                        this.bound = true;
                    });
                }
            }
        }

        setupSlots();
        setupPlayerInventory(inv);
    }

    private void setupSlots() {
        // 根据实际游戏截图精确调整坐标

        // 顶部特殊槽位 - 与发光装饰中心对齐
        // 左侧输入槽 - 第一个发光装饰中心
        this.customSlots.put(INPUT_LEFT_SLOT, this.addSlot(new SlotItemHandler(internal, INPUT_LEFT_SLOT, 83, 27) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true; // 允许放入物品
            }
        }));

        // 中间输出槽 - 第二个发光装饰中心
        this.customSlots.put(OUTPUT_SLOT, this.addSlot(new SlotItemHandler(internal, OUTPUT_SLOT, 256, 27) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // 禁止放入物品，只能输出
            }
        }));

        // 右侧输入槽 - 第三个发光装饰中心
        this.customSlots.put(INPUT_RIGHT_SLOT, this.addSlot(new SlotItemHandler(internal, INPUT_RIGHT_SLOT, 429, 27) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true; // 允许放入物品
            }
        }));

        // 主要网格区域：486个槽位 (27列 x 18行)
        // 从图片看，网格紧贴在装饰下方，左右居中
        int gridStartX = 37;  // 让27列在512像素内居中（(512-27*18)/2 ≈ 37）
        int gridStartY = 65;  // 网格起始Y坐标，在装饰下方
        int slotSpacing = 18; // 标准MC槽位间距
        int slotsPerRow = 27; // 每行27个槽位
        int rows = 18; // 18行

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < slotsPerRow; col++) {
                int index = row * slotsPerRow + col;
                if (index >= MAIN_GRID_SLOTS) break; // 防止超出486个槽位

                int x = gridStartX + col * slotSpacing;
                int y = gridStartY + row * slotSpacing;
                this.customSlots.put(index, this.addSlot(new SlotItemHandler(internal, index, x, y)));
            }
        }
    }

    private void setupPlayerInventory(Inventory inv) {
        // 玩家物品栏位置调整
        // 主网格从Y=65开始，18行*18像素 = 324，所以结束在Y=389
        int playerInvStartY = 400; // 在主网格下方留一些空间

        // 玩家物品栏 (3x9) - 居中显示
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + (row + 1) * 9, 175 + col * 18, playerInvStartY + row * 18));
            }
        }

        // 快速物品栏 (1x9) - 居中显示
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 175 + col * 18, playerInvStartY + 58));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < TOTAL_CUSTOM_SLOTS) {
                // 从自定义槽位移动到玩家物品栏
                if (!this.moveItemStackTo(itemstack1, TOTAL_CUSTOM_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } else {
                // 从玩家物品栏移动到自定义槽位
                // 优先移动到输入槽
                if (!this.moveItemStackTo(itemstack1, INPUT_LEFT_SLOT, INPUT_RIGHT_SLOT + 1, false)) {
                    // 如果输入槽满了，移动到主网格区域
                    if (!this.moveItemStackTo(itemstack1, 0, MAIN_GRID_SLOTS, false)) {
                        // 在玩家物品栏内部移动
                        if (index < TOTAL_CUSTOM_SLOTS + 27) {
                            if (!this.moveItemStackTo(itemstack1, TOTAL_CUSTOM_SLOTS + 27, this.slots.size(), true)) {
                                return ItemStack.EMPTY;
                            }
                        } else {
                            if (!this.moveItemStackTo(itemstack1, TOTAL_CUSTOM_SLOTS, TOTAL_CUSTOM_SLOTS + 27, false)) {
                                return ItemStack.EMPTY;
                            }
                        }
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemstack1.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = startIndex;

        if (reverseDirection) {
            i = endIndex - 1;
        }

        if (stack.isStackable()) {
            while (!stack.isEmpty()) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                Slot slot = this.slots.get(i);
                ItemStack itemstack = slot.getItem();

                if (slot.mayPlace(stack) && !itemstack.isEmpty() && ItemStack.isSameItemSameTags(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());

                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.set(itemstack);
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.set(itemstack);
                        flag = true;
                    }
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while (true) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                Slot slot1 = this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();

                if (itemstack1.isEmpty() && slot1.mayPlace(stack)) {
                    if (stack.getCount() > slot1.getMaxStackSize()) {
                        slot1.set(stack.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.set(stack.split(stack.getCount()));
                    }
                    slot1.setChanged();
                    flag = true;
                    break;
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        if (!bound && playerIn instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) {
                for (int j = 0; j < internal.getSlots(); ++j) {
                    playerIn.drop(internal.extractItem(j, internal.getStackInSlot(j).getCount(), false), false);
                }
            } else {
                for (int i = 0; i < internal.getSlots(); ++i) {
                    playerIn.getInventory().placeItemBackInInventory(internal.extractItem(i, internal.getStackInSlot(i).getCount(), false));
                }
            }
        }
    }

    public Map<Integer, Slot> get() {
        return customSlots;
    }
}