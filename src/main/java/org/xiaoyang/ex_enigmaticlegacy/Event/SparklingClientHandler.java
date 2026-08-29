package org.xiaoyang.ex_enigmaticlegacy.Event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;
import org.xiaoyang.ex_enigmaticlegacy.Client.particle.state.SparklingFlightClientState;
import org.xiaoyang.ex_enigmaticlegacy.Client.particle.state.SparklingOutlineClientState;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEffects;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.SparklingBoostC2SPacket;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.SparklingFlightInputC2SPacket;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline.MiaoOutlineStyle;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

@Mod.EventBusSubscriber(modid = Exe.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SparklingClientHandler {
    public static int teleportCooldown = 0;
    public static boolean lastTeleportKeyDown;
    public static boolean lastFlightBoostKeyDown;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        SparklingOutlineClientState.tick(level);
        SparklingFlightClientState.tick(level);
        if (player == null) {
            lastFlightBoostKeyDown = false;
            lastTeleportKeyDown = false;
            return;
        }

        if (teleportCooldown > 0) {
            teleportCooldown--;
        }
        handleFlightBoostInput(minecraft, player);
        handleTeleportInput(minecraft, player);
    }

    public static void handleFlightBoostInput(Minecraft minecraft, LocalPlayer player) {
        boolean hasBuff = player.hasEffect(ModEffects.SPARKLING_EFFECT.get());
        boolean boostKeyDown = minecraft.screen == null && hasBuff && minecraft.options.keySprint.isDown();
        if (boostKeyDown == lastFlightBoostKeyDown) return;

        lastFlightBoostKeyDown = boostKeyDown;
        SparklingFlightClientState.apply(player.getId(), boostKeyDown, false,
                ConfigFile.sparklingFlightBoostMaxSpeed());
        NetworkHandler.sendToServer(new SparklingFlightInputC2SPacket(boostKeyDown));
    }

    public static void handleTeleportInput(Minecraft minecraft, LocalPlayer player) {
        boolean teleportKeyDown = isTeleportKeyDown(minecraft);
        boolean pressedThisTick = teleportKeyDown && !lastTeleportKeyDown;
        lastTeleportKeyDown = teleportKeyDown;

        if (!pressedThisTick) return;
        if (teleportCooldown > 0) return;
        if (!player.hasEffect(ModEffects.SPARKLING_EFFECT.get())) return;

        NetworkHandler.sendToServer(new SparklingBoostC2SPacket());
        teleportCooldown = ConfigFile.sparklingTeleportCooldownTicks();
    }

    public static void submitSparklingFireOutlines(ClientLevel level, Frustum frustum) {
        if (level == null || Exe.POST == null) return;

        SparklingOutlineClientState.forEachActive(level, entity -> {
            if (!shouldRenderSparklingOutline(entity, frustum)) return;
            Exe.POST.addMiaoOutline(entity, MiaoOutlineStyle.SPARKLING_FRUIT_FIRE);
        });
    }

    public static boolean shouldCaptureSparklingOutline(Entity entity) {
        if (shouldSkipFirstPersonLocalPlayerOutline(entity)) return false;
        if (!(entity instanceof LivingEntity)) return false;
        return isSparklingOutlineActive(entity);
    }

    public static boolean shouldRenderSparklingOutline(Entity entity, Frustum frustum) {
        if (entity == null || !entity.isAlive()) return false;
        if (shouldSkipFirstPersonLocalPlayerOutline(entity)) return false;
        if (!(entity instanceof LivingEntity)) return false;
        if (!isSparklingOutlineActive(entity)) return false;
        return frustum == null || frustum.isVisible(entity.getBoundingBox().inflate(0.5D));
    }

    public static boolean isSparklingOutlineActive(Entity entity) {
        if (entity == null) return false;
        if (SparklingOutlineClientState.isActive(entity.getId())) return true;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        return player != null && entity.getId() == player.getId() && player.hasEffect(ModEffects.SPARKLING_EFFECT.get());
    }

    public static boolean shouldSkipFirstPersonLocalPlayerOutline(Entity entity) {
        if (entity == null) return false;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return false;

        if (!minecraft.options.getCameraType().isFirstPerson()) return false;

        return entity == player || entity.getId() == player.getId();
    }

    public static boolean isTeleportKeyDown(Minecraft minecraft) {
        long window = minecraft.getWindow().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT);
    }
}
