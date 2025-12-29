package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;

public class FakeKnowledgeProvider implements IKnowledgeProvider {

    @Override
    public boolean hasFullKnowledge() {
        return false;
    }

    @Override
    public void setFullKnowledge(boolean var1) {
    }

    @Override
    public void clearKnowledge() {
    }

    @Override
    public boolean hasKnowledge(@NotNull ItemInfo var1) {
        return false;
    }

    @Override
    public boolean addKnowledge(@NotNull ItemInfo var1) {
        return false;
    }

    @Override
    public boolean removeKnowledge(@NotNull ItemInfo var1) {
        return false;
    }

    @NotNull
    @Override
    public Set<ItemInfo> getKnowledge() {
        return Collections.emptySet();
    }

    @NotNull
    @Override
    public IItemHandler getInputAndLocks() {
        return null;
    }

    @Override
    public BigInteger getEmc() {
        return BigInteger.ZERO;
    }

    @Override
    public void setEmc(BigInteger var1) {
    }

    @Override
    public void sync(@NotNull ServerPlayer var1) {
    }

    @Override
    public void syncEmc(@NotNull ServerPlayer var1) {
    }

    @Override
    public void syncKnowledgeChange(@NotNull ServerPlayer var1, ItemInfo var2, boolean var3) {
    }

    @Override
    public void syncInputAndLocks(@NotNull ServerPlayer var1, List<Integer> var2, TargetUpdateType var3) {
    }

    @Override
    public void receiveInputsAndLocks(Map<Integer, ItemStack> var1) {
    }

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
    }
}