package net.xiaoyang010.ex_enigmaticlegacy.Event;

import morph.avaritia.container.MachineMenu;
import morph.avaritia.container.slot.ScrollingFakeSlot;
import morph.avaritia.container.slot.StaticFakeSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.InfinityPotato;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating.BelieverTile;
import net.xiaoyang010.ex_enigmaticlegacy.Container.CelestialHTMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Effect.Drowning;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTags;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.NebulaArmor;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.NebulaArmorHelper;
import net.xiaoyang010.ex_enigmaticlegacy.Util.ColorText;
import vazkii.botania.common.block.decor.BlockTinyPotato;

import java.lang.reflect.Field;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class ModEventHandler {

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() == ModItems.PRISMATICRADIANCEINGOT.get()) {
            event.getToolTip().add(new TranslatableComponent("item.ex_enigmaticlegacy.prismaticradianceingot.desc").withStyle(ChatFormatting.GOLD));
        }
    }

    @SubscribeEvent
    public static void rightBlock(RightClickBlock event) {
        LivingEntity living = event.getEntityLiving();
        if (living instanceof Player player) {
            BlockHitResult hitVec = event.getHitVec();
            ItemStack stack = event.getItemStack();
            if (stack.isEmpty()) {
                BlockPos pos = hitVec.getBlockPos();
                Level world = player.getLevel();
                BlockState state = world.getBlockState(pos);

                boolean isInfinityPotato = state.getBlock() instanceof InfinityPotato;
                boolean isTinyPotato = state.getBlock() instanceof BlockTinyPotato;

                if (isInfinityPotato || isTinyPotato) {
                    int range = 4;
                    BlockPos pos1 = pos.offset(-range, 0, -range);
                    BlockPos pos2 = pos.offset(range, 1, range);
                    for (BlockPos blockPos : BlockPos.betweenClosed(pos1, pos2)) {
                        BlockEntity entity = world.getBlockEntity(blockPos);
                        if (entity instanceof BelieverTile believer) {
                            believer.addRightMana(isInfinityPotato);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void playerHurt(LivingHurtEvent event){
        if (event.getEntityLiving() instanceof Player player){
            float amount = event.getAmount();
            ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
            ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
            int mana = (int) Math.ceil(amount * 1000.f / 4.f); //应消耗的魔力 = 伤害值 * 1000mana
            int headMana = NebulaArmor.getManaInternal(head);
            int chestMana = NebulaArmor.getManaInternal(chest);
            int legsMana = NebulaArmor.getManaInternal(legs);
            int feetMana = NebulaArmor.getManaInternal(feet);
            //第二版 单件各减25%
            if (NebulaArmorHelper.isNebulaArmor(head) && headMana > mana){
                NebulaArmor.setManaInternal(head, headMana - mana);
                event.setAmount(amount - amount / 4.0f);
            }
            if (NebulaArmorHelper.isNebulaArmor(chest) && chestMana > mana){
                NebulaArmor.setManaInternal(chest, chestMana - mana);
                event.setAmount(amount - amount / 4.0f);
            }
            if (NebulaArmorHelper.isNebulaArmor(legs) && legsMana > mana){
                NebulaArmor.setManaInternal(legs, legsMana - mana);
                event.setAmount(amount - amount / 4.0f);
            }
            if (NebulaArmorHelper.isNebulaArmor(feet) && feetMana > mana){
                NebulaArmor.setManaInternal(feet, feetMana - mana);
                event.setAmount(amount - amount / 4.0f);
            }
            if (NebulaArmorHelper.hasNebulaArmor(player) && headMana > mana && chestMana > mana && legsMana > mana && feetMana > mana){
                event.setAmount(0);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(PotionEvent.PotionRemoveEvent event) {
        if (event.getPotion() instanceof Drowning) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level.isClientSide) {
            Player player = event.player;
            AbstractContainerMenu container = player.containerMenu;

            if (container != null) {
                boolean isAllowedContainer = false;
                BlockPos containerPos = null;
                BlockEntity blockEntity = null;

                // 1. 特定容器检查
                if (container instanceof MachineMenu<?> machineMenu) {
                    blockEntity = machineMenu.machineTile;
                    if (blockEntity != null) {
                        isAllowedContainer = blockEntity.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER);
                    }
                }
                else if (container instanceof CelestialHTMenu celestialMenu) {
                    containerPos = new BlockPos(celestialMenu.x, celestialMenu.y, celestialMenu.z);
                    blockEntity = player.level.getBlockEntity(containerPos);
                    if (blockEntity != null) {
                        isAllowedContainer = blockEntity.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER);
                    }
                }
                // 铁砧检查
                else if (container instanceof ItemCombinerMenu itemCombiner) {
                    try {
                        Field accessField = ItemCombinerMenu.class.getDeclaredField("access");
                        accessField.setAccessible(true);
                        ContainerLevelAccess access = (ContainerLevelAccess) accessField.get(itemCombiner);

                        Optional<Boolean> allowed = access.evaluate((level, pos) -> {
                            BlockState state = level.getBlockState(pos);
                            return state.is(ModTags.Blocks.SPECTRITE_CONTAINER);
                        });
                        isAllowedContainer = allowed.orElse(false);
                    } catch (Exception ignored) {}
                }
                // 其他通用容器检查
                else {
                    try {
                        // 尝试获取 ContainerLevelAccess
                        for (Field field : container.getClass().getDeclaredFields()) {
                            field.setAccessible(true);
                            Object value = field.get(container);
                            if (value instanceof ContainerLevelAccess) {
                                ContainerLevelAccess access = (ContainerLevelAccess) value;
                                Optional<BlockState> state = access.evaluate((level, pos) -> level.getBlockState(pos));
                                if (state.isPresent()) {
                                    isAllowedContainer = state.get().is(ModTags.Blocks.SPECTRITE_CONTAINER);
                                    break;
                                }
                            }
                        }

                        // 如果没找到 ContainerLevelAccess，尝试其他方式
                        if (!isAllowedContainer) {
                            for (Field field : container.getClass().getDeclaredFields()) {
                                field.setAccessible(true);
                                Object value = field.get(container);
                                if (value instanceof BlockPos) {
                                    containerPos = (BlockPos) value;
                                    break;
                                } else if (value instanceof BlockEntity) {
                                    blockEntity = (BlockEntity) value;
                                    containerPos = blockEntity.getBlockPos();
                                    break;
                                }
                            }

                            // 如果找到了位置，检查方块
                            if (containerPos != null) {
                                if (blockEntity == null) {
                                    blockEntity = player.level.getBlockEntity(containerPos);
                                }
                                if (blockEntity != null) {
                                    isAllowedContainer = blockEntity.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER);
                                } else {
                                    BlockState state = player.level.getBlockState(containerPos);
                                    isAllowedContainer = state.is(ModTags.Blocks.SPECTRITE_CONTAINER);
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }

                // 2. 检查槽位
                for (Slot slot : container.slots) {
                    // 跳过玩家物品栏和特殊槽位
                    if (slot.container instanceof Inventory ||
                            slot instanceof StaticFakeSlot ||
                            slot instanceof ScrollingFakeSlot) {
                        continue;
                    }

                    ItemStack slotItem = slot.getItem();
                    if (slotItem.is(ModTags.Items.SPECTRITE_ITEMS)) {
                        boolean isAllowed = isAllowedContainer;

                        // 处理特殊槽位
                        if (container instanceof ItemCombinerMenu && slot.container instanceof ResultContainer) {
                            isAllowed = isAllowedContainer;
                        }
                        else if (slot.container instanceof CraftingContainer || slot.container instanceof ResultContainer) {
                            isAllowed = isAllowedContainer;
                        }
                        // 检查其他容器槽位
                        else if (!isAllowed && slot.container instanceof Container) {
                            BlockEntity slotBlockEntity = null;

                            if (slot.container instanceof BlockEntity) {
                                slotBlockEntity = (BlockEntity) slot.container;
                            }
                            else if (slot.container instanceof CompoundContainer) {
                                CompoundContainer compoundContainer = (CompoundContainer) slot.container;
                                if (compoundContainer.container1 instanceof BlockEntity) {
                                    slotBlockEntity = (BlockEntity) compoundContainer.container1;
                                } else if (compoundContainer.container2 instanceof BlockEntity) {
                                    slotBlockEntity = (BlockEntity) compoundContainer.container2;
                                }
                            }

                            if (slotBlockEntity != null) {
                                isAllowed = slotBlockEntity.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER);
                            }
                        }

                        // 如果不允许，返回物品
                        if (!isAllowed) {
                            ItemStack itemToReturn = slotItem.copy();
                            slot.set(ItemStack.EMPTY);
                            returnItemToPlayerInventory(player, itemToReturn);

//                            player.displayClientMessage(
//                                    new TranslatableComponent("msg.ex_enigmaticlegacy.container_not_allowed")
//                                            .withStyle(ChatFormatting.RED),
//                                    true
                            player.displayClientMessage(
                                    Component.nullToEmpty(ColorText.GetColor1(I18n.get("msg.ex_enigmaticlegacy.container_not_allowed"))),
                                    true
                            );
                        }
                    }
                }
            }
        }
    }

//    @SubscribeEvent
//    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
//        if (event.phase == TickEvent.Phase.END && !event.player.level.isClientSide) {
//            Player player = event.player;
//            AbstractContainerMenu container = player.containerMenu;
//
//            if (container != null) {
//                boolean isAllowedContainer = false;
//                BlockPos containerPos = null;
//                BlockEntity blockEntity = null;
//
//                if (container instanceof MachineMenu<?> machineMenu) {
//                    blockEntity = machineMenu.machineTile;
//                    if (blockEntity != null) {
//                        isAllowedContainer = blockEntity.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER);
//                    }
//                }
//                else if (container instanceof CelestialHTMenu celestialMenu) {
//                    containerPos = new BlockPos(celestialMenu.x, celestialMenu.y, celestialMenu.z);
//                    blockEntity = player.level.getBlockEntity(containerPos);
//                    if (blockEntity != null) {
//                        isAllowedContainer = blockEntity.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER);
//                    }
//                }
//
//                else {
//                    try {
//                        for (Field field : container.getClass().getDeclaredFields()) {
//                            field.setAccessible(true);
//                            Object value = field.get(container);
//                            if (value instanceof BlockPos) {
//                                containerPos = (BlockPos) value;
//                                break;
//                            } else if (value instanceof BlockEntity) {
//                                blockEntity = (BlockEntity) value;
//                                containerPos = blockEntity.getBlockPos();
//                                break;
//                            } else if (value instanceof ContainerLevelAccess) {
//                                ContainerLevelAccess access = (ContainerLevelAccess) value;
//                                Optional<BlockPos> pos = access.evaluate((l, p) -> p);
//                                if (pos.isPresent()) {
//                                    containerPos = pos.get();
//                                    break;
//                                }
//                            }
//                        }
//
//                        if (containerPos != null && blockEntity == null) {
//                            blockEntity = player.level.getBlockEntity(containerPos);
//                        }
//
//                        if (blockEntity != null) {
//                            isAllowedContainer = blockEntity.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER);
//                        } else if (containerPos != null) {
//                            BlockState state = player.level.getBlockState(containerPos);
//                            isAllowedContainer = state.is(ModTags.Blocks.SPECTRITE_CONTAINER);
//                        }
//                    } catch (Exception ignored) {}
//                }
//
//                for (Slot slot : container.slots) {
//                    if (slot.container instanceof Inventory ||
//                            slot instanceof StaticFakeSlot ||
//                            slot instanceof ScrollingFakeSlot) {
//                        continue;
//                    }
//
//                    ItemStack slotItem = slot.getItem();
//                    if (slotItem.is(ModTags.Items.SPECTRITE_ITEMS)) {
//                        boolean isAllowed = isAllowedContainer;
//
//                        if (slot.container instanceof CraftingContainer || slot.container instanceof ResultContainer) {
//                            isAllowed = isAllowedContainer;
//                        }
//                        else if (!isAllowed && slot.container instanceof Container) {
//                            BlockEntity slotBlockEntity = null;
//
//                            if (slot.container instanceof BlockEntity) {
//                                slotBlockEntity = (BlockEntity) slot.container;
//                            }
//                            else if (slot.container instanceof CompoundContainer) {
//                                CompoundContainer compoundContainer = (CompoundContainer) slot.container;
//                                if (compoundContainer.container1 instanceof BlockEntity) {
//                                    slotBlockEntity = (BlockEntity) compoundContainer.container1;
//                                } else if (compoundContainer.container2 instanceof BlockEntity) {
//                                    slotBlockEntity = (BlockEntity) compoundContainer.container2;
//                                }
//                            }
//
//                            if (slotBlockEntity != null) {
//                                isAllowed = slotBlockEntity.getBlockState().is(ModTags.Blocks.SPECTRITE_CONTAINER);
//                            }
//                        }
//
//                        if (!isAllowed) {
//                            ItemStack itemToReturn = slotItem.copy();
//                            slot.set(ItemStack.EMPTY);
//                            returnItemToPlayerInventory(player, itemToReturn);
//
//                            player.displayClientMessage(
//                                    new TranslatableComponent("msg.ex_enigmaticlegacy.container_not_allowed")
//                                            .withStyle(ChatFormatting.RED),
//                                    true
//                            );
//                        }
//                    }
//                }
//            }
//        }
//    }

    @SubscribeEvent
    public static void onItemPlaceInContainer(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getPlayer();
        Level world = event.getWorld();
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.is(ModTags.Items.SPECTRITE_ITEMS)) {
            boolean isAllowed = state.is(ModTags.Blocks.SPECTRITE_CONTAINER);

            if (!isAllowed) {
                if (!world.isClientSide) {
                    player.displayClientMessage(
                            Component.nullToEmpty(ColorText.GetColor1(I18n.get("msg.ex_enigmaticlegacy.container_not_allowed"))),
                            true
//                    player.displayClientMessage(
//                            new TranslatableComponent("msg.ex_enigmaticlegacy.container_not_allowed")
//                                    .withStyle(ChatFormatting.RED),
//                            true
                    );
                }
                event.setCanceled(true);
                return;
            }
        }
    }

    private static void returnItemToPlayerInventory(Player player, ItemStack stack) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack invStack = player.getInventory().items.get(i);
            if (ItemStack.isSame(invStack, stack) && invStack.getCount() < invStack.getMaxStackSize()) {
                int spaceLeft = invStack.getMaxStackSize() - invStack.getCount();
                int amountToAdd = Math.min(spaceLeft, stack.getCount());
                invStack.grow(amountToAdd);
                stack.shrink(amountToAdd);
                if (stack.isEmpty()) return;
            }
        }

        if (!stack.isEmpty()) {
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                if (player.getInventory().items.get(i).isEmpty()) {
                    player.getInventory().items.set(i, stack.copy());
                    stack.setCount(0);
                    return;
                }
            }
        }

        if (!stack.isEmpty()) {
            player.drop(stack, false);
        }
    }
}