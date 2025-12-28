package net.xiaoyang010.ex_enigmaticlegacy.Container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.PacketIndex;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.DeconRecipe;
import net.xiaoyang010.ex_enigmaticlegacy.Util.DeconstructionManager;

public class DeconTableMenu extends AbstractContainerMenu {
    private int recipeIndex;
    private Player player;
    private final ContainerLevelAccess access;
    public SimpleContainer matrix = new SimpleContainer(9);
    private SimpleContainer in = new SimpleContainer(1);
    private SimpleContainer enchantInv = new SimpleContainer(1) {
        @Override
        public int getMaxStackSize() {
            return 1;
        }
    };

    public DeconTableMenu(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        this(windowId, playerInventory, ContainerLevelAccess.create(
                playerInventory.player.level, data.readBlockPos()));
    }

    public DeconTableMenu(int windowId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenus.DECON_TABLE_MENU, windowId);
        this.player = playerInventory.player;
        this.access = access;

        this.addSlot(new Slot(this.in, 0, 45, 21));

        this.addSlot(new Slot(this.enchantInv, 0, 45, 46) {
            @Override
            public boolean mayPlace(@Nullable ItemStack stack) {
                return stack != ItemStack.EMPTY ? stack.is(Items.BOOK) : false;
            }
        });

        for(int x = 0; x < 3; ++x) {
            for(int y = 0; y < 3; ++y) {
                this.addSlot((new Slot(this.matrix, y + x * 3, 106 + y * 18, 17 + x * 18) {
                    private DeconTableMenu eventHandler;

                    @Override
                    public boolean mayPlace(@Nullable ItemStack stack) {
                        return false;
                    }

                    @Override
                    public void onTake(Player playerIn, ItemStack stack) {
                        if (!playerIn.level.isClientSide()) {
                            this.eventHandler.decrementCurrentRecipe();
                        }
                    }

                    private Slot setEventHandler(DeconTableMenu c) {
                        this.eventHandler = c;
                        return this;
                    }
                }).setEventHandler(this));
            }
        }

        for(int y = 0; y < 3; ++y) {
            for(int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        for(int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 142));
        }
    }

    @Override
    public void slotsChanged(Container inventoryIn) {
        super.slotsChanged(inventoryIn);
    }

    private void decrementCurrentRecipe() {
        if (!this.enchantInv.getItem(0).isEmpty()) {
            this.handleEnchant(this.enchantInv, this.in);
        }

        List<DeconRecipe> recipes = DeconstructionManager.instance.getRecipes(this.in.getItem(0));
        if (!recipes.isEmpty()) {
            DeconRecipe recipe = recipes.get(this.getRecipeIndex());
            ItemStack first = this.slots.get(0).getItem();
            ItemStack decremented = this.in.removeItem(0, recipe.getResult().getCount());
            this.slots.get(0).onQuickCraft(first, decremented);
            this.broadcastChanges();
        }
    }

    private void handleEnchant(Container enchantInv, Container in) {
        if (this.in.getItem(0).isEnchanted()) {
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK, 1);
            Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(in.getItem(0));
            Random rand = new Random();
            Enchantment key = (Enchantment)enchants.keySet().toArray()[rand.nextInt(enchants.size())];
            Integer val = enchants.get(key);
            Map<Enchantment, Integer> map = new HashMap<>();
            map.put(key, val);
            EnchantmentHelper.setEnchantments(map, book);
            this.enchantInv.setItem(0, book);
        }
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(this.access, playerIn, ModBlockss.DECON_TABLE.get());
    }

    @Override
    public void broadcastChanges() {
        this.matrix.clearContent();
        List<DeconRecipe> recipes = DeconstructionManager.instance.getRecipes(this.in.getItem(0));
        if (!recipes.isEmpty()) {
            if (this.getRecipeIndex() >= recipes.size()) {
                this.setRecipeIndex(0);
                if (!this.player.level.isClientSide()) {
                    NetworkHandler.CHANNEL.sendToServer(new PacketIndex(0));
                }
            }

            DeconRecipe recipe = recipes.get(this.getRecipeIndex());
            ItemStack[] result = recipe.getIngredients();

            for(int i = 0; i < recipe.height; ++i) {
                for(int j = 0; j < recipe.width; ++j) {
                    int arrIndex = i * recipe.width + j;
                    int slotIndex = i * 3 + j;
                    this.matrix.setItem(slotIndex, result[arrIndex]);
                }
            }
        }

        super.broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (clickTypeIn == ClickType.PICKUP_ALL) {
            return;
        } else {
            if (clickTypeIn == ClickType.QUICK_CRAFT) {
                super.clicked(slotId, dragType, clickTypeIn, player);
                return;
            }

            if (slotId >= 0 && slotId < this.slots.size()) {
                Slot clickSlot = this.slots.get(slotId);
                if (slotId >= 0 && !clickSlot.getItem().isEmpty() && clickSlot.getItem().getItem() != Items.AIR) {
                    if (clickTypeIn == ClickType.PICKUP && slotId > 1 && slotId < 11) {
                        Slot currSlot = this.slots.get(slotId);
                        ItemStack currStack = currSlot.getItem();

                        for(int i = 2; i < 11; ++i) {
                            if (i != slotId) {
                                Slot slot = this.slots.get(i);
                                ItemStack stack = slot.getItem();
                                if (!stack.isEmpty()) {
                                    ItemStack copy = stack.copy();
                                    if (!this.moveItemStackTo(stack, 11, 47, true)) {
                                        player.drop(stack, true);
                                        slot.set(ItemStack.EMPTY);
                                    } else {
                                        slot.onQuickCraft(stack, copy);
                                    }

                                    if (stack.getCount() == 0) {
                                        slot.set(ItemStack.EMPTY);
                                    } else {
                                        slot.onQuickCraft(ItemStack.EMPTY, ItemStack.EMPTY);
                                    }
                                }
                            }
                        }

                        super.clicked(slotId, dragType, clickTypeIn, player);
                    } else if (clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE ||
                            clickTypeIn == ClickType.QUICK_CRAFT || clickTypeIn == ClickType.CLONE) {
                        super.clicked(slotId, dragType, clickTypeIn, player);
                    }

                    if (clickTypeIn == ClickType.THROW && slotId > 11) {
                        super.clicked(slotId, dragType, clickTypeIn, player);
                    }
                } else {
                    super.clicked(slotId, dragType, clickTypeIn, player);
                }
            }

            if (slotId == 0) {
                this.broadcastChanges();
            }

            return;
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                ItemStack copy = stack.copy();
                if (index < 2) {
                    if (!this.moveItemStackTo(stack, 11, 47, true)) {
                        return ItemStack.EMPTY;
                    }

                    slot.onQuickCraft(stack, copy);
                } else if (index < 11) {
                    for(int i = 2; i < 11; ++i) {
                        slot = this.slots.get(i);
                        stack = slot.getItem();
                        if (!stack.isEmpty()) {
                            copy = stack.copy();
                            if (!this.moveItemStackTo(stack, 11, 47, true)) {
                                player.drop(stack, true);
                                slot.set(ItemStack.EMPTY);
                            } else {
                                slot.onQuickCraft(stack, copy);
                            }

                            if (stack.getCount() == 0) {
                                slot.set(ItemStack.EMPTY);
                            } else {
                                slot.setChanged();
                            }
                        }
                    }

                    slot.onTake(player, stack);
                } else if (index < 47) {
                    if (!this.moveItemStackTo(stack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }

                    slot.onQuickCraft(stack, copy);
                }

                if (index > 10 || index < 2) {
                    if (stack.getCount() == 0) {
                        slot.set(ItemStack.EMPTY);
                    } else {
                        slot.setChanged();
                    }

                    if (stack.getCount() == copy.getCount()) {
                        return ItemStack.EMPTY;
                    }

                    slot.onTake(player, stack);
                }

                return ItemStack.EMPTY;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.access.execute((level, pos) -> {
            if (!this.in.getItem(0).isEmpty()) {
                playerIn.drop(this.in.getItem(0), false);
            }
            if (!this.enchantInv.getItem(0).isEmpty()) {
                playerIn.drop(this.enchantInv.getItem(0), false);
            }
        });
    }

//    @Override
//    public void removed(Player playerIn) {
//        super.removed(playerIn);
//        if (!this.in.getItem(0).isEmpty()) {
//            playerIn.drop(this.in.getItem(0), false);
//            this.in.setItem(0, ItemStack.EMPTY);
//        }
//
//        if (!this.enchantInv.getItem(0).isEmpty()) {
//            playerIn.drop(this.enchantInv.getItem(0), false);
//            this.enchantInv.setItem(0, ItemStack.EMPTY);
//        }
//    }

    public void setRecipeIndex(int index) {
        this.recipeIndex = index;
        this.broadcastChanges();
    }

    public int getRecipeIndex() {
        return this.recipeIndex;
    }
}