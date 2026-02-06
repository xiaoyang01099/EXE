package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TextComponent;
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
import net.minecraft.world.phys.Vec3;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.IWandBindable;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.internal.IManaNetwork;
import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.subtile.TileEntitySpecialFlower;
import vazkii.botania.common.helper.MathHelper;
import vazkii.botania.common.item.ItemTwigWand;

import javax.annotation.Nullable;
import java.util.Objects;


public abstract class TileEntityHybridFlower extends TileEntitySpecialFlower implements IWandBindable {
    private static final ResourceLocation POOL_ID = new ResourceLocation(BotaniaAPI.MODID, "mana_pool");
    private static final ResourceLocation SPREADER_ID = new ResourceLocation(BotaniaAPI.MODID, "mana_spreader");
    public static final int LINK_RANGE = 10;
    private static final String TAG_MANA = "mana";
    private static final String TAG_POOL_BINDING = "poolBinding";
    private static final String TAG_COLLECTOR_BINDING = "collectorBinding";
    private static final String TAG_MODE = "workMode";
    private static final String TAG_REDSTONE_SIGNAL = "redstoneSignal";

    public enum WorkMode {
        FUNCTIONAL, // 功能模式：优先消耗魔力工作
        GENERATING  // 产能模式：优先产生魔力
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

    /**
     * 功能模式：优先使用自身魔力，不足时从魔力池补充
     */
    private void tickFunctionalMode() {
        if (canWork()) {
            doFunctionalWork();
        }

        drawManaFromPool();

        if (canGenerate()) {
            int generatedMana = doGeneratingWork();
            if (generatedMana > 0) {
                addMana(generatedMana);
            }
        }

//        emptyManaIntoCollector(); //循环魔力
    }

    /**
     * 产能模式：优先产生魔力并储存/输出
     */
    private void tickGeneratingMode() {
        if (canGenerate()) {
            int generatedMana = doGeneratingWork();
            if (generatedMana > 0) {
                addMana(generatedMana);
            }
        }

        emptyManaIntoCollector();

        if (canWork() && getMana() > 0) {
            doFunctionalWork();
        }
    }

    /**
     * 更新红石信号
     */
    private void updateRedstoneSignal() {
        redstoneSignal = 0;
        if (acceptsRedstone()) {
            for (Direction dir : Direction.values()) {
                int redstoneSide = getLevel().getSignal(getBlockPos().relative(dir), dir);
                redstoneSignal = Math.max(redstoneSignal, redstoneSide);
            }
        }
    }

    /**
     * 从魔力池获取魔力
     */
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

    /**
     * 将魔力输出到收集器
     */
    public void emptyManaIntoCollector() {
        IManaCollector collector = findBoundCollector();
        if (collector != null && !collector.isFull() && getMana() > 0) {
            int manaval = Math.min(getMana(), collector.getMaxMana() - collector.getCurrentMana());
            addMana(-manaval);
            collector.receiveMana(manaval);
            sync();
        }
    }

    /**
     * 寻找最近的魔力池
     */
    @Nullable
    public BlockPos findClosestPool() {
        IManaNetwork network = BotaniaAPI.instance().getManaNetworkInstance();
        var closestPool = network.getClosestPool(getBlockPos(), getLevel(), LINK_RANGE);
        return closestPool == null ? null : closestPool.getManaReceiverPos();
    }

    /**
     * 寻找最近的魔力收集器
     */
    @Nullable
    public BlockPos findClosestCollector() {
        IManaNetwork network = BotaniaAPI.instance().getManaNetworkInstance();
        var closestCollector = network.getClosestCollector(getBlockPos(), getLevel(), LINK_RANGE);
        return closestCollector == null ? null : closestCollector.getManaReceiverPos();
    }

    /**
     * 寻找绑定的魔力池
     */
    @Nullable
    public IManaPool findBoundPool() {
        if (level == null || poolBindingPos == null || !level.isLoaded(poolBindingPos)) {
            return null;
        }

        BlockEntity be = level.getBlockEntity(poolBindingPos);
        return (be instanceof IManaPool) ? (IManaPool) be : null;
    }

    /**
     * 寻找绑定的魔力收集器
     */
    @Nullable
    public IManaCollector findBoundCollector() {
        if (level == null || collectorBindingPos == null || !level.isLoaded(collectorBindingPos)) {
            return null;
        }

        BlockEntity be = level.getBlockEntity(collectorBindingPos);
        return (be instanceof IManaCollector) ? (IManaCollector) be : null;
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
        return isValidBinding(poolBindingPos, IManaPool.class);
    }

    public boolean isValidCollectorBinding() {
        return isValidBinding(collectorBindingPos, IManaCollector.class);
    }

    private boolean isValidBinding(@Nullable BlockPos pos, Class<?> targetClass) {
        if (level == null || pos == null || !level.isLoaded(pos) ||
                MathHelper.distSqr(getBlockPos(), pos) > (long) LINK_RANGE * LINK_RANGE) {
            return false;
        }

        BlockEntity be = level.getBlockEntity(pos);
        return be != null && targetClass.isAssignableFrom(be.getClass());
    }

    @Override
    public boolean canSelect(Player player, ItemStack wand, BlockPos pos, Direction side) {
        return ItemTwigWand.getBindMode(wand);
    }

    @Override
    public boolean bindTo(Player player, ItemStack wand, BlockPos pos, Direction side) {
        if (level == null) return false;

        BlockEntity be = level.getBlockEntity(pos);
        boolean success = false;

        if (be instanceof IManaPool && isValidBinding(pos, IManaPool.class)) {
            setPoolBinding(pos);
            if (player != null) {
                player.sendMessage(new TextComponent("Bound to Mana Pool at " + pos.toShortString()), player.getUUID());
            }
            success = true;

        } else if (be instanceof IManaCollector && isValidBinding(pos, IManaCollector.class)) {
            setCollectorBinding(pos);
            if (player != null) {
                player.sendMessage(new TextComponent("Bound to Mana Collector at " + pos.toShortString()), player.getUUID());
            }
            success = true;
        }

        if (success) {
            level.playSound(null, getBlockPos(), vazkii.botania.common.handler.ModSounds.ding,
                    SoundSource.BLOCKS, 1F, 1F);
        }

        return success;
    }

    @Override
    @Nullable
    public BlockPos getBinding() {
        if (workMode == WorkMode.FUNCTIONAL) {
            return getPoolBinding();
        } else {
            return getCollectorBinding();
        }
    }

    /**
     * 处理右击切换模式
     */
    public InteractionResult onRightClick(Player player, InteractionHand hand) {
        if (level != null && (level.isClientSide || hand != InteractionHand.MAIN_HAND)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        if (isWandOfTheForest(heldItem)) {
            boolean wandBindMode = ItemTwigWand.getBindMode(heldItem);

            if (!wandBindMode) {
                toggleWorkMode();

                String modeName = workMode == WorkMode.FUNCTIONAL ? "Functional" : "Generating";
                player.sendMessage(new TextComponent("Mode switched to: " + modeName), player.getUUID());

                level.playSound(null, getBlockPos(), SoundEvents.NOTE_BLOCK_CHIME,
                        SoundSource.BLOCKS, 0.5F, workMode == WorkMode.FUNCTIONAL ? 1.0F : 1.5F);

                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }

    /**
     * 检查物品是否为森林法杖
     */
    private boolean isWandOfTheForest(ItemStack stack) {
        return stack.getItem() instanceof ItemTwigWand;
    }

    /**
     * 生成粒子效果
     */
    private void spawnParticles() {
        double particleChance = 1F - (double) mana / (double) getMaxMana() / 3.5F;
        int color = getColor();

        if (workMode == WorkMode.GENERATING) {
            color = adjustColorForGenerating(color);
        }

        float red = (color >> 16 & 0xFF) / 255F;
        float green = (color >> 8 & 0xFF) / 255F;
        float blue = (color & 0xFF) / 255F;

        if (Math.random() > particleChance) {
            Vec3 offset = level.getBlockState(getBlockPos()).getOffset(level, getBlockPos());
            double x = getBlockPos().getX() + offset.x;
            double y = getBlockPos().getY() + offset.y;
            double z = getBlockPos().getZ() + offset.z;

            BotaniaAPI.instance().sparkleFX(level,
                    x + 0.3 + Math.random() * 0.5,
                    y + 0.5 + Math.random() * 0.5,
                    z + 0.3 + Math.random() * 0.5,
                    red, green, blue, (float) Math.random(), 5);
        }
    }

    /**
     * 为产能模式调整颜色
     */
    private int adjustColorForGenerating(int originalColor) {
        int red = (originalColor >> 16 & 0xFF);
        int green = (originalColor >> 8 & 0xFF);
        int blue = (originalColor & 0xFF);

        red = Math.min(255, red + 30);
        green = Math.min(255, green + 20);

        return (red << 16) | (green << 8) | blue;
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);

        cmp.putInt(TAG_MANA, mana);
        cmp.putInt(TAG_MODE, workMode.ordinal());
        cmp.putInt(TAG_REDSTONE_SIGNAL, redstoneSignal);

        if (poolBindingPos != null) {
            cmp.put(TAG_POOL_BINDING, NbtUtils.writeBlockPos(poolBindingPos));
        }

        if (collectorBindingPos != null) {
            cmp.put(TAG_COLLECTOR_BINDING, NbtUtils.writeBlockPos(collectorBindingPos));
        }
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);

        mana = cmp.getInt(TAG_MANA);

        if (cmp.contains(TAG_MODE)) {
            workMode = WorkMode.values()[cmp.getInt(TAG_MODE)];
        }

        redstoneSignal = cmp.getInt(TAG_REDSTONE_SIGNAL);

        if (cmp.contains(TAG_POOL_BINDING)) {
            poolBindingPos = NbtUtils.readBlockPos(cmp.getCompound(TAG_POOL_BINDING));
        }

        if (cmp.contains(TAG_COLLECTOR_BINDING)) {
            collectorBindingPos = NbtUtils.readBlockPos(cmp.getCompound(TAG_COLLECTOR_BINDING));
        }
    }

    public ItemStack getHudIcon() {
        if (workMode == WorkMode.FUNCTIONAL) {
            return Registry.ITEM.getOptional(POOL_ID).map(ItemStack::new).orElse(ItemStack.EMPTY);
        } else {
            return Registry.ITEM.getOptional(SPREADER_ID).map(ItemStack::new).orElse(ItemStack.EMPTY);
        }
    }

    public abstract int getMaxMana();
    public abstract int getColor();

    /**
     * 是否接受红石信号
     */
    public boolean acceptsRedstone() {
        return false;
    }

    /**
     * 是否可以执行功能花工作
     */
    public abstract boolean canWork();

    /**
     * 执行功能花工作
     */
    public abstract void doFunctionalWork();

    /**
     * 是否可以产生魔力
     */
    public abstract boolean canGenerate();

    /**
     * 执行产能工作，返回产生的魔力数量
     */
    public abstract int doGeneratingWork();

    public static class HybridWandHud<T extends TileEntityHybridFlower> implements IWandHUD {
        protected final T flower;

        public HybridWandHud(T flower) {
            this.flower = flower;
        }

        @Override
        public void renderHUD(PoseStack ms, Minecraft mc) {
            String name = I18n.get(flower.getBlockState().getBlock().getDescriptionId());
            String mode = flower.getWorkMode() == WorkMode.FUNCTIONAL ? "Functional" : "Generating";
            String displayName = name + " (" + mode + ")";

            int color = flower.getColor();
            BotaniaAPIClient.instance().drawComplexManaHUD(ms, color, flower.getMana(), flower.getMaxMana(),
                    displayName, flower.getHudIcon(),
                    flower.isValidPoolBinding() || flower.isValidCollectorBinding());
        }
    }
}