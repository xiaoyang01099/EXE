package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.block.IWandable;
import vazkii.botania.api.internal.IManaNetwork;
import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.subtile.TileEntitySpecialFlower;
import vazkii.botania.common.helper.MathHelper;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 杂交花基类 - 同时具有产能花和功能花的能力
 * 可以绑定魔力池和魔力发射器，通过法杖右击切换模式
 */
public abstract class TileEntityHybridFlower extends TileEntitySpecialFlower implements IWandable {

    // 常量定义
    private static final ResourceLocation POOL_ID = new ResourceLocation(BotaniaAPI.MODID, "mana_pool");
    private static final ResourceLocation SPREADER_ID = new ResourceLocation(BotaniaAPI.MODID, "mana_spreader");

    public static final int LINK_RANGE_POOL = 10;      // 魔力池绑定范围
    public static final int LINK_RANGE_COLLECTOR = 6;   // 魔力发射器绑定范围

    // 工作模式枚举
    public enum FlowerMode {
        GENERATING,  // 产能模式
        FUNCTIONAL   // 功能模式
    }

    // NBT标签
    private static final String TAG_MANA = "mana";
    private static final String TAG_MODE = "mode";
    private static final String TAG_POOL_BINDING = "poolBinding";
    private static final String TAG_COLLECTOR_BINDING = "collectorBinding";
    private static final String TAG_REDSTONE_SIGNAL = "redstoneSignal";

    private int mana;
    private FlowerMode mode = FlowerMode.GENERATING;  // 默认产能模式
    @Nullable private BlockPos poolBinding = null;      // 魔力池绑定
    @Nullable private BlockPos collectorBinding = null; // 魔力发射器绑定
    public int redstoneSignal = 0;

    public TileEntityHybridFlower(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void tickFlower() {
        super.tickFlower();
        if (ticksExisted == 1 && !level.isClientSide) {
            if (poolBinding == null || !isValidPoolBinding()) {
                setPoolBinding(findClosestPool());
            }
            if (collectorBinding == null || !isValidCollectorBinding()) {
                setCollectorBinding(findClosestCollector());
            }
        }

        updateRedstoneSignal();

        if (mode == FlowerMode.GENERATING) {
            tickGenerating();
        } else {
            tickFunctional();
        }

        spawnParticles();
    }

    private void tickGenerating() {
        emptyManaIntoCollector();
    }

    private void tickFunctional() {
        drawManaFromPool();
    }

    public void emptyManaIntoCollector() {
        IManaCollector collector = findBoundCollector();
        if (collector != null && !collector.isFull() && getMana() > 0) {
            int manaval = Math.min(getMana(), collector.getMaxMana() - collector.getCurrentMana());
            addMana(-manaval);
            collector.receiveMana(manaval);
            sync();
        }
    }

    public void drawManaFromPool() {
        IManaPool pool = findBoundPool();
        if (pool != null) {
            int manaInPool = pool.getCurrentMana();
            int manaMissing = getMaxMana() - mana;
            int manaToRemove = Math.min(manaMissing, manaInPool);
            pool.receiveMana(-manaToRemove);
            addMana(manaToRemove);
        }
    }

    private void updateRedstoneSignal() {
        redstoneSignal = 0;
        if (acceptsRedstone()) {
            for (Direction dir : Direction.values()) {
                int redstoneSide = getLevel().getSignal(getBlockPos().relative(dir), dir);
                redstoneSignal = Math.max(redstoneSignal, redstoneSide);
            }
        }
    }

    private void spawnParticles() {
        if (getLevel() != null && getLevel().isClientSide) {
            double particleChance = 1F - (double) mana / (double) getMaxMana() / 3.5F;
            int color = getColor();
            float red = (color >> 16 & 0xFF) / 255F;
            float green = (color >> 8 & 0xFF) / 255F;
            float blue = (color & 0xFF) / 255F;

            if (Math.random() > particleChance) {
                Vec3 offset = getLevel().getBlockState(getBlockPos()).getOffset(getLevel(), getBlockPos());
                double x = getBlockPos().getX() + offset.x;
                double y = getBlockPos().getY() + offset.y;
                double z = getBlockPos().getZ() + offset.z;
                BotaniaAPI.instance().sparkleFX(getLevel(),
                        x + 0.3 + Math.random() * 0.5,
                        y + 0.5 + Math.random() * 0.5,
                        z + 0.3 + Math.random() * 0.5,
                        red, green, blue, (float) Math.random(), 5);
            }
        }
    }

    public int getMana() {
        return mana;
    }

    public void addMana(int mana) {
        this.mana = Mth.clamp(this.mana + mana, 0, getMaxMana());
        setChanged();
    }

    public abstract int getMaxMana();
    public abstract int getColor();

    // ==================== 模式切换 ====================

    public FlowerMode getMode() {
        return mode;
    }

    public void switchMode() {
        mode = (mode == FlowerMode.GENERATING) ? FlowerMode.FUNCTIONAL : FlowerMode.GENERATING;
        setChanged();
        sync();
    }

    // ==================== 绑定管理 ====================

    // 魔力池绑定
    public @Nullable BlockPos getPoolBinding() {
        return poolBinding;
    }

    public void setPoolBinding(@Nullable BlockPos pos) {
        boolean changed = !Objects.equals(this.poolBinding, pos);
        this.poolBinding = pos;
        if (changed) {
            setChanged();
            sync();
        }
    }

    public @Nullable IManaPool findBoundPool() {
        if (level == null || poolBinding == null) return null;
        BlockEntity be = level.getBlockEntity(poolBinding);
        return (be instanceof IManaPool) ? (IManaPool) be : null;
    }

    public boolean isValidPoolBinding() {
        return wouldBeValidPoolBinding(poolBinding);
    }

    public boolean wouldBeValidPoolBinding(@Nullable BlockPos pos) {
        if (level == null || pos == null || !level.isLoaded(pos) ||
                MathHelper.distSqr(getBlockPos(), pos) > (long) LINK_RANGE_POOL * LINK_RANGE_POOL) {
            return false;
        }
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof IManaPool;
    }

    public @Nullable BlockPos findClosestPool() {
        IManaNetwork network = BotaniaAPI.instance().getManaNetworkInstance();
        var closestPool = network.getClosestPool(getBlockPos(), getLevel(), LINK_RANGE_POOL);
        return closestPool == null ? null : closestPool.getManaReceiverPos();
    }

    // 魔力发射器绑定
    public @Nullable BlockPos getCollectorBinding() {
        return collectorBinding;
    }

    public void setCollectorBinding(@Nullable BlockPos pos) {
        boolean changed = !Objects.equals(this.collectorBinding, pos);
        this.collectorBinding = pos;
        if (changed) {
            setChanged();
            sync();
        }
    }

    public @Nullable IManaCollector findBoundCollector() {
        if (level == null || collectorBinding == null) return null;
        BlockEntity be = level.getBlockEntity(collectorBinding);
        return (be instanceof IManaCollector) ? (IManaCollector) be : null;
    }

    public boolean isValidCollectorBinding() {
        return wouldBeValidCollectorBinding(collectorBinding);
    }

    public boolean wouldBeValidCollectorBinding(@Nullable BlockPos pos) {
        if (level == null || pos == null || !level.isLoaded(pos) ||
                MathHelper.distSqr(getBlockPos(), pos) > (long) LINK_RANGE_COLLECTOR * LINK_RANGE_COLLECTOR) {
            return false;
        }
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof IManaCollector;
    }

    public @Nullable BlockPos findClosestCollector() {
        IManaNetwork network = BotaniaAPI.instance().getManaNetworkInstance();
        var closestCollector = network.getClosestCollector(getBlockPos(), getLevel(), LINK_RANGE_COLLECTOR);
        return closestCollector == null ? null : closestCollector.getManaReceiverPos();
    }

    // ==================== 法杖交互 ====================

    @Override
    public boolean onUsedByWand(@Nullable Player player, ItemStack stack, Direction side) {
        if (player != null && !level.isClientSide) {
            switchMode();

            Component message = new TranslatableComponent(
                    "ex.hybrid_flower.mode_switched",
                    new TranslatableComponent("ex.hybrid_flower.mode." + mode.name().toLowerCase())
            );
            player.sendMessage(message, player.getUUID());

            return true;
        }
        return false;
    }

    /**
     * 是否接受红石信号（仅功能模式下）
     */
    public boolean acceptsRedstone() {
        return mode == FlowerMode.FUNCTIONAL;
    }

    /**
     * 获取HUD图标
     */
    public ItemStack getHudIcon() {
        ResourceLocation id = (mode == FlowerMode.GENERATING) ? SPREADER_ID : POOL_ID;
        return Registry.ITEM.getOptional(id).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        mana = cmp.getInt(TAG_MANA);
        mode = FlowerMode.values()[cmp.getInt(TAG_MODE)];
        redstoneSignal = cmp.getInt(TAG_REDSTONE_SIGNAL);

        if (cmp.contains(TAG_POOL_BINDING)) {
            poolBinding = NbtUtils.readBlockPos(cmp.getCompound(TAG_POOL_BINDING));
        }
        if (cmp.contains(TAG_COLLECTOR_BINDING)) {
            collectorBinding = NbtUtils.readBlockPos(cmp.getCompound(TAG_COLLECTOR_BINDING));
        }
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt(TAG_MANA, mana);
        cmp.putInt(TAG_MODE, mode.ordinal());
        cmp.putInt(TAG_REDSTONE_SIGNAL, redstoneSignal);

        if (poolBinding != null) {
            cmp.put(TAG_POOL_BINDING, NbtUtils.writeBlockPos(poolBinding));
        }
        if (collectorBinding != null) {
            cmp.put(TAG_COLLECTOR_BINDING, NbtUtils.writeBlockPos(collectorBinding));
        }
    }

    public static class HybridWandHud<T extends TileEntityHybridFlower> implements IWandHUD {
        protected final T flower;

        public HybridWandHud(T flower) {
            this.flower = flower;
        }

        @Override
        public void renderHUD(PoseStack ms, Minecraft mc) {
            String name = I18n.get(flower.getBlockState().getBlock().getDescriptionId());
            String modeName = I18n.get("ex.hybrid_flower.mode." + flower.getMode().name().toLowerCase());
            name += " (" + modeName + ")";

            int color = flower.getColor();
            boolean isValidBinding = (flower.getMode() == FlowerMode.GENERATING)
                    ? flower.isValidCollectorBinding()
                    : flower.isValidPoolBinding();

            BotaniaAPIClient.instance().drawComplexManaHUD(ms, color, flower.getMana(), flower.getMaxMana(),
                    name, flower.getHudIcon(), isValidBinding);
        }
    }
}