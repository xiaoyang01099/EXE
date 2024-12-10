package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.FlowerTile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;

import javax.annotation.Nullable;

public class DaybloomBlockTile extends TileEntityGeneratingFlower {
    private static final String TAG_PRIME_POSITION_X = "primePositionX";
    private static final String TAG_PRIME_POSITION_Y = "primePositionY";
    private static final String TAG_PRIME_POSITION_Z = "primePositionZ";
    private static final String TAG_SAVED_POSITION = "savedPosition";
    private static final String TAG_AGE = "age";  // 用于追踪花的寿命
    private static final String TAG_MANA = "mana";  // 用于追踪魔力

    private int primePositionX, primePositionY, primePositionZ;
    private boolean savedPosition;
    private int age;
    private int passiveGenerationTickCounter;
    private int mana;  // 当前魔力值

    public DaybloomBlockTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DAYBLOOM_TILE.get(), pos, state);
        this.age = 0;
        this.passiveGenerationTickCounter = 0;
        this.mana = 0;  // 初始化魔力为0
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (!getLevel().isClientSide) {
            // tick增加年龄计时器
            this.age++;

            // 检查是否可以生成魔力
            if (canGenerate(getLevel(), getBlockPos())) {
                // 增加被动生成的计时器
                passiveGenerationTickCounter++;

                // 根据是否为Prime花来决定生成频率
                int delay = isPrime() ? 10 : 12;

                // 每达到延迟值后生成一次魔力
                if (passiveGenerationTickCounter >= delay) {
                    addMana(getValueForPassiveGeneration());
                    passiveGenerationTickCounter = 0;  // 重置计时器
                }
            }

            // 如果花已经超过3个Minecraft天（72000 ticks），则枯萎
            if (this.age >= 72000) {
                getLevel().removeBlock(getBlockPos(), false);
            }

            // 检查是否为Prime位置，如果是则保持位置
            if (isPrime() && (!savedPosition || primePositionX != getBlockPos().getX()
                    || primePositionY != getBlockPos().getY() || primePositionZ != getBlockPos().getZ())) {
                getLevel().removeBlock(getBlockPos(), false);
            }
        }
    }

    public void setPrimusPosition() {
        primePositionX = getBlockPos().getX();
        primePositionY = getBlockPos().getY();
        primePositionZ = getBlockPos().getZ();
        savedPosition = true;
    }

    @Override
    public int getMaxMana() {
        return 100;  // 最大魔力为100
    }

    @Override
    public int getColor() {
        return 0xFFFF00;  // 花的颜色
    }

    // 增加魔力的方法
    public void addMana(int amount) {
        this.mana = Math.min(this.mana + amount, getMaxMana());  // 确保不会超过最大魔力值
        setChanged();
    }

    // 获取当前魔力值
    public int getMana() {
        return this.mana;
    }

    @Nullable
    private IManaReceiver getManaReceiverInRange() {
        int range = 6;
        for (BlockPos pos : BlockPos.betweenClosed(getBlockPos().offset(-range, -range, -range),
                getBlockPos().offset(range, range, range))) {
            if (level != null && level.getBlockEntity(pos) instanceof IManaReceiver receiver) {
                return receiver;
            }
        }
        return null;
    }

    // 确定花是否可以生成魔力
    public static boolean canGenerate(Level world, BlockPos pos) {
        if (world == null || pos == null) {
            return false;
        }

        Biome biome = world.getBiome(pos).value();
        boolean rain = biome.getPrecipitation() != Biome.Precipitation.NONE
                && (world.isRaining() || world.isThundering());
        return world.isDay() && !rain && world.canSeeSky(pos.above());
    }

    // 设置被动产能值，根据是否为Prime花来调整
    private int getValueForPassiveGeneration() {
        return isPrime() ? 2 : 1;  // Prime版本的花生成更多魔力
    }

    // 写入花的状态到NBT
    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        if (isPrime()) {
            cmp.putInt(TAG_PRIME_POSITION_X, primePositionX);
            cmp.putInt(TAG_PRIME_POSITION_Y, primePositionY);
            cmp.putInt(TAG_PRIME_POSITION_Z, primePositionZ);
            cmp.putBoolean(TAG_SAVED_POSITION, savedPosition);
        }
        cmp.putInt(TAG_AGE, this.age);  // 保存花的年龄
        cmp.putInt(TAG_MANA, this.mana);  // 保存魔力值
    }

    // 从NBT读取花的状态
    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        if (isPrime()) {
            primePositionX = cmp.getInt(TAG_PRIME_POSITION_X);
            primePositionY = cmp.getInt(TAG_PRIME_POSITION_Y);
            primePositionZ = cmp.getInt(TAG_PRIME_POSITION_Z);
            savedPosition = cmp.getBoolean(TAG_SAVED_POSITION);
        }
        this.age = cmp.getInt(TAG_AGE);  // 读取花的年龄
        this.mana = cmp.getInt(TAG_MANA);  // 读取魔力值
    }

    @Nullable
    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getBlockPos(), 5);  // 设置作用半径为5格
    }

    // 判断是否为Prime花
    public boolean isPrime() {
        return false;  // 默认不是Prime花
    }

    // Prime子类，返回true
    public static class Prime extends DaybloomBlockTile {
        public Prime(BlockPos pos, BlockState state) {
            super(pos, state);
        }

        @Override
        public boolean isPrime() {
            return true;  // Prime花返回true
        }
    }
}
