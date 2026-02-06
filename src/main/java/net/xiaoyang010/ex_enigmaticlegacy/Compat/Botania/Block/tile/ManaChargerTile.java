package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
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
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block.IWandBindable;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.IManaSpark;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.client.core.helper.RenderHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ManaChargerTile extends BlockEntity implements IWandBindable, ISparkAttachable, IManaReceiver, Container {
    private static final int MANA_SPEED = 11240;
    private static final int MAX_MANA = 1000000; // 最大魔力容量
    private final LazyOptional<IManaReceiver> manaReceiverCap = LazyOptional.of(() -> this);
    private final LazyOptional<ISparkAttachable> sparkAttachableCap = LazyOptional.of(() -> this);
    public boolean requestUpdate = false;
    private int currentMana = 0; // 当前魔力值
    private BlockPos receiverPos = null;
    public int[] clientTick = new int[]{0, 0, 3, 12, 6};

    private final ItemStackHandler inventory = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            requestUpdate = true;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM).isPresent();
        }
    };

    private final LazyOptional<IItemHandler> inventoryCapability = LazyOptional.of(() -> inventory);

    public ManaChargerTile(@NotNull BlockEntityType<ManaChargerTile> manaChargerTileBlockEntityType, BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_CHARGER_TILE.get(), pos, state);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryCapability.invalidate();
        manaReceiverCap.invalidate();
        sparkAttachableCap.invalidate();
    }

    public void tick() {
        boolean hasUpdate = false;

        if (!level.isClientSide && requestUpdate) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            requestUpdate = false;
        }

        if (!level.isClientSide) {
            IManaSpark spark = getAttachedSpark();
            if (spark != null) {
                SparkHelper.getSparksAround(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, spark.getNetwork())
                        .filter(otherSpark -> spark != otherSpark && otherSpark.getAttachedManaReceiver() instanceof IManaPool)
                        .forEach(os -> os.registerTransfer(spark));
            }
        }

        ISparkAttachable sparkReceiver = getSparkReceiver();
        IManaReceiver manaReceiver = getManaReceiver();

        if (sparkReceiver != null && manaReceiver != null) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    var manaItemOpt = stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM);
                    if (manaItemOpt.isPresent()) {
                        IManaItem manaItem = manaItemOpt.orElse(null);

                        if (i == 0) {
                            if (manaItem.getMana() > 0 && !manaReceiver.isFull()) {
                                int availableMana = sparkReceiver.getAvailableSpaceForMana();
                                int manaVal = Math.min(
                                        Math.min(manaItem.getMaxMana() / 256, MANA_SPEED) * 3,
                                        Math.min(availableMana, manaItem.getMana())
                                );

                                if (!level.isClientSide) {
                                    manaItem.addMana(-manaVal);
                                    if (level.getGameTime() % 15L == 0L) {
                                        hasUpdate = true;
                                    }
                                } else {
                                    clientTick[i]++;
                                }

                                manaReceiver.receiveMana(manaVal);
                            }
                        } else {
                            if (manaReceiver.getCurrentMana() > 0 &&
                                    manaItem.getMana() < manaItem.getMaxMana()) {

                                int manaVal = Math.min(
                                        Math.min(manaItem.getMaxMana() / 256, MANA_SPEED),
                                        Math.min(manaReceiver.getCurrentMana(),
                                                manaItem.getMaxMana() - manaItem.getMana())
                                );

                                if (!level.isClientSide) {
                                    manaItem.addMana(manaVal);
                                    if (level.getGameTime() % 15L == 0L) {
                                        hasUpdate = true;
                                    }
                                } else if (ConfigHandler.useManaChargerAnimation.get()) {
                                    clientTick[i]++;
                                }

                                manaReceiver.receiveMana(-manaVal);
                            }
                        }
                    }
                }
            }

            requestUpdate = hasUpdate;
        }

        if (sparkReceiver == null || manaReceiver == null) {
            if (!level.isClientSide && currentMana > 0) {
                receiveMana(-10);
            }
        }
    }

    @Nullable
    private ISparkAttachable getSparkReceiver() {
        if (level != null && receiverPos != null) {
            BlockEntity tile = level.getBlockEntity(receiverPos);
            if (tile != null) {
                var sparkCapability = tile.getCapability(BotaniaForgeCapabilities.SPARK_ATTACHABLE);
                if (sparkCapability.isPresent()) {
                    return sparkCapability.orElse(null);
                }
            }
        }

        if (receiverPos != null) {
            receiverPos = null;
        }

        return null;
    }

    @Nullable
    private IManaReceiver getManaReceiver() {
        if (level != null && receiverPos != null) {
            BlockEntity tile = level.getBlockEntity(receiverPos);
            if (tile != null) {
                var manaCapability = tile.getCapability(BotaniaForgeCapabilities.MANA_RECEIVER);
                if (manaCapability.isPresent()) {
                    return manaCapability.orElse(null);
                }
            }
        }
        return null;
    }

    @Override
    public boolean canSelect(Player player, ItemStack wand, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public boolean bindTo(Player player, ItemStack wand, BlockPos pos, Direction side) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile == null) {
            return false;
        }

        boolean isFar = Math.abs(worldPosition.getX() - pos.getX()) >= 10 ||
                Math.abs(worldPosition.getY() - pos.getY()) >= 10 ||
                Math.abs(worldPosition.getZ() - pos.getZ()) >= 10;
        if (isFar) {
            return false;
        }

        var sparkCapability = tile.getCapability(BotaniaForgeCapabilities.SPARK_ATTACHABLE);
        var manaCapability = tile.getCapability(BotaniaForgeCapabilities.MANA_RECEIVER);

        if (sparkCapability.isPresent() && manaCapability.isPresent()) {
            IManaReceiver manaReceiver = manaCapability.orElse(null);
            if (manaReceiver.canReceiveManaFromBursts()) {
                if (!level.isClientSide) {
                    receiverPos = pos.immutable();
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                    setChanged();
                }
                return true;
            }
        }

        return false;
    }

    @Nullable
    @Override
    public BlockPos getBinding() {
        return receiverPos;
    }

    public void onWanded(Player player, ItemStack wand) {
        if (player != null) {
            level.playSound(player, worldPosition,
                    SoundEvents.NOTE_BLOCK_PLING, SoundSource.BLOCKS, 0.11F, 1.0F);
        }
    }

    public static float getManaPercent(ItemStack stack) {
        var manaItemOpt = stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM);
        if (manaItemOpt.isPresent()) {
            IManaItem manaItem = manaItemOpt.orElse(null);
            return (float) manaItem.getMana() / ((float) manaItem.getMaxMana() / 100.0F);
        }
        return 0.0F;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", inventory.serializeNBT());

        if (receiverPos != null) {
            tag.putInt("bindingX", receiverPos.getX());
            tag.putInt("bindingY", receiverPos.getY());
            tag.putInt("bindingZ", receiverPos.getZ());
        }

        tag.putBoolean("requestUpdate", requestUpdate);
        tag.putInt("currentMana", currentMana);

        for (int i = 0; i < clientTick.length; i++) {
            tag.putInt("clientTick" + i, clientTick[i]);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("inventory"));

        if (tag.contains("bindingX")) {
            receiverPos = new BlockPos(
                    tag.getInt("bindingX"),
                    tag.getInt("bindingY"),
                    tag.getInt("bindingZ")
            );
        }

        requestUpdate = tag.getBoolean("requestUpdate");
        currentMana = tag.getInt("currentMana");

        for (int i = 0; i < clientTick.length; i++) {
            if (tag.contains("clientTick" + i)) {
                clientTick[i] = tag.getInt("clientTick" + i);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return inventoryCapability.cast();
        }
        if (cap == BotaniaForgeCapabilities.MANA_RECEIVER) {
            return manaReceiverCap.cast();
        }
        if (cap == BotaniaForgeCapabilities.SPARK_ATTACHABLE) {
            return sparkAttachableCap.cast();
        }
        if (cap == BotaniaForgeClientCapabilities.WAND_HUD) {
            return LazyOptional.of(() -> new WandHud(this)).cast();
        }
        return super.getCapability(cap, side);
    }

    @OnlyIn(Dist.CLIENT)
    public static class WandHud implements IWandHUD {
        private final ManaChargerTile charger;

        private static final int CIRCLE_Y_OFFSET = -70;
        private static final int CIRCLE_RADIUS = 42;
        private static final float RECEIVER_ICON_SCALE = 0.75f;
        private static final int Z_LEVEL = 200;

        public WandHud(ManaChargerTile charger) {
            this.charger = charger;
        }

        @Override
        public void renderHUD(PoseStack ms, Minecraft mc) {
            ItemStack chargerStack = new ItemStack(charger.getBlockState().getBlock());
            String name = chargerStack.getHoverName().getString();

            ISparkAttachable sparkReceiver = charger.getSparkReceiver();
            IManaReceiver manaReceiver = charger.getManaReceiver();

            if (sparkReceiver != null && manaReceiver != null) {
                int currentMana = manaReceiver.getCurrentMana();
                int maxMana = sparkReceiver.getAvailableSpaceForMana() + currentMana;

                int color = 0xF5FF62;
                BotaniaAPIClient.instance().drawSimpleManaHUD(ms, color, currentMana, maxMana, name);
                drawItemsInCircle(ms, mc);
            } else {
                int color = 0xB9B6B2;
                String unlinkedText = EComponent.translatable("gui.ex_enigmaticlegacy.mana_charger.unlinked").getString();
                BotaniaAPIClient.instance().drawSimpleManaHUD(ms, color, 0, MAX_MANA, name + " (" + unlinkedText + ")");
            }
        }

        private void drawItemsInCircle(PoseStack ms, Minecraft mc) {
            int centerX = mc.getWindow().getGuiScaledWidth() / 2;
            int centerY = mc.getWindow().getGuiScaledHeight() / 2 + CIRCLE_Y_OFFSET;
            int radius = CIRCLE_RADIUS;

            int itemCount = 0;
            for (int i = 0; i < charger.getInventorySize(); i++) {
                if (!charger.getStackInSlot(i).isEmpty()) {
                    itemCount++;
                }
            }

            if (itemCount == 0) return;

            float angle = -90.0f;
            float anglePer = 360.0f / (float) itemCount;

            for (int i = 0; i < charger.getInventorySize(); i++) {
                ItemStack stack = charger.getStackInSlot(i);
                if (stack.isEmpty()) continue;

                int xPos = (int) (centerX + Math.cos(angle * Math.PI / 180.0) * radius - 8.0);
                int yPos = (int) (centerY + Math.sin(angle * Math.PI / 180.0) * radius - 8.0);

                ms.pushPose();
                ms.translate(0, 0, Z_LEVEL);

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.75f);

                float manaPercent = ManaChargerTile.getManaPercent(stack) / 100.0f;
                RenderHelper.renderProgressPie(ms, xPos, yPos, manaPercent, stack);

                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.depthMask(true);
                GL11.glStencilMask(0xFF);
                RenderSystem.enableTexture();
                RenderSystem.disableBlend();
                GL11.glDisable(GL11.GL_STENCIL_TEST);

                ms.pushPose();
                ms.translate(0, 0, 1);
                RenderSystem.enableDepthTest();
                mc.getItemRenderer().renderAndDecorateItem(stack, xPos, yPos);
                RenderSystem.disableDepthTest();
                ms.popPose();

                if (i == 0 && charger.receiverPos != null) {
                    ms.pushPose();
                    ms.translate(xPos + 11.0, yPos + 10.0, 2.0);
                    ms.scale(RECEIVER_ICON_SCALE, RECEIVER_ICON_SCALE, RECEIVER_ICON_SCALE);

                    BlockEntity receiverTile = charger.level.getBlockEntity(charger.receiverPos);
                    if (receiverTile != null) {
                        ItemStack receiverStack = new ItemStack(
                                charger.level.getBlockState(charger.receiverPos).getBlock());
                        mc.getItemRenderer().renderAndDecorateItem(receiverStack, 0, 0);
                    }

                    ms.popPose();
                }

                ms.popPose();
                angle += anglePer;
            }
        }
    }

    public int getInventorySize() {
        return inventory.getSlots();
    }

    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public int getContainerSize() {
        return inventory.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return inventory.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return inventory.extractItem(slot, inventory.getStackInSlot(slot).getCount(), false);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.setStackInSlot(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        if (level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public Level getManaReceiverLevel() {
        return level;
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return worldPosition;
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
        currentMana = Math.max(0, Math.min(MAX_MANA, currentMana + mana));
        setChanged();
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return receiverPos != null && !isFull();
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public int getAvailableSpaceForMana() {
        return Math.max(0, MAX_MANA - currentMana);
    }

    @Override
    public IManaSpark getAttachedSpark() {
        if (level == null) return null;

        List<Entity> sparks = level.getEntitiesOfClass(Entity.class,
                new AABB(worldPosition.above(), worldPosition.above().offset(1, 1, 1)),
                entity -> entity instanceof IManaSpark);

        if (!sparks.isEmpty()) {
            Entity entity = sparks.get(0);
            return (IManaSpark) entity;
        }
        return null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return isFull() || receiverPos == null;
    }
}