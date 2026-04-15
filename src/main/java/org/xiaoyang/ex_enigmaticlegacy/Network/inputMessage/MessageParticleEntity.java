package org.xiaoyang.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Item.res.Sling;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Supplier;

public class MessageParticleEntity {

    private final int entityID;
    private final int particleType;

    public MessageParticleEntity(Entity entity, int particleType) {
        this.entityID = entity.getId();
        this.particleType = particleType;
    }

    private MessageParticleEntity(int entityID, int particleType) {
        this.entityID = entityID;
        this.particleType = particleType;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityID);
        buf.writeInt(this.particleType);
    }

    public static MessageParticleEntity decode(FriendlyByteBuf buf) {
        return new MessageParticleEntity(buf.readInt(), buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handleOnClient)
        );
        context.setPacketHandled(true);
    }

    private void handleOnClient() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = getEntity(level);
        if (entity == null) return;

        Random random = new Random();

        switch (this.particleType) {
            case Sling.ENTITY_SILING_CHAGE -> {
                for (int i = 0; i < 20; i++) {
                    level.addParticle(ParticleTypes.CRIT,
                            entity.getX() + (random.nextFloat() * entity.getBbWidth() * 2.0F) - entity.getBbWidth(),
                            entity.getY() + (random.nextFloat() * entity.getBbHeight()),
                            entity.getZ() + (random.nextFloat() * entity.getBbWidth() * 2.0F) - entity.getBbWidth(),
                            0.0D, 0.0D, 0.0D);
                }
            }
            case Sling.ENTITY_SILING_CHAGE_MAX -> {
                for (int i = 0; i < 20; i++) {
                    level.addParticle(ParticleTypes.ENCHANTED_HIT,
                            entity.getX() + (random.nextFloat() * entity.getBbWidth() * 2.0F) - entity.getBbWidth(),
                            entity.getY() + (random.nextFloat() * entity.getBbHeight()),
                            entity.getZ() + (random.nextFloat() * entity.getBbWidth() * 2.0F) - entity.getBbWidth(),
                            0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    @Nullable
    public Entity getEntity(Level world) {
        return world.getEntity(this.entityID);
    }
}