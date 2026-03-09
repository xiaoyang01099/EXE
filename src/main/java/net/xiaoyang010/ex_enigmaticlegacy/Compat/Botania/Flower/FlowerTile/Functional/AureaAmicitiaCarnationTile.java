package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Util.InterdimensionalFlowerRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AureaAmicitiaCarnationTile extends TileEntityFunctionalFlower {

    private static final String TAG_COOLDOWN = "cooldown";
    private static final String TAG_TARGET_DIM = "targetDim";
    private static final String TAG_TARGET_POS = "targetPos";
    private static final String TAG_FLOWER_ID = "flowerProcessorId";

    // 用于标记物品已被处理的NBT标签
    private static final String ITEM_PROCESSED_TAG = "interdimensional_flower_processed";
    // 用于标记物品来源的NBT标签
    private static final String ITEM_SOURCE_TAG = "interdimensional_source";

    private static final int TRANSFER_COST = 16000;
    private static final int COOLDOWN_TIME = 60;
    private static final int SCAN_RANGE = 3;
    private static final int ITEM_DETECTION_DELAY = 40; // 2秒延迟检测新物品

    private int cooldown = 0;
    private ResourceKey<Level> targetDimension = null;
    private BlockPos targetPos = BlockPos.ZERO;
    private UUID flowerProcessorId;

    public AureaAmicitiaCarnationTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        if (flowerProcessorId == null) {
            flowerProcessorId = UUID.randomUUID();
        }
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<AureaAmicitiaCarnationTile> {
        public FunctionalWandHud(AureaAmicitiaCarnationTile flower) {
            super(flower);
        }
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (level == null || level.isClientSide) return;

        registerSelf();

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        findTargetFlower();

        if (canTransfer()) {
            attemptTransfer();
        }
    }

    private void registerSelf() {
        if (level instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            InterdimensionalFlowerRegistry registry = InterdimensionalFlowerRegistry.getInstance(server);
            registry.registerFlower(level.dimension(), getBlockPos());
        }
    }

    private void findTargetFlower() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        MinecraftServer server = serverLevel.getServer();
        InterdimensionalFlowerRegistry registry = InterdimensionalFlowerRegistry.getInstance(server);

        InterdimensionalFlowerRegistry.FlowerLocation target = registry.findTargetFlower(level.dimension());

        if (target != null) {
            ServerLevel targetLevel = server.getLevel(target.dimension);
            if (targetLevel != null) {
                targetDimension = target.dimension;
                targetPos = target.position;
                setChanged();

                if (level.getGameTime() % 100 == 0) {
                    System.out.println("[" + level.dimension().location() + "] Found target: " +
                            target.dimension.location() + " at " + target.position);
                }
            }
        } else {
            if (targetDimension != null) {
                System.out.println("[" + level.dimension().location() + "] Lost target, clearing...");
                targetDimension = null;
                targetPos = BlockPos.ZERO;
                setChanged();
            }
        }
    }

    private boolean canTransfer() {
        boolean canTransfer = getMana() >= TRANSFER_COST &&
                targetDimension != null &&
                !targetPos.equals(BlockPos.ZERO) &&
                hasNewItemsToTransfer();

        if (!canTransfer && level.getGameTime() % 100 == 0) {
            System.out.println("[" + level.dimension().location() + "] Cannot transfer - " +
                    "Mana: " + getMana() + "/" + TRANSFER_COST +
                    ", Target: " + (targetDimension != null ? targetDimension.location() : "null") +
                    ", HasNewItems: " + hasNewItemsToTransfer());
        }

        return canTransfer;
    }

    /**
     * 检查周围是否有新的物品可以传送（改进的检测逻辑）
     */
    private boolean hasNewItemsToTransfer() {
        // 检查掉落的物品 - 添加年龄检测和来源检测
        List<ItemEntity> items = getValidItemsForTransfer();
        if (!items.isEmpty()) return true;

        // 检查容器中的物品
        List<IItemHandler> containers = getContainersInRange();
        for (IItemHandler container : containers) {
            for (int i = 0; i < container.getSlots(); i++) {
                if (!container.getStackInSlot(i).isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 获取有效的可传送物品实体（新的智能检测）
     */
    private List<ItemEntity> getValidItemsForTransfer() {
        AABB scanBox = new AABB(getBlockPos()).inflate(SCAN_RANGE);
        List<ItemEntity> allItems = level.getEntitiesOfClass(ItemEntity.class, scanBox);
        List<ItemEntity> validItems = new ArrayList<>();

        for (ItemEntity item : allItems) {
            if (isItemValidForTransfer(item)) {
                validItems.add(item);
            }
        }

        return validItems;
    }

    /**
     * 改进的物品有效性检测
     */
    private boolean isItemValidForTransfer(ItemEntity item) {
        // 1. 检查物品年龄 - 太新的物品跳过（防止传送刚刚生成的物品）
        if (item.getAge() < ITEM_DETECTION_DELAY) {
            return false;
        }

        // 2. 检查物品是否来自跨维度传送
        if (isItemFromInterdimensionalTransfer(item)) {
            return false;
        }

        // 3. 检查是否已被处理
        if (isItemProcessedRecently(item)) {
            return false;
        }

        return true;
    }

    /**
     * 检查物品是否来自跨维度传送
     */
    private boolean isItemFromInterdimensionalTransfer(ItemEntity item) {
        CompoundTag persistentData = item.getPersistentData();
        if (!persistentData.contains(ITEM_SOURCE_TAG)) {
            return false;
        }

        CompoundTag sourceTag = persistentData.getCompound(ITEM_SOURCE_TAG);
        long transferTime = sourceTag.getLong("transferTime");
        long currentTime = level.getGameTime();

        // 如果物品是在最近传送过来的（5秒内），则不再传送
        return (currentTime - transferTime) < 100; // 5秒 = 100 ticks
    }

    /**
     * 检查物品是否最近被处理过
     */
    private boolean isItemProcessedRecently(ItemEntity item) {
        CompoundTag tag = item.getPersistentData();
        if (!tag.contains(ITEM_PROCESSED_TAG)) {
            return false;
        }

        CompoundTag processedTag = tag.getCompound(ITEM_PROCESSED_TAG);
        String processorId = processedTag.getString("processorId");
        long processTime = processedTag.getLong("processTime");

        // 如果不是这个花朵处理的，则未处理
        if (!flowerProcessorId.toString().equals(processorId)) {
            return false;
        }

        long currentTime = level.getGameTime();
        long timeSinceProcessed = currentTime - processTime;
        int itemAge = item.getAge();

        // 如果物品年龄小于处理后经过的时间，说明物品被重新丢出
        if (itemAge < timeSinceProcessed - 10) {
            // 清除旧标记，允许重新处理
            tag.remove(ITEM_PROCESSED_TAG);
            return false;
        }

        return true;
    }

    /**
     * 标记物品为已处理
     */
    private void markItemAsProcessed(ItemEntity item) {
        CompoundTag tag = item.getPersistentData();
        CompoundTag processedTag = new CompoundTag();
        processedTag.putString("processorId", flowerProcessorId.toString());
        processedTag.putLong("processTime", level.getGameTime());
        processedTag.putInt("itemAgeWhenProcessed", item.getAge());
        tag.put(ITEM_PROCESSED_TAG, processedTag);
    }

    /**
     * 标记物品为跨维度传送物品
     */
    private void markItemAsTransferred(ItemEntity item, ResourceKey<Level> sourceDimension) {
        CompoundTag persistentData = item.getPersistentData();
        CompoundTag sourceTag = new CompoundTag();
        sourceTag.putString("sourceDimension", sourceDimension.location().toString());
        sourceTag.putLong("transferTime", level.getGameTime());
        persistentData.put(ITEM_SOURCE_TAG, sourceTag);
    }

    private void attemptTransfer() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        MinecraftServer server = serverLevel.getServer();
        ServerLevel targetLevel = server.getLevel(targetDimension);

        if (targetLevel == null) {
            System.out.println("[" + level.dimension().location() + "] Target dimension not loaded: " + targetDimension.location());
            return;
        }

        InterdimensionalFlowerRegistry registry = InterdimensionalFlowerRegistry.getInstance(server);
        if (registry.getFlowersInDimension(targetDimension).isEmpty()) {
            System.out.println("[" + level.dimension().location() + "] No flowers in target dimension: " + targetDimension.location());
            findTargetFlower();
            return;
        }

        List<ItemStack> itemsToTransfer = collectValidItemsForTransfer();

        if (itemsToTransfer.isEmpty()) {
            System.out.println("[" + level.dimension().location() + "] No valid items to transfer");
            return;
        }

        // 执行传送
        addMana(-TRANSFER_COST);

        for (ItemStack stack : itemsToTransfer) {
            ItemEntity itemEntity = new ItemEntity(targetLevel,
                    targetPos.getX() + 0.5,
                    targetPos.getY() + 1.0,
                    targetPos.getZ() + 0.5,
                    stack.copy());

            itemEntity.setDeltaMovement(0, 0, 0);

            // 关键修复：标记传送过来的物品
            markItemAsTransferred(itemEntity, level.dimension());

            targetLevel.addFreshEntity(itemEntity);
        }

        cooldown = COOLDOWN_TIME;
        setChanged();

        playTransferEffects();

        System.out.println("[SUCCESS] Transferred " + itemsToTransfer.size() + " item stacks from " +
                level.dimension().location() + " to " + targetDimension.location());
    }

    /**
     * 收集有效的传送物品
     */
    private List<ItemStack> collectValidItemsForTransfer() {
        List<ItemStack> items = new ArrayList<>();

        // 处理有效的物品实体
        List<ItemEntity> validItemEntities = getValidItemsForTransfer();
        for (ItemEntity entity : validItemEntities) {
            items.add(entity.getItem().copy());
            markItemAsProcessed(entity);
            entity.discard();
        }

        // 处理容器中的物品（容器物品不会形成循环，因为它们不会被重新放回容器）
        List<IItemHandler> containers = getContainersInRange();
        for (IItemHandler container : containers) {
            for (int i = 0; i < container.getSlots(); i++) {
                ItemStack stack = container.extractItem(i, Integer.MAX_VALUE, false);
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
        }

        return items;
    }

    private List<IItemHandler> getContainersInRange() {
        List<IItemHandler> containers = new ArrayList<>();

        for (int x = -SCAN_RANGE; x <= SCAN_RANGE; x++) {
            for (int y = -SCAN_RANGE; y <= SCAN_RANGE; y++) {
                for (int z = -SCAN_RANGE; z <= SCAN_RANGE; z++) {
                    BlockPos pos = getBlockPos().offset(x, y, z);
                    var blockEntity = level.getBlockEntity(pos);

                    if (blockEntity != null) {
                        var capability = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                        capability.ifPresent(containers::add);
                    }
                }
            }
        }

        return containers;
    }

    private void playTransferEffects() {
        if (level.isClientSide) return;

        for (int i = 0; i < 20; i++) {
            double x = getBlockPos().getX() + 0.5 + (Math.random() - 0.5) * 2;
            double y = getBlockPos().getY() + 0.5 + (Math.random() - 0.5) * 2;
            double z = getBlockPos().getZ() + 0.5 + (Math.random() - 0.5) * 2;

            vazkii.botania.api.BotaniaAPI.instance().sparkleFX(level, x, y, z,
                    0.8f, 0.4f, 1.0f, 1.0f, 10);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (level instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            InterdimensionalFlowerRegistry registry = InterdimensionalFlowerRegistry.getInstance(server);
            registry.unregisterFlower(level.dimension(), getBlockPos());
            System.out.println("[" + level.dimension().location() + "] Flower removed and unregistered at " + getBlockPos());
        }
    }

    @Override
    public @Nullable RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getBlockPos(), SCAN_RANGE);
    }

    @Override
    public int getMaxMana() {
        return 16000;
    }

    @Override
    public int getColor() {
        return 0xFFFFFFE0;
    }

    public int getCooldown() {
        return cooldown;
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        cooldown = cmp.getInt(TAG_COOLDOWN);

        if (cmp.contains(TAG_TARGET_DIM)) {
            String dimString = cmp.getString(TAG_TARGET_DIM);
            if (!dimString.isEmpty()) {
                ResourceLocation dimLocation = new ResourceLocation(dimString);
                targetDimension = ResourceKey.create(net.minecraft.core.Registry.DIMENSION_REGISTRY, dimLocation);
            }
        }

        if (cmp.contains(TAG_TARGET_POS)) {
            CompoundTag posTag = cmp.getCompound(TAG_TARGET_POS);
            targetPos = new BlockPos(
                    posTag.getInt("x"),
                    posTag.getInt("y"),
                    posTag.getInt("z")
            );
        }

        if (cmp.contains(TAG_FLOWER_ID)) {
            String idString = cmp.getString(TAG_FLOWER_ID);
            try {
                flowerProcessorId = UUID.fromString(idString);
            } catch (IllegalArgumentException e) {
                flowerProcessorId = UUID.randomUUID();
            }
        } else {
            flowerProcessorId = UUID.randomUUID();
        }
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt(TAG_COOLDOWN, cooldown);

        if (targetDimension != null) {
            cmp.putString(TAG_TARGET_DIM, targetDimension.location().toString());
        }

        CompoundTag posTag = new CompoundTag();
        posTag.putInt("x", targetPos.getX());
        posTag.putInt("y", targetPos.getY());
        posTag.putInt("z", targetPos.getZ());
        cmp.put(TAG_TARGET_POS, posTag);

        if (flowerProcessorId != null) {
            cmp.putString(TAG_FLOWER_ID, flowerProcessorId.toString());
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap,
                LazyOptional.of(() -> new FunctionalWandHud(this)).cast());
    }
}