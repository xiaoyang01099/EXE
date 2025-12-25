//package net.xiaoyang010.ex_enigmaticlegacy.Container;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import javax.annotation.Nullable;
//import net.minecraft.client.Minecraft;
//import net.minecraft.world.inventory.AbstractContainerMenu;
//import net.minecraft.world.inventory.Slot;
//import net.minecraft.world.inventory.CraftingContainer;
//import net.minecraft.world.inventory.ContainerLevelAccess;
//import net.minecraft.world.inventory.ClickType;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.entity.player.Inventory;
//import net.minecraft.world.SimpleContainer;
//import net.minecraft.world.Container;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.Items;
//import net.minecraft.world.item.enchantment.Enchantment;
//import net.minecraft.world.item.enchantment.EnchantmentHelper;
//import net.minecraft.network.FriendlyByteBuf;
//import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
//import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
//import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
//import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.PacketIndex;
//import net.xiaoyang010.ex_enigmaticlegacy.Recipe.DeconRecipe;
//import net.xiaoyang010.ex_enigmaticlegacy.Util.ExtremeCraftingDeconstructionManager;
//
//public class ExtremeDisassemblyMenu extends AbstractContainerMenu {
//    private int recipeIndex;
//    private Player player;
//    private final ContainerLevelAccess access;
//
//    // 81格输出矩阵 (9x9) - 专门显示极限合成配方
//    public SimpleContainer matrix = new SimpleContainer(81);
//
//    // 输入槽 - 只接受通过极限合成台制作的物品
//    private Container in = new CraftingContainer(this, 1, 1);
//
//    // 附魔书槽
//    private SimpleContainer enchantInv = new SimpleContainer(1) {
//        @Override
//        public int getMaxStackSize() {
//            return 1;
//        }
//    };
//
//    public ExtremeDisassemblyMenu(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
//        this(windowId, playerInventory, ContainerLevelAccess.create(
//                playerInventory.player.level, data.readBlockPos()));
//    }
//
//    public ExtremeDisassemblyMenu(int windowId, Inventory playerInventory, ContainerLevelAccess access) {
//        super(ModMenus.EXTREME_CRAFTING_DISASSEMBLY_MENU, windowId);
//        this.player = playerInventory.player;
//        this.access = access;
//
//        // 输入槽 (左侧中心)
//        this.addSlot(new Slot(this.in, 0, 8, 84) {
//            @Override
//            public boolean mayPlace(ItemStack stack) {
//                // 只允许通过极限合成制作的物品
//                return ExtremeDisassemblyMenu.this.isExtremeCraftedItem(stack);
//            }
//        });
//
//        // 附魔书槽 (输入槽下方)
//        this.addSlot(new Slot(this.enchantInv, 0, 8, 109) {
//            @Override
//            public boolean mayPlace(@Nullable ItemStack stack) {
//                return stack != ItemStack.EMPTY ? stack.is(Items.BOOK) : false;
//            }
//        });
//
//        // 9x9 输出网格 (81个槽位)
//        int startX = 35;
//        int startY = 8;
//        for(int y = 0; y < 9; ++y) {
//            for(int x = 0; x < 9; ++x) {
//                this.addSlot((new Slot(this.matrix, x + y * 9, startX + x * 18, startY + y * 18) {
//                    private ExtremeDisassemblyMenu eventHandler;
//
//                    @Override
//                    public boolean mayPlace(@Nullable ItemStack stack) {
//                        return false; // 输出槽不允许放入物品
//                    }
//
//                    @Override
//                    public void onTake(Player playerIn, ItemStack stack) {
//                        if (!playerIn.level.isClientSide()) {
//                            this.eventHandler.decrementCurrentRecipe();
//                        }
//                    }
//
//                    private Slot setEventHandler(ExtremeDisassemblyMenu c) {
//                        this.eventHandler = c;
//                        return this;
//                    }
//                }).setEventHandler(this));
//            }
//        }
//
//        // 玩家物品栏 (3x9)
//        for(int y = 0; y < 3; ++y) {
//            for(int x = 0; x < 9; ++x) {
//                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 35 + x * 18, 176 + y * 18));
//            }
//        }
//
//        // 玩家快捷栏 (1x9)
//        for(int x = 0; x < 9; ++x) {
//            this.addSlot(new Slot(playerInventory, x, 35 + x * 18, 234));
//        }
//    }
//
//    /**
//     * 检查物品是否通过极限合成台制作
//     */
//    private boolean isExtremeCraftedItem(ItemStack stack) {
//        if (stack.isEmpty()) return false;
//
//        // 检查该物品是否有对应的极限合成配方
//        return ExtremeCraftingDeconstructionManager.instance.hasExtremeCraftingRecipe(stack);
//    }
//
//    private void decrementCurrentRecipe() {
//        if (!this.enchantInv.getItem(0).isEmpty()) {
//            this.handleEnchant(this.enchantInv, this.in);
//        }
//
//        List<DeconRecipe> recipes = ExtremeCraftingDeconstructionManager.instance.getExtremeCraftingRecipes(this.in.getItem(0));
//        if (!recipes.isEmpty()) {
//            DeconRecipe recipe = recipes.get(this.getRecipeIndex());
//            ItemStack first = this.slots.get(0).getItem();
//            ItemStack decremented = this.in.removeItem(0, recipe.getResult().getCount());
//            this.slots.get(0).onQuickCraft(first, decremented);
//            this.broadcastChanges();
//        }
//    }
//
//    private void handleEnchant(Container enchantInv, Container in) {
//        if (this.in.getItem(0).isEnchanted()) {
//            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK, 1);
//            Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(in.getItem(0));
//            Random rand = new Random();
//            Enchantment key = (Enchantment)enchants.keySet().toArray()[rand.nextInt(enchants.size())];
//            Integer val = enchants.get(key);
//            Map<Enchantment, Integer> map = new HashMap<>();
//            map.put(key, val);
//            EnchantmentHelper.setEnchantments(map, book);
//            this.enchantInv.setItem(0, book);
//        }
//    }
//
//    @Override
//    public boolean stillValid(Player playerIn) {
//        return stillValid(this.access, playerIn, ModBlockss.EXTREME_CRAFTING_DISASSEMBLY_TABLE.get());
//    }
//
//    @Override
//    public void broadcastChanges() {
//        this.matrix.clearContent();
//        List<DeconRecipe> recipes = ExtremeCraftingDeconstructionManager.instance.getExtremeCraftingRecipes(this.in.getItem(0));
//        if (!recipes.isEmpty()) {
//            if (this.getRecipeIndex() >= recipes.size()) {
//                this.setRecipeIndex(0);
//                NetworkHandler.CHANNEL.sendToServer(new PacketIndex(0));
//            }
//
//            DeconRecipe recipe = recipes.get(this.getRecipeIndex());
//            ItemStack[] result = recipe.getIngredients();
//
//            // 处理9x9网格布局 (强制填满81格)
//            for(int i = 0; i < 9; ++i) {
//                for(int j = 0; j < 9; ++j) {
//                    int arrIndex = i * 9 + j;
//                    int slotIndex = i * 9 + j;
//
//                    if (arrIndex < result.length && result[arrIndex] != null && !result[arrIndex].isEmpty()) {
//                        this.matrix.setItem(slotIndex, result[arrIndex]);
//                    } else {
//                        this.matrix.setItem(slotIndex, ItemStack.EMPTY);
//                    }
//                }
//            }
//        }
//
//        super.broadcastChanges();
//    }
//
//    @Override
//    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
//        if (clickTypeIn == ClickType.PICKUP_ALL) {
//            return;
//        }
//
//        if (clickTypeIn == ClickType.QUICK_CRAFT) {
//            super.clicked(slotId, dragType, clickTypeIn, player);
//        }
//
//        if (slotId >= 0) {
//            Slot clickSlot = this.slots.get(slotId);
//            if (slotId >= 0 && !clickSlot.getItem().isEmpty() && clickSlot.getItem().getItem() != Items.AIR) {
//                // 处理输出槽点击 (槽位2-82是输出槽)
//                if (clickTypeIn == ClickType.PICKUP && slotId > 1 && slotId < 83) {
//                    // 取出所有相同物品
//                    for(int i = 2; i < 83; ++i) {
//                        if (i != slotId) {
//                            Slot slot = this.slots.get(i);
//                            ItemStack stack = slot.getItem();
//                            if (!stack.isEmpty()) {
//                                ItemStack copy = stack.copy();
//                                if (!this.moveItemStackTo(stack, 83, this.slots.size(), true)) {
//                                    player.drop(stack, true);
//                                    slot.set(ItemStack.EMPTY);
//                                } else {
//                                    slot.onQuickCraft(stack, copy);
//                                }
//
//                                if (stack.getCount() == 0) {
//                                    slot.set(ItemStack.EMPTY);
//                                } else {
//                                    slot.onQuickCraft(ItemStack.EMPTY, ItemStack.EMPTY);
//                                }
//                            }
//                        }
//                    }
//
//                    super.clicked(slotId, dragType, clickTypeIn, player);
//                } else if (clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE ||
//                        clickTypeIn == ClickType.QUICK_CRAFT || clickTypeIn == ClickType.CLONE) {
//                    super.clicked(slotId, dragType, clickTypeIn, player);
//                }
//
//                if (clickTypeIn == ClickType.THROW && slotId > 82) {
//                    super.clicked(slotId, dragType, clickTypeIn, player);
//                }
//            } else {
//                super.clicked(slotId, dragType, clickTypeIn, player);
//            }
//        }
//
//        if (slotId == 0) {
//            this.broadcastChanges();
//        }
//    }
//
//    @Override
//    public ItemStack quickMoveStack(Player player, int index) {
//        Slot slot = this.slots.get(index);
//        if (slot != null && slot.hasItem()) {
//            ItemStack stack = slot.getItem();
//            if (!stack.isEmpty()) {
//                ItemStack copy = stack.copy();
//
//                // 输入槽和附魔槽
//                if (index < 2) {
//                    if (!this.moveItemStackTo(stack, 83, this.slots.size(), true)) {
//                        return ItemStack.EMPTY;
//                    }
//                    slot.onQuickCraft(stack, copy);
//                }
//                // 输出槽 (2-82)
//                else if (index < 83) {
//                    // 取出所有输出物品到背包
//                    for(int i = 2; i < 83; ++i) {
//                        slot = this.slots.get(i);
//                        stack = slot.getItem();
//                        if (!stack.isEmpty()) {
//                            copy = stack.copy();
//                            if (!this.moveItemStackTo(stack, 83, this.slots.size(), true)) {
//                                player.drop(stack, true);
//                                slot.set(ItemStack.EMPTY);
//                            } else {
//                                slot.onQuickCraft(stack, copy);
//                            }
//
//                            if (stack.getCount() == 0) {
//                                slot.set(ItemStack.EMPTY);
//                            } else {
//                                slot.setChanged();
//                            }
//                        }
//                    }
//                    slot.onTake(player, stack);
//                }
//                // 玩家背包
//                else {
//                    if (!this.moveItemStackTo(stack, 0, 1, false)) {
//                        return ItemStack.EMPTY;
//                    }
//                    slot.onQuickCraft(stack, copy);
//                }
//
//                if (index > 82 || index < 2) {
//                    if (stack.getCount() == 0) {
//                        slot.set(ItemStack.EMPTY);
//                    } else {
//                        slot.setChanged();
//                    }
//
//                    if (stack.getCount() == copy.getCount()) {
//                        return ItemStack.EMPTY;
//                    }
//
//                    slot.onTake(player, stack);
//                }
//
//                return ItemStack.EMPTY;
//            }
//        }
//
//        return ItemStack.EMPTY;
//    }
//
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
//
//    public void setRecipeIndex(int index) {
//        this.recipeIndex = index;
//        this.broadcastChanges();
//    }
//
//    public int getRecipeIndex() {
//        return this.recipeIndex;
//    }
//}