package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Item.Sling;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class MessagePlayerAction {

    private final int entityID;
    private final int actionType;

    public MessagePlayerAction(LivingEntity entity, int actionType) {
        this.entityID = entity.getId();
        this.actionType = actionType;
    }

    private MessagePlayerAction(int entityID, int actionType) {
        this.entityID = entityID;
        this.actionType = actionType;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityID);
        buf.writeInt(this.actionType);
    }

    public static MessagePlayerAction decode(FriendlyByteBuf buf) {
        return new MessagePlayerAction(buf.readInt(), buf.readInt());
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

        LivingEntity entity = getEntityLivingBase(level);
        if (entity == null) return;

        switch (this.actionType) {
            case Sling.SWING_ARM -> entity.swing(InteractionHand.MAIN_HAND);
        }
    }

    @Nullable
    public LivingEntity getEntityLivingBase(Level world) {
        Entity entity = world.getEntity(this.entityID);
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        return null;
    }
}