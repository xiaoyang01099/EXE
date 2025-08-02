package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.energy.IEnergyStorage;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IControl;

import javax.annotation.Nonnull;

public class EnergyControl implements IEnergyStorage, IControl<EnergyControl> {
    private final int capacity;
    private int energyUsage;
    private int energy;

    public EnergyControl(final int capacity, final int energyUsage)
    {
        this(capacity, energyUsage, 0);
    }

    public EnergyControl(final int capacity, final int energyUsage, final int energy) {
        this.capacity = capacity;
        this.energyUsage = energyUsage;
        this.energy = energy;
    }

    @Override
    public int receiveEnergy(final int maxReceive, final boolean simulate) {
        if (!canReceive())
            return 0;

        int energyReceived = Math.min(capacity - energy, Math.min(this.capacity, maxReceive));
        if (!simulate)
            energy += energyReceived;
        return energyReceived;
    }

    public int getEnergyUsage()
    {
        return energyUsage;
    }

    public void setEnergyUsage(final int energyUsage)
    {
        this.energyUsage = energyUsage;
    }

    @Override
    public int extractEnergy(final int maxExtract, final boolean simulate)
    {
        return 0;
    }

    @Override
    public int getEnergyStored()
    {
        return energy;
    }

    @Override
    public int getMaxEnergyStored()
    {
        return capacity;
    }

    @Override
    public boolean canExtract()
    {
        return false;
    }

    @Override
    public boolean canReceive()
    {
        return true;
    }

    private void useEnergy(final int energy)
    {
        this.energy -= energy;
    }

    public void setEnergyStored(final int energy)
    {
        this.energy = energy;
    }

    @Override
    public boolean canOperate()
    {
        return getEnergyStored() >= energyUsage;
    }

    @Override
    public void operate()
    {
        useEnergy(energyUsage);
    }

    @Nonnull
    @Override
    public CompoundTag writeNBT() {
        final CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("Energy", energy);
        return compoundTag;
    }

    @Override
    public void readNBT(@Nonnull final CompoundTag compoundTag) {
        if (compoundTag.contains("Energy"))
            setEnergyStored(compoundTag.getInt("Energy"));
    }

    @Nonnull
    @Override
    public EnergyControl copy()
    {
        return new EnergyControl(capacity, energyUsage, energy);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || (obj instanceof EnergyControl && this.energy == ((EnergyControl) obj).energy);
    }
}