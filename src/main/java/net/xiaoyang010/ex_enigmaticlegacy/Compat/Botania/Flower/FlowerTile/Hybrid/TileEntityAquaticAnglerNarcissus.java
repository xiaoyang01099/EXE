package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Hybrid;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.TileEntityHybridFlower;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 水生垂钓莲花 - 混合花朵实现
 * 功能模式：自动钓鱼并存储到附近的箱子
 * 产能模式：在水边产生魔力
 */
public class TileEntityAquaticAnglerNarcissus extends TileEntityHybridFlower {

    private static final int MAX_MANA = 1000000;        // 最大魔力容量
    private static final int MANA_GENERATION_RATE = 100000; // 每tick产生的魔力
    private static final int MANA_CONSUMPTION_PER_SECOND = 5000; // 每秒消耗的魔力
    private static final int FISHING_RANGE = 3;         // 钓鱼和箱子搜索范围
    private static final int WATER_CHECK_RANGE = 5;     // 水源检测范围
    private static final int FISHING_COOLDOWN = 20;    // 钓鱼冷却时间（5秒）

    private static final int FUNCTIONAL_FLOWER_COLOR = 0x4A90E2;
    private static final int GENERATING_FLOWER_COLOR = 0xFF0000;

    // NBT标签
    private static final String TAG_FISHING_COOLDOWN = "fishingCooldown";
    private static final String TAG_LAST_MANA_CONSUME_TIME = "lastManaConsumeTime";
    private static final String TAG_IS_NEAR_WATER = "isNearWater";

    // 状态变量
    private int fishingCooldown = 0;
    private long lastManaConsumeTime = 0;
    private boolean isNearWater = false;

    // 物品处理器
    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sync();
        }
    };

    public TileEntityAquaticAnglerNarcissus(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        if (getWorkMode() == WorkMode.GENERATING) {
            return GENERATING_FLOWER_COLOR;
        } else {
            return FUNCTIONAL_FLOWER_COLOR;
        }
    }

    @Override
    public boolean acceptsRedstone() {
        return true;
    }

    @Override
    protected void tickFlower() {
        super.tickFlower();

        if (ticksExisted % 20 == 0) {
            isNearWater = checkNearWater();
        }

        if (fishingCooldown > 0) {
            fishingCooldown--;
        }
    }

    @Override
    public boolean canWork() {
        return getWorkMode() == WorkMode.FUNCTIONAL &&
                redstoneSignal == 0 &&
                fishingCooldown <= 0 &&
                isNearWater &&
                (getMana() >= MANA_CONSUMPTION_PER_SECOND || canDrawFromPool());
    }

    @Override
    public void doFunctionalWork() {
        if (level == null || level.isClientSide || getWorkMode() != WorkMode.FUNCTIONAL) return;

        if (!consumeManaForFishing()) {
            return;
        }

        if (performFishing()) {
            fishingCooldown = FISHING_COOLDOWN;

            level.playSound(null, getBlockPos(), SoundEvents.FISHING_BOBBER_SPLASH,
                    SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
        }
    }

    @Override
    public boolean canGenerate() {
        return getWorkMode() == WorkMode.GENERATING && isNearWater;
    }

    @Override
    public int doGeneratingWork() {
        if (level == null || level.isClientSide || getWorkMode() != WorkMode.GENERATING || !isNearWater) return 0;

        return MANA_GENERATION_RATE;
    }

    /**
     * 检查是否靠近水源
     */
    private boolean checkNearWater() {
        if (level == null) return false;

        BlockPos center = getBlockPos();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-WATER_CHECK_RANGE, -2, -WATER_CHECK_RANGE),
                center.offset(WATER_CHECK_RANGE, 2, WATER_CHECK_RANGE))) {

            BlockState state = level.getBlockState(pos);
            if (state.getFluidState().getType() == Fluids.WATER &&
                    state.getFluidState().isSource()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查是否能从魔力池获取魔力
     */
    private boolean canDrawFromPool() {
        var pool = findBoundPool();
        return pool != null && pool.getCurrentMana() >= MANA_CONSUMPTION_PER_SECOND;
    }

    /**
     * 为钓鱼消耗魔力
     */
    private boolean consumeManaForFishing() {
        long currentTime = level.getGameTime();

        if (currentTime - lastManaConsumeTime >= 20) {
            if (getMana() >= MANA_CONSUMPTION_PER_SECOND) {
                addMana(-MANA_CONSUMPTION_PER_SECOND);
                lastManaConsumeTime = currentTime;
                return true;
            } else {
                var pool = findBoundPool();
                if (pool != null && pool.getCurrentMana() >= MANA_CONSUMPTION_PER_SECOND) {
                    pool.receiveMana(-MANA_CONSUMPTION_PER_SECOND);
                    lastManaConsumeTime = currentTime;
                    return true;
                } else {
                    return false;
                }
            }
        }

        return getMana() >= MANA_CONSUMPTION_PER_SECOND || canDrawFromPool();
    }

    /**
     * 执行钓鱼操作
     */
    private boolean performFishing() {
        if (level == null) return false;

        List<ItemStack> loot = getFishingLoot();
        if (loot.isEmpty()) return false;

        List<ItemStack> remainingItems = new ArrayList<>();

        for (ItemStack item : loot) {
            ItemStack remaining = tryStoreInNearbyContainers(item);
            if (!remaining.isEmpty()) {
                remainingItems.add(remaining);
            }
        }

        for (ItemStack remaining : remainingItems) {
            dropItemNearby(remaining);
        }

        spawnFishingParticles();

        return true;
    }

    /**
     * 获取钓鱼战利品
     */
    private List<ItemStack> getFishingLoot() {
        if (level == null) return new ArrayList<>();

        LootContext.Builder contextBuilder = new LootContext.Builder((ServerLevel) level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(getBlockPos()))
                .withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD))
                .withRandom(level.random);

        LootContext context = contextBuilder.create(LootContextParamSets.FISHING);

        LootTable lootTable = Objects.requireNonNull(level.getServer()).getLootTables().get(BuiltInLootTables.FISHING);

        return lootTable.getRandomItems(context);
    }

    /**
     * 尝试将物品存储到附近的容器中
     */
    private ItemStack tryStoreInNearbyContainers(ItemStack item) {
        if (level == null || item.isEmpty()) return item;

        BlockPos center = getBlockPos();
        ItemStack remaining = item.copy();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-FISHING_RANGE, -FISHING_RANGE, -FISHING_RANGE),
                center.offset(FISHING_RANGE, FISHING_RANGE, FISHING_RANGE))) {

            if (pos.equals(center)) continue;

            if (level.getBlockEntity(pos) instanceof Container container) {
                IItemHandler handler = new InvWrapper(container);
                ItemStack insertResult = ItemHandlerHelper.insertItemStacked(handler, remaining, false);

                if (insertResult.isEmpty()) {
                    return ItemStack.EMPTY;
                } else if (insertResult.getCount() < remaining.getCount()) {
                    remaining = insertResult;
                }
            }
        }

        return remaining;
    }

    /**
     * 在附近掉落物品
     */
    private void dropItemNearby(ItemStack item) {
        if (level == null || item.isEmpty()) return;

        Random random = level.random;
        double x = getBlockPos().getX() + 0.5 + (random.nextDouble() - 0.5) * 3.0;
        double y = getBlockPos().getY() + 1.0;
        double z = getBlockPos().getZ() + 0.5 + (random.nextDouble() - 0.5) * 3.0;

        ItemEntity itemEntity =
                new ItemEntity(level, x, y, z, item);

        double motionX = (random.nextDouble() - 0.5) * 0.1;
        double motionZ = (random.nextDouble() - 0.5) * 0.1;
        itemEntity.setDeltaMovement(motionX, 0.0, motionZ);

        level.addFreshEntity(itemEntity);
    }

    /**
     * 生成钓鱼粒子效果
     */
    private void spawnFishingParticles() {
        if (level == null || !level.isClientSide) return;

        BlockPos pos = getBlockPos();
        Random random = level.random;

        for (int i = 0; i < 8; i++) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double y = pos.getY() + 0.5 + random.nextDouble() * 0.5;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0;

            BotaniaAPI.instance().sparkleFX(level, x, y, z,
                    0.3F, 0.6F, 1.0F, 0.8F, 15);
        }

        for (int i = 0; i < 3; i++) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.0;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.0;

            BotaniaAPI.instance().sparkleFX(level, x, y, z,
                    1.0F, 1.0F, 1.0F, 0.6F, 8);
        }
    }

    /**
     * 获取工作范围描述
     */
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getBlockPos(), FISHING_RANGE);
    }

    /**
     * 是否靠近水源
     */
    public boolean isNearWater() {
        return isNearWater;
    }

    /**
     * 获取钓鱼冷却剩余时间
     */
    public int getFishingCooldown() {
        return fishingCooldown;
    }

    /**
     * 获取钓鱼冷却进度（0-1）
     */
    public float getFishingProgress() {
        return 1.0F - (float) fishingCooldown / FISHING_COOLDOWN;
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);

        cmp.putInt(TAG_FISHING_COOLDOWN, fishingCooldown);
        cmp.putLong(TAG_LAST_MANA_CONSUME_TIME, lastManaConsumeTime);
        cmp.putBoolean(TAG_IS_NEAR_WATER, isNearWater);
        cmp.put("inventory", inventory.serializeNBT());
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);

        fishingCooldown = cmp.getInt(TAG_FISHING_COOLDOWN);
        lastManaConsumeTime = cmp.getLong(TAG_LAST_MANA_CONSUME_TIME);
        isNearWater = cmp.getBoolean(TAG_IS_NEAR_WATER);

        if (cmp.contains("inventory")) {
            inventory.deserializeNBT(cmp.getCompound("inventory"));
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap,
                LazyOptional.of(() -> new AquaticAnglerWandHud(this)).cast());
    }

    public static class AquaticAnglerWandHud extends HybridWandHud<TileEntityAquaticAnglerNarcissus> {

        public AquaticAnglerWandHud(TileEntityAquaticAnglerNarcissus flower) {
            super(flower);
        }

        @Override
        public void renderHUD(PoseStack ms, Minecraft mc) {
            super.renderHUD(ms, mc);

            int x = 10;
            int y = 40;

            String waterStatus = flower.isNearWater() ? "§aNear Water: Yes" : "§cNear Water: No";
            mc.font.draw(ms, waterStatus, x, y, 0xFFFFFF);
            y += 12;

            if (flower.getFishingCooldown() > 0) {
                String fishingInfo = String.format("§bFishing: %.1f%%", flower.getFishingProgress() * 100);
                mc.font.draw(ms, fishingInfo, x, y, 0xFFFFFF);
                y += 12;

                String cooldownInfo = String.format("§7Cooldown: %ds", flower.getFishingCooldown() / 20);
                mc.font.draw(ms, cooldownInfo, x, y, 0xFFFFFF);
                y += 12;
            }

            if (flower.getWorkMode() == WorkMode.FUNCTIONAL) {
                String consumptionInfo = "§eMana Cost: 1000/s";
                mc.font.draw(ms, consumptionInfo, x, y, 0xFFFFFF);
                y += 12;

                String rangeInfo = String.format("§3Fishing Range: %dx%d", FISHING_RANGE * 2 + 1, FISHING_RANGE * 2 + 1);
                mc.font.draw(ms, rangeInfo, x, y, 0xFFFFFF);
                y += 12;
            } else {
                String generationInfo = "§aMana Gen: +500/t";
                mc.font.draw(ms, generationInfo, x, y, 0xFFFFFF);
                y += 12;
            }

            String waterRangeInfo = String.format("§9Water Range: %dx%d", WATER_CHECK_RANGE * 2 + 1, WATER_CHECK_RANGE * 2 + 1);
            mc.font.draw(ms, waterRangeInfo, x, y, 0xFFFFFF);
        }
    }
}