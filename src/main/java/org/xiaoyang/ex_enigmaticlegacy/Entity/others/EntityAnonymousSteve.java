package org.xiaoyang.ex_enigmaticlegacy.Entity.others;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class EntityAnonymousSteve extends AbstractClientPlayer {
    public EntityAnonymousSteve(ClientLevel world) {
        super(world, new GameProfile(UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed7"), "abSteveForRenderer"));
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

    @Override
    public boolean hasPermissions(int i) {
        return false;
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 15728880;
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    public boolean isInvisibleTo(Player player) {
        return true;
    }

    @Override
    public void sendSystemMessage(Component message) {
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}