package net.xiaoyang010.ex_enigmaticlegacy.api.test.api;

import net.minecraft.nbt.CompoundTag;


public class PoolCorruptionData {
    private int corruption = 0;
    private int originalCapacity = 1000000;

    public PoolCorruptionData() {}

    public PoolCorruptionData(int originalCapacity) {
        this.originalCapacity = originalCapacity;
    }

    public int getCorruption() {
        return corruption;
    }

    public void addCorruption(int amount) {
        this.corruption = Math.min(100, this.corruption + amount);
    }

    public void reduceCorruption(int amount) {
        this.corruption = Math.max(0, this.corruption - amount);
    }

    /**
     * 计算当前容量(污染会降低容量)
     */
    public int getCurrentCapacity() {
        float penalty = corruption / 100.0F;
        return (int) (originalCapacity * (1.0F - penalty * 0.5F));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("corruption", corruption);
        tag.putInt("originalCapacity", originalCapacity);
        return tag;
    }

    public static PoolCorruptionData load(CompoundTag tag) {
        PoolCorruptionData data = new PoolCorruptionData();
        data.corruption = tag.getInt("corruption");
        data.originalCapacity = tag.getInt("originalCapacity");
        return data;
    }
}
