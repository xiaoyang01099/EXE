package org.xiaoyang.ex_enigmaticlegacy.Event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Util.AutoTrackingTargetValidator;
import org.xiaoyang.ex_enigmaticlegacy.Config.MagicBowConfig;
import org.xiaoyang.ex_enigmaticlegacy.Item.MagicBowItem;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.AutoTrackingShootC2SPacket;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline.MiaoOutlineStyle;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline.MiaoOutlineTargetMaskStore;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

import java.util.Comparator;

@Mod.EventBusSubscriber(modid = Exe.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AutoTrackingClientHandler {
    private static int lockedTargetId = -1;
    private static int autoShootRequestCooldown = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) {
            setLockedTarget(-1);
            return;
        }

        if (autoShootRequestCooldown > 0) {
            autoShootRequestCooldown--;
        }
        if (!isDrawingMagicBowWithAutoTracking(player)) {
            autoShootRequestCooldown = 0;
            setLockedTarget(-1);
            return;
        }

        setLockedTarget(findBestTarget(level, player));
    }

    public static int getLockedTargetId() {
        return lockedTargetId;
    }

    public static void requestShoot(boolean restartUsing) {
        NetworkHandler.sendToServer(new AutoTrackingShootC2SPacket(lockedTargetId, restartUsing));
        if (!restartUsing) {
            setLockedTarget(-1);
        }
    }

    public static void requestAutoShootIfReady(Player player, ItemStack stack, int remainingUseDuration) {
        if (!(stack.getItem() instanceof MagicBowItem magicBowItem)) return;
        if (autoShootRequestCooldown > 0) return;

        int useTicks = magicBowItem.getChargeUseTicks(stack, player, remainingUseDuration);
        int fullTicks = magicBowItem.getFullChargeTicks(stack, magicBowItem.getChargeType(stack));
        if (useTicks < fullTicks) return;

        autoShootRequestCooldown = 5;
        requestShoot(true);
    }

    public static boolean isDrawingMagicBowWithAutoTracking(LocalPlayer player) {
        if (!player.isUsingItem()) return false;
        ItemStack stack = player.getUseItem();
        if (!(stack.getItem() instanceof MagicBowItem magicBowItem)) return false;
        return magicBowItem.hasAutoTracking(stack);
    }

    public static int findBestTarget(ClientLevel level, LocalPlayer player) {
        double maxRange = MagicBowConfig.autoTrackingMaxLockRange();
        AABB searchBox = player.getBoundingBox().inflate(maxRange);
        return level.getEntitiesOfClass(LivingEntity.class, searchBox, entity -> AutoTrackingTargetValidator.isValidClientTarget(player, entity))
                .stream()
                .min(Comparator
                        .comparingDouble((LivingEntity entity) -> AutoTrackingTargetValidator.aimScore(player, entity))
                        .thenComparingDouble(entity -> entity.distanceToSqr(player)))
                .map(Entity::getId)
                .orElse(-1);
    }

    public static void setLockedTarget(int targetId) {
        if (lockedTargetId != targetId && lockedTargetId >= 0) {
            MiaoOutlineTargetMaskStore.clear(lockedTargetId);
        }
        lockedTargetId = targetId;
    }

    public static void submitLockedTargetOutline(ClientLevel level) {
        if (level == null || lockedTargetId < 0) return;
        if (Exe.POST == null) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !isDrawingMagicBowWithAutoTracking(player)) return;

        Entity entity = level.getEntity(lockedTargetId);
        if (entity == null || !entity.isAlive()) return;
        Exe.POST.addMiaoOutline(entity, MiaoOutlineStyle.AUTO_TRACKING_RED);
    }

    public static Entity getLockedTarget(ClientLevel level) {
        if (level == null || lockedTargetId < 0) return null;
        Entity entity = level.getEntity(lockedTargetId);
        if (entity == null || !entity.isAlive()) return null;
        return entity;
    }
}
