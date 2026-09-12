package org.xiaoyang.ex_enigmaticlegacy.api.test.curse.api;

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
        if (amount <= 0) return;
        this.corruption = Math.min(100, this.corruption + amount);
    }

    public void reduceCorruption(int amount) {
        if (amount <= 0) return;
        this.corruption = Math.max(0, this.corruption - amount);
    }

    public int getCurrentCapacity() {
        float penalty = corruption / 100.0F;
        return Math.max(0, (int) (Math.max(0, originalCapacity) * (1.0F - penalty * 0.5F)));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("corruption", corruption);
        tag.putInt("originalCapacity", originalCapacity);
        return tag;
    }

    public static PoolCorruptionData load(CompoundTag tag) {
        PoolCorruptionData data = new PoolCorruptionData();
        data.corruption = Math.max(0, Math.min(100, tag.getInt("corruption")));
        int capacity = tag.getInt("originalCapacity");
        data.originalCapacity = capacity > 0 ? capacity : 1000000;
        return data;
    }
}
