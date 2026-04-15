package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.WandBindable;
import vazkii.botania.api.block.WandHUD;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.api.internal.ManaNetwork;
import vazkii.botania.api.mana.ManaCollector;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.helper.MathHelper;
import vazkii.botania.common.item.WandOfTheForestItem;

import javax.annotation.Nullable;
import java.util.Objects;


public abstract class TileEntityHybridFlower extends SpecialFlowerBlockEntity implements WandBindable {
    private static final ResourceLocation POOL_ID = new ResourceLocation(BotaniaAPI.MODID, "mana_pool");
    private static final ResourceLocation SPREADER_ID = new ResourceLocation(BotaniaAPI.MODID, "mana_spreader");
    public static final int LINK_RANGE = 10;
    private static final String TAG_MANA = "mana";
    private static final String TAG_POOL_BINDING = "poolBinding";
    private static final String TAG_COLLECTOR_BINDING = "collectorBinding";
    private static final String TAG_MODE = "workMode";
    private static final String TAG_REDSTONE_SIGNAL = "redstoneSignal";

    public enum WorkMode {
        FUNCTIONAL,
        GENERATING
    }

    private int mana;
    public int redstoneSignal = 0;
    private WorkMode workMode = WorkMode.FUNCTIONAL;

    protected @Nullable BlockPos poolBindingPos = null;
    protected @Nullable BlockPos collectorBindingPos = null;

    public TileEntityHybridFlower(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void tickFlower() {
        super.tickFlower();

        if (ticksExisted == 1 && !level.isClientSide) {
            if (poolBindingPos == null) {
                setPoolBinding(findClosestPool());
            }
            if (collectorBindingPos == null) {
                setCollectorBinding(findClosestCollector());
            }
        }

        updateRedstoneSignal();

        if (workMode == WorkMode.FUNCTIONAL) {
            tickFunctionalMode();
        } else {
            tickGeneratingMode();
        }

        if (level.isClientSide) {
            spawnParticles();
        }
    }

    private void tickFunctionalMode() {
        if (canWork()) {
            doFunctionalWork();
        }
        drawManaFromPool();
        if (canGenerate()) {
            int generatedMana = doGeneratingWork();
            if (generatedMana > 0) addMana(generatedMana);
        }
    }

    private void tickGeneratingMode() {
        if (canGenerate()) {
            int generatedMana = doGeneratingWork();
            if (generatedMana > 0) addMana(generatedMana);
        }
        emptyManaIntoCollector();
        if (canWork() && getMana() > 0) {
            doFunctionalWork();
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

    public void drawManaFromPool() {
        ManaPool pool = findBoundPool();
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

    public void emptyManaIntoCollector() {
        ManaCollector collector = findBoundCollector();
        if (collector != null && !collector.isFull() && getMana() > 0) {
            int manaval = Math.min(getMana(), collector.getMaxMana() - collector.getCurrentMana());
            addMana(-manaval);
            collector.receiveMana(manaval);
            sync();
        }
    }

    @Nullable
    public BlockPos findClosestPool() {
        ManaNetwork network = BotaniaAPI.instance().getManaNetworkInstance();
        var closestPool = network.getClosestPool(getBlockPos(), getLevel(), LINK_RANGE);
        if (closestPool instanceof BlockEntity be) return be.getBlockPos();
        return null;
    }

    @Nullable
    public BlockPos findClosestCollector() {
        ManaNetwork network = BotaniaAPI.instance().getManaNetworkInstance();
        var closestCollector = network.getClosestCollector(getBlockPos(), getLevel(), LINK_RANGE);
        if (closestCollector instanceof BlockEntity be) return be.getBlockPos();
        return null;
    }

    @Nullable
    public ManaPool findBoundPool() {
        if (level == null || poolBindingPos == null || !level.isLoaded(poolBindingPos)) return null;
        BlockEntity be = level.getBlockEntity(poolBindingPos);
        return (be instanceof ManaPool pool) ? pool : null;
    }

    @Nullable
    public ManaCollector findBoundCollector() {
        if (level == null || collectorBindingPos == null || !level.isLoaded(collectorBindingPos)) return null;
        BlockEntity be = level.getBlockEntity(collectorBindingPos);
        return (be instanceof ManaCollector collector) ? collector : null;
    }

    public int getMana() {
        return mana;
    }

    public void addMana(int mana) {
        this.mana = Mth.clamp(this.mana + mana, 0, getMaxMana());
        setChanged();
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public void setWorkMode(WorkMode mode) {
        if (this.workMode != mode) {
            this.workMode = mode;
            setChanged();
            sync();
        }
    }

    public void toggleWorkMode() {
        setWorkMode(workMode == WorkMode.FUNCTIONAL ? WorkMode.GENERATING : WorkMode.FUNCTIONAL);
    }

    public void setPoolBinding(@Nullable BlockPos pos) {
        if (!Objects.equals(this.poolBindingPos, pos)) {
            this.poolBindingPos = pos;
            setChanged();
            sync();
        }
    }

    public void setCollectorBinding(@Nullable BlockPos pos) {
        if (!Objects.equals(this.collectorBindingPos, pos)) {
            this.collectorBindingPos = pos;
            setChanged();
            sync();
        }
    }

    @Nullable
    public BlockPos getPoolBinding() {
        return isValidPoolBinding() ? poolBindingPos : null;
    }

    @Nullable
    public BlockPos getCollectorBinding() {
        return isValidCollectorBinding() ? collectorBindingPos : null;
    }

    public boolean isValidPoolBinding() {
        return isValidBinding(poolBindingPos, ManaPool.class);
    }

    public boolean isValidCollectorBinding() {
        return isValidBinding(collectorBindingPos, ManaCollector.class);
    }

    private boolean isValidBinding(@Nullable BlockPos pos, Class<?> targetClass) {
        if (level == null || pos == null || !level.isLoaded(pos)) return false;
        if (MathHelper.distSqr(getBlockPos(), pos) > (long) LINK_RANGE * LINK_RANGE) return false;
        BlockEntity be = level.getBlockEntity(pos);
        return be != null && targetClass.isAssignableFrom(be.getClass());
    }

    @Override
    public boolean canSelect(Player player, ItemStack wand, BlockPos pos, Direction side) {
        return WandOfTheForestItem.getBindMode(wand);
    }

    @Override
    public boolean bindTo(Player player, ItemStack wand, BlockPos pos, Direction side) {
        if (level == null) return false;

        BlockEntity be = level.getBlockEntity(pos);
        boolean success = false;

        if (be instanceof ManaPool && isValidBinding(pos, ManaPool.class)) {
            setPoolBinding(pos);
            if (player != null) {
                player.sendSystemMessage(Component.literal("Bound to Mana Pool at " + pos.toShortString()));
            }
            success = true;
        } else if (be instanceof ManaCollector && isValidBinding(pos, ManaCollector.class)) {
            setCollectorBinding(pos);
            if (player != null) {
                player.sendSystemMessage(Component.literal("Bound to Mana Collector at " + pos.toShortString()));
            }
            success = true;
        }

        if (success) {
            level.playSound(null, getBlockPos(), BotaniaSounds.ding, SoundSource.BLOCKS, 1F, 1F);
        }

        return success;
    }

    @Override
    @Nullable
    public BlockPos getBinding() {
        return workMode == WorkMode.FUNCTIONAL ? getPoolBinding() : getCollectorBinding();
    }

    public InteractionResult onRightClick(Player player, InteractionHand hand) {
        if (level != null && (level.isClientSide || hand != InteractionHand.MAIN_HAND)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        if (isWandOfTheForest(heldItem)) {
            if (!WandOfTheForestItem.getBindMode(heldItem)) {
                toggleWorkMode();
                String modeName = workMode == WorkMode.FUNCTIONAL ? "Functional" : "Generating";
                player.sendSystemMessage(Component.literal("Mode switched to: " + modeName));
                level.playSound(null, getBlockPos(), SoundEvents.NOTE_BLOCK_CHIME.value(),
                        SoundSource.BLOCKS, 0.5F, workMode == WorkMode.FUNCTIONAL ? 1.0F : 1.5F);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }

    private boolean isWandOfTheForest(ItemStack stack) {
        return stack.getItem() instanceof WandOfTheForestItem;
    }

    private void spawnParticles() {
        double particleChance = 1F - (double) mana / (double) getMaxMana() / 3.5F;
        int color = getColor();
        if (workMode == WorkMode.GENERATING) color = adjustColorForGenerating(color);

        float red = (color >> 16 & 0xFF) / 255F;
        float green = (color >> 8 & 0xFF) / 255F;
        float blue = (color & 0xFF) / 255F;

        if (Math.random() > particleChance) {
            BlockPos effectivePos = getEffectivePos();
            double x = effectivePos.getX();
            double y = effectivePos.getY();
            double z = effectivePos.getZ();
            BotaniaAPI.instance().sparkleFX(level,
                    x + 0.3 + Math.random() * 0.5,
                    y + 0.5 + Math.random() * 0.5,
                    z + 0.3 + Math.random() * 0.5,
                    red, green, blue, (float) Math.random(), 5);
        }
    }

    private int adjustColorForGenerating(int originalColor) {
        int red = Math.min(255, (originalColor >> 16 & 0xFF) + 30);
        int green = Math.min(255, (originalColor >> 8 & 0xFF) + 20);
        int blue = originalColor & 0xFF;
        return (red << 16) | (green << 8) | blue;
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt(TAG_MANA, mana);
        cmp.putInt(TAG_MODE, workMode.ordinal());
        cmp.putInt(TAG_REDSTONE_SIGNAL, redstoneSignal);
        if (poolBindingPos != null) cmp.put(TAG_POOL_BINDING, NbtUtils.writeBlockPos(poolBindingPos));
        if (collectorBindingPos != null) cmp.put(TAG_COLLECTOR_BINDING, NbtUtils.writeBlockPos(collectorBindingPos));
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        mana = cmp.getInt(TAG_MANA);
        if (cmp.contains(TAG_MODE)) workMode = WorkMode.values()[cmp.getInt(TAG_MODE)];
        redstoneSignal = cmp.getInt(TAG_REDSTONE_SIGNAL);
        if (cmp.contains(TAG_POOL_BINDING)) poolBindingPos = NbtUtils.readBlockPos(cmp.getCompound(TAG_POOL_BINDING));
        if (cmp.contains(TAG_COLLECTOR_BINDING)) collectorBindingPos = NbtUtils.readBlockPos(cmp.getCompound(TAG_COLLECTOR_BINDING));
    }

    public ItemStack getHudIcon() {
        if (workMode == WorkMode.FUNCTIONAL) {
            return BuiltInRegistries.ITEM.getOptional(POOL_ID).map(ItemStack::new).orElse(ItemStack.EMPTY);
        } else {
            return BuiltInRegistries.ITEM.getOptional(SPREADER_ID).map(ItemStack::new).orElse(ItemStack.EMPTY);
        }
    }

    public abstract int getMaxMana();
    public abstract int getColor();
    public boolean acceptsRedstone() { return false; }
    public abstract boolean canWork();
    public abstract void doFunctionalWork();
    public abstract boolean canGenerate();
    public abstract int doGeneratingWork();

    public static class HybridWandHud<T extends TileEntityHybridFlower> implements WandHUD {
        protected final T flower;

        public HybridWandHud(T flower) {
            this.flower = flower;
        }

        @Override
        public void renderHUD(GuiGraphics ms, Minecraft mc) {
            String name = I18n.get(flower.getBlockState().getBlock().getDescriptionId());
            String mode = flower.getWorkMode() == WorkMode.FUNCTIONAL ? "Functional" : "Generating";
            BotaniaAPIClient.instance().drawComplexManaHUD(ms, flower.getColor(), flower.getMana(), flower.getMaxMana(),
                    name + " (" + mode + ")", flower.getHudIcon(),
                    flower.isValidPoolBinding() || flower.isValidCollectorBinding());
        }
    }
}