package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Client.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Util.ClientHelper;
import vazkii.botania.api.block.IWandBindable;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.IManaSpark;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.handler.ModSounds;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class TileManaCharger extends BlockEntity implements IWandHUD, IWandBindable, IManaReceiver, ISparkAttachable {
    private static final int MANA_SPEED = 11240;
    private static final int MAX_MANA = 1000000;
    private static final int ITEM_SLOTS = 5;

    private final ItemStackHandler itemHandler = createHandler();
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);

    private int currentMana = 0;
    public boolean requestUpdate;
    private int clientMana = -1;
    private BlockPos receiverPos = BlockPos.ZERO;
    public int[] clientTick = new int[]{0, 0, 3, 12, 6};

    public TileManaCharger(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(ITEM_SLOTS) {
            @Override
            protected void onContentsChanged(int slot) {
                if (isValidSlot(slot)) {  // 添加槽位检查
                    setChanged();
                    if (level != null && !level.isClientSide) {
                        VanillaPacketDispatcher.dispatchTEToNearbyPlayers(TileManaCharger.this);
                    }
                }
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (!isValidSlot(slot)) {  // 添加槽位检查
                    return false;
                }
                return stack.getItem() instanceof IManaItem;
            }

            @Override
            @Nonnull
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (!isValidSlot(slot)) {  // 添加槽位检查
                    return stack;
                }
                if (!(stack.getItem() instanceof IManaItem)) {
                    return stack;
                }
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    private boolean isValidSlot(int slot) {
        return slot >= 0 && slot < ITEM_SLOTS;
    }

    public void tick() {
        if (level == null) return;

        boolean hasUpdate = false;
        if (!level.isClientSide && requestUpdate) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
            requestUpdate = false;
        }

        ISparkAttachable receiver = getReceiver();
        if (receiver != null) {
            for (int i = 0; i < ITEM_SLOTS; i++) {
                ItemStack stack = getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof IManaItem manaItem) {
                    if (i == 0) {
                        hasUpdate |= handleManaExport(stack, manaItem, receiver, i);
                    } else {
                        hasUpdate |= handleManaImport(stack, manaItem, receiver, i);
                    }
                }
            }
            requestUpdate = hasUpdate;
        }
    }

    public ItemStack getItem(int slot) {
        if (!isValidSlot(slot)) {
            return ItemStack.EMPTY;
        }
        return itemHandler.getStackInSlot(slot);
    }

    private boolean handleManaExport(ItemStack stack, IManaItem manaItem, ISparkAttachable receiver, int slot) {
        boolean updated = false;
        if (manaItem.getMana() > 0 && !receiver.areIncomingTranfersDone()
                && receiver instanceof IManaPool manaPool
                && manaItem.canExportManaToPool((BlockEntity) manaPool)) {

            int availableMana = receiver.getAvailableSpaceForMana();
            int manaVal = Math.min(
                    Math.min(manaItem.getMaxMana() / 256, MANA_SPEED) * 3,
                    Math.min(availableMana, manaItem.getMana())
            );

            if (!level.isClientSide) {
                manaItem.addMana(-manaVal);
                if (level.getGameTime() % 15L == 0L) {
                    updated = true;
                }
            } else {
                clientTick[slot]++;
            }

            manaPool.receiveMana(manaVal);
        }
        return updated;
    }

    private boolean handleManaImport(ItemStack stack, IManaItem manaItem, ISparkAttachable receiver, int slot) {
        boolean updated = false;
        if (receiver instanceof IManaPool manaPool &&
                manaPool.getCurrentMana() > 0 &&
                manaItem.getMana() < manaItem.getMaxMana() &&
                manaItem.canReceiveManaFromPool((BlockEntity) manaPool)) {

            int manaVal = Math.min(
                    Math.min(manaItem.getMaxMana() / 256, MANA_SPEED),
                    Math.min(manaPool.getCurrentMana(),
                            manaItem.getMaxMana() - manaItem.getMana())
            );

            if (!level.isClientSide) {
                manaItem.addMana(manaVal);
                if (level.getGameTime() % 15L == 0L) {
                    updated = true;
                }
            } else if (ConfigHandler.useManaChargerAnimation.get()) {
                clientTick[slot]++;
            }

            manaPool.receiveMana(-manaVal);
        }
        return updated;
    }

    @Override
    public Level getManaReceiverLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getBlockPos();
    }

    @Override
    public int getCurrentMana() {
        return currentMana;
    }

    @Override
    public boolean isFull() {
        return currentMana >= MAX_MANA;
    }

    @Override
    public void receiveMana(int mana) {
        this.currentMana = Math.min(MAX_MANA, this.currentMana + mana);
        setChanged();
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }

    @Override
    public int getAvailableSpaceForMana() {
        return Math.max(0, MAX_MANA - currentMana);
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public IManaSpark getAttachedSpark() {
        if (level != null) {
            List<Entity> sparks = level.getEntitiesOfClass(Entity.class,
                    new AABB(worldPosition.above(), worldPosition.above().offset(1, 1, 1)),
                    e -> e instanceof IManaSpark);
            if (!sparks.isEmpty()) {
                return (IManaSpark) sparks.get(0);
            }
        }
        return null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return isFull();
    }

    @Override
    public boolean canSelect(Player player, ItemStack wand, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public boolean bindTo(Player player, ItemStack wand, BlockPos pos, Direction side) {
        if (level == null) return false;

        BlockEntity tile = level.getBlockEntity(pos);
        boolean isFar = Math.abs(getBlockPos().getX() - pos.getX()) >= 10 ||
                Math.abs(getBlockPos().getY() - pos.getY()) >= 10 ||
                Math.abs(getBlockPos().getZ() - pos.getZ()) >= 10;

        if (isFar) {
            return false;
        }

        // 修改类型检查逻辑
        if (tile instanceof ISparkAttachable sparkAttachable &&
                tile instanceof IManaReceiver manaReceiver &&
                manaReceiver.canReceiveManaFromBursts()) {

            if (!level.isClientSide) {
                receiverPos = pos;
                VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
            }
            return true;
        }

        return false;
    }

    @Override
    public BlockPos getBinding() {
        ISparkAttachable receiver = getReceiver();
        if (receiver == null) {
            return null;
        }
        return ((BlockEntity) receiver).getBlockPos();
    }

    public ISparkAttachable getReceiver() {
        ISparkAttachable receiver = null;
        if (level != null && !receiverPos.equals(BlockPos.ZERO)) {
            BlockEntity tile = level.getBlockEntity(receiverPos);
            if (tile instanceof ISparkAttachable) {
                receiver = (ISparkAttachable) tile;
            }
        }

        if (receiver == null) {
            receiverPos = BlockPos.ZERO;
        }

        return receiver;
    }

    public static float getManaPercent(ItemStack stack) {
        if (!(stack.getItem() instanceof IManaItem manaItem)) {
            return 0.0F;
        }
        return (float) manaItem.getMana() / ((float) manaItem.getMaxMana() / 100.0F);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("currentMana", currentMana);
        tag.putInt("bindingX", receiverPos.getX());
        tag.putInt("bindingY", receiverPos.getY());
        tag.putInt("bindingZ", receiverPos.getZ());
        tag.putBoolean("requestUpdate", requestUpdate);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        currentMana = tag.getInt("currentMana");
        receiverPos = new BlockPos(
                tag.getInt("bindingX"),
                tag.getInt("bindingY"),
                tag.getInt("bindingZ")
        );
        requestUpdate = tag.getBoolean("requestUpdate");
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
    }

    public ItemStackHandler getItemHandler() {
        return this.itemHandler;
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }

    public void setItem(int slot, ItemStack stack) {
        if (isValidSlot(slot)) {
            itemHandler.setStackInSlot(slot, stack);
        }
    }

    public void onWanded(Player player, ItemStack stack) {
        if (level == null || player == null) return;

        ISparkAttachable receiver = getReceiver();
        if (level.isClientSide && receiver != null && receiver instanceof IManaReceiver manaReceiver) {
            clientMana = manaReceiver.getCurrentMana();
        }

        player.playSound(ModSounds.ding, 0.11F, 1.0F);
    }

    @Override
    public void renderHUD(PoseStack ms, Minecraft mc) {
        if (mc.font == null) return;

        ms.pushPose();

        int scaledWidth = mc.getWindow().getGuiScaledWidth();
        int scaledHeight = mc.getWindow().getGuiScaledHeight();
        int xc = scaledWidth / 2;
        int yc = scaledHeight / 2;
        int radius = 42;

        int amt = 0;
        for (int i = 0; i < ITEM_SLOTS; i++) {
            if (!getItem(i).isEmpty()) {
                amt++;
            }
        }

        float angle = -90.0F;
        if (amt > 0) {
            for (int i = 0; i < ITEM_SLOTS; i++) {
                ItemStack stack = getItem(i);
                if (!stack.isEmpty()) {
                    float anglePer = 360.0F / amt;
                    double xPos = xc + Math.cos(angle * Math.PI / 180.0F) * radius - 8.0F;
                    double yPos = yc + Math.sin(angle * Math.PI / 180.0F) * radius - 8.0F;

                    ms.pushPose();
                    ms.translate(xPos, yPos, 0);

                    vazkii.botania.client.core.helper.RenderHelper.renderProgressPie(
                            ms,
                            0,
                            0,
                            getManaPercent(stack) / 100.0F,
                            stack
                    );

                    if (i == 0) {
                        ms.scale(0.75F, 0.75F, 0.75F);
                        ms.translate(11.0F, 10.0F, 0);
                        mc.getItemRenderer().renderGuiItem(
                                new ItemStack(ModBlocks.manaPool),
                                0, 0
                        );
                    }

                    ms.popPose();
                    angle += anglePer;
                }
            }
        }

        renderReceiverInfo(mc, ms, xc, scaledHeight);

        ms.popPose();
    }

    public boolean isItemSlotValid(int slot) {
        return isValidSlot(slot);
    }

    public int getContainerSize() {
        return ITEM_SLOTS;
    }

    @OnlyIn(Dist.CLIENT)
    private void renderReceiverInfo(Minecraft mc, PoseStack ms, int xc, int scaledHeight) {
        ISparkAttachable receiver = getReceiver();
        if (receiver != null) {
            BlockEntity receiverTile = (BlockEntity) receiver;
            ItemStack receiverStack = new ItemStack(
                    level.getBlockState(receiverTile.getBlockPos()).getBlock()
            );

            if (!receiverStack.isEmpty()) {
                renderReceiverItemAndName(mc, ms, xc, scaledHeight, receiverStack);
            }

            // 渲染魔力条
            if (receiver instanceof IManaReceiver manaReceiver) {
                ClientHelper.drawPoolManaHUD(
                        ms,
                        "ex_enigmaticlegacy.manaCharger.wandHud",
                        clientMana,
                        manaReceiver.getCurrentMana() + receiver.getAvailableSpaceForMana(),
                        0xB9F2FF
                );
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renderReceiverItemAndName(Minecraft mc, PoseStack ms, int xc, int scaledHeight, ItemStack receiverStack) {
        String stackName = receiverStack.getHoverName().getString();
        int width = 16 + mc.font.width(stackName) / 2;
        int x = xc - width;
        int y = scaledHeight / 2 + 48;

        // 渲染接收器名称
        GuiComponent.drawString(
                ms,
                mc.font,
                stackName,
                x + 20,
                y + 5,
                0xF4A460
        );

        mc.getItemRenderer().renderGuiItem(receiverStack, x, y);
    }

    // 额外的工具方法
    public void markForUpdate() {
        requestUpdate = true;
        if (level != null && !level.isClientSide) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        }
    }

    public boolean isValidReceiver(BlockEntity tile) {
        return tile instanceof ISparkAttachable &&
                tile instanceof IManaReceiver manaReceiver &&
                manaReceiver.canReceiveManaFromBursts();
    }

    public void clearBinding() {
        receiverPos = BlockPos.ZERO;
        markForUpdate();
    }

    public boolean hasValidReceiver() {
        return getReceiver() != null;
    }

    public int getTransferRate() {
        return MANA_SPEED;
    }

    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (!isValidSlot(slot)) {
            return false;
        }

        if (!(stack.getItem() instanceof IManaItem manaItem)) {
            return false;
        }

        if (slot == 0) {
            return !manaItem.isNoExport();
        } else {
            return true;
        }
    }

    public void syncClientAndServer() {
        if (level != null && !level.isClientSide) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        }
    }

    public boolean canHandleMana(int slot) {
        if (!isValidSlot(slot)) {  // 添加槽位检查
            return false;
        }

        ItemStack stack = getItem(slot);
        if (stack.isEmpty() || !(stack.getItem() instanceof IManaItem manaItem)) {
            return false;
        }

        ISparkAttachable receiver = getReceiver();
        if (!(receiver instanceof IManaPool manaPool)) {
            return false;
        }

        if (slot == 0) {
            return manaItem.canExportManaToPool((BlockEntity) manaPool);
        } else {
            return manaItem.canReceiveManaFromPool((BlockEntity) manaPool);
        }
    }
}