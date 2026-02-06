package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte;

import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.UUID;

public class PlayerEMC {
    private IKnowledgeProvider provider;
    private UUID playerUUID;
    private Level level;
    private static ITransmutationProxy transmutationProxy;
    private static final IKnowledgeProvider FAKE_KNOWLEDGE_PROVIDER = new FakeKnowledgeProvider();

    public PlayerEMC(@NotNull Level world, @NotNull UUID uuid) {
        this.provider = getKnowledgeProvider(world, uuid);
        this.playerUUID = uuid;
        this.level = world;
        if (this.provider == null) {
            this.provider = FAKE_KNOWLEDGE_PROVIDER;
        }
    }

    public boolean isValid() {
        return provider != null && provider != FAKE_KNOWLEDGE_PROVIDER;
    }

    public double getEmc() {
        if (!isValid()) {
            return 0.0;
        }
        return provider.getEmc().doubleValue();
    }

    public void removeEMC(double emc) {
        if (!isValid()) {
            return;
        }

        BigInteger current = provider.getEmc();
        BigInteger toRemove = BigInteger.valueOf((long) emc);
        BigInteger newEmc = current.subtract(toRemove);

        if (newEmc.compareTo(BigInteger.ZERO) < 0) {
            newEmc = BigInteger.ZERO;
        }

        provider.setEmc(newEmc);
        syncToClient();
    }

    private void syncToClient() {
        if (level == null || level.isClientSide || !isValid()) {
            return;
        }

        ServerPlayer player = (ServerPlayer) level.getPlayerByUUID(playerUUID);
        if (player != null) {
            provider.syncEmc(player);
        }
    }

    @Nullable
    private static IKnowledgeProvider getKnowledgeProvider(@NotNull Level world, @NotNull UUID uuid) {
        if (world.isClientSide) {
            return FAKE_KNOWLEDGE_PROVIDER;
        }

        try {
            if (transmutationProxy == null) {
                transmutationProxy = ProjectEAPI.getTransmutationProxy();
            }

            if (transmutationProxy == null) {
                return FAKE_KNOWLEDGE_PROVIDER;
            }

            IKnowledgeProvider knowledge = transmutationProxy.getKnowledgeProviderFor(uuid);

            return knowledge != null ? knowledge : FAKE_KNOWLEDGE_PROVIDER;

        } catch (Exception e) {
            System.err.println("Failed to get KnowledgeProvider for UUID: " + uuid);
            e.printStackTrace();
            return FAKE_KNOWLEDGE_PROVIDER;
        }
    }

//    @NotNull
//    private static IKnowledgeProvider getKnowledgeProvider(@NotNull Level world, @NotNull UUID uuid) {
//        if (transmutationProxy == null) {
//            transmutationProxy = ProjectEAPI.getTransmutationProxy();
//        }
//
//        IKnowledgeProvider knowledge;
//        if (transmutationProxy != null) {
//            knowledge = transmutationProxy.getKnowledgeProviderFor(uuid);
//        } else {
//            knowledge = fakeKnowledgeProvider;
//        }
//        return knowledge;
//    }
//
//    static {
//        fakeKnowledgeProvider = new FakeKnowledgeProvider();
//    }
}