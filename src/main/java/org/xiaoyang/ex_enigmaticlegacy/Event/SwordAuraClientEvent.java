package org.xiaoyang.ex_enigmaticlegacy.Event;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Util.CameraShakeUtil;
import org.xiaoyang.ex_enigmaticlegacy.Event.registry.ChargeLightningClientRegistry;
import org.xiaoyang.ex_enigmaticlegacy.Item.BeamCrossTestItem;
import org.xiaoyang.ex_enigmaticlegacy.Item.LightingItem;
import org.xiaoyang.ex_enigmaticlegacy.Item.FlySwordItem;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful.ChargeTracker;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Exe.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SwordAuraClientEvent {
    @SubscribeEvent
    public static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() || event.isCanceled()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) return;
        if (!(minecraft.player.getMainHandItem().getItem() instanceof FlySwordItem)) return;
        if (minecraft.hitResult == null || minecraft.hitResult.getType() == HitResult.Type.MISS) return;
        ForgeEvent.playSwordAuraSound(minecraft.player);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (Exe.POST == null || Minecraft.getInstance().level == null) return;

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            Exe.POST.flushFlySwordTrailPose(event.getPoseStack());
            queueChargingLightning(event);
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            Exe.POST.setPartialTick(event.getPartialTick(), event.getCamera());
            AutoTrackingClientHandler.submitLockedTargetOutline(Minecraft.getInstance().level);
            SparklingClientHandler.submitSparklingFireOutlines(Minecraft.getInstance().level, event.getFrustum());
            Exe.POST.doPostProcessing();
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        CameraShakeUtil.apply(event);
    }


    public static void queueChargingLightning(RenderLevelStageEvent event) {
        float partialTick = event.getPartialTick();
        Level level = Minecraft.getInstance().level;

        ChargeLightningClientRegistry.cleanup(level);
        for (ChargeLightningClientRegistry.ChargeVisualState state : ChargeLightningClientRegistry.activeCharges()) {
            Player player = level.getPlayerByUUID(state.playerId());
            if (player == null || !player.isAlive()) {
                ChargeLightningClientRegistry.stop(state.playerId());
                continue;
            }

            if (!player.isUsingItem()) {
                if (state.type() != ChargeLightningClientRegistry.ChargeVisualType.BEAM_CROSS) {
                    ChargeTracker.stopCharge(player);
                }
                ChargeLightningClientRegistry.stop(player);
                continue;
            }

            if (state.type() == ChargeLightningClientRegistry.ChargeVisualType.BEAM_CROSS) {
                if (!(player.getUseItem().getItem() instanceof BeamCrossTestItem)) {
                    ChargeLightningClientRegistry.stop(player);
                    continue;
                }
                BeamCrossTestItem.renderChargeEffects(player, partialTick);
                continue;
            }

            if (!ChargeTracker.isCharging(player)) {
                ChargeLightningClientRegistry.stop(player);
                continue;
            }
            if (!(player.getUseItem().getItem() instanceof LightingItem)) {
                ChargeTracker.stopCharge(player);
                ChargeLightningClientRegistry.stop(player);
                continue;
            }

            float progress = Mth.clamp(ChargeTracker.getProgress(player), 0.0f, 1.0f);
            Exe.POST.effects().addChargingLightning(player, progress, partialTick, state.colorful());
        }
    }
}
