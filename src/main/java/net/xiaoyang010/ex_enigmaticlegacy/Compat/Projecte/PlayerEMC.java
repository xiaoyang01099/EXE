package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte;

import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.UUID;

public class PlayerEMC {

    private IKnowledgeProvider provider;
    private static ITransmutationProxy transmutationProxy;
    private static IKnowledgeProvider fakeKnowledgeProvider;

    public PlayerEMC(@NotNull Player player) {
        this.provider = getKnowledgeProvider(player);
    }

    public PlayerEMC(@NotNull Level world, @NotNull UUID uuid) {
        this.provider = getKnowledgeProvider(world, uuid);
    }

    public double getEmc() {
        return provider.getEmc().doubleValue();
    }

    public void setEmc(double emc) {
        provider.setEmc(BigInteger.valueOf((long) emc));
    }

    public void removeEMC(double emc) {
        BigInteger current = provider.getEmc();
        BigInteger toRemove = BigInteger.valueOf((long) emc);
        BigInteger newEmc = current.subtract(toRemove);
        if (newEmc.compareTo(BigInteger.ZERO) < 0) {
            newEmc = BigInteger.ZERO;
        }
        provider.setEmc(newEmc);
    }

    @NotNull
    private static IKnowledgeProvider getKnowledgeProvider(@NotNull Player player) {
        if (transmutationProxy == null) {
            transmutationProxy = ProjectEAPI.getTransmutationProxy();
            if (transmutationProxy == null) {
                System.out.println("Still unable to get transmutation proxy from ProjectE");
            }
        }

        IKnowledgeProvider knowledge;
        if (transmutationProxy != null) {
            knowledge = transmutationProxy.getKnowledgeProviderFor(player.getUUID());
        } else {
            knowledge = fakeKnowledgeProvider;
        }

        return knowledge;
    }

    @NotNull
    private static IKnowledgeProvider getKnowledgeProvider(@NotNull Level world, @NotNull UUID uuid) {
        if (transmutationProxy == null) {
            transmutationProxy = ProjectEAPI.getTransmutationProxy();
            if (transmutationProxy == null) {
                System.out.println("Still unable to get transmutation proxy from ProjectE");
            }
        }

        IKnowledgeProvider knowledge;
        if (transmutationProxy != null) {
            knowledge = transmutationProxy.getKnowledgeProviderFor(uuid);
        } else {
            knowledge = fakeKnowledgeProvider;
        }

        return knowledge;
    }

    static {
        fakeKnowledgeProvider = new FakeKnowledgeProvider();
    }
}