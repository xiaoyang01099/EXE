package org.xiaoyang.ex_enigmaticlegacy.api.shader.bladeflash;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigHandler;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModTags;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.coffin.CoffinVisuals;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Exe.MODID, value = Dist.CLIENT)
public final class BladeEvents {
    private static boolean eligible(net.minecraft.world.item.ItemStack stack) {
        return org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.blade(stack)!=org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.Blade.NONE;
    }
    @SubscribeEvent public static void logout(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.clearClient();
    }
    private static final KeyMapping PREVIEW = new KeyMapping("key.ex_enigmaticlegacy.preview",
            KeyConflictContext.IN_GAME,
            InputConstants.UNKNOWN,
            "key.categories.ex_enigmaticlegacy");

    private static final KeyMapping DEBUG = new KeyMapping("key.ex_enigmaticlegacy.debug",
            KeyConflictContext.IN_GAME,
            InputConstants.UNKNOWN,
            "key.categories.ex_enigmaticlegacy");

    private record Swing(int tick, boolean active) {}
    private static final Map<UUID, Swing> SWINGS = new HashMap<>();
    private static ClientLevel lastLevel;

    @Mod.EventBusSubscriber(modid = Exe.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent event) {
            event.register(PREVIEW);
            event.register(DEBUG);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (lastLevel != mc.level) {
            BladeFlashClient.clear();
            DemoSlashes.clear();
            SWINGS.clear();
            BladeFlashRenderer.INSTANCE.release();
            lastLevel = mc.level;
        }
        if (mc.level == null || mc.player == null) return;
        while (DEBUG.consumeClick()) BladeFlashRenderer.INSTANCE.debugView = (BladeFlashRenderer.INSTANCE.debugView + 1) % 3;
        while (PREVIEW.consumeClick()) {
            if (ConfigHandler.ENABLED.get() && (eligible(mc.player.getMainHandItem())
                || eligible(mc.player.getOffhandItem()))) DemoSlashes.spawn(mc.player);
        }
        if (mc.isPaused() || !ConfigHandler.ENABLED.get() || ConfigHandler.HELD_SWORD_TRAILS.get()
            || !ConfigHandler.DEMO_ON_SWORD_SWING.get()) {
            SWINGS.clear();
            return;
        }
        SWINGS.keySet().removeIf(id -> mc.level.getPlayerByUUID(id) == null);
        for (Player player : mc.level.players()) {
            Swing old = SWINGS.put(player.getUUID(), new Swing(player.swingTime, player.swinging));
            if (player.swinging && (old == null || !old.active || player.swingTime < old.tick)
                && eligible(player.getItemInHand(player.swingingArm == null ? net.minecraft.world.InteractionHand.MAIN_HAND : player.swingingArm))
                && player.distanceToSqr(mc.player) < 4096) {
                DemoSlashes.spawn(player);
            }
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            HeldItemTrails.beginFrame(event);
            return;
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            BladeFlashRenderer.INSTANCE.captureView(event);
            CoffinVisuals.captureView(event);
            return;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        if (Minecraft.getInstance().level == null) return;
        DemoSlashes.update();
        CoffinVisuals.render(event);
        BladeFlashRenderer.INSTANCE.render(event);
        HeldItemTrails.prepareFirstPerson(event);
    }
}
