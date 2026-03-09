package net.xiaoyang010.ex_enigmaticlegacy.api.test.res;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.CorruptionEvent;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.api.ICursedManaPool;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.api.IManaConverter;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.client.fx.WispParticleData;

import javax.annotation.Nullable;

public class TileManaConverter extends BlockEntity implements IManaConverter {

    private static final String TAG_CORRUPTION = "corruption";
    private static final String TAG_CONVERTING = "converting";
    private static final String TAG_CONVERSION_PROGRESS = "conversionProgress";

    private static final int CONVERSION_TIME = 20; // 1秒转换一次
    private static final float BASE_EFFICIENCY = 0.8f; // 基础转换效率80%
    private static final int CONVERSION_AMOUNT = 100; // 每次转换100魔力

    private int corruptionLevel = 0;
    private boolean converting = false;
    private int conversionProgress = 0;
    private final BlockManaConverter.ConversionMode mode;

    public TileManaConverter(BlockPos pos, BlockState state) {
        this(pos, state, BlockManaConverter.ConversionMode.NORMAL_TO_CURSED);
    }

    public TileManaConverter(BlockPos pos, BlockState state, BlockManaConverter.ConversionMode mode) {
        super(ModBlockEntities.MANA_CONVERTER.get(), pos, state);
        this.mode = mode;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileManaConverter converter) {
        if (converter.converting) {
            converter.spawnConversionParticles();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileManaConverter converter) {
        converter.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) return;

        IManaPool normalPool = findNearbyNormalPool();
        ICursedManaPool cursedPool = findNearbyCursedPool();

        if (normalPool != null && cursedPool != null) {
            if (mode == BlockManaConverter.ConversionMode.NORMAL_TO_CURSED) {
                convertNormalToCursedTick(normalPool, cursedPool);
            } else {
                convertCursedToNormalTick(normalPool, cursedPool);
            }
        } else {
            converting = false;
            conversionProgress = 0;
        }

        if (corruptionLevel > 0) {
            if (level.getGameTime() % 20 == 0) {
                ManaCorruptionManager.applyCorruptionEffects(level, worldPosition, corruptionLevel);
            }

            if (level.getGameTime() % 100 == 0) {
                spreadCorruption();
            }
        }

        BlockState state = getBlockState();
        if (state.getValue(BlockManaConverter.CONVERTING) != converting) {
            level.setBlockAndUpdate(worldPosition, state.setValue(BlockManaConverter.CONVERTING, converting));
        }
    }

    private void convertNormalToCursedTick(IManaPool normalPool, ICursedManaPool cursedPool) {
        if (normalPool.getCurrentMana() >= CONVERSION_AMOUNT && !cursedPool.isCursedManaFull()) {
            converting = true;
            conversionProgress++;

            if (conversionProgress >= CONVERSION_TIME) {
                int converted = convertNormalToCursed(CONVERSION_AMOUNT);
                normalPool.receiveMana(-CONVERSION_AMOUNT);
                cursedPool.receiveCursedMana(converted);

                addCorruption(1);

                conversionProgress = 0;
                setChanged();
            }
        } else {
            converting = false;
            conversionProgress = 0;
        }
    }

    private void convertCursedToNormalTick(IManaPool normalPool, ICursedManaPool cursedPool) {
        if (cursedPool.getCurrentCursedMana() >= CONVERSION_AMOUNT && !normalPool.isFull()) {
            converting = true;
            conversionProgress++;

            if (conversionProgress >= CONVERSION_TIME) {
                int converted = convertCursedToNormal(CONVERSION_AMOUNT);
                cursedPool.receiveCursedMana(-CONVERSION_AMOUNT);
                normalPool.receiveMana(converted);

                reduceCorruption(2);

                conversionProgress = 0;
                setChanged();
            }
        } else {
            converting = false;
            conversionProgress = 0;
        }
    }

    @Override
    public Level getConverterLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getConverterPos() {
        return getBlockPos();
    }

    @Override
    public int convertNormalToCursed(int normalMana) {
        float efficiency = getConversionEfficiency();
        return (int) (normalMana * efficiency);
    }

    @Override
    public int convertCursedToNormal(int cursedMana) {
        float efficiency = getConversionEfficiency() * 0.6f;
        return (int) (cursedMana * efficiency);
    }

    @Override
    public float getConversionEfficiency() {
        float corruptionPenalty = ManaCorruptionManager.getCorruptionEfficiencyPenalty(corruptionLevel);
        return BASE_EFFICIENCY * corruptionPenalty;
    }

    @Override
    public boolean isConverting() {
        return converting;
    }

    @Override
    public int getCorruptionLevel() {
        return corruptionLevel;
    }

    @Override
    public void addCorruption(int amount) {
        int oldCorruption = this.corruptionLevel;
        this.corruptionLevel = Math.min(ManaCorruptionManager.MAX_CORRUPTION, this.corruptionLevel + amount);

        if (level != null && !level.isClientSide) {
            MinecraftForge.EVENT_BUS.post(new CorruptionEvent.Increase(level, worldPosition, corruptionLevel, amount));

            if (oldCorruption < 75 && corruptionLevel >= 75) {
                MinecraftForge.EVENT_BUS.post(new CorruptionEvent.Critical(level, worldPosition, corruptionLevel));
            }
        }

        setChanged();
    }

    @Override
    public void reduceCorruption(int amount) {
        int oldCorruption = this.corruptionLevel;
        this.corruptionLevel = Math.max(0, this.corruptionLevel - amount);

        if (level != null && !level.isClientSide) {
            MinecraftForge.EVENT_BUS.post(new CorruptionEvent.Decrease(level, worldPosition, corruptionLevel, amount));
        }

        setChanged();
    }

    @Nullable
    private IManaPool findNearbyNormalPool() {
        if (level == null) return null;

        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos checkPos = worldPosition.offset(x, y, z);
                    BlockEntity tile = level.getBlockEntity(checkPos);
                    if (tile instanceof IManaPool pool && !(tile instanceof ICursedManaPool)) {
                        return pool;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private ICursedManaPool findNearbyCursedPool() {
        if (level == null) return null;

        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos checkPos = worldPosition.offset(x, y, z);
                    BlockEntity tile = level.getBlockEntity(checkPos);
                    if (tile instanceof ICursedManaPool pool) {
                        return pool;
                    }
                }
            }
        }
        return null;
    }

    private void spreadCorruption() {
        if (level == null || corruptionLevel < 25) return;

        int spreadAmount = ManaCorruptionManager.calculateCorruptionSpread(corruptionLevel);

        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos checkPos = worldPosition.offset(x, y, z);
                    BlockEntity tile = level.getBlockEntity(checkPos);

                    if (tile instanceof TileManaConverter converter) {
                        converter.addCorruption(spreadAmount);
                    } else if (tile instanceof TileCursedManaPool pool) {
                        MinecraftForge.EVENT_BUS.post(new CorruptionEvent.Spread(level, worldPosition, corruptionLevel, checkPos));
                        reduceCorruption(1);
                    }
                }
            }
        }
    }

    private void spawnConversionParticles() {
        if (level == null || !level.isClientSide) return;

        int color = mode == BlockManaConverter.ConversionMode.NORMAL_TO_CURSED ? 0x8B00FF : 0x00BFFF;
        float r = (color >> 16 & 0xFF) / 255F;
        float g = (color >> 8 & 0xFF) / 255F;
        float b = (color & 0xFF) / 255F;

        for (int i = 0; i < 3; i++) {
            WispParticleData data = WispParticleData.wisp(0.15F, r, g, b, true);
            level.addParticle(data,
                    worldPosition.getX() + 0.5 + (Math.random() - 0.5) * 0.5,
                    worldPosition.getY() + 0.5 + (Math.random() - 0.5) * 0.5,
                    worldPosition.getZ() + 0.5 + (Math.random() - 0.5) * 0.5,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        corruptionLevel = tag.getInt(TAG_CORRUPTION);
        converting = tag.getBoolean(TAG_CONVERTING);
        conversionProgress = tag.getInt(TAG_CONVERSION_PROGRESS);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_CORRUPTION, corruptionLevel);
        tag.putBoolean(TAG_CONVERTING, converting);
        tag.putInt(TAG_CONVERSION_PROGRESS, conversionProgress);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt(TAG_CORRUPTION, corruptionLevel);
        tag.putBoolean(TAG_CONVERTING, converting);
        return tag;
    }
}
