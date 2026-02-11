package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud.ClientHelper;
import net.xiaoyang010.ex_enigmaticlegacy.api.IBoundRender;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.block.IWandable;
import vazkii.botania.common.item.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.TileInventory;

import javax.annotation.Nullable;

public class TileEngineerHopper extends TileInventory implements IBoundRender, Hopper, IWandable, IWandHUD {
    private int cooldown;
    private BlockPos[] invPos = new BlockPos[]{BlockPos.ZERO, BlockPos.ZERO};
    private Direction[] invSide = new Direction[]{null, null};
    private boolean bindType;
    public int redstoneSignal = 0;

    public TileEngineerHopper(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void serverTick() {
        this.redstoneSignal = 0;

        for(Direction dir : Direction.values()) {
            int redstoneSide = this.level.getSignal(this.worldPosition.relative(dir), dir);
            this.redstoneSignal = Math.max(this.redstoneSignal, redstoneSide);
        }

        if (this.redstoneSignal <= 0) {
            if (this.level != null && !this.level.isClientSide) {
                if (this.cooldown > 0) {
                    --this.cooldown;
                } else if (this.cooldown == 0) {
                    boolean hasUpdate = false;
                    ItemStack stack = this.getItem(0);

                    if (!stack.isEmpty()) {
                        hasUpdate = this.canExtractStack();
                    }

                    if (stack.isEmpty() || stack.getCount() != stack.getMaxStackSize()) {
                        hasUpdate = this.canInsertStack() || hasUpdate;
                    }

                    if (hasUpdate) {
                        this.cooldown = 8;
                    }

                    this.setChanged();
                }
            }
        }
    }

    @Override
    public boolean bindTo(Player player, ItemStack wand, BlockPos pos, Direction side) {
        BlockEntity blockEntity = player.getLevel().getBlockEntity(pos);
        boolean isFar = Math.abs(this.worldPosition.getX() - pos.getX()) >= 10 ||
                Math.abs(this.worldPosition.getY() - pos.getY()) >= 10 ||
                Math.abs(this.worldPosition.getZ() - pos.getZ()) >= 10;

        if (isFar) {
            return false;
        }

        int invCount = this.bindType ? 0 : 1;
        if (blockEntity instanceof TileEngineerHopper) {
            return false;
        } else if (blockEntity != null && blockEntity instanceof Container) {
            this.setDistantInventory(invCount, pos);
            this.invSide[invCount] = side;
            return true;
        } else {
            this.setDistantInventory(invCount, BlockPos.ZERO);
            this.invSide[invCount] = null;
            return false;
        }
    }

    public void changeBindType() {
        this.bindType = !this.bindType;
    }

    public void setDistantInventory(int count, BlockPos pos) {
        this.invPos[count] = pos;
    }

    public Container getDistantInventory(int count) {
        if (this.invPos[count].equals(BlockPos.ZERO)) {
            return null;
        }

        BlockEntity blockEntity = this.level.getBlockEntity(this.invPos[count]);
        if (blockEntity instanceof Container container) {
            return container;
        } else {
            this.setDistantInventory(count, BlockPos.ZERO);
            return null;
        }
    }

    @Override
    public boolean canSelect(Player player, ItemStack wand, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public BlockPos[] getBlocksCoord() {
        return new BlockPos[]{this.invPos[0], this.invPos[1]};
    }

    @Override
    public BlockPos getBinding() {
        return null;
    }

    @Override
    public boolean onUsedByWand(@Nullable Player player, ItemStack wand, Direction side) {
        return false;
    }

    @Override
    public void renderHUD(PoseStack poseStack, Minecraft mc) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int x = screenWidth / 2 - 7;
        int y = screenHeight / 2 + 16;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ClientHelper.drawArrow(poseStack, x - 2, y, true);

        for(int i = 0; i < 2; ++i) {
            ItemStack stack = ItemStack.EMPTY;
            boolean hasInv = false;
            int posX = x + (i == 0 ? 32 : -32);

            Container inventory = this.getDistantInventory(i);
            if (inventory != null && inventory instanceof BlockEntity blockEntity) {
                Block block = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos()).getBlock();
                stack = new ItemStack(block);
                hasInv = true;
            }

            Gui.fill(poseStack, posX - 4, y - 4, posX + 20, y + 20, 0x44000000);
            Gui.fill(poseStack, posX - 2, y - 2, posX + 18, y + 18, 0x44000000);

            if (!stack.isEmpty()) {
                RenderSystem.enableDepthTest();
                mc.getItemRenderer().renderAndDecorateItem(stack, posX, y);
                RenderSystem.disableDepthTest();
            }

            int invCount = this.bindType ? 0 : 1;
            if (invCount == i) {
                poseStack.pushPose();
                poseStack.translate(0.0F, 0.0F, 300.0F);
                RenderSystem.enableDepthTest();
                mc.getItemRenderer().renderGuiItem(new ItemStack(ModItems.twigWand), posX + 10, y + 7);
                RenderSystem.disableDepthTest();
                poseStack.popPose();
            }

            Font font = mc.font;
            if (!hasInv) {
                font.drawShadow(poseStack, "✗", posX + 5, y + 6, 0x4C4C4C);
                font.drawShadow(poseStack, "✗", posX + 5, y + 5, 0xD1D1AD);
            }
            RenderSystem.enableDepthTest();
        }

        RenderSystem.disableBlend();
    }

    @Override
    public void writePacketNBT(CompoundTag tag) {
        super.writePacketNBT(tag);
        tag.putInt("cooldown", this.cooldown);
        tag.putBoolean("bindType", this.bindType);

        CompoundTag pos0 = new CompoundTag();
        pos0.putInt("x", this.invPos[0].getX());
        pos0.putInt("y", this.invPos[0].getY());
        pos0.putInt("z", this.invPos[0].getZ());
        tag.put("bindingPos0", pos0);

        CompoundTag pos1 = new CompoundTag();
        pos1.putInt("x", this.invPos[1].getX());
        pos1.putInt("y", this.invPos[1].getY());
        pos1.putInt("z", this.invPos[1].getZ());
        tag.put("bindingPos1", pos1);

        if (this.invSide[0] != null) {
            tag.putInt("bindingSide0", this.invSide[0].ordinal());
        } else {
            tag.putInt("bindingSide0", -1);
        }

        if (this.invSide[1] != null) {
            tag.putInt("bindingSide1", this.invSide[1].ordinal());
        } else {
            tag.putInt("bindingSide1", -1);
        }
    }

    @Override
    public void readPacketNBT(CompoundTag tag) {
        super.readPacketNBT(tag);
        this.cooldown = tag.getInt("cooldown");
        this.bindType = tag.getBoolean("bindType");

        if (tag.contains("bindingPos0")) {
            CompoundTag pos0 = tag.getCompound("bindingPos0");
            this.invPos[0] = new BlockPos(pos0.getInt("x"), pos0.getInt("y"), pos0.getInt("z"));
        }

        if (tag.contains("bindingPos1")) {
            CompoundTag pos1 = tag.getCompound("bindingPos1");
            this.invPos[1] = new BlockPos(pos1.getInt("x"), pos1.getInt("y"), pos1.getInt("z"));
        }

        int side0 = tag.getInt("bindingSide0");
        this.invSide[0] = side0 >= 0 ? Direction.values()[side0] : null;

        int side1 = tag.getInt("bindingSide1");
        this.invSide[1] = side1 >= 0 ? Direction.values()[side1] : null;
    }

    private boolean canExtractStack() {
        Container inv = this.getDistantInventory(0);
        if (inv == null) {
            return false;
        }

        Direction side = this.invSide[0];
        ItemStack hopperStack = this.getItem(0);

        if (hopperStack.isEmpty()) {
            return false;
        }

        ItemStack remaining = HopperBlockEntity.addItem(this, inv, hopperStack.copy(), side);

        if (remaining.isEmpty() || remaining.getCount() < hopperStack.getCount()) {
            if (remaining.isEmpty()) {
                this.setItem(0, ItemStack.EMPTY);
            } else {
                this.setItem(0, remaining);
            }
            inv.setChanged();
            return true;
        }

        return false;
    }

    private boolean canInsertStack() {
        Container inv = this.getDistantInventory(1);
        if (inv != null) {
            Direction side = this.invSide[1];
            int takeCount = this.getTakeCount(inv, side);
            if (takeCount <= 0) {
                return false;
            }

            if (inv instanceof WorldlyContainer && side != null) {
                WorldlyContainer sidedInv = (WorldlyContainer)inv;
                int[] slots = sidedInv.getSlotsForFace(side);

                for(int k = 0; k < slots.length; ++k) {
                    if (this.tryInsertItem(inv, slots[k], side, takeCount)) {
                        return true;
                    }
                }
            } else {
                int i = inv.getContainerSize();

                for(int j = 0; j < i; ++j) {
                    if (this.tryInsertItem(inv, j, side, takeCount)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private int getTakeCount(Container inv, Direction side) {
        if (inv instanceof WorldlyContainer && side != null) {
            WorldlyContainer sideInv = (WorldlyContainer)inv;
            int[] slots = sideInv.getSlotsForFace(side);

            for(int i = 0; i < slots.length; ++i) {
                ItemStack stack = sideInv.getItem(slots[i]);
                if (!stack.isEmpty()) {
                    if (this.getItem(0).isEmpty()) {
                        return Math.min(stack.getMaxStackSize(), this.getMaxStackSize());
                    }

                    if (this.getMaxStackSize() != this.getItem(0).getCount() &&
                            this.getItem(0).getCount() != this.getItem(0).getMaxStackSize() &&
                            ItemStack.isSameItemSameTags(stack, this.getItem(0))) {
                        return this.getMaxStackSize() - this.getItem(0).getCount();
                    }
                }
            }
        } else {
            for(int i = 0; i < inv.getContainerSize(); ++i) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty()) {
                    if (this.getItem(0).isEmpty()) {
                        return Math.min(stack.getMaxStackSize(), this.getMaxStackSize());
                    }

                    if (this.getMaxStackSize() != this.getItem(0).getCount() &&
                            this.getItem(0).getCount() != this.getItem(0).getMaxStackSize() &&
                            ItemStack.isSameItemSameTags(stack, this.getItem(0))) {
                        return this.getMaxStackSize() - this.getItem(0).getCount();
                    }
                }
            }
        }

        return 0;
    }

    private boolean tryInsertItem(Container inv, int slot, Direction side, int takeCount) {
        ItemStack itemstack = inv.getItem(slot);
        if (!itemstack.isEmpty() && canTakeItemFromContainer(inv, itemstack, slot, side)) {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = HopperBlockEntity.addItem(inv, this, inv.removeItem(slot, takeCount), side);
            if (itemstack2.isEmpty() || itemstack2.getCount() == 0) {
                inv.setChanged();
                return true;
            }

            inv.setItem(slot, itemstack1);
        }

        return false;
    }

    private static boolean canTakeItemFromContainer(Container inv, ItemStack stack, int slot, Direction side) {
        return !(inv instanceof WorldlyContainer) || ((WorldlyContainer)inv).canTakeItemThroughFace(slot, stack, side);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    public double getLevelX() {
        return (double)this.worldPosition.getX() + 0.5D;
    }

    public double getLevelY() {
        return (double)this.worldPosition.getY() + 0.5D;
    }

    public double getLevelZ() {
        return (double)this.worldPosition.getZ() + 0.5D;
    }
}