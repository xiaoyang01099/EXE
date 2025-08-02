package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.IWandBindable;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.internal.IManaNetwork;
import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.subtile.TileEntitySpecialFlower;

import javax.annotation.Nullable;

/**
 * 混合花的基本类，既可以产生魔力，也可以消耗魔力。
 * 这朵花可以绑定到魔力池（用于魔力消耗）和魔力收集器（用于魔力输出）。
 */

public abstract class TileEntityHybridFlower extends TileEntitySpecialFlower implements IWandBindable {
    private static final ResourceLocation SPREADER_ID = new ResourceLocation(BotaniaAPI.MODID, "mana_spreader");
    private static final ResourceLocation POOL_ID = new ResourceLocation(BotaniaAPI.MODID, "mana_pool");
    public static final int LINK_RANGE = 10;
    private static final String TAG_MANA = "mana";
    private static final String TAG_BOUND_POOL_X = "boundPoolX";
    private static final String TAG_BOUND_POOL_Y = "boundPoolY";
    private static final String TAG_BOUND_POOL_Z = "boundPoolZ";
    private static final String TAG_BOUND_COLLECTOR_X = "boundCollectorX";
    private static final String TAG_BOUND_COLLECTOR_Y = "boundCollectorY";
    private static final String TAG_BOUND_COLLECTOR_Z = "boundCollectorZ";
    private int mana;
    public int redstoneSignal = 0;
    private BlockPos boundPool = null;
    private BlockPos boundCollector = null;

    public TileEntityHybridFlower(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean acceptsRedstone() {
        return false;
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        redstoneSignal = 0;
        if (acceptsRedstone()) {
            for (Direction dir : Direction.values()) {
                int redstoneSide = getLevel().getSignal(getBlockPos().relative(dir), dir);
                redstoneSignal = Math.max(redstoneSignal, redstoneSide);
            }
        }

        drawManaFromPool();

        emptyManaIntoCollector();

        if (getLevel() != null && getLevel().isClientSide) {
            double particleChance = 1F - (double) getMana() / (double) getMaxMana() / 3.5F;
            int color = getColor();
            float red = (color >> 16 & 0xFF) / 255F;
            float green = (color >> 8 & 0xFF) / 255F;
            float blue = (color & 0xFF) / 255F;

            if (Math.random() > particleChance) {
                Vec3 offset = getLevel().getBlockState(getBlockPos()).getOffset(getLevel(), getBlockPos());
                double x = getBlockPos().getX() + offset.x;
                double y = getBlockPos().getY() + offset.y;
                double z = getBlockPos().getZ() + offset.z;
                BotaniaAPI.instance().sparkleFX(getLevel(), x + 0.3 + Math.random() * 0.5, y + 0.5 + Math.random() * 0.5, z + 0.3 + Math.random() * 0.5, red, green, blue, (float) Math.random(), 5);
            }
        }
    }

    @Nullable
    public BlockPos findClosestPool() {
        IManaNetwork network = BotaniaAPI.instance().getManaNetworkInstance();
        var closestPool = network.getClosestPool(getBlockPos(), getLevel(), LINK_RANGE);
        return closestPool == null ? null : closestPool.getManaReceiverPos();
    }

    @Nullable
    public IManaPool findBoundPool() {
        if (boundPool == null) {
            return null;
        }

        if (getLevel().getBlockEntity(boundPool) instanceof IManaPool pool) {
            return pool;
        }

        boundPool = null;
        return null;
    }

    public void drawManaFromPool() {
        IManaPool pool = findBoundPool();
        if (pool != null) {
            int manaInPool = pool.getCurrentMana();
            int manaMissing = getMaxMana() - mana;
            int manaToRemove = Math.min(manaMissing, manaInPool);
            if (manaToRemove > 0) {
                pool.receiveMana(-manaToRemove);
                addMana(manaToRemove);
            }
        }
    }

    @Nullable
    public BlockPos findClosestCollector() {
        IManaNetwork network = BotaniaAPI.instance().getManaNetworkInstance();
        var closestCollector = network.getClosestCollector(getBlockPos(), getLevel(), LINK_RANGE);
        return closestCollector == null ? null : closestCollector.getManaReceiverPos();
    }

    @Nullable
    public IManaCollector findBoundCollector() {
        if (boundCollector == null) {
            return null;
        }

        if (getLevel().getBlockEntity(boundCollector) instanceof IManaCollector collector) {
            return collector;
        }

        boundCollector = null;
        return null;
    }

    public void emptyManaIntoCollector() {
        IManaCollector collector = findBoundCollector();
        if (collector != null && !collector.isFull() && getMana() > 0) {
            int manaval = Math.min(getMana(), collector.getMaxMana() - collector.getCurrentMana());
            if (manaval > 0) {
                addMana(-manaval);
                collector.receiveMana(manaval);
                sync();
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

    @Override
    public boolean canSelect(Player player, ItemStack wand, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public boolean bindTo(Player player, ItemStack wand, BlockPos pos, Direction side) {
        if (getLevel().getBlockEntity(pos) instanceof IManaPool) {
            boundPool = pos;
            sync();
            return true;
        } else if (getLevel().getBlockEntity(pos) instanceof IManaCollector) {
            boundCollector = pos;
            sync();
            return true;
        }
        return false;
    }

    @Override
    public BlockPos getBinding() {
        return boundPool;
    }

    public BlockPos getCollectorBinding() {
        return boundCollector;
    }

    public boolean isValidPoolBinding() {
        return findBoundPool() != null;
    }

    public boolean isValidCollectorBinding() {
        return findBoundCollector() != null;
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        mana = cmp.getInt(TAG_MANA);

        if (cmp.contains(TAG_BOUND_POOL_X)) {
            boundPool = new BlockPos(
                    cmp.getInt(TAG_BOUND_POOL_X),
                    cmp.getInt(TAG_BOUND_POOL_Y),
                    cmp.getInt(TAG_BOUND_POOL_Z)
            );
        }

        if (cmp.contains(TAG_BOUND_COLLECTOR_X)) {
            boundCollector = new BlockPos(
                    cmp.getInt(TAG_BOUND_COLLECTOR_X),
                    cmp.getInt(TAG_BOUND_COLLECTOR_Y),
                    cmp.getInt(TAG_BOUND_COLLECTOR_Z)
            );
        }
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt(TAG_MANA, mana);

        if (boundPool != null) {
            cmp.putInt(TAG_BOUND_POOL_X, boundPool.getX());
            cmp.putInt(TAG_BOUND_POOL_Y, boundPool.getY());
            cmp.putInt(TAG_BOUND_POOL_Z, boundPool.getZ());
        }

        if (boundCollector != null) {
            cmp.putInt(TAG_BOUND_COLLECTOR_X, boundCollector.getX());
            cmp.putInt(TAG_BOUND_COLLECTOR_Y, boundCollector.getY());
            cmp.putInt(TAG_BOUND_COLLECTOR_Z, boundCollector.getZ());
        }
    }

    public ItemStack getPoolHudIcon() {
        return Registry.ITEM.getOptional(POOL_ID).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    public ItemStack getCollectorHudIcon() {
        return Registry.ITEM.getOptional(SPREADER_ID).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    public static class HybridWandHud<T extends TileEntityHybridFlower> implements IWandHUD {
        protected final T flower;

        public HybridWandHud(T flower) {
            this.flower = flower;
        }

        @Override
        public void renderHUD(PoseStack ms, Minecraft mc) {
            String name = I18n.get(flower.getBlockState().getBlock().getDescriptionId());
            int color = flower.getColor();

            BotaniaAPIClient.instance().drawComplexManaHUD(ms, color, flower.getMana(), flower.getMaxMana(),
                    name + " (魔力池)", flower.getPoolHudIcon(), flower.isValidPoolBinding());

            ms.pushPose();
            ms.translate(0, 20, 0);
            BotaniaAPIClient.instance().drawComplexManaHUD(ms, color, flower.getMana(), flower.getMaxMana(),
                    name + " (收集器)", flower.getCollectorHudIcon(), flower.isValidCollectorBinding());
            ms.popPose();
        }
    }
}