package net.xiaoyang010.ex_enigmaticlegacy.Entity.others;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class EntityAnonymousSteve extends AbstractClientPlayer {

    public EntityAnonymousSteve(ClientLevel world) {
        super(world, new GameProfile(null, "abSteveForRenderer"));
        this.noPhysics = true;
    }

    @Override
    public void tick() {
    }

    @Override
    public void setYHeadRot(float yHeadRot) {
        this.yHeadRot = yHeadRot;
        this.yHeadRotO = yHeadRot;
    }

    @Override
    public void setXRot(float xRot) {
        this.xRot = xRot;
        this.xRotO = xRot;
    }

    @Override
    public void setYRot(float yRot) {
        this.yRot = yRot;
        this.yRotO = yRot;
    }

    public boolean hasPermissions(int i) {
        return false;
    }

    @Nullable
    public BlockPos getRespawnPosition() {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public int getLightColor() {
        return 15728880;
    }

    public boolean isInvisible() {
        return true;
    }

    @Override
    public boolean isInvisibleTo(Player player) {
        return true;
    }

    public void sendSystemMessage(Component message) {
    }
}