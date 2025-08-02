package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityMotionMessage {
    private int entityID;
    private double motX;
    private double motY;
    private double motZ;
    private boolean motionless;

    public EntityMotionMessage() {
    }

    public EntityMotionMessage(int entityID, double motX, double motY, double motZ, boolean motionless) {
        this.entityID = entityID;
        this.motX = motX;
        this.motY = motY;
        this.motZ = motZ;
        this.motionless = motionless;
    }

    public EntityMotionMessage(Entity entity, double motX, double motY, double motZ, boolean motionless) {
        this(entity.getId(), motX, motY, motZ, motionless);
    }

    public static void encode(EntityMotionMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.entityID);
        buf.writeDouble(message.motX);
        buf.writeDouble(message.motY);
        buf.writeDouble(message.motZ);
        buf.writeBoolean(message.motionless);
    }

    public static EntityMotionMessage decode(FriendlyByteBuf buf) {
        int entityID = buf.readInt();
        double motX = buf.readDouble();
        double motY = buf.readDouble();
        double motZ = buf.readDouble();
        boolean motionless = buf.readBoolean();
        return new EntityMotionMessage(entityID, motX, motY, motZ, motionless);
    }

    public static void handle(EntityMotionMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                message.handleClientSide();
            }
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClientSide() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            Level level = player.level;
            Entity targetEntity = level.getEntity(this.entityID);

            if (targetEntity instanceof LivingEntity livingEntity) {
                if (targetEntity != player) {
                    if (this.motionless) {
                        livingEntity.fallDistance = 0.0F;
                    }

                    livingEntity.setDeltaMovement(this.motX, this.motY, this.motZ);
                    livingEntity.hasImpulse = true;
                } else {
                    if (this.motionless) {
                        player.fallDistance = 0.0F;
                    }

                    player.setDeltaMovement(this.motX, this.motY, this.motZ);
                }
            }
        }
    }
}